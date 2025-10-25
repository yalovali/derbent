package tech.derbent.api.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Field;
import java.time.Clock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.base.session.service.ISessionService;

/** Unit tests for CAbstractService validation functionality. Demonstrates the nullable field validation using a concrete implementation. */
class CAbstractServiceValidationTest {

	/** Test entity class with nullable and non-nullable fields. */
	private static class TestEntity extends CEntityNamed<TestEntity> {

		public TestEntity() {
			super();
		}

		public TestEntity(String name) {
			super(TestEntity.class, name);
		}

		@Override
		public void initializeAllFields() {
			// No lazy fields to initialize in test entity
		}
	}

	/** Concrete service implementation for testing. */
	private static class TestService extends CAbstractService<TestEntity> {

		public TestService(IAbstractRepository<TestEntity> repository, Clock clock, ISessionService sessionService) {
			super(repository, clock, sessionService);
		}

		@Override
		protected Class<TestEntity> getEntityClass() { return TestEntity.class; }
	}

	private IAbstractRepository<TestEntity> mockRepository;
	private ISessionService mockSessionService;
	private TestService service;

	/** Helper method to set ID using reflection since there's no public setter. */
	private void setEntityId(TestEntity entity, Long id) throws Exception {
		Field idField = entity.getClass().getSuperclass().getSuperclass().getDeclaredField("id");
		idField.setAccessible(true);
		idField.set(entity, id);
	}

	@SuppressWarnings ("unchecked")
	@BeforeEach
	void setUp() {
		mockRepository = Mockito.mock(IAbstractRepository.class);
		mockSessionService = Mockito.mock(ISessionService.class);
		service = new TestService(mockRepository, Clock.systemDefaultZone(), mockSessionService);
	}

	@Test
	void testCheckDeleteAllowed_WithNullEntity_ThrowsException() {
		// When/Then: passing null entity should throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> {
			service.checkDeleteAllowed(null);
		}, "Null entity should throw IllegalArgumentException");
	}

	@Test
	void testCheckDeleteAllowed_WithNullId_ThrowsException() {
		// Given: an entity without an ID
		TestEntity entity = new TestEntity("Test Entity");
		// ID is null by default
		// When/Then: entity without ID should throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> {
			service.checkDeleteAllowed(entity);
		}, "Entity without ID should throw IllegalArgumentException");
	}

	@Test
	void testCheckDeleteAllowed_WithValidEntity_ReturnsNull() throws Exception {
		// Given: a valid entity
		TestEntity entity = new TestEntity("Test Entity");
		setEntityId(entity, 1L);
		// When: checking if delete is allowed
		String result = service.checkDeleteAllowed(entity);
		// Then: validation should pass (base implementation allows delete)
		assertNull(result, "Valid entity should pass delete validation");
	}

	@Test
	void testCheckSaveAllowed_WithMissingName_ReturnsError() throws Exception {
		// Given: an entity with null name (name is marked as nullable=false in CEntityNamed)
		TestEntity entity = new TestEntity();
		setEntityId(entity, 1L);
		// Name is null by default in TestEntity()
		// When: checking if save is allowed
		String result = service.checkSaveAllowed(entity);
		// Then: validation should fail with appropriate message
		assertNotNull(result, "Entity with null required field should fail validation");
		assertTrue(result.contains("Name") || result.contains("name"), "Error message should mention the missing field name");
	}

	@Test
	void testCheckSaveAllowed_WithNullEntity_ThrowsException() {
		// When/Then: passing null entity should throw IllegalArgumentException
		assertThrows(IllegalArgumentException.class, () -> {
			service.checkSaveAllowed(null);
		}, "Null entity should throw IllegalArgumentException");
	}

	@Test
	void testCheckSaveAllowed_WithValidEntity_ReturnsNull() throws Exception {
		// Given: an entity with all required fields populated
		TestEntity entity = new TestEntity("Test Entity");
		setEntityId(entity, 1L);
		// When: checking if save is allowed
		String result = service.checkSaveAllowed(entity);
		// Then: validation should pass
		assertNull(result, "Valid entity should pass validation");
	}

	@Test
	void testValidateNullableFields_FormatsFieldNamesNicely() throws Exception {
		// Given: an entity with missing required field
		TestEntity entity = new TestEntity();
		setEntityId(entity, 1L);
		// When: validating nullable fields
		String result = service.checkSaveAllowed(entity);
		// Then: error message should contain properly formatted field name
		assertNotNull(result, "Missing required field should fail validation");
		// The error should mention "Name" (from @AMetaData displayName)
		// or "name" (formatted from field name)
		assertTrue(result.toLowerCase().contains("name"), "Error message should contain the field name: " + result);
	}
}
