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
 * Unit test to verify that CScreenLinesEditDialog does not throw binding exceptions when created and populated. This
 * specifically tests the fix for the incomplete bindings error that occurred when clicking "Add Screen Field
 * Description".
 */
@ExtendWith(MockitoExtension.class)
class CScreenLinesEditDialogBindingTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CScreenLinesEditDialogBindingTest.class);

    @Mock
    private CEntityFieldService entityFieldService;

    @Mock
    private CViewsService viewsService;

    @Test
    void testDialogCreationWithoutBindingErrors() {
        LOGGER.info("🧪 Testing dialog creation for new screen line...");

        // Create a test screen
        CScreen screen = new CScreen();
        screen.setName("Test Screen");
        screen.setEntityType("CActivity");

        // Create a new screen line
        CScreenLines screenLine = new CScreenLines(screen, "Test Field", "testField");
        screenLine.setEntityLineType("CActivity");
        screenLine.setEntityFieldName("name");
        screenLine.setFieldType("TEXT");

        // This should not throw any binding exceptions
        assertDoesNotThrow(() -> {
            CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(screenLine, (savedLine) -> {
                LOGGER.info("Save callback called for: {}", savedLine);
            }, // onSave callback
                    true, // isNew
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully");
            LOGGER.info("✅ Dialog created successfully for new screen line");
        }, "CScreenLinesEditDialog creation should not throw binding exceptions");
    }

    @Test
    void testDialogCreationForExistingEntityWithoutBindingErrors() {
        LOGGER.info("🧪 Testing dialog creation for existing screen line...");

        // Create a test screen
        CScreen screen = new CScreen();
        screen.setName("Test Screen");
        screen.setEntityType("CActivity");

        // Create an existing screen line (not new)
        CScreenLines screenLine = new CScreenLines(screen, "Existing Field", "existingField");
        screenLine.setEntityLineType("CActivity");
        screenLine.setEntityFieldName("description");
        screenLine.setFieldType("TEXT");
        screenLine.setFieldDescription("Test description");

        // This should not throw any binding exceptions when editing existing entity
        assertDoesNotThrow(() -> {
            CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(screenLine, (savedLine) -> {
                LOGGER.info("Save callback called for: {}", savedLine);
            }, // onSave callback
                    false, // isNew = false (editing existing)
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully for existing entity");
            LOGGER.info("✅ Dialog created successfully for existing screen line");
        }, "CScreenLinesEditDialog creation for existing entity should not throw binding exceptions");
    }

    @Test
    void testDialogCreationWithComplexEntityFieldName() {
        LOGGER.info("🧪 Testing dialog creation with complex Entity Field Name...");

        // Create a test screen
        CScreen screen = new CScreen();
        screen.setName("Complex Test Screen");
        screen.setEntityType("tech.derbent.users.domain.CUser");

        // Create screen line with the specific field that was causing the error
        CScreenLines screenLine = new CScreenLines(screen, "Entity Field Name", "entityFieldName");
        screenLine.setEntityLineType("tech.derbent.users.domain.CUser");
        screenLine.setEntityFieldName("firstName"); // This is the field mentioned in the error
        screenLine.setFieldType("TEXT");
        screenLine.setLineOrder(5);
        screenLine.setIsRequired(true);
        screenLine.setIsReadonly(false);
        screenLine.setIsHidden(false);
        screenLine.setIsActive(true);

        // This test specifically targets the "Entity Field Name" field binding error
        assertDoesNotThrow(() -> {
            CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(screenLine, (savedLine) -> {
                LOGGER.info("Save callback called for complex field: {}", savedLine);
            }, // onSave callback
                    false, // isNew = false (this triggers readBean with existing data)
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully with complex Entity Field Name");
            LOGGER.info("✅ Dialog created successfully with complex Entity Field Name binding");
        }, "CScreenLinesEditDialog should handle Entity Field Name binding without exceptions");
    }

    @Test
    void testDialogCreationWithNullData() {
        LOGGER.info("🧪 Testing dialog creation with null data (new entry)...");

        // Create a test screen
        CScreen screen = new CScreen();
        screen.setName("Test Screen for New Entry");
        screen.setEntityType("CActivity");

        // Test with null data (new entry scenario)
        assertDoesNotThrow(() -> {
            CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(null, // data = null for new entries
                    (savedLine) -> {
                        LOGGER.info("Save callback called for new entry: {}", savedLine);
                    }, true, // isNew = true
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully with null data");
            LOGGER.info("✅ Dialog created successfully with null data (new entry)");
        }, "CScreenLinesEditDialog should handle null data without binding exceptions");
    }
}