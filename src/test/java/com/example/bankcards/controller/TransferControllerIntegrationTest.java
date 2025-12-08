package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TransferControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CardRepository cardRepository;

    private String userToken;
    private Long userId;
    private Long fromCardId;
    private Long toCardId;

    @BeforeEach
    void setUp() throws Exception {
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            Role userRole = Role.builder()
                    .name(Role.RoleName.ROLE_USER)
                    .build();
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = Role.builder()
                    .name(Role.RoleName.ROLE_ADMIN)
                    .build();
            roleRepository.save(adminRole);
        }

        RegisterRequest registerRequest = new RegisterRequest("transferuser", "transferuser@example.com", "password123", "Transfer User");

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andReturn();

        AuthResponse authResponse = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AuthResponse.class
        );
        userToken = authResponse.token();
        userId = authResponse.id();

        CardRequest card1Request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult card1Result = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card1Request)))
                .andReturn();

        fromCardId = objectMapper.readTree(card1Result.getResponse().getContentAsString())
                .get("id").asLong();

        CardRequest card2Request = new CardRequest("9876543210987654", LocalDate.now().plusYears(2), userId);

        MvcResult card2Result = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(card2Request)))
                .andReturn();

        toCardId = objectMapper.readTree(card2Result.getResponse().getContentAsString())
                .get("id").asLong();

        Card fromCard = cardRepository.findById(fromCardId).orElseThrow();
        fromCard.setBalance(new BigDecimal("1000.00"));
        cardRepository.save(fromCard);

        Card toCard = cardRepository.findById(toCardId).orElseThrow();
        toCard.setBalance(new BigDecimal("1000.00"));
        cardRepository.save(toCard);
    }

    @Test
    void transfer_ValidRequest_ReturnsSuccess() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("100.00"), "Test transfer");

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fromCardId").value(fromCardId))
                .andExpect(jsonPath("$.toCardId").value(toCardId))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    void transfer_SameCard_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, fromCardId, new BigDecimal("100.00"), "Invalid transfer");

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_InsufficientBalance_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("100000.00"), "Too much money");

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_NegativeAmount_ReturnsBadRequest() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("-100.00"), "Negative amount");

        mockMvc.perform(post("/api/transfers")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transfer_NoAuth_ReturnsUnauthorized() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("100.00"), "Unauthorized transfer");

        mockMvc.perform(post("/api/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCardTransferHistory_ReturnsTransfers() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("50.00"), "First transfer");

        mockMvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        TransferRequest request2 = new TransferRequest(fromCardId, toCardId, new BigDecimal("30.00"), "Second transfer");

        mockMvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/transfers/card/" + fromCardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getUserTransferHistory_ReturnsTransfers() throws Exception {
        TransferRequest request = new TransferRequest(fromCardId, toCardId, new BigDecimal("25.00"), "User transfer");

        mockMvc.perform(post("/api/transfers")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        mockMvc.perform(get("/api/transfers/user/" + userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getCardTransferHistory_NonExistingCard_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/transfers/card/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserTransferHistory_NonExistingUser_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/transfers/user/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }
}
