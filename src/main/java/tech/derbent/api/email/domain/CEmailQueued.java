package tech.derbent.api.email.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;

/**
 * CEmailQueued - Entity representing emails waiting to be sent.
 * 
 * Domain Layer (MVC Pattern)
 * 
 * Emails in the queue are processed by the email service:
 * 1. Emails are created in this queue
 * 2. Background processor attempts to send
 * 3. On success: moved to CEmailSent (archive)
 * 4. On failure: retry count incremented
 * 5. After max retries: marked as failed (stays in queue for manual review)
 * 
 * Database Table: cemail_queued
 * 
 * @author Derbent Team
 * @since 2026-02-11
 */
@Entity
@Table(name = "cemail_queued")
@AttributeOverride(name = "id", column = @Column(name = "email_queued_id"))
public class CEmailQueued extends CEmail<CEmailQueued> {

	public static final String DEFAULT_COLOR = "#FF9800"; // Material Orange - pending action
	public static final String DEFAULT_ICON = "vaadin:clock";
	public static final String ENTITY_TITLE_PLURAL = "Queued Emails";
	public static final String ENTITY_TITLE_SINGULAR = "Queued Email";
	public static final String VIEW_NAME = "Queued Emails View";

	// Queue-specific fields
	@Column(name = "status", nullable = false, length = 50)
	private String status = STATUS_PENDING;

	@Column(name = "scheduled_for", nullable = true)
	private java.time.LocalDateTime scheduledFor;

	/** Default constructor for JPA. */
	protected CEmailQueued() {
		super();
	}

	/** Business constructor for queued emails. */
	public CEmailQueued(final String subject, final String toEmail, final CCompany company) {
		super(CEmailQueued.class, subject, toEmail, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		// Set timestamp at creation
		setQueuedAt(java.time.LocalDateTime.now());
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public String getStatus() { return status; }

	public void setStatus(final String status) { this.status = status; }

	public java.time.LocalDateTime getScheduledFor() { return scheduledFor; }

	public void setScheduledFor(final java.time.LocalDateTime scheduledFor) { this.scheduledFor = scheduledFor; }

	@Override
	public String toString() {
		return "CEmailQueued[id=%d, subject='%s', to='%s', retryCount=%d, queuedAt=%s]"
			.formatted(getId(), getSubject(), getToEmail(), getRetryCount(), getQueuedAt());
	}
}
