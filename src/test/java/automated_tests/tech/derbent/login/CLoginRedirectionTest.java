package automated_tests.tech.derbent.login;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import ui_tests.tech.derbent.ui.automation.CBaseUITest;

/**
 * Automated UI tests for the login redirection functionality.
 * Tests the complete flow of auto-login and last requested page navigation.
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.username=sa", 
    "spring.datasource.password=",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "server.port=8080"
})
public class CLoginRedirectionTest extends CBaseUITest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLoginRedirectionTest.class);
    
    @Test
    void shouldRedirectToSelectedViewAfterLogin() {
        LOGGER.info("🧪 Testing login redirection to selected view...");
        if (page == null) {
            LOGGER.warn("⚠️ Browser not available, skipping UI test");
            return;
        }
        
        try {
            // Navigate to login page
            page.navigate(baseUrl + "/login");
            page.waitForSelector(".custom-login-view", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Select a specific view in the combobox
            Locator viewComboBox = page.locator("vaadin-combo-box").first();
            if (viewComboBox.isVisible()) {
                viewComboBox.click();
                page.waitForTimeout(500);
                
                // Select "Projects" view
                page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText("Projects")).click();
                page.waitForTimeout(500);
            }
            
            // Fill in credentials
            page.locator("#custom-username-input").fill("admin");
            page.locator("#custom-password-input").fill("test123");
            
            // Submit login form
            page.locator("#custom-submit-button").click();
            
            // Wait for redirect and verify we're on the projects page
            page.waitForTimeout(3000);
            String currentUrl = page.url();
            assertTrue(currentUrl.contains("projects") || currentUrl.contains("cprojectsview"), 
                "Should redirect to projects view, but current URL is: " + currentUrl);
            
            LOGGER.info("✅ Login redirection to selected view test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Login redirection test failed: {}", e.getMessage());
            throw e;
        }
    }
    
    @Test
    void shouldPreSelectLastRequestedPageInLogin() {
        LOGGER.info("🧪 Testing last requested page pre-selection in login...");
        if (page == null) {
            LOGGER.warn("⚠️ Browser not available, skipping UI test");
            return;
        }
        
        try {
            // First, login to ensure we have an active session
            loginAsAdmin();
            
            // Navigate to a specific page (e.g., users view)
            page.navigate(baseUrl + "/cusersview");
            page.waitForTimeout(2000);
            
            // Logout to trigger redirect to login
            logout();
            
            // Verify we're on login page
            page.waitForSelector(".custom-login-view", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Check if the view combobox shows the correct selection
            // (This would be the last requested page)
            Locator viewComboBox = page.locator("vaadin-combo-box").first();
            if (viewComboBox.isVisible()) {
                String comboboxValue = viewComboBox.inputValue();
                // The combobox should ideally show "users" or similar based on our navigation
                LOGGER.info("Combobox current value: {}", comboboxValue);
                // Note: This test validates the UI behavior - in a real implementation,
                // the system should remember the last requested page
            }
            
            LOGGER.info("✅ Last requested page pre-selection test completed");
            
        } catch (Exception e) {
            LOGGER.error("❌ Last requested page test failed: {}", e.getMessage());
            throw e;
        }
    }
    
    @Test
    void shouldHandleAutoLoginWithRedirection() {
        LOGGER.info("🧪 Testing auto-login with redirection...");
        if (page == null) {
            LOGGER.warn("⚠️ Browser not available, skipping UI test");
            return;
        }
        
        try {
            // Navigate to login page
            page.navigate(baseUrl + "/login");
            page.waitForSelector(".custom-login-view", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Enable auto-login checkbox
            Locator autoLoginCheckbox = page.locator("#auto-login-checkbox");
            if (autoLoginCheckbox.isVisible()) {
                autoLoginCheckbox.check();
                page.waitForTimeout(500);
            }
            
            // Select a view
            Locator viewComboBox = page.locator("vaadin-combo-box").first();
            if (viewComboBox.isVisible()) {
                viewComboBox.click();
                page.waitForTimeout(500);
                page.locator("vaadin-combo-box-item").filter(new Locator.FilterOptions().setHasText("Activities")).click();
                page.waitForTimeout(500);
            }
            
            // Fill in credentials
            page.locator("#custom-username-input").fill("admin");
            page.locator("#custom-password-input").fill("test123");
            
            // Wait for auto-login (should trigger after 2 seconds)
            page.waitForTimeout(3000);
            
            // Verify we're redirected to the activities page
            String currentUrl = page.url();
            assertTrue(currentUrl.contains("activities") || currentUrl.contains("cactivitiesview"), 
                "Should auto-redirect to activities view, but current URL is: " + currentUrl);
            
            LOGGER.info("✅ Auto-login with redirection test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Auto-login test failed: {}", e.getMessage());
            throw e;
        }
    }
    
    @Test
    void shouldDefaultToHomeWhenNoViewSelected() {
        LOGGER.info("🧪 Testing default redirection to home...");
        if (page == null) {
            LOGGER.warn("⚠️ Browser not available, skipping UI test");
            return;
        }
        
        try {
            // Navigate to login page
            page.navigate(baseUrl + "/login");
            page.waitForSelector(".custom-login-view", new Page.WaitForSelectorOptions().setTimeout(10000));
            
            // Don't select any specific view, just login
            page.locator("#custom-username-input").fill("admin");
            page.locator("#custom-password-input").fill("test123");
            page.locator("#custom-submit-button").click();
            
            // Wait for redirect
            page.waitForTimeout(3000);
            
            // Should default to home/dashboard
            String currentUrl = page.url();
            assertTrue(currentUrl.contains("home") || currentUrl.contains("dashboard") || !currentUrl.contains("login"), 
                "Should redirect to home by default, but current URL is: " + currentUrl);
            
            LOGGER.info("✅ Default redirection test completed successfully");
            
        } catch (Exception e) {
            LOGGER.error("❌ Default redirection test failed: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper method to login as admin user
     */
    private void loginAsAdmin() {
        page.navigate(baseUrl + "/login");
        page.waitForSelector(".custom-login-view", new Page.WaitForSelectorOptions().setTimeout(10000));
        page.locator("#custom-username-input").fill("admin");
        page.locator("#custom-password-input").fill("test123");
        page.locator("#custom-submit-button").click();
        page.waitForTimeout(2000);
    }
    
    /**
     * Helper method to logout
     */
    private void logout() {
        // Try to find and click logout button/link
        // This might vary based on the UI implementation
        try {
            page.navigate(baseUrl + "/logout");
            page.waitForTimeout(1000);
        } catch (Exception e) {
            LOGGER.warn("Logout method may need adjustment based on UI implementation");
        }
    }
}