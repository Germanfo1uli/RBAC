package com.rbac.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create a valid user")
    void shouldCreateValidUser() {
        User user = User.validate("alex_smith", "Alex Smith", "alex.smith@company.com");

        assertThat(user.username()).isEqualTo("alex_smith");
        assertThat(user.fullName()).isEqualTo("Alex Smith");
        assertThat(user.email()).isEqualTo("alex.smith@company.com");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ab",
            "user name with space",
            "кириллица123",
            "veryveryveryveryveryverylongusername123"
    })
    @DisplayName("Should reject invalid usernames")
    void shouldRejectInvalidUsername(String invalidUsername) {
        assertThatThrownBy(() -> User.validate(invalidUsername, "Name", "test@ex.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "test@", "@domain.com", "test@domain", "test@.com"})
    @DisplayName("Should reject invalid emails")
    void shouldRejectInvalidEmail(String invalidEmail) {
        assertThatThrownBy(() -> User.validate("validuser", "Name", invalidEmail))
                .isInstanceOf(IllegalArgumentException.class);
    }
}