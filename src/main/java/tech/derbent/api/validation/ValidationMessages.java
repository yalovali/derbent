package tech.derbent.api.validation;
/** Centralized validation messages for JPA entity validation. Provides consistent, reusable error messages across the application. Usage in entities:
 * @NotBlank(message = ValidationMessages.FIELD_REQUIRED)
 * @Size(max = 255, message = ValidationMessages.FIELD_MAX_LENGTH) */
public final class ValidationMessages {
	// ========== Generic Field Validation Messages ==========

	/** Color code format */
	public static final String COLOR_INVALID_FORMAT = "Color must be a valid hex code (e.g., #FF5733)";
	/** Color code maximum length */
	public static final String COLOR_MAX_LENGTH = "Color code cannot exceed {max} characters";
	/** Company reference is required */
	public static final String COMPANY_REQUIRED = "Company reference is required";
	/** Data provider bean maximum length */
	public static final String DATA_PROVIDER_MAX_LENGTH = "Data provider bean cannot exceed {max} characters";
	/** Date in future not allowed */
	public static final String DATE_FUTURE_NOT_ALLOWED = "Date cannot be in the future";
	/** Date in past not allowed */
	public static final String DATE_PAST_NOT_ALLOWED = "Date cannot be in the past";
	// ========== Specific Field Type Messages ==========
	/** Default value maximum length */
	public static final String DEFAULT_VALUE_MAX_LENGTH = "Default value cannot exceed {max} characters";
	/** Cannot delete - has children */
	public static final String DELETE_HAS_CHILDREN = "Cannot delete: This record has {count} child record(s)";
	/** Cannot delete - has references */
	public static final String DELETE_HAS_REFERENCES = "Cannot delete: This record is referenced by {count} other record(s)";
	/** Cannot delete - in use */
	public static final String DELETE_IN_USE = "Cannot delete: This record is currently in use";
	/** Cannot delete - marked as non-deletable */
	public static final String DELETE_NON_DELETABLE = "Cannot delete: This record is marked as non-deletable";
	/** Cannot delete - generic */
	public static final String DELETE_NOT_ALLOWED = "Cannot delete: Operation not allowed";
	// ========== Numeric Field Messages ==========
	/** Cannot delete - system record */
	public static final String DELETE_SYSTEM_RECORD = "Cannot delete: System records cannot be deleted";
	/** Description field maximum length */
	public static final String DESCRIPTION_MAX_LENGTH = "Description cannot exceed {max} characters";
	/** Duplicate email */
	public static final String DUPLICATE_EMAIL = "This email address is already registered";
	/** Duplicate name in scope */
	public static final String DUPLICATE_NAME = "A record with this name already exists";
	/** Duplicate name in company */
	public static final String DUPLICATE_NAME_IN_COMPANY = "A record with this name already exists in this company";
	/** Duplicate name in project */
	public static final String DUPLICATE_NAME_IN_PROJECT = "A record with this name already exists in this project";
	// ========== Color Field Messages ==========
	/** Duplicate username */
	public static final String DUPLICATE_USERNAME = "This username is already taken";
	/** Email field format validation */
	public static final String EMAIL_INVALID = "Email must be a valid email address";
	// ========== Relationship Field Messages ==========
	/** Email field maximum length */
	public static final String EMAIL_MAX_LENGTH = "Email cannot exceed {max} characters";
	/** Email field is required */
	public static final String EMAIL_REQUIRED = "Email is required";
	/** End date must be after start date */
	public static final String END_DATE_AFTER_START = "End date must be after start date";
	/** End date is required */
	public static final String END_DATE_REQUIRED = "End date is required";
	/** Entity reference is required */
	public static final String ENTITY_REFERENCE_REQUIRED = "Entity reference is required";
	// ========== Delete Constraint Messages ==========
	/** Field caption maximum length */
	public static final String FIELD_CAPTION_MAX_LENGTH = "Field caption cannot exceed {max} characters";
	/** Field caption is required */
	public static final String FIELD_CAPTION_REQUIRED = "Field caption is required";
	/** Field description maximum length */
	public static final String FIELD_DESCRIPTION_MAX_LENGTH = "Field description cannot exceed {max} characters";
	/** Generic message for max length violations */
	public static final String FIELD_MAX_LENGTH = "{field.maxLength}";
	/** Generic message for min length violations */
	public static final String FIELD_MIN_LENGTH = "{field.minLength}";
	/** Generic message for blank fields */
	public static final String FIELD_NOT_BLANK = "{field.notBlank}";
	// ========== Unique Constraint Messages ==========
	/** Generic message for null fields */
	public static final String FIELD_NOT_NULL = "{field.notNull}";
	/** Field property maximum length */
	public static final String FIELD_PROPERTY_MAX_LENGTH = "Field property name cannot exceed {max} characters";
	/** Field property name is required */
	public static final String FIELD_PROPERTY_REQUIRED = "Field property name is required";
	/** Generic message for required fields */
	public static final String FIELD_REQUIRED = "{field.required}";
	/** Generic message for size violations */
	public static final String FIELD_SIZE = "{field.size}";
	// ========== Business Rule Messages ==========
	/** Invalid state */
	public static final String INVALID_STATE = "Record is in an invalid state for this operation";
	/** Line order maximum */
	public static final String LINE_ORDER_MAX = "Line order cannot exceed {value}";
	/** Line order minimum */
	public static final String LINE_ORDER_MIN = "Line order must be at least {value}";
	/** Line order required */
	public static final String LINE_ORDER_REQUIRED = "Line order is required";
	// ========== Date/Time Messages ==========
	/** Max length maximum */
	public static final String MAX_LENGTH_MAX = "Max length cannot exceed {value}";
	/** Max length minimum */
	public static final String MAX_LENGTH_MIN = "Max length must be at least {value}";
	/** Name field maximum length */
	public static final String NAME_MAX_LENGTH = "Name cannot exceed {max} characters";
	/** Name field is required */
	public static final String NAME_REQUIRED = "Name is required";
	/** Parent reference is required */
	public static final String PARENT_REQUIRED = "Parent reference is required";
	// ========== Custom Field Messages ==========
	/** Permission denied */
	public static final String PERMISSION_DENIED = "You do not have permission to perform this operation";
	/** Project reference is required */
	public static final String PROJECT_REQUIRED = "Project reference is required";
	/** Related entity type maximum length */
	public static final String RELATED_ENTITY_TYPE_MAX_LENGTH = "Related entity type cannot exceed {max} characters";
	/** Relation field name maximum length */
	public static final String RELATION_FIELD_NAME_MAX_LENGTH = "Relation field name cannot exceed {max} characters";
	/** Screen reference is required */
	public static final String SCREEN_REFERENCE_REQUIRED = "Screen reference is required";
	/** Section name maximum length */
	public static final String SECTION_NAME_MAX_LENGTH = "Section name cannot exceed {max} characters";
	/** Sort order maximum */
	public static final String SORT_ORDER_MAX = "Sort order cannot exceed {value}";
	/** Sort order minimum */
	public static final String SORT_ORDER_MIN = "Sort order must be at least {value}";
	/** Sort order required */
	public static final String SORT_ORDER_REQUIRED = "Sort order is required";
	/** Start date is required */
	public static final String START_DATE_REQUIRED = "Start date is required";
	/** Status transition not allowed */
	public static final String STATUS_TRANSITION_NOT_ALLOWED = "Status transition from '{from}' to '{to}' is not allowed";
	/** User reference is required */
	public static final String USER_REQUIRED = "User reference is required";
	/** Generic maximum value message */
	public static final String VALUE_MAX = "Value cannot exceed {value}";
	/** Generic minimum value message */
	public static final String VALUE_MIN = "Value must be at least {value}";
	/** Generic range message */
	public static final String VALUE_RANGE = "Value must be between {min} and {max}";
	/** Workflow violation */
	public static final String WORKFLOW_VIOLATION = "Operation violates workflow rules";
	// ========== Helper Methods for Dynamic Messages ==========

