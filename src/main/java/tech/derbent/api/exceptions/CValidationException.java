package tech.derbent.api.exceptions;

/** CValidationException - Exception for validation rule violations that should be surfaced to the user. */
public class CValidationException extends CException {

	private static final long serialVersionUID = 1L;

	public CValidationException() {
		super();
	}

	public CValidationException(final String message) {
		super(message);
	}

	public CValidationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CValidationException(final Throwable cause) {
		super(cause);
	}
}
