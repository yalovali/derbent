package automated_tests.tech.derbent.ui.automation.debug;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import tech.derbent.Application;

/**
 * Simplified Playwright test to debug why browser can't connect to Spring Boot server.
 * This test mimics our working CSimpleStartupTest but adds browser connection.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource(properties = {
    "spring.profiles.active=test",
    "server.port=0",  // Let Spring Boot pick an available port
    "spring.datasource.url=jdbc:h2:mem:testdb", 
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver", 
    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("🔍 Simple Playwright Connection Test")
public class CSimplePlaywrightTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSimplePlaywrightTest.class);

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Verify Spring Boot starts and browser can connect")
    public void testSpringBootStartupAndBrowserConnection() throws Exception {
        LOGGER.info("🚀 Starting simplified Playwright connection test");
        
        // Verify port is assigned
        LOGGER.info("✅ Spring Boot test server started on port: {}", port);
        
        // Create Playwright browser
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(false)  // Visible browser for debugging
                .setSlowMo(100));    // Slow motion for debugging
            
            Page page = browser.newPage();
            
            String testUrl = "http://localhost:" + port;
            LOGGER.info("🌐 Attempting to navigate to: {}", testUrl);
            
            // Navigate to application
            page.navigate(testUrl);
            
            // Wait a moment and capture what we get
            Thread.sleep(2000);
            
            String pageTitle = page.title();
            String pageUrl = page.url();
            
            LOGGER.info("📄 Page title: '{}'", pageTitle);
            LOGGER.info("🔗 Page URL: '{}'", pageUrl);
            
            // Check if we have any content
            String bodyText = page.locator("body").textContent();
            LOGGER.info("📝 Body content (first 200 chars): '{}'", 
                bodyText != null ? bodyText.substring(0, Math.min(200, bodyText.length())) : "null");
            
            // Check for login elements
            boolean hasUsernameInput = page.locator("#custom-username-input").count() > 0;
            boolean hasPasswordInput = page.locator("#custom-password-input").count() > 0;
            boolean hasLoginButton = page.locator("#cbutton-login").count() > 0;
            
            LOGGER.info("🔍 Login elements found:");
            LOGGER.info("  - Username input: {}", hasUsernameInput);
            LOGGER.info("  - Password input: {}", hasPasswordInput);
            LOGGER.info("  - Login button: {}", hasLoginButton);
            
            // Take a screenshot for debugging
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("debug-playwright-page.png")));
            LOGGER.info("📸 Screenshot saved to: debug-playwright-page.png");
            
            browser.close();
        }
        
        LOGGER.info("✅ Test completed successfully");
    }
}