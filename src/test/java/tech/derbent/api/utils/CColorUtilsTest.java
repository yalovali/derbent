package tech.derbent.api.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.activities.domain.CActivityStatus;
import tech.derbent.activities.domain.CActivityType;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.users.domain.CUser;

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

	@Test
	void testGetEntityWithIcon_WithUser_ReturnsHorizontalLayout() {
		// Test the new generic function with a user entity
		final CUser user = new CUser("Test User");
		final HorizontalLayout layout = CColorUtils.getEntityWithIcon(user);
		assertNotNull(layout, "Layout should not be null");
		assertTrue(layout.getComponentCount() >= 1, "Layout should contain at least one component");
		// The layout should have proper alignment and spacing
		assertTrue(layout.isSpacing(), "Layout should have spacing enabled");
	}

	@Test
	void testGetEntityWithIcon_WithActivityType_ReturnsHorizontalLayout() {
		// Test the new generic function with a type entity
		final CActivityType activityType = new CActivityType();
		activityType.setName("Development");
		final HorizontalLayout layout = CColorUtils.getEntityWithIcon(activityType);
		assertNotNull(layout, "Layout should not be null");
		assertTrue(layout.getComponentCount() >= 1, "Layout should contain at least one component");
		assertTrue(layout.isSpacing(), "Layout should have spacing enabled");
	}
}
