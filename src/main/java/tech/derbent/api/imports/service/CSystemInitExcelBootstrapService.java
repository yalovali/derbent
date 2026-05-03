package tech.derbent.api.imports.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.config.CScreensInitializerService;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.domain.CProjectType;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.projects.service.CProjectTypeService;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.CUserProjectSettingsService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;

/** Runs the committed "system init" workbooks after DB reset. WHY: DB reset must be reproducible and driven by committed Excel templates;
 * initializer-service sample creators are no longer used. We still seed the minimal "core" objects required to have a company/user/project context,
 * then Excel populates everything else. */
@Service
public class CSystemInitExcelBootstrapService {

	public record CBootstrapSummary(int companies, int projects, int totalSuccess, int totalErrors) {

		public String toUiSummary() {
			return "Excel init: companies=" + companies + ", projects=" + projects + ", ok=" + totalSuccess
					+ ", errors=" + totalErrors;
		}
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemInitExcelBootstrapService.class);
	private static final String SEED_ADMIN_LOGIN = "admin";
	private static final String SEED_ADMIN_PASSWORD = "test123";
	private static final String SEED_COMPANY_NAME = "Of Teknoloji Çözümleri";
	// Keep these aligned with src/main/resources/excel/system_init.xlsx company/project tokens.
	private static final List<String> SEED_PROJECT_NAMES = List.of("Derbent PM Demo", "Derbent API Platform",
			"BAB Integration Program", "Mobile App Delivery", "Data & Analytics Platform", "Customer Portal Revamp");
	private static final String SEED_PROJECT_TYPE_NAME = "Default Project Type";
	private static final String SEED_STATUS_FROM_NAME = "Seed Start";
	private static final String SEED_STATUS_TO_NAME = "Open";
	private static final String SEED_WORKFLOW_NAME = "Default Workflow";
	private final CCompanyService companyService;
	private final CExcelImportService excelImportService;
	private final CExcelTemplateService excelTemplateService;
	private final CProjectService<?> projectService;
	private final CProjectTypeService projectTypeService;
	private final CScreensInitializerService screensInitializerService;
	private final ISessionService sessionService;
	private final CProjectItemStatusService statusService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserProjectSettingsService userProjectSettingsService;
	private final CUserService userService;
	private final CWorkflowEntityService workflowEntityService;
	private final CWorkflowStatusRelationService workflowStatusRelationService;

	public CSystemInitExcelBootstrapService(final CCompanyService companyService,
			final CProjectService<?> projectService, final CProjectTypeService projectTypeService,
			final CUserService userService, final CUserCompanyRoleService userCompanyRoleService,
			final CUserProjectRoleService userProjectRoleService,
			final CUserProjectSettingsService userProjectSettingsService,
			final CWorkflowEntityService workflowEntityService,
			final CWorkflowStatusRelationService workflowStatusRelationService,
			final CProjectItemStatusService statusService, final ISessionService sessionService,
			final CExcelImportService excelImportService, final CExcelTemplateService excelTemplateService,
			final CScreensInitializerService screensInitializerService) {
		this.companyService = companyService;
		this.projectService = projectService;
		this.projectTypeService = projectTypeService;
		this.userService = userService;
		this.userCompanyRoleService = userCompanyRoleService;
		this.userProjectRoleService = userProjectRoleService;
		this.userProjectSettingsService = userProjectSettingsService;
		this.workflowEntityService = workflowEntityService;
		this.workflowStatusRelationService = workflowStatusRelationService;
		this.statusService = statusService;
		this.sessionService = sessionService;
		this.excelImportService = excelImportService;
		this.excelTemplateService = excelTemplateService;
		this.screensInitializerService = screensInitializerService;
	}

	/** Excel-first DB reset entrypoint. This method is designed to be called right after the DB has been cleared. */
	public CBootstrapSummary bootstrapAfterReset(final boolean minimal) throws Exception {
		ensureSeedData(minimal);
		final CBootstrapSummary summary = bootstrapAllProjects(minimal);
		// WHY: screens_init.xlsx defines grid/detail/master/page layout overrides; run it after entity data is present
		// but before the code-based fallback so Excel wins where both define the same screen.
		bootstrapScreensFromExcel();
		// screensInitializerService.initializeScreensForAllProjects(minimal);
		return summary;
	}

