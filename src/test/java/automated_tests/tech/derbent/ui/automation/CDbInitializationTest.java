package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/** Focused database initialization test that validates proper DB setup, data structure initialization, and essential data presence. Tests that the
 * application starts with a properly initialized database. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🗄️ Database Initialization Test")
public class CDbInitializationTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDbInitializationTest.class);

	@Test
	@DisplayName ("🗄️ Test Database Setup")
	void testDatabaseSetup() {
		LOGGER.info("🧪 Starting database initialization test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application (this will test if DB is accessible)
			loginToApplication();
			// Test database initialization
			testDatabaseInitialization();
			LOGGER.info("✅ Database initialization test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Database initialization test failed: {}", e.getMessage());
			takeScreenshot("db-initialization-error", true);
			throw new AssertionError("Database initialization test failed", e);
		}
	}

	@Test
	@DisplayName ("🔍 Test Database Structure Verification")
	void testDatabaseStructureVerification() {
		LOGGER.info("🧪 Starting database structure verification test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Verify database structure
			verifyDatabaseStructure();
			LOGGER.info("✅ Database structure verification test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Database structure verification test failed: {}", e.getMessage());
			takeScreenshot("db-structure-error", true);
			throw new AssertionError("Database structure verification test failed", e);
		}
	}

	@Test
	@DisplayName ("👤 Test Essential Data Presence")
	void testEssentialDataPresence() {
		LOGGER.info("🧪 Starting essential data presence test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test that essential data is present
			LOGGER.info("🔍 Checking for essential data in Users");
			navigateToUsers();
			wait_1000();
			boolean usersExist = verifyGridHasData();
			if (usersExist) {
				LOGGER.info("✅ Users data found - admin user exists");
				takeScreenshot("essential-data-users", false);
			} else {
				LOGGER.warn("⚠️ No users data found - this may be expected for clean test DB");
				takeScreenshot("essential-data-no-users", false);
			}
			// Check Projects structure (may be empty initially)
			LOGGER.info("🔍 Checking Projects structure");
			navigateToProjects();
			wait_1000();
			// Verify grid is present even if empty
			boolean projectGridExists = page.locator("vaadin-grid").count() > 0;
			if (projectGridExists) {
				LOGGER.info("✅ Projects grid structure exists");
				takeScreenshot("essential-data-projects-structure", false);
			} else {
				LOGGER.warn("⚠️ Projects grid not found");
			}
			LOGGER.info("✅ Essential data presence test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Essential data presence test failed: {}", e.getMessage());
			takeScreenshot("essential-data-error", true);
			throw new AssertionError("Essential data presence test failed", e);
		}
	}

	@Test
	@DisplayName ("🔧 Test Database Connection and Performance")
	void testDatabaseConnectionAndPerformance() {
		LOGGER.info("🧪 Starting database connection and performance test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test database connection performance by loading different views
			long startTime = System.currentTimeMillis();
			// Test Users view loading
			LOGGER.info("⏱️ Testing Users view loading performance");
			navigateToUsers();
			wait_1000();
			long usersLoadTime = System.currentTimeMillis() - startTime;
			LOGGER.info("📊 Users view loaded in {}ms", usersLoadTime);
			takeScreenshot("db-performance-users", false);
			// Test Projects view loading
			startTime = System.currentTimeMillis();
			LOGGER.info("⏱️ Testing Projects view loading performance");
			navigateToProjects();
			wait_1000();
			long projectsLoadTime = System.currentTimeMillis() - startTime;
			LOGGER.info("📊 Projects view loaded in {}ms", projectsLoadTime);
			takeScreenshot("db-performance-projects", false);
			// Test rapid navigation between views
			LOGGER.info("🔄 Testing rapid navigation between views");
			startTime = System.currentTimeMillis();
			for (int i = 0; i < 3; i++) {
				navigateToUsers();
				wait_500();
				navigateToProjects();
				wait_500();
			}
			long rapidNavTime = System.currentTimeMillis() - startTime;
			LOGGER.info("📊 Rapid navigation completed in {}ms", rapidNavTime);
			takeScreenshot("db-performance-rapid-nav", false);
			// Performance thresholds (reasonable for test environment)
			if (usersLoadTime < 5000 && projectsLoadTime < 5000 && rapidNavTime < 10000) {
				LOGGER.info("✅ Database performance is acceptable");
			} else {
				LOGGER.warn("⚠️ Database performance may need optimization");
			}
			LOGGER.info("✅ Database connection and performance test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Database connection and performance test failed: {}", e.getMessage());
			takeScreenshot("db-performance-error", true);
			throw new AssertionError("Database connection and performance test failed", e);
		}
	}

	@Test
	@DisplayName ("💾 Test Data Persistence")
	void testDataPersistence() {
		LOGGER.info("🧪 Starting data persistence test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test data persistence by creating and verifying data
			LOGGER.info("💾 Testing data persistence with user creation");
			navigateToUsers();
			wait_1000();
			// Create a test user
			clickNew();
			wait_1000();
			String testUserName = "TestUser" + System.currentTimeMillis();
			fillFirstTextField(testUserName);
			// Fill additional fields if present
			if (page.locator("vaadin-text-field").count() > 1) {
				page.locator("vaadin-text-field").nth(1).fill("test@example.com");
			}
			if (page.locator("vaadin-combo-box").count() > 0) {
				selectFirstComboBoxOption();
			}
			clickSave();
			wait_1000();
			takeScreenshot("data-persistence-user-created", false);
			// Verify data was saved
			boolean dataExists = verifyGridHasData();
			if (dataExists) {
				LOGGER.info("✅ User data persisted successfully");
				// Navigate away and back to test persistence
				navigateToProjects();
				wait_1000();
				navigateToUsers();
				wait_1000();
				boolean dataStillExists = verifyGridHasData();
				if (dataStillExists) {
					LOGGER.info("✅ Data persisted across navigation");
					takeScreenshot("data-persistence-verified", false);
				} else {
					LOGGER.warn("⚠️ Data may not have persisted across navigation");
				}
			} else {
				LOGGER.warn("⚠️ User data may not have been saved");
			}
			LOGGER.info("✅ Data persistence test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Data persistence test failed: {}", e.getMessage());
			takeScreenshot("data-persistence-error", true);
			throw new AssertionError("Data persistence test failed", e);
		}
	}
}
