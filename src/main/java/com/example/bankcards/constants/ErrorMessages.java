package com.example.bankcards.constants;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String TRANSFER_NOT_FOUND = "Transfer not found";
    public static final String TRANSFER_SAME_CARD = "Cannot transfer to the same card";
    public static final String SOURCE_CARD_NOT_FOUND = "Source card not found";
    public static final String DESTINATION_CARD_NOT_FOUND = "Destination card not found";
    public static final String INSUFFICIENT_BALANCE = "Insufficient balance on source card";
    public static final String SOURCE_CARD_NOT_ACTIVE = "Source card is not active";
    public static final String DESTINATION_CARD_NOT_ACTIVE = "Destination card is not active";
    public static final String UNAUTHORIZED_TRANSFER_FROM = "You can only transfer from your own cards";
    public static final String UNAUTHORIZED_TRANSFER_TO = "You can only transfer to your own cards";
    public static final String UNAUTHORIZED_VIEW_TRANSFER = "You don't have permission to view this transfer";
    public static final String UNAUTHORIZED_VIEW_TRANSFER_HISTORY = "You can only view transfer history for your own cards";
    public static final String UNAUTHORIZED_VIEW_USER_HISTORY = "You can only view your own transfer history";

    public static final String CARD_NOT_FOUND = "Card not found";
    public static final String USER_NOT_FOUND = "User not found";
    public static final String CURRENT_USER_NOT_FOUND = "Current user not found";
    public static final String CARD_NUMBER_EXISTS = "Card number already exists";
    public static final String UNAUTHORIZED_CREATE_CARD = "You don't have permission to create card for this user";
    public static final String UNAUTHORIZED_VIEW_CARDS = "You don't have permission to view these cards";
    public static final String UNAUTHORIZED_CARD_ACTION = "You don't have permission to perform this action on this card";

    public static final String INVALID_CREDENTIALS = "Invalid username or password";
    public static final String USERNAME_EXISTS = "Username already exists";
    public static final String EMAIL_EXISTS = "Email already exists";
    public static final String ROLE_NOT_FOUND = "Role not found";

    public static final String INVALID_TOKEN = "Invalid or expired token";
    public static final String ACCESS_DENIED = "Access denied";
}
