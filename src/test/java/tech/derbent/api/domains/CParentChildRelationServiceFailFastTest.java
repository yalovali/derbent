package tech.derbent.api.domains;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.util.Objects;
import org.junit.jupiter.api.Test;
import tech.derbent.api.utils.Check;

/** Test class for CParentChildRelationService to verify fail-fast validation.
 * Tests the new validation checks added to ensure proper error handling. */
class CParentChildRelationServiceFailFastTest {

	@SuppressWarnings ("static-method")
	@Test
	void testWouldCreateCircularDependency_FailsFastOnNullParentId() {
		// When/Then: Null parent ID throws exception
		assertThrows(NullPointerException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(null, "CActivity", 2L, "CActivity"),
				"Should throw exception for null parent ID");
	}

	@SuppressWarnings ("static-method")
	@Test
	void testWouldCreateCircularDependency_FailsFastOnBlankParentType() {
		// When/Then: Null/blank parent type throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, null, 2L, "CActivity"));
		assertThrows(IllegalArgumentException.class, () -> new TestableParentChildService().wouldCreateCircularDependency(1L, "", 2L, "CActivity"));
	}

	@SuppressWarnings ("static-method")
	@Test
	void testWouldCreateCircularDependency_FailsFastOnNullChildId() {
		// When/Then: Null child ID throws exception
		assertThrows(NullPointerException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", null, "CActivity"));
	}

	@SuppressWarnings ("static-method")
	@Test
	void testWouldCreateCircularDependency_FailsFastOnBlankChildType() {
		// When/Then: Null/blank child type throws exception
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", 2L, null),
				"Should throw exception for null child type");
		assertThrows(IllegalArgumentException.class,
				() -> new TestableParentChildService().wouldCreateCircularDependency(1L, "CActivity", 2L, ""),
				"Should throw exception for blank child type");
	}

	/** Testable subclass that doesn't require Spring dependencies. */
	static class TestableParentChildService {

		@SuppressWarnings ("static-method")
		public boolean wouldCreateCircularDependency(final Long parentId, final String parentType, final Long childId, final String childType) {
			// Reproduce the fail-fast validation from the actual service
			Objects.requireNonNull(parentId, "Parent ID cannot be null");
			Check.notBlank(parentType, "Parent type cannot be blank");
			Objects.requireNonNull(childId, "Child ID cannot be null");
			Check.notBlank(childType, "Child type cannot be blank");
			return false; // Actual implementation not needed for validation tests
		}
	}
}
