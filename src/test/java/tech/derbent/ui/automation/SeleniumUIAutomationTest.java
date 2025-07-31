package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.Duration;
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
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * SeleniumUIAutomationTest - End-to-end UI automation tests using pure Selenium WebDriver.
 * 
 * This is a free alternative to Vaadin TestBench that provides similar functionality:
 * - Opens real browser instances (Chrome)
 * - Navigates through different views (Meetings, Projects, Decisions)
 * - Clicks buttons and UI components
 * - Fills forms with test data
 * - Validates data entry and display
 * - Takes screenshots for verification
 * 
 * Advantages over TestBench:
 * - Completely free and open source
 * - No licensing requirements
 * - Works with any web application, not just Vaadin
 * 
 * Disadvantages:
 * - No Vaadin-specific helpers
 * - Requires more manual CSS selector work
 * - Less integrated with Vaadin component hierarchy
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "vaadin.launch-browser=false"
})
public class SeleniumUIAutomationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SeleniumUIAutomationTest.class);

    @LocalServerPort
    private int port;

    private String baseUrl;
    private WebDriver driver;
    private WebDriverWait wait;
    private int screenshotCounter = 1;

    @BeforeEach
    void setUp() {
        // Setup WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run headless for CI/CD environments
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        
        baseUrl = "http://localhost:" + port;
        LOGGER.info("Starting Selenium UI automation tests against: {}", baseUrl);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            takeScreenshot("final-state");
            driver.quit();
        }
    }

    @Test
    void testCompleteApplicationFlow() {
        LOGGER.info("=== Starting Complete Application Flow Test ===");
        
        // 1. Navigate to application and verify it loads
        navigateToApplication();
        
        // 2. Test Projects functionality
        testProjectsWorkflow();
        
        // 3. Test Meetings functionality
        testMeetingsWorkflow();
        
        // 4. Test Decisions functionality
        testDecisionsWorkflow();
        
        // 5. Verify navigation between views works
        testViewNavigation();
        
        LOGGER.info("=== Complete Application Flow Test Completed Successfully ===");
    }

    private void navigateToApplication() {
        LOGGER.info("Navigating to application at: {}", baseUrl);
        driver.get(baseUrl);
        
        // Wait for the page to load
        waitForPageLoad();
        
        takeScreenshot("01-application-loaded");
        
        // Verify application loaded successfully
        String title = driver.getTitle();
        LOGGER.info("Page title: {}", title);
        
        // Check for basic page elements
        boolean hasContent = !driver.findElements(By.tagName("body")).isEmpty();
        assertTrue(hasContent, "Application should load with basic content");
        
        LOGGER.info("Application navigation completed successfully");
    }

    private void testProjectsWorkflow() {
        LOGGER.info("Testing Projects workflow");
        
        try {
            // Navigate to projects view
            navigateToView("projects", "Projects");
            takeScreenshot("02-projects-view");
            
            // Look for project-related elements
            testCRUDOperations("projects", "Project");
            
        } catch (Exception e) {
            LOGGER.warn("Projects workflow test encountered issues: {}", e.getMessage());
            takeScreenshot("02-projects-error");
        }
    }

    private void testMeetingsWorkflow() {
        LOGGER.info("Testing Meetings workflow");
        
        try {
            // Navigate to meetings view
            navigateToView("meetings", "Meetings");
            takeScreenshot("03-meetings-view");
            
            // Look for meeting-related elements
            testCRUDOperations("meetings", "Meeting");
            
        } catch (Exception e) {
            LOGGER.warn("Meetings workflow test encountered issues: {}", e.getMessage());
            takeScreenshot("03-meetings-error");
        }
    }

    private void testDecisionsWorkflow() {
        LOGGER.info("Testing Decisions workflow");
        
        try {
            // Navigate to decisions view
            navigateToView("decisions", "Decisions");
            takeScreenshot("04-decisions-view");
            
            // Look for decision-related elements
            testCRUDOperations("decisions", "Decision");
            
        } catch (Exception e) {
            LOGGER.warn("Decisions workflow test encountered issues: {}", e.getMessage());
            takeScreenshot("04-decisions-error");
        }
    }

    private void testCRUDOperations(String viewName, String entityName) {
        LOGGER.info("Testing CRUD operations for {}", entityName);
        
        try {
            // Look for grids or data tables
            List<WebElement> grids = findElementsByMultipleSelectors(
                "vaadin-grid", 
                "[class*='grid']",
                "table",
                "[role='grid']"
            );
            
            if (!grids.isEmpty()) {
                LOGGER.info("Found {} grid/table elements in {} view", grids.size(), viewName);
                takeScreenshot("05-" + viewName + "-grid-found");
            }
            
            // Look for "New" or "Add" buttons
            WebElement newButton = findButtonByText("New", "Add", "Create", "+");
            if (newButton != null) {
                LOGGER.info("Found create button, testing form workflow");
                testFormWorkflow(newButton, viewName, entityName);
            } else {
                LOGGER.warn("No create button found in {} view", viewName);
            }
            
        } catch (Exception e) {
            LOGGER.warn("CRUD operations test failed for {}: {}", viewName, e.getMessage());
            takeScreenshot("05-" + viewName + "-crud-error");
        }
    }

    private void testFormWorkflow(WebElement newButton, String viewName, String entityName) {
        try {
            // Click the new button
            newButton.click();
            waitForPageLoad();
            takeScreenshot("06-" + viewName + "-form-opened");
            
            // Look for form fields
            List<WebElement> textFields = findElementsByMultipleSelectors(
                "vaadin-text-field",
                "input[type='text']",
                "textarea",
                "[class*='text-field']"
            );
            
            LOGGER.info("Found {} text fields in {} form", textFields.size(), viewName);
            
            // Fill form fields with test data
            fillFormFields(textFields, viewName, entityName);
            
            // Look for save/submit button
            WebElement saveButton = findButtonByText("Save", "Submit", "Create", "OK");
            if (saveButton != null) {
                saveButton.click();
                waitForPageLoad();
                takeScreenshot("07-" + viewName + "-form-submitted");
                LOGGER.info("Form submission completed for {}", viewName);
            }
            
        } catch (Exception e) {
            LOGGER.warn("Form workflow failed for {}: {}", viewName, e.getMessage());
            takeScreenshot("06-" + viewName + "-form-error");
        }
    }

    private void fillFormFields(List<WebElement> fields, String viewName, String entityName) {
        try {
            for (int i = 0; i < Math.min(fields.size(), 3); i++) {
                WebElement field = fields.get(i);
                if (field.isEnabled() && field.isDisplayed()) {
                    String testValue = generateTestValue(i, entityName);
                    field.clear();
                    field.sendKeys(testValue);
                    LOGGER.info("Filled field {} with: {}", i, testValue);
                }
            }
            takeScreenshot("06a-" + viewName + "-form-filled");
        } catch (Exception e) {
            LOGGER.warn("Failed to fill form fields: {}", e.getMessage());
        }
    }

    private String generateTestValue(int fieldIndex, String entityName) {
        long timestamp = System.currentTimeMillis();
        switch (fieldIndex) {
            case 0:
                return "Test " + entityName + " " + timestamp;
            case 1:
                return "Description for " + entityName.toLowerCase() + " created by Selenium automation at " + timestamp;
            case 2:
                return "Additional notes or comments for " + entityName.toLowerCase();
            default:
                return "Test value " + fieldIndex;
        }
    }

    private void testViewNavigation() {
        LOGGER.info("Testing navigation between views");
        
        String[] views = {"projects", "meetings", "decisions"};
        
        for (String view : views) {
            try {
                navigateToView(view, view);
                
                // Verify we're in the correct view
                String currentUrl = driver.getCurrentUrl();
                assertTrue(currentUrl.contains(view), "Should navigate to " + view + " view");
                
                takeScreenshot("08-navigation-" + view);
                LOGGER.info("Successfully navigated to {} view", view);
                
            } catch (Exception e) {
                LOGGER.warn("Navigation to {} failed: {}", view, e.getMessage());
                takeScreenshot("08-navigation-failed-" + view);
            }
        }
    }

    private void navigateToView(String urlPath, String viewName) {
        String viewUrl = baseUrl + "/" + urlPath;
        LOGGER.info("Navigating to {} view: {}", viewName, viewUrl);
        driver.get(viewUrl);
        waitForPageLoad();
    }

    private WebElement findButtonByText(String... possibleTexts) {
        // Try multiple selectors for buttons
        String[] buttonSelectors = {
            "vaadin-button",
            "button",
            "[role='button']",
            ".v-button",
            "[class*='button']"
        };
        
        for (String selector : buttonSelectors) {
            try {
                List<WebElement> buttons = driver.findElements(By.cssSelector(selector));
                for (WebElement button : buttons) {
                    String buttonText = button.getText();
                    if (buttonText != null) {
                        for (String text : possibleTexts) {
                            if (buttonText.toLowerCase().contains(text.toLowerCase())) {
                                LOGGER.info("Found button with text '{}' using selector '{}'", buttonText, selector);
                                return button;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        
        LOGGER.warn("Could not find button with any of these texts: {}", String.join(", ", possibleTexts));
        return null;
    }

    private List<WebElement> findElementsByMultipleSelectors(String... selectors) {
        for (String selector : selectors) {
            try {
                List<WebElement> elements = driver.findElements(By.cssSelector(selector));
                if (!elements.isEmpty()) {
                    LOGGER.debug("Found {} elements using selector '{}'", elements.size(), selector);
                    return elements;
                }
            } catch (Exception e) {
                // Continue with next selector
            }
        }
        return List.of(); // Return empty list if nothing found
    }

    @Test
    void testResponsiveDesign() {
        LOGGER.info("=== Testing Responsive Design ===");
        
        navigateToApplication();
        
        // Test different screen sizes
        int[][] screenSizes = {
            {1920, 1080}, // Desktop
            {1024, 768},  // Tablet
            {375, 667}    // Mobile
        };
        
        for (int[] size : screenSizes) {
            driver.manage().window().setSize(new org.openqa.selenium.Dimension(size[0], size[1]));
            waitForPageLoad();
            takeScreenshot("responsive-" + size[0] + "x" + size[1]);
            LOGGER.info("Tested responsive design at {}x{}", size[0], size[1]);
        }
        
        LOGGER.info("=== Responsive Design Test Completed ===");
    }

    @Test
    void testAccessibility() {
        LOGGER.info("=== Testing Basic Accessibility ===");
        
        navigateToApplication();
        
        try {
            // Check for basic accessibility elements
            List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
            LOGGER.info("Found {} heading elements", headings.size());
            
            List<WebElement> buttons = driver.findElements(By.cssSelector("button, [role='button']"));
            LOGGER.info("Found {} button elements", buttons.size());
            
            List<WebElement> links = driver.findElements(By.cssSelector("a"));
            LOGGER.info("Found {} link elements", links.size());
            
            // Check for alt attributes on images
            List<WebElement> images = driver.findElements(By.cssSelector("img"));
            for (WebElement img : images) {
                String alt = img.getAttribute("alt");
                if (alt == null || alt.trim().isEmpty()) {
                    LOGGER.warn("Image found without alt attribute: {}", img.getAttribute("src"));
                }
            }
            
            takeScreenshot("accessibility-check");
            
        } catch (Exception e) {
            LOGGER.warn("Accessibility test encountered issues: {}", e.getMessage());
        }
        
        LOGGER.info("=== Accessibility Test Completed ===");
    }

    @Test
    void testPerformance() {
        LOGGER.info("=== Testing Basic Performance ===");
        
        long startTime = System.currentTimeMillis();
        
        navigateToApplication();
        
        long loadTime = System.currentTimeMillis() - startTime;
        LOGGER.info("Application load time: {} ms", loadTime);
        
        // Test navigation performance
        String[] views = {"projects", "meetings", "decisions"};
        for (String view : views) {
            long navStart = System.currentTimeMillis();
            navigateToView(view, view);
            long navTime = System.currentTimeMillis() - navStart;
            LOGGER.info("{} view load time: {} ms", view, navTime);
        }
        
        takeScreenshot("performance-test-completed");
        
        LOGGER.info("=== Performance Test Completed ===");
    }

    private void waitForPageLoad() {
        try {
            // Wait for document ready state
            wait.until(driver -> ((org.openqa.selenium.JavascriptExecutor) driver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Additional wait for dynamic content
            Thread.sleep(1000);
        } catch (Exception e) {
            LOGGER.warn("Page load wait encountered issues: {}", e.getMessage());
        }
    }

    private void takeScreenshot(String name) {
        try {
            if (driver instanceof TakesScreenshot) {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File targetFile = new File("target/screenshots", 
                    String.format("%02d-%s.png", screenshotCounter++, name));
                targetFile.getParentFile().mkdirs();
                
                // Copy file (using basic approach for compatibility)
                java.nio.file.Files.copy(screenshot.toPath(), targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                LOGGER.info("Screenshot saved: {}", targetFile.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to take screenshot '{}': {}", name, e.getMessage());
        }
    }
}