package tech.derbent.bab.config;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.companies.service.CCompanyInitializerService;
import tech.derbent.api.companies.service.CCompanyService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusInitializerService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.page.service.CPageEntityInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.service.CProjectTypeInitializerService;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.roles.service.CUserProjectRoleInitializerService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CDashboardProject_BabInitializerService;
import tech.derbent.bab.device.service.CBabDeviceInitializerService;
import tech.derbent.bab.device.service.CBabDeviceService;
import tech.derbent.bab.project.domain.CProject_Bab;
import tech.derbent.bab.project.service.CProject_BabInitializerService;
import tech.derbent.bab.project.service.CProject_BabService;
import tech.derbent.bab.setup.service.CSystemSettings_BabInitializerService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserInitializerService;
import tech.derbent.base.users.service.CUserService;

@Component
@Profile ("bab")
public class CBabDataInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDataInitializer.class);

	private static boolean isPostgreSql(final DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		try (Connection connection = dataSource.getConnection()) {
			final String product = connection.getMetaData().getDatabaseProductName();
			return product != null && product.toLowerCase().contains("postgresql");
		} catch (final Exception e) {
			LOGGER.debug("Unable to detect database product for sample data cleanup: {}", e.getMessage());
			return false;
		}
	}

	private final CBabDeviceService babDeviceService;
	private final CDetailSectionService detailSectionService;
	@PersistenceContext
	private EntityManager entityManager;
	private final CGridEntityService gridEntityService;
	// Service dependencies - injected via constructor
	private final JdbcTemplate jdbcTemplate;
	private final CPageEntityService pageEntityService;
	private final CProject_BabService projectService;
	private final ISessionService sessionService;
	private final CUserService userService;
	// ========================================================================
	// UTILITY METHODS
	// ========================================================================

	public CBabDataInitializer(final JdbcTemplate jdbcTemplate, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService, final CBabDeviceService babDeviceService,
			final CProject_BabService projectService, final CUserService userService, final CCompanyService companyService,
			final ISessionService sessionService) {
		Check.notNull(jdbcTemplate, "JdbcTemplate cannot be null");
		Check.notNull(gridEntityService, "GridEntityService cannot be null");
		Check.notNull(detailSectionService, "DetailSectionService cannot be null");
		Check.notNull(pageEntityService, "PageEntityService cannot be null");
		Check.notNull(babDeviceService, "BabDeviceService cannot be null");
		Check.notNull(projectService, "ProjectService cannot be null");
		Check.notNull(userService, "UserService cannot be null");
		Check.notNull(companyService, "CompanyService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.gridEntityService = gridEntityService;
		this.detailSectionService = detailSectionService;
		this.pageEntityService = pageEntityService;
		this.babDeviceService = babDeviceService;
		this.projectService = projectService;
		this.userService = userService;
		this.sessionService = sessionService;
	}
	// ========================================================================
	// CLEAR DATA METHODS
	// ========================================================================

	@Transactional
	private void clearSampleData() {
		LOGGER.debug("Clearing BAB sample data from database (forced)");
		try {
			// PostgreSQL path: TRUNCATE with CASCADE
			if (isPostgreSql(jdbcTemplate.getDataSource())) {
				try {
					final List<String> tableNames = jdbcTemplate.queryForList("""
							SELECT tablename
							FROM pg_tables
							WHERE schemaname = 'public'
							  AND tablename NOT IN ('flyway_schema_history')
							""", String.class);
					if (!tableNames.isEmpty()) {
						final List<String> quoted = tableNames.stream().map(t -> "\"" + t + "\"").toList();
						final String joined = String.join(", ", quoted);
						final String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";
						jdbcTemplate.execute(sql);
						LOGGER.info("All public tables truncated (PostgreSQL).");
						return;
					}
					LOGGER.warn("No user tables found to truncate in public schema.");
				} catch (final Exception pgEx) {
					LOGGER.warn("PostgreSQL truncate path failed. Falling back to JPA deletes. Cause: {}", pgEx.getMessage());
				}
			}
			// Fallback: JPA batch delete (FK order matters)
			babDeviceService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			userService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during BAB sample data cleanup", e);
			throw e;
		}
	}
	// ========================================================================
	// INITIALIZATION METHODS
	// ========================================================================

	/** Initialize UI views for a project. Creates screens and grids for all BAB entities. */
	private void initializeStandardViews(final CProject_Bab project) throws Exception {
		try {
			LOGGER.debug("Initializing BAB standard views for project: {}", project.getName());
			// Core system views
			CSystemSettings_BabInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CCompanyInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CUserInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CUserCompanyRoleInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			// BAB-specific views
			CProject_BabInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CDashboardProject_BabInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CBabDeviceInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			// Administrative views
			CGridEntityInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			CPageEntityInitializerService.initialize(project, gridEntityService, detailSectionService, pageEntityService);
			LOGGER.debug("BAB standard views initialized successfully");
		} catch (final Exception e) {
			LOGGER.error("Error initializing BAB standard views", e);
			throw e;
		}
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("BAB User count = {}", cnt);
		return cnt == 0;
	}
	// ========================================================================
	// PUBLIC API METHODS
	// ========================================================================

	/** Load minimal data required for BAB profile. Following CDataInitializer pattern but with BAB-specific entities only. */
	private void loadMinimalData(final boolean minimal) throws Exception {
		try {
			LOGGER.info("Loading BAB minimal data (minimal={})", minimal);
			// ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
			// Create BAB company
			final CCompany company = CCompanyInitializerService.initializeSampleBab(minimal);
			// Create user roles and users
			final CUserCompanyRole adminRole = CUserCompanyRoleInitializerService.initializeSampleBab(company, minimal);
			CUserInitializerService.initializeSampleBab(company, adminRole, minimal);
			// Set session context for company-scoped initializations
			sessionService.setActiveCompany(company);
			final CUser user = userService.getRandomByCompany(company);
			Check.notNull(user, "No user found for BAB company");
			sessionService.setActiveUser(user);
			// ========== ESSENTIAL FOUNDATION ENTITIES ==========
			// These are REQUIRED for projects to work - must be in exact order
			// 1. Create project item statuses (REQUIRED for workflow relations)
			CProjectItemStatusInitializerService.initializeSample(company, minimal);
			// 2. Create user project roles (REQUIRED for workflow relations)
			CUserProjectRoleInitializerService.initializeSample(company, minimal);
			// 3. Create workflow entities WITH status relations (REQUIRED for projects)
			final CWorkflowEntityService workflowService = CSpringContext.getBean(CWorkflowEntityService.class);
			final CWorkflowStatusRelationService workflowRelationService = CSpringContext.getBean(CWorkflowStatusRelationService.class);
			final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
			final CUserProjectRoleService projectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
			// Create BAB workflow with proper status relations
			CWorkflowEntityInitializerService.initializeSampleWorkflowEntities(company, minimal, statusService, projectRoleService, workflowService,
					workflowRelationService);
			// 4. Create project types
			CProjectTypeInitializerService.initializeSampleBab(company, minimal);
			// ========== PROJECT-SPECIFIC INITIALIZATION PHASE ==========
			// Create BAB project (now that workflow/status/types exist)
			final CProject_Bab project = CProject_BabInitializerService.initializeSampleBab(company, minimal);
			sessionService.setActiveProject(project);
			// Initialize UI views
			initializeStandardViews(project);
			// Initialize system settings
			CSystemSettings_BabInitializerService.initializeSample(company, minimal);
			// Initialize dashboard projects
			CDashboardProject_BabInitializerService.initializeSample(project, minimal);
			// ========== BAB ENTITY INITIALIZATION ==========
			// Initialize BAB devices and nodes (sample data)
			CBabDeviceInitializerService.initializeSample(project, minimal);
			entityManager.flush();
			LOGGER.info("BAB minimal data loaded successfully");
		} catch (final Exception e) {
			LOGGER.error("Error loading BAB minimal data", e);
			throw e;
		}
	}

	public void loadSampleData(final boolean minimal) throws Exception {
		loadMinimalData(minimal);
	}

	@Transactional
	public void reloadForced(final boolean minimal) throws Exception {
		LOGGER.info("BAB data reload (forced) started (minimal={})", minimal);
		clearSampleData();
		loadMinimalData(minimal);
		LOGGER.info("BAB data reload (forced) finished");
	}
}
