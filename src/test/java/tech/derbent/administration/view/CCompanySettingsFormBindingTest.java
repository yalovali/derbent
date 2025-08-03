package tech.derbent.administration.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.domains.CTestBase;
import tech.derbent.administration.domain.CCompanySettings;

import static org.mockito.Mockito.mock;

/**
 * Test class to verify that CEntityFormBuilder properly completes all forField bindings
 * for CCompanySettings before readBean is called, preventing IllegalStateException.
 */
@ExtendWith(MockitoExtension.class)
class CCompanySettingsFormBindingTest extends CTestBase {

    @Override
    protected void setupForTest() {
        // Mock Vaadin environment
        final VaadinRequest request = mock(VaadinRequest.class);
        final VaadinService service = mock(VaadinService.class);
        final VaadinSession session = mock(VaadinSession.class);
        VaadinSession.setCurrent(session);
        UI.setCurrent(new UI());
    }

    @Test
    void testFormBindingWithReadBean() {
        // This test verifies that all forField bindings are properly completed
        // before readBean is called, preventing IllegalStateException
        assertDoesNotThrow(() -> {
            // Create a binder for CCompanySettings
            var binder = new BeanValidationBinder<>(CCompanySettings.class);
            
            // Create form using CEntityFormBuilder - this should complete all forField bindings
            var formLayout = CEntityFormBuilder.buildForm(CCompanySettings.class, binder);
            assertNotNull(formLayout, "Form layout should be created");
            
            // Create test settings
            var testSettings = new CCompanySettings();
            
            // Check if there are any incomplete bindings before calling readBean
            // This is the actual test - we should not get an IllegalStateException here
            binder.readBean(testSettings);
            
        }, "Form binding and readBean should not throw IllegalStateException");
    }

    @Test
    void testFormBindingWithNullBean() {
        // Test with null bean to ensure binding handles null values properly
        assertDoesNotThrow(() -> {
            var binder = new BeanValidationBinder<>(CCompanySettings.class);
            var formLayout = CEntityFormBuilder.buildForm(CCompanySettings.class, binder);
            assertNotNull(formLayout, "Form layout should be created");
            
            // This should also not throw exceptions
            binder.readBean(null);
            
        }, "Form binding with null bean should not throw exceptions");
    }
}