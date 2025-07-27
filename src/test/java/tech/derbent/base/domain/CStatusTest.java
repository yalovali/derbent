package tech.derbent.base.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tech.derbent.activities.domain.CActivityStatus;

/**
 * Test class for CStatus base class functionality.
 * Uses CActivityStatus as a concrete implementation for testing.
 */
class CStatusTest {

    @Test
    void testDefaultConstructor() {
        // When
        final CActivityStatus status = new CActivityStatus();

        // Then
        assertNotNull(status);
    }

    @Test
    void testConstructorWithName() {
        // Given
        final String name = "TODO";

        // When
        final CActivityStatus status = new CActivityStatus(name);

        // Then
        assertNotNull(status);
        assertEquals(name, status.getName());
    }

    @Test
    void testConstructorWithNameAndDescription() {
        // Given
        final String name = "IN_PROGRESS";
        final String description = "Task is currently being worked on";

        // When
        final CActivityStatus status = new CActivityStatus(name, description);

        // Then
        assertNotNull(status);
        assertEquals(name, status.getName());
        assertEquals(description, status.getDescription());
    }

    @Test
    void testConstructorWithNullName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new CActivityStatus(null));
    }

    @Test
    void testConstructorWithEmptyName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new CActivityStatus(""));
    }

    @Test
    void testConstructorWithBlankName() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> new CActivityStatus("   "));
    }

    @Test
    void testSetName() {
        // Given
        final CActivityStatus status = new CActivityStatus();
        final String name = "DONE";

        // When
        status.setName(name);

        // Then
        assertEquals(name, status.getName());
    }

    @Test
    void testSetNameWithNull() {
        // Given
        final CActivityStatus status = new CActivityStatus("TODO");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> status.setName(null));
    }

    @Test
    void testSetNameWithEmpty() {
        // Given
        final CActivityStatus status = new CActivityStatus("TODO");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> status.setName(""));
    }

    @Test
    void testSetNameWithBlank() {
        // Given
        final CActivityStatus status = new CActivityStatus("TODO");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> status.setName("   "));
    }

    @Test
    void testSetDescription() {
        // Given
        final CActivityStatus status = new CActivityStatus("REVIEW");
        final String description = "Task is waiting for review";

        // When
        status.setDescription(description);

        // Then
        assertEquals(description, status.getDescription());
    }

    @Test
    void testSetDescriptionWithNull() {
        // Given
        final CActivityStatus status = new CActivityStatus("REVIEW");

        // When
        status.setDescription(null);

        // Then - should accept null description
        assertEquals(null, status.getDescription());
    }

    @Test
    void testToStringWithName() {
        // Given
        final String name = "BLOCKED";
        final CActivityStatus status = new CActivityStatus(name);

        // When
        final String result = status.toString();

        // Then
        assertEquals(name, result);
    }

    @Test
    void testToStringWithoutName() {
        // Given
        final CActivityStatus status = new CActivityStatus();

        // When
        final String result = status.toString();

        // Then - should fall back to superclass toString
        assertNotNull(result);
    }
}