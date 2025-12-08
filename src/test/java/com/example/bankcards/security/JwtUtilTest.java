package com.example.bankcards.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "MyTestSecretKeyForJWTTokenGenerationAndValidation123456789");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    void generateToken_ValidUserDetails_ReturnsToken() {
        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        String token = jwtUtil.generateToken(userDetails);

        String extractedUsername = jwtUtil.extractUsername(token);

        assertEquals("testuser", extractedUsername);
    }

    @Test
    void extractExpiration_ValidToken_ReturnsExpirationDate() {
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.extractExpiration(token);

        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void validateToken_ValidTokenSameUser_ReturnsTrue() {
        String token = jwtUtil.generateToken(userDetails);

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    void validateToken_ValidTokenDifferentUser_ReturnsFalse() {
        String token = jwtUtil.generateToken(userDetails);

        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        Boolean isValid = jwtUtil.validateToken(token, differentUser);

        assertFalse(isValid);
    }

    @Test
    void validateToken_ExpiredToken_ReturnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L);
        String token = jwtUtil.generateToken(userDetails);

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        String malformedToken = "this.is.not.a.valid.jwt.token";

        Boolean isValid = jwtUtil.validateToken(malformedToken, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        Boolean isValid = jwtUtil.validateToken(null, userDetails);

        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        Boolean isValid = jwtUtil.validateToken("", userDetails);

        assertFalse(isValid);
    }

    @Test
    void extractUsername_MalformedToken_ThrowsException() {
        String malformedToken = "invalid.token.format";

        assertThrows(JwtException.class, () -> {
            jwtUtil.extractUsername(malformedToken);
        });
    }

    @Test
    void extractExpiration_MalformedToken_ThrowsException() {
        String malformedToken = "invalid.token.format";

        assertThrows(JwtException.class, () -> {
            jwtUtil.extractExpiration(malformedToken);
        });
    }

    @Test
    void generateToken_DifferentUsers_GeneratesDifferentTokens() {
        String token1 = jwtUtil.generateToken(userDetails);

        UserDetails anotherUser = User.builder()
                .username("anotheruser")
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        String token2 = jwtUtil.generateToken(anotherUser);

        assertNotEquals(token1, token2);
    }

    @Test
    void generateToken_SameUserMultipleCalls_GeneratesDifferentTokens() {
        String token1 = jwtUtil.generateToken(userDetails);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        String token2 = jwtUtil.generateToken(userDetails);

        assertNotEquals(token1, token2);
    }

    @Test
    void validateToken_TokenFromDifferentSecret_ReturnsFalse() {
        String token = jwtUtil.generateToken(userDetails);

        ReflectionTestUtils.setField(jwtUtil, "secret", "DifferentSecretKeyForJWTTokenGeneration123456789012345");

        Boolean isValid = jwtUtil.validateToken(token, userDetails);

        assertFalse(isValid);
    }

    @Test
    void extractClaim_CustomClaim_Works() {
        String token = jwtUtil.generateToken(userDetails);

        String subject = jwtUtil.extractClaim(token, claims -> claims.getSubject());

        assertEquals("testuser", subject);
    }

    @Test
    void extractClaim_IssuedAtClaim_ReturnsDate() {
        String token = jwtUtil.generateToken(userDetails);

        Date issuedAt = jwtUtil.extractClaim(token, claims -> claims.getIssuedAt());

        assertNotNull(issuedAt);
        assertTrue(issuedAt.before(new Date()) || issuedAt.equals(new Date()));
    }
}
