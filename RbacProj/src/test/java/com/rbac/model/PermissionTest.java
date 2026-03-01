package com.rbac.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class PermissionTest {

    @Test
    void shouldCreateValidPermission() {
        Permission p = new Permission("READ", "documents", "Read access to documents");

        assertThat(p.name()).isEqualTo("READ");
        assertThat(p.resource()).isEqualTo("documents");
        assertThat(p.description()).isEqualTo("Read access to documents");
    }

    @Test
    void shouldRejectLowercasePermissionName() {
        assertThatThrownBy(() -> new Permission("read", "docs", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("uppercase");
    }

    @Test
    void shouldRejectUppercaseResource() {
        assertThatThrownBy(() -> new Permission("READ", "DOCS", "desc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("lowercase");
    }

    @Test
    void shouldRejectEmptyFields() {
        assertThatThrownBy(() -> new Permission("", "docs", "desc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Permission("READ", "", "desc"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Permission("READ", "docs", ""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}