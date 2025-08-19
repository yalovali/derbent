package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CVerticalLayout;

/**
 * Test to verify that the EntityFormBuilder forField fix resolves the CUserView grid click issue. This ensures that
 * BigDecimal and Integer fields with type converters work correctly without causing "All bindings created with forField
 * must be completed" errors.
 */
public class CEntityFormBuilderForFieldFixVerificationTest {

    /**
     * Test entity with various numeric types that require converters
     */
    public static class TestEntityWithNumbers {

        @MetaData(displayName = "Name", order = 1)
        private String name;

        @MetaData(displayName = "Price", order = 2)
        private BigDecimal price;

        @MetaData(displayName = "Quantity", order = 3)
        private Integer quantity;

        @MetaData(displayName = "Count", order = 4)
        private Long count;

        public Long getCount() {
            return count;
        }

        // Getters and setters
        public String getName() {
            return name;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setCount(Long count) {
            this.count = count;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    /**
     * Test that simulates the CUserView grid click scenario: 1. Create a form (like view initialization) 2. Populate
     * form with entity data (like grid row selection) This should work without forField binding completion errors.
     */
    @Test
    void testGridClickScenarioWorksWithNumericFields() {
        assertDoesNotThrow(() -> {
            // Create form and binder (like CUserView initialization)
            final CEnhancedBinder<TestEntityWithNumbers> binder = CEntityFormBuilder
                    .createEnhancedBinder(TestEntityWithNumbers.class);
            CVerticalLayout formLayout = CEntityFormBuilder.buildForm(TestEntityWithNumbers.class, binder);
            assertNotNull(binder);
            assertNotNull(formLayout);
            // Create test entity (like selected grid row)
            TestEntityWithNumbers entity = new TestEntityWithNumbers();
            entity.setName("Test Product");
            entity.setPrice(new BigDecimal("99.99"));
            entity.setQuantity(5);
            entity.setCount(100L);
            // Populate form (like grid click event)
            binder.readBean(entity);

            // Test multiple grid clicks (switching between entities)
            for (int i = 0; i < 3; i++) {
                TestEntityWithNumbers newEntity = new TestEntityWithNumbers();
                newEntity.setName("Product " + i);
                newEntity.setPrice(new BigDecimal(String.valueOf(10.0 + i)));
                newEntity.setQuantity(i + 1);
                newEntity.setCount((long) (i * 10));
                binder.readBean(newEntity);
            }
        }, "Grid click scenario with numeric fields should work without forField binding errors");
    }
}