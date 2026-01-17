package tech.derbent.app.teams.team.service;
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
import tech.derbent.api.utils.Check;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.teams.team.domain.CTeam;
import tech.derbent.app.attachments.service.CAttachmentInitializerService;
import tech.derbent.app.comments.service.CCommentInitializerService;

public class CTeamInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CTeam.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTeamInitializerService.class);
	private static final String menuOrder = Menu_Order_SETUP + ".15";
	private static final String menuTitle = MenuTitle_SETUP + ".Teams";
	private static final String pageDescription = "Manage teams and team members";
	private static final String pageTitle = "Team Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		Check.notNull(project, "project cannot be null");
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			
			detailSection.addScreenLine(CDetailLinesService.createSection("Team Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "teamManager"));
			
			detailSection.addScreenLine(CDetailLinesService.createSection("Team Members"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "members"));

			detailSection.addScreenLine(CDetailLinesService.createSection("System Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			
			CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			
			// Comments section - standard section for discussion entities
			CCommentInitializerService.addCommentsSection(detailSection, clazz);
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating team view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "company", "teamManager", "description", "active", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
			{ "Development Team", "Software development and engineering" },
			{ "QA Team", "Quality assurance and testing" },
			{ "DevOps Team", "Infrastructure and deployment" },
			{ "Design Team", "UI/UX design and graphics" },
			{ "Management Team", "Project management and leadership" }
		};
		initializeCompanyEntity(nameAndDescriptions,
			(CEntityOfCompanyService<?>) CSpringContext.getBean(
				CEntityRegistry.getServiceClassForEntity(clazz)),
			company, minimal, null);
	}
}
