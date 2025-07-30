package tech.derbent.abstracts.domains;

/**
 * Contains constant values used across entity classes to enforce consistent validation
 * constraints and database schema definitions.
 */
public final class CEntityConstants {

	/**
	 * Maximum length for name fields in entities
	 */
	public static final int MAX_LENGTH_NAME = 100;

	/**
	 * Maximum length for description fields in entities
	 */
	public static final int MAX_LENGTH_DESCRIPTION = 2000;

	/**
	 * Default display order for name fields in UI
	 */
	public static final int DEFAULT_NAME_ORDER = 0;

	/**
	 * Default display order for description fields in UI
	 */
	public static final int DEFAULT_DESCRIPTION_ORDER = 1;

	/**
	 * Default display order for audit fields in UI
	 */
	public static final int DEFAULT_AUDIT_FIELD_ORDER = 80;

	public static final int DEFAULT_PAGE_SIZE = 20;

	public static final int MAX_PAGE_SIZE = 1000;

	// Prevent instantiation
	private CEntityConstants() {
		throw new AssertionError("This constants class should not be instantiated");
	}
}
