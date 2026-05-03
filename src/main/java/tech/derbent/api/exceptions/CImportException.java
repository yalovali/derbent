package tech.derbent.api.exceptions;

/**
 * Exception thrown during Excel import when data is invalid or missing required references.
 * <p>
 * WHY: Import errors should immediately stop processing and report the exact row/issue,
 * not propagate through complex result handling chains.
 * </p>
 */
public class CImportException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int rowNumber;

	public CImportException(final int rowNumber, final String message) {
		super("Import error at row " + rowNumber + ": " + message);
		this.rowNumber = rowNumber;
	}

	public CImportException(final int rowNumber, final String message, final Throwable cause) {
		super("Import error at row " + rowNumber + ": " + message, cause);
		this.rowNumber = rowNumber;
	}

	public int getRowNumber() {
		return rowNumber;
	}
}
