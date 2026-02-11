package tech.derbent.api.email.domain;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

/** CEmail - Abstract base class for email entities. Domain Layer (MVC Pattern) This abstract entity represents emails in the system with all common
 * fields. Emails are company-scoped and track sending status, recipients, content, and metadata. Inheritance Hierarchy: - CEmailQueued: Emails
 * waiting to be sent - CEmailSent: Successfully sent emails (archive) Email Flow: 1. Email created as CEmailQueued (in queue) 2. Email processor
 * attempts to send 3. On success: moved to CEmailSent (archive) 4. On failure: retry count incremented, stays in queue
 * @author Derbent Team
 * @since 2026-02-11 */
@MappedSuperclass
public abstract class CEmail<EntityClass extends CEmail<EntityClass>> extends CEntityOfCompany<EntityClass> {

	// Email Status Constants
	public static final String STATUS_PENDING = "PENDING";
	public static final String STATUS_SENDING = "SENDING";
	public static final String STATUS_SENT = "SENT";
	public static final String STATUS_FAILED = "FAILED";

	// CC/BCC Recipients
	@Column(name = "cc_email", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData(
		displayName = "CC Email",
		required = false,
		readOnly = false,
		description = "CC recipient email addresses (comma-separated)",
		hidden = false,
		maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String ccEmail;

	@Column(name = "bcc_email", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size(max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData(
		displayName = "BCC Email",
		required = false,
		readOnly = false,
		description = "BCC recipient email addresses (comma-separated)",
		hidden = false,
		maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String bccEmail;

	@Column (name = "body_html", nullable = true, length = 10000)
	@Size (max = 10000)
	@AMetaData (
			displayName = "Body (HTML)", required = false, readOnly = false, description = "Email body in HTML format", hidden = false,
			maxLength = 10000
	)
	private String bodyHtml;
	@Column (name = "body_text", nullable = true, length = 10000)
	@Size (max = 10000)
	@AMetaData (
			displayName = "Body (Text)", required = false, readOnly = false, description = "Email body in plain text format", hidden = false,
			maxLength = 10000
	)
	private String bodyText;
	// Metadata
	@Column (name = "email_type", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Email Type", required = false, readOnly = false,
			description = "Email type/category (e.g., WELCOME, PASSWORD_RESET, NOTIFICATION)", hidden = false, maxLength = 100
	)
	private String emailType;
	// Sender Information
	@Column (name = "from_email", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@NotBlank (message = "From email is required")
	@Email (message = "Invalid from email format")
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "From Email", required = true, readOnly = false, description = "Sender email address", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String fromEmail;
	@Column (name = "from_name", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "From Name", required = false, readOnly = false, description = "Sender display name", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String fromName;
	@Column (name = "last_error", nullable = true, length = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@Size (max = CEntityConstants.MAX_LENGTH_DESCRIPTION)
	@AMetaData (
			displayName = "Last Error", required = false, readOnly = true, description = "Last error message if send failed", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_DESCRIPTION
	)
	private String lastError;
	@Column (name = "max_retries", nullable = false)
	@AMetaData (displayName = "Max Retries", defaultValue = "3")
	private Integer maxRetries = 3;
	// Email Priority
	@Column (name = "priority", nullable = false, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Priority", required = true, readOnly = false, defaultValue = "NORMAL",
			description = "Email priority level (LOW, NORMAL, HIGH)", hidden = false, maxLength = 50
	)
	private String priority = "NORMAL";
	// Timestamps
	@Column (name = "queued_at", nullable = false)
	@AMetaData (displayName = "Queued At", required = true, readOnly = true, description = "Timestamp when email was queued", hidden = false)
	private LocalDateTime queuedAt;
	@Column (name = "reference_entity_id", nullable = true)
	@AMetaData (
			displayName = "Reference Entity ID", required = false, readOnly = false, description = "ID of entity this email relates to",
			hidden = false
	)
	private Long referenceEntityId;
	@Column (name = "reference_entity_type", nullable = true, length = 100)
	@Size (max = 100)
	@AMetaData (
			displayName = "Reference Entity Type", required = false, readOnly = false, description = "Type of entity this email relates to",
			hidden = false, maxLength = 100
	)
	private String referenceEntityType;
	// Reply-To Information
	@Column (name = "reply_to_email", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Email (message = "Invalid reply-to email format")
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Reply-To Email", required = false, readOnly = false, description = "Reply-to email address", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String replyToEmail;
	@Column (name = "reply_to_name", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Reply-To Name", required = false, readOnly = false, description = "Reply-to display name", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String replyToName;
	// Retry Management (initialized at declaration - nullable=false)
	@Column (name = "retry_count", nullable = false)
	@AMetaData (displayName = "Retry Count", readOnly = true, defaultValue = "0")
	private Integer retryCount = 0;
	@Column (name = "sent_at", nullable = true)
	@AMetaData (
			displayName = "Sent At", required = false, readOnly = true, description = "Timestamp when email was successfully sent", hidden = false
	)
	private LocalDateTime sentAt;
	// Email Content
	@Column (name = "subject", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@NotBlank (message = "Email subject is required")
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "Subject", required = true, readOnly = false, description = "Email subject line", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String subject;
	// Recipient Information
	@Column (name = "to_email", nullable = false, length = CEntityConstants.MAX_LENGTH_NAME)
	@NotBlank (message = "To email is required")
	@Email (message = "Invalid recipient email format")
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "To Email", required = true, readOnly = false, description = "Recipient email address", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String toEmail;
	@Column (name = "to_name", nullable = true, length = CEntityConstants.MAX_LENGTH_NAME)
	@Size (max = CEntityConstants.MAX_LENGTH_NAME)
	@AMetaData (
			displayName = "To Name", required = false, readOnly = false, description = "Recipient display name", hidden = false,
			maxLength = CEntityConstants.MAX_LENGTH_NAME
	)
	private String toName;

	/** Default constructor for JPA. */
	protected CEmail() {
		super();
	}

	/** Business constructor with subject, toEmail, and company. */
	public CEmail(final Class<EntityClass> entityClass, final String subject, final String toEmail, final CCompany company) {
		super(entityClass, subject != null ? subject : "No Subject", company);
		this.toEmail = toEmail;
		// Subject already set in super() call
		// DO NOT call initializeDefaults() - abstract constructor, concrete classes will call it
	}

	public String getBodyHtml() { return bodyHtml; }

	public String getBodyText() { return bodyText; }

	public String getEmailType() { return emailType; }

	// Getters and Setters
	public String getFromEmail() { return fromEmail; }

	public String getFromName() { return fromName; }

	public String getLastError() { return lastError; }

	public Integer getMaxRetries() { return maxRetries; }

	public String getPriority() { return priority; }

	public LocalDateTime getQueuedAt() { return queuedAt; }

	public Long getReferenceEntityId() { return referenceEntityId; }

	public String getReferenceEntityType() { return referenceEntityType; }

	public String getReplyToEmail() { return replyToEmail; }

	public String getReplyToName() { return replyToName; }

	public Integer getRetryCount() { return retryCount; }

	public LocalDateTime getSentAt() { return sentAt; }

	public String getSubject() { return subject; }

	public String getToEmail() { return toEmail; }

	public String getToName() { return toName; }

	public String getCcEmail() { return ccEmail; }

	public void setCcEmail(final String ccEmail) { this.ccEmail = ccEmail; }

	public String getBccEmail() { return bccEmail; }

	public void setBccEmail(final String bccEmail) { this.bccEmail = bccEmail; }

	public boolean hasReachedMaxRetries() {
		return retryCount != null && maxRetries != null && retryCount >= maxRetries;
	}

	public void incrementRetryCount() {
		retryCount = (retryCount == null ? 0 : retryCount) + 1;
	}

	public void setBodyHtml(final String bodyHtml) { this.bodyHtml = bodyHtml; }

	public void setBodyText(final String bodyText) { this.bodyText = bodyText; }

	public void setEmailType(final String emailType) { this.emailType = emailType; }

	public void setFromEmail(final String fromEmail) { this.fromEmail = fromEmail; }

	public void setFromName(final String fromName) { this.fromName = fromName; }

	public void setLastError(final String lastError) { this.lastError = lastError; }

	public void setMaxRetries(final Integer maxRetries) { this.maxRetries = maxRetries; }

	public void setPriority(final String priority) { this.priority = priority; }

	public void setQueuedAt(final LocalDateTime queuedAt) { this.queuedAt = queuedAt; }

	public void setReferenceEntityId(final Long referenceEntityId) { this.referenceEntityId = referenceEntityId; }

	public void setReferenceEntityType(final String referenceEntityType) { this.referenceEntityType = referenceEntityType; }

	public void setReplyToEmail(final String replyToEmail) { this.replyToEmail = replyToEmail; }

	public void setReplyToName(final String replyToName) { this.replyToName = replyToName; }

	public void setRetryCount(final Integer retryCount) { this.retryCount = retryCount; }

	public void setSentAt(final LocalDateTime sentAt) { this.sentAt = sentAt; }

	public void setSubject(final String subject) { this.subject = subject; }

	public void setToEmail(final String toEmail) { this.toEmail = toEmail; }

	public void setToName(final String toName) { this.toName = toName; }
}
