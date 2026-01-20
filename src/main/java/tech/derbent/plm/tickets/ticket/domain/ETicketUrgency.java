package tech.derbent.plm.tickets.ticket.domain;

/**
 * Enum representing the urgency levels of a ticket.
 * Urgency indicates how quickly the ticket needs to be addressed.
 */
public enum ETicketUrgency {

	/** Critical urgency - immediate action required */
	CRITICAL("Critical", "Immediate action required"),

	/** High urgency - requires prompt attention */
	HIGH("High", "Requires prompt attention"),

	/** Medium urgency - normal processing */
	MEDIUM("Medium", "Normal processing timeframe"),

	/** Low urgency - can be addressed later */
	LOW("Low", "Can be addressed in due course");

	private final String displayName;
	private final String description;

	ETicketUrgency(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDescription() { return description; }

	public String getDisplayName() { return displayName; }
}
