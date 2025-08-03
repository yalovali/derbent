package tech.derbent.activities.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.projects.domain.CProject;

/**
 * Unit tests for CActivityStatus class. Tests the activity status entity functionality.
 */
@DisplayName("CActivityStatus Tests")
class CActivityStatusTest extends CTestBase {

    private CActivityStatus status;

    private CProject project;

    @Override
    protected void setupForTest() {
        status = new CActivityStatus("test status", project);
    }

    @Test
    @DisplayName("Should create with default constructor")
    void shouldCreateWithDefaultConstructor() {
        assertNotNull(status);
        assertNull(status.getName());
        assertNull(status.getDescription());
        assertEquals("#808080", status.getColor()); // Default gray color
        assertFalse(status.isFinal());
        assertEquals(100, status.getSortOrder()); // Default sort order
    }

    @Test
    @DisplayName("Should create with full constructor")
    void shouldCreateWithFullConstructor() {
        final String name = "DONE";
        final String description = "Task completed successfully";
        final String color = "#00cc00";
        final boolean isFinal = true;
        // When
        final CActivityStatus status = new CActivityStatus(name, project, description, color, isFinal);
        // Then
        assertNotNull(status);
        assertEquals(name, status.getName());
        assertEquals(description, status.getDescription());
        assertEquals(project, status.getProject());
        assertEquals(isFinal, status.isFinal());
    }

    @Test
    @DisplayName("Should create with name constructor")
    void shouldCreateWithNameConstructor() {
        final String name = "TODO";
        // When
        final CActivityStatus status = new CActivityStatus(name, project);
        // Then
        assertNotNull(status);
        assertEquals(name, status.getName());
        assertEquals(project, status.getProject());
        assertFalse(status.isFinal()); // Default should be false
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        final CActivityStatus status1 = new CActivityStatus("TODO", project);
        final CActivityStatus status2 = new CActivityStatus("TODO", project);
        final CActivityStatus status3 = new CActivityStatus("DONE", project);
        // When & Then
        assertEquals(status1, status2, "Statuses with same name and project should be equal");
        assertNotEquals(status1, status3, "Statuses with different names should not be equal");
        assertEquals(status1.hashCode(), status2.hashCode(), "Equal objects should have same hash code");
        // Test reflexivity
        assertEquals(status1, status1, "Status should equal itself");
        // Test null and different class
        assertNotEquals(status1, null, "Status should not equal null");
        assertNotEquals(status1, "not a status", "Status should not equal different class");
    }

    @Test
    @DisplayName("Should handle final status flag correctly")
    void shouldHandleFinalStatusFlagCorrectly() {
        // Initially not final
        assertFalse(status.isFinal());
        // Set to final
        status.setFinal(true);
        assertTrue(status.isFinal());
        // Set back to non-final
        status.setFinal(false);
        assertFalse(status.isFinal());
    }

    @Test
    @DisplayName("Should handle null color gracefully")
    void shouldHandleNullColorGracefully() {
        final CActivityStatus nullColorStatus = new CActivityStatus("TEST", null, "Test status", null, false);
        assertEquals("#808080", nullColorStatus.getColor()); // Should default to gray
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void shouldHandleNullNameGracefully() {
        final CActivityStatus nullNameStatus = new CActivityStatus(null, null);
        assertNull(nullNameStatus.getName());
        // Should not throw exception - validation happens at service layer
    }

    @Test
    @DisplayName("Should handle null sort order gracefully")
    void shouldHandleNullSortOrderGracefully() {
        status.setSortOrder(null);
        assertEquals(100, status.getSortOrder()); // Should default to 100
    }

    @Test
    @DisplayName("Should maintain color consistency")
    void shouldMaintainColorConsistency() {
        // Test color setting with null
        status.setColor(null);
        assertEquals("#808080", status.getColor());
        // Test color setting with valid value
        status.setColor("#FF0000");
        assertEquals("#FF0000", status.getColor());
        // Test color setting with empty string
        status.setColor("");
        assertEquals("#808080", status.getColor()); // Should default to gray
    }

    @Test
    @DisplayName("Should provide meaningful toString")
    void shouldProvideMeaningfulToString() {
        status.setName("BLOCKED");
        assertEquals("BLOCKED", status.toString());
        // Test with null name
        status.setName(null);
        assertTrue(status.toString().contains("CActivityStatus"));
    }

    @Test
    @DisplayName("Should set and get properties correctly")
    void shouldSetAndGetPropertiesCorrectly() {
        status.setName("REVIEW");
        status.setDescription("Under review");
        status.setColor("#FFA500");
        status.setFinal(false);
        status.setSortOrder(3);
        assertEquals("REVIEW", status.getName());
        assertEquals("Under review", status.getDescription());
        assertEquals("#FFA500", status.getColor());
        assertFalse(status.isFinal());
        assertEquals(3, status.getSortOrder());
    }

    @Test
    @DisplayName("Should validate typical workflow statuses")
    void shouldValidateTypicalWorkflowStatuses() {
        // todo write test
    }
}