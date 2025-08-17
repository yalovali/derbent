package unit_tests.tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.annotations.MetaData;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class to verify MetaData annotation improvements including: - Better null pointer checking - Enhanced
 * documentation - Parameter validation
 */
class MetaDataValidationTest extends CTestBase {

    /**
     * Test entity class with MetaData annotations for testing
     */
    private static class TestEntity {

        @MetaData(displayName = "Test Name", required = true, description = "A test field for validation", order = 1, maxLength = 100, defaultValue = "test")
        private String name;

        @MetaData(displayName = "Test Number", required = false, description = "A numeric test field", order = 2, min = 0.0, max = 100.0, defaultValue = "50")
        private Double number;

        @MetaData(displayName = "Test Boolean", required = true, description = "A boolean test field", order = 3, defaultValue = "true")
        private Boolean flag;

        @MetaData(displayName = "", // Test empty display name
                required = false, description = "", // Test empty description
                order = 4, defaultValue = "" // Test empty default value
        )
        private String emptyValues;
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub
    }

    @Test
    @DisplayName("MetaData should handle boolean fields correctly")
    void testBooleanField() throws NoSuchFieldException {
        final Field flagField = TestEntity.class.getDeclaredField("flag");
        final MetaData metaData = flagField.getAnnotation(MetaData.class);
        assertNotNull(metaData, "MetaData annotation should be present");
        assertTrue(metaData.required());
        assertEquals("true", metaData.defaultValue());
    }

    @Test
    @DisplayName("MetaData defaults should be properly set")
    void testDefaults() throws NoSuchFieldException {
        // Test defaults by creating a minimal annotation
        final Field emptyField = TestEntity.class.getDeclaredField("emptyValues");
        final MetaData metaData = emptyField.getAnnotation(MetaData.class);
        // These should be the defaults according to our annotation definition
        assertEquals("", metaData.defaultValue());
        assertEquals("", metaData.description());
        assertFalse(metaData.hidden());
        assertFalse(metaData.readOnly());
        assertFalse(metaData.required());
        assertEquals(4, metaData.order()); // This field overrides to 4, but default
                                           // should be 100
        assertEquals("", metaData.width());
        assertEquals(Double.MIN_VALUE, metaData.min());
        assertEquals(Double.MAX_VALUE, metaData.max());
        assertEquals(-1, metaData.maxLength());
        assertFalse(metaData.useRadioButtons());
    }

    @Test
    @DisplayName("MetaData should handle empty values gracefully")
    void testEmptyValues() throws NoSuchFieldException {
        final Field emptyField = TestEntity.class.getDeclaredField("emptyValues");
        final MetaData metaData = emptyField.getAnnotation(MetaData.class);
        assertNotNull(metaData, "MetaData annotation should be present");
        assertEquals("", metaData.displayName()); // Should be empty, not null
        assertEquals("", metaData.description()); // Should be empty, not null
        assertEquals("", metaData.defaultValue()); // Should be empty, not null
        assertFalse(metaData.required()); // Should default to false
        assertFalse(metaData.hidden()); // Should default to false
        assertFalse(metaData.readOnly()); // Should default to false
        assertEquals(4, metaData.order()); // Should preserve the set value
    }

    @Test
    @DisplayName("MetaData annotation should preserve all parameter values")
    void testMetaDataAnnotationValues() throws NoSuchFieldException {
        final Field nameField = TestEntity.class.getDeclaredField("name");
        final MetaData metaData = nameField.getAnnotation(MetaData.class);
        assertNotNull(metaData, "MetaData annotation should be present");
        assertEquals("Test Name", metaData.displayName());
        assertTrue(metaData.required());
        assertEquals("A test field for validation", metaData.description());
        assertEquals(1, metaData.order());
        assertEquals(100, metaData.maxLength());
        assertEquals("test", metaData.defaultValue());
    }

    @Test
    @DisplayName("MetaData should handle numeric constraints properly")
    void testNumericConstraints() throws NoSuchFieldException {
        final Field numberField = TestEntity.class.getDeclaredField("number");
        final MetaData metaData = numberField.getAnnotation(MetaData.class);
        assertNotNull(metaData, "MetaData annotation should be present");
        assertEquals(0.0, metaData.min());
        assertEquals(100.0, metaData.max());
        assertEquals("50", metaData.defaultValue());
        assertFalse(metaData.required());
    }
}