package com.example.bankcards.constants;

public final class LogConstants {

    private LogConstants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String TRANSFER_CREATE = "[TRANSFER_CREATE]";
    public static final String TRANSFER_GET = "[TRANSFER_GET]";
    public static final String TRANSFER_HISTORY = "[TRANSFER_HISTORY]";
    public static final String TRANSFER_USER_HISTORY = "[TRANSFER_USER_HISTORY]";

    public static final String CARD_CREATE = "[CARD_CREATE]";
    public static final String CARD_GET = "[CARD_GET]";
    public static final String CARD_GET_ALL = "[CARD_GET_ALL]";
    public static final String CARD_BLOCK = "[CARD_BLOCK]";
    public static final String CARD_ACTIVATE = "[CARD_ACTIVATE]";
    public static final String CARD_DELETE = "[CARD_DELETE]";

    public static final String AUTH_LOGIN = "[AUTH_LOGIN]";
    public static final String AUTH_REGISTER = "[AUTH_REGISTER]";

    public static final String USER_GET_ALL = "[USER_GET_ALL]";
    public static final String USER_GET = "[USER_GET]";
    public static final String USER_UPDATE = "[USER_UPDATE]";
    public static final String USER_DELETE = "[USER_DELETE]";
}
