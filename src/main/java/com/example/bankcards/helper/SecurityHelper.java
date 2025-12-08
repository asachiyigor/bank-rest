package com.example.bankcards.helper;

import com.example.bankcards.constants.ErrorMessages;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.ResourceNotFoundException;
import com.example.bankcards.exception.UnauthorizedException;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityHelper {

    private final UserRepository userRepository;

    public User getCurrentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorMessages.CURRENT_USER_NOT_FOUND));
    }

    public void validateCardOwnership(Card card, User user) {
        if (!card.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException(ErrorMessages.UNAUTHORIZED_CARD_ACTION);
        }
    }

    public void validateUserAccess(Authentication authentication, Long userId) {
        if (!hasAccess(authentication, userId)) {
            throw new UnauthorizedException(ErrorMessages.UNAUTHORIZED_VIEW_CARDS);
        }
    }

    public boolean hasAccess(Authentication authentication, Long userId) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return true;
        }
        User currentUser = getCurrentUser(authentication);
        return currentUser.getId().equals(userId);
    }

    public boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }
}
