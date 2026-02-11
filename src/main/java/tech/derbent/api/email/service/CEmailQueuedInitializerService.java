package tech.derbent.api.email.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.email.domain.CEmailQueued;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;

/**
 * CEmailQueuedInitializerService - Initializer for email queue management.
 * 
 * Layer: Service (MVC)
 * Profile: derbent (PLM framework)
 * 
 * Provides initialization for email queue interface with:
 * - Complete email composition fields (sender, recipient, reply-to)
 * - Priority and type management
 * - Retry tracking and error monitoring
 * - Entity reference linking
 * - Queue status visualization
 * 
 * @see CEmailQueued
 * @see CEmailQueuedService
 */
public final class CEmailQueuedInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CEmailQueued.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CEmailQueuedInitializerService.class);

	/**
	 * Create comprehensive detail section for email queue entity.
	 * Following Derbent standard entity initializer pattern.
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
		
		// Queue Management section
		scr.addScreenLine(CDetailLinesService.createSection("Queue Status"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "queuedAt"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "retryCount"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "maxRetries"));
		scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastError"));
		
		return scr;
	}

	/**
	 * Create grid entity for email queue display.
	 * Shows key fields for queue monitoring and management.
	 * 
	 * @param project the project context
	 * @return configured grid entity
	 * @throws Exception if creation fails
	 */
	private static CGridEntity createGridEntity(final CProject<?> project) throws Exception {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		
		// Grid columns: priority, recipient, subject, type, queued time, retry status
		grid.setColumnFields(List.of(
				"priority", 
				"toEmail", 
				"subject", 
				"emailType", 
				"queuedAt",
				"retryCount",
				"lastError"
		));
		
		return grid;
	}

	/**
	 * Initialize email queue entity configuration.
	 * Creates page, grid, and navigation entries for email queue management.
	 * 
	 * @param project the project context
	 * @throws Exception if initialization fails
	 */
	public static void initialize(final CProject<?> project) throws Exception {
		LOGGER.info("Initializing CEmailQueued entity configuration for project: {}", project.getName());
		
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		
		// Entity configuration following Derbent standards
		final String menuTitle = "Email Queue";
		final String pageTitle = "Email Queue Management";
		final String description = "Queue of emails pending delivery with retry management";
		final boolean toolbar = true;
		final String menuOrder = "9.10"; // System section, email subsection
		
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
		
		LOGGER.info("CEmailQueued entity configuration initialized successfully");
	}

	/**
	 * Initialize sample email queue data for testing.
	 * Creates test emails with different priorities and states.
	 * 
	 * @param project the project context
	 * @param minimal if true, create minimal test data
	 * @throws Exception if sample creation fails
	 */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CEmailQueuedService service = CSpringContext.getBean(CEmailQueuedService.class);
		
		// Check if samples already exist
		if (!service.listByCompany(project.getCompany()).isEmpty()) {
			LOGGER.info("Email queue samples already exist for company: {}", project.getCompany().getName());
			return;
		}
		
		LOGGER.info("Creating sample email queue entries for project: {}", project.getName());
		
		// Sample 1: High priority welcome email
		CEmailQueued email1 = new CEmailQueued("Welcome to Derbent PLM", "user@example.com", project.getCompany());
		email1.setFromEmail("info@derbent.tech");
		email1.setFromName("Derbent PLM System");
		email1.setToName("New User");
		email1.setBodyText("Welcome! Your account has been created successfully.");
		email1.setBodyHtml("<p>Welcome! Your account has been created successfully.</p>");
		email1.setPriority("HIGH");
		email1.setEmailType("WELCOME");
		email1 = service.save(email1);
		LOGGER.debug("Created sample email: {}", email1.getSubject());
		
		if (!minimal) {
			// Sample 2: Normal priority notification
			CEmailQueued email2 = new CEmailQueued("Activity Assigned", "developer@example.com", project.getCompany());
			email2.setFromEmail("notifications@derbent.tech");
			email2.setFromName("Derbent Notifications");
			email2.setToName("Developer User");
			email2.setBodyText("A new activity has been assigned to you. Please review.");
			email2.setBodyHtml("<p>A new activity has been assigned to you. <strong>Please review.</strong></p>");
			email2.setPriority("NORMAL");
			email2.setEmailType("NOTIFICATION");
			email2 = service.save(email2);
			LOGGER.debug("Created sample email: {}", email2.getSubject());
			
			// Sample 3: Low priority system update
			CEmailQueued email3 = new CEmailQueued("System Update", "admin@example.com", project.getCompany());
			email3.setFromEmail("system@derbent.tech");
			email3.setFromName("Derbent System");
			email3.setToName("Administrator");
			email3.setBodyText("System maintenance will be performed this weekend.");
			email3.setBodyHtml("<p>System maintenance will be performed this weekend.</p>");
			email3.setPriority("LOW");
			email3.setEmailType("SYSTEM_UPDATE");
			email3 = service.save(email3);
			LOGGER.debug("Created sample email: {}", email3.getSubject());
		}
		
		LOGGER.info("Sample email queue entries created successfully");
	}

	// Private constructor - utility class
	private CEmailQueuedInitializerService() {
		// No instantiation
	}
}
