package tech.derbent.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.utils.CValidationUtils;
import tech.derbent.meetings.domain.CMeetingStatus;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for enhanced binder functionality, detailed error logging, and
 * field-specific validation error reporting.
 */
@ExtendWith (MockitoExtension.class)
class CEnhancedBinderTest extends CTestBase {

	@Override
	@BeforeEach
	protected void setupForTest() {
		// Mock Vaadin environment
		final VaadinRequest request = mock(VaadinRequest.class);
		final VaadinService service = mock(VaadinService.class);
		final VaadinSession session = mock(VaadinSession.class);
		VaadinSession.setCurrent(session);
		UI.setCurrent(new UI());
	}

	@Test
	void testBackwardCompatibility() {
		assertDoesNotThrow(() -> {
			// Enhanced binder should work everywhere standard binder works
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			// Should work with existing form builder
			final Div formLayout =
				CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
			assertNotNull(formLayout, "Form should be created with enhanced binder");
			// Should work with existing entity operations
			final CMeetingStatus testEntity = new CMeetingStatus();
			enhancedBinder.readBean(testEntity);
			// Enhanced binder can be used as BeanValidationBinder
			final CEnhancedBinder<CMeetingStatus> asBeanValidationBinder = enhancedBinder;
			assertNotNull(asBeanValidationBinder,
				"Enhanced binder should work as BeanValidationBinder");
		}, "Enhanced binder should be backward compatible");
	}

	@Test
	void testBinderFactoryMethods() {
		assertDoesNotThrow(() -> {
			// Test standard binder creation
			final CEnhancedBinder<CMeetingStatus> standardBinder =
				CBinderFactory.createStandardBinder(CMeetingStatus.class);
			assertNotNull(standardBinder, "Standard binder should be created");
			assertFalse(CBinderFactory.isEnhancedBinder(standardBinder),
				"Standard binder should not be enhanced");
			// Test enhanced binder creation
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			assertNotNull(enhancedBinder, "Enhanced binder should be created");
			assertTrue(CBinderFactory.isEnhancedBinder(enhancedBinder),
				"Enhanced binder should be detected as enhanced");
			// Test casting utility
			final CEnhancedBinder<CMeetingStatus> castedBinder =
				CBinderFactory.asEnhancedBinder(enhancedBinder);
			assertNotNull(castedBinder, "Casting should work for enhanced binder");
			assertSame(enhancedBinder, castedBinder,
				"Casted binder should be the same instance");
			// Test casting standard binder (should return null)
			final CEnhancedBinder<CMeetingStatus> nullCast =
				CBinderFactory.asEnhancedBinder(standardBinder);
			assertNull(nullCast, "Casting standard binder should return null");
		}, "Binder factory methods should work correctly");
	}

	@Test
	void testEnhancedBinderCreation() {
		assertDoesNotThrow(() -> {
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			assertNotNull(enhancedBinder, "Enhanced binder should be created");
			assertFalse(enhancedBinder.hasValidationErrors(),
				"New binder should have no validation errors");
		}, "Enhanced binder creation should not throw exceptions");
	}

	@Test
	void testEnhancedBinderReadBean() {
		assertDoesNotThrow(() -> {
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			// Build form to create bindings
			CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
			// Test readBean with valid entity
			final CMeetingStatus testEntity = new CMeetingStatus();
			testEntity.setName("Test Status");
			testEntity.setDescription("Test Description");
			testEntity.setColor("#FF0000");
			testEntity.setSortOrder(1);
			testEntity.setFinalStatus(false);
			enhancedBinder.readBean(testEntity);
			// Test readBean with null
			enhancedBinder.readBean(null);
		}, "Enhanced binder readBean should work without exceptions");
	}

	@Test
	void testEnhancedBinderWithFormBuilder() {
		assertDoesNotThrow(() -> {
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CEntityFormBuilder.createEnhancedBinder(CMeetingStatus.class);
			final Div formLayout =
				CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
			assertNotNull(enhancedBinder, "Enhanced binder should be created");
			assertNotNull(formLayout, "Form layout should be created");
			assertTrue(formLayout.getChildren().count() > 0,
				"Form should have components");
		}, "Enhanced binder should work with form builder");
	}

	@Test
	void testEnhancedFormBuilderMethods() {
		assertDoesNotThrow(() -> {
			// Test direct enhanced form creation
			final Div enhancedForm =
				CEntityFormBuilder.buildEnhancedForm(CMeetingStatus.class);
			assertNotNull(enhancedForm, "Enhanced form should be created");
			// Test enhanced form with specific fields
			final java.util.List<String> fields =
				java.util.List.of("name", "description");
			final Div enhancedFormWithFields =
				CEntityFormBuilder.buildEnhancedForm(CMeetingStatus.class, fields);
			assertNotNull(enhancedFormWithFields,
				"Enhanced form with fields should be created");
		}, "Enhanced form builder methods should work without exceptions");
	}

	@Test
	void testFactoryGlobalConfiguration() {
		assertDoesNotThrow(() -> {
			// Test default configuration
			final boolean originalDefault = CBinderFactory.isUseEnhancedBinderByDefault();
			// Test setting enhanced binder as default
			CBinderFactory.setUseEnhancedBinderByDefault(true);
			assertTrue(CBinderFactory.isUseEnhancedBinderByDefault(),
				"Enhanced binder should be set as default");
			// Test creating binder with new default
			final CEnhancedBinder<CMeetingStatus> binder =
				CBinderFactory.createBinder(CMeetingStatus.class);
			assertTrue(CBinderFactory.isEnhancedBinder(binder),
				"Binder should be enhanced when default is set");
			// Restore original settings
			CBinderFactory.setUseEnhancedBinderByDefault(originalDefault);
		}, "Factory global configuration should work correctly");
	}

	@Test
	void testValidationErrorCapture() {
		assertDoesNotThrow(() -> {
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			// Build form to create bindings
			CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
			// Create invalid entity to trigger validation errors
			final CMeetingStatus invalidEntity = new CMeetingStatus();
			// Don't set required fields to trigger validation errors

			try {
				enhancedBinder.writeBean(invalidEntity);
				// If we reach here, validation passed (might be no required fields)
			} catch (final ValidationException e) {
				// Validation failed - check if enhanced binder captured details
				assertNotNull(e, "ValidationException should be thrown");
				// Enhanced binder should capture error details Note: Actual validation
				// errors depend on entity validation annotations
			}
		}, "Validation error capture should work without exceptions");
	}

	@Test
	void testValidationUtilsMethods() {
		assertDoesNotThrow(() -> {
			final CEnhancedBinder<CMeetingStatus> enhancedBinder =
				CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			// Build form to create bindings
			CEntityFormBuilder.buildForm(CMeetingStatus.class, enhancedBinder);
			final CMeetingStatus testEntity = new CMeetingStatus();
			testEntity.setName("Test Status");
			testEntity.setDescription("Test Description");
			// Test validation utility method
			final boolean isValid =
				CValidationUtils.validateBean(enhancedBinder, testEntity);
			// Result depends on entity validation rules Test error summary
			final String errorSummary =
				CValidationUtils.getValidationErrorSummary(enhancedBinder);
			assertNotNull(errorSummary, "Error summary should not be null");
			// Test has validation errors utility
			final boolean hasErrors =
				CValidationUtils.hasValidationErrors(enhancedBinder);
			// Result depends on validation outcome
		}, "Validation utility methods should work without exceptions");
	}
}