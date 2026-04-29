package tech.derbent.plm.tickets.tickettype.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CEntityNamedInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.services.CEntityTypeInitializerService;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;

public class CTicketTypeInitializerService extends CEntityTypeInitializerService {

	private static final Class<?> clazz = CTicketType.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicketTypeInitializerService.class);
	private static final String menuOrder = Menu_Order_TYPES + ".20";
	private static final String menuTitle = MenuTitle_TYPES + "." + MenuTitle_TYPES_PROJECT + ".Ticket Types";
	private static final String pageDescription = "Manage ticket type categories";
	private static final String pageTitle = "TicketType Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			return CEntityNamedInitializerService.createTypeEntityView(project, clazz, "Display Configuration", true,
					"level");
		} catch (final Exception e) {
			LOGGER.error("Error creating ticket type view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "color", "sortOrder", "active", "company"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService)
			throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid,
				menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder, null);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final String[][] ticketTypes = {
				{
						"Bug", "Bug reports and defects"
				}, {
						"Feature Request", "New feature requests and enhancements"
				}
		};
		final CTicketTypeService service = CSpringContext.getBean(CTicketTypeService.class);
		final CCompany company = project.getCompany();
		initializeCompanyEntity(ticketTypes, service, company, minimal, null);
	}
}