	/** Replace {count} placeholder with actual count
	 * @param message the message template
	 * @param count   the count value
	 * @return formatted message */
	public static String formatCount(String message, long count) {
		return message.replace("{count}", String.valueOf(count));
	}

	/** Replace {max} placeholder with actual value
	 * @param message the message template
	 * @param max     the maximum value
	 * @return formatted message */
	public static String formatMaxLength(String message, int max) {
		return message.replace("{max}", String.valueOf(max));
	}

	/** Replace {min} and {max} placeholders with actual values
	 * @param message the message template
	 * @param min     the minimum value
	 * @param max     the maximum value
	 * @return formatted message */
	public static String formatRange(String message, int min, int max) {
		return message.replace("{min}", String.valueOf(min)).replace("{max}", String.valueOf(max));
	}

	/** Replace {from} and {to} placeholders for status transitions
	 * @param message the message template
	 * @param from    the from status
	 * @param to      the to status
	 * @return formatted message */
	public static String formatStatusTransition(String message, String from, String to) {
		return message.replace("{from}", from).replace("{to}", to);
	}

	/** Replace {value} placeholder with actual value
	 * @param message the message template
	 * @param value   the value
	 * @return formatted message */
	public static String formatValue(String message, int value) {
		return message.replace("{value}", String.valueOf(value));
	}

	// Private constructor to prevent instantiation
	private ValidationMessages() {
		throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
	}
}
