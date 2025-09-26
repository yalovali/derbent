package tech.derbent.api.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.domains.CTypeEntity;

/** Unit tests for CColorUtils to verify color field detection and status entity identification. */
class CColorUtilsTest {

	@Test
	void testIsStatusEntity_WithActivityStatus_ReturnsTrue() {
		// Test with CActivityStatus which extends CTypeEntity
		assertTrue(CColorUtils.isStatusEntity(CActivityStatus.class));
	}

	@Test
	void testIsStatusEntity_WithActivityType_ReturnsTrue() {
		// Test with CActivityType which extends CTypeEntity
		assertTrue(CColorUtils.isStatusEntity(CActivityType.class));
	}

	@Test
	void testIsStatusEntity_WithTypeEntity_ReturnsTrue() {
		// Test with CTypeEntity directly
		assertTrue(CColorUtils.isStatusEntity(CTypeEntity.class));
	}

	@Test
	void testIsStatusEntity_WithString_ReturnsFalse() {
		// Test with non-status entity
		assertFalse(CColorUtils.isStatusEntity(String.class));
	}

	@Test
	void testIsStatusEntity_WithClassNamePattern_ReturnsTrue() {
		// Test with class name that contains "Status"
		// This would work for custom status entities that follow naming convention
		class TestStatus {
		}
		assertTrue(CColorUtils.isStatusEntity(TestStatus.class));
	}
}
