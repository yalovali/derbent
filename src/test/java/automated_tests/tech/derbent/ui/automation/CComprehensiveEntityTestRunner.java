package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

/** Test runner that executes the comprehensive generic entity tests and generates screenshots showing all operations working. This class demonstrates
 * the "Don't Repeat Yourself" principle by using the generic superclass to test all entity views without writing individual test classes for each
 * entity type. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
public class CComprehensiveEntityTestRunner extends CGenericEntityPlaywrightTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComprehensiveEntityTestRunner.class);

	/** Runs the comprehensive test suite and generates visual proof that all entity operations work correctly. */
	@Test
	void runComprehensiveEntityTestSuite() {
		LOGGER.info("🚀 Starting comprehensive test suite for all entity types");
		// This will automatically run the demonstrateComprehensiveTestingApproach test
		// which shows the pattern working for multiple entities
		demonstrateComprehensiveTestingApproach();
		LOGGER.info("✅ Comprehensive test suite completed");
		LOGGER.info("📸 Screenshots generated in target/screenshots/ directory");
		LOGGER.info("🎯 All entity types tested using generic superclass pattern");
	}
}
