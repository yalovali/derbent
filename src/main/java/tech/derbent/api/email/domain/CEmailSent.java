package tech.derbent.api.email.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;

/**
 * CEmailSent - Entity representing successfully sent emails (archive).
 * 
 * Domain Layer (MVC Pattern)
 * 
 * This is the archive of successfully sent emails:
 * 1. Emails are moved here from CEmailQueued after successful send
 * 2. Provides audit trail of all sent emails
 * 3. Used for tracking and compliance
 * 4. Can be purged periodically based on retention policy
 * 
 * Database Table: cemail_sent
 * 
 * @author Derbent Team
 * @since 2026-02-11
 */
@Entity
@Table(name = "cemail_sent")
@AttributeOverride(name = "id", column = @Column(name = "email_sent_id"))
public class CEmailSent extends CEmail<CEmailSent> {

	public static final String DEFAULT_COLOR = "#4CAF50"; // Material Green - success
	public static final String DEFAULT_ICON = "vaadin:check-circle";
	public static final String ENTITY_TITLE_PLURAL = "Sent Emails";
	public static final String ENTITY_TITLE_SINGULAR = "Sent Email";
	public static final String VIEW_NAME = "Sent Emails View";

	/** Default constructor for JPA. */
	protected CEmailSent() {
	}

	/** Business constructor for sent emails. */
	public CEmailSent(final String subject, final String toEmail, final CCompany company) {
		super(CEmailSent.class, subject, toEmail, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		// Set sent timestamp
		setSentAt(java.time.LocalDateTime.now());
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public String toString() {
		return "CEmailSent[id=%d, subject='%s', to='%s', sentAt=%s]"
			.formatted(getId(), getSubject(), getToEmail(), getSentAt());
	}
}
