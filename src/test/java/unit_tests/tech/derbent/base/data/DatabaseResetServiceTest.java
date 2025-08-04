package unit_tests.tech.derbent.base.data;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import tech.derbent.base.data.DatabaseResetService;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test for DatabaseResetService to ensure it handles missing data.sql gracefully.
 */
@SpringBootTest
@TestPropertySource (properties = {
	"spring.jpa.hibernate.ddl-auto=create" }
)
public class DatabaseResetServiceTest extends CTestBase {

	@Autowired
	private DatabaseResetService databaseResetService;

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}

	@Test
	public void testDatabaseResetWithoutDataSql() {
		// This should not throw any exceptions when data.sql doesn't exist
		assertDoesNotThrow(() -> databaseResetService.resetDatabase());
	}
}