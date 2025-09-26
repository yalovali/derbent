package tech.derbent.api.exceptions;

/**
 * Exception thrown when service initialization operations fail.
 * Provides specific error handling for initialization-related operations.
 */
public class CInitializationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CInitializationException(final String message) {
		super(message);
	}

	public CInitializationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}