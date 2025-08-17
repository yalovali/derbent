package unit_tests.tech.derbent.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tech.derbent.users.domain.CUser;

/**
 * Test class for reflection-based methods in CEntityDB base class.
 */
class CEntityDBReflectionTest {

    @Test
    void testCopyNonNullFields() {
        // Create source user with some data
        final CUser source = new CUser("Jane");
        source.setLastname("Smith");
        source.setEmail("jane@example.com");
        source.setPhone("123-456-7890");

        // Create target user with some existing data
        final CUser target = new CUser("John");
        target.setLastname("Doe");
        target.setEmail("john@example.com");
        // phone is null in target

        // Copy non-null fields from source to target
        target.copyNonNullFields(source, target);

        // Verify that non-null fields were copied
        assertEquals("Jane", target.getName(), "Name should be copied");
        assertEquals("Smith", target.getLastname(), "Lastname should be copied");
        assertEquals("jane@example.com", target.getEmail(), "Email should be copied");
        assertEquals("123-456-7890", target.getPhone(), "Phone should be copied");
    }

    @Test
    void testPerformSave() {
        final CUser user = new CUser("Test User");

        // Test that performSave returns the same instance
        final CUser result = user.performSave();
        assertNotNull(result, "performSave should return an instance");
        assertEquals(user, result, "performSave should return the same instance");
    }

    @Test
    void testPerformSoftDelete() {
        final CUser user = new CUser("Test User");
        user.setEnabled(true);

        // Test soft delete using reflection
        final boolean softDeletePerformed = user.performSoftDelete();

        // Should return true because CUser has an 'enabled' field
        assertTrue(softDeletePerformed, "Soft delete should be performed for CUser");

        // Verify the enabled field was set to false
        assertFalse(user.isEnabled(), "User should be disabled after soft delete");
    }
}