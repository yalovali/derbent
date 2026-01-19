package tech.derbent.plm.issues.issue.domain;

/**
 * Enum representing the severity levels of an issue/bug.
 * Severity indicates the impact or seriousness of the issue.
 */
public enum EIssueSeverity {
	/** Critical issues that block major functionality or cause system crashes */
	CRITICAL("Critical", "Blocks major functionality or causes system crash"),
	
	/** Major issues that significantly impact functionality but have workarounds */
	MAJOR("Major", "Significantly impacts functionality but has workaround"),
	
	/** Minor issues with limited impact on functionality */
	MINOR("Minor", "Limited impact on functionality"),
	
	/** Trivial issues with minimal or cosmetic impact */
	TRIVIAL("Trivial", "Minimal or cosmetic impact");

	private final String description;
	private final String displayName;

	EIssueSeverity(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
