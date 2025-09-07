package unit_tests.tech.derbent;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import tech.derbent.Application;

/** Test to verify that the Application's dataInitializer handles the PostgreSQL "relation cuser does not exist" error gracefully. */
public class ApplicationDataInitializerErrorHandlingTest {

	@Test
	public void shouldHandleCuserTableNotExistError() throws Exception {
		// Create a mock JdbcTemplate that throws the exact error from the problem
		// statement
		final JdbcTemplate mockJdbcTemplate = mock(JdbcTemplate.class);
		// Simulate the PostgreSQL error: "relation cuser does not exist"
		when(mockJdbcTemplate.queryForObject(eq("SELECT COUNT(*) FROM cuser"), eq(Integer.class)))
				.thenThrow(new DataAccessException("ERROR: relation \"cuser\" does not exist") {

					private static final long serialVersionUID = 1L;
				});
		// Create the Application instance and get the dataInitializer bean
		final Application app = new Application();
		final ApplicationRunner dataInitializer = app.dataInitializer(mockJdbcTemplate);
		// Create mock application arguments
		final ApplicationArguments args = new DefaultApplicationArguments();
		// The dataInitializer should not throw an exception when the table doesn't exist
		assertDoesNotThrow(() -> {
			dataInitializer.run(args);
		}, "dataInitializer should handle 'cuser table does not exist' error gracefully");
	}
}
