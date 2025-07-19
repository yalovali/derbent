package tech.derbent.base.data;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

@Service
public class DatabaseResetService {

	private final DataSource dataSource;

	public DatabaseResetService(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void resetDatabase() {
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator(new ClassPathResource("data.sql"));
		populator.execute(dataSource);
	}
}
