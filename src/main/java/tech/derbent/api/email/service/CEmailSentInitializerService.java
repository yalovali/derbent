package tech.derbent.api.email.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.email.domain.CEmailSent;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;

/**
 * CEmailSentInitializerService - Initializer for sent email archive.
 * 
 * Layer: Service (MVC)
 * Profile: derbent (PLM framework)
 * 
 * Provides initialization for sent email archive interface with:
 * - Complete audit trail visualization
 * - Read-only archive views
 * - Date range and type filtering
 * - Compliance reporting support
 * 
 * @see CEmailSent
 * @see CEmailSentService
 */
public final class CEmailSentInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CEmailSent.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailSentInitializerService.class);

	/**
	 * Create comprehensive detail section for sent email archive.
	 * Following Derbent standard entity initializer pattern.
	 * All fields are read-only as this is an audit archive.
	 * 
	 * @param project the project context
	 * @return configured detail section
	 * @throws Exception if initialization fails
	 */
	private static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		final CDetailSection scr = createBaseScreenEntity(project, clazz);
		
		// Sender Information section
		scr.addScreenLine(CDetailLinesService.createSection("Sender Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fromEmail"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "fromName"));
		
		// Recipient Information section
		scr.addScreenLine(CDetailLinesService.createSection("Recipient Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "toEmail"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "toName"));
		
		// Reply-To Information section
		scr.addScreenLine(CDetailLinesService.createSection("Reply-To Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "replyToEmail"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "replyToName"));
		
		// Email Content section
		scr.addScreenLine(CDetailLinesService.createSection("Email Content"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "subject"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "bodyText"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "bodyHtml"));
		
		// Email Metadata section
		scr.addScreenLine(CDetailLinesService.createSection("Email Metadata"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "priority"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "emailType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "referenceEntityType"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "referenceEntityId"));
		
		// Delivery Status section
		scr.addScreenLine(CDetailLinesService.createSection("Delivery Information"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "queuedAt"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sentAt"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		
		return scr;
	}

	/**
	 * Create grid entity for sent email archive display.
	 * Shows key fields for audit and reporting.
	 * 
	 * @param project the project context
	 * @return configured grid entity
	 * @throws Exception if creation fails
	 */
	private static CGridEntity createGridEntity(final CProject<?> project) throws Exception {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		
		// Grid columns: sent time, recipient, subject, type, priority
		grid.setColumnFields(List.of(
				"sentAt",
				"toEmail", 
				"subject", 
				"emailType", 
				"priority",
				"retryCount"
		));
		
		return grid;
	}

	/**
	 * Initialize sent email archive entity configuration.
	 * Creates page, grid, and navigation entries for email archive.
	 * 
	 * @param project the project context
	 * @throws Exception if initialization fails
	 */
	public static void initialize(final CProject<?> project) throws Exception {
		LOGGER.info("Initializing CEmailSent entity configuration for project: {}", project.getName());
		
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		
		// Entity configuration following Derbent standards
		final String menuTitle = "Email Archive";
		final String pageTitle = "Sent Email Archive";
		final String description = "Archive of successfully sent emails for audit and compliance";
		final boolean toolbar = true;
		final String menuOrder = "9.11"; // System section, after email queue
		
		// Get required services
		final CGridEntityService gridEntityService = CSpringContext.getBean(CGridEntityService.class);
		final CDetailSectionService detailSectionService = CSpringContext.getBean(CDetailSectionService.class);
		final CPageEntityService pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		
		initBase(
				clazz,
				project,
				gridEntityService,
				detailSectionService,
				pageEntityService,
				detailSection,
				grid,
				menuTitle,
				pageTitle,
				description,
				toolbar,
				menuOrder
		);
		
		LOGGER.info("CEmailSent entity configuration initialized successfully");
	}

	/**
	 * Initialize sample sent email archive data.
	 * Creates historical email records for testing and demonstration.
	 * 
	 * @param project the project context
	 * @param minimal if true, create minimal test data
	 * @throws Exception if sample creation fails
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CEmailSentService service = CSpringContext.getBean(CEmailSentService.class);
		
		// Check if samples already exist
		if (!service.listByCompany(project.getCompany()).isEmpty()) {
			LOGGER.info("Email archive samples already exist for company: {}", project.getCompany().getName());
			return;
		}
		
		LOGGER.info("Creating sample email archive entries for project: {}", project.getName());
		
		// Sample 1: Successfully delivered welcome email
		CEmailSent email1 = new CEmailSent("Welcome to Derbent PLM", "user@example.com", project.getCompany());
		email1.setFromEmail("info@derbent.tech");
		email1.setFromName("Derbent PLM System");
		email1.setToName("New User");
		email1.setBodyText("Welcome! Your account is now active.");
		email1.setBodyHtml("<p>Welcome! Your account is now active.</p>");
		email1.setPriority("HIGH");
		email1.setEmailType("WELCOME");
		email1 = service.save(email1);
		LOGGER.debug("Created sample sent email: {}", email1.getSubject());
		
		if (!minimal) {
			// Sample 2: Password reset delivered
			CEmailSent email2 = new CEmailSent("Password Reset Request", "user@example.com", project.getCompany());
			email2.setFromEmail("security@derbent.tech");
			email2.setFromName("Derbent Security");
			email2.setToName("User Account");
			email2.setBodyText("Your password has been reset successfully.");
			email2.setBodyHtml("<p>Your password has been reset successfully.</p>");
			email2.setPriority("HIGH");
			email2.setEmailType("PASSWORD_RESET");
			email2 = service.save(email2);
			LOGGER.debug("Created sample sent email: {}", email2.getSubject());
			
			// Sample 3: Notification delivered after retry
			CEmailSent email3 = new CEmailSent("Activity Update", "developer@example.com", project.getCompany());
			email3.setFromEmail("notifications@derbent.tech");
			email3.setFromName("Derbent Notifications");
			email3.setToName("Developer");
			email3.setBodyText("Your assigned activity has been updated.");
			email3.setBodyHtml("<p>Your assigned activity has been <strong>updated</strong>.</p>");
			email3.setPriority("NORMAL");
			email3.setEmailType("NOTIFICATION");
			email3.setRetryCount(1); // Delivered on second attempt
			email3 = service.save(email3);
			LOGGER.debug("Created sample sent email: {}", email3.getSubject());
		}
		
		LOGGER.info("Sample email archive entries created successfully");
	}

	// Private constructor - utility class
	private CEmailSentInitializerService() {
		// No instantiation
	}
}
