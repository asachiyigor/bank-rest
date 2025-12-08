package com.example.bankcards.helper;

import org.slf4j.Logger;

public final class LogHelper {

    private LogHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static void logOperationStart(Logger log, String operation, Object... params) {
        log.info("{} Starting operation - {}", operation, formatParams(params));
    }

    public static void logOperationSuccess(Logger log, String operation, Object... params) {
        log.info("{} Operation completed successfully - {}", operation, formatParams(params));
    }

    public static void logOperation(Logger log, String operation, String message, Object... params) {
        log.info("{} {} - {}", operation, message, formatParams(params));
    }

    private static String formatParams(Object... params) {
        if (params.length == 0) {
            return "";
        }
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must be key-value pairs");
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i += 2) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(params[i]).append("=").append(params[i + 1]);
        }
        return sb.toString();
    }
}
