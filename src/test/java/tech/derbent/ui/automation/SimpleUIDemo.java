package tech.derbent.ui.automation;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * SimpleUIDemo - A standalone demo of browser automation without Spring Boot dependency.
 * 
 * This test demonstrates basic browser automation capabilities:
 * - Opens a real Chrome browser
 * - Navigates to a website
 * - Takes screenshots
 * - Verifies page content
 * 
 * This test works independently of the application and shows the testing infrastructure.
 */
public class SimpleUIDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleUIDemo.class);
    private WebDriver driver;

    @BeforeEach
    void setUp() {
        LOGGER.info("Setting up browser automation demo...");
        
        // Setup WebDriver using WebDriverManager
        WebDriverManager.chromedriver().setup();
        
        // Configure Chrome options
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Run headless for demo
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        
        LOGGER.info("Browser automation setup completed");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            takeScreenshot("demo-final");
            driver.quit();
            LOGGER.info("Browser closed");
        }
    }

    @Test
    void demonstrateBrowserAutomation() {
        LOGGER.info("=== Browser Automation Demo ===");
        
        // Navigate to a simple webpage
        LOGGER.info("Navigating to example.com...");
        driver.get("https://example.com");
        
        // Wait for page to load
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Take screenshot
        takeScreenshot("demo-example-page");
        
        // Verify page content
        String title = driver.getTitle();
        LOGGER.info("Page title: {}", title);
        assertTrue(title.contains("Example"), "Page should contain 'Example' in title");
        
        // Find and verify content
        String pageText = driver.findElement(By.tagName("body")).getText();
        LOGGER.info("Page contains {} characters of text", pageText.length());
        
        // Verify specific content
        assertTrue(pageText.contains("Example Domain"), "Page should contain 'Example Domain'");
        
        LOGGER.info("✅ Browser automation demo completed successfully!");
        LOGGER.info("📸 Screenshots saved to target/screenshots/");
        
        // This demonstrates what the UI tests would do:
        LOGGER.info("");
        LOGGER.info("🎯 In a real Vaadin application test, this would:");
        LOGGER.info("   1. Navigate to http://localhost:8080");
        LOGGER.info("   2. Click on 'Projects' menu item");
        LOGGER.info("   3. Click 'New' button to create project");
        LOGGER.info("   4. Fill form fields with test data");
        LOGGER.info("   5. Click 'Save' to submit form");
        LOGGER.info("   6. Verify project appears in grid");
        LOGGER.info("   7. Take screenshots at each step");
        LOGGER.info("   8. Repeat for Meetings and Decisions");
    }

    @Test
    void demonstrateFormInteraction() {
        LOGGER.info("=== Form Interaction Demo ===");
        
        // Navigate to a page with forms (using httpbin.org which has form examples)
        LOGGER.info("Navigating to httpbin.org form demo...");
        driver.get("https://httpbin.org/forms/post");
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        takeScreenshot("demo-form-page");
        
        // This demonstrates form interaction capabilities
        try {
            // Find form elements (similar to what we'd do in Vaadin app)
            var inputs = driver.findElements(By.cssSelector("input[type='text']"));
            LOGGER.info("Found {} text input fields", inputs.size());
            
            if (!inputs.isEmpty()) {
                inputs.get(0).sendKeys("Test Name");
                LOGGER.info("✅ Successfully filled text field");
            }
            
            var buttons = driver.findElements(By.cssSelector("button, input[type='submit']"));
            LOGGER.info("Found {} buttons/submit elements", buttons.size());
            
            takeScreenshot("demo-form-filled");
            
        } catch (Exception e) {
            LOGGER.warn("Form interaction demo encountered issues: {}", e.getMessage());
        }
        
        LOGGER.info("✅ Form interaction demo completed!");
    }

    private void takeScreenshot(String name) {
        try {
            if (driver instanceof TakesScreenshot) {
                File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                File targetFile = new File("target/screenshots", name + "-demo.png");
                targetFile.getParentFile().mkdirs();
                
                java.nio.file.Files.copy(screenshot.toPath(), targetFile.toPath(), 
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                
                LOGGER.info("📸 Screenshot saved: {}", targetFile.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to take screenshot: {}", e.getMessage());
        }
    }
}