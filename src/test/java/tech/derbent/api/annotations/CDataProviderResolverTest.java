package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import tech.derbent.api.domains.CEntityDB;

/** Unit tests for CDataProviderResolver parameter handling functionality. */
class CDataProviderResolverTest {

	@Mock
	private ApplicationContext applicationContext;
	@Mock
	private TestService testService;
	private CDataProviderResolver resolver;

	// Simple test entity for testing
	public static class TestEntity extends CEntityDB<TestEntity> {

		public TestEntity() {
			super();
		}

		@Override
		public void initializeAllFields() {
			// TODO Auto-generated method stub
			
		}
	}

	// Mock service for testing parameter methods
	public static class TestService {

		public List<TestEntity> listForComboboxSelector(Object param, Pageable pageable) {
			return Arrays.asList(new TestEntity());
		}

		public List<TestEntity> listForComboboxSelector(Object param) {
			return Arrays.asList(new TestEntity());
		}

		public List<TestEntity> listForComboboxSelector(Pageable pageable) {
			return Arrays.asList(new TestEntity());
		}

		public List<TestEntity> listForComboboxSelector() {
			return Arrays.asList(new TestEntity());
		}

		public String getProject() { return "test-project"; }
	}

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		resolver = new CDataProviderResolver(applicationContext);
	}

	@Test
	void testClearCaches_ClearsSuccessfully() {
		// This test verifies the caching functionality works
		assertDoesNotThrow(() -> resolver.clearCaches());
		// Test that cache stats method works
		String stats = resolver.getCacheStats();
		assertNotNull(stats);
		assertTrue(stats.contains("Method cache"));
		assertTrue(stats.contains("Bean cache"));
	}
}
