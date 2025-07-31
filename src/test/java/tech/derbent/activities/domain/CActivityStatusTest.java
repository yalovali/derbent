package tech.derbent.activities.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for CActivityStatus class. Tests the activity status entity functionality.
 */
@DisplayName ("CActivityStatus Tests")
class CActivityStatusTest {

	private CActivityStatus status;

	@BeforeEach
	void setUp() {
		status = new CActivityStatus();
	}

	@Test
	@DisplayName ("Should create with default constructor")
	void shouldCreateWithDefaultConstructor() {
		assertNotNull(status);
		assertNull(status.getName());
		assertNull(status.getDescription());
		assertEquals("#808080", status.getColor()); // Default gray color
		assertFalse(status.isFinal());
		assertEquals(100, status.getSortOrder()); // Default sort order
	}

	@Test
	@DisplayName ("Should create with full constructor")
	void shouldCreateWithFullConstructor() {
		// TODO write test for full constructor
	}

	@Test
	@DisplayName ("Should create with name and description constructor")
	void shouldCreateWithNameAndDescriptionConstructor() {
		// TODO write test for name and description constructor
	}

	@Test
	@DisplayName ("Should create with name constructor")
	void shouldCreateWithNameConstructor() {
		// todo write test for name constructor
	}

	@Test
	@DisplayName ("Should handle equals and hashCode correctly")
	void shouldHandleEqualsAndHashCodeCorrectly() {
		// write test for equals and hashCode
	}

	@Test
	@DisplayName ("Should handle final status flag correctly")
	void shouldHandleFinalStatusFlagCorrectly() {
		// Initially not final
		assertFalse(status.isFinal());
		// Set to final
		status.setFinal(true);
		assertTrue(status.isFinal());
		// Set back to non-final
		status.setFinal(false);
		assertFalse(status.isFinal());
	}

	@Test
	@DisplayName ("Should handle null color gracefully")
	void shouldHandleNullColorGracefully() {
		final CActivityStatus nullColorStatus =
			new CActivityStatus("TEST", null, "Test status", null, false);
		assertEquals("#808080", nullColorStatus.getColor()); // Should default to gray
	}

	@Test
	@DisplayName ("Should handle null name gracefully")
	void shouldHandleNullNameGracefully() {
		final CActivityStatus nullNameStatus = new CActivityStatus(null, null);
		assertNull(nullNameStatus.getName());
		// Should not throw exception - validation happens at service layer
	}

	@Test
	@DisplayName ("Should handle null sort order gracefully")
	void shouldHandleNullSortOrderGracefully() {
		status.setSortOrder(null);
		assertEquals(100, status.getSortOrder()); // Should default to 100
	}

	@Test
	@DisplayName ("Should maintain color consistency")
	void shouldMaintainColorConsistency() {
		// Test color setting with null
		status.setColor(null);
		assertEquals("#808080", status.getColor());
		// Test color setting with valid value
		status.setColor("#FF0000");
		assertEquals("#FF0000", status.getColor());
		// Test color setting with empty string
		status.setColor("");
		assertEquals("#808080", status.getColor()); // Should default to gray
	}

	@Test
	@DisplayName ("Should provide meaningful toString")
	void shouldProvideMeaningfulToString() {
		status.setName("BLOCKED");
		assertEquals("BLOCKED", status.toString());
		// Test with null name
		status.setName(null);
		assertTrue(status.toString().contains("CActivityStatus"));
	}

	@Test
	@DisplayName ("Should set and get properties correctly")
	void shouldSetAndGetPropertiesCorrectly() {
		status.setName("REVIEW");
		status.setDescription("Under review");
		status.setColor("#FFA500");
		status.setFinal(false);
		status.setSortOrder(3);
		assertEquals("REVIEW", status.getName());
		assertEquals("Under review", status.getDescription());
		assertEquals("#FFA500", status.getColor());
		assertFalse(status.isFinal());
		assertEquals(3, status.getSortOrder());
	}

	@Test
	@DisplayName ("Should validate typical workflow statuses")
	void shouldValidateTypicalWorkflowStatuses() {
		// todo write test
	}
}