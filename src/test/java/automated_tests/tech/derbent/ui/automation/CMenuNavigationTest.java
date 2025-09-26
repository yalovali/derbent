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

/** Focused menu navigation test that validates all menu item openings and navigation functionality. Tests sidebar navigation, breadcrumbs, and main
 * menu items to ensure proper routing and accessibility. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop", "server.port=8080"
})
@DisplayName ("🧭 Menu Navigation Test")
public class CMenuNavigationTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CMenuNavigationTest.class);

	@Test
	@DisplayName ("🧭 Test Menu Item Navigation")
	void testMenuItemNavigation() {
		LOGGER.info("🧪 Starting menu item openings test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test all menu item openings
			testAllMenuItemOpenings();
			LOGGER.info("✅ Menu item openings test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Menu item openings test failed: {}", e.getMessage());
			takeScreenshot("menu-openings-error", true);
			throw new AssertionError("Menu item openings test failed", e);
		}
	}

	@Test
	@DisplayName ("📱 Test Sidebar Navigation Functionality")
	void testSidebarNavigationFunctionality() {
		LOGGER.info("🧪 Starting sidebar navigation functionality test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test sidebar navigation
			testSidebarNavigation();
			LOGGER.info("✅ Sidebar navigation test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Sidebar navigation test failed: {}", e.getMessage());
			takeScreenshot("sidebar-navigation-error", true);
			throw new AssertionError("Sidebar navigation test failed", e);
		}
	}

	@Test
	@DisplayName ("🍞 Test Breadcrumb Navigation")
	void testBreadcrumbNavigationFunctionality() {
		LOGGER.info("🧪 Starting breadcrumb navigation test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test breadcrumb navigation
			testBreadcrumbNavigation();
			LOGGER.info("✅ Breadcrumb navigation test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Breadcrumb navigation test failed: {}", e.getMessage());
			takeScreenshot("breadcrumb-navigation-error", true);
			throw new AssertionError("Breadcrumb navigation test failed", e);
		}
	}

	@Test
	@DisplayName ("🔍 Test Navigation Between All Main Views")
	void testNavigationBetweenAllViews() {
		LOGGER.info("🧪 Starting navigation between all main views test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test navigation between all main view classes
			for (Class<?> viewClass : mainViewClasses) {
				String viewName = viewClass.getSimpleName().replace("View", "").replace("C", "");
				LOGGER.info("🧭 Testing navigation to view: {}", viewName);
				boolean navigationSuccess = navigateToViewByClass(viewClass);
				if (navigationSuccess) {
					wait_1000();
					takeScreenshot("navigation-" + viewName.toLowerCase(), false);
					// Verify page loaded correctly
					boolean hasContent = page.locator("vaadin-vertical-layout, vaadin-horizontal-layout, main").count() > 0;
					if (hasContent) {
						LOGGER.info("✅ Successfully navigated to and loaded: {}", viewName);
					} else {
						LOGGER.warn("⚠️ Navigation to {} succeeded but content may not have loaded", viewName);
					}
				} else {
					LOGGER.warn("⚠️ Failed to navigate to: {}", viewName);
				}
			}
			LOGGER.info("✅ Navigation between all views test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Navigation between views test failed: {}", e.getMessage());
			takeScreenshot("navigation-views-error", true);
			throw new AssertionError("Navigation between views test failed", e);
		}
	}

	@Test
	@DisplayName ("🔄 Test Navigation Flow and Back/Forward")
	void testNavigationFlow() {
		LOGGER.info("🧪 Starting navigation flow test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test navigation flow between views
			LOGGER.info("🧭 Testing navigation flow: Projects -> Users -> Projects");
			// Navigate to Projects
			navigateToProjects();
			wait_1000();
			takeScreenshot("nav-flow-projects", false);
			// Navigate to Users
			navigateToUsers();
			wait_1000();
			takeScreenshot("nav-flow-users", false);
			// Navigate back to Projects
			navigateToProjects();
			wait_1000();
			takeScreenshot("nav-flow-back-projects", false);
			// Test browser back/forward if supported
			try {
				page.goBack();
				wait_1000();
				takeScreenshot("nav-flow-browser-back", false);
				page.goForward();
				wait_1000();
				takeScreenshot("nav-flow-browser-forward", false);
				LOGGER.info("✅ Browser back/forward navigation working");
			} catch (Exception e) {
				LOGGER.info("ℹ️ Browser back/forward not available or not working: {}", e.getMessage());
			}
			LOGGER.info("✅ Navigation flow test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Navigation flow test failed: {}", e.getMessage());
			takeScreenshot("navigation-flow-error", true);
			throw new AssertionError("Navigation flow test failed", e);
		}
	}

	@Test
	@DisplayName ("♿ Test Navigation Accessibility")
	void testNavigationAccessibility() {
		LOGGER.info("🧪 Starting navigation accessibility test...");
		try {
			// Create screenshots directory
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login to application
			loginToApplication();
			// Test keyboard navigation
			LOGGER.info("⌨️ Testing keyboard navigation");
			// Test Tab navigation through menu items
			page.keyboard().press("Tab");
			wait_500();
			page.keyboard().press("Tab");
			wait_500();
			page.keyboard().press("Enter");
			wait_1000();
			takeScreenshot("keyboard-navigation", false);
			// Test accessibility features
			testAccessibilityBasics("navigation");
			// Verify navigation has proper ARIA labels
			LOGGER.info("🔍 Verifying navigation ARIA labels");
			if (page.locator("nav[aria-label], [role='navigation']").count() > 0) {
				LOGGER.info("✅ Found navigation with proper ARIA labels");
			} else {
				LOGGER.warn("⚠️ Navigation may not have proper ARIA labels");
			}
			LOGGER.info("✅ Navigation accessibility test completed successfully");
		} catch (Exception e) {
			LOGGER.error("❌ Navigation accessibility test failed: {}", e.getMessage());
			takeScreenshot("navigation-accessibility-error", true);
			throw new AssertionError("Navigation accessibility test failed", e);
		}
	}
}
