package tech.derbent.api.config;

import java.util.Objects;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

/**
 * Service to detect and report the active database configuration.
 * This service helps identify which database (PostgreSQL, H2, etc.) is currently being used.
 */
@Service
public class CDatabaseConfigService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDatabaseConfigService.class);

	@Autowired
	private Environment environment;

	@Autowired
	private DataSource dataSource;

	// Cache the detected database type to avoid repeated connection requests
	private DatabaseType cachedDatabaseType = null;

	/**
	 * Enumeration of supported database types
	 */
	public enum DatabaseType {
		POSTGRESQL("PostgreSQL", "PostgreSQL Database"),
		H2("H2", "H2 In-Memory Database"),
		UNKNOWN("Unknown", "Unknown Database");

		private final String shortName;
		private final String displayName;

		DatabaseType(final String shortName, final String displayName) {
			this.shortName = shortName;
			this.displayName = displayName;
		}

		public String getShortName() {
			return shortName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	/**
	 * Detects the current database type based on the datasource URL
	 * Result is cached after first detection to avoid repeated connection requests.
	 * 
	 * @return the detected database type
	 */
	public DatabaseType detectDatabaseType() {
		// Return cached value if already detected
		if (cachedDatabaseType != null) {
			return cachedDatabaseType;
		}

		try {
			final String url = dataSource.getConnection().getMetaData().getURL();
			LOGGER.debug("Database URL: {}", url);

			if (url == null || url.trim().isEmpty()) {
				cachedDatabaseType = DatabaseType.UNKNOWN;
				return cachedDatabaseType;
			}

			if (url.toLowerCase().contains("postgresql")) {
				cachedDatabaseType = DatabaseType.POSTGRESQL;
			} else if (url.toLowerCase().contains("h2")) {
				cachedDatabaseType = DatabaseType.H2;
			} else {
				cachedDatabaseType = DatabaseType.UNKNOWN;
			}

			return cachedDatabaseType;
		} catch (final Exception e) {
			LOGGER.error("Error detecting database type", e);
			cachedDatabaseType = DatabaseType.UNKNOWN;
			return cachedDatabaseType;
		}
	}

	/**
	 * Gets the active Spring profiles
	 * 
	 * @return comma-separated list of active profiles, or "default" if none
	 */
	public String getActiveProfiles() {
		final String[] profiles = environment.getActiveProfiles();
		if (profiles.length == 0) {
			return "default";
		}
		return String.join(", ", profiles);
	}

	/**
	 * Checks if the H2 profile is active
	 * 
	 * @return true if H2 profile is active
	 */
	public boolean isH2ProfileActive() {
		final String[] profiles = environment.getActiveProfiles();
		for (final String profile : profiles) {
			if (profile.toLowerCase().contains("h2")) {
				return true;
			}
		}
		// Also check if we're using H2 based on datasource
		return detectDatabaseType() == DatabaseType.H2;
	}

	/**
	 * Gets instructions for switching database type
	 * 
	 * @param targetType the target database type
	 * @return instructions for switching
	 */
	public String getSwitchInstructions(final DatabaseType targetType) {
		if (targetType == DatabaseType.H2) {
			return "To use H2 database, restart the application with:\n" +
					"mvn spring-boot:run -Dspring.profiles.active=h2-local-development";
		} else if (targetType == DatabaseType.POSTGRESQL) {
			return "To use PostgreSQL database, restart the application with:\n" +
					"mvn spring-boot:run\n" +
					"(or without any profile specification)";
		}
		return "Database switching requires application restart.";
	}
}
