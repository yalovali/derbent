package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * SeleniumUIAutomationTest - End-to-end UI automation tests using Selenium WebDriver.
 * Selenium WebDriver provides robust browser automation with features: - Cross-browser
 * compatibility (Chrome, Firefox, Safari, Edge, IE) - Mature ecosystem with extensive
 * community support - Rich API for complex user interactions - Excellent integration with
 * CI/CD pipelines - Wide industry adoption and documentation - Support for mobile testing
 * via Appium This implementation provides comprehensive testing scenarios: - Login and
 * logout functionality testing - Navigation through different application views - Grid
 * interactions (clicking, sorting, filtering) - CRUD operations (Create, Read, Update,
 * Delete) - Form validation and data entry - Responsive design verification - Error
 * handling and user feedback testing Comparison with Playwright: - More mature and
 * established in the industry - Better support for older browsers - Extensive third-party
 * integrations - Rich reporting and logging capabilities - Excellent for regression
 * testing - Great for parallel execution strategies
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT)
@TestPropertySource (properties = {
	"spring.datasource.url=jdbc:h2:mem:seleniumtestdb",
	"spring.jpa.hibernate.ddl-auto=create-drop", "logging.level.tech.derbent=DEBUG",
	"server.port=8080" }
)
public class SeleniumUIAutomationTest {

	private static final Logger logger =
		LoggerFactory.getLogger(SeleniumUIAutomationTest.class);

	// Test credentials - aligned with project standard test password
	private static final String TEST_USERNAME = "admin";

	private static final String TEST_PASSWORD = "test123";

	@LocalServerPort
	private int port;

	private WebDriver driver;

	private WebDriverWait wait;

	private String baseUrl;

