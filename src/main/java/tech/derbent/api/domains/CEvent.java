package tech.derbent.api.domains;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.users.domain.CUser;

/** CEvent - Abstract base class for event-based entities in the system. Layer: Domain (MVC) Provides common fields and functionality for event-like
 * entities such as: - Comments - Notifications - Activity logs - Status changes This class extends CEntityOfProject to maintain project context for
 * all events. */
@MappedSuperclass
public abstract class CEvent<EntityClass> extends CEntityDB<EntityClass> {

	// Event timestamp - when the event occurred
	@Column (name = "event_date", nullable = false)
	@AMetaData (
			displayName = "Event Date", required = true, readOnly = true, description = "Date and time when the event occurred", hidden = false,
			order = 90
	)
	private LocalDateTime eventDate;
	// Author of the event
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "author_id", nullable = false)
	@AMetaData (
			displayName = "Author", required = true, readOnly = true, description = "User who created this event", hidden = false, order = 91,
			dataProviderBean = "CUserService"
	)
	private CUser author;

	/** Default constructor for JPA. */
	protected CEvent() {
		super();
		this.eventDate = LocalDateTime.now();
	}

	// Default constructor for JPA
	public CEvent(final Class<EntityClass> clazz) {
		super(clazz);
		this.eventDate = LocalDateTime.now();
	}

	public CUser getAuthor() { return author; }

	public String getAuthorName() { return (author != null) ? author.getName() : "Unknown Author"; }

	public LocalDateTime getEventDate() { return eventDate; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (this.eventDate == null) {
			this.eventDate = LocalDateTime.now();
		}
	}

	public void setAuthor(final CUser author) { this.author = author; }

	public void setEventDate(final LocalDateTime eventDate) { this.eventDate = eventDate; }

	@Override
	public String toString() {
		return String.format("%s{eventDate=%s, author=%s}", super.toString(), eventDate, getAuthorName());
	}
}
