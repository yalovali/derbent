package tech.derbent.base.data;

import java.sql.Connection;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import tech.derbent.config.CSampleDataInitializer;

@Service
public class DatabaseResetService {

    @Autowired
    private CSampleDataInitializer sampleDataInitializer;

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final DataSource dataSource;

    public DatabaseResetService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void resetDatabase() throws Exception {
        LOGGER.info("Resetting database to initial state...");

        try {
            // Drop all tables to ensure clean state
            LOGGER.info("Dropping all tables for clean reset...");
            final Connection connection = dataSource.getConnection();
            try {
                // For H2, drop all objects to ensure clean state
                connection.createStatement().execute("DROP ALL OBJECTS");
                LOGGER.info("All database objects dropped successfully");
            } catch (final Exception e) {
                LOGGER.warn("Could not drop all objects, continuing with reset: {}", e.getMessage());
            } finally {
                connection.close();
            }
        } catch (final Exception e) {
            LOGGER.warn("Error during database cleanup, continuing: {}", e.getMessage());
        }

        // Check if data.sql exists before trying to execute it
        final ClassPathResource dataResource = new ClassPathResource("data.sql");
        if (dataResource.exists()) {
            LOGGER.info("Found data.sql, executing SQL script...");
            final ResourceDatabasePopulator populator = new ResourceDatabasePopulator(dataResource);
            populator.execute(dataSource);
        } else {
            LOGGER.info("No data.sql found, skipping SQL script execution");
        }

        // Reinitialize sample data
        LOGGER.info("Reinitializing sample data...");
        sampleDataInitializer.loadSampleData();
        LOGGER.info("Database reset completed successfully");
    }
}
