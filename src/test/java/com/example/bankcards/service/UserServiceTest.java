package com.example.bankcards.service;

import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setPassword("encodedPassword");

        testUserResponse = new UserResponse(1L, "testuser", "test@example.com", "Test User", null, null);

        updateRequest = new UpdateUserRequest("newemail@example.com", "New Name", "newPassword");
    }

    @Test
    void getAllUsers_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));

        when(userRepository.findAll(pageable)).thenReturn(userPage);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("testuser", result.getContent().get(0).username());
        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, times(1)).toResponse(testUser);
    }

    @Test
    void getAllUsers_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<User> emptyPage = Page.empty();

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<UserResponse> result = userService.getAllUsers(pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        verify(userRepository, times(1)).findAll(pageable);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.username());
        assertEquals("test@example.com", result.email());
        verify(userRepository, times(1)).findById(1L);
        verify(userMapper, times(1)).toResponse(testUser);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.getUserById(999L)
        );

        verify(userRepository, times(1)).findById(999L);
        verify(userMapper, never()).toResponse(any());
    }

    @Test
    void updateUser_AllFields_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, updateRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("newemail@example.com");
        verify(passwordEncoder, times(1)).encode("newPassword");
        verify(userRepository, times(1)).save(testUser);
        assertEquals("newemail@example.com", testUser.getEmail());
        assertEquals("New Name", testUser.getFullName());
        assertEquals("encodedNewPassword", testUser.getPassword());
    }

    @Test
    void updateUser_EmailOnly_Success() {
        UpdateUserRequest emailOnlyRequest = new UpdateUserRequest("newemail@example.com", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, emailOnlyRequest);

        assertNotNull(result);
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals("newemail@example.com", testUser.getEmail());
        assertEquals("Test User", testUser.getFullName());
    }

    @Test
    void updateUser_FullNameOnly_Success() {
        UpdateUserRequest nameOnlyRequest = new UpdateUserRequest(null, "New Name Only", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, nameOnlyRequest);

        assertNotNull(result);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        assertEquals("New Name Only", testUser.getFullName());
        assertEquals("test@example.com", testUser.getEmail());
    }

    @Test
    void updateUser_PasswordOnly_Success() {
        UpdateUserRequest passwordOnlyRequest = new UpdateUserRequest(null, null, "newPassword123");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword123");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, passwordOnlyRequest);

        assertNotNull(result);
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(userRepository, never()).existsByEmail(anyString());
        assertEquals("encodedNewPassword123", testUser.getPassword());
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("newemail@example.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () ->
                userService.updateUser(1L, updateRequest)
        );

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail("newemail@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_SameEmail_Success() {
        UpdateUserRequest sameEmailRequest = new UpdateUserRequest("test@example.com", "Updated Name", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(1L, sameEmailRequest);

        assertNotNull(result);
        verify(userRepository, times(1)).save(testUser);
        assertEquals("test@example.com", testUser.getEmail());
        assertEquals("Updated Name", testUser.getFullName());
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.updateUser(999L, updateRequest)
        );

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).delete(testUser);
    }

    @Test
    void deleteUser_UserNotFound_ThrowsException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                userService.deleteUser(999L)
        );

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).delete(any());
    }
}
