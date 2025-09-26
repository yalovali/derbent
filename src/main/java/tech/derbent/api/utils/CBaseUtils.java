package tech.derbent.api.utils;

import org.slf4j.Logger;

/** Base utility class providing common patterns and helper methods used across multiple utility classes in the system. This class consolidates common
 * validation, logging, and helper patterns to reduce code duplication and increase maintainability. */
public abstract class CBaseUtils {

	/** Safely gets the simple class name from a fully qualified class name.
	 * @param fqcn the fully qualified class name
	 * @return the simple class name, or the original string if no package separator found */
	protected static String getSimpleClassName(final String fqcn) {
		Check.notBlank(fqcn, "Class name cannot be null or blank");
		final int lastDotIndex = fqcn.lastIndexOf('.');
		return (lastDotIndex >= 0 && lastDotIndex < fqcn.length() - 1) ? fqcn.substring(lastDotIndex + 1) : fqcn;
	}

	/** Creates a standardized error message with context information.
	 * @param operation the operation that failed
	 * @param context   additional context information
	 * @param cause     the underlying cause (optional)
	 * @return formatted error message */
	protected static String createErrorMessage(final String operation, final String context, final Throwable cause) {
		Check.notBlank(operation, "Operation cannot be null or blank");
		Check.notBlank(context, "Context cannot be null or blank");
		final StringBuilder message = new StringBuilder();
		message.append("Failed to ").append(operation);
		message.append(" for ").append(context);
		if (cause != null) {
			message.append(": ").append(cause.getMessage());
		}
		return message.toString();
	}

	/** Logs an error with consistent formatting and returns a formatted message.
	 * @param logger    the logger to use
	 * @param operation the operation that failed
	 * @param context   the context information
	 * @param cause     the underlying cause
	 * @return formatted error message for exceptions */
	protected static String logAndCreateErrorMessage(final Logger logger, final String operation, final String context, final Throwable cause) {
		final String errorMessage = createErrorMessage(operation, context, cause);
		logger.error(errorMessage, cause);
		return errorMessage;
	}

	/** Validates that a string is not null, not empty, and not blank. This is a common pattern used across utility classes.
	 * @param value         the string to validate
	 * @param parameterName the parameter name for error messages
	 * @throws IllegalArgumentException if validation fails */
	protected static void validateStringParameter(final String value, final String parameterName) {
		Check.notNull(value, parameterName + " cannot be null");
		Check.notBlank(value, parameterName + " cannot be blank or empty");
	}
}
