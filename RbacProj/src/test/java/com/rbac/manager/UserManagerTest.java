package com.rbac.manager;

import com.rbac.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class UserManagerTest {

    private UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void shouldAddAndFindUserByUsername() {
        User u = User.validate("alice", "Alice Smith", "alice@ex.com");
        manager.add(u);

        assertThat(manager.exists("alice")).isTrue();
        assertThat(manager.findByUsername("alice")).isPresent();
        assertThat(manager.findByUsername("bob")).isEmpty();
    }

    @Test
    void shouldPreventDuplicateUsername() {
        User u1 = User.validate("alice", "Alice", "a@ex.com");
        User u2 = User.validate("alice", "Another", "b@ex.com");

        manager.add(u1);

        assertThatThrownBy(() -> manager.add(u2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldUpdateUserDetails() {
        User u = User.validate("alice", "Alice", "old@ex.com");
        manager.add(u);

        manager.update("alice", "Alice Updated", "new@ex.com");

        User updated = manager.findByUsername("alice").orElseThrow();
        assertThat(updated.fullName()).isEqualTo("Alice Updated");
        assertThat(updated.email()).isEqualTo("new@ex.com");
    }
}