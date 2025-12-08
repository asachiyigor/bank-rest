package com.example.bankcards.controller;

import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String userToken;
    private String adminToken;
    private Long userId;

    @BeforeEach
    void setUp() throws Exception {
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_USER).build()));

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_ADMIN).build()));

        RegisterRequest registerRequest = new RegisterRequest("carduser", "carduser@example.com", "password123", "Card User");

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

        User adminUser = User.builder()
                .username("cardadmin")
                .email("cardadmin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Card Admin")
                .roles(Set.of(adminRole))
                .createdAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        adminToken = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(adminUser.getUsername())
                        .password(adminUser.getPassword())
                        .authorities("ROLE_ADMIN")
                        .build()
        );
    }

    @Test
    void createCard_ValidRequest_ReturnsCreatedCard() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cardNumber").value("**** **** **** 3456"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createCard_InvalidCardNumber_ReturnsBadRequest() throws Exception {
        CardRequest request = new CardRequest("123", LocalDate.now().plusYears(2), userId);

        mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCard_NoAuth_ReturnsUnauthorized() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getCardById_ExistingCard_ReturnsCard() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(get("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.cardNumber").exists());
    }

    @Test
    void getCardById_NonExistingCard_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/cards/999999")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserCards_ReturnsUserCards() throws Exception {
        CardRequest request1 = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)));

        CardRequest request2 = new CardRequest("9876543210987654", LocalDate.now().plusYears(3), userId);

        mockMvc.perform(post("/api/cards")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)));

        mockMvc.perform(get("/api/cards/user/" + userId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void blockCard_ExistingCard_ReturnsSuccess() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(put("/api/cards/" + cardId + "/block")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"));
    }

    @Test
    void blockCard_NonExistingCard_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/api/cards/999999/block")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void activateCard_AsAdmin_Success() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(put("/api/cards/" + cardId + "/block")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/cards/" + cardId + "/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void activateCard_AsRegularUser_Forbidden() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(put("/api/cards/" + cardId + "/activate")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void activateCard_NonExistingCard_NotFound() throws Exception {
        mockMvc.perform(put("/api/cards/999999/activate")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_AsAdmin_Success() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(delete("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCard_AsRegularUser_Forbidden() throws Exception {
        CardRequest request = new CardRequest("1234567890123456", LocalDate.now().plusYears(2), userId);

        MvcResult createResult = mockMvc.perform(post("/api/cards")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long cardId = objectMapper.readTree(responseBody).get("id").asLong();

        mockMvc.perform(delete("/api/cards/" + cardId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCard_NonExistingCard_NotFound() throws Exception {
        mockMvc.perform(delete("/api/cards/999999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
