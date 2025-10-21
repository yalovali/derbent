package tech.derbent.api.exceptions;
/** CExceptionNotify - Exception for user-facing validation and business rule errors. Unlike technical exceptions (like CReflectionException,
 * CInitializationException), this exception is intended to be caught and displayed to the end user as a notification. Use this exception when: -
 * Validation rules are violated (e.g., required fields are null) - Business rules prevent an operation (e.g., cannot delete a status that's in use) -
 * Data integrity constraints are violated (e.g., status cannot be both from and to) The message should be user-friendly and explain what went wrong
 * and possibly how to fix it. Layer: Exception (Infrastructure) */
public class CExceptionNotify extends Exception {

	private static final long serialVersionUID = 1L;

	public CExceptionNotify() {
		super();
	}

	public CExceptionNotify(final String message) {
		super(message);
	}

	public CExceptionNotify(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CExceptionNotify(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CExceptionNotify(final Throwable cause) {
		super(cause);
	}
}
