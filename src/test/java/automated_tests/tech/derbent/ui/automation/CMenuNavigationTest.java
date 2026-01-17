package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Assumptions;


/** Fast hierarchical menu navigation test - logs in and browses all menu items at all levels. Handles dynamic database-driven menu from
 * CPageEntity. */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧭 Hierarchical Menu Navigation Test")
public class CMenuNavigationTest extends CBaseUITest {

	private static final String BACK_BUTTON_SELECTOR = ".hierarchical-back-button";
	private static final Logger LOGGER = LoggerFactory.getLogger(CMenuNavigationTest.class);
	private static final String MENU_ITEM_SELECTOR = ".hierarchical-menu-item";

	/** Check if menu item has submenu (doesn't navigate to a page) */
	private static boolean checkIfHasSubMenu(Locator item) {
		try {
			// Check if item has arrow icon or navigation arrow
			final Locator arrow = item.locator("vaadin-icon[icon*='angle-right'], vaadin-icon[icon*='arrow-right']");
			return arrow.count() > 0;
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return false;
		}
	}

	/** Make filename-safe name from menu label */
	private static String makeSafeName(String label) {
		return label.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "").substring(0, Math.min(label.length(), 50));
	}

	private int screenshotCounter = 1;
	private final Set<String> visitedPages = new HashSet<>();

	/** Recursively explore menu items at current level */
	private void exploreMenuLevel(int depth) {
		if (depth > 5) {
			LOGGER.warn("⚠️ Max menu depth reached, stopping recursion");
			return;
		}
		final String indent = "  ".repeat(depth);
		LOGGER.info("{}🔍 Exploring menu level {}", indent, depth);
		// Get all menu items at current level
		final Locator menuItems = page.locator(MENU_ITEM_SELECTOR);
		final int itemCount = menuItems.count();
		if (itemCount == 0) {
			LOGGER.info("{}📭 No menu items at this level", indent);
			return;
		}
		LOGGER.info("{}📋 Found {} menu items at level {}", indent, itemCount, depth);
		// Visit each menu item
		for (int i = 0; i < itemCount; i++) {
			try {
				// Re-query menu items (DOM changes with navigation)
				final Locator items = page.locator(MENU_ITEM_SELECTOR);
				if (i >= items.count()) {
					LOGGER.warn("{}⚠️ Menu items changed, stopping at index {}", indent, i);
					break;
				}
				final Locator item = items.nth(i);
				String label = item.textContent();
				if (label == null || label.trim().isEmpty()) {
					continue;
				}
				label = label.trim();
				LOGGER.info("{}📍 Item {}/{}: {}", indent, i + 1, itemCount, label);
				// Check if this is a navigation item (has arrow or triggers navigation)
				final boolean hasSubMenu = checkIfHasSubMenu(item);
				// Click the menu item
				item.click();
				// Wait briefly for navigation/animation
				try {
					Thread.sleep(500);
				} catch (@SuppressWarnings ("unused") final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				// Check current URL/page
				final String currentUrl = page.url();
				if (hasSubMenu) {
					// This opens a submenu - recursively explore it
					LOGGER.info("{}  ➡️ Entering submenu: {}", indent, label);
					// Take screenshot of submenu
					final String safeName = makeSafeName(label);
					takeScreenshot(String.format("%03d-menu-%s", screenshotCounter++, safeName), false);
					// Recursively explore submenu
					exploreMenuLevel(depth + 1);
					// Go back to parent menu
					goBackToParentMenu(indent);
				} else {
					// This is a leaf item - just visit the page
					if (!visitedPages.contains(currentUrl)) {
						LOGGER.info("{}  📄 Visited page: {}", indent, label);
						visitedPages.add(currentUrl);
						// Take screenshot of page
						final String safeName = makeSafeName(label);
						takeScreenshot(String.format("%03d-page-%s", screenshotCounter++, safeName), false);
						// Wait for page load
						waitForPageLoad();
					}
				}
			} catch (final Exception e) {
				LOGGER.warn("{}⚠️ Failed to process menu item {}: {}", indent, i + 1, e.getMessage());
			}
		}
	}

	/** Go back to parent menu level */
	private void goBackToParentMenu(String indent) {
		try {
			final Locator backButton = page.locator(BACK_BUTTON_SELECTOR);
			if (backButton.count() > 0) {
				LOGGER.info("{}  ⬅️ Going back to parent menu", indent);
				backButton.click();
				// Wait for animation
				try {
					Thread.sleep(300);
				} catch (@SuppressWarnings ("unused") final InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("{}⚠️ Failed to go back: {}", indent, e.getMessage());
		}
	}

	@Test
	@DisplayName ("✅ Login and browse all hierarchical menu items")
	void testMenuNavigation() {
		LOGGER.info("🚀 Starting hierarchical menu navigation test...");
		// Check if browser is available
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test (expected in CI without browser)");
			Assumptions.assumeTrue(false, "Browser not available in CI environment");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Login
			loginToApplication();
			LOGGER.info("✅ Login successful");
			takeScreenshot(String.format("%03d-after-login", screenshotCounter++), false);
			// FAIL-FAST CHECK: After login
			performFailFastCheck("After Login Complete");
			// Wait for menu to be ready
			page.waitForSelector(MENU_ITEM_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
			// FAIL-FAST CHECK: After menu load
			performFailFastCheck("After Menu Load");
			// Browse all menu levels starting from root
			LOGGER.info("📋 Starting hierarchical menu navigation from root level");
			exploreMenuLevel(0);
			// FAIL-FAST CHECK: After menu navigation
			performFailFastCheck("After Menu Navigation Complete");
			LOGGER.info("✅ Menu navigation test completed - visited {} unique pages", visitedPages.size());
		} catch (final Exception e) {
			LOGGER.error("❌ Menu navigation test failed: {}", e.getMessage(), e);
			takeScreenshot("error", true);
			throw new AssertionError("Menu navigation test failed", e);
		}
	}

	/** Wait for page to load */
	private void waitForPageLoad() {
		try {
			page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(2000));
		} catch (@SuppressWarnings ("unused") final Exception e) {
			// Ignore timeout, page may already be loaded
		}
	}
}
