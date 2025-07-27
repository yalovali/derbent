package tech.derbent.base.data;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

@Service
public class DatabaseResetService {

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	private final DataSource dataSource;

	public DatabaseResetService(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void resetDatabase() {
		LOGGER.info("Resetting database to initial state...");
		final ResourceDatabasePopulator populator =
			new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
		populator.execute(dataSource);
	}
}
