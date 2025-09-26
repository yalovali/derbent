package tech.derbent.api.exceptions;
/** Exception thrown when image processing operations fail. Provides specific error handling for image-related operations. */
public class CImageProcessingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public CImageProcessingException(final String message) {
		super(message);
	}

	public CImageProcessingException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
