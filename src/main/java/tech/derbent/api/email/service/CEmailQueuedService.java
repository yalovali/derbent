package tech.derbent.api.email.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;

/** CEmailQueuedService - Service for queued email management. Service Layer (MVC Pattern) Handles business logic for emails waiting to be sent: -
 * Queue management - Retry logic - Move to sent archive - Cleanup operations
 * @author Derbent Team
 * @since 2026-02-11 */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CEmailQueuedService extends CEntityOfCompanyService<CEmailQueued> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailQueuedService.class);
	private final CEmailSentService emailSentService;

	public CEmailQueuedService(final IEmailQueuedRepository repository, final Clock clock, final ISessionService sessionService,
			final CEmailSentService emailSentService) {
		super(repository, clock, sessionService);
		this.emailSentService = emailSentService;
	}

	/** Clean up old queued emails (older than retention period).
	 * @param company    the company
	 * @param beforeDate cutoff date
	 * @return count of deleted emails */
	@Transactional
	public int cleanupOldEmails(final CCompany company, final LocalDateTime beforeDate) {
		Check.notNull(company, "Company cannot be null");
		Check.notNull(beforeDate, "Before date cannot be null");
		final List<CEmailQueued> oldEmails = ((IEmailQueuedRepository) repository).findQueuedBefore(company, beforeDate);
		LOGGER.info("Cleaning up {} old queued emails for company {}", oldEmails.size(), company.getName());
		oldEmails.forEach(this::delete);
		return oldEmails.size();
	}

	/** Count failed emails.
	 * @param company the company
	 * @return count */
	@Transactional (readOnly = true)
	public long countFailed(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return ((IEmailQueuedRepository) repository).countFailedEmails(company);
	}

	/** Count pending emails.
	 * @param company the company
	 * @return count */
	@Transactional (readOnly = true)
	public long countPending(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return ((IEmailQueuedRepository) repository).countPendingEmails(company);
	}

	/** Find emails by type.
	 * @param company   the company
	 * @param emailType the email type
	 * @return list of emails */
	@Transactional (readOnly = true)
	public List<CEmailQueued> findByEmailType(final CCompany company, final String emailType) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(emailType, "Email type cannot be blank");
		return ((IEmailQueuedRepository) repository).findByEmailType(company, emailType);
	}

	/** Find failed emails that have reached max retries.
	 * @param company the company
	 * @return list of failed emails */
	@Transactional (readOnly = true)
	public List<CEmailQueued> findFailedEmails(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		return ((IEmailQueuedRepository) repository).findFailedEmails(company);
	}

	/** Find pending emails ready to be sent.
	 * @return list of sendable emails */
	@Transactional (readOnly = true)
	public List<CEmailQueued> findPendingEmails() {
		return ((IEmailQueuedRepository) repository).findPendingEmails();
	}

	@Override
	public Class<CEmailQueued> getEntityClass() { return CEmailQueued.class; }

	public Class<?> getInitializerServiceClass() { return CEmailQueuedInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceEmailQueued.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Move queued email to sent archive. Called after successful email send.
	 * @param queuedEmail the queued email
	 * @return the sent email (archive record) */
	@Transactional
	public CEmailSent moveToSent(final CEmailQueued queuedEmail) {
		Check.notNull(queuedEmail, "Queued email cannot be null");
		Check.notNull(queuedEmail.getId(), "Queued email must be persisted");
		LOGGER.info("Moving email {} to sent archive: {}", queuedEmail.getId(), queuedEmail.getSubject());
		// Create sent email (archive)
		final CEmailSent sentEmail = new CEmailSent(queuedEmail.getSubject(), queuedEmail.getToEmail(), queuedEmail.getCompany());
		// Copy all fields
		sentEmail.setFromEmail(queuedEmail.getFromEmail());
		sentEmail.setFromName(queuedEmail.getFromName());
		sentEmail.setToName(queuedEmail.getToName());
		sentEmail.setReplyToEmail(queuedEmail.getReplyToEmail());
		sentEmail.setReplyToName(queuedEmail.getReplyToName());
		sentEmail.setBodyText(queuedEmail.getBodyText());
		sentEmail.setBodyHtml(queuedEmail.getBodyHtml());
		sentEmail.setPriority(queuedEmail.getPriority());
		sentEmail.setQueuedAt(queuedEmail.getQueuedAt());
		sentEmail.setSentAt(LocalDateTime.now());
		sentEmail.setRetryCount(queuedEmail.getRetryCount());
		sentEmail.setMaxRetries(queuedEmail.getMaxRetries());
		sentEmail.setLastError(queuedEmail.getLastError());
		sentEmail.setEmailType(queuedEmail.getEmailType());
		sentEmail.setReferenceEntityType(queuedEmail.getReferenceEntityType());
		sentEmail.setReferenceEntityId(queuedEmail.getReferenceEntityId());
		// Save to archive
		final CEmailSent saved = emailSentService.save(sentEmail);
		// Delete from queue
		delete(queuedEmail);
		LOGGER.info("Email {} archived successfully with ID {}", queuedEmail.getId(), saved.getId());
		return saved;
	}

	/** Process all pending emails across all companies. Called by scheduler. */
	@Transactional
	public void processQueue() {
		LOGGER.info("Processing email queue");
		// Delegate to processor service in actual implementation
		// This is just a stub for scheduler integration
	}

	/** Increment retry count for failed send attempt.
	 * @param email        the email
	 * @param errorMessage the error message */
	@Transactional
	public void recordFailedAttempt(final CEmailQueued email, final String errorMessage) {
		Check.notNull(email, "Email cannot be null");
		email.incrementRetryCount();
		email.setLastError(errorMessage);
		save(email);
		if (email.hasReachedMaxRetries()) {
			LOGGER.error("Email {} failed permanently after {} retries: {}", email.getId(), email.getRetryCount(), errorMessage);
		} else {
			LOGGER.warn("Email {} failed (attempt {}/{}): {}", email.getId(), email.getRetryCount(), email.getMaxRetries(), errorMessage);
		}
	}

	@Override
	protected void validateEntity(final CEmailQueued email) {
		super.validateEntity(email);
		// Required fields
		Check.notBlank(email.getSubject(), "Email subject is required");
		Check.notBlank(email.getToEmail(), "Recipient email is required");
		// At least one body format required
		if ((email.getBodyText() == null || email.getBodyText().isBlank()) && (email.getBodyHtml() == null || email.getBodyHtml().isBlank())) {
			throw new CValidationException("Email must have either text or HTML body");
		}
		// Valid priority
		if (email.getPriority() != null) {
			final String priority = email.getPriority().toUpperCase();
			if (!"LOW".equals(priority) && !"NORMAL".equals(priority) && !"HIGH".equals(priority)) {
				throw new CValidationException("Invalid priority. Must be LOW, NORMAL, or HIGH");
			}
		}
	}
}
