package tech.derbent.abstracts.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CDataProviderResolver to verify annotation-based data provider
 * functionality. These tests focus on the resolver's behavior with different
 * configurations and error conditions. Note: These are unit tests that focus on the
 * resolver's logic rather than actual Spring integration. Integration tests are handled
 * separately to test the full Spring context integration.
 */
class CDataProviderResolverTest extends CTestBase {

	/**
	 * Test entity for ComboBox testing
	 */
	public static class TestEntity extends CEntityDB<TestEntity> {

		private String name;

		public TestEntity() {
			super(TestEntity.class);
		}

		public String getName() { return name; }

		public void setName(final String name) { this.name = name; }

		@Override
		public String toString() {
			return name != null ? name : "TestEntity[" + getId() + "]";
		}
	}

	@Mock
	private ApplicationContext applicationContext;

	private CDataProviderResolver resolver;

	/**
	 * Helper method to create MetaData mock with specified values
	 */
	private MetaData createMetaData(final String beanName, final String beanValue,
		final Class<?> providerClass, final String methodName) {
		final MetaData metaData = mock(MetaData.class);
		when(metaData.dataProviderBean()).thenReturn(beanName);
		doReturn(providerClass).when(metaData).dataProviderClass();
		when(metaData.dataProviderMethod()).thenReturn(methodName);
		return metaData;
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		resolver = new CDataProviderResolver(applicationContext);
	}

	@Test
	@DisplayName ("should try automatic resolution when no explicit provider specified")
	void testAutomaticResolutionAttempt() {
		// Given
		final MetaData metaData = createMetaData("", "", Object.class, "list");
		when(applicationContext.containsBean("TestEntityService")).thenReturn(false);
		when(applicationContext.containsBean("testEntityService")).thenReturn(false);
		when(applicationContext.containsBean("testentityService")).thenReturn(false);
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty when no service found");
		// Should try multiple naming conventions
		verify(applicationContext).containsBean("TestEntityService");
		verify(applicationContext).containsBean("testEntityService");
		verify(applicationContext).containsBean("testentityService");
	}

	@Test
	@DisplayName ("should prioritize explicit bean name over class")
	void testBeanNamePriorityOverClass() {
		// Given - both bean name and class specified
		final MetaData metaData =
			createMetaData("explicitService", "", String.class, "list");
		when(applicationContext.containsBean("explicitService")).thenReturn(false);
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty");
		// Should try bean name first, not class
		verify(applicationContext).containsBean("explicitService");
		verify(applicationContext, never()).getBean(String.class);
	}

	@Test
	@DisplayName ("should return empty list when bean is not found by class")
	void testBeanNotFoundByClass() {
		// Given
		final MetaData metaData = createMetaData("", "", String.class, "list");
		when(applicationContext.getBean(String.class))
			.thenThrow(new RuntimeException("Bean not found"));
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty when bean not found");
		verify(applicationContext).getBean(String.class);
	}

	@Test
	@DisplayName ("should return empty list when bean is not found by name")
	void testBeanNotFoundByName() {
		// Given
		final MetaData metaData =
			createMetaData("nonExistentService", "", Object.class, "list");
		when(applicationContext.containsBean("nonExistentService")).thenReturn(false);
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty when bean not found");
		verify(applicationContext).containsBean("nonExistentService");
		verify(applicationContext, never()).getBean("nonExistentService");
	}

	@Test
	@DisplayName ("should prioritize explicit class when no bean name specified")
	void testClassResolutionWhenNoBeanName() {
		// Given - only class specified
		final MetaData metaData = createMetaData("", "", String.class, "list");
		when(applicationContext.getBean(String.class))
			.thenThrow(new RuntimeException("Bean not found"));
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(), "Result should be empty");
		// Should try class resolution
		verify(applicationContext).getBean(String.class);
	}

	@Test
	@DisplayName ("should clear caches successfully")
	void testClearCaches() {
		// When
		resolver.clearCaches();
		// Then - should not throw exception
		final String stats = resolver.getCacheStats();
		assertTrue(stats.contains("Method cache: 0 entries"),
			"Method cache should be cleared");
		assertTrue(stats.contains("Bean cache: 0 entries"),
			"Bean cache should be cleared");
	}

	@Test
	@DisplayName ("should provide cache statistics")
	void testGetCacheStats() {
		// When
		final String stats = resolver.getCacheStats();
		// Then
		assertNotNull(stats, "Stats should not be null");
		assertTrue(stats.contains("Method cache:"),
			"Stats should contain method cache info");
		assertTrue(stats.contains("Bean cache:"), "Stats should contain bean cache info");
	}

	@Test
	@DisplayName ("should handle null entity type gracefully")
	void testNullEntityType() {
		// Given
		final MetaData metaData = createMetaData("testService", "", Object.class, "list");
		// When & Then
		final IllegalArgumentException exception = assertThrows(
			IllegalArgumentException.class, () -> resolver.resolveData(null, metaData));
		assertEquals("Entity type cannot be null", exception.getMessage());
	}

	@Test
	@DisplayName ("should handle null metadata gracefully")
	void testNullMetaData() {
		// When & Then
		final IllegalArgumentException exception =
			assertThrows(IllegalArgumentException.class,
				() -> resolver.resolveData(TestEntity.class, null));
		assertEquals("MetaData cannot be null", exception.getMessage());
	}

	@Test
	@DisplayName ("should handle service method invocation errors gracefully")
	void testServiceMethodInvocationError() {
		// Given
		final MetaData metaData = createMetaData("testService", "", Object.class, "list");
		final Object mockService = new Object(); // Service without the expected method
		when(applicationContext.containsBean("testService")).thenReturn(true);
		when(applicationContext.getBean("testService")).thenReturn(mockService);
		// When
		final List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);
		// Then
		assertNotNull(result, "Result should not be null");
		assertTrue(result.isEmpty(),
			"Result should be empty when method invocation fails");
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}