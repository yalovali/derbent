package tech.derbent.api.roles.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.roles.domain.CUserCompanyRole;

public class CUserCompanyRoleInitializerService extends CInitializerServiceBase {

	static final Class<?> clazz = CUserCompanyRole.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserCompanyRoleInitializerService.class);
	private static final String menuOrder = Menu_Order_ROLES + ".1";
	private static final String menuTitle = MenuTitle_ROLES + ".User Company Roles";
	private static final String pageDescription = "Company Roles management";
	private static final String pageTitle = "User Company Roles Management";
	private static final boolean showInQuickToolbar = false;
	private static final String BAB_ADMIN_ROLE_NAME = "Company Admin";
	private static final String BAB_ADMIN_ROLE_DESCRIPTION = "Administrative role for BAB Gateway";

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Basic Company Role Information
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "color"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "sortOrder"));
			// Role Type Attributes
			detailSection.addScreenLine(CDetailLinesService.createSection("Role Type"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isAdmin"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isUser"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isGuest"));
   
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating company role view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "isAdmin", "isUser", "isGuest", "color", "sortOrder"));
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
                final CUserCompanyRoleService service = CSpringContext.getBean(CUserCompanyRoleService.class);
                final String[][] nameAndDescription = {
                                {
                                                "Company Admin", "Administrative role with full company access"
                                }, {
                                                "Company User", "Standard user role with regular access"
                                }, {
                                                "Company Guest", "Guest role with limited access"
                                }
                };
                int index = 0;
                for (final String[] seed : nameAndDescription) {
                        final CUserCompanyRole role = service.newEntity(seed[0]);
                        role.setDescription(seed[1]);
                        role.setCompany(company);
                        role.setColor(CColorUtils.getRandomColor(true));
                        role.setSortOrder(index + 1);
                        role.setIsAdmin(index == 0);
                        role.setIsUser(index == 1);
                        role.setIsGuest(index == 2);
                        service.save(role);
                        index++;
                        if (minimal) {
                                return;
                        }
                }
        }

        public static CUserCompanyRole initializeSampleBab(final CCompany company, final boolean minimal) throws Exception {
                final CUserCompanyRoleService service = CSpringContext.getBean(CUserCompanyRoleService.class);
                final String[][] seeds = {
                                {
                                                BAB_ADMIN_ROLE_NAME, BAB_ADMIN_ROLE_DESCRIPTION
                                }
                };
                initializeCompanyEntity(seeds, service, company, minimal, (item, index) -> {
                        final CUserCompanyRole role = (CUserCompanyRole) item;
                        role.setColor(CColorUtils.getRandomColor(true));
                        role.setSortOrder(1);
                        role.setIsAdmin(true);
                        role.setIsUser(true);
                        role.setIsGuest(false);
                });
                return service.listByCompany(company).stream()
                                .filter(role -> BAB_ADMIN_ROLE_NAME.equals(role.getName()))
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException("BAB admin role not found after initialization"));
        }
}
