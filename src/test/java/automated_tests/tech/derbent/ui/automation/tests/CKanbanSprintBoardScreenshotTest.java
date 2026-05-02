package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;

// KEYWORDS: Kanban, Screenshot, SprintBoard, Playwright
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent",
		"server.port=0",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("📸 Kanban sprint board screenshot")
public class CKanbanSprintBoardScreenshotTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanSprintBoardScreenshotTest.class);

	@Test
	@DisplayName ("✅ Capture Kanban LinesSprint Board screenshot")
	void testCaptureSprintBoardScreenshot() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();
		assertTrue(navigateByMenuSearch("Kanban LinesSprint Board"), "Could not navigate to Kanban LinesSprint Board");
		page.waitForSelector(".kanban-column");
		wait_1000();
		takeScreenshot("kanban-sprint-board", false);
	}
}