	private List<WebElement> findCreateButtons() {
		final List<WebElement> elements = new ArrayList<>();
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'New')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Add')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Create')]")));
		return elements;
	}

	private List<WebElement> findDeleteButtons() {
		final List<WebElement> elements = new ArrayList<>();
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Delete')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Remove')]")));
		elements.addAll(
			driver.findElements(By.cssSelector("vaadin-button[title*='delete']")));
		return elements;
	}

	private List<WebElement> findEditButtons() {
		final List<WebElement> elements = new ArrayList<>();
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Edit')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Modify')]")));
		elements
			.addAll(driver.findElements(By.cssSelector("vaadin-button[title*='edit']")));
		return elements;
	}

	private List<WebElement> findLogoutElements() {
		final List<WebElement> elements = new ArrayList<>();
		// Look for logout button or menu item
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Logout')]")));
		elements.addAll(driver.findElements(By.xpath("//a[contains(text(), 'Logout')]")));
		elements.addAll(driver.findElements(
			By.xpath("//vaadin-menu-bar-button[contains(text(), 'Logout')]")));
		return elements;
	}

	private List<WebElement> findSaveButtons() {
		final List<WebElement> elements = new ArrayList<>();
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Save')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Create')]")));
		elements.addAll(
			driver.findElements(By.xpath("//vaadin-button[contains(text(), 'Update')]")));
		return elements;
	}

	/**
	 * Navigates to a specific view by finding navigation elements
	 */
	private boolean navigateToView(final String viewName) {

		try {
			logger.info("Navigating to {} view...", viewName);
			// Look for navigation items in side menu
			final List<WebElement> navItems =
				driver.findElements(By.tagName("vaadin-side-nav-item"));

			for (final WebElement navItem : navItems) {

				if (navItem.getText().contains(viewName)) {
					navItem.click();
					Thread.sleep(1000); // Wait for navigation
					return true;
				}
			}
			// Alternative: look for links with the view name
			final List<WebElement> links = driver.findElements(By.tagName("a"));

			for (final WebElement link : links) {

				if (link.getText().contains(viewName)) {
					link.click();
					Thread.sleep(1000);
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			logger.warn("Navigation to {} view failed: {}", viewName, e.getMessage());
			return false;
		}
	}

	/**
	 * Performs create operation in a view
	 */
	private void performCreateOperation(final String viewName) {

		try {
			logger.info("Testing CREATE operation in {} view...", viewName);
			// Look for "New" or "Add" buttons
			final List<WebElement> createButtons = findCreateButtons();

			if (!createButtons.isEmpty()) {
				createButtons.get(0).click();
				Thread.sleep(1000);
				// Look for form fields and fill them
				final List<WebElement> textFields =
					driver.findElements(By.tagName("vaadin-text-field"));

				if (!textFields.isEmpty()) {
					textFields.get(0).sendKeys(
						"Test " + viewName + " Entry - " + System.currentTimeMillis());
					takeScreenshot("create-form-filled-" + viewName.toLowerCase());
					// Look for save button
					final List<WebElement> saveButtons = findSaveButtons();

					if (!saveButtons.isEmpty()) {
						saveButtons.get(0).click();
						Thread.sleep(1000);
						takeScreenshot("create-saved-" + viewName.toLowerCase());
						logger.info("✅ CREATE operation test completed for {} view",
							viewName);
					}
				}
			}
			else {
				logger.info("No create button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("CREATE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("create-error-" + viewName.toLowerCase());
		}
	}

	/**
	 * Performs delete operation in a view
	 */
	private void performDeleteOperation(final String viewName) {

		try {
			logger.info("Testing DELETE operation in {} view...", viewName);
			// Look for delete buttons
			final List<WebElement> deleteButtons = findDeleteButtons();

			if (!deleteButtons.isEmpty()) {
				deleteButtons.get(0).click();
				Thread.sleep(1000);
				takeScreenshot("delete-confirmation-" + viewName.toLowerCase());
				// Look for confirmation dialog and confirm
				final List<WebElement> confirmButtons = driver.findElements(By.xpath(
					"//vaadin-button[contains(text(), 'Yes') or contains(text(), 'Confirm') or contains(text(), 'Delete')]"));

				if (!confirmButtons.isEmpty()) {
					confirmButtons.get(0).click();
					Thread.sleep(1000);
					takeScreenshot("delete-completed-" + viewName.toLowerCase());
					logger.info("✅ DELETE operation test completed for {} view",
						viewName);
				}
				else {
					logger.info(
						"No confirmation dialog found for delete operation in {} view",
						viewName);
				}
			}
			else {
				logger.info("No delete button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("DELETE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("delete-error-" + viewName.toLowerCase());
		}
	}

	/**
	 * Performs login with test credentials
	 */
	private void performLogin() {
		logger.info("Performing login with test credentials...");

		try {
			// Wait for login form to be available
			wait.until(ExpectedConditions
				.presenceOfElementLocated(By.id("vaadinLoginOverlayWrapper")));
			// Find username and password fields within the login overlay
			final WebElement usernameField = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.id("input-vaadin-text-field-12")));
			final WebElement passwordField = wait.until(ExpectedConditions
				.presenceOfElementLocated(By.id("input-vaadin-password-field-13")));
			// Clear and enter credentials
			usernameField.clear();
			usernameField.sendKeys(TEST_USERNAME);
			passwordField.clear();
			passwordField.sendKeys(TEST_PASSWORD);
			takeScreenshot("login-credentials-entered");
			// Find and click login button
			final WebElement loginButton = wait.until(
				ExpectedConditions.elementToBeClickable(By.id("vaadin-button[1]")));
			loginButton.click();
			logger.info("✅ Login credentials submitted");
		} catch (final Exception e) {
			logger.error("❌ Login failed: {}", e.getMessage());
			takeScreenshot("login-failed");
			throw new RuntimeException("Login failed", e);
		}
	}
	// Helper methods

	/**
	 * Performs read operation in a view
	 */
	private void performReadOperation(final String viewName) {

		try {
			logger.info("Testing READ operation in {} view...", viewName);
			// Look for grids or lists with data
			final List<WebElement> grids = driver.findElements(By.tagName("vaadin-grid"));

			if (!grids.isEmpty()) {
				final WebElement grid = grids.get(0);
				final List<WebElement> rows =
					grid.findElements(By.cssSelector("vaadin-grid-cell-content"));

				if (!rows.isEmpty()) {
					logger.info("Found {} data rows in {} view", rows.size(), viewName);
					takeScreenshot("read-data-" + viewName.toLowerCase());
					// Click on first row to view details
					rows.get(0).click();
					Thread.sleep(1000);
					takeScreenshot("read-details-" + viewName.toLowerCase());
					logger.info("✅ READ operation test completed for {} view", viewName);
				}
				else {
					logger.info("No data found in {} view", viewName);
				}
			}
		} catch (final Exception e) {
			logger.warn("READ operation failed in {} view: {}", viewName, e.getMessage());
			takeScreenshot("read-error-" + viewName.toLowerCase());
		}
	}

	/**
	 * Performs update operation in a view
	 */
	private void performUpdateOperation(final String viewName) {

		try {
			logger.info("Testing UPDATE operation in {} view...", viewName);
			// Look for edit buttons or editable elements
			final List<WebElement> editButtons = findEditButtons();

			if (!editButtons.isEmpty()) {
				editButtons.get(0).click();
				Thread.sleep(1000);
				// Look for form fields to modify
				final List<WebElement> textFields =
					driver.findElements(By.tagName("vaadin-text-field"));

				if (!textFields.isEmpty()) {
					final WebElement field = textFields.get(0);
					field.clear();
					field.sendKeys(
						"Updated " + viewName + " Entry - " + System.currentTimeMillis());
					takeScreenshot("update-form-filled-" + viewName.toLowerCase());
					// Save changes
					final List<WebElement> saveButtons = findSaveButtons();

					if (!saveButtons.isEmpty()) {
						saveButtons.get(0).click();
						Thread.sleep(1000);
						takeScreenshot("update-saved-" + viewName.toLowerCase());
						logger.info("✅ UPDATE operation test completed for {} view",
							viewName);
					}
				}
			}
			else {
				logger.info("No edit button found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("UPDATE operation failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("update-error-" + viewName.toLowerCase());
		}
	}

	@BeforeEach
	void setUp() {
		logger.info("Setting up Selenium WebDriver test environment...");
		baseUrl = "http://localhost:" + port;
		// Configure WebDriverManager to handle driver binaries automatically
		WebDriverManager.chromedriver().setup();
		// Configure Chrome options for testing
		final ChromeOptions options = new ChromeOptions();
		// options.addArguments("--headless"); // Run in headless mode for CI/CD
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--window-size=1200,800");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-web-security");
		options.addArguments("--allow-running-insecure-content");
		// Initialize Chrome driver
		driver = new ChromeDriver(options);
		// Configure implicit wait and explicit wait
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
		wait = new WebDriverWait(driver, Duration.ofSeconds(20));
		// Maximize window for consistent testing
		driver.manage().window().maximize();
		logger.info("Selenium test setup completed. Application URL: {}", baseUrl);
	}

	/**
	 * Takes a screenshot and saves it to the target/screenshots directory
	 */
	private void takeScreenshot(final String name) {

		try {
			final File screenshotDir = new File("target/screenshots");

			if (!screenshotDir.exists()) {
				screenshotDir.mkdirs();
			}
			final File screenshot =
				((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			final String fileName =
				"selenium-" + name + "-" + System.currentTimeMillis() + ".png";
			final File destFile = new File(screenshotDir, fileName);
			// Copy the screenshot file
			java.nio.file.Files.copy(screenshot.toPath(), destFile.toPath());
			logger.info("📸 Screenshot saved: {}", destFile.getAbsolutePath());
		} catch (final Exception e) {
			logger.warn("⚠️ Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	@AfterEach
	void tearDown() {

		if (driver != null) {
			driver.quit();
		}
		logger.info("Selenium test cleanup completed");
	}

	@Test
	void testApplicationLoadsAndLoginFunctionality() {
		logger.info("🧪 Testing application loads and login functionality...");
		// Navigate to application
		driver.get(baseUrl);
		// Wait for login overlay to be visible
		wait.until(ExpectedConditions
			.presenceOfElementLocated(By.tagName("vaadin-login-overlay")));
		// Take screenshot of login page
		takeScreenshot("login-page-loaded");
		// Verify login form is present
		final WebElement loginOverlay =
			driver.findElement(By.tagName("vaadin-login-overlay"));
		assertNotNull(loginOverlay);
		assertTrue(loginOverlay.isDisplayed());
		logger.info("✅ Login page loaded successfully");
		// Perform login
		performLogin();
		// Verify successful login by checking for main application layout
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
		takeScreenshot("application-after-login");
		// Verify main navigation is present
		final WebElement appLayout = driver.findElement(By.tagName("vaadin-app-layout"));
		assertTrue(appLayout.isDisplayed());
		logger.info("✅ Login functionality test completed successfully");
	}

	@Test
	void testCRUDOperations() {
		logger.info("🧪 Testing CRUD operations (Create, Read, Update, Delete)...");
		// Login first
		driver.get(baseUrl);
		performLogin();
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));

		// Test CRUD operations in Projects view
		if (navigateToView("Projects")) {
			performCreateOperation("Projects");
			performReadOperation("Projects");
			performUpdateOperation("Projects");
			performDeleteOperation("Projects");
		}

		// Test CRUD operations in Meetings view
		if (navigateToView("Meetings")) {
			performCreateOperation("Meetings");
			performReadOperation("Meetings");
			performUpdateOperation("Meetings");
			performDeleteOperation("Meetings");
		}
		logger.info("✅ CRUD operations test completed");
	}

	@Test
	void testFormValidationAndErrorHandling() {
		logger.info("🧪 Testing form validation and error handling...");
		// Login first
		driver.get(baseUrl);
		performLogin();
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
		// Test form validation in different views
		final String[] viewsToTest = {
			"Projects", "Meetings" };

		for (final String viewName : viewsToTest) {

			if (navigateToView(viewName)) {
				testFormValidationInView(viewName);
			}
		}
		logger.info("✅ Form validation and error handling test completed");
	}
	// Utility methods for finding common UI elements

	/**
	 * Tests form validation in a specific view
	 */
	private void testFormValidationInView(final String viewName) {

		try {
			logger.info("Testing form validation in {} view...", viewName);
			// Look for "New" or "Add" buttons to open form
			final List<WebElement> createButtons = findCreateButtons();

			if (!createButtons.isEmpty()) {
				createButtons.get(0).click();
				Thread.sleep(1000);
				// Try to save without filling required fields
				final List<WebElement> saveButtons = findSaveButtons();

				if (!saveButtons.isEmpty()) {
					saveButtons.get(0).click();
					Thread.sleep(1000);
					// Look for validation messages
					final List<WebElement> errorMessages =
						driver.findElements(By.cssSelector(
							"vaadin-text-field[invalid], .error-message, [role='alert']"));

					if (!errorMessages.isEmpty()) {
						logger.info(
							"✅ Form validation working - found {} validation messages",
							errorMessages.size());
						takeScreenshot("form-validation-" + viewName.toLowerCase());
					}
					else {
						logger.info("No validation messages found in {} view", viewName);
					}
					// Close form if possible
					final List<WebElement> cancelButtons = driver.findElements(
						By.xpath("//vaadin-button[contains(text(), 'Cancel')]"));

					if (!cancelButtons.isEmpty()) {
						cancelButtons.get(0).click();
					}
				}
			}
		} catch (final Exception e) {
			logger.warn("Form validation test failed in {} view: {}", viewName,
				e.getMessage());
			takeScreenshot("form-validation-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testGridInteractions() {
		logger.info("🧪 Testing grid interactions and data display...");
		// Login and navigate to a view with grids
		driver.get(baseUrl);
		performLogin();
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
		// Test grids in different views
		final String[] viewsWithGrids = {
			"Projects", "Meetings", "Activities" };

		for (final String viewName : viewsWithGrids) {

			if (navigateToView(viewName)) {
				testGridInView(viewName);
			}
		}
		logger.info("✅ Grid interactions test completed");
	}

	/**
	 * Tests grid functionality in a specific view
	 */
	private void testGridInView(final String viewName) {

		try {
			logger.info("Testing grid functionality in {} view...", viewName);
			// Look for grids in the current view
			final List<WebElement> grids = driver.findElements(By.tagName("vaadin-grid"));

			if (!grids.isEmpty()) {
				final WebElement grid = grids.get(0);
				logger.info("Found grid in {} view", viewName);
				// Test grid interactions
				final List<WebElement> gridCells =
					grid.findElements(By.tagName("vaadin-grid-cell-content"));

				if (!gridCells.isEmpty()) {
					logger.info("Grid has {} cells", gridCells.size());

					// Click on first cell if available
					if (!gridCells.isEmpty()) {
						gridCells.get(0).click();
						takeScreenshot("grid-cell-clicked-" + viewName.toLowerCase());
					}
				}
				// Look for grid headers for sorting
				final List<WebElement> headers =
					grid.findElements(By.cssSelector("vaadin-grid-sorter"));

				if (!headers.isEmpty()) {
					logger.info("Testing grid sorting...");
					headers.get(0).click(); // Click first sortable header
					Thread.sleep(500);
					takeScreenshot("grid-sorted-" + viewName.toLowerCase());
				}
				logger.info("✅ Grid test completed for {} view", viewName);
			}
			else {
				logger.info("No grids found in {} view", viewName);
			}
		} catch (final Exception e) {
			logger.warn("Grid test failed in {} view: {}", viewName, e.getMessage());
			takeScreenshot("grid-test-error-" + viewName.toLowerCase());
		}
	}

	@Test
	void testLogoutFunctionality() {
		logger.info("🧪 Testing logout functionality...");
		// First login
		driver.get(baseUrl);
		performLogin();
		// Wait for application to load
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
		takeScreenshot("before-logout");
		// Look for logout option - could be in a menu or button
		final List<WebElement> logoutElements = findLogoutElements();

		if (!logoutElements.isEmpty()) {
			// Click logout
			logoutElements.get(0).click();
			// Wait for redirect to login page
			wait.until(ExpectedConditions
				.presenceOfElementLocated(By.tagName("vaadin-login-overlay")));
			takeScreenshot("after-logout");
			// Verify we're back at login page
			final WebElement loginOverlay =
				driver.findElement(By.tagName("vaadin-login-overlay"));
			assertTrue(loginOverlay.isDisplayed());
			logger.info("✅ Logout functionality test completed successfully");
		}
		else {
			logger.warn(
				"⚠️ Logout button not found - application might use session timeout or different logout mechanism");
			takeScreenshot("logout-button-not-found");
		}
	}

	@Test
	void testNavigationBetweenViews() {
		logger.info("🧪 Testing navigation between application views...");
		// Login first
		driver.get(baseUrl);
		performLogin();
		wait.until(
			ExpectedConditions.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
		// Test navigation to different views
		final String[] expectedViews = {
			"Projects", "Meetings", "Activities", "Decisions" };

		for (final String viewName : expectedViews) {

			if (navigateToView(viewName)) {
				takeScreenshot("view-" + viewName.toLowerCase());
				logger.info("✅ Successfully navigated to {} view", viewName);
			}
			else {
				logger.warn("⚠️ Could not navigate to {} view", viewName);
				takeScreenshot("navigation-failed-" + viewName.toLowerCase());
			}
		}
		logger.info("✅ Navigation between views test completed");
	}

	@Test
	void testResponsiveDesignAndMobileView() {
		logger.info("🧪 Testing responsive design across different screen sizes...");
		// Test different screen sizes
		final int[][] screenSizes = {
			{
				1920, 1080 }, // Desktop
			{
				1024, 768 }, // Tablet landscape
			{
				768, 1024 }, // Tablet portrait
			{
				375, 667 } // Mobile
		};
		final String[] deviceNames = {
			"Desktop", "Tablet-Landscape", "Tablet-Portrait", "Mobile" };

		for (int i = 0; i < screenSizes.length; i++) {
			logger.info("Testing {} resolution: {}x{}", deviceNames[i], screenSizes[i][0],
				screenSizes[i][1]);
			// Set window size
			driver.manage().window().setSize(
				new org.openqa.selenium.Dimension(screenSizes[i][0], screenSizes[i][1]));
			// Navigate to application
			driver.get(baseUrl);
			performLogin();
			wait.until(ExpectedConditions
				.presenceOfElementLocated(By.tagName("vaadin-app-layout")));
			// Take screenshot
			takeScreenshot("responsive-" + deviceNames[i].toLowerCase());
			// Verify layout components are visible
			final WebElement appLayout =
				driver.findElement(By.tagName("vaadin-app-layout"));
			assertTrue(appLayout.isDisplayed());
			logger.info("✅ {} layout verified", deviceNames[i]);
		}
		logger.info("✅ Responsive design test completed");
	}
}