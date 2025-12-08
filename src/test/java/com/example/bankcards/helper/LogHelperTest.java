package com.example.bankcards.helper;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

class LogHelperTest {

    private static final Logger log = LoggerFactory.getLogger(LogHelperTest.class);

    @Test
    void constructor_ThrowsUnsupportedOperationException() throws Exception {
        var constructor = LogHelper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        var exception = assertThrows(java.lang.reflect.InvocationTargetException.class,
            constructor::newInstance);
        assertInstanceOf(UnsupportedOperationException.class, exception.getCause());
    }

    @Test
    void logOperationStart_WithParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationStart(log, "[TEST_OP]", "key1", "value1", "key2", "value2")
        );
    }

    @Test
    void logOperationStart_WithEmptyParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationStart(log, "[TEST_OP]")
        );
    }

    @Test
    void logOperationSuccess_WithParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationSuccess(log, "[TEST_OP]", "userId", 123L, "status", "completed")
        );
    }

    @Test
    void logOperationSuccess_WithEmptyParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationSuccess(log, "[TEST_OP]")
        );
    }

    @Test
    void logOperation_WithParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperation(log, "[TEST_OP]", "Custom message", "param1", "val1")
        );
    }

    @Test
    void logOperation_WithEmptyParameters_Success() {
        assertDoesNotThrow(() ->
                LogHelper.logOperation(log, "[TEST_OP]", "Custom message")
        );
    }

    @Test
    void formatParams_WithOddNumberOfParams_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                LogHelper.logOperationStart(log, "[TEST_OP]", "key1", "value1", "key2")
        );
    }

    @Test
    void formatParams_WithMultipleKeyValuePairs_FormatsCorrectly() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationStart(log, "[TEST_OP]",
                        "key1", "value1",
                        "key2", 123,
                        "key3", true)
        );
    }

    @Test
    void formatParams_WithNullValues_HandlesGracefully() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationStart(log, "[TEST_OP]", "key1", null, "key2", "value2")
        );
    }

    @Test
    void formatParams_WithDifferentTypes_HandlesCorrectly() {
        assertDoesNotThrow(() ->
                LogHelper.logOperationSuccess(log, "[TEST_OP]",
                        "stringKey", "stringValue",
                        "intKey", 42,
                        "longKey", 123L,
                        "boolKey", true)
        );
    }
}
