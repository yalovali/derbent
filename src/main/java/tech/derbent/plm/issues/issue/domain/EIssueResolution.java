package tech.derbent.plm.issues.issue.domain;

/**
 * Enum representing the resolution status of a closed issue.
 */
public enum EIssueResolution {
	/** Issue was fixed */
	FIXED("Fixed", "Issue was resolved and fixed"),
	
	/** Issue was a duplicate of another issue */
	DUPLICATE("Duplicate", "Duplicate of another issue"),
	
	/** Issue was not reproducible */
	NOT_REPRODUCIBLE("Not Reproducible", "Could not reproduce the issue"),
	
	/** Issue will not be fixed */
	WONT_FIX("Won't Fix", "Issue will not be fixed"),
	
	/** Issue is not actually a bug */
	NOT_A_BUG("Not a Bug", "Works as designed, not a bug"),
	
	/** Issue cannot be reproduced */
	CANNOT_REPRODUCE("Cannot Reproduce", "Unable to reproduce the issue"),
	
	/** None - issue not yet resolved */
	NONE("None", "Issue not yet resolved");

	private final String description;
	private final String displayName;

	EIssueResolution(final String displayName, final String description) {
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
