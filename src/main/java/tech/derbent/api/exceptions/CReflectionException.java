package tech.derbent.api.exceptions;

/**
 * Exception thrown when reflection operations fail.
 * Provides specific error handling for reflection-based operations.
 */
public class CReflectionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CReflectionException(final String message) {
		super(message);
	}

	public CReflectionException(final String message, final Throwable cause) {
		super(message, cause);
	}
}