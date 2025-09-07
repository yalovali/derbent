package tech.derbent.screens.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

class CEntityFieldServiceTest {

	@BeforeEach
	void setUp() {}

	@Test
	void testEntityFieldInfoProperties() {
		// When
		final List<EntityFieldInfo> fields = CEntityFieldService.getEntityFields("CActivity");
		// Then
		assertFalse(fields.isEmpty());
		final EntityFieldInfo nameField = fields.stream().filter(f -> "name".equals(f.getFieldName())).findFirst().orElse(null);
		assertNotNull(nameField);
		assertEquals("name", nameField.getFieldName());
		assertEquals("Name", nameField.getDisplayName());
		assertEquals("TEXT", nameField.getFieldType());
		assertTrue(nameField.isRequired());
	}

	@Test
	void testGetAvailableEntityTypes() {
		// When
		final List<String> entityTypes = CEntityFieldService.getAvailableEntityTypes();
		// Then
		assertNotNull(entityTypes);
		assertFalse(entityTypes.isEmpty());
		assertTrue(entityTypes.contains("CActivity"));
		assertTrue(entityTypes.contains("CMeeting"));
		assertTrue(entityTypes.contains("CRisk"));
		assertTrue(entityTypes.contains("CProject"));
		assertTrue(entityTypes.contains("CUser"));
	}

	@Test
	void testGetDataProviderBeans() {
		// When
		final List<String> beans = CEntityFieldService.getDataProviderBeans();
		// Then
		assertNotNull(beans);
		assertFalse(beans.isEmpty());
		assertTrue(beans.contains("CActivityService"));
		assertTrue(beans.contains("CScreenService"));
		assertTrue(beans.contains("CUserService"));
	}

	@Test
	void testGetEntityFieldsForActivity() {
		// When
		final List<EntityFieldInfo> fields = CEntityFieldService.getEntityFields("CActivity");
		// Then
		assertNotNull(fields);
		assertFalse(fields.isEmpty());
		// Check that we have some expected fields
		final boolean hasName = fields.stream().anyMatch(f -> "name".equals(f.getFieldName()));
		final boolean hasDescription = fields.stream().anyMatch(f -> "description".equals(f.getFieldName()));
		final boolean hasActivityType = fields.stream().anyMatch(f -> "activityType".equals(f.getFieldName()));
		assertTrue(hasName, "Should have 'name' field");
		assertTrue(hasDescription, "Should have 'description' field");
		assertTrue(hasActivityType, "Should have 'activityType' field");
	}

	@Test
	void testGetEntityFieldsForInvalidType() {
		// When
		final List<EntityFieldInfo> fields = CEntityFieldService.getEntityFields("InvalidType");
		// Then
		assertNotNull(fields);
		assertTrue(fields.isEmpty());
	}
}
