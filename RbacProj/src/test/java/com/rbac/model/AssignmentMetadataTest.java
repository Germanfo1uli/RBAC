package com.rbac.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class AssignmentMetadataTest {

    @Test
    void shouldCreateMetadataWithNow() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test assignment");

        assertThat(meta.assignedBy()).isEqualTo("admin");
        assertThat(meta.reason()).isEqualTo("Test assignment");
        assertThat(meta.assignedAt()).matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    void shouldCreateMetadataWithoutReason() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin");

        assertThat(meta.assignedBy()).isEqualTo("admin");
        assertThat(meta.reason()).isNull();
        assertThat(meta.hasReason()).isFalse();
    }

    @Test
    void shouldRejectNullOrEmptyAssignedBy() {
        assertThatThrownBy(() -> new AssignmentMetadata(null, "2025-01-01 12:00:00", "reason"))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new AssignmentMetadata("", "2025-01-01 12:00:00", "reason"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}