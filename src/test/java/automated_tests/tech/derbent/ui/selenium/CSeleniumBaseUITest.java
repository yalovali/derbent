package automated_tests.tech.derbent.ui.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.File;
import java.time.Duration;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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
import org.springframework.test.context.ActiveProfiles;

/**
 * Base class for Selenium UI tests providing common functionality for testing Vaadin applications.
 * This class provides utilities for:
 * - Browser setup (headless and visible modes)
 * - Login and authentication
 * - Navigation between views
 * - CRUD operations testing
 * - Form field interactions
 * - Grid operations
 * - Screenshot capture
 * 
 * All Selenium tests should extend this class to inherit the common testing infrastructure.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
public abstract class CSeleniumBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSeleniumBaseUITest.class);

	static {
		System.setProperty("vaadin.devmode.liveReload.enabled", "false");
		System.setProperty("vaadin.launch-browser", "false");
		System.setProperty("vaadin.devmode.enabled", "false");
		System.setProperty("vaadin.devserver.enabled", "false");
		System.setProperty("spring.devtools.restart.enabled", "false");
		System.setProperty("spring.devtools.livereload.enabled", "false");
	}

	protected WebDriver driver;
	protected WebDriverWait wait;

	@LocalServerPort
	protected int port = 8081;

	protected String baseUrl;

	/** Setup Selenium WebDriver before each test */
	@BeforeEach
	void setupSeleniumEnvironment() {
		LOGGER.info("🧪 Setting up Selenium test environment...");
		try {
			// Determine headless mode
			boolean headless = Boolean.parseBoolean(System.getProperty("selenium.headless", "true"));
			LOGGER.info("🎭 Browser mode: {}", headless ? "HEADLESS" : "VISIBLE");

			// Setup ChromeDriver using WebDriverManager
			WebDriverManager.chromedriver().setup();

			// Configure Chrome options
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			options.addArguments("--disable-gpu");
			options.addArguments("--disable-extensions");
			options.addArguments("--remote-allow-origins=*");

			if (headless) {
				options.addArguments("--headless=new");
			} else {
				options.addArguments("--start-maximized");
			}

			// Create ChromeDriver instance
			driver = new ChromeDriver(options);
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
			driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));

			// Create WebDriverWait with 15 second timeout
			wait = new WebDriverWait(driver, Duration.ofSeconds(15));

			baseUrl = "http://localhost:" + port;

			LOGGER.info("✅ Selenium test environment setup complete");
		} catch (Exception e) {
			LOGGER.error("❌ Failed to setup Selenium environment: {}", e.getMessage(), e);
			throw new RuntimeException("Selenium setup failed", e);
		}
	}

	/** Teardown Selenium WebDriver after each test */
	@AfterEach
	void teardownSeleniumEnvironment() {
		LOGGER.info("🧹 Tearing down Selenium test environment...");
		if (driver != null) {
			try {
				driver.quit();
			} catch (Exception e) {
				LOGGER.warn("⚠️ Error during driver cleanup: {}", e.getMessage());
			}
		}
		LOGGER.info("✅ Selenium test environment teardown complete");
	}

	// ===========================================
	// NAVIGATION METHODS
	// ===========================================

	/** Navigate to the login page */
	protected void navigateToLogin() {
		LOGGER.info("🔐 Navigating to login page");
		driver.get(baseUrl + "/login");
		waitForPageLoad();
	}

	/** Navigate to a specific URL path */
	protected void navigateTo(String path) {
		LOGGER.info("🧭 Navigating to: {}", path);
		driver.get(baseUrl + path);
		waitForPageLoad();
	}

	/** Wait for page to fully load */
	protected void waitForPageLoad() {
		wait_500();
		wait.until(webDriver -> ((JavascriptExecutor) webDriver)
				.executeScript("return document.readyState").equals("complete"));
		wait_500();
	}

	// ===========================================
	// LOGIN AND AUTHENTICATION METHODS
	// ===========================================

	/** Perform complete login workflow with default admin credentials */
	protected void loginToApplication() {
		loginToApplication("admin", "test123");
	}

	/** Perform login with specified credentials */
	protected void loginToApplication(String username, String password) {
		LOGGER.info("🔐 Attempting login with username: {}", username);
		try {
			navigateToLogin();
			initializeSampleDataFromLoginPage();
			navigateToLogin();
			ensureCompanySelected();

			// Fill username
			WebElement usernameInput = findLoginField("custom-username-input");
			if (usernameInput != null) {
				usernameInput.clear();
				usernameInput.sendKeys(username);
				LOGGER.info("📝 Username entered");
			} else {
				throw new AssertionError("Username field not found");
			}

			// Fill password
			WebElement passwordInput = findLoginField("custom-password-input");
			if (passwordInput != null) {
				passwordInput.clear();
				passwordInput.sendKeys(password);
				LOGGER.info("📝 Password entered");
			} else {
				throw new AssertionError("Password field not found");
			}

			// Click login button
			clickLoginButton();
			waitForLoginComplete();

			LOGGER.info("✅ Login successful");
			takeScreenshot("post-login");
		} catch (Exception e) {
			LOGGER.error("❌ Login failed: {}", e.getMessage(), e);
			takeScreenshot("login-error");
			throw new AssertionError("Login failed", e);
		}
	}

	/** Find login field by ID, handling shadow DOM if necessary */
	protected WebElement findLoginField(String fieldId) {
		try {
			// Try direct input element first
			List<WebElement> inputs = driver.findElements(By.cssSelector("input"));
			for (WebElement input : inputs) {
				WebElement parent = input.findElement(By.xpath(".."));
				if (parent.getAttribute("id") != null && parent.getAttribute("id").contains(fieldId)) {
					return input;
				}
			}

			// Try by ID on shadow root container
			WebElement container = driver.findElement(By.id(fieldId));
			if (container != null) {
				// Try to find input within container
				try {
					return container.findElement(By.tagName("input"));
				} catch (Exception ignored) {
					return container;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Could not find field with ID: {}", fieldId);
		}
		return null;
	}

	/** Click the login button */
	protected void clickLoginButton() {
		try {
			// Try multiple selectors for login button
			String[] selectors = {
					"vaadin-button[theme~='primary']",
					"button:contains('Login')",
					"vaadin-button"
			};

			WebElement loginButton = null;
			for (String selector : selectors) {
				try {
					List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
					if (!buttons.isEmpty()) {
						for (WebElement button : buttons) {
							if (button.getText().contains("Login") || button.getText().contains("Giriş")) {
								loginButton = button;
								break;
							}
						}
						if (loginButton != null) break;
					}
				} catch (Exception ignored) {
				}
			}

			if (loginButton != null) {
				loginButton.click();
				wait_500();
				LOGGER.info("▶️ Login button clicked");
			} else {
				throw new AssertionError("Login button not found");
			}
		} catch (Exception e) {
			throw new AssertionError("Failed to click login button", e);
		}
	}

	/** Initialize sample data from login page */
	protected void initializeSampleDataFromLoginPage() {
		try {
			LOGGER.info("📥 Loading sample data via login screen button");
			wait_1000();

			// Look for DB Min or DB Full button
			List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
			for (WebElement button : buttons) {
				String buttonText = button.getText();
				if (buttonText.contains("DB Min") || buttonText.contains("DB Full") || buttonText.contains("Reset")) {
					LOGGER.info("🔘 Found data initialization button: {}", buttonText);
					button.click();
					wait_1000();

					// Accept confirmation dialog if present
					acceptConfirmDialogIfPresent();
					closeInformationDialogIfPresent();
					wait_1000();

					LOGGER.info("✅ Sample data initialization completed");
					return;
				}
			}
			LOGGER.info("ℹ️ No data initialization button found, assuming data is already loaded");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Sample data initialization failed: {}", e.getMessage());
		}
	}

	/** Accept confirmation dialog if present */
	protected void acceptConfirmDialogIfPresent() {
		try {
			List<WebElement> confirmButtons = driver.findElements(
					By.cssSelector("vaadin-confirm-dialog-overlay[opened] vaadin-button"));
			for (WebElement button : confirmButtons) {
				if (button.getText().contains("Evet") || button.getText().contains("Yes")) {
					button.click();
					wait_1000();
					LOGGER.info("✅ Confirmation dialog accepted");
					return;
				}
			}
		} catch (Exception e) {
			LOGGER.debug("No confirmation dialog to accept");
		}
	}

	/** Close information dialog if present */
	protected void closeInformationDialogIfPresent() {
		try {
			List<WebElement> okButtons = driver.findElements(
					By.cssSelector("vaadin-dialog-overlay[opened] vaadin-button"));
			for (WebElement button : okButtons) {
				if (button.getText().contains("OK") || button.getText().contains("Tamam")) {
					button.click();
					wait_1000();
					LOGGER.info("✅ Information dialog closed");
					return;
				}
			}
		} catch (Exception e) {
			LOGGER.debug("No information dialog to close");
		}
	}

	/** Ensure company is selected in login form */
	protected void ensureCompanySelected() {
		try {
			WebElement companyCombo = driver.findElement(By.id("custom-company-input"));
			if (companyCombo != null) {
				// Check if value is already set
				String value = (String) ((JavascriptExecutor) driver)
						.executeScript("return arguments[0].value", companyCombo);
				if (value == null || value.isEmpty()) {
					LOGGER.info("🏢 Selecting default company");
					companyCombo.click();
					wait_500();

					List<WebElement> items = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
					if (!items.isEmpty()) {
						items.get(0).click();
						wait_500();
						LOGGER.info("✅ Company selected");
					}
				}
			}
		} catch (Exception e) {
			LOGGER.debug("Company selection not required or failed: {}", e.getMessage());
		}
	}

	/** Wait for login to complete and application shell to load */
	protected void waitForLoginComplete() {
		try {
			wait.until(ExpectedConditions.or(
					ExpectedConditions.presenceOfElementLocated(By.cssSelector("vaadin-app-layout")),
					ExpectedConditions.presenceOfElementLocated(By.cssSelector("vaadin-side-nav")),
					ExpectedConditions.presenceOfElementLocated(By.cssSelector("vaadin-drawer-layout"))));
			wait_1000();
			LOGGER.info("✅ Login complete - application shell detected");
		} catch (Exception e) {
			LOGGER.warn("⚠️ Post-login application shell not detected: {}", e.getMessage());
		}
	}

	// ===========================================
	// BUTTON ACTION METHODS
	// ===========================================

	/** Click the "New" button to create a new entity */
	protected void clickNew() {
		LOGGER.info("➕ Clicking New button");
		try {
			WebElement newButton = wait.until(ExpectedConditions.elementToBeClickable(
					By.cssSelector("vaadin-button[text='New'], vaadin-button:contains('New')")));
			if (newButton == null) {
				// Fallback: find button by text
				List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
				for (WebElement button : buttons) {
					if (button.getText().contains("New")) {
						newButton = button;
						break;
					}
				}
			}
			if (newButton != null) {
				newButton.click();
				wait_500();
			} else {
				throw new AssertionError("New button not found");
			}
		} catch (Exception e) {
			throw new AssertionError("Failed to click New button", e);
		}
	}

	/** Click the "Save" button */
	protected void clickSave() {
		LOGGER.info("💾 Clicking Save button");
		try {
			List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
			for (WebElement button : buttons) {
				if (button.getText().contains("Save") || button.getText().contains("Kaydet")) {
					button.click();
					wait_1000();
					return;
				}
			}
			throw new AssertionError("Save button not found");
		} catch (Exception e) {
			throw new AssertionError("Failed to click Save button", e);
		}
	}

	/** Click the "Edit" button */
	protected void clickEdit() {
		LOGGER.info("✏️ Clicking Edit button");
		try {
			List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
			for (WebElement button : buttons) {
				if (button.getText().contains("Edit") || button.getText().contains("Düzenle")) {
					button.click();
					wait_500();
					return;
				}
			}
			throw new AssertionError("Edit button not found");
		} catch (Exception e) {
			throw new AssertionError("Failed to click Edit button", e);
		}
	}

	/** Click the "Delete" button */
	protected void clickDelete() {
		LOGGER.info("🗑️ Clicking Delete button");
		try {
			List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
			for (WebElement button : buttons) {
				if (button.getText().contains("Delete") || button.getText().contains("Sil")) {
					button.click();
					wait_500();
					return;
				}
			}
			throw new AssertionError("Delete button not found");
		} catch (Exception e) {
			throw new AssertionError("Failed to click Delete button", e);
		}
	}

	/** Click the "Cancel" button */
	protected void clickCancel() {
		LOGGER.info("❌ Clicking Cancel button");
		try {
			List<WebElement> buttons = driver.findElements(By.cssSelector("vaadin-button"));
			for (WebElement button : buttons) {
				if (button.getText().contains("Cancel") || button.getText().contains("İptal")) {
					button.click();
					wait_500();
					return;
				}
			}
			throw new AssertionError("Cancel button not found");
		} catch (Exception e) {
			throw new AssertionError("Failed to click Cancel button", e);
		}
	}

	// ===========================================
	// FORM FIELD METHODS
	// ===========================================

	/** Fill the first text field with the specified value */
	protected void fillFirstTextField(String value) {
		LOGGER.info("📝 Filling first text field with: {}", value);
		try {
			List<WebElement> textFields = driver.findElements(By.cssSelector("vaadin-text-field"));
			if (!textFields.isEmpty()) {
				WebElement textField = textFields.get(0);
				WebElement input = textField.findElement(By.tagName("input"));
				input.clear();
				input.sendKeys(value);
				wait_500();
			} else {
				throw new AssertionError("No text field found");
			}
		} catch (Exception e) {
			throw new AssertionError("Failed to fill first text field", e);
		}
	}

	/** Fill the first text area with the specified value */
	protected void fillFirstTextArea(String value) {
		LOGGER.info("📝 Filling first text area with: {}", value);
		try {
			List<WebElement> textAreas = driver.findElements(By.cssSelector("vaadin-text-area"));
			if (!textAreas.isEmpty()) {
				WebElement textArea = textAreas.get(0);
				WebElement textarea = textArea.findElement(By.tagName("textarea"));
				textarea.clear();
				textarea.sendKeys(value);
				wait_500();
			} else {
				LOGGER.warn("⚠️ No text area found");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to fill first text area: {}", e.getMessage());
		}
	}

	/** Select the first option in the first ComboBox */
	protected void selectFirstComboBoxOption() {
		LOGGER.info("📋 Selecting first option in ComboBox");
		try {
			List<WebElement> comboBoxes = driver.findElements(By.cssSelector("vaadin-combo-box"));
			if (!comboBoxes.isEmpty()) {
				WebElement comboBox = comboBoxes.get(0);
				comboBox.click();
				wait_500();

				List<WebElement> items = driver.findElements(By.cssSelector("vaadin-combo-box-item"));
				if (!items.isEmpty()) {
					items.get(0).click();
					wait_500();
					LOGGER.info("✅ Selected first ComboBox option");
				} else {
					LOGGER.warn("⚠️ No ComboBox options found");
				}
			} else {
				LOGGER.warn("⚠️ No ComboBox found");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to select ComboBox option: {}", e.getMessage());
		}
	}

	// ===========================================
	// GRID INTERACTION METHODS
	// ===========================================

	/** Click the first row in the grid */
	protected void clickFirstGridRow() {
		LOGGER.info("📊 Clicking first grid row");
		try {
			List<WebElement> grids = driver.findElements(By.cssSelector("vaadin-grid"));
			if (!grids.isEmpty()) {
				WebElement grid = grids.get(0);
				List<WebElement> cells = grid.findElements(By.cssSelector("vaadin-grid-cell-content"));
				if (!cells.isEmpty()) {
					cells.get(0).click();
					wait_500();
					LOGGER.info("✅ Clicked first grid row");
				} else {
					LOGGER.warn("⚠️ No grid rows found");
				}
			} else {
				LOGGER.warn("⚠️ No grid found");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to click first grid row: {}", e.getMessage());
		}
	}

	/** Verify that the grid has data */
	protected boolean verifyGridHasData() {
		try {
			List<WebElement> grids = driver.findElements(By.cssSelector("vaadin-grid"));
			if (!grids.isEmpty()) {
				WebElement grid = grids.get(0);
				List<WebElement> cells = grid.findElements(By.cssSelector("vaadin-grid-cell-content"));
				boolean hasData = !cells.isEmpty();
				LOGGER.info("📊 Grid has data: {}", hasData);
				return hasData;
			}
			LOGGER.warn("⚠️ No grid found");
			return false;
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to verify grid data: {}", e.getMessage());
			return false;
		}
	}

	/** Get the count of rows in the grid */
	protected int getGridRowCount() {
		try {
			List<WebElement> grids = driver.findElements(By.cssSelector("vaadin-grid"));
			if (!grids.isEmpty()) {
				WebElement grid = grids.get(0);
				List<WebElement> cells = grid.findElements(By.cssSelector("vaadin-grid-cell-content"));
				return cells.size();
			}
			return 0;
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to get grid row count: {}", e.getMessage());
			return 0;
		}
	}

	// ===========================================
	// CRUD WORKFLOW METHODS
	// ===========================================

	/** Perform complete CRUD workflow for the current view */
	protected void performCRUDWorkflow(String entityName) {
		LOGGER.info("🔄 Performing CRUD workflow for: {}", entityName);

		// CREATE
		clickNew();
		fillFirstTextField("Test " + entityName);
		selectFirstComboBoxOption();
		clickSave();
		takeScreenshot("crud-create-" + entityName.toLowerCase());

		// READ - verify in grid
		wait_1000();
		boolean hasData = verifyGridHasData();
		if (hasData) {
			LOGGER.info("✅ CREATE operation successful for: {}", entityName);
		}

		// UPDATE
		clickFirstGridRow();
		clickEdit();
		fillFirstTextField("Updated " + entityName);
		clickSave();
		takeScreenshot("crud-update-" + entityName.toLowerCase());

		// DELETE
		wait_1000();
		clickFirstGridRow();
		clickDelete();
		takeScreenshot("crud-delete-" + entityName.toLowerCase());

		LOGGER.info("✅ CRUD workflow complete for: {}", entityName);
	}

	// ===========================================
	// SCREENSHOT METHODS
	// ===========================================

	/** Take a screenshot with the specified name */
	protected void takeScreenshot(String name) {
		try {
			LOGGER.info("📸 Taking screenshot: {}", name);
			File screenshotDir = new File("target/screenshots");
			if (!screenshotDir.exists()) {
				screenshotDir.mkdirs();
			}

			File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
			File destination = new File("target/screenshots/" + name + ".png");
			FileUtils.copyFile(screenshot, destination);

			LOGGER.info("✅ Screenshot saved: {}", destination.getPath());
		} catch (Exception e) {
			LOGGER.error("❌ Failed to take screenshot '{}': {}", name, e.getMessage());
		}
	}

	// ===========================================
	// WAIT METHODS
	// ===========================================

	/** Wait for 500 milliseconds */
	protected void wait_500() {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Wait for 1000 milliseconds */
	protected void wait_1000() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/** Wait for 2000 milliseconds */
	protected void wait_2000() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	// ===========================================
	// NAVIGATION BY TEXT METHODS
	// ===========================================

	/** Navigate to a view by clicking on menu item with specified text */
	protected boolean navigateToViewByText(String viewText) {
		LOGGER.info("🧭 Navigating to view: {}", viewText);
		try {
			List<WebElement> navItems = driver.findElements(By.cssSelector("vaadin-side-nav-item"));
			for (WebElement navItem : navItems) {
				if (navItem.getText().contains(viewText)) {
					navItem.click();
					wait_1000();
					LOGGER.info("✅ Successfully navigated to: {}", viewText);
					return true;
				}
			}
			LOGGER.warn("⚠️ Navigation item not found for: {}", viewText);
			return false;
		} catch (Exception e) {
			LOGGER.error("❌ Navigation failed for view: {} - Error: {}", viewText, e.getMessage());
			return false;
		}
	}

	/** Check if browser is available */
	protected boolean isBrowserAvailable() {
		return driver != null;
	}
}
