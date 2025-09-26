package tech.derbent.api.exceptions;
public class CException extends Exception {

	private static final long serialVersionUID = 1L;

	public CException() {
		super();
	}

	public CException(final String message) {
		super(message);
	}

	public CException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CException(final Throwable cause) {
		super(cause);
	}
}
