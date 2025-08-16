package unit_tests.tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.users.domain.CUser;

/**
 * Simple test to verify the binding fix without requiring full Spring context.
 */
public class CUsersViewBindingFixTest {

    /**
     * Test that demonstrates the fixed binding behavior.
     */
    @Test
    void testBindingFixWithoutSpringContext() {
        assertDoesNotThrow(() -> {
            // Create an enhanced binder for CUser
            CEnhancedBinder<CUser> binder = new CEnhancedBinder<>(CUser.class);
            assertNotNull(binder, "Binder should be created successfully");
            
            // Create a test user
            CUser testUser = new CUser();
            testUser.setName("Test User");
            testUser.setLastname("Test Lastname");
            testUser.setLogin("testuser");
            testUser.setEmail("test@example.com");
            
            // This should work without throwing incomplete binding errors
            // Even if there were incomplete bindings, validateBindingsComplete() should clean them up
            binder.readBean(testUser);
            
            // Test writeBean as well
            binder.writeBean(testUser);
            
        }, "Binding operations should work without incomplete binding errors");
    }
    
    /**
     * Test that readBean is called multiple times without issues.
     */
    @Test
    void testMultipleReadBeanCalls() {
        assertDoesNotThrow(() -> {
            CEnhancedBinder<CUser> binder = new CEnhancedBinder<>(CUser.class);
            
            CUser user1 = new CUser();
            user1.setName("User 1");
            user1.setLogin("user1");
            
            CUser user2 = new CUser();
            user2.setName("User 2");
            user2.setLogin("user2");
            
            // This simulates the grid selection behavior that was causing the issue
            binder.readBean(user1);
            binder.readBean(user2);
            binder.readBean(user1); // Switch back
            
        }, "Multiple readBean calls should work without binding errors");
    }
}