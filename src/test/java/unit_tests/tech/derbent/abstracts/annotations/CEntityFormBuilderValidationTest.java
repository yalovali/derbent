package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.views.components.CVerticalLayout;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class to verify CEntityFormBuilder improvements including: - Enhanced null pointer checking - Better logging and
 * error handling - Robust parameter validation
 */
class CEntityFormBuilderValidationTest extends CTestBase {

    /**
     * Test entity with various AMetaData annotations and proper getters/setters
     */
    public static class TestEntity {

        @AMetaData(displayName = "Test String", required = true, description = "A test string field", order = 1, maxLength = 50, defaultValue = "test")
        private String testString;

        @AMetaData(displayName = "Test Boolean", required = false, description = "A test boolean field", order = 2, defaultValue = "false")
        private Boolean testBoolean;

        public Boolean getTestBoolean() {
            return testBoolean;
        }

        // Getters and setters required for Vaadin binding
        public String getTestString() {
            return testString;
        }

        public void setTestBoolean(final Boolean testBoolean) {
            this.testBoolean = testBoolean;
        }

        public void setTestString(final String testString) {
            this.testString = testString;
        }
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    @DisplayName("buildForm should handle null binder gracefully")
    void testBuildFormWithNullBinder() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CEntityFormBuilder.buildForm(TestEntity.class, null));
        assertEquals("Binder cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("buildForm should handle null entity class gracefully")
    void testBuildFormWithNullEntityClass() {
        final var binder = CBinderFactory.createEnhancedBinder(TestEntity.class);
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> CEntityFormBuilder.buildForm(null, binder));
        assertEquals("Entity class cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("buildForm should handle entity class without AMetaData annotations")
    void testBuildFormWithoutAMetaData()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        // Test entity without AMetaData annotations
        class EntityWithoutAMetaData {

            @SuppressWarnings("unused")
            private String plainField;
        }
        final var binder = CBinderFactory.createEnhancedBinder(EntityWithoutAMetaData.class);
        final CVerticalLayout formLayout = CEntityFormBuilder.buildForm(EntityWithoutAMetaData.class, binder);
        assertNotNull(formLayout, "Form should be created even without AMetaData fields");
        // Should have minimal content since no fields have AMetaData
    }

    @Test
    @DisplayName("buildForm should successfully create form with valid parameters")
    void testBuildFormWithValidParameters()
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        final var binder = CBinderFactory.createEnhancedBinder(TestEntity.class);
        final CVerticalLayout formLayout = CEntityFormBuilder.buildForm(TestEntity.class, binder);
        assertTrue(formLayout.getChildren().count() > 0, "Form should contain components");
    }
}