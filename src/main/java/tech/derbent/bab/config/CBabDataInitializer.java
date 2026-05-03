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
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.imports.service.CSystemInitExcelBootstrapService;
import tech.derbent.api.users.service.CUserService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.device.service.CBabDeviceService;
import tech.derbent.bab.project.service.CProject_BabService;

@Component
@Profile ("bab")
public class CBabDataInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDataInitializer.class);

	private static boolean isPostgreSql(final DataSource dataSource) {
		return isDatabaseProduct(dataSource, "postgresql");
	}

	private static boolean isH2(final DataSource dataSource) {
		return isDatabaseProduct(dataSource, "h2");
	}

	private static boolean isDatabaseProduct(final DataSource dataSource, final String productKeyword) {
		if (dataSource == null) {
			return false;
		}
		try (Connection connection = dataSource.getConnection()) {
			final String product = connection.getMetaData().getDatabaseProductName();
			return product != null && product.toLowerCase().contains(productKeyword);
		} catch (final Exception e) {
			LOGGER.debug("Unable to detect database product for sample data cleanup: {}", e.getMessage());
			return false;
		}
	}

	private final CBabDeviceService babDeviceService;
	// Service dependencies - injected via constructor
	private final JdbcTemplate jdbcTemplate;
	private final CProject_BabService projectService;
	private final CUserService userService;
	// ========================================================================
	// UTILITY METHODS
	// ========================================================================

	public CBabDataInitializer(final JdbcTemplate jdbcTemplate,
			final CBabDeviceService babDeviceService,
			final CProject_BabService projectService,
			final CUserService userService) {
		Check.notNull(jdbcTemplate, "JdbcTemplate cannot be null");
		Check.notNull(babDeviceService, "BabDeviceService cannot be null");
		Check.notNull(projectService, "ProjectService cannot be null");
		Check.notNull(userService, "UserService cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.babDeviceService = babDeviceService;
		this.projectService = projectService;
		this.userService = userService;
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
					LOGGER.warn("No user tables found to truncate in H2 PUBLIC schema.");
				} catch (final Exception h2Ex) {
					LOGGER.warn("H2 truncate path failed. Falling back to JPA deletes. Cause: {}", h2Ex.getMessage());
				}
			}
			// Fallback: JPA batch delete (FK order matters)
			babDeviceService.deleteAllInBatch();
			projectService.deleteAllInBatch();
			userService.deleteAllInBatch();
			LOGGER.info("Fallback JPA deleteAllInBatch completed.");
		} catch (final Exception e) {
			LOGGER.error("Error during BAB sample data cleanup reason={}", e.getMessage());
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

	/**
	 * @deprecated Sample creator methods are retired; use {@link #reloadForcedExcel(boolean)} which bootstraps from committed Excel templates.
	 */
	@Deprecated(forRemoval = true)
	public void loadSampleData(final boolean minimal) throws Exception {
		throw new UnsupportedOperationException("Sample data initialization via Java initializers is disabled. Use reloadForcedExcel(minimal) to import system_init.xlsx.");
	}

	/** Backward-compatible entrypoint: delegates to Excel-first reset. */
	@Transactional
	public void reloadForced(final boolean minimal) throws Exception {
		reloadForcedExcel(minimal);
	}

	@Transactional
	public void reloadForcedExcel(final boolean minimal) throws Exception {
		try {
			LOGGER.info("BAB data reload (forced, Excel-first) started (minimal={})", minimal);
			clearSampleData();
			final CSystemInitExcelBootstrapService excelBootstrap = CSpringContext.getBean(CSystemInitExcelBootstrapService.class);
			final var summary = excelBootstrap.bootstrapAfterReset(minimal);
			LOGGER.info("BAB data reload (forced, Excel-first) finished: {}", summary.toUiSummary());
		} catch (final Exception e) {
			LOGGER.error("Error during BAB Excel-first reset: {}", e.getMessage());
			throw e;
		}
	}
}
