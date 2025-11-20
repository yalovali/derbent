package tech.derbent.app.orders.approval.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;

public class CApprovalStatusInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CApprovalStatus.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CApprovalStatusInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".1";
	private static final String menuTitle = MenuTitle_TYPES + ".Approval Statuses";
	private static final String pageDescription = "Manage approval status definitions for projects";
	private static final String pageTitle = "Approval Status Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating approval status view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany project, final boolean minimal) throws Exception {
		// Approval status data: [name, description, sortOrder]
		final String[][] statusData = {
				{
						"Draft", "Approval is in draft state", "1"
				}, {
						"Submitted", "Approval has been submitted", "2"
				}, {
						"Approved", "Approval has been approved", "3"
				}, {
						"Rejected", "Approval has been rejected", "4"
				}
		};
		initializeCompanyEntity(statusData, (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				project, minimal, (item, index) -> {
					final CApprovalStatus status = (CApprovalStatus) item;
					final String[] data = statusData[index];
					status.setColor(CColorUtils.getRandomColor(true));
					status.setSortOrder(Integer.parseInt(data[2]));
				});
	}
}
