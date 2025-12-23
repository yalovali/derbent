package tech.derbent.app.orders.approval.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;
import tech.derbent.app.orders.approval.domain.COrderApproval;
import tech.derbent.app.page.service.CPageEntityService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;

/** Initializes UI metadata for {@link COrderApproval} entities. */
public final class COrderApprovalInitializerService extends CInitializerServiceBase {

	public static final String BASE_PANEL_NAME = "Approval Details";
	private static final Logger LOGGER = LoggerFactory.getLogger(COrderApprovalInitializerService.class);
	private static final Class<?> ENTITY_CLASS = COrderApproval.class;
	private static final String menuTitle = "Orders.Approvals";
	private static final String pageTitle = "Order Approval Management";
	private static final String pageDescription = "Track approvals and decision workflow for orders";
	private static final String menuOrder = "1.1";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, ENTITY_CLASS);
			detailSection.addScreenLine(CDetailLinesService.createSection(BASE_PANEL_NAME));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "order"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "approvalStatus"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "approvalLevel"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "approver"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Decision"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "approvalDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "comments"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "active"));
   
			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(ENTITY_CLASS, "id"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating order approval view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, ENTITY_CLASS);
		grid.setColumnFields(List.of("id", "name", "order", "approvalStatus", "approvalLevel", "approver", "approvalDate", "active"));
		return grid;
	}

        public static void initialize(final CProject project, final CGridEntityService gridEntityService,
                        final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
                        throws Exception {
                Check.notNull(project, "project cannot be null");
                final CDetailSection detailSection = createBasicView(project);
                final CGridEntity grid = createGridEntity(project);
                initBase(ENTITY_CLASS, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
                                pageDescription, showInQuickToolbar, menuOrder);
        }

	private COrderApprovalInitializerService() { /*****/ }
}
