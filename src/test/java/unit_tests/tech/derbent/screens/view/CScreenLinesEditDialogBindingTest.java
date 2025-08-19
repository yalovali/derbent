package unit_tests.tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;
import tech.derbent.screens.view.CScreenLinesEditDialog;

/**
 * Unit test to verify that CScreenLinesEditDialog does not throw binding exceptions when
 * created and populated. This specifically tests the fix for the incomplete bindings
 * error that occurred when clicking "Add Screen Field Description".
 */
@ExtendWith (MockitoExtension.class)
class CScreenLinesEditDialogBindingTest {

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CScreenLinesEditDialogBindingTest.class);

	@Mock
	private CEntityFieldService entityFieldService;

	@Mock
	private CViewsService viewsService;

	@Test
	void testDialogCreationWithComplexEntityFieldName() {
		LOGGER.info("🧪 Testing dialog creation with complex Entity Field Name...");
		// Create a test screen
		final CScreen screen = new CScreen();
		screen.setName("Complex Test Screen");
		screen.setEntityType("tech.derbent.users.domain.CUser");
		// Create screen line with the specific field that was causing the error
		final CScreenLines screenLine =
			new CScreenLines(screen, "Entity Field Name", "entityProperty");
		screenLine.setEntityProperty("firstName"); // This is the field mentioned in the
		// error
		screenLine.setLineOrder(5);
		screenLine.setIsRequired(true);
		screenLine.setIsReadonly(false);
		screenLine.setIsHidden(false);
		screenLine.setIsActive(true);
		// This test specifically targets the "Entity Field Name" field binding error
		assertDoesNotThrow(() -> {
			final CScreenLinesEditDialog dialog =
				new CScreenLinesEditDialog(screenLine, (savedLine) -> {
					LOGGER.info("Save callback called for complex field: {}", savedLine);
				}, // onSave callback
					false, // isNew = false (this triggers readBean with existing data)
					entityFieldService, screen);
			assertNotNull(dialog,
				"Dialog should be created successfully with complex Entity Field Name");
			LOGGER.info(
				"✅ Dialog created successfully with complex Entity Field Name binding");
		}, "CScreenLinesEditDialog should handle Entity Field Name binding without exceptions");
	}

	@Test
	void testDialogCreationWithNullData() {
		LOGGER.info("🧪 Testing dialog creation with null data (new entry)...");
		// Create a test screen
		final CScreen screen = new CScreen();
		screen.setName("Test Screen for New Entry");
		screen.setEntityType("CActivity");
		// Test with null data (new entry scenario)
		assertDoesNotThrow(() -> {
			final CScreenLinesEditDialog dialog =
				new CScreenLinesEditDialog(null, (savedLine) -> {
					LOGGER.info("Save callback called for new entry: {}", savedLine);
				}, true, // isNew = true
					entityFieldService, screen);
			assertNotNull(dialog, "Dialog should be created successfully with null data");
			LOGGER.info("✅ Dialog created successfully with null data (new entry)");
		}, "CScreenLinesEditDialog should handle null data without binding exceptions");
	}
}