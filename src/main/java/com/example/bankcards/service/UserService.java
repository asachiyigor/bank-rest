package com.example.bankcards.service;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.constants.LogConstants;
import com.example.bankcards.dto.UpdateUserRequest;
import com.example.bankcards.dto.UserResponse;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.BadRequestException;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.helper.LogHelper;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        LogHelper.logOperation(log, LogConstants.USER_GET_ALL, "Getting all users",
                "page", pageable.getPageNumber(),
                "size", pageable.getPageSize());

        Page<User> users = userRepository.findAll(pageable);

        LogHelper.logOperation(log, LogConstants.USER_GET_ALL, "Found users",
                "count", users.getTotalElements());

        return users.map(userMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        LogHelper.logOperation(log, LogConstants.USER_GET, "Getting user by id",
                "userId", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        LogHelper.logOperation(log, LogConstants.USER_GET, "User found",
                "userId", user.getId(),
                "username", user.getUsername());

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        LogHelper.logOperationStart(log, LogConstants.USER_UPDATE,
                "userId", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        if (request.email() != null) {
            if (userRepository.existsByEmail(request.email()) &&
                    !user.getEmail().equals(request.email())) {
                throw new BadRequestException("Email already exists");
            }
            user.setEmail(request.email());
        }

        if (request.fullName() != null) {
            user.setFullName(request.fullName());
        }

        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        userRepository.save(user);

        LogHelper.logOperationSuccess(log, LogConstants.USER_UPDATE,
                "userId", user.getId(),
                "username", user.getUsername());

        return userMapper.toResponse(user);
    }

    public void deleteUser(Long userId) {
        LogHelper.logOperationStart(log, LogConstants.USER_DELETE,
                "userId", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.USER_NOT_FOUND));

        userRepository.delete(user);

        LogHelper.logOperationSuccess(log, LogConstants.USER_DELETE,
                "userId", userId,
                "username", user.getUsername());
    }
}
