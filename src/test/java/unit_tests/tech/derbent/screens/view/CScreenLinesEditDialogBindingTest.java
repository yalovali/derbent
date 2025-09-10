package unit_tests.tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.view.CDetailLinesEditDialog;

/** Unit test to verify that CScreenLinesEditDialog does not throw binding exceptions when created and populated. This specifically tests the fix for
 * the incomplete bindings error that occurred when clicking "Add Screen Field Description". */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=0"
})
class CScreenLinesEditDialogBindingTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CScreenLinesEditDialogBindingTest.class);

	@Test
	void testDialogCreationWithComplexEntityFieldName() {
		LOGGER.info("🧪 Testing dialog creation with complex Entity Field Name...");
		// Create a test screen
		final CDetailSection screen = new CDetailSection();
		screen.setName("Complex Test Screen");
		screen.setEntityType("tech.derbent.activities.domain.CActivity");
		// Create screen line with the specific field that was causing the error
		final CDetailLines screenLine = new CDetailLines(screen, "Entity Field Name", "entityProperty");
		screenLine.setProperty("name"); // This is a valid field for CActivity
		screenLine.setLineOrder(5);
		screenLine.setIsRequired(true);
		screenLine.setIsReadonly(false);
		screenLine.setIsHidden(false);
		screenLine.setIsActive(true);
		// This test specifically targets the "Entity Field Name" field binding error
		assertDoesNotThrow(() -> {
			final CDetailLinesEditDialog dialog = new CDetailLinesEditDialog(screenLine, (savedLine) -> {
				LOGGER.info("Save callback called for complex field: {}", savedLine);
			}, false, screen);
			assertNotNull(dialog, "Dialog should be created successfully with complex Entity Field Name");
			LOGGER.info("✅ Dialog created successfully with complex Entity Field Name binding");
		}, "CScreenLinesEditDialog should handle Entity Field Name binding without exceptions");
	}

	@Test
	void testDialogCreationWithNullData() {
		LOGGER.info("🧪 Testing dialog creation with null data (new entry)...");
		// Create a test screen
		final CDetailSection screen = new CDetailSection();
		screen.setName("Test Screen for New Entry");
		screen.setEntityType("tech.derbent.activities.domain.CActivity");
		// Test with null data (new entry scenario)
		assertDoesNotThrow(() -> {
			final CDetailLinesEditDialog dialog = new CDetailLinesEditDialog(null, (savedLine) -> {
				LOGGER.info("Save callback called for new entry: {}", savedLine);
			}, true, screen);
			assertNotNull(dialog, "Dialog should be created successfully with null data");
			LOGGER.info("✅ Dialog created successfully with null data (new entry)");
		}, "CScreenLinesEditDialog should handle null data without binding exceptions");
	}
}
