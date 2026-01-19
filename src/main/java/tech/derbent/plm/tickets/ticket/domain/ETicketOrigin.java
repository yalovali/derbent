package tech.derbent.plm.tickets.ticket.domain;

/**
 * Enum representing the origin or channel through which a ticket was created.
 */
public enum ETicketOrigin {

	/** Ticket created via email */
	EMAIL("Email", "Submitted via email"),

	/** Ticket created via phone call */
	PHONE("Phone", "Submitted via phone call"),

	/** Ticket created via chat/messaging */
	CHAT("Chat", "Submitted via chat or messaging"),

	/** Ticket created via web portal */
	WEB("Web Portal", "Submitted via web portal"),

	/** Ticket created via API integration */
	API("API", "Created via API integration"),

	/** Ticket created internally by staff */
	INTERNAL("Internal", "Created internally by staff");

	private final String displayName;
	private final String description;

	ETicketOrigin(final String displayName, final String description) {
		this.displayName = displayName;
		this.description = description;
	}

	public String getDescription() { return description; }

	public String getDisplayName() { return displayName; }
}
