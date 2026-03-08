package com.rbac.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void shouldNormalizeString() {
        assertEquals("user_admin", ValidationUtils.normalizeString("  USER_ADMIN  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user123", "admin.test", "john_doe"})
    void shouldValidateCorrectUsernames(String username) {
        assertTrue(ValidationUtils.isValidUsername(username));
    }

    @Test
    void shouldThrowExceptionWhenFieldIsEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty("", "Username"));
    }
}