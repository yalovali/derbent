package tech.derbent.abstracts.annotations;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * Test class for CDataProviderResolver to verify annotation-based data provider functionality.
 * These tests focus on the resolver's behavior with different configurations and error conditions.
 * 
 * Note: These are unit tests that focus on the resolver's logic rather than actual Spring integration.
 * Integration tests are handled separately to test the full Spring context integration.
 */
class CDataProviderResolverTest {

    @Mock
    private ApplicationContext applicationContext;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resolver = new CDataProviderResolver(applicationContext);
    }

    @Test
    @DisplayName("should return empty list when bean is not found by name")
    void testBeanNotFoundByName() {
        // Given
        MetaData metaData = createMetaData("nonExistentService", "", Object.class, "list");
        when(applicationContext.containsBean("nonExistentService")).thenReturn(false);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when bean not found");
        
        verify(applicationContext).containsBean("nonExistentService");
        verify(applicationContext, never()).getBean("nonExistentService");
    }

    @Test
    @DisplayName("should return empty list when bean is not found by class")
    void testBeanNotFoundByClass() {
        // Given
        MetaData metaData = createMetaData("", "", String.class, "list");
        when(applicationContext.getBean(String.class)).thenThrow(new RuntimeException("Bean not found"));

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when bean not found");
        
        verify(applicationContext).getBean(String.class);
    }

    @Test
    @DisplayName("should try automatic resolution when no explicit provider specified")
    void testAutomaticResolutionAttempt() {
        // Given
        MetaData metaData = createMetaData("", "", Object.class, "list");
        when(applicationContext.containsBean("TestEntityService")).thenReturn(false);
        when(applicationContext.containsBean("testEntityService")).thenReturn(false);
        when(applicationContext.containsBean("testentityService")).thenReturn(false);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when no service found");
        
        // Should try multiple naming conventions
        verify(applicationContext).containsBean("TestEntityService");
        verify(applicationContext).containsBean("testEntityService");
        verify(applicationContext).containsBean("testentityService");
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
        // When
        resolver.clearCaches();

        // Then - should not throw exception
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

    @Test
    @DisplayName("should prioritize explicit bean name over class")
    void testBeanNamePriorityOverClass() {
        // Given - both bean name and class specified
        MetaData metaData = createMetaData("explicitService", "", String.class, "list");
        when(applicationContext.containsBean("explicitService")).thenReturn(false);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        
        // Should try bean name first, not class
        verify(applicationContext).containsBean("explicitService");
        verify(applicationContext, never()).getBean(String.class);
    }

    @Test
    @DisplayName("should prioritize explicit class when no bean name specified")
    void testClassResolutionWhenNoBeanName() {
        // Given - only class specified
        MetaData metaData = createMetaData("", "", String.class, "list");
        when(applicationContext.getBean(String.class)).thenThrow(new RuntimeException("Bean not found"));

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty");
        
        // Should try class resolution
        verify(applicationContext).getBean(String.class);
    }

    @Test
    @DisplayName("should handle service method invocation errors gracefully")
    void testServiceMethodInvocationError() {
        // Given
        MetaData metaData = createMetaData("testService", "", Object.class, "list");
        Object mockService = new Object(); // Service without the expected method
        when(applicationContext.containsBean("testService")).thenReturn(true);
        when(applicationContext.getBean("testService")).thenReturn(mockService);

        // When
        List<TestEntity> result = resolver.resolveData(TestEntity.class, metaData);

        // Then
        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when method invocation fails");
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