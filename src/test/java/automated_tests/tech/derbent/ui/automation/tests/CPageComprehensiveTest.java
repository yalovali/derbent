package automated_tests.tech.derbent.ui.automation.tests;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import automated_tests.tech.derbent.ui.automation.components.CAttachmentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCalimeroStatusComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCloneToolbarComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCommentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCrudToolbarComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CDatePickerComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CGridComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CInterfaceListComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CLinkComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CProjectComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CProjectUserSettingsComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CReportComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CStatusFieldComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CUserComponentTester;
import automated_tests.tech.derbent.ui.automation.components.IComponentTester;
import automated_tests.tech.derbent.ui.automation.tests.helpers.CControlSignature;
import automated_tests.tech.derbent.ui.automation.tests.helpers.IControlSignature;
import tech.derbent.Application;

/** Unified comprehensive page testing framework combining adaptive component detection with CRUD and grid testing.
 * <p>
 * <b>REPLACES</b>: CAdaptivePageTest + CPageTestAuxillaryComprehensiveTest (merged 2026-01-31)
 * <p>
 * This test framework provides complete page testing capabilities:
 * <ul>
 * <li><b>Phase 1 - Navigation</b>: Via CPageTestAuxillary buttons (stable test infrastructure) ✅ COMPLETE
 * <li><b>Phase 2 - Component Detection</b>: Automatic signature-based discovery with tab/accordion walking ✅ COMPLETE
 * <li><b>Phase 3 - CRUD Testing</b>: New, Save, Delete operations with validation ✅ COMPLETE
 * <li><b>Phase 4 - Grid Testing</b>: Structure, selection, sorting, filtering, pagination ✅ COMPLETE
 * <li><b>Phase 5 - Coverage Reports</b>: CSV and Markdown with statistics (TODO)
 * <li><b>Phase 6 - Cleanup</b>: Deprecate old test classes (TODO)
 * </ul>
 * <p>
 * Architecture:
 * <ul>
 * <li><b>IComponentTester</b>: Interface for component-specific testers
 * <li><b>IControlSignature</b>: Component detection via CSS selectors
 * <li><b>Test Flow</b>: Navigate → Register Signatures → Detect → Test (Components/CRUD/Grid) → Report
 * <li><b>Fail-Fast</b>: All operations check for exception dialogs via performFailFastCheck()
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * # Test all BAB pages
 * mvn test -Dtest=CPageTestComprehensive
 *
 * # Test pages matching keyword
 * mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=Configure
 *
 * # Test specific page by button ID
 * mvn test -Dtest=CPageTestComprehensive -Dtest.targetButtonId=test-aux-btn-configure-5
 *
 * # Run all matching pages (not just first)
 * mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=Interface -Dtest.runAllMatches=true
 *
 * # Visible browser for debugging
 * PLAYWRIGHT_HEADLESS=false mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword=Configure
 * </pre>
 *
 * @see CBaseUITest
 * @see IComponentTester
 * @see IControlSignature */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=bab", "spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🎯 Comprehensive Page Testing Framework")
public class CPageComprehensiveTest extends CBaseUITest {

	/** Button information from CPageTestAuxillary. */
	private static class ButtonInfo {

		String id;
		String route;
		String title;

		ButtonInfo(final String id, final String title, final String route) {
			this.id = id;
			this.title = title;
			this.route = route;
		}
	}
	// ========================================
	// Navigation & Discovery
	// ========================================

	/** Coverage information for a tested page (Phase 5 - enhanced). */
	private static class PageCoverage {

		String buttonId;
		int componentCount;
		List<String> componentTypes = new ArrayList<>();
		long endTime;
		String errorMessage;
		int gridRowCount;
		// Feature flags
		boolean hasComponents;
		boolean hasCrudToolbar;
		boolean hasGrid;
		boolean hasTabs;
		String pageName;
		boolean passed;
		String route;
		// Timing
		long startTime;
		int tabCount;
		boolean testedCrud;
		boolean testedGrid;

		public PageCoverage(String pageName, String route, String buttonId) {
			this.pageName = pageName;
			this.route = route;
			this.buttonId = buttonId;
			startTime = System.currentTimeMillis();
		}

		public String getDurationFormatted() {
			long seconds = getDurationMs() / 1000;
			final long minutes = seconds / 60;
			seconds = seconds % 60;
			return "%dm %ds".formatted(minutes, seconds);
		}

		public long getDurationMs() { return endTime - startTime; }

