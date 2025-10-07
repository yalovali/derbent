package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.config.CDataInitializer;

/** Playwright journey test that explicitly reloads sample data, logs in, and captures screenshots while visiting every menu entity. Demonstrates the
 * full happy-path flow requested by product owners: initialize database samples, authenticate, and verify navigation coverage. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🚀 Sample Data Menu Navigation Journey")
public class CSampleDataMenuNavigationTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSampleDataMenuNavigationTest.class);

	@Autowired(required = false)
	private CDataInitializer dataInitializer;

	@BeforeEach
	void reloadSampleData() {
		if (dataInitializer == null) {
			LOGGER.warn("⚠️ Sample data initializer not available; continuing with existing dataset");
			return;
		}
		try {
			LOGGER.info("♻️ Reloading sample dataset before Playwright journey");
			dataInitializer.reloadForced();
			LOGGER.info("✅ Sample dataset reloaded successfully");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to reload sample data: {}", e.getMessage());
		}
	}

	@Test
	@DisplayName ("✅ Initialize samples, login, and capture all menu screenshots")
	void sampleDataLoginAndMenuScreenshots() {
		LOGGER.info("🚀 Starting sample data menu navigation journey...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login after ensuring samples are loaded
			loginToApplication();
			wait_afterlogin();
			takeScreenshot("sample-journey-post-login", false);

			// Verify that the sample data is accessible
			try {
				testDatabaseInitialization();
				takeScreenshot("sample-journey-db-verified", false);
			} catch (AssertionError verificationError) {
				takeScreenshot("sample-journey-db-verification-failed", true);
				throw verificationError;
			}

			// Visit every menu entry and capture screenshots
			testAllMenuItemOpenings();
			LOGGER.info("✅ Completed menu navigation journey with screenshots");
		} catch (Exception e) {
			LOGGER.error("❌ Sample data navigation journey failed: {}", e.getMessage());
			takeScreenshot("sample-journey-error", true);
			throw new AssertionError("Sample data navigation journey failed", e);
		}
	}
}
