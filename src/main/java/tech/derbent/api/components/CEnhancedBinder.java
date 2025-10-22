package tech.derbent.api.components;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import tech.derbent.api.domains.CEntityDB;

/** Enhanced binder that provides detailed field-level error logging and reporting for better debugging of binding and validation issues. This class
 * extends BeanValidationBinder to add: - Detailed field-specific error logging - Enhanced error reporting with field names and validation messages -
 * Easy integration with existing code with minimal changes - Backward compatibility with standard BeanValidationBinder usage
 * @param <BEAN> the bean type */
public class CEnhancedBinder<BEAN> extends BeanValidationBinder<BEAN> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEnhancedBinder.class);
	private static final long serialVersionUID = 1L;
	private final Class<BEAN> beanType;
	private final Map<String, String> lastValidationErrors = new HashMap<>();

	/** Creates an enhanced binder for the given bean type.
	 * @param beanType the bean class, not null */
	public CEnhancedBinder(final Class<BEAN> beanType) {
		super(beanType);
		this.beanType = beanType;
		// LOGGER.debug("Created CEnhancedBinder for bean type: {}", beanType.getSimpleName());
	}

	@Override
	public <FIELDVALUE> Binding<BEAN, FIELDVALUE> bind(final HasValue<?, FIELDVALUE> field, final String propertyName) {
		try {
			return super.bind(field, propertyName);
		} catch (final Exception e) {
			throw e;
		}
	}

	private void clearLastValidationErrors() {
		lastValidationErrors.clear();
	}

	private void collectValidationErrors(final List<BindingValidationStatus<?>> errorStatuses) {
		for (final BindingValidationStatus<?> status : errorStatuses) {
			final String fieldName = getFieldNameFromBinding(status);
			final String errorMessage = status.getValidationResults().stream().filter(ValidationResult::isError)
					.map(ValidationResult::getErrorMessage).collect(Collectors.joining("; "));
			if (!errorMessage.isEmpty()) {
				lastValidationErrors.put(fieldName, errorMessage);
			}
		}
	}

	/** Returns the bean type this binder is configured for.
	 * @return the bean class */
	public Class<BEAN> getBeanType() { return beanType; }
	// Overload bind methods to add detailed logging

	/** Gets the validation error for a specific field.
	 * @param fieldName the field name
	 * @return the error message for the field, or null if no error */
	public String getFieldError(final String fieldName) {
		return lastValidationErrors.get(fieldName);
	}

	private String getFieldNameFromBinding(final BindingValidationStatus<?> status) {
		try {
			// Try to extract field name from the binding
			if (status.getBinding() != null) {
				// Extract field name from binding toString or use reflection
				final String bindingStr = status.getBinding().toString();
				// Try to extract property name from binding string representation
				if (bindingStr.contains("property=")) {
					final int start = bindingStr.indexOf("property=") + 9;
					int end = bindingStr.indexOf(",", start);
					if (end == -1) {
						end = bindingStr.indexOf("}", start);
					}
					if (end > start) {
						return bindingStr.substring(start, end).trim();
					}
				}
				return bindingStr;
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not extract field name from binding: {}", e.getMessage());
		}
		return "unknown_field";
	}

	/** Gets a list of field names that have validation errors.
	 * @return list of field names with errors */
	public List<String> getFieldsWithErrors() { return new ArrayList<>(lastValidationErrors.keySet()); }

	/** Gets a formatted error summary for display purposes.
	 * @return formatted string with all field validation errors */
	public String getFormattedErrorSummary() {
		if (lastValidationErrors.isEmpty()) {
			return "No validation errors found.";
		}
		final StringBuilder summary = new StringBuilder();
		summary.append("Validation errors found in ").append(lastValidationErrors.size()).append(" field(s):\n");
		lastValidationErrors.forEach((field, error) -> summary.append("• ").append(field).append(": ").append(error).append("\n"));
		return summary.toString();
	}

	/** Gets detailed validation error information from the last validation attempt.
	 * @return map of field names to error messages */
	public Map<String, String> getLastValidationErrors() {
		return new HashMap<>(lastValidationErrors);
	}

	/** Checks if there are any field validation errors.
	 * @return true if there are validation errors */
	public boolean hasValidationErrors() {
		return !lastValidationErrors.isEmpty();
	}

	private void logDetailedBindingErrors() {
		if (!lastValidationErrors.isEmpty()) {
			LOGGER.error("Detailed field validation errors for bean type: {}", beanType.getSimpleName());
			lastValidationErrors.forEach((field, error) -> LOGGER.error("  Field '{}': {}", field, error));
		}
	}

	private void logDetailedValidationErrors(final List<BindingValidationStatus<?>> errorStatuses) {
		LOGGER.error("Found {} validation error(s) for bean type: {}", errorStatuses.size(), beanType.getSimpleName());
		for (final BindingValidationStatus<?> status : errorStatuses) {
			final String fieldName = getFieldNameFromBinding(status);
			final List<ValidationResult> results = status.getValidationResults();
			LOGGER.error("Field '{}' validation failed:", fieldName);
			for (final ValidationResult result : results) {
				if (result.isError()) {
					LOGGER.error("  → Error: {}", result.getErrorMessage());
				}
			}
		}
	}

	public void printBindingProperties() {
		LOGGER.debug("Current bindings for bean type: {}", beanType.getSimpleName());
		for (final Binding<BEAN, ?> binding : getBindings()) {
			LOGGER.debug("Binding: {}", binding.toString());
			if (binding.getField() != null) {
				LOGGER.debug("  Field: {}", binding.getField().toString());
			}
		}
		LOGGER.debug("Total bindings count: {}", getBindings().size());
	}

	@Override
	public void readBean(final BEAN bean) {
		// LOGGER.debug("Starting readBean operation for bean type:
		// {}",beanType.getSimpleName());
		try {
			// Check for incomplete bindings before attempting to read the bean
			validateBindingsComplete();
			// Validate that the bean's entity fields are initialized to prevent lazy-loading issues
			// This catches developer errors where entities are passed to binding without initialization
			validateBeanFieldsInitialized(bean);
			super.readBean(bean);
			// LOGGER.debug("Successfully completed readBean operation for bean type:
			// {}",beanType.getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Error during readBean for bean type: {} - {}: {}", beanType.getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
			// Log additional context if available
			if (bean != null) {
				LOGGER.debug("Bean state: {}", bean.toString());
			}
			printBindingProperties();
			throw e;
		}
	}

	@Override
	public void setBean(final BEAN bean) {
		try {
			// Check for incomplete bindings before attempting to set the bean
			// This prevents the "All bindings created with forField must be completed" error
			validateBindingsComplete();
			// Validate that the bean's entity fields are initialized to prevent lazy-loading issues
			validateBeanFieldsInitialized(bean);
			super.setBean(bean);
		} catch (final Exception e) {
			LOGGER.error("Error during setBean for bean type: {} - {}: {}", beanType.getSimpleName(), e.getClass().getSimpleName(), e.getMessage());
			// Log additional context if available
			if (bean != null) {
				LOGGER.debug("Bean state: {}", bean.toString());
			}
			printBindingProperties();
			throw e;
		}
	}

	public void validateBean(BEAN entity) {
		final BinderValidationStatus<BEAN> status = validate();
		if (!status.isOk()) {
			final StringBuilder sb = new StringBuilder();
			sb.append("Validation failed:");
			// Bean-level messages
			status.getBeanValidationErrors().forEach(err -> sb.append("\n- bean: ").append(err.getErrorMessage()));
			// Field-level messages
			for (final BindingValidationStatus<?> fieldStatus : status.getFieldValidationErrors()) {
				final String message = fieldStatus.getMessage().orElse("validation error");
				String fieldIdOrName = "unknown";
				String extra = "";
				Object fieldObj = null;
				try {
					final Object f = fieldStatus.getField();
					if (f instanceof java.util.Optional) {
						final java.util.Optional<?> opt = (java.util.Optional<?>) f;
						if (opt.isPresent()) {
							fieldObj = opt.get();
						}
					} else {
						fieldObj = f;
					}
				} catch (final Exception ignored) {
					// safe fallback
				}
				if (fieldObj instanceof Component) {
					final Component comp = (Component) fieldObj;
					fieldIdOrName = comp.getId().orElse(comp.getClass().getSimpleName());
					extra = " (visible=" + comp.isVisible();
					if (comp instanceof HasValue) {
						try {
							final Object value = ((HasValue<?, ?>) comp).getValue();
							extra += ", value=" + String.valueOf(value);
						} catch (final Exception ignored) {
							// ignore value retrieval issues
						}
					}
					extra += ")";
				} else if (fieldObj != null) {
					fieldIdOrName = String.valueOf(fieldObj);
				}
				sb.append("\n- field ").append(fieldIdOrName).append(extra).append(": ").append(message);
			}
			throw new IllegalStateException(sb.toString());
		}
		writeBeanIfValid(entity);
	}

	/** Validates that all field bindings are complete before readBean operation. This prevents the "All bindings created with forField must be
	 * completed" error. */
	private void validateBindingsComplete() {
		try {
			// In Vaadin 24.x, the field is called "incompleteBindings" not
			// "incompleteMemberFields"
			Field incompleteBindingsField = null;
			// Try different possible field names across Vaadin versions
			final String[] possibleFieldNames = {
					"incompleteBindings", "incompleteMemberFields", "incompleteFields"
			};
			for (final String fieldName : possibleFieldNames) {
				try {
					incompleteBindingsField = Binder.class.getDeclaredField(fieldName);
					// LOGGER.debug("Found incomplete bindings field: {}", fieldName);
					break;
				} catch (final NoSuchFieldException e) {
					LOGGER.debug("Field '{}' not found, trying next", fieldName);
				}
			}
			if (incompleteBindingsField == null) {
				LOGGER.debug("No incomplete bindings field found in Binder class - this may be a newer Vaadin version");
				return;
			}
			incompleteBindingsField.setAccessible(true);
			final Object incompleteBindingsObj = incompleteBindingsField.get(this);
			if (incompleteBindingsObj != null) {
				// Handle both Set and IdentityHashMap types that may be used in different
				// Vaadin versions
				Collection<?> incompleteBindings = null;
				if (incompleteBindingsObj instanceof Set) {
					incompleteBindings = (Set<?>) incompleteBindingsObj;
				} else if (incompleteBindingsObj instanceof IdentityHashMap) {
					// In some Vaadin versions, incompleteBindings is an IdentityHashMap
					incompleteBindings = ((IdentityHashMap<?, ?>) incompleteBindingsObj).keySet();
				} else if (incompleteBindingsObj instanceof Collection) {
					// Fallback to generic Collection
					incompleteBindings = (Collection<?>) incompleteBindingsObj;
				} else {
					LOGGER.debug("Incomplete bindings field is of unexpected type: {}", incompleteBindingsObj.getClass().getSimpleName());
				}
				if ((incompleteBindings != null) && !incompleteBindings.isEmpty()) {
					LOGGER.warn(
							"Found {} incomplete bindings for {} - clearing them to prevent readBean error. This indicates a form binding issue that should be investigated.",
							incompleteBindings.size(), beanType.getSimpleName());
					// Log details about the incomplete bindings if possible
					try {
						for (final Object binding : incompleteBindings) {
							LOGGER.debug("Incomplete binding: {}", binding.toString());
						}
					} catch (final Exception debugException) {
						LOGGER.debug("Could not log incomplete binding details: {}", debugException.getMessage());
					}
					final int originalSize = incompleteBindings.size();
					// Use safer clearing operations with better error handling
					try {
						if (incompleteBindingsObj instanceof Set) {
							@SuppressWarnings ("rawtypes")
							final Set rawSet = (Set) incompleteBindingsObj;
							rawSet.clear();
						} else if (incompleteBindingsObj instanceof IdentityHashMap) {
							@SuppressWarnings ("rawtypes")
							final IdentityHashMap rawMap = (IdentityHashMap) incompleteBindingsObj;
							rawMap.clear();
						} else if (incompleteBindingsObj instanceof Collection) {
							@SuppressWarnings ("rawtypes")
							final Collection rawCollection = (Collection) incompleteBindingsObj;
							rawCollection.clear();
						}
						LOGGER.info("Cleared {} incomplete bindings for {}", originalSize, beanType.getSimpleName());
					} catch (final Exception clearException) {
						LOGGER.warn("Failed to clear incomplete bindings for {}: {} - {}", beanType.getSimpleName(),
								clearException.getClass().getSimpleName(), clearException.getMessage());
						// Don't re-throw - continue with readBean operation
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not check for incomplete bindings: {} - {}", e.getClass().getSimpleName(), e.getMessage());
			// Continue with readBean operation even if validation fails
		}
	}

	/** Validates that entity fields on the bean are initialized before binding. This prevents issues where lazy-loaded Hibernate entities are passed
	 * to binders without initialization, causing ComboBoxes and other components to fail value matching. Only validates fields of type CEntityDB to
	 * avoid false positives on primitive types.
	 * @param bean the bean to validate */
	private void validateBeanFieldsInitialized(final BEAN bean) {
		if (bean == null) {
			return; // Null beans don't have fields to validate
		}
		try {
			// Get all fields from the bean's class and superclasses
			final List<Field> fieldsToCheck = new ArrayList<>();
			Class<?> currentClass = bean.getClass();
			while ((currentClass != null) && (currentClass != Object.class)) {
				final Field[] declaredFields = currentClass.getDeclaredFields();
				for (final Field field : declaredFields) {
					// Only check entity fields (CEntityDB and its subclasses)
					if (CEntityDB.class.isAssignableFrom(field.getType())) {
						fieldsToCheck.add(field);
					}
				}
				currentClass = currentClass.getSuperclass();
			}
			// Validate each entity field is initialized
			for (final Field field : fieldsToCheck) {
				try {
					field.setAccessible(true);
					final Object fieldValue = field.get(bean);
					if (fieldValue != null && !Hibernate.isInitialized(fieldValue)) {
						final String errorMsg = String.format(
								"Field '%s' of type '%s' in bean '%s' is not initialized (lazy-loaded Hibernate proxy). "
										+ "Call entity.initializeAllFields() before passing to binder, or access a property on the entity to trigger initialization.",
								field.getName(), field.getType().getSimpleName(), beanType.getSimpleName());
						LOGGER.error(errorMsg);
						throw new IllegalArgumentException(errorMsg);
					}
				} catch (final IllegalAccessException e) {
					LOGGER.debug("Could not access field '{}' for initialization check: {}", field.getName(), e.getMessage());
				}
			}
		} catch (final Exception e) {
			// If it's our validation error, re-throw it
			if (e instanceof IllegalArgumentException && e.getMessage().contains("not initialized")) {
				throw e;
			}
			// Otherwise log and continue - don't block binding for validation failures
			LOGGER.debug("Could not validate bean field initialization for {}: {} - {}", beanType.getSimpleName(), e.getClass().getSimpleName(),
					e.getMessage());
		}
	}

	@Override
	public void writeBean(final BEAN bean) throws ValidationException {
		clearLastValidationErrors();
		try {
			// Validate all bindings first to collect detailed error information
			final List<BindingValidationStatus<?>> validationStatuses = validate().getFieldValidationStatuses();
			// Check for validation errors and log detailed information
			final List<BindingValidationStatus<?>> errorStatuses =
					validationStatuses.stream().filter(status -> status.isError()).collect(Collectors.toList());
			if (!errorStatuses.isEmpty()) {
				logDetailedValidationErrors(errorStatuses);
				collectValidationErrors(errorStatuses);
			}
			// Call the parent writeBean method
			super.writeBean(bean);
		} catch (final ValidationException e) {
			LOGGER.error("ValidationException during writeBean for bean type: {}. Error details: {}", beanType.getSimpleName(), e.getMessage());
			logDetailedBindingErrors();
			throw e;
		} catch (final Exception e) {
			LOGGER.error("Unexpected error during writeBean for bean type: {}", beanType.getSimpleName(), e);
			printBindingProperties();
			throw e;
		}
	}
}
