package tech.derbent.comments.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CCommentPriority domain class.
 * Tests the priority creation, validation, and basic operations.
 */
@DisplayName("CCommentPriority Domain Tests")
class CCommentPriorityTest {

    private CCommentPriority priority;

    @BeforeEach
    void setUp() {
        // No common setup needed for individual tests
    }

    @Test
    @DisplayName("Should create priority with name and level")
    void testPriorityCreation() {
        // When
        priority = new CCommentPriority("High", 1);
        
        // Then
        assertNotNull(priority);
        assertEquals("High", priority.getName());
        assertEquals(Integer.valueOf(1), priority.getPriorityLevel());
        assertEquals("#0066CC", priority.getColor()); // Default color
        assertFalse(priority.isDefault());
    }

    @Test
    @DisplayName("Should create priority with name, level, and description") 
    void testPriorityCreationWithDescription() {
        // When
        priority = new CCommentPriority("Critical", 1, "Critical priority for urgent comments");
        
        // Then
        assertNotNull(priority);
        assertEquals("Critical", priority.getName());
        assertEquals(Integer.valueOf(1), priority.getPriorityLevel());
        assertEquals("Critical priority for urgent comments", priority.getDescription());
        assertEquals("#0066CC", priority.getColor()); // Default color
        assertFalse(priority.isDefault());
    }

    @Test
    @DisplayName("Should handle null priority level in constructor")
    void testNullPriorityLevelInConstructor() {
        // When
        priority = new CCommentPriority("Medium", null);
        
        // Then
        assertEquals(Integer.valueOf(2), priority.getPriorityLevel()); // Should default to 2
    }

    @Test
    @DisplayName("Should set and get priority level")
    void testPriorityLevelSetterGetter() {
        // Given
        priority = new CCommentPriority("High", 1);
        
        // When
        priority.setPriorityLevel(3);
        
        // Then
        assertEquals(Integer.valueOf(3), priority.getPriorityLevel());
    }

    @Test
    @DisplayName("Should handle null priority level in setter")
    void testNullPriorityLevelInSetter() {
        // Given
        priority = new CCommentPriority("High", 1);
        final Integer originalLevel = priority.getPriorityLevel();
        
        // When
        priority.setPriorityLevel(null);
        
        // Then
        assertEquals(originalLevel, priority.getPriorityLevel()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should set and get color")
    void testColorSetterGetter() {
        // Given
        priority = new CCommentPriority("High", 1);
        
        // When
        priority.setColor("#FF0000");
        
        // Then
        assertEquals("#FF0000", priority.getColor());
    }

    @Test
    @DisplayName("Should set and get default flag")
    void testDefaultFlagSetterGetter() {
        // Given
        priority = new CCommentPriority("Normal", 2);
        assertFalse(priority.isDefault()); // Default is false
        
        // When
        priority.setDefault(true);
        
        // Then
        assertTrue(priority.isDefault());
    }

    @Test
    @DisplayName("Should initialize defaults correctly")
    void testInitializeDefaults() {
        // Given
        priority = new CCommentPriority();
        priority.setPriorityLevel(null);
        priority.setColor(null);
        
        // When
        priority.initializeDefaults();
        
        // Then
        assertEquals(Integer.valueOf(2), priority.getPriorityLevel()); // Default to NORMAL
        assertEquals("#0066CC", priority.getColor()); // Default blue color
    }

    @Test
    @DisplayName("Should not override existing values in initializeDefaults")
    void testInitializeDefaultsDoesNotOverride() {
        // Given
        priority = new CCommentPriority("Urgent", 1);
        priority.setColor("#FF0000");
        
        // When
        priority.initializeDefaults();
        
        // Then
        assertEquals(Integer.valueOf(1), priority.getPriorityLevel()); // Should remain unchanged
        assertEquals("#FF0000", priority.getColor()); // Should remain unchanged
    }

    @Test
    @DisplayName("Should generate meaningful toString")
    void testToString() {
        // Given
        priority = new CCommentPriority("High", 1);
        priority.setColor("#FF0000");
        
        // When
        final String result = priority.toString();
        
        // Then
        assertEquals("High (Level: 1, Color: #FF0000)", result);
    }

    @Test
    @DisplayName("Should handle null name in toString")
    void testToStringWithNullName() {
        // Given
        priority = new CCommentPriority();
        priority.setName(null);
        priority.setPriorityLevel(2);
        priority.setColor("#0066CC");
        
        // When
        final String result = priority.toString();
        
        // Then
        assertTrue(result.contains("Level: 2"));
        assertTrue(result.contains("Color: #0066CC"));
    }

    @Test
    @DisplayName("Should inherit from CTypeEntity correctly")
    void testInheritance() {
        // Given
        priority = new CCommentPriority("Test Priority", 2, "Test description");
        
        // Then - Should have inherited fields from CTypeEntity -> CEntityNamed -> CEntityDB
        assertNotNull(priority.getName());
        assertNotNull(priority.getDescription());
        // ID will be null until persisted, which is expected
    }
}