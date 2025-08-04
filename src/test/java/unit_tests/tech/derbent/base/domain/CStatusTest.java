package unit_tests.tech.derbent.base.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tech.derbent.activities.domain.CActivityStatus;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for CStatus base class functionality. Uses CActivityStatus as a concrete
 * implementation for testing.
 */
class CStatusTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	void testConstructorWithBlankName() {
		// When/Then
		assertThrows(IllegalArgumentException.class,
			() -> new CActivityStatus("   ", project));
	}

	@Test
	void testConstructorWithEmptyName() {
		// When/Then
		assertThrows(IllegalArgumentException.class,
			() -> new CActivityStatus("", project));
	}

	@Test
	void testConstructorWithName() {
		// Given
		final String name = "TODO";
		// When
		final CActivityStatus status = new CActivityStatus(name, project);
		// Then
		assertNotNull(status);
		assertEquals(name, status.getName());
	}

	@Test
	void testConstructorWithNameAndDescription() {
		// Given
		final String name = "IN_PROGRESS";
		final String description = "Task is currently being worked on";
		// When
		final CActivityStatus status = new CActivityStatus(name, null);
		// Then
		status.setDescription(description);
		assertNotNull(status);
		assertEquals(name, status.getName());
		assertEquals(description, status.getDescription());
	}

	@Test
	void testConstructorWithNullName() {
		// When/Then
		assertThrows(IllegalArgumentException.class,
			() -> new CActivityStatus(null, null));
	}

	@Test
	void testSetDescription() {
		// Given
		final CActivityStatus status = new CActivityStatus("REVIEW", project);
		final String description = "Task is waiting for review";
		// When
		status.setDescription(description);
		// Then
		assertEquals(description, status.getDescription());
	}

	@Test
	void testSetDescriptionWithNull() {
		// Given
		final CActivityStatus status = new CActivityStatus("REVIEW", project);
		// When
		status.setDescription(null);
		// Then - should accept null description
		assertEquals(null, status.getDescription());
	}

	@Test
	void testSetName() {
		final String name = "DONE";
		final CActivityStatus status = new CActivityStatus(name, project);
		assertEquals(name, status.getName());
	}

	@Test
	void testSetNameWithBlank() {
		// Given
		final CActivityStatus status = new CActivityStatus("TODO", project);
		// When/Then
		assertThrows(IllegalArgumentException.class, () -> status.setName("   "));
	}

	@Test
	void testSetNameWithEmpty() {
		// Given
		final CActivityStatus status = new CActivityStatus("TODO", project);
		// When/Then
		assertThrows(IllegalArgumentException.class, () -> status.setName(""));
	}

	@Test
	void testSetNameWithNull() {
		// Given
		final CActivityStatus status = new CActivityStatus("TODO", project);
		// When/Then
		assertThrows(IllegalArgumentException.class, () -> status.setName(null));
	}

	@Test
	void testToStringWithName() {
		// Given
		final String name = "BLOCKED";
		final CActivityStatus status = new CActivityStatus(name, project);
		final String result = status.toString();
		assertEquals(name, result);
	}

	@Test
	void testToStringWithoutName() {
		final CActivityStatus status = new CActivityStatus("TODO", project);
		final String result = status.toString();
		// Then - should fall back to superclass toString
		assertNotNull(result);
	}
}