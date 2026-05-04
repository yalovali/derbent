package tech.derbent.api.config;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Applies one-time idempotent schema fixes that ddl-auto=update cannot handle automatically.
 * <p>
 * WHY: When entity classes are refactored from standalone @Entity to JOINED inheritance children, the root table inherits all @MappedSuperclass fields
 * (e.g. active, name) while the child table only keeps its own columns. However, ddl-auto=update never drops columns, so legacy schemas retain the old
 * columns (e.g. cepic.active NOT NULL) without a DEFAULT, causing Hibernate inserts to fail. This service adds the missing DEFAULT so the DB accepts
 * inserts without the child table needing to re-declare every inherited column.
 * </p> */
@Component
@Order (2)
public class CSchemaMaintenanceService implements CommandLineRunner {

	// Tables that were previously standalone @Entity classes and were later merged into JOINED
	// inheritance under CAgileEntity. They may retain NOT-NULL columns whose DEFAULTs were dropped.
	private static final List<String> AGILE_CHILD_TABLES = List.of("cepic", "cfeature", "cuserstory");

	private static final Logger LOGGER = LoggerFactory.getLogger(CSchemaMaintenanceService.class);

	private final JdbcTemplate jdbcTemplate;

	public CSchemaMaintenanceService(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	/** Returns true when the given column exists in the given table (PostgreSQL only). */
	private boolean columnExists(final String table, final String column) {
		final Integer cnt = jdbcTemplate.queryForObject("""
				SELECT COUNT(*)
				FROM information_schema.columns
				WHERE table_schema = 'public'
				  AND table_name   = ?
				  AND column_name  = ?
				""", Integer.class, table, column);
		return cnt != null && cnt > 0;
	}

	/** Returns true when the column already has a column_default (PostgreSQL only). */
	private boolean hasDefault(final String table, final String column) {
		final String def = jdbcTemplate.queryForObject("""
				SELECT column_default
				FROM information_schema.columns
				WHERE table_schema = 'public'
				  AND table_name   = ?
				  AND column_name  = ?
				""", String.class, table, column);
		return def != null && !def.isBlank();
	}

	/** For each legacy agile-child table, if the 'active' column exists without a DEFAULT, set DEFAULT true.
	 * This is safe and idempotent: the import never writes to these columns; the DEFAULT ensures Hibernate's
	 * child-table INSERT (which only includes PK + entity-specific columns) does not violate NOT NULL. */
	private void fixLegacyActiveDefaults() {
		for (final String table : AGILE_CHILD_TABLES) {
			try {
				if (!columnExists(table, "active")) {
					continue;
				}
				if (hasDefault(table, "active")) {
					continue;
				}
				jdbcTemplate.execute("ALTER TABLE " + table + " ALTER COLUMN active SET DEFAULT true");
				LOGGER.info("Schema fix applied: {}.active SET DEFAULT true", table);
			} catch (final Exception e) {
				LOGGER.warn("Schema fix skipped for {}.active: {}", table, e.getMessage());
			}
		}
	}

	@Override
	public void run(final String... args) {
		try {
			fixLegacyActiveDefaults();
		} catch (final Exception e) {
			LOGGER.warn("CSchemaMaintenanceService skipped (non-PostgreSQL or no schema access): {}", e.getMessage());
		}
	}
}
