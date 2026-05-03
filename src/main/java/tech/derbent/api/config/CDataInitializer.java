package tech.derbent.api.config;

import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CSystemInitExcelBootstrapService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.roles.service.CUserCompanyRoleService;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.plm.activities.service.CActivityPriorityService;
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.activities.service.CActivityTypeService;
import tech.derbent.plm.decisions.service.CDecisionService;
import tech.derbent.plm.kanban.kanbanline.service.CKanbanLineService;
import tech.derbent.plm.meetings.service.CMeetingService;
import tech.derbent.plm.meetings.service.CMeetingTypeService;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
import tech.derbent.plm.orders.type.service.COrderTypeService;
import tech.derbent.plm.project.domain.CProject_Derbent;
import tech.derbent.plm.risks.risk.service.CRiskService;
import tech.derbent.plm.risks.risktype.service.CRiskTypeService;

/** System bootstrap: clears DB and delegates to CSystemInitExcelBootstrapService which runs Excel
 * import and initializes all grids/screens/pages via CScreensInitializerService. */
public class CDataInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDataInitializer.class);

	private static boolean isH2(final DataSource dataSource) {
		if (dataSource == null) {
			return false;
		}
		try (Connection connection = dataSource.getConnection()) {
			final String product = connection.getMetaData().getDatabaseProductName();
			return product != null && product.toLowerCase().contains("h2");
		} catch (final Exception e) {
			LOGGER.debug("Unable to detect database product for sample data cleanup: {}", e.getMessage());
			return false;
		}
	}

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

	private final CActivityPriorityService activityPriorityService;
	private final CActivityService activityService;
	private final CActivityTypeService activityTypeService;
	private final CCurrencyService currencyService;
	private final CDecisionService decisionService;
	private final JdbcTemplate jdbcTemplate;
	private final CKanbanLineService kanbanLineService;
	private final CMeetingService meetingService;
	private final CMeetingTypeService meetingTypeService;
	private final COrderTypeService orderTypeService;
	private final CPageEntityService pageEntityService;
	private final CProjectService<CProject_Derbent> projectService;
	private final CRiskService riskService;
	private final CRiskTypeService riskTypeService;
	private final CDetailLinesService screenLinesService;
	private final CDetailSectionService screenService;
	private final CProjectItemStatusService statusService;
	private final CUserCompanyRoleService userCompanyRoleService;
	private final CUserProjectRoleService userProjectRoleService;
	private final CUserService userService;

	@SuppressWarnings ("unchecked")
	public CDataInitializer() {
		LOGGER.info("DataInitializer starting - obtaining service beans from application context");
		projectService = CSpringContext.getBean(CProjectService.class);
		userService = CSpringContext.getBean(CUserService.class);
		activityService = CSpringContext.getBean(CActivityService.class);
		activityPriorityService = CSpringContext.getBean(CActivityPriorityService.class);
		activityTypeService = CSpringContext.getBean(CActivityTypeService.class);
		riskTypeService = CSpringContext.getBean(CRiskTypeService.class);
		meetingTypeService = CSpringContext.getBean(CMeetingTypeService.class);
		orderTypeService = CSpringContext.getBean(COrderTypeService.class);
		meetingService = CSpringContext.getBean(CMeetingService.class);
		riskService = CSpringContext.getBean(CRiskService.class);
		decisionService = CSpringContext.getBean(CDecisionService.class);
		currencyService = CSpringContext.getBean(CCurrencyService.class);
		screenService = CSpringContext.getBean(CDetailSectionService.class);
		screenLinesService = CSpringContext.getBean(CDetailLinesService.class);
		pageEntityService = CSpringContext.getBean(CPageEntityService.class);
		userProjectRoleService = CSpringContext.getBean(CUserProjectRoleService.class);
		userCompanyRoleService = CSpringContext.getBean(CUserCompanyRoleService.class);
		statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		kanbanLineService = CSpringContext.getBean(CKanbanLineService.class);
		jdbcTemplate = CSpringContext.getBean(JdbcTemplate.class);
		LOGGER.info("All service beans obtained successfully");
	}

	@Transactional
	private void clearSampleData() {
		try {
			// ---- 1) PostgreSQL: TRUNCATE all public tables with CASCADE
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
						jdbcTemplate.execute("TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE");
						LOGGER.info("All public tables truncated (PostgreSQL).");
						return;
					}
					LOGGER.warn("No user tables found to truncate in public schema.");
				} catch (final Exception pgEx) {
					LOGGER.warn("PostgreSQL truncate path failed. Falling back to JPA deletes. Cause: {}",
							pgEx.getMessage());
				}
			} else {
				LOGGER.debug("Skipping PostgreSQL truncate path; database is not PostgreSQL.");
			}
			// ---- 2) H2: disable FK checks and DELETE all rows
			if (isH2(jdbcTemplate.getDataSource())) {
				try {
					final List<String> tableNames = jdbcTemplate.queryForList("""
							SELECT TABLE_NAME
							FROM INFORMATION_SCHEMA.TABLES
							WHERE TABLE_SCHEMA = 'PUBLIC'
							  AND TABLE_TYPE = 'BASE TABLE'
							  AND TABLE_NAME NOT IN ('FLYWAY_SCHEMA_HISTORY')
							""", String.class);
					if (!tableNames.isEmpty()) {
						jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
						try {
							for (final String tableName : tableNames) {
								jdbcTemplate.execute("DELETE FROM \"" + tableName + "\"");
							}
						} finally {
							jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
						}
						LOGGER.info("All public tables cleared (H2).");
						return;
					}
					LOGGER.warn("No user tables found to clear in H2 PUBLIC schema.");
				} catch (final Exception h2Ex) {
					LOGGER.warn("H2 cleanup path failed. Falling back to JPA deletes. Cause: {}", h2Ex.getMessage());
				}
			}
			// ---- 3) Fallback: JPA batch delete (FK order matters)
			meetingService.deleteAllInBatch();
			meetingTypeService.deleteAllInBatch();
			decisionService.deleteAllInBatch();
			activityService.deleteAllInBatch();
			activityPriorityService.deleteAllInBatch();
			activityTypeService.deleteAllInBatch();
			riskTypeService.deleteAllInBatch();
			riskService.deleteAllInBatch();
			screenLinesService.deleteAllInBatch();
			screenService.deleteAllInBatch();
			currencyService.deleteAllInBatch();
			orderTypeService.deleteAllInBatch();
			statusService.deleteAllInBatch();
			userService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			pageEntityService.deleteAllInBatch();
			userProjectRoleService.deleteAllInBatch();
			userCompanyRoleService.deleteAllInBatch();
			kanbanLineService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during sample data cleanup reason={}", e.getMessage());
			throw e;
		}
	}

	public boolean isDatabaseEmpty() {
		final long cnt = userService.count();
		LOGGER.info("User count = {}", cnt);
		return cnt == 0;
	}

	/** Excel-first DB reset. Clears DB, then delegates to CSystemInitExcelBootstrapService which
	 * runs Excel import and initializes all screens via CScreensInitializerService. */
	public void reloadForcedExcel(final boolean minimal) throws Exception {
		LOGGER.info("DB reset (Excel-first) started");
		clearSampleData();
		final CSystemInitExcelBootstrapService excelBootstrap =
				CSpringContext.getBean(CSystemInitExcelBootstrapService.class);
		final var summary = excelBootstrap.bootstrapAfterReset(minimal);
		LOGGER.info("DB reset (Excel-first) finished: {}", summary.toUiSummary());
	}
}
