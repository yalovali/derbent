package unit_tests.tech.derbent.screens.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Mock
    private CEntityFieldService entityFieldService;

    @Mock
    private CViewsService viewsService;

    @Test
    void testDialogCreationWithoutBindingErrors() {
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
            }, // onSave callback
                    true, // isNew
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully");
        }, "CScreenLinesEditDialog creation should not throw binding exceptions");
    }

    @Test
    void testDialogCreationForExistingEntityWithoutBindingErrors() {
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
            }, // onSave callback
                    false, // isNew = false (editing existing)
                    entityFieldService, screen, viewsService);

            assertNotNull(dialog, "Dialog should be created successfully for existing entity");
        }, "CScreenLinesEditDialog creation for existing entity should not throw binding exceptions");
    }
}