package tech.derbent.base.users.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.base.users.domain.CUser;

public class CUserInitializerService extends CInitializerServiceBase {

	private record UserSeed(String username, String firstName, String lastName, String phone) {}

	public static final String BASE_PANEL_NAME = "User Information";
	static final Class<?> clazz = CUser.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CUserInitializerService.class);
	private static final String STANDARD_PASSWORD = "test123";
	private static final String BAB_ADMIN_LOGIN = "admin";
	private static final String BAB_ADMIN_NAME = "Admin";
	private static final String BAB_ADMIN_LASTNAME = "Gateway";
	private static final String BAB_ADMIN_EMAIL = "admin@babgateway.local";
	private static final String BAB_ADMIN_PASSWORD = "test123";
	private static final String menuOrder = Menu_Order_SYSTEM + ".10";
	private static final String menuTitle = MenuTitle_SYSTEM + ".Users";
	private static final String pageDescription = "User management for system access and permissions";
	private static final String pageTitle = "User Management";
	private static final boolean showInQuickToolbar = true;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// create screen lines
			detailSection.addScreenLine(CDetailLinesService.createSection(CUserInitializerService.BASE_PANEL_NAME));
			// for test purposes only
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "name"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastname"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "login"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "email"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "phone"));
			detailSection.addScreenLine(CDetailLinesService.createSection("System Access"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "password"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Project & Company Relations"));
			final CDetailLines line = CDetailLinesService.createLineFromDefaults(clazz, "projectSettings");
			line.setRelationFieldName("projectSettings");
			line.setFieldCaption("projectSettings");
			line.setEntityProperty("Component:createUserProjectSettingsComponent");
			line.setDataProviderBean("CUserService");
			detailSection.addScreenLine(line);
			detailSection.addScreenLine(CDetailLinesService.createSection("Organization"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyRole"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Profile"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "profilePictureData"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "description"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "id"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "activities"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Settings"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "attributeDisplaySectionsAsTabs"));
			
			// Attachments section - standard section for ALL entities
			tech.derbent.app.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);
			
			// Comments section - standard section for discussion entities
			tech.derbent.app.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);
			
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating user view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(
				List.of("id", "name", "lastname", "login", "email", "phone", "projectSettings", "active", "createdDate", "lastModifiedDate"));
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
		final CUserService userService = CSpringContext.getBean(CUserService.class);
		final CUserCompanyRoleService roleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		final String companyShortName = company.getName().toLowerCase().replaceAll("[^a-z0-9]", "");
		final List<UserSeed> seeds = List.of(new UserSeed("admin", "Admin", company.getName() + " Yöneticisi", "+90-462-751-1001"),
				new UserSeed("yasin", "yasin", company.getName() + " Yöneticisi", "+90-462-751-1002"));
		int created = 0;
		for (final UserSeed seed : seeds) {
			final String email = seed.username() + "@" + companyShortName + ".com.tr";
			final CUser user =
					userService.createLoginUser(seed.username(), STANDARD_PASSWORD, seed.firstName(), email, company, roleService.getRandom(company));
			user.setLastname(seed.lastName());
			user.setPhone(seed.phone());
			user.setAttributeDisplaySectionsAsTabs(true);
			userService.save(user);
			created++;
			if (minimal && created >= 1) {
				break;
			}
		}
	}

	public static CUser initializeSampleBab(final CCompany company, final CUserCompanyRole adminRole, final boolean minimal) throws Exception {
		final CUserService userService = CSpringContext.getBean(CUserService.class);
		final CUser user = userService.createLoginUser(BAB_ADMIN_LOGIN, BAB_ADMIN_PASSWORD, BAB_ADMIN_NAME, BAB_ADMIN_EMAIL, company, adminRole);
		user.setLastname(BAB_ADMIN_LASTNAME);
		user.setActive(true);
		userService.save(user);
		return user;
	}
}
