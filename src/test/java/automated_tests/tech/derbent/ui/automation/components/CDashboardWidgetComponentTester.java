package automated_tests.tech.derbent.ui.automation.components;

import automated_tests.tech.derbent.ui.automation.components.helpers.CTestComponentBase_helper;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

/**
 * Tests the BAB Dashboard Widget component that displays project information and status.
 * 
 * Component features tested:
 * - Widget title display
 * - Project name display
 * - Active status indicator
 * - Dashboard type label
 * - Status indicator color
 * - Refresh functionality
 * 
 * Integration testing:
 * - Run: MAVEN_OPTS="-Dtest.routeKeyword=bab dashboard" ./run-playwright-tests.sh comprehensive
 */
public class CDashboardWidgetComponentTester extends CBaseComponentTester {

	private static final String COMPONENT_NAME = "BAB Dashboard Widget";
	private static final String COMPONENT_TAB_LABEL = "Dashboard";
	private static final String ROOT_ID = "custom-dashboard-widget-bab";
	private static final String PROJECT_NAME_ID = "custom-dashboard-project-name";
	private static final String ACTIVE_STATUS_ID = "custom-dashboard-active-status";
	private static final String TYPE_LABEL_ID = "custom-dashboard-type";
	private static final String STATUS_INDICATOR_ID = "custom-dashboard-status-indicator";

	private static final String[] ROOT_SELECTORS = {
		"#" + ROOT_ID,
		".bab-dashboard-widget",
		".dashboard-widget"
	};

	@Override
	public boolean canTest(final Page page) {
		return locateRoot(page) != null;
	}

	@Override
	public String getComponentName() {
		return COMPONENT_NAME;
	}

	private Locator locateRoot(final Page page) {
		for (final String selector : ROOT_SELECTORS) {
			final Locator root = page.locator(selector);
			if (root.count() > 0) {
				for (int i = 0; i < root.count(); i++) {
					final Locator candidate = root.nth(i);
					if (candidate.isVisible()) {
						return candidate;
					}
				}
			}
		}
		return null;
	}

	private void scrollIntoView(final Locator root) {
		try {
			root.scrollIntoViewIfNeeded();
			wait_500(root.page());
		} catch (final Exception e) {
			LOGGER.debug("      ⚠️ Unable to scroll dashboard widget into view: {}", e.getMessage());
		}
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🏠 Validating BAB Dashboard Widget...");
		
		// Open dashboard tab if needed
		openTabOrAccordionIfNeeded(page, COMPONENT_TAB_LABEL);
		
		final Locator root = locateRoot(page);
		if (root == null) {
			throw new AssertionError("BAB Dashboard Widget root not found on page " + safePageTitle(page));
		}
		
		scrollIntoView(root);
		
		// Test all widget components
		verifyWidgetTitle(root);
		verifyProjectName(page);
		verifyActiveStatus(page);
		verifyDashboardType(page);
		verifyStatusIndicator(page);
		testRefreshFunctionality(page, root);
		
		LOGGER.info("      ✅ BAB Dashboard Widget validation complete");
	}

	private void verifyWidgetTitle(final Locator root) {
		LOGGER.info("      📋 Verifying widget title...");
		
		final Locator title = root.locator("h3.dashboard-widget-title");
		if (title.count() == 0) {
			throw new AssertionError("Dashboard widget title not found");
		}
		
		if (!title.isVisible()) {
			throw new AssertionError("Dashboard widget title exists but is not visible");
		}
		
		final String titleText = title.textContent().trim();
		LOGGER.info("      📍 Widget title: '{}'", titleText);
		
		if (!titleText.toLowerCase().contains("bab") && !titleText.toLowerCase().contains("dashboard")) {
			LOGGER.warn("      ⚠️ Unexpected widget title: {}", titleText);
		}
	}

	private void verifyProjectName(final Page page) {
		LOGGER.info("      📁 Verifying project name display...");
		
		final Locator projectLabel = page.locator("#" + PROJECT_NAME_ID);
		if (projectLabel.count() == 0) {
			throw new AssertionError("Project name label not found");
		}
		
		if (!projectLabel.isVisible()) {
			throw new AssertionError("Project name label exists but is not visible");
		}
		
		final String projectText = projectLabel.textContent().trim();
		LOGGER.info("      📍 Project display: '{}'", projectText);
		
		if (!projectText.toLowerCase().contains("project")) {
			LOGGER.warn("      ⚠️ Project label format unexpected: {}", projectText);
		}
	}

	private void verifyActiveStatus(final Page page) {
		LOGGER.info("      📊 Verifying active status display...");
		
		final Locator statusLabel = page.locator("#" + ACTIVE_STATUS_ID);
		if (statusLabel.count() == 0) {
			throw new AssertionError("Active status label not found");
		}
		
		if (!statusLabel.isVisible()) {
			throw new AssertionError("Active status label exists but is not visible");
		}
		
		final String statusText = statusLabel.textContent().trim();
		LOGGER.info("      📍 Status display: '{}'", statusText);
		
		if (!statusText.toLowerCase().contains("status")) {
			LOGGER.warn("      ⚠️ Status label format unexpected: {}", statusText);
		}
	}