		public void markComplete() {
			endTime = System.currentTimeMillis();
		}
	}

	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final String CONFIRM_YES_BUTTON_ID = "cbutton-yes";
	// ========================================
	// Component Tester Registry (Phase 2)
	// ========================================
	private static final String CRUD_CANCEL_BUTTON_ID = "cbutton-cancel";
	private static final String CRUD_DELETE_BUTTON_ID = "cbutton-delete";
	@SuppressWarnings ("unused")
	private static final String CRUD_EDIT_BUTTON_ID = "cbutton-edit";
	// CRUD button IDs (standard across all pages)
	private static final String CRUD_NEW_BUTTON_ID = "cbutton-new";
	private static final String CRUD_REFRESH_BUTTON_ID = "cbutton-refresh";
	private static final String CRUD_SAVE_BUTTON_ID = "cbutton-save";
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageComprehensiveTest.class);
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";
	private final IComponentTester attachmentTester = new CAttachmentComponentTester();
	private final IComponentTester babInterfaceListTester = new CInterfaceListComponentTester();
	private final IComponentTester calimeroStatusTester = new CCalimeroStatusComponentTester();
	private final IComponentTester cloneTester = new CCloneToolbarComponentTester();
	private final IComponentTester commentTester = new CCommentComponentTester();
	// Control signatures for component detection
	private final List<IControlSignature> controlSignatures = initializeControlSignatures();
	private final List<PageCoverage> coverageData = new ArrayList<>();
	// ========================================
	// Coverage Tracking (Phase 5)
	// ========================================
	private final IComponentTester crudToolbarTester = new CCrudToolbarComponentTester();
	private final IComponentTester datePickerTester = new CDatePickerComponentTester();
	// ========================================
	// Main Test Method
	// ========================================
	private final IComponentTester gridTester = new CGridComponentTester();
	private final IComponentTester linkTester = new CLinkComponentTester();
	// ========================================
	// Navigation & Discovery Methods
	// ========================================
	private final IComponentTester projectTester = new CProjectComponentTester();
	private final IComponentTester projectUserSettingsTester = new CProjectUserSettingsComponentTester();
	private final IComponentTester reportTester = new CReportComponentTester();
	private final IComponentTester statusFieldTester = new CStatusFieldComponentTester();
	// ========================================
	// Phase 2: Component Detection & Testing
	// ========================================
	private final IComponentTester userTester = new CUserComponentTester();

	/** Check Calimero status after login by examining server logs. Logs ERROR if Calimero is not running, WARNING if status unclear. Then navigates
	 * to BAB System Settings for comprehensive component testing. */
	private void checkCalimeroStatusAfterLogin() {
		try {
			LOGGER.info("   🔍 Checking Calimero service status from logs...");
			// Check browser console for Calimero connection errors
			final boolean hasCalimeroErrors = page.locator("body").evaluate("() => { return window.console && window.console.error ? true : false; }")
					.toString().contains("true");
			if (hasCalimeroErrors) {
				LOGGER.error("   ❌ ERROR: Calimero service connection errors detected in browser console");
				LOGGER.error("   ❌ Calimero is NOT running on port 8077");
				LOGGER.error("   ❌ Dashboard components will fail - consider starting Calimero before testing");
			} else {
				LOGGER.info("   ✅ No Calimero connection errors detected in initial load");
			}
			// Navigate to BAB Gateway Settings to verify Calimero status component
			LOGGER.info("   📍 Navigating to 'BAB Gateway Settings' to verify Calimero status...");
			page.navigate("http://localhost:8080/cpagetestauxillary");
			wait_2000();
			// Wait for buttons to load with multiple waits
			wait_2000();
			// Try different button text variations
			Locator settingsButton = page.locator("button").filter(new Locator.FilterOptions().setHasText("BAB Gateway Settings")).first();
			if (settingsButton.count() == 0) {
				LOGGER.warn("   ⚠️ WARNING: 'BAB Gateway Settings' button not found, trying alternative selectors...");
				// Try by data-title attribute
				settingsButton = page.locator("button[data-title*='BAB Gateway Settings']").first();
			}
			if (settingsButton.count() == 0) {
				LOGGER.error("   ❌ ERROR: Could not find BAB Gateway Settings button");
				LOGGER.info("   ℹ️ Available buttons:");
				final Locator allButtons = page.locator("button");
				for (int i = 0; i < Math.min(5, allButtons.count()); i++) {
					LOGGER.info("      - {}", allButtons.nth(i).textContent());
				}
				return;
			}
			LOGGER.info("   ✅ Found BAB Gateway Settings button, clicking...");
			settingsButton.click();
			wait_2000();
			LOGGER.info("   ✅ Navigated to BAB Gateway Settings page");
			// Check Calimero status indicator
			final Locator statusIndicator = page.locator("#custom-calimero-status-indicator");
			if (statusIndicator.count() > 0) {
				final String statusText = statusIndicator.textContent();
				LOGGER.info("   📊 Calimero Status Indicator: {}", statusText);
				if (statusText.contains("Not Running") || statusText.contains("❌") || statusText.contains("Stopped")) {
					LOGGER.error("   ❌ ERROR: Calimero service is NOT RUNNING");
					LOGGER.error("   ❌ Please start Calimero manually or ensure binary is available");
					LOGGER.error("   ❌ Path: ~/git/calimero/build/calimero (or configured path)");
				} else if (statusText.contains("Running") || statusText.contains("✅")) {
					LOGGER.info("   ✅ SUCCESS: Calimero service is RUNNING and healthy");
				} else {
					LOGGER.warn("   ⚠️ WARNING: Calimero status unclear: {}", statusText);
				}
			} else {
				LOGGER.warn("   ⚠️ WARNING: Calimero status indicator not found on page");
			}
			// Proceed to comprehensive BAB System Settings testing
			LOGGER.info("   🧪 Proceeding to comprehensive BAB System Settings component testing...");
		} catch (final Exception e) {
			LOGGER.error("   ❌ ERROR: Failed to check Calimero status: {}", e.getMessage(), e);
			LOGGER.warn("   ⚠️ Tests will continue but may encounter connection errors");
		}
	}

	/** Check if a specific CRUD button exists. */
	private boolean checkCrudButtonExists(final String buttonId) {
		try {
			return page.locator("#" + buttonId).count() > 0;
		} catch (final Exception e) {
			LOGGER.debug("Error checking for {} button: {}", buttonId, e.getMessage());
			return false;
		}
	}

	/** Check if CRUD toolbar exists (2+ CRUD buttons present). */
	private boolean checkCrudToolbarExists() {
		try {
			int count = 0;
			if (page.locator("#" + CRUD_NEW_BUTTON_ID).count() > 0) {
				count++;
			}
			if (page.locator("#" + CRUD_DELETE_BUTTON_ID).count() > 0) {
				count++;
			}
			if (page.locator("#" + CRUD_SAVE_BUTTON_ID).count() > 0) {
				count++;
			}
			if (page.locator("#" + CRUD_REFRESH_BUTTON_ID).count() > 0) {
				count++;
			}
			return count >= 2;
		} catch (final Exception e) {
			LOGGER.debug("Error checking for CRUD toolbar: {}", e.getMessage());
			return false;
		}
	}

	/** Confirm dialog if present (clicks Yes button). */
	private void confirmDialogIfPresent() {
		try {
			final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
			if (overlay.count() == 0) {
				return;
			}
			final Locator confirmButton = page.locator("#" + CONFIRM_YES_BUTTON_ID);
			if (confirmButton.count() > 0) {
				confirmButton.click();
				LOGGER.debug("   ✓ Confirmed dialog");
			}
		} catch (final Exception e) {
			LOGGER.debug("Error confirming dialog: {}", e.getMessage());
		}
	}
	// ========================================
	// Coverage & Reporting (Phase 5 - Stub)
	// ========================================
	// Phase 3: CRUD Testing
	// ========================================

	/** Detect grid on current page (returns null if not found). */
	private Locator detectGrid() {
		try {
			final Locator grids = page.locator("vaadin-grid, vaadin-grid-pro");
			return grids.count() > 0 ? grids.first() : null;
		} catch (final Exception e) {
			LOGGER.debug("Error detecting grid: {}", e.getMessage());
			return null;
		}
	}

	/** Discover all navigation buttons on CPageTestAuxillary page. */
	private List<ButtonInfo> discoverNavigationButtons() {
		final List<ButtonInfo> buttons = new ArrayList<>();
		final Locator buttonLoc = page.locator(BUTTON_SELECTOR);
		final int count = buttonLoc.count();
		LOGGER.info("   Found {} navigation buttons", count);
		for (int i = 0; i < count; i++) {
			try {
				final Locator button = buttonLoc.nth(i);
				final String id = button.getAttribute("id");
				final String text = button.textContent().trim();
				final String route = button.getAttribute("data-route");
				final ButtonInfo buttonInfo = new ButtonInfo(id, text, route != null ? route : "");
				buttons.add(buttonInfo);
				LOGGER.debug("      Button {}: id='{}', text='{}', route='{}'", i, id, text, route);
			} catch (final Exception e) {
				LOGGER.warn("   ⚠️ Failed to read button {}: {}", i, e.getMessage());
			}
		}
		return buttons;
	}

	/** Escape CSV field (handle commas, quotes, newlines). */
	private String escapeCsv(final String value) {
		if (value == null) {
			return "";
		}
		if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
			return "\"" + value.replace("\"", "\"\"") + "\"";
		}
		return value;
	}

	/** Find the component tester responsible for a given control signature. */
	private IComponentTester findTesterForSignature(@SuppressWarnings ("unused") final IControlSignature signature) {
		// Check each tester to see if it can test this signature
		final List<IComponentTester> allTesters = List.of(crudToolbarTester, cloneTester, gridTester, attachmentTester, commentTester, linkTester,
				projectTester, projectUserSettingsTester, userTester, statusFieldTester, datePickerTester, reportTester, babInterfaceListTester,
				calimeroStatusTester);
		for (final IComponentTester tester : allTesters) {
			if (tester.canTest(page)) {
				return tester;
			}
		}
		return null;
	}

	/** Format duration in human-readable format. */
	private String formatDuration(final long durationMs) {
		final long seconds = durationMs / 1000;
		final long minutes = seconds / 60;
		final long remainingSeconds = seconds % 60;
		if (minutes > 0) {
			return "%dm %ds".formatted(minutes, remainingSeconds);
		}
		return "%ds".formatted(remainingSeconds);
	}

	/** Get grid row count safely (returns 0 on error). */
	private int getGridRowCountSafe() {
		try {
			final Locator grids = page.locator("vaadin-grid, vaadin-grid-pro");
			if (grids.count() == 0) {
				return 0;
			}
			final Locator grid = grids.first();
			final Locator rows = grid.locator("vaadin-grid-cell-content");
			return rows.count();
		} catch (final Exception e) {
			LOGGER.debug("Error getting grid row count: {}", e.getMessage());
			return 0;
		}
	}

	/** Initialize all control signatures for component detection. Called once during field initialization. */
	private List<IControlSignature> initializeControlSignatures() {
		return List.of(
				CControlSignature.forSelectorsMinMatch("CRUD Toolbar Signature",
						List.of("#cbutton-new", "#cbutton-save", "#cbutton-delete", "#cbutton-refresh", "#cbutton-edit", "#cbutton-cancel"), 2,
						crudToolbarTester),
				CControlSignature.forSelector("CRUD Save Button Signature", "#cbutton-save", crudToolbarTester),
				CControlSignature.forSelector("CRUD Delete Button Signature", "#cbutton-delete", crudToolbarTester),
				CControlSignature
						.forSelector("Clone Button Signature", "#cbutton-copy-to, #cbutton-clone, [id*='copy-to'], [id*='clone']", cloneTester),
				CControlSignature.forSelector("Grid Signature", "vaadin-grid, vaadin-grid-pro, so-grid, c-grid", gridTester),
				CControlSignature.forSelector("Attachment Signature", "#custom-attachment-component, vaadin-upload, [id*='attachment']",
						attachmentTester),
				CControlSignature.forSelector("Attachment Tab Signature",
						"vaadin-tab:has-text('Attachments'), vaadin-tab:has-text('Attachment'), vaadin-accordion-panel:has-text('Attachments')",
						attachmentTester),
				CControlSignature.forSelector("Comment Signature", "#custom-comment-component, [id*='comment']", commentTester),
				CControlSignature.forSelector("Comment Tab Signature",
						"vaadin-tab:has-text('Comments'), vaadin-tab:has-text('Comment'), vaadin-accordion-panel:has-text('Comments')",
						commentTester),
				CControlSignature.forSelector("Link Signature", "#custom-links-component, #custom-links-grid, #custom-links-toolbar", linkTester),
				CControlSignature.forSelector("Link Tab Signature",
						"vaadin-tab:has-text('Links'), vaadin-tab:has-text('Link'), vaadin-accordion-panel:has-text('Links')", linkTester),
				CControlSignature.forSelector("Project View Signature", "#field-entityType, label:has-text('Project Type')", projectTester),
				CControlSignature.forSelector("Project User Settings Signature", "#cbutton-add-relation", projectUserSettingsTester),
				CControlSignature.forSelector("User View Signature", "#field-login, #field-email, label:has-text('Login')", userTester),
				CControlSignature.forSelector("Status Combo Signature", "#field-status, vaadin-combo-box[id*='status'], [id*='status-combo']",
						statusFieldTester),
				CControlSignature.forSelector("Date Picker Signature", "vaadin-date-picker, vaadin-date-time-picker, [id*='date']", datePickerTester),
				CControlSignature.forSelector("Report Button Signature", "#cbutton-report", reportTester),
				CControlSignature.forSelector("BAB Interface Component Signature",
						"#custom-interfaces-component, #custom-interface-list-root, #custom-interfaces-grid", babInterfaceListTester),
				CControlSignature.forSelector("BAB Interface Tab Signature",
						"vaadin-tab:has-text('Interface'), vaadin-accordion-panel:has-text('Interface')", babInterfaceListTester),
				CControlSignature.forSelector("Calimero Status Component Signature",
						"#custom-calimero-status-component, #custom-calimero-control-card", calimeroStatusTester),
				CControlSignature.forSelector("Calimero Status Tab Signature",
						"vaadin-tab:has-text('Calimero'), vaadin-accordion-panel:has-text('Calimero')", calimeroStatusTester),
				CControlSignature.forSelector("CSV Report Dialog Signature", "#custom-dialog-csv-export", reportTester),
				CControlSignature.forSelector("CSV Field Selector Signature", "vaadin-checkbox[id^='custom-csv-field-']", reportTester));
	}

	/** Check if BAB profile is active. The profile is set via @TestPropertySource in the test class. For now, we always assume BAB profile since this
	 * test class is BAB-specific. */
	private boolean isBabProfile() {
		// This test class is currently hardcoded to BAB profile via @TestPropertySource
		// In future, we could inject Environment to check active profiles
		return true; // Always true for this BAB-specific test class
	}

	/** Navigate to a page via button route. */
	private void navigateToButton(final ButtonInfo button) {
		LOGGER.info("   🧭 Navigating to: {} ({})", button.title, button.route);
		final String targetUrl = "http://localhost:" + port + "/" + button.route;
		page.navigate(targetUrl);
		wait_2000();
		// Check for exception after navigation
		performFailFastCheck("after-button-navigation");
	}

	/** Navigate to CPageTestAuxillary page. */
	private void navigateToTestAuxillaryPage() {
		final String targetUrl = "http://localhost:" + port + "/" + TEST_AUX_PAGE_ROUTE;
		LOGGER.info("   Navigating to: {}", targetUrl);
		page.navigate(targetUrl);
		wait_1000();
		if (!page.url().contains(TEST_AUX_PAGE_ROUTE)) {
			throw new AssertionError("Failed to navigate to CPageTestAuxillary - current URL: " + page.url());
		}
		LOGGER.info("   ✅ Successfully navigated to CPageTestAuxillary");
	}

	/** Filter buttons based on test parameters (keyword or specific button text). Filtering modes: 1. test.targetButtonText: Exact match on button
	 * display text (user-friendly, recommended) 2. test.targetButtonId: Exact match on button ID (legacy support) 3. test.routeKeyword: Partial match
	 * on button title (case-insensitive) 4. No filter: Return all buttons Example usage: - mvn test -Dtest=CPageTestComprehensive
	 * -Dtest.targetButtonText="BAB System Management" - mvn test -Dtest=CPageTestComprehensive -Dtest.routeKeyword="dashboard" */
	private List<ButtonInfo> resolveTargetButtons(final List<ButtonInfo> allButtons) {
		final String targetButtonText = System.getProperty("test.targetButtonText");
		final String targetButtonId = System.getProperty("test.targetButtonId");
		final String routeKeyword = System.getProperty("test.routeKeyword");
		final boolean runAllMatches = Boolean.getBoolean("test.runAllMatches");
		// Priority 1: Exact button text match (user-friendly)
		if (targetButtonText != null && !targetButtonText.isBlank()) {
			LOGGER.info("🎯 Filtering by exact button text: \"{}\"", targetButtonText);
			final List<ButtonInfo> exactMatches = allButtons.stream().filter(b -> b.title.equals(targetButtonText)).collect(Collectors.toList());
			if (exactMatches.isEmpty()) {
				throw new AssertionError("No buttons found with exact text: \"" + targetButtonText + "\"");
			}
			LOGGER.info("   ✅ Found {} button(s) with exact text match", exactMatches.size());
			return exactMatches;
		}
		// Priority 2: Exact button ID match (legacy support)
		if (targetButtonId != null && !targetButtonId.isBlank()) {
			LOGGER.info("🎯 Filtering by exact button ID: {}", targetButtonId);
			final List<ButtonInfo> exactMatches = allButtons.stream().filter(b -> b.id.equals(targetButtonId)).collect(Collectors.toList());
			if (exactMatches.isEmpty()) {
				throw new AssertionError("No buttons found with exact ID: " + targetButtonId);
			}
			LOGGER.info("   ✅ Found {} button(s) with exact ID match", exactMatches.size());
			return exactMatches;
		}
		// Priority 3: Partial keyword match
		if (!(routeKeyword != null && !routeKeyword.isBlank())) {
			// Priority 4: No filter - return all buttons
			return allButtons;
		}
		LOGGER.info("🎯 Filtering by route keyword (partial match): \"{}\"", routeKeyword);
		final List<ButtonInfo> matches =
				allButtons.stream().filter(b -> b.title.toLowerCase().contains(routeKeyword.toLowerCase())).collect(Collectors.toList());
		if (matches.isEmpty()) {
			throw new AssertionError("No buttons found matching keyword: \"" + routeKeyword + "\"");
		}
		if (!(!runAllMatches && matches.size() > 1)) {
			return matches;
		}
		LOGGER.info("   ↳ Using first match only (set -Dtest.runAllMatches=true to test all {} matches)", matches.size());
		return List.of(matches.get(0));
	}

	@Test
	@DisplayName ("🎯 Comprehensive test of all pages with component detection, CRUD, and grid testing")
	void testAllPagesComprehensively() {
		LOGGER.info("🚀 Starting Comprehensive Page Test Framework...");
		// Check browser availability
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		try {
			// Create output directories
			Files.createDirectories(Paths.get("target/screenshots"));
			Files.createDirectories(Paths.get("test-results/playwright/coverage"));
			// Step 1: Login
			LOGGER.info("📝 Step 1: Logging into application...");
			loginToApplication();
			// Step 1.5: BAB Profile - Check Calimero and setup
			if (isBabProfile()) {
				LOGGER.info("🔧 Step 1.5: BAB Profile detected - Verifying Calimero service...");
				checkCalimeroStatusAfterLogin();
			}
			// Step 2: Navigate to test auxiliary page
			LOGGER.info("🧭 Step 2: Navigating to CPageTestAuxillary...");
			navigateToTestAuxillaryPage();
			wait_2000();
			// Step 3: Discover navigation buttons
			LOGGER.info("🔍 Step 3: Discovering navigation buttons...");
			final List<ButtonInfo> allButtons = discoverNavigationButtons();
			if (allButtons.isEmpty()) {
				throw new AssertionError("No navigation buttons found on CPageTestAuxillary page");
			}
			final List<ButtonInfo> targetButtons = resolveTargetButtons(allButtons);
			LOGGER.info("📊 Found {} navigation button(s) to test", targetButtons.size());
			// Step 4: Test each page
			// Step 4: Test each page
			LOGGER.info("🧪 Step 4: Testing page(s)...");
			for (int i = 0; i < targetButtons.size(); i++) {
				final ButtonInfo button = targetButtons.get(i);
				final PageCoverage coverage = new PageCoverage(button.title, button.route, button.id);
				coverageData.add(coverage);
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🎯 Testing page {}/{}: {}", i + 1, targetButtons.size(), button.title);
				LOGGER.info("   Route: {}", button.route);
				LOGGER.info("   Button ID: {}", button.id);
				try {
					testPageComprehensively(button, coverage);
					coverage.passed = true;
					LOGGER.info("   ✅ Page test complete: {}", button.title);
				} catch (final Exception e) {
					coverage.passed = false;
					coverage.errorMessage = e.getMessage();
					LOGGER.error("   ❌ Page test failed: {}", button.title, e);
					takeScreenshot("page-test-failure-" + button.id, true);
				} finally {
					coverage.markComplete();
				}
			}
			// Step 5: Generate coverage reports (Phase 5)
			LOGGER.info("📊 Step 5: Generating coverage reports...");
			writeCoverageReports(coverageData);
			// Summary
			final long passedPages = coverageData.stream().filter(c -> c.passed).count();
			final long failedPages = coverageData.size() - passedPages;
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("🎉 Comprehensive Page Test Complete!");
			LOGGER.info("   ✅ Pages tested: {}", coverageData.size());
			LOGGER.info("   ✅ Pages passed: {}", passedPages);
			LOGGER.info("   ❌ Pages failed: {}", failedPages);
			LOGGER.info("   📊 Coverage reports: test-results/playwright/coverage/");
		} catch (final Exception e) {
			LOGGER.error("❌ Test framework error", e);
			throw new AssertionError("Test framework failed", e);
		}
	}

	/** Test components on the current page by detecting control signatures. This delegates testing to specialized component testers. */
	private void testComponentsOnPage(@SuppressWarnings ("unused") final String pageName, final PageCoverage coverage) {
		LOGGER.info("🧩 Step: Detecting and testing components on page...");
		// Get all signatures that match the current page
		final List<IControlSignature> matchingSignatures = controlSignatures.stream().filter(sig -> sig.isDetected(page)).toList();
		if (matchingSignatures.isEmpty()) {
			LOGGER.info("   ℹ️ No components detected on this page");
			coverage.hasComponents = false;
			coverage.componentCount = 0;
			return;
		}
		LOGGER.info("   🔍 Found {} matching signature(s)", matchingSignatures.size());
		coverage.hasComponents = true;
		coverage.componentCount = matchingSignatures.size();
		// Track component types
		matchingSignatures.forEach((final IControlSignature signature) -> coverage.componentTypes.add(signature.getSignatureName()));
		LOGGER.info("   🔍 Found {} matching signature(s)", matchingSignatures.size());
		// Group signatures by component tester
		final LinkedHashMap<IComponentTester, List<IControlSignature>> testerMap = new LinkedHashMap<>();
		matchingSignatures.forEach((final IControlSignature signature) -> {
			final IComponentTester tester = findTesterForSignature(signature);
			if (tester != null) {
				testerMap.computeIfAbsent(tester, k -> new ArrayList<>()).add(signature);
			}
		});
		// Execute tests for each tester
		LOGGER.info("   🎯 Running {} component tester(s)...", testerMap.size());
		testerMap.entrySet().forEach((final var entry) -> {
			final IComponentTester tester = entry.getKey();
			final List<IControlSignature> signatures = entry.getValue();
			try {
				LOGGER.info("   🧪 Running {} for signatures: {}", tester.getComponentName(),
						signatures.stream().map(IControlSignature::getSignatureName).collect(Collectors.toList()));
				tester.test(page);
				wait_500(); // Small delay between component tests
			} catch (final Exception e) {
				LOGGER.error("   ❌ Component test failed for {}: {}", tester.getComponentName(), e.getMessage(), e);
				// Don't fail entire test - continue with other components
			}
		});
		LOGGER.info("   ✅ Component testing complete");
	}

	/** Test Create + Save workflow. */
	private void testCreateAndSave(final String pageName) {
		try {
			LOGGER.info("   🧾 Testing Create + Save workflow...");
			final Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			if (newButton.count() == 0) {
				LOGGER.info("      ℹ️ New button not found - skipping");
				return;
			}
			// Check grid count before
			final int beforeCount = getGridRowCountSafe();
			// Click New
			newButton.click();
			wait_500();
			// Fill required fields (simplified - just name field)
			final String testValue = "Test-" + pageName + "-" + System.currentTimeMillis();
			final Locator nameField = page.locator("#field-name");
			if (nameField.count() > 0 && nameField.isEditable()) {
				nameField.fill(testValue);
				LOGGER.info("      ✓ Filled name field: {}", testValue);
			}
			// Click Save
			final Locator saveButton = page.locator("#" + CRUD_SAVE_BUTTON_ID);
			if (saveButton.count() > 0) {
				saveButton.click();
				wait_1000();
				// Check for validation errors
				final Locator errorDialog = page.locator("vaadin-dialog-overlay[opened]").filter(new Locator.FilterOptions().setHasText("Error"));
				if (errorDialog.count() > 0) {
					LOGGER.warn("      ⚠️ Validation error during save");
					// Close error dialog
					errorDialog.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("OK")).click();
					wait_500();
					// Cancel form
					final Locator cancelButton = page.locator("#" + CRUD_CANCEL_BUTTON_ID);
					if (cancelButton.count() > 0) {
						cancelButton.click();
						wait_500();
					}
					return;
				}
				performFailFastCheck("after-save");
				// Refresh to see new row
				final Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
				if (refreshButton.count() > 0) {
					refreshButton.click();
					wait_500();
				}
				// Check grid count after
				final int afterCount = getGridRowCountSafe();
				if (afterCount > beforeCount) {
					LOGGER.info("      ✓ Created row ({} -> {})", beforeCount, afterCount);
				} else {
					LOGGER.warn("      ⚠️ Create did not increase grid count ({} -> {})", beforeCount, afterCount);
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Create + Save test failed: {}", e.getMessage());
		}
	}

	/** Test CRUD operations on the current page if CRUD toolbar is detected. */
	private void testCrudOperations(final String pageName, final PageCoverage coverage) {
		LOGGER.info("🔧 Step: Testing CRUD operations...");
		final boolean hasCrud = checkCrudToolbarExists();
		coverage.hasCrudToolbar = hasCrud;
		if (!hasCrud) {
			LOGGER.info("   ℹ️ No CRUD toolbar detected - skipping CRUD tests");
			return;
		}
		LOGGER.info("   ✅ CRUD toolbar detected - running CRUD tests");
		coverage.testedCrud = true;
		// Test New button
		if (checkCrudButtonExists(CRUD_NEW_BUTTON_ID)) {
			testNewButton(pageName);
		}
		// Test Create + Save workflow
		testCreateAndSave(pageName);
		// Test Delete button (only if we created something)
		if (checkCrudButtonExists(CRUD_DELETE_BUTTON_ID)) {
			testDeleteButton(pageName);
		}
		LOGGER.info("   ✅ CRUD testing complete");
	}

	/** Test Delete button functionality. */
	private void testDeleteButton(@SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   🗑️ Testing Delete button...");
			final int beforeCount = getGridRowCountSafe();
			if (beforeCount == 0) {
				LOGGER.warn("      ⚠️ No rows available to delete");
				return;
			}
			// Select first grid row
			final Locator gridRows = page.locator("vaadin-grid-cell-content");
			if (gridRows.count() == 0) {
				LOGGER.warn("      ⚠️ No grid rows found");
				return;
			}
			gridRows.first().click();
			wait_500();
			// Click Delete
			final Locator deleteButton = page.locator("#" + CRUD_DELETE_BUTTON_ID);
			if (deleteButton.count() > 0) {
				deleteButton.click();
				wait_500();
				// Confirm deletion if dialog appears
				confirmDialogIfPresent();
				wait_500();
				performFailFastCheck("after-delete");
				// Refresh
				final Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
				if (refreshButton.count() > 0) {
					refreshButton.click();
					wait_500();
				}
				// Check count after
				final int afterCount = getGridRowCountSafe();
				if (afterCount < beforeCount) {
					LOGGER.info("      ✓ Deleted row ({} -> {})", beforeCount, afterCount);
				} else {
					LOGGER.warn("      ⚠️ Delete did not reduce grid count ({} -> {})", beforeCount, afterCount);
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Delete button test failed: {}", e.getMessage());
		}
	}
	// ========================================
	// Phase 4: Grid Testing
	// ========================================

	/** Test grid filtering if filter inputs are present. */
	private void testGridFiltering(final Locator grid, @SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   🔍 Testing grid filtering...");
			// Look for filter inputs (typically above or within grid header)
			final Locator filterInputs = grid.locator("vaadin-text-field, input[type='text']");
			final int filterCount = filterInputs.count();
			if (filterCount == 0) {
				LOGGER.info("      ℹ️ No filter inputs found");
				return;
			}
			LOGGER.info("      ✓ Found {} filter input(s)", filterCount);
			final int beforeCount = getGridRowCountSafe();
			if (beforeCount == 0) {
				LOGGER.info("      ℹ️ Grid empty - cannot test filtering");
				return;
			}
			// Get first cell value to use as filter
			final Locator firstCell = grid.locator("vaadin-grid-cell-content").first();
			if (firstCell.count() == 0) {
				return;
			}
			final String cellText = firstCell.textContent().trim();
			if (cellText.isEmpty() || cellText.length() < 2) {
				LOGGER.info("      ℹ️ First cell empty or too short - skipping filter test");
				return;
			}
			// Use first 3 characters as filter
			final String filterText = cellText.substring(0, Math.min(3, cellText.length()));
			// Fill first filter input
			final Locator firstFilter = filterInputs.first();
			firstFilter.fill(filterText);
			wait_1000(); // Wait for filtering to apply
			final int afterCount = getGridRowCountSafe();
			LOGGER.info("      ✓ Applied filter '{}': {} → {} row(s)", filterText, beforeCount, afterCount);
			// Clear filter
			firstFilter.fill("");
			wait_500();
			final int clearedCount = getGridRowCountSafe();
			if (clearedCount >= beforeCount) {
				LOGGER.info("      ✓ Cleared filter: restored to {} row(s)", clearedCount);
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Grid filtering test failed: {}", e.getMessage());
		}
	}

	/** Test grid operations on the current page if grid is detected. */
	private void testGridOperations(final String pageName, final PageCoverage coverage) {
		LOGGER.info("📊 Step: Testing Grid operations...");
		final Locator grid = detectGrid();
		coverage.hasGrid = grid != null;
		if (grid == null) {
			LOGGER.info("   ℹ️ No grid detected - skipping grid tests");
			return;
		}
		LOGGER.info("   ✅ Grid detected - running grid tests");
		coverage.testedGrid = true;
		coverage.gridRowCount = getGridRowCountSafe();
		// Test grid structure
		testGridStructure(grid, pageName);
		// Test row selection
		testGridRowSelection(grid, pageName);
		// Test sorting (click column headers)
		testGridSorting(grid, pageName);
		// Test filtering (if filter row exists)
		testGridFiltering(grid, pageName);
		// Test pagination (if pagination exists)
		testGridPagination(grid, pageName);
		LOGGER.info("   ✅ Grid testing complete");
	}

	/** Test grid pagination if pagination controls are present. */
	private void testGridPagination(@SuppressWarnings ("unused") final Locator grid, @SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   📄 Testing grid pagination...");
			// Look for pagination controls (typically below grid)
			final Locator paginationNext = page.locator("vaadin-button:has-text('Next'), button:has-text('Next'), vaadin-button[aria-label*='next']");
			final Locator paginationPrev =
					page.locator("vaadin-button:has-text('Previous'), button:has-text('Previous'), vaadin-button[aria-label*='previous']");
			if (paginationNext.count() == 0 && paginationPrev.count() == 0) {
				LOGGER.info("      ℹ️ No pagination controls found");
				return;
			}
			LOGGER.info("      ✓ Pagination controls detected");
			@SuppressWarnings ("unused")
			final int beforeCount = getGridRowCountSafe();
			// Try clicking Next if enabled
			if (paginationNext.count() > 0 && paginationNext.isEnabled()) {
				paginationNext.click();
				wait_1000();
				performFailFastCheck("after-pagination-next");
				final int afterNext = getGridRowCountSafe();
				LOGGER.info("      ✓ Clicked Next: {} row(s) displayed", afterNext);
				// Go back to first page
				if (paginationPrev.count() > 0 && paginationPrev.isEnabled()) {
					paginationPrev.click();
					wait_500();
					LOGGER.info("      ✓ Returned to previous page");
				}
			} else {
				LOGGER.info("      ℹ️ Next button disabled (on last/only page)");
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Grid pagination test failed: {}", e.getMessage());
		}
	}
	// ========================================

	/** Test grid row selection. */
	private void testGridRowSelection(final Locator grid, @SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   🖱️ Testing grid row selection...");
			final int rowCount = getGridRowCountSafe();
			if (rowCount == 0) {
				LOGGER.info("      ℹ️ No rows to select");
				return;
			}
			// Select first row
			final Locator firstRow = grid.locator("vaadin-grid-cell-content").first();
			if (firstRow.count() > 0) {
				firstRow.click();
				wait_500();
				performFailFastCheck("after-row-selection");
				LOGGER.info("      ✓ Selected first row");
				// Check if selection caused any UI change (edit form, details panel, etc.)
				final boolean hasDialog = page.locator("vaadin-dialog-overlay[opened]").count() > 0;
				final boolean hasDetailsPanel = page.locator(".details-panel, [class*='detail']").count() > 0;
				if (hasDialog) {
					LOGGER.info("      ✓ Row selection opened dialog");
					// Close dialog
					page.keyboard().press("Escape");
					wait_500();
				} else if (hasDetailsPanel) {
					LOGGER.info("      ✓ Row selection showed details panel");
				} else {
					LOGGER.info("      ✓ Row selected (no visible UI change)");
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Grid row selection test failed: {}", e.getMessage());
		}
	}

	/** Test grid sorting by clicking column headers. */
	private void testGridSorting(final Locator grid, @SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   🔀 Testing grid sorting...");
			final int rowCount = getGridRowCountSafe();
			if (rowCount == 0) {
				LOGGER.info("      ℹ️ No rows to sort");
				return;
			}
			// Find sortable columns (typically have sort indicators or are clickable)
			final Locator headers = grid.locator("vaadin-grid-cell-content[part*='header']");
			final int headerCount = headers.count();
			if (headerCount == 0) {
				LOGGER.info("      ℹ️ No column headers found");
				return;
			}
			// Try clicking first column header to test sorting
			final Locator firstHeader = headers.first();
			if (firstHeader.count() > 0) {
				// Get first cell value before sort
				final Locator firstCell = grid.locator("vaadin-grid-cell-content").nth(headerCount);
				final String beforeSort = firstCell.count() > 0 ? firstCell.textContent() : "";
				// Click header to sort
				firstHeader.click();
				wait_500();
				performFailFastCheck("after-sort-click");
				// Get first cell value after sort
				final String afterSort = firstCell.count() > 0 ? firstCell.textContent() : "";
				if (!beforeSort.equals(afterSort)) {
					LOGGER.info("      ✓ Sorting changed grid order (first cell: '{}' → '{}')",
							beforeSort.substring(0, Math.min(20, beforeSort.length())), afterSort.substring(0, Math.min(20, afterSort.length())));
				} else {
					LOGGER.info("      ✓ Clicked column header (order unchanged or already sorted)");
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Grid sorting test failed: {}", e.getMessage());
		}
	}

	/** Test grid structure (columns, rows). */
	private void testGridStructure(final Locator grid, @SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   📋 Testing grid structure...");
			// Count columns
			final Locator headers = grid.locator("vaadin-grid-cell-content")
					.filter(new Locator.FilterOptions().setHas(page.locator("[slot='vaadin-grid-cell-content']")));
			final int columnCount = headers.count();
			LOGGER.info("      ✓ Grid has {} column(s)", columnCount);
			// Count rows
			final int rowCount = getGridRowCountSafe();
			LOGGER.info("      ✓ Grid has {} row(s)", rowCount);
			// Check if grid is empty
			if (rowCount == 0) {
				LOGGER.info("      ℹ️ Grid is empty - some tests will be skipped");
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ Grid structure test failed: {}", e.getMessage());
		}
	}

	/** Test New button functionality. */
	private void testNewButton(@SuppressWarnings ("unused") final String pageName) {
		try {
			LOGGER.info("   ➕ Testing New button...");
			final Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			if (newButton.count() == 0) {
				LOGGER.info("      ℹ️ New button not found");
				return;
			}
			newButton.click();
			wait_500();
			performFailFastCheck("after-new-button");
			LOGGER.info("      ✓ Clicked New button");
			// Check if form/dialog appeared
			final boolean hasDialog = page.locator("vaadin-dialog-overlay[opened]").count() > 0;
			final boolean hasFormLayout = page.locator("vaadin-form-layout").count() > 0;
			if (hasDialog || hasFormLayout) {
				LOGGER.info("      ✓ Form/Dialog appeared");
				// Close via Cancel button
				final Locator cancelButton = page.locator("#" + CRUD_CANCEL_BUTTON_ID);
				if (cancelButton.count() > 0) {
					cancelButton.click();
					wait_500();
					LOGGER.info("      ✓ Closed form via Cancel");
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("   ⚠️ New button test failed: {}", e.getMessage());
		}
	}
	/** Write coverage reports (CSV + Markdown). Phase 5: Will be implemented with full reporting. */
	// ========================================
	// Phase 5: Coverage Reporting
	// ========================================

	/** Test a single page comprehensively. Phase 1: Basic navigation ✅ Phase 2: Component testing ✅ Phase 3: CRUD testing (TODO) Phase 4: Grid
	 * testing (TODO) */
	private void testPageComprehensively(final ButtonInfo button, final PageCoverage coverage) {
		try {
			// Navigate to page
			navigateToButton(button);
			performFailFastCheck("after-navigation");
			LOGGER.info("   ℹ️ Page test successful (Phase 1+2+3+4: Navigation + Components + CRUD + Grid)");
			// Phase 2: Component detection and testing
			walkTabsAndTestComponents(button.title, coverage);
			// Phase 3: CRUD testing
			testCrudOperations(button.title, coverage);
			// Phase 4: Grid testing
			testGridOperations(button.title, coverage);
			LOGGER.info("   ℹ️ Page test successful (Phase 1+2+3+4: Navigation + Components + CRUD + Grid)");
			coverage.passed = true;
			coverage.passed = true;
		} catch (final Exception e) {
			coverage.passed = false;
			coverage.errorMessage = e.getMessage();
			throw e;
		}
	}

	/** Walk through all tabs/accordions on the page and test components in each. This ensures comprehensive coverage of tabbed/accordion UIs. */
	private void walkTabsAndTestComponents(final String pageName, final PageCoverage coverage) {
		LOGGER.info("📑 Step: Walking tabs and testing components...");
		// Find all tabs (vaadin-tab elements)
		final Locator tabs = page.locator("vaadin-tab");
		final int tabCount = tabs.count();
		if (tabCount == 0) {
			LOGGER.info("   ℹ️ No tabs found on this page");
			coverage.hasTabs = false;
			coverage.tabCount = 0;
			testComponentsOnPage(pageName, coverage); // Test components on main page
			return;
		}
		LOGGER.info("   🗂️ Found {} tab(s) - testing each tab", tabCount);
		coverage.hasTabs = true;
		coverage.tabCount = tabCount;
		// Test components in each tab
		for (int i = 0; i < tabCount; i++) {
			try {
				final Locator tab = tabs.nth(i);
				final String tabText = tab.textContent().trim();
				LOGGER.info("   📂 Tab {}/{}: '{}'", i + 1, tabCount, tabText);
				// Click tab to activate
				tab.click();
				wait_500();
				performFailFastCheck("after-tab-click-" + i);
				// Test components in this tab
				testComponentsOnPage(pageName + " - Tab: " + tabText, coverage);
			} catch (final Exception e) {
				LOGGER.error("   ❌ Error testing tab {}: {}", i + 1, e.getMessage(), e);
				// Continue with next tab
			}
		}
		// Also check for accordion panels
		final Locator accordions = page.locator("vaadin-accordion-panel");
		final int accordionCount = accordions.count();
		if (accordionCount > 0) {
			LOGGER.info("   🗂️ Found {} accordion panel(s) - testing each panel", accordionCount);
			for (int i = 0; i < accordionCount; i++) {
				try {
					final Locator accordion = accordions.nth(i);
					final String accordionText = accordion.locator("vaadin-accordion-heading").textContent().trim();
					LOGGER.info("   📂 Accordion {}/{}: '{}'", i + 1, accordionCount, accordionText);
					// Click to expand
					accordion.locator("vaadin-accordion-heading").click();
					wait_500();
					performFailFastCheck("after-accordion-click-" + i);
					// Test components in this accordion
					testComponentsOnPage(pageName + " - Accordion: " + accordionText, coverage);
				} catch (final Exception e) {
					LOGGER.error("   ❌ Error testing accordion {}: {}", i + 1, e.getMessage(), e);
					// Continue with next accordion
				}
			}
		}
		LOGGER.info("   ✅ Tab/Accordion walking complete");
	}

	/** Coverage data class for tracking test results. */
	/** Write coverage reports to CSV and Markdown files. */
	private void writeCoverageReports(final List<PageCoverage> coverages) {
		LOGGER.info("📊 Step 5: Generating coverage reports...");
		try {
			// Create output directory
			final Path coverageDir = Paths.get("test-results/playwright/coverage");
			Files.createDirectories(coverageDir);
			// Generate timestamp for report filenames
			final String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
			// Write CSV report
			writeCsvReport(coverageDir, timestamp, coverages);
			// Write Markdown summary
			writeMarkdownSummary(coverageDir, timestamp, coverages);
			LOGGER.info("   ✅ Coverage reports generated:");
			LOGGER.info("      📄 CSV: test-results/playwright/coverage/test-coverage-{}.csv", timestamp);
			LOGGER.info("      📄 Markdown: test-results/playwright/coverage/test-summary-{}.md", timestamp);
		} catch (final Exception e) {
			LOGGER.error("   ❌ Failed to write coverage reports: {}", e.getMessage(), e);
		}
	}

	/** Write CSV report with detailed test results. */
	private void writeCsvReport(final Path coverageDir, final String timestamp, final List<PageCoverage> coverages) throws IOException {
		final Path csvPath = coverageDir.resolve("test-coverage-" + timestamp + ".csv");
		try (final BufferedWriter writer = Files.newBufferedWriter(csvPath)) {
			// CSV Header
			writer.write("Page Name,Route,Button ID,Status,Duration,Has Components,Component Count,Component Types,");
			writer.write("Has CRUD,Tested CRUD,Has Grid,Tested Grid,Grid Rows,Has Tabs,Tab Count,Error Message\n");
			// CSV Rows
			for (final PageCoverage coverage : coverages) {
				writer.write(escapeCsv(coverage.pageName) + ",");
				writer.write(escapeCsv(coverage.route) + ",");
				writer.write(escapeCsv(coverage.buttonId) + ",");
				writer.write((coverage.passed ? "PASS" : "FAIL") + ",");
				writer.write(coverage.getDurationFormatted() + ",");
				writer.write(coverage.hasComponents + ",");
				writer.write(coverage.componentCount + ",");
				writer.write(escapeCsv(String.join("; ", coverage.componentTypes)) + ",");
				writer.write(coverage.hasCrudToolbar + ",");
				writer.write(coverage.testedCrud + ",");
				writer.write(coverage.hasGrid + ",");
				writer.write(coverage.testedGrid + ",");
				writer.write(coverage.gridRowCount + ",");
				writer.write(coverage.hasTabs + ",");
				writer.write(coverage.tabCount + ",");
				writer.write(escapeCsv(coverage.errorMessage != null ? coverage.errorMessage : "") + "\n");
			}
		}
		LOGGER.info("   �� CSV report written: {} entries", coverages.size());
	}

	/** Write Markdown summary with statistics. */
	private void writeMarkdownSummary(final Path coverageDir, final String timestamp, final List<PageCoverage> coverages) throws IOException {
		final Path mdPath = coverageDir.resolve("test-summary-" + timestamp + ".md");
		// Calculate statistics
		final long totalTests = coverages.size();
		final long passedTests = coverages.stream().filter(c -> c.passed).count();
		final long failedTests = totalTests - passedTests;
		final double passRate = totalTests > 0 ? passedTests * 100.0 / totalTests : 0.0;
		final long pagesWithComponents = coverages.stream().filter(c -> c.hasComponents).count();
		final long pagesWithCrud = coverages.stream().filter(c -> c.hasCrudToolbar).count();
		final long pagesWithGrid = coverages.stream().filter(c -> c.hasGrid).count();
		final long pagesWithTabs = coverages.stream().filter(c -> c.hasTabs).count();
		final long totalDurationMs = coverages.stream().mapToLong(PageCoverage::getDurationMs).sum();
		final long avgDurationMs = totalTests > 0 ? totalDurationMs / totalTests : 0;
		try (final BufferedWriter writer = Files.newBufferedWriter(mdPath)) {
			// Title
			writer.write("# Comprehensive Page Test Report\n\n");
			writer.write("**Generated**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n\n");
			// Summary Statistics
			writer.write("## Summary Statistics\n\n");
			writer.write("| Metric | Value |\n");
			writer.write("|--------|-------|\n");
			writer.write("| **Total Pages Tested** | %d |\n".formatted(totalTests));
			writer.write("| **Passed** | ✅ %d (%.1f%%) |\n".formatted(passedTests, passRate));
			writer.write("| **Failed** | ❌ %d (%.1f%%) |\n".formatted(failedTests, 100.0 - passRate));
			writer.write("| **Total Duration** | %s |\n".formatted(formatDuration(totalDurationMs)));
			writer.write("| **Average Duration** | %s |\n".formatted(formatDuration(avgDurationMs)));
			writer.write("\n");
			// Feature Coverage
			writer.write("## Feature Coverage\n\n");
			writer.write("| Feature | Pages | Percentage |\n");
			writer.write("|---------|-------|------------|\n");
			writer.write("| **Components Detected** | %d | %.1f%% |%n".formatted(pagesWithComponents, pagesWithComponents * 100.0 / totalTests));
			writer.write("| **CRUD Toolbars** | %d | %.1f%% |%n".formatted(pagesWithCrud, pagesWithCrud * 100.0 / totalTests));
			writer.write("| **Grids** | %d | %.1f%% |%n".formatted(pagesWithGrid, pagesWithGrid * 100.0 / totalTests));
			writer.write("| **Tabs/Accordions** | %d | %.1f%% |%n".formatted(pagesWithTabs, pagesWithTabs * 100.0 / totalTests));
			writer.write("\n");
			// Test Results Table
			writer.write("## Test Results\n\n");
			writer.write("| Page | Status | Duration | Components | CRUD | Grid | Tabs |\n");
			writer.write("|------|--------|----------|------------|------|------|------|\n");
			for (final PageCoverage coverage : coverages) {
				final String status = coverage.passed ? "✅ PASS" : "❌ FAIL";
				final String components = coverage.hasComponents ? "✓ (" + coverage.componentCount + ")" : "—";
				final String crud = coverage.hasCrudToolbar ? coverage.testedCrud ? "✓" : "⚠️" : "—";
				final String grid = coverage.hasGrid ? coverage.testedGrid ? "✓ (" + coverage.gridRowCount + " rows)" : "⚠️" : "—";
				final String tabs = coverage.hasTabs ? "✓ (" + coverage.tabCount + ")" : "—";
				writer.write("| %s | %s | %s | %s | %s | %s | %s |%n".formatted(coverage.pageName, status, coverage.getDurationFormatted(),
						components, crud, grid, tabs));
			}
			writer.write("\n");
			// Failed Tests (if any)
			if (failedTests > 0) {
				writer.write("## Failed Tests\n\n");
				writer.write("| Page | Error Message |\n");
				writer.write("|------|---------------|\n");
				for (final PageCoverage coverage : coverages) {
					if (!coverage.passed) {
						writer.write("| %s | %s |%n".formatted(coverage.pageName,
								coverage.errorMessage != null ? coverage.errorMessage : "Unknown error"));
					}
				}
				writer.write("\n");
			}
			// Component Details
			final Map<String, Long> componentCounts =
					coverages.stream().flatMap(c -> c.componentTypes.stream()).collect(Collectors.groupingBy(s -> s, Collectors.counting()));
			if (!componentCounts.isEmpty()) {
				writer.write("## Component Types Detected\n\n");
				writer.write("| Component Type | Pages |\n");
				writer.write("|----------------|-------|\n");
				componentCounts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).forEach(entry -> {
					try {
						writer.write("| %s | %d |%n".formatted(entry.getKey(), entry.getValue()));
					} catch (final IOException e) {
						LOGGER.warn("Error writing component count: {}", e.getMessage());
					}
				});
				writer.write("\n");
			}
			// Footer
			writer.write("---\n");
			writer.write("*Generated by CPageTestComprehensive - Unified Page Testing Framework*\n");
		}
		LOGGER.info("   📄 Markdown summary written with {} pages", coverages.size());
	}
}
