package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.html.Div;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.MetaData;

/**
 * Test to specifically verify that the forField fix in CEntityFormBuilder works correctly.
 * This test ensures that BigDecimal and Integer fields can be created without causing
 * "All bindings created with forField must be completed" errors.
 */
public class CEntityFormBuilderForFieldFixTest {

    /**
     * Test entity with BigDecimal and Integer fields to test the forField fix
     */
    public static class TestEntityWithNumbers {
        
        @MetaData(displayName = "Price", order = 1)
        private BigDecimal price;
        
        @MetaData(displayName = "Quantity", order = 2)
        private Integer quantity;
        
        @MetaData(displayName = "Count", order = 3)
        private Long count;

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Long getCount() {
            return count;
        }

        public void setCount(Long count) {
            this.count = count;
        }
    }

    /**
     * Test that CEntityFormBuilder can create forms with BigDecimal and Integer fields
     * without throwing forField binding completion errors.
     */
    @Test
    void testBigDecimalAndIntegerFieldsCanBeCreatedWithoutForFieldErrors() {
        assertDoesNotThrow(() -> {
            // This should not throw "All bindings created with forField must be completed" error
            final Div form = CEntityFormBuilder.buildEnhancedForm(TestEntityWithNumbers.class);
            assertNotNull(form, "Form should be created successfully");
        }, "Form creation with BigDecimal and Integer fields should not throw forField binding errors");
    }

    /**
     * Test that the enhanced binder can be created and used without forField errors
     */
    @Test
    void testEnhancedBinderWithNumberFieldsWorksCorrectly() {
        assertDoesNotThrow(() -> {
            // Create enhanced binder - this should not fail
            var binder = CEntityFormBuilder.createEnhancedBinder(TestEntityWithNumbers.class);
            assertNotNull(binder, "Enhanced binder should be created successfully");
            
            // Create form using the binder - this should not fail
            final Div form = CEntityFormBuilder.buildForm(TestEntityWithNumbers.class, binder);
            assertNotNull(form, "Form should be created successfully with enhanced binder");
            
            // Create a test entity and populate form - this should not throw binding errors
            TestEntityWithNumbers testEntity = new TestEntityWithNumbers();
            testEntity.setPrice(new BigDecimal("99.99"));
            testEntity.setQuantity(5);
            testEntity.setCount(100L);
            
            binder.readBean(testEntity);
            
        }, "Enhanced binder with number fields should work without forField binding errors");
    }
}