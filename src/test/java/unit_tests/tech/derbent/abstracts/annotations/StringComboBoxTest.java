package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CVerticalLayout;

/**
 * Simple test to demonstrate the String ComboBox issue
 */
class StringComboBoxTest {

    /**
     * Test entity with String field that should be rendered as ComboBox
     */
    public static class TestEntity extends CEntityDB<TestEntity> {

        @AMetaData(displayName = "String Category", required = true, order = 1, dataProviderBean = "someService" // This will cause the issue
        )
        private String category;

        public String getCategory() {
            return category;
        }

        public void setCategory(final String category) {
            this.category = category;
        }
    }

    @Test
    @DisplayName("Should create String ComboBox for String field with dataProviderBean")
    void testStringFieldWithDataProvider() {
        // Given
        final CEnhancedBinder<TestEntity> binder = CBinderFactory.createEnhancedBinder(TestEntity.class);
        // When
        CVerticalLayout formLayout = null;
        Exception caughtException = null;

        try {
            formLayout = CEntityFormBuilder.buildForm(TestEntity.class, binder, null);
            System.out.println("Form created successfully!");
            // Print out what components were created
            formLayout.getChildren().forEach(child -> {
                System.out.println("Child component: " + child.getClass().getSimpleName());
                child.getChildren().forEach(grandchild -> {
                    System.out.println("  - Grandchild: " + grandchild.getClass().getSimpleName());
                    // Look for ComboBox components
                    grandchild.getChildren().forEach(greatGrandchild -> {
                        System.out.println("    - Great-grandchild: " + greatGrandchild.getClass().getSimpleName());

                        if (greatGrandchild.getClass().getSimpleName().contains("ComboBox")) {
                            System.out.println("      *** Found ComboBox! ***");
                        }
                    });
                });
            });
        } catch (final Exception e) {
            caughtException = e;
            System.out.println("Exception caught: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
        }
        // Then
        assertNotNull(formLayout, "Form should be created successfully");
        assertNull(caughtException, "No exception should be thrown when creating String ComboBox");
    }
}