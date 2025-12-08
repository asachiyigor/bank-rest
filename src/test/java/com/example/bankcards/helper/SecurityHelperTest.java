package com.example.bankcards.helper;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SecurityHelperTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityHelper securityHelper;

    private User testUser;
    private User otherUser;
    private Card userCard;
    private Card otherUserCard;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .roles(new HashSet<>())
                .build();

        otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .email("other@example.com")
                .roles(new HashSet<>())
                .build();

        userCard = Card.builder()
                .id(1L)
                .user(testUser)
                .build();

        otherUserCard = Card.builder()
                .id(2L)
                .user(otherUser)
                .build();
    }

    @Test
    void getCurrentUser_Success() {
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User result = securityHelper.getCurrentUser(authentication);

        assertEquals(testUser, result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void getCurrentUser_UserNotFound_ThrowsException() {
        when(authentication.getName()).thenReturn("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                securityHelper.getCurrentUser(authentication)
        );

        assertEquals(ErrorMessages.CURRENT_USER_NOT_FOUND, exception.getMessage());
    }

    @Test
    void validateCardOwnership_Success() {
        assertDoesNotThrow(() ->
                securityHelper.validateCardOwnership(userCard, testUser)
        );
    }

    @Test
    void validateCardOwnership_Unauthorized_ThrowsException() {
        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                securityHelper.validateCardOwnership(otherUserCard, testUser)
        );

        assertEquals(ErrorMessages.UNAUTHORIZED_CARD_ACTION, exception.getMessage());
    }

    @Test
    void validateUserAccess_AsOwner_Success() {
        when(authentication.getName()).thenReturn("testuser");
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        assertDoesNotThrow(() ->
                securityHelper.validateUserAccess(authentication, testUser.getId())
        );
    }

    @Test
    void validateUserAccess_AsAdmin_Success() {
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        assertDoesNotThrow(() ->
                securityHelper.validateUserAccess(authentication, otherUser.getId())
        );
    }

    @Test
    void validateUserAccess_Unauthorized_ThrowsException() {
        when(authentication.getName()).thenReturn("testuser");
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () ->
                securityHelper.validateUserAccess(authentication, otherUser.getId())
        );

        assertEquals(ErrorMessages.UNAUTHORIZED_VIEW_CARDS, exception.getMessage());
    }

    @Test
    void hasAccess_AsOwner_ReturnsTrue() {
        when(authentication.getName()).thenReturn("testuser");
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = securityHelper.hasAccess(authentication, testUser.getId());

        assertTrue(result);
    }

    @Test
    void hasAccess_AsAdmin_ReturnsTrue() {
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = securityHelper.hasAccess(authentication, otherUser.getId());

        assertTrue(result);
    }

    @Test
    void hasAccess_AsOtherUser_ReturnsFalse() {
        when(authentication.getName()).thenReturn("testuser");
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        boolean result = securityHelper.hasAccess(authentication, otherUser.getId());

        assertFalse(result);
    }

    @Test
    void isAdmin_WithAdminRole_ReturnsTrue() {
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = securityHelper.isAdmin(authentication);

        assertTrue(result);
    }

    @Test
    void isAdmin_WithUserRole_ReturnsFalse() {
        Collection<GrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = securityHelper.isAdmin(authentication);

        assertFalse(result);
    }

    @Test
    void isAdmin_WithMultipleRoles_ReturnsTrue() {
        Collection<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);

        boolean result = securityHelper.isAdmin(authentication);

        assertTrue(result);
    }
}
