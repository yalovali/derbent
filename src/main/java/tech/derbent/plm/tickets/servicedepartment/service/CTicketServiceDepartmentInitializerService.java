package tech.derbent.plm.tickets.servicedepartment.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.tickets.servicedepartment.domain.CTicketServiceDepartment;

public class CTicketServiceDepartmentInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTicketServiceDepartment.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketServiceDepartmentInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".16";
	private static final String menuTitle = MenuTitle_SETUP + ".Service Departments";
	private static final String pageDescription = "Manage ticket service departments and responsible users";
	private static final String pageTitle = "Service Department Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);

			detailSection.addScreenLine(CDetailLinesService.createSection("Department Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "departmentManager"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Responsible Users"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "responsibleUsers"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Settings"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isActive"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "emailNotificationEnabled"));

			detailSection.addScreenLine(CDetailLinesService.createSection("System Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));

			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			CCommentInitializerService.addCommentsSection(detailSection, clazz);

			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating service department view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "name", "company", "departmentManager", "isActive", "emailNotificationEnabled", "description", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{"IT Support", "General IT support and technical assistance"},
				{"Help Desk", "First-line customer and user support"},
				{"Maintenance", "System and infrastructure maintenance"},
				{"Development Support", "Application development support"},
				{"Network Operations", "Network and connectivity issues"}
		};
		initializeCompanyEntity(nameAndDescriptions,
				(CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), company, minimal, null);
	}
}