	public CBootstrapSummary bootstrapAllProjects(final boolean minimal) {
		final byte[] workbookBytes = loadTemplateBytes(minimal);
		final CImportOptions options = CImportOptions.defaults();
		options.setDryRun(false);
		options.setRollbackOnError(true);
		options.setSkipUnknownSheets(true);
		int companiesProcessed = 0;
		int projectsProcessed = 0;
		int totalSuccess = 0;
		int totalErrors = 0;
		final List<CCompany> companies = companyService.findActiveCompanies();
		for (final CCompany company : companies) {
			companiesProcessed++;
			// WHY: many services validate against session-scoped company/user; set stable context before importing.
			sessionService.setActiveCompany(company);
			final CUser user = userService.getRandomByCompany(company);
			Check.notNull(user, "No user found for company: " + company.getName());
			sessionService.setActiveUser(user);
			final List<? extends CProject<?>> projects = projectService.listByCompany(company);
			for (final CProject<?> project : projects) {
				projectsProcessed++;
				sessionService.setActiveProject(project);
				final CImportResult result =
						excelImportService.importExcel(new ByteArrayInputStream(workbookBytes), options, project);
				totalSuccess += result.getTotalSuccess();
				totalErrors += result.getTotalErrors();
				LOGGER.info("Excel init imported into project {}:{} (ok={}, errors={})", project.getId(),
						project.getName(), result.getTotalSuccess(), result.getTotalErrors());
			}
		}
		return new CBootstrapSummary(companiesProcessed, projectsProcessed, totalSuccess, totalErrors);
	}

	/** Loads screens_init.xlsx and imports grid/detail/master/page configuration per project. No-op if the file does not exist — code-based init remains
	 * the fallback. */
	public void bootstrapScreensFromExcel() {
		final byte[] workbookBytes;
		try (final InputStream in = excelTemplateService.openScreensInitTemplate()) {
			if (in == null) {
				LOGGER.info("screens_init.xlsx not found; skipping Excel-based screen init");
				return;
			}
			workbookBytes = in.readAllBytes();
		} catch (final Exception e) {
			LOGGER.warn("Could not read screens_init.xlsx: {}; skipping", e.getMessage());
			return;
		}
		if (workbookBytes.length == 0) {
			LOGGER.info("screens_init.xlsx is empty; skipping Excel-based screen init");
			return;
		}
		final CImportOptions options = CImportOptions.defaults();
		options.setDryRun(false);
		options.setRollbackOnError(false);
		options.setSkipUnknownSheets(true);
		// WHY: screens_init.xlsx rows carry an explicit "project" column so the workbook is imported once per
		// company; the handlers resolve the target project from each row. Importing per-project (as before)
		// would create duplicate entities because every row would be processed N times.
		for (final CCompany company : companyService.findActiveCompanies()) {
			final List<? extends CProject<?>> projects = projectService.listByCompany(company);
			if (projects.isEmpty()) {
				LOGGER.warn("No projects for company {}; skipping screens Excel init", company.getName());
				continue;
			}
			sessionService.setActiveCompany(company);
			final CUser user = userService.getRandomByCompany(company);
			if (user == null) {
				LOGGER.warn("No user for company {}; skipping screens Excel init", company.getName());
				continue;
			}
			sessionService.setActiveUser(user);
			// Use first project as fallback context; individual rows override via the "project" column.
			final CProject<?> fallbackProject = projects.get(0);
			sessionService.setActiveProject(fallbackProject);
			final CImportResult result =
					excelImportService.importExcel(new ByteArrayInputStream(workbookBytes), options, fallbackProject);
			LOGGER.info("Screens Excel imported for company {} (ok={}, errors={})", company.getName(),
					result.getTotalSuccess(), result.getTotalErrors());
		}
	}

	private CProject<?> createProject(final CCompany company, final String name) {
		sessionService.setActiveCompany(company);
		try {
			final CProject<?> project = projectService.newEntity();
			project.setName(name);
			return saveProject(project);
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to create seed project '" + name + "'", e);
		}
	}

	private void ensureSeedData(final boolean minimal) {
		final List<CCompany> active = companyService.findActiveCompanies();
		if (active.isEmpty()) {
			LOGGER.info("No companies found; creating minimal seed company/project/user for Excel bootstrap");
			final CCompany seed = new CCompany(SEED_COMPANY_NAME);
			companyService.save(seed);
		}
		for (final CCompany company : companyService.findActiveCompanies()) {
			ensureSeedDataForCompany(company, minimal);
		}
	}

	private void ensureSeedDataForCompany(final CCompany company, final boolean minimal) {
		Check.notNull(company, "Company cannot be null");
		sessionService.setActiveCompany(company);
		userCompanyRoleService.initializeDefaultRoles(company);
		userProjectRoleService.initializeDefaultRoles(company);
		final CWorkflowEntity workflow = getOrCreateWorkflow(company);
		final CProjectItemStatus fromStatus = getOrCreateStatus(company, SEED_STATUS_FROM_NAME);
		final CProjectItemStatus toStatus = getOrCreateStatus(company, SEED_STATUS_TO_NAME);
		ensureSeedWorkflowInitialRelation(workflow, fromStatus, toStatus);
		getOrCreateProjectType(company, workflow);
		final List<CProject<?>> projects = getOrCreateProjects(company, minimal);
		Check.notEmpty(projects, "Seed projects must not be empty");
		final CUser user = getOrCreateAdminUser(company);
		for (final CProject<?> project : projects) {
			ensureUserInProject(user, project);
		}
		sessionService.setActiveUser(user);
		ensureSystemAuthentication(user);
		sessionService.setActiveProject(projects.get(0));
	}