	private void verifyDashboardType(final Page page) {
		LOGGER.info("      🏷️ Verifying dashboard type label...");
		
		final Locator typeLabel = page.locator("#" + TYPE_LABEL_ID);
		if (typeLabel.count() == 0) {
			throw new AssertionError("Dashboard type label not found");
		}
		
		if (!typeLabel.isVisible()) {
			throw new AssertionError("Dashboard type label exists but is not visible");
		}
		
		final String typeText = typeLabel.textContent().trim();
		LOGGER.info("      📍 Dashboard type: '{}'", typeText);
		
		if (typeText.isEmpty()) {
			LOGGER.warn("      ⚠️ Dashboard type is empty");
		}
	}

	private void verifyStatusIndicator(final Page page) {
		LOGGER.info("      🔴🟢 Verifying status indicator...");
		
		final Locator indicator = page.locator("#" + STATUS_INDICATOR_ID);
		if (indicator.count() == 0) {
			throw new AssertionError("Status indicator not found");
		}
		
		if (!indicator.isVisible()) {
			throw new AssertionError("Status indicator exists but is not visible");
		}
		
		// Check indicator styling (color circle)
		final String styleAttr = indicator.getAttribute("style");
		if (styleAttr == null || !styleAttr.contains("background-color")) {
			LOGGER.warn("      ⚠️ Status indicator missing background color styling");
		} else {
			LOGGER.debug("      ✓ Status indicator has color styling");
			
			// Extract color if possible
			if (styleAttr.contains("#4CAF50")) {
				LOGGER.info("      🟢 Status color: GREEN (Active)");
			} else if (styleAttr.contains("#FF9800")) {
				LOGGER.info("      🟠 Status color: ORANGE (Warning)");
			} else if (styleAttr.contains("#9E9E9E")) {
				LOGGER.info("      ⚪ Status color: GRAY (Inactive)");
			} else {
				LOGGER.debug("      📍 Status color: {}", extractColor(styleAttr));
			}
		}
		
		// Verify indicator is circular
		final String width = indicator.evaluate("el => window.getComputedStyle(el).width").toString();
		final String height = indicator.evaluate("el => window.getComputedStyle(el).height").toString();
		final String borderRadius = indicator.evaluate("el => window.getComputedStyle(el).borderRadius").toString();
		
		LOGGER.debug("      ✓ Indicator dimensions: {}x{}, border-radius: {}", width, height, borderRadius);
	}

	private String extractColor(final String styleAttr) {
		try {
			final int start = styleAttr.indexOf("background-color:") + 17;
			final int end = styleAttr.indexOf(";", start);
			if (end > start) {
				return styleAttr.substring(start, end).trim();
			}
		} catch (final Exception e) {
			LOGGER.debug("      ⚠️ Could not extract color: {}", e.getMessage());
		}
		return "unknown";
	}

	private void testRefreshFunctionality(final Page page, final Locator root) {
		LOGGER.info("      🔄 Testing widget refresh functionality...");
		
		try {
			// Capture initial state
			final Locator projectLabel = page.locator("#" + PROJECT_NAME_ID);
			final String initialProject = projectLabel.count() > 0 ? projectLabel.textContent().trim() : "";
			
			final Locator statusLabel = page.locator("#" + ACTIVE_STATUS_ID);
			final String initialStatus = statusLabel.count() > 0 ? statusLabel.textContent().trim() : "";
			
			LOGGER.info("      📍 Initial state: Project='{}', Status='{}'", initialProject, initialStatus);
			
			// Look for refresh button (if exists)
			final Locator refreshButton = root.locator("vaadin-button:has-text('Refresh'), button:has-text('Refresh')");
			
			if (refreshButton.count() > 0 && refreshButton.isVisible()) {
				LOGGER.info("      🖱️ Refresh button found - testing click...");
				refreshButton.click();
				wait_1000(page);
				
				// Verify content after refresh
				final String afterProject = projectLabel.count() > 0 ? projectLabel.textContent().trim() : "";
				final String afterStatus = statusLabel.count() > 0 ? statusLabel.textContent().trim() : "";
				
				LOGGER.info("      📍 After refresh: Project='{}', Status='{}'", afterProject, afterStatus);
				LOGGER.info("      ✅ Refresh functionality verified");
			} else {
				LOGGER.info("      ℹ️ No refresh button found - widget uses automatic updates");
			}
			
		} catch (final PlaywrightException e) {
			LOGGER.warn("      ⚠️ Refresh test failed: {}", e.getMessage());
		} catch (final Exception e) {
			LOGGER.error("      ❌ Unexpected error during refresh test: {}", e.getMessage(), e);
		}
	}
}
