package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.components.CEnhancedBinder;

/**
 * Test to verify that the CEntityFormBuilder fixes for incomplete forField bindings work correctly.
 * This specifically tests the fix for the binding error:
 * "All bindings created with forField must be completed before calling readBean"
 */
@SpringBootTest(classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "spring.jpa.defer-datasource-initialization=false"
})
public class CEntityFormBuilderBindingTest {

    // Test entity with various field types that could cause binding issues
    public static class TestEntity {
        private Integer intField;
        private Long longField;
        private BigDecimal bigDecimalField;
        private String stringField;
        private Double doubleField;

        // Getters and setters
        public Integer getIntField() { return intField; }
        public void setIntField(Integer intField) { this.intField = intField; }

        public Long getLongField() { return longField; }
        public void setLongField(Long longField) { this.longField = longField; }

        public BigDecimal getBigDecimalField() { return bigDecimalField; }
        public void setBigDecimalField(BigDecimal bigDecimalField) { this.bigDecimalField = bigDecimalField; }

        public String getStringField() { return stringField; }
        public void setStringField(String stringField) { this.stringField = stringField; }

        public Double getDoubleField() { return doubleField; }
        public void setDoubleField(Double doubleField) { this.doubleField = doubleField; }
    }

    /**
     * Test that form building with various field types doesn't cause incomplete bindings.
     * This test specifically targets the forField binding issues that were fixed.
     */
    @Test
    void testFormBuildingWithNumberFields() {
        assertDoesNotThrow(() -> {
            // Create a binder for our test entity
            CEnhancedBinder<TestEntity> binder = new CEnhancedBinder<>(TestEntity.class);
            
            // Build form for the test entity - this should not throw binding errors
            com.vaadin.flow.component.html.Div form = 
                CEntityFormBuilder.buildForm(TestEntity.class, binder, null);
            
            assertNotNull(form, "Form should be created successfully");
            assertNotNull(binder, "Binder should be created successfully");
            
            // Test that we can create and populate a test entity without binding errors
            TestEntity testEntity = new TestEntity();
            testEntity.setIntField(42);
            testEntity.setLongField(999L);
            testEntity.setBigDecimalField(new BigDecimal("123.45"));
            testEntity.setStringField("Test String");
            testEntity.setDoubleField(3.14);
            
            // This readBean call should not throw "All bindings created with forField must be completed"
            binder.readBean(testEntity);
            
        }, "Form building and binding should work without incomplete forField errors");
    }

    /**
     * Test that specific number field types work correctly with the fixed binding logic.
     */
    @Test
    void testSpecificNumberFieldBindings() {
        assertDoesNotThrow(() -> {
            CEnhancedBinder<TestEntity> binder = new CEnhancedBinder<>(TestEntity.class);
            
            // Test Integer field specifically
            Field intField = TestEntity.class.getDeclaredField("intField");
            assertNotNull(intField, "Integer field should exist");
            
            // Test Long field specifically  
            Field longField = TestEntity.class.getDeclaredField("longField");
            assertNotNull(longField, "Long field should exist");
            
            // Test BigDecimal field specifically
            Field bigDecimalField = TestEntity.class.getDeclaredField("bigDecimalField");
            assertNotNull(bigDecimalField, "BigDecimal field should exist");
            
            // Build the complete form - this exercises the fixed binding logic
            com.vaadin.flow.component.html.Div form = 
                CEntityFormBuilder.buildForm(TestEntity.class, binder, null);
            assertNotNull(form, "Form with number fields should be created successfully");
            
            // Test entity creation and binding
            TestEntity entity = new TestEntity();
            entity.setIntField(100);
            entity.setLongField(200L);
            entity.setBigDecimalField(new BigDecimal("300.50"));
            
            // This should work without throwing incomplete binding errors
            binder.readBean(entity);
            binder.writeBean(entity);
            
        }, "Number field bindings should work without forField completion errors");
    }
}