package tech.derbent.examples;

import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.data.binder.ValidationException;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.CValidationUtils;
import tech.derbent.base.ui.dialogs.CWarningDialog;
import tech.derbent.meetings.domain.CMeetingStatus;
import tech.derbent.session.service.CSessionService;

/**
 * Example showing how to integrate enhanced binder with existing views with minimal
 * changes. This demonstrates the migration path from standard to enhanced binders.
 */
public class CEnhancedBinderIntegrationExample {

	/**
	 * MIGRATION STRATEGY: Gradual migration with configuration
	 */
	public static class ConfigurableMeetingStatusView {

		private final CEnhancedBinder<CMeetingStatus> binder;

		private final CAbstractService<CMeetingStatus> service;

		public ConfigurableMeetingStatusView(
			final CAbstractService<CMeetingStatus> service,
			final CSessionService sessionService, final boolean useEnhancedBinder)
			throws NoSuchMethodException, SecurityException, IllegalAccessException,
			InvocationTargetException {
			this.service = service;

			if (useEnhancedBinder) {
				// Use enhanced binder
				this.binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			}
			else {
				// Use standard binder
				this.binder = CBinderFactory.createStandardBinder(CMeetingStatus.class);
			}
			CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
		}

		public void save() {

			try {
				final CMeetingStatus entity = new CMeetingStatus();
				binder.writeBean(entity);
				service.save(entity);
				LOGGER.info("Entity saved successfully");
			} catch (final ValidationException validationException) {

				// Smart error handling - uses enhanced features if available
				if (CBinderFactory.isEnhancedBinder(binder)) {
					CValidationUtils.handleValidationException(binder,
						validationException, "Meeting Status");
				}
				else {
					// Fallback to standard error handling
					LOGGER.error("Validation error during save", validationException);
					new CWarningDialog(
						"Failed to save the data. Please check that all required fields are filled and values are valid.")
						.open();
				}
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
			}
		}
	}

	/**
	 * AFTER: Enhanced implementation with minimal changes Only the binder creation and
	 * error handling are changed
	 */
	public static class EnhancedMeetingStatusView {

		private final CEnhancedBinder<CMeetingStatus> binder; // Same type!

		private final CAbstractService<CMeetingStatus> service;

		public EnhancedMeetingStatusView(final CAbstractService<CMeetingStatus> service,
			final CSessionService sessionService) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException {
			this.service = service;
			// MINIMAL CHANGE: Use factory method instead of direct instantiation
			this.binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
		}

		public void save() {

			try {
				final CMeetingStatus entity = new CMeetingStatus();
				binder.writeBean(entity);
				service.save(entity);
				LOGGER.info("Entity saved successfully");
			} catch (final ValidationException validationException) {
				// MINIMAL CHANGE: Use enhanced error handling
				CValidationUtils.handleValidationException(binder, validationException,
					"Meeting Status");
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
			}
		}
	}

	/**
	 * ALTERNATIVE: Direct enhanced binder usage with explicit typing
	 */
	public static class ExplicitEnhancedMeetingStatusView {

		private final CEnhancedBinder<CMeetingStatus> enhancedBinder;

		private final CAbstractService<CMeetingStatus> service;

		public ExplicitEnhancedMeetingStatusView(
			final CAbstractService<CMeetingStatus> service,
			final CSessionService sessionService) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException {
			this.service = service;
			// Direct enhanced binder creation
			this.enhancedBinder =
				CEntityFormBuilder.createEnhancedBinder(CMeetingStatus.class);
			CEntityFormBuilder.buildEnhancedForm(CMeetingStatus.class);
		}

		public void save() {

			try {
				final CMeetingStatus entity = new CMeetingStatus();
				enhancedBinder.writeBean(entity);
				service.save(entity);
				LOGGER.info("Entity saved successfully");
			} catch (final ValidationException validationException) {

				// Enhanced error handling with direct access to error details
				if (enhancedBinder.hasValidationErrors()) {
					LOGGER.error("Detailed validation errors:");
					LOGGER.error(enhancedBinder.getFormattedErrorSummary());
					// Show enhanced error dialog
					CValidationUtils.showEnhancedValidationDialog(enhancedBinder,
						"Meeting Status");
				}
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
			}
		}

		// Additional method to demonstrate enhanced features
		public void validateAndShowErrors() {
			final CMeetingStatus testEntity = new CMeetingStatus();
			final boolean isValid =
				CValidationUtils.validateBean(enhancedBinder, testEntity);

			if (!isValid) {
				LOGGER.warn("Validation failed for the following fields:");
				enhancedBinder.getFieldsWithErrors().forEach(field -> LOGGER
					.warn("  - {}: {}", field, enhancedBinder.getFieldError(field)));
			}
		}
	}

	/**
	 * BEFORE: Standard implementation using BeanValidationBinder
	 */
	public static class StandardMeetingStatusView {

		private final CEnhancedBinder<CMeetingStatus> binder;

		private final CAbstractService<CMeetingStatus> service;

		public StandardMeetingStatusView(final CAbstractService<CMeetingStatus> service,
			final CSessionService sessionService) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, InvocationTargetException {
			this.service = service;
			// Standard binder creation
			this.binder = CBinderFactory.createEnhancedBinder(CMeetingStatus.class);
			CEntityFormBuilder.buildForm(CMeetingStatus.class, binder);
		}

		public void save() {

			try {
				final CMeetingStatus entity = new CMeetingStatus();
				binder.writeBean(entity);
				service.save(entity);
				LOGGER.info("Entity saved successfully");
			} catch (final ValidationException validationException) {
				LOGGER.error("Validation error during save", validationException);
				// Generic error message - no field details
				new CWarningDialog(
					"Failed to save the data. Please check that all required fields are filled and values are valid.")
					.open();
			} catch (final Exception exception) {
				LOGGER.error("Unexpected error during save operation", exception);
			}
		}
	}

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CEnhancedBinderIntegrationExample.class);

	/**
	 * DEMONSTRATION: Compare error outputs
	 */
	public static void demonstrateErrorOutputComparison() {
		LOGGER.info("=== ERROR OUTPUT COMPARISON ===");
		// Standard binder error (generic)
		LOGGER.info("BEFORE (Standard Binder):");
		LOGGER.info(
			"  Validation error during save: Validation error in property 'Entity'");
		// Enhanced binder error (detailed)
		LOGGER.info("AFTER (Enhanced Binder):");
		LOGGER.info(
			"  Validation error for CMeetingStatus: Validation error in property 'Entity'");
		LOGGER.info("  Found 2 validation error(s) for bean type: CMeetingStatus");
		LOGGER.info("  Field 'name' validation failed:");
		LOGGER.info("    → Error: Name is required");
		LOGGER.info("  Field 'color' validation failed:");
		LOGGER.info("    → Error: Invalid color format");
	}

	/**
	 * GLOBAL CONFIGURATION: Set enhanced binder as default
	 */
	public static void enableEnhancedBindersGlobally() {
		// Enable enhanced binders by default
		CBinderFactory.setUseEnhancedBinderByDefault(true);
	}

	/**
	 * UTILITY: Update existing abstract entity page with enhanced error handling
	 */
	public static void updateAbstractEntityPageSaveMethod() {
		// This shows how to update the save method in CAbstractEntityDBPage Replace the
		// existing ValidationException catch block with:
		/*
		 * } catch (final ValidationException validationException) {
		 * CValidationUtils.handleValidationException(getBinder(), validationException,
		 * entityClass.getSimpleName()); }
		 */
	}
}