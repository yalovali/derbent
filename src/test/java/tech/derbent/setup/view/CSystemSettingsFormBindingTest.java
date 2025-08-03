package tech.derbent.setup.view;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

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
import tech.derbent.setup.domain.CSystemSettings;

/**
 * Test class to verify that CEntityFormBuilder properly completes all forField bindings
 * before readBean is called, preventing IllegalStateException.
 */
@ExtendWith (MockitoExtension.class)
class CSystemSettingsFormBindingTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// Mock Vaadin environment
		@SuppressWarnings ("unused")
		final VaadinRequest request = mock(VaadinRequest.class);
		@SuppressWarnings ("unused")
		final VaadinService service = mock(VaadinService.class);
		final VaadinSession session = mock(VaadinSession.class);
		VaadinSession.setCurrent(session);
		UI.setCurrent(new UI());
	}

	@Test
	void testFormBindingWithNullBean() {
		// Test with null bean to ensure binding handles null values properly
		assertDoesNotThrow(() -> {
			final var binder = new BeanValidationBinder<>(CSystemSettings.class);
			final var formLayout =
				CEntityFormBuilder.buildForm(CSystemSettings.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// This should also not throw exceptions
			binder.readBean(null);
		}, "Form binding with null bean should not throw exceptions");
	}

	@Test
	void testFormBindingWithReadBean() {
		// This test verifies that all forField bindings are properly completed before
		// readBean is called, preventing IllegalStateException
		assertDoesNotThrow(() -> {
			// Create a binder for CSystemSettings
			final var binder = new BeanValidationBinder<>(CSystemSettings.class);
			// Create form using CEntityFormBuilder - this should complete all forField
			// bindings
			final var formLayout =
				CEntityFormBuilder.buildForm(CSystemSettings.class, binder);
			assertNotNull(formLayout, "Form layout should be created");
			// Create test settings
			final var testSettings = new CSystemSettings();
			testSettings.setApplicationName("Test App");
			testSettings.setApplicationVersion("1.0.0");
			testSettings.setSessionTimeoutMinutes(30);
			testSettings.setMaxLoginAttempts(3);
			testSettings.setRequireStrongPasswords(true);
			testSettings.setMaintenanceModeEnabled(false);
			// Check if there are any incomplete bindings before calling readBean This is
			// the actual test - we should not get an IllegalStateException here
			binder.readBean(testSettings);
		}, "Form binding and readBean should not throw IllegalStateException");
	}
}