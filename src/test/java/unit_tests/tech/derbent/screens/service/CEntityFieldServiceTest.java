package unit_tests.tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CFieldServiceBase;

/**
 * Test class for CEntityFieldService and CFieldServiceBase focusing on entity field extraction.
 */
class CEntityFieldServiceTest {

    private CEntityFieldService entityFieldService;

    @BeforeEach
    void setUp() {
        entityFieldService = new CEntityFieldService();
    }

    @Test
    void testGetEntityClassForKnownTypes() {
        // Test known base entity types
        assertNotNull(CFieldServiceBase.getEntityClass("CActivity"));
        assertNotNull(CFieldServiceBase.getEntityClass("CMeeting"));
        assertNotNull(CFieldServiceBase.getEntityClass("CRisk"));
        assertNotNull(CFieldServiceBase.getEntityClass("CProject"));
        assertNotNull(CFieldServiceBase.getEntityClass("CUser"));

        // Test new entity types
        assertNotNull(CFieldServiceBase.getEntityClass("CActivityType"));
        assertNotNull(CFieldServiceBase.getEntityClass("CActivityStatus"));
        assertNotNull(CFieldServiceBase.getEntityClass("CActivityPriority"));
        assertNotNull(CFieldServiceBase.getEntityClass("CMeetingType"));
        assertNotNull(CFieldServiceBase.getEntityClass("CMeetingStatus"));
        assertNotNull(CFieldServiceBase.getEntityClass("CRiskStatus"));
        assertNotNull(CFieldServiceBase.getEntityClass("CRiskSeverity"));
    }

    @Test
    void testGetEntityClassForUnknownType() {
        assertNull(CFieldServiceBase.getEntityClass("UnknownEntity"));
    }

    @Test
    void testGetEntityFieldsForActivity() {
        final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields("CActivity");

        assertFalse(fields.isEmpty());

        // Check that we have some expected fields from the Activity entity
        final boolean hasNameField = fields.stream().anyMatch(field -> "name".equals(field.getFieldName()));
        assertTrue(hasNameField, "Activity should have a 'name' field");

        // Check that we have fields from the parent class (CEntityOfProject)
        final boolean hasProjectField = fields.stream().anyMatch(field -> "project".equals(field.getFieldName()));
        assertTrue(hasProjectField, "Activity should have a 'project' field from CEntityOfProject");

        // Check that we have activity-specific fields
        final boolean hasEstimatedHoursField = fields.stream()
                .anyMatch(field -> "estimatedHours".equals(field.getFieldName()));
        assertTrue(hasEstimatedHoursField, "Activity should have an 'estimatedHours' field");
    }

    @Test
    void testGetEntityFieldsForProject() {
        final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields("CProject");

        assertFalse(fields.isEmpty());

        // Check that we have the basic name field
        final boolean hasNameField = fields.stream().anyMatch(field -> "name".equals(field.getFieldName()));
        assertTrue(hasNameField, "Project should have a 'name' field");
    }

    @Test
    void testGetEntityFieldsForUser() {
        final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields("CUser");

        assertFalse(fields.isEmpty());

        // Check that we have some expected user fields
        final boolean hasNameField = fields.stream().anyMatch(field -> "name".equals(field.getFieldName()));
        assertTrue(hasNameField, "User should have a 'name' field");
    }

    @Test
    void testGetEntityFieldsForUnknownEntity() {
        final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields("UnknownEntity");

        assertTrue(fields.isEmpty(), "Unknown entity should return empty field list");
    }

    @Test
    void testFieldInfoProperties() {
        final List<CEntityFieldService.EntityFieldInfo> fields = entityFieldService.getEntityFields("CActivity");

        // Find a field with MetaData annotation to test
        final CEntityFieldService.EntityFieldInfo nameField = fields.stream()
                .filter(field -> "name".equals(field.getFieldName())).findFirst().orElse(null);

        assertNotNull(nameField, "Should find the 'name' field");
        assertNotNull(nameField.getDisplayName(), "Field should have a display name");
        assertNotNull(nameField.getFieldType(), "Field should have a field type");
        assertNotNull(nameField.getJavaType(), "Field should have a Java type");
    }

    @Test
    void testGetAvailableEntityTypes() {
        final List<String> entityTypes = new CFieldServiceBase().getAvailableEntityTypes();

        assertEquals(5, entityTypes.size());
        assertTrue(entityTypes.contains("CActivity"));
        assertTrue(entityTypes.contains("CMeeting"));
        assertTrue(entityTypes.contains("CRisk"));
        assertTrue(entityTypes.contains("CProject"));
        assertTrue(entityTypes.contains("CUser"));
    }

    @Test
    void testGetDataProviderBeans() {
        final List<String> providers = entityFieldService.getDataProviderBeans();

        assertFalse(providers.isEmpty());
        assertTrue(providers.contains("CActivityService"));
        assertTrue(providers.contains("CUserService"));
        assertTrue(providers.contains("CProjectService"));
    }
}