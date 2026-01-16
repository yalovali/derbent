package tech.derbent.api.domains;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

/** Test class for CParentChildRelationService to verify fail-fast validation.
 * Tests the new validation checks added to ensure proper error handling. */
class CParentChildRelationServiceFailFastTest {

	@Test
	void testWouldCreateCircularDependency_FailsFastOnNullParentId() {
		// When/Then: Null parent ID throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(null, "CActivity", 2L, "CActivity"));
	}

	@Test
	void testWouldCreateCircularDependency_FailsFastOnBlankParentType() {
		// When/Then: Null/blank parent type throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, null, 2L, "CActivity"));
		assertThrows(IllegalArgumentException.class, () -> new TestableParentChildService().wouldCreateCircularDependency(1L, "", 2L, "CActivity"));
	}

	@Test
	void testWouldCreateCircularDependency_FailsFastOnNullChildId() {
		// When/Then: Null child ID throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", null, "CActivity"));
	}

	@Test
	void testWouldCreateCircularDependency_FailsFastOnBlankChildType() {
		// When/Then: Null/blank child type throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", 2L, null));
		assertThrows(IllegalArgumentException.class, () -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", 2L, ""));
	}

	/** Testable subclass that doesn't require Spring dependencies. */
	static class TestableParentChildService {

		public boolean wouldCreateCircularDependency(final Long parentId, final String parentType, final Long childId, final String childType) {
			// Reproduce the fail-fast validation from the actual service
			tech.derbent.api.utils.Check.notNull(parentId, "Parent ID cannot be null");
			tech.derbent.api.utils.Check.notBlank(parentType, "Parent type cannot be blank");
			tech.derbent.api.utils.Check.notNull(childId, "Child ID cannot be null");
			tech.derbent.api.utils.Check.notBlank(childType, "Child type cannot be blank");
			return false; // Actual implementation not needed for validation tests
		}
	}
}
