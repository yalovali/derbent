package tech.derbent.abstracts.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * Test class for CDataProviderResolver to verify annotation-based data provider functionality.
 * Tests the various strategies for resolving ComboBox data providers including:
 * - Bean name resolution
 * - Bean class resolution  
 * - Automatic resolution by naming convention
 * - Method resolution with different signatures
 * - Error handling and fallback mechanisms
 */
class CDataProviderResolverTest {

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private TestService testService;

    private CDataProviderResolver resolver;

    /**
     * Test entity for ComboBox testing
     */
    public static class TestEntity extends CEntityDB {
        private String name;
        
        public TestEntity() {
            super();
        }
        
        public TestEntity(String name) {
            super();
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name != null ? name : "TestEntity[" + getId() + "]";
        }
    }

    /**
     * Mock service for testing data provider resolution
     */
    public static class TestService {
        
        public List<TestEntity> list(org.springframework.data.domain.Pageable pageable) {
            return Arrays.asList(
                new TestEntity("Entity 1"),
                new TestEntity("Entity 2")
            );
        }
        
        public List<TestEntity> list() {
            return Arrays.asList(
                new TestEntity("Entity A"),
                new TestEntity("Entity B")
            );
        }
        
        public List<TestEntity> findAll() {
            return Arrays.asList(
                new TestEntity("Entity X"),
                new TestEntity("Entity Y")
            );
        }
        
        public List<TestEntity> findAllActive() {
            return Arrays.asList(
                new TestEntity("Active Entity 1"),
                new TestEntity("Active Entity 2")
            );
        }
    }

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new CDataProviderResolver(applicationContext);
    }

    @Test
    @DisplayName("should resolve data using specified bean name")
    void testResolveDataFromBeanName() {
        // Given
        MetaData metaData = createMetaData("testService", "", Object.class, "list");
        when(applicationContext.containsBean("testService")).thenReturn(true);
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 entities");
        assertEquals("Entity A", result.get(0).getName(), "First entity should have correct name");
        assertEquals("Entity B", result.get(1).getName(), "Second entity should have correct name");
        
        verify(applicationContext).containsBean("testService");
        verify(applicationContext).getBean("testService");
    }

    @Test
    @DisplayName("should resolve data using specified bean class")
    void testResolveDataFromBeanClass() {
        // Given
        MetaData metaData = createMetaData("", "", TestService.class, "list");
        when(applicationContext.getBean(TestService.class)).thenReturn(testService);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 entities");
        assertEquals("Entity A", result.get(0).getName(), "First entity should have correct name");
        
        verify(applicationContext).getBean(TestService.class);
    }

    @Test
    @DisplayName("should resolve data using custom method name")
    void testResolveDataWithCustomMethod() {
        // Given
        MetaData metaData = createMetaData("testService", "", Object.class, "findAllActive");
        when(applicationContext.containsBean("testService")).thenReturn(true);
        when(applicationContext.getBean("testService")).thenReturn(testService);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 entities");
        assertEquals("Active Entity 1", result.get(0).getName(), "First entity should be active");
        assertEquals("Active Entity 2", result.get(1).getName(), "Second entity should be active");
    }

    @Test
    @DisplayName("should attempt automatic resolution by naming convention")
    void testAutomaticResolution() {
        // Given
        MetaData metaData = createMetaData("", "", Object.class, "list");
        when(applicationContext.containsBean("TestEntityService")).thenReturn(true);
        when(applicationContext.getBean("TestEntityService")).thenReturn(testService);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "Should return 2 entities");
        
        verify(applicationContext).containsBean("TestEntityService");
        verify(applicationContext).getBean("TestEntityService");
    }

    @Test
    @DisplayName("should return empty list when no provider is found")
    void testNoProviderFound() {
        // Given
        MetaData metaData = createMetaData("nonExistentService", "", Object.class, "list");
        when(applicationContext.containsBean("nonExistentService")).thenReturn(false);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when no provider found");
    }

    @Test
    @DisplayName("should handle null entity type gracefully")
    void testNullEntityType() {
        // Given
        MetaData metaData = createMetaData("testService", "", Object.class, "list");

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> resolver.resolveData(null, metaData)
        );
        
        assertEquals("Entity type cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should handle null metadata gracefully")
    void testNullMetaData() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> resolver.resolveData(TestEntity.class, null)
        );
        
        assertEquals("MetaData cannot be null", exception.getMessage());
    }

    @Test
    @DisplayName("should clear caches successfully")
    void testClearCaches() {
        // Given - first call to populate cache
        MetaData metaData = createMetaData("testService", "", Object.class, "list");
        when(applicationContext.containsBean("testService")).thenReturn(true);
        when(applicationContext.getBean("testService")).thenReturn(testService);
        
        resolver.resolveData(TestEntity.class, metaData);

        // When
        resolver.clearCaches();

        // Then
        String stats = resolver.getCacheStats();
        assertTrue(stats.contains("Method cache: 0 entries"), "Method cache should be cleared");
        assertTrue(stats.contains("Bean cache: 0 entries"), "Bean cache should be cleared");
    }

    @Test
    @DisplayName("should provide cache statistics")
    void testGetCacheStats() {
        // When
        String stats = resolver.getCacheStats();

        // Then
        assertNotNull(stats, "Stats should not be null");
        assertTrue(stats.contains("Method cache:"), "Stats should contain method cache info");
        assertTrue(stats.contains("Bean cache:"), "Stats should contain bean cache info");
    }

    /**
     * Helper method to create MetaData mock with specified values
     */
    @SuppressWarnings("unchecked")
    private MetaData createMetaData(String beanName, String beanValue, Class<?> providerClass, String methodName) {
        MetaData metaData = mock(MetaData.class);
        when(metaData.dataProviderBean()).thenReturn(beanName);
        doReturn(providerClass).when(metaData).dataProviderClass();
        when(metaData.dataProviderMethod()).thenReturn(methodName);
        return metaData;
    }
}