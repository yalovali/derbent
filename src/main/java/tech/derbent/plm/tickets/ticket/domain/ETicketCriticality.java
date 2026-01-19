package tech.derbent.plm.tickets.ticket.domain;

/**
 * Enum representing the criticality or system impact level of a ticket.
 * Criticality indicates the overall importance and business impact.
 */
public enum ETicketCriticality {

	/** Critical impact - system down or severe business impact */
	CRITICAL("Critical", "System down or severe business impact"),

	/** High impact - major functionality affected */
	HIGH("High", "Major functionality affected"),

	/** Medium impact - moderate functionality affected */
	MEDIUM("Medium", "Moderate functionality affected"),

	/** Low impact - minor issue with minimal business impact */
	LOW("Low", "Minor issue with minimal impact");

	private final String displayName;
	private final String description;

	ETicketCriticality(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDescription() { return description; }

	public String getDisplayName() { return displayName; }
}
