package tech.derbent.app.issues.issue.domain;

/**
 * Enum representing the priority levels of an issue/bug.
 * Priority indicates the urgency of addressing the issue.
 */
public enum EIssuePriority {
	/** Urgent issues requiring immediate attention */
	URGENT("Urgent", "Requires immediate attention"),
	
	/** High priority issues that should be addressed soon */
	HIGH("High", "Should be addressed soon"),
	
	/** Medium priority issues for normal processing */
	MEDIUM("Medium", "Normal priority processing"),
	
	/** Low priority issues that can be addressed later */
	LOW("Low", "Can be addressed later");

	private final String description;
	private final String displayName;

	EIssuePriority(final String displayName, final String description) {
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
