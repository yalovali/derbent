package tech.derbent.activities.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CActivityStatus class.
 * Tests the activity status entity functionality.
 */
@DisplayName("CActivityStatus Tests")
class CActivityStatusTest {

    private CActivityStatus status;

    @BeforeEach
    void setUp() {
        status = new CActivityStatus();
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
    @DisplayName("Should create with name constructor")
    void shouldCreateWithNameConstructor() {
        CActivityStatus namedStatus = new CActivityStatus("TODO");
        
        assertEquals("TODO", namedStatus.getName());
        assertNull(namedStatus.getDescription());
        assertEquals("#808080", namedStatus.getColor());
        assertFalse(namedStatus.isFinal());
    }

    @Test
    @DisplayName("Should create with name and description constructor")
    void shouldCreateWithNameAndDescriptionConstructor() {
        CActivityStatus detailedStatus = new CActivityStatus("IN_PROGRESS", "Task is being worked on");
        
        assertEquals("IN_PROGRESS", detailedStatus.getName());
        assertEquals("Task is being worked on", detailedStatus.getDescription());
        assertEquals("#808080", detailedStatus.getColor());
        assertFalse(detailedStatus.isFinal());
    }

    @Test
    @DisplayName("Should create with full constructor")
    void shouldCreateWithFullConstructor() {
        CActivityStatus fullStatus = new CActivityStatus("DONE", "Task completed", "#00AA00", true);
        
        assertEquals("DONE", fullStatus.getName());
        assertEquals("Task completed", fullStatus.getDescription());
        assertEquals("#00AA00", fullStatus.getColor());
        assertTrue(fullStatus.isFinal());
    }

    @Test
    @DisplayName("Should handle null name gracefully")
    void shouldHandleNullNameGracefully() {
        CActivityStatus nullNameStatus = new CActivityStatus(null);
        
        assertNull(nullNameStatus.getName());
        // Should not throw exception - validation happens at service layer
    }

    @Test
    @DisplayName("Should handle null color gracefully")
    void shouldHandleNullColorGracefully() {
        CActivityStatus nullColorStatus = new CActivityStatus("TEST", "Test status", null, false);
        
        assertEquals("#808080", nullColorStatus.getColor()); // Should default to gray
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
    @DisplayName("Should handle null sort order gracefully")
    void shouldHandleNullSortOrderGracefully() {
        status.setSortOrder(null);
        
        assertEquals(100, status.getSortOrder()); // Should default to 100
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
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        CActivityStatus status1 = new CActivityStatus("TODO", "Task ready");
        CActivityStatus status2 = new CActivityStatus("TODO", "Task ready");
        
        // Since these extend CTypeEntity, equality is based on ID
        // For new entities without ID, they should not be equal
        assertNotEquals(status1, status2);
        
        // Test with same instance
        assertEquals(status1, status1);
        
        // Test with null
        assertNotEquals(status1, null);
        
        // Test with different type
        assertNotEquals(status1, "not a status");
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
    @DisplayName("Should validate typical workflow statuses")
    void shouldValidateTypicalWorkflowStatuses() {
        // Test typical workflow statuses
        String[][] typicalStatuses = {
            {"TODO", "Task ready to start", "#808080", "false"},
            {"IN_PROGRESS", "Task being worked on", "#007ACC", "false"},
            {"REVIEW", "Task under review", "#FFA500", "false"},
            {"BLOCKED", "Task is blocked", "#FF4444", "false"},
            {"DONE", "Task completed", "#00AA00", "true"},
            {"CANCELLED", "Task cancelled", "#888888", "true"}
        };

        for (String[] statusData : typicalStatuses) {
            CActivityStatus testStatus = new CActivityStatus(
                statusData[0], 
                statusData[1], 
                statusData[2], 
                Boolean.parseBoolean(statusData[3])
            );
            
            assertEquals(statusData[0], testStatus.getName());
            assertEquals(statusData[1], testStatus.getDescription());
            assertEquals(statusData[2], testStatus.getColor());
            assertEquals(Boolean.parseBoolean(statusData[3]), testStatus.isFinal());
        }

    }
}