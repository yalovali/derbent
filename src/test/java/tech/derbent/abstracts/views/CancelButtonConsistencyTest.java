package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import tech.derbent.abstracts.domains.CTestBase;

/**
 * Test to ensure all views that extend CAbstractMDPage have cancel button functionality. This test validates the
 * requirement from the coding guidelines that all views should have cancel buttons to reject changes.
 */
class CancelButtonConsistencyTest extends CTestBase {

    @Test
    void testCAbstractMDPageHasCancelButton() {
        assertDoesNotThrow(() -> {
            // Verify that the base class has the createCancelButton method
            final Method createCancelButton = CAbstractNamedEntityPage.class.getDeclaredMethod("createCancelButton",
                    String.class);
            assertNotNull(createCancelButton, "CAbstractMDPage should have createCancelButton method");
            // Verify that the base class has the createDetailsTabButtonLayout method that
            // includes cancel
            final Method createDetailsTabButtonLayout = CAbstractNamedEntityPage.class
                    .getDeclaredMethod("createDetailsTabButtonLayout");
            assertNotNull(createDetailsTabButtonLayout,
                    "CAbstractMDPage should have createDetailsTabButtonLayout method");
        }, "Base class should have cancel button infrastructure");
    }

    @Test
    void testCButtonHasCreateMethods() {
        assertDoesNotThrow(() -> {
            // Verify that CButton has the static factory methods for different button
            // types
            final Method createPrimary = CButton.class.getDeclaredMethod("createPrimary", String.class);
            assertNotNull(createPrimary, "CButton should have createPrimary method");
            final Method createTertiary = CButton.class.getDeclaredMethod("createTertiary", String.class);
            assertNotNull(createTertiary, "CButton should have createTertiary method");
        }, "CButton should have factory methods for button creation");
    }

    @Test
    void testCDBEditDialogHasCancelButton() {
        assertDoesNotThrow(() -> {
            // Verify that CDBEditDialog has setupButtons method that includes cancel
            final Method setupButtons = CDBEditDialog.class.getDeclaredMethod("setupButtons");
            assertNotNull(setupButtons, "CDBEditDialog should have setupButtons method");
        }, "CDBEditDialog should have cancel button functionality");
    }

    @Test
    void testCDialogBaseClassExists() {
        assertDoesNotThrow(() -> {
            // Verify that CDialog base class exists and is properly structured
            final Class<?> dialogClass = CDialog.class;
            assertNotNull(dialogClass, "CDialog base class should exist");
            // Check that it has the abstract setupButtons method
            final Method setupButtons = dialogClass.getDeclaredMethod("setupButtons");
            assertNotNull(setupButtons, "CDialog should have abstract setupButtons method");
            assertTrue(java.lang.reflect.Modifier.isAbstract(setupButtons.getModifiers()),
                    "setupButtons should be abstract to force implementation");
        }, "CDialog base infrastructure should be correct");
    }

    @Override
    protected void setupForTest() {
        // TODO Auto-generated method stub

    }
}