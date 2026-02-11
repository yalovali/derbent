package tech.derbent.api.email.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;

/**
 * CEmailSentService - Service for managing sent email archive.
 * 
 * Layer: Service (MVC)
 * Profile: derbent (PLM framework)
 * 
 * This service manages the archive of successfully sent emails, providing:
 * - Complete audit trail of all sent emails
 * - Date range queries for reporting
 * - Email type and recipient filtering
 * - Compliance and archival support
 * - Statistics and analytics queries
 * 
 * Sent emails are moved from CEmailQueued to CEmailSent after successful delivery.
 * This archive provides complete email history for audit and compliance purposes.
 * 
 * @see CEmailSent
 * @see CEmailQueuedService
 * @see IEmailSentRepository
 */
@Service
@Profile("derbent")
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CEmailSentService extends CEntityOfCompanyService<CEmailSent> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailSentService.class);

	public CEmailSentService(
			final IEmailSentRepository repository,
			final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	public Class<CEmailSent> getEntityClass() {
		return CEmailSent.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CEmailSentInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceEmailSent.class;
	}
	
	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	/**
	 * Find sent emails within a date range for a company.
	 * Used for reporting and audit purposes.
	 * 
	 * @param company the company to query
	 * @param startDate start of date range (inclusive)
	 * @param endDate end of date range (inclusive)
	 * @return list of sent emails in date range
	 */
	@Transactional(readOnly = true)
	public List<CEmailSent> findByDateRange(final CCompany company, 
			final LocalDateTime startDate, final LocalDateTime endDate) {
		Check.notNull(company, "Company cannot be null");
		Check.notNull(startDate, "Start date cannot be null");
		Check.notNull(endDate, "End date cannot be null");
		
		LOGGER.debug("Finding sent emails for company {} between {} and {}", 
				company.getName(), startDate, endDate);
		
		return ((IEmailSentRepository) repository).findByDateRange(company, startDate, endDate);
	}

	/**
	 * Find sent emails by type for a company.
	 * Used for analyzing email distribution by category.
	 * 
	 * @param company the company to query
	 * @param emailType email type to filter (WELCOME, PASSWORD_RESET, etc.)
	 * @return list of sent emails of specified type
	 */
	@Transactional(readOnly = true)
	public List<CEmailSent> findByEmailType(final CCompany company, final String emailType) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(emailType, "Email type cannot be blank");
		
		LOGGER.debug("Finding sent emails of type {} for company {}", emailType, company.getName());
		
		return ((IEmailSentRepository) repository).findByEmailType(company, emailType);
	}

	/**
	 * Find sent emails by recipient for a company.
	 * Used for tracking communication with specific users.
	 * 
	 * @param company the company to query
	 * @param toEmail recipient email address
	 * @return list of sent emails to specified recipient
	 */
	@Transactional(readOnly = true)
	public List<CEmailSent> findByRecipient(final CCompany company, final String toEmail) {
		Check.notNull(company, "Company cannot be null");
		Check.notBlank(toEmail, "Recipient email cannot be blank");
		
		LOGGER.debug("Finding sent emails to {} for company {}", toEmail, company.getName());
		
		return ((IEmailSentRepository) repository).findByRecipient(company, toEmail);
	}

	/**
	 * Get total count of sent emails for a company.
	 * Used for statistics and dashboards.
	 * 
	 * @param company the company to query
	 * @return total count of sent emails
	 */
	@Transactional(readOnly = true)
	public Long getCountForCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null");
		
		LOGGER.debug("Getting sent email count for company {}", company.getName());
		
		return ((IEmailSentRepository) repository).countByCompany(company);
	}

	/**
	 * Get recent sent emails for a company.
	 * Used for dashboards and recent activity views.
	 * 
	 * @param company the company to query
	 * @param limit maximum number of emails to return
	 * @return list of recent sent emails (most recent first)
	 */
	@Transactional(readOnly = true)
	public List<CEmailSent> findRecentByCompany(final CCompany company, final int limit) {
		Check.notNull(company, "Company cannot be null");
		Check.isTrue(limit > 0, "Limit must be positive");
		
		LOGGER.debug("Finding {} most recent sent emails for company {}", limit, company.getName());
		
		final IEmailSentRepository sentRepo = (IEmailSentRepository) repository;
		return sentRepo.findRecentByCompany(company, limit);
	}

	@Override
	protected void validateEntity(final CEmailSent entity) {
		super.validateEntity(entity);
		
		// Email validation
		Check.notBlank(entity.getFromEmail(), "From email is required");
		Check.notBlank(entity.getToEmail(), "To email is required");
		Check.notBlank(entity.getSubject(), "Subject is required");
		
		// At least one body type required
		if (entity.getBodyText() == null && entity.getBodyHtml() == null) {
			throw new IllegalArgumentException("Email must have either text or HTML body");
		}
		
		// Priority validation
		if (entity.getPriority() == null) {
			throw new IllegalArgumentException("Priority is required");
		}
		
		// Sent timestamp required for archive
		if (entity.getSentAt() == null) {
			throw new IllegalArgumentException("Sent timestamp required for email archive");
		}
		
		LOGGER.debug("Validated sent email: {} to {}", entity.getSubject(), entity.getToEmail());
	}
	
	@Override
	public String checkDeleteAllowed(final CEmailSent entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		
		// Sent emails are archive records - normally should not be deleted
		// Consider retention policies instead
		LOGGER.warn("Attempting to delete sent email archive record: {}", entity.getId());
		return "Sent emails are archive records and should not be deleted. Consider retention policies instead.";
	}
}
