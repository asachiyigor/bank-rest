package com.example.bankcards.controller;

import com.example.bankcards.dto.RegisterRequest;
import com.example.bankcards.dto.UpdateUserRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private String adminToken;
    private User adminUser;
    private User testUser;

    @BeforeEach
    void setUp() {
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_ADMIN).build()));
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseGet(() -> roleRepository.save(Role.builder().name(Role.RoleName.ROLE_USER).build()));

        adminUser = User.builder()
                .username("admintest")
                .email("admintest@example.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("Admin Test User")
                .roles(Set.of(adminRole))
                .createdAt(LocalDateTime.now())
                .build();
        adminUser = userRepository.save(adminUser);

        testUser = User.builder()
                .username("testuser")
                .email("testuser@example.com")
                .password(passwordEncoder.encode("user123"))
                .fullName("Test User")
                .roles(Set.of(userRole))
                .createdAt(LocalDateTime.now())
                .build();
        testUser = userRepository.save(testUser);

        adminToken = jwtUtil.generateToken(
                org.springframework.security.core.userdetails.User.builder()
                        .username(adminUser.getUsername())
                        .password(adminUser.getPassword())
                        .authorities("ROLE_ADMIN")
                        .build()
        );
    }

    @Test
    void getAllUsers_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void getAllUsers_WithoutAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_AsAdmin_Success() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testUser.getId()))
                .andExpect(jsonPath("$.username").value(testUser.getUsername()))
                .andExpect(jsonPath("$.email").value(testUser.getEmail()))
                .andExpect(jsonPath("$.fullName").value(testUser.getFullName()));
    }

    @Test
    void getUserById_NonExistentUser_NotFound() throws Exception {
        mockMvc.perform(get("/api/users/{userId}", 999999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateUser_DuplicateEmail_BadRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                adminUser.getEmail(),
                null,
                null
        );

        mockMvc.perform(put("/api/users/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_InvalidEmail_BadRequest() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest(
                "invalid-email",
                null,
                null
        );

        mockMvc.perform(put("/api/users/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_AsAdmin_Success() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", testUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_NonExistentUser_NotFound() throws Exception {
        mockMvc.perform(delete("/api/users/{userId}", 999999L)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
