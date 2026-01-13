package tech.derbent.bab.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
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
import tech.derbent.api.page.service.CPageEntityInitializerService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectInitializerService;
import tech.derbent.api.projects.service.CProjectTypeInitializerService;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.roles.service.CUserCompanyRoleInitializerService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityInitializerService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.service.CWorkflowEntityInitializerService;
import tech.derbent.base.setup.service.CSystemSettingsInitializerService;
import tech.derbent.base.users.service.CUserInitializerService;

@Component
@Profile ("bab")
public class CBabDataInitializer {

	@FunctionalInterface
	private interface IBabUiInitializer {

		void initialize(CProject project) throws Exception;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDataInitializer.class);
	private final CPageEntityService pageEntityService;
	private final JdbcTemplate jdbcTemplate;
	private final CDetailSectionService detailSectionService;
	private final CGridEntityService gridEntityService;
	@PersistenceContext
	private EntityManager entityManager;
	private final tech.derbent.bab.device.service.CBabDeviceService babDeviceService;
	private final tech.derbent.bab.node.service.CBabNodeService babNodeService;

	public CBabDataInitializer(final JdbcTemplate jdbcTemplate, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService,
			final tech.derbent.bab.device.service.CBabDeviceService babDeviceService,
			final tech.derbent.bab.node.service.CBabNodeService babNodeService) {
		Check.notNull(jdbcTemplate, "JdbcTemplate cannot be null");
		Check.notNull(gridEntityService, "GridEntityService cannot be null");
		Check.notNull(detailSectionService, "DetailSectionService cannot be null");
		Check.notNull(pageEntityService, "PageEntityService cannot be null");
		Check.notNull(babDeviceService, "BabDeviceService cannot be null");
		Check.notNull(babNodeService, "BabNodeService cannot be null");
		this.jdbcTemplate = jdbcTemplate;
		this.gridEntityService = gridEntityService;
		this.detailSectionService = detailSectionService;
		this.pageEntityService = pageEntityService;
		this.babDeviceService = babDeviceService;
		this.babNodeService = babNodeService;
	}

	private void clearDatabase() {
		LOGGER.debug("Clearing BAB data from database (forced)");
		final DataSource dataSource = jdbcTemplate.getDataSource();
		Check.notNull(dataSource, "DataSource cannot be null");
		try (Connection connection = dataSource.getConnection()) {
			final String productName = connection.getMetaData().getDatabaseProductName();
			if (isPostgreSql(productName)) {
				truncatePostgresTables(connection);
				return;
			}
			truncateTablesGeneric(connection, productName);
		} catch (final Exception e) {
			LOGGER.error("Error during BAB data cleanup", e);
			throw new RuntimeException("Failed to clear BAB data", e);
		}
	}

	private void initializeStandardViews(final CProject project) throws Exception {
		try {
			final List<IBabUiInitializer> initializers =
					List.of(p -> CSystemSettingsInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CCompanyInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CUserInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CUserCompanyRoleInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CProjectInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CGridEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService),
							p -> CPageEntityInitializerService.initialize(p, gridEntityService, detailSectionService, pageEntityService));
			for (final IBabUiInitializer initializer : initializers) {
				initializer.initialize(project);
			}
		} catch (final Exception e) {
			LOGGER.error("Error initializing BAB standard views", e);
			throw e;
		}
	}

	private boolean isPostgreSql(final String productName) {
		return productName != null && productName.toLowerCase().contains("postgresql");
	}

	private void loadMinimalData(final boolean minimal) throws Exception {
		try {
			Check.notNull(entityManager, "EntityManager cannot be null");
			final CCompany company = CCompanyInitializerService.initializeSampleBab(minimal);
			final CUserCompanyRole adminRole = CUserCompanyRoleInitializerService.initializeSampleBab(company, minimal);
			CUserInitializerService.initializeSampleBab(company, adminRole, minimal);
			CWorkflowEntityInitializerService.initializeSampleBab(company, minimal);
			CProjectTypeInitializerService.initializeSampleBab(company, minimal);
			final CProject project = CProjectInitializerService.initializeSampleBab(company, minimal);
			initializeStandardViews(project);
			CSystemSettingsInitializerService.initializeSampleBab(project, minimal);
			// Initialize BAB device and nodes
			tech.derbent.bab.device.service.CBabDeviceInitializerService.initializeSample(project, minimal);
			if (!minimal) {
				LOGGER.info("BAB initializer uses minimal defaults for all modes.");
			}
			entityManager.flush();
		} catch (final Exception e) {
			LOGGER.error("Error loading BAB minimal data", e);
			throw e;
		}
	}

	private String normalizeQuote(final String quote) {
		if (quote == null) {
			return "";
		}
		final String trimmed = quote.trim();
		return trimmed.isEmpty() ? "" : trimmed;
	}

	private String normalizeSchema(final String schema) {
		if (schema == null || schema.isBlank()) {
			return "PUBLIC";
		}
		return schema;
	}

	@Transactional
	public void reloadForced(final boolean minimal) throws Exception {
		LOGGER.info("BAB data reload (forced) started");
		clearDatabase();
		loadMinimalData(minimal);
		LOGGER.info("BAB data reload (forced) finished");
	}

	private void truncatePostgresTables(final Connection connection) throws Exception {
		final List<String> tableNames = new ArrayList<>();
		try (Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery("""
				SELECT tablename
				FROM pg_tables
				WHERE schemaname = 'public'
				  AND tablename NOT IN ('flyway_schema_history')
				""")) {
			while (rs.next()) {
				tableNames.add(rs.getString(1));
			}
		}
		if (tableNames.isEmpty()) {
			LOGGER.warn("No user tables found to truncate in public schema.");
			return;
		}
		final String joined = String.join(", ", tableNames.stream().map(name -> "\"" + name + "\"").toList());
		try (Statement statement = connection.createStatement()) {
			final String sql = "TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE";
			LOGGER.debug("Executing: {}", sql);
			statement.execute(sql);
			LOGGER.info("All public tables truncated (PostgreSQL).");
		}
	}

	private void truncateTablesGeneric(final Connection connection, final String productName) throws Exception {
		final DatabaseMetaData metaData = connection.getMetaData();
		final String quote = normalizeQuote(metaData.getIdentifierQuoteString());
		final String schema = normalizeSchema(connection.getSchema());
		final List<String> tables = new ArrayList<>();
		try (ResultSet rs = metaData.getTables(connection.getCatalog(), schema, "%", new String[] {
				"TABLE"
		})) {
			while (rs.next()) {
				final String tableName = rs.getString("TABLE_NAME");
				if ("flyway_schema_history".equalsIgnoreCase(tableName)) {
					continue;
				}
				tables.add(tableName);
			}
		}
		final boolean disableReferentialIntegrity = productName != null && productName.toLowerCase().contains("h2");
		try (Statement statement = connection.createStatement()) {
			if (disableReferentialIntegrity) {
				statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
			}
			for (final String tableName : tables) {
				final String qualified = quote.isEmpty() ? tableName : quote + tableName + quote;
				statement.execute("TRUNCATE TABLE " + qualified);
			}
			if (disableReferentialIntegrity) {
				statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
			}
		}
		LOGGER.info("All tables truncated (generic).");
	}
}
