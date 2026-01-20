package tech.derbent.plm.tickets.ticket.domain;

/**
 * Enum representing how a ticket was resolved or closed.
 */
public enum ETicketResolution {

	/** Ticket issue was fixed */
	FIXED("Fixed", "Issue was resolved and fixed"),

	/** Ticket is duplicate of another ticket */
	DUPLICATE("Duplicate", "Duplicate of another ticket"),

	/** Ticket will not be fixed */
	WONT_FIX("Won't Fix", "Will not be addressed"),

	/** Issue could not be reproduced */
	CANNOT_REPRODUCE("Cannot Reproduce", "Unable to reproduce the issue"),

	/** Ticket behavior is as designed */
	WORKING_AS_DESIGNED("Working as Designed", "Behavior is intended and correct"),

	/** Ticket resolved by workaround */
	WORKAROUND("Workaround", "Resolved with a workaround solution"),

	/** Ticket cancelled by requestor */
	CANCELLED("Cancelled", "Ticket was cancelled"),

	/** No resolution specified */
	NONE("None", "No resolution specified");

	private final String displayName;
	private final String description;

	ETicketResolution(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDescription() { return description; }

	public String getDisplayName() { return displayName; }
}