	private void ensureSeedWorkflowInitialRelation(final CWorkflowEntity workflow, final CProjectItemStatus fromStatus,
			final CProjectItemStatus toStatus) {
		final List<CWorkflowStatusRelation> relations = workflowStatusRelationService.findByWorkflow(workflow);
		if (!relations.isEmpty()) {
			return;
		}
		final CWorkflowStatusRelation rel = new CWorkflowStatusRelation(true);
		rel.setWorkflowEntity(workflow);
		rel.setFromStatus(fromStatus);
		rel.setToStatus(toStatus);
		rel.setInitialStatus(Boolean.TRUE);
		rel.setRoles(List.of());
		workflowStatusRelationService.save(rel);
	}

	private void ensureSystemAuthentication(final CUser user) {
		if (user == null) {
			return;
		}
		// WHY: The Excel bootstrap/import pipeline runs before any interactive login session exists.
		// Many service beans are protected with @PreAuthorize("isAuthenticated()") even for internal initialization hooks.
		// Setting a temporary authenticated principal avoids bootstrap failures while keeping UI/API security intact.
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getUsername(),
				"bootstrap", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
	}

	private void ensureUserInProject(final CUser user, final CProject<?> project) {
		if (user == null || project == null) {
			return;
		}
		if (user.getId() == null || project.getId() == null) {
			return;
		}
		if (userProjectSettingsService.relationshipExists(user.getId(), project.getId())) {
			return;
		}
		userProjectSettingsService.addUserToProject(user, project, "write");
	}

	private CUser getOrCreateAdminUser(final CCompany company) {
		final CUser existing = userService.findByLogin(SEED_ADMIN_LOGIN, company.getId());
		if (existing != null) {
			return existing;
		}
		final List<CUserCompanyRole> roles = userCompanyRoleService.listByCompany(company);
		final CUserCompanyRole role =
				roles.stream().filter(CUserCompanyRole::isAdmin).findFirst().orElseGet(() -> roles.get(0));
		return userService.createLoginUser(SEED_ADMIN_LOGIN, SEED_ADMIN_PASSWORD, "Admin", "admin@local", company,
				role);
	}

	private List<CProject<?>> getOrCreateProjects(final CCompany company, final boolean minimal) {
		final List<? extends CProject<?>> existing = projectService.listByCompany(company);
		final List<CProject<?>> result = new java.util.ArrayList<>();
		final List<String> targetNames = minimal ? List.of(SEED_PROJECT_NAMES.get(0)) : SEED_PROJECT_NAMES;
		for (final String name : targetNames) {
			CProject<?> match = null;
			for (final CProject<?> p : existing) {
				if (p.getName() != null && p.getName().equalsIgnoreCase(name)) {
					match = p;
					break;
				}
			}
			if (match == null) {
				match = createProject(company, name);
			}
			result.add(match);
		}
		return result;
	}

	private CProjectType getOrCreateProjectType(final CCompany company, final CWorkflowEntity workflow) {
		final List<CProjectType> types = projectTypeService.listByCompany(company);
		if (!types.isEmpty()) {
			return types.get(0);
		}
		final CProjectType type = new CProjectType(SEED_PROJECT_TYPE_NAME, company);
		type.setWorkflow(workflow);
		return projectTypeService.save(type);
	}

	private CProjectItemStatus getOrCreateStatus(final CCompany company, final String name) {
		final List<CProjectItemStatus> statuses = statusService.listByCompany(company);
		for (final CProjectItemStatus status : statuses) {
			if (status.getName() != null && status.getName().equalsIgnoreCase(name)) {
				return status;
			}
		}
		return statusService.save(new CProjectItemStatus(name, company));
	}

	private CWorkflowEntity getOrCreateWorkflow(final CCompany company) {
		final List<CWorkflowEntity> workflows = workflowEntityService.listByCompany(company);
		if (!workflows.isEmpty()) {
			return workflows.get(0);
		}
		final CWorkflowEntity wf = new CWorkflowEntity(SEED_WORKFLOW_NAME, company);
		return workflowEntityService.save(wf);
	}

	private byte[] loadTemplateBytes(final boolean minimal) {
		try (final InputStream in = excelTemplateService.openSystemInitTemplate(minimal)) {
			final byte[] bytes = in.readAllBytes();
			Check.isTrue(bytes.length > 0, "Empty system init workbook");
			return bytes;
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to read system init workbook bytes", e);
		}
	}

	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	private CProject<?> saveProject(final CProject<?> project) {
		// WHY: Spring injects a single concrete CProjectService implementation per profile; we bridge wildcard generics here.
		return ((CProjectService) projectService).save(project);
	}
}
