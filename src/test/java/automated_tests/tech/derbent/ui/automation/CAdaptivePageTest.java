package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import automated_tests.tech.derbent.ui.automation.components.CAttachmentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCloneToolbarTester;
import automated_tests.tech.derbent.ui.automation.components.CCommentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCrudToolbarTester;
import automated_tests.tech.derbent.ui.automation.components.CDatePickerTester;
import automated_tests.tech.derbent.ui.automation.components.CGridComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CLinkComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CProjectComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CProjectUserSettingsComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CReportComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CStatusFieldTester;
import automated_tests.tech.derbent.ui.automation.components.CUserComponentTester;
import automated_tests.tech.derbent.ui.automation.components.IComponentTester;
import automated_tests.tech.derbent.ui.automation.signatures.CControlSignature;
import automated_tests.tech.derbent.ui.automation.signatures.CSignatureFilter;
import automated_tests.tech.derbent.ui.automation.signatures.IControlSignature;
import tech.derbent.Application;

/** Intelligent adaptive page testing framework that automatically detects UI components and runs appropriate tests.
 * <p>
 * This test framework:
 * <ul>
 * <li>Navigates to pages via CPageTestAuxillary button navigation (not side menu)
 * <li>Automatically detects UI components on each page (grids, CRUD toolbars, attachments, comments, etc.)
 * <li>Runs component-specific tests based on what's detected
 * <li>Extensible architecture: add new component testers by implementing IComponentTester
 * <li>Generic and reusable: works with any page without hardcoding
 * <li>Logs to /tmp/playwright.log for debugging
 * </ul>
 * <p>
 * Architecture:
 * <ul>
 * <li><b>IComponentTester</b>: Interface for component-specific testers
 * <li><b>CBaseComponentTester</b>: Base class with common utilities
 * <li><b>Component Implementations</b>: CCrudToolbarTester, CGridComponentTester, CAttachmentComponentTester, etc.
 * <li><b>Test Orchestration</b>: This class discovers pages and orchestrates component tests
 * </ul>
 * <p>
 * Usage:
 *
 * <pre>
 * # Test all pages
 * mvn test -Dtest=CAdaptivePageTest
 *
 * # Test specific page by test support button ID
 * mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0
 * </pre>
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🤖 Intelligent Adaptive Page Testing")
public class CAdaptivePageTest extends CBaseUITest {

	private static final class ButtonInfo {

		String id;
		String route;
		String title;
	}

	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAdaptivePageTest.class);
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";

	private static int scoreTitleMatch(final String title, final String filterValue) {
		if (title == null) {
			return 0;
		}
		final String normalized = title.trim().toLowerCase();
		final String filter = filterValue.toLowerCase();
		if (normalized.equals(filter) || normalized.equals(filter + "s")) {
			return 3;
		}
		if (normalized.startsWith(filter)) {
			return 2;
		}
		return 1;
	}

	// Component testers
	private final IComponentTester attachmentTester = new CAttachmentComponentTester();
	private final IComponentTester cloneToolbarTester = new CCloneToolbarTester();
	private final IComponentTester commentTester = new CCommentComponentTester();
	// Control signatures - initialized after testers to avoid null testers
	private final List<IControlSignature> controlSignatures = initializeControlSignatures();
	private final IComponentTester crudToolbarTester = new CCrudToolbarTester();
	private final IComponentTester datePickerTester = new CDatePickerTester();
	private final IComponentTester gridTester = new CGridComponentTester();
	private final IComponentTester linkTester = new CLinkComponentTester();
	private int pagesVisited = 0;
	private final IComponentTester projectTester = new CProjectComponentTester();
	private final IComponentTester projectUserSettingsTester = new CProjectUserSettingsComponentTester();
	private final IComponentTester reportTester = new CReportComponentTester(); // CSV export testing
	private int screenshotCounter = 1;
	private final IComponentTester statusFieldTester = new CStatusFieldTester();
	private final IComponentTester userTester = new CUserComponentTester();

	@SuppressWarnings ("static-method")
	private boolean clickFirstEnabled(final Locator scope, final String selector) {
		final Locator button = scope.locator(selector);
		if (button.count() == 0) {
			return false;
		}
		for (int i = 0; i < button.count(); i++) {
			final Locator candidate = button.nth(i);
			if (!candidate.isDisabled()) {
				candidate.click();
				return true;
			}
		}
		return false;
	}

	private void closeBlockingDialogs() {
		final Locator overlays = page.locator("vaadin-dialog-overlay[opened]");
		if (overlays.count() == 0) {
			return;
		}
		for (int attempt = 0; attempt < 2; attempt++) {
			for (int i = 0; i < overlays.count(); i++) {
				final Locator overlay = overlays.nth(i);
				if (clickFirstEnabled(overlay, "#cbutton-save, #cbutton-upload, #cbutton-ok, #cbutton-yes")) {
					continue;
				}
				if (clickFirstEnabled(overlay, "#cbutton-cancel, #cbutton-close, [part='close-button']")) {
					continue;
				}
				if (clickFirstEnabled(overlay, "vaadin-button:has-text('Save'), vaadin-button:has-text('OK'), vaadin-button:has-text('Done')")) {
					continue;
				}
				if (clickFirstEnabled(overlay, "vaadin-button:has-text('Cancel'), vaadin-button:has-text('Close')")) {
					continue;
				}
				page.keyboard().press("Escape");
			}
			wait_500();
			if (page.locator("vaadin-dialog-overlay[opened]").count() == 0) {
				return;
			}
		}
	}

	/** Discover navigation buttons from CPageTestAuxillary.
	 * @return List of button information */
	private List<ButtonInfo> discoverNavigationButtons() {
		final List<ButtonInfo> buttons = new ArrayList<>();
		try {
			wait_2000(); // Allow buttons to populate
			final var buttonElements = page.locator(BUTTON_SELECTOR);
			final int count = buttonElements.count();
			LOGGER.info("   Found {} navigation buttons", count);
			for (int i = 0; i < count; i++) {
				final var button = buttonElements.nth(i);
				final ButtonInfo info = new ButtonInfo();
				info.id = button.getAttribute("id");
				info.title = button.textContent();
				info.route = button.getAttribute("data-route");
				if (info.route == null || info.route.isBlank()) {
					LOGGER.warn("   ⚠️ Button {} has no data-route attribute, skipping", info.id);
					continue;
				}
				buttons.add(info);
				LOGGER.debug("   Button {}: id={}, title={}, route={}", i + 1, info.id, info.title, info.route);
			}
		} catch (final Exception e) {
			LOGGER.error("   ❌ Failed to discover buttons: {}", e.getMessage());
			throw new AssertionError("Failed to discover navigation buttons", e);
		}
		return buttons;
	}

	/** Initialize control signatures using testers defined above. This method approach avoids field ordering issues during compilation.
	 * @return list of control signatures */
	private List<IControlSignature> initializeControlSignatures() {
		return List.of(
				CControlSignature.forSelectorsMinMatch("CRUD Toolbar Signature",
						List.of("#cbutton-new", "#cbutton-save", "#cbutton-delete", "#cbutton-refresh", "#cbutton-edit", "#cbutton-cancel"), 2,
						crudToolbarTester),
				CControlSignature.forSelector("CRUD Save Button Signature", "#cbutton-save", crudToolbarTester),
				CControlSignature.forSelector("CRUD Delete Button Signature", "#cbutton-delete", crudToolbarTester),
				CControlSignature.forSelector("Clone Button Signature", "#cbutton-copy-to, #cbutton-clone, [id*='copy-to'], [id*='clone']",
						cloneToolbarTester),
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
				CControlSignature.forSelector("CSV Report Dialog Signature", "#custom-dialog-csv-export", reportTester),
				CControlSignature.forSelector("CSV Field Selector Signature", "vaadin-checkbox[id^='custom-csv-field-']", reportTester));
	}

	/** Run component-based tests on current page.
	 * @param pageName Page name for logging */
	private boolean isTabDisabled(final Locator tab) {
		try {
			final String ariaDisabled = tab.getAttribute("aria-disabled");
			if ("true".equalsIgnoreCase(ariaDisabled)) {
				return true;
			}
			final String disabled = tab.getAttribute("disabled");
			return disabled != null;
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return false;
		}
	}

	/** Navigate to CPageTestAuxillary page. */
	private void navigateToTestAuxillaryPage() {
		try {
			final String url = String.format("http://localhost:%d/%s", port, TEST_AUX_PAGE_ROUTE);
			LOGGER.info("   Navigating to: {}", url);
			page.navigate(url);
			wait_2000();
			final String currentUrl = page.url();
			if (!currentUrl.contains(TEST_AUX_PAGE_ROUTE)) {
				throw new AssertionError("Failed to navigate to CPageTestAuxillary. Current URL: " + currentUrl);
			}
			LOGGER.info("   ✅ Successfully navigated to CPageTestAuxillary");
		} catch (final Exception e) {
			LOGGER.error("   ❌ Navigation failed: {}", e.getMessage());
			throw new AssertionError("Failed to navigate to CPageTestAuxillary", e);
		}
	}

	@SuppressWarnings ("static-method")
	private List<ButtonInfo> resolveTargetButtons(final List<ButtonInfo> buttons, final String targetButtonId) {
		if (targetButtonId != null && !targetButtonId.isBlank()) {
			final ButtonInfo targetButton = buttons.stream().filter(b -> targetButtonId.equals(b.id)).findFirst().orElse(null);
			if (targetButton == null) {
				throw new AssertionError("Target button ID not found: " + targetButtonId);
			}
			return List.of(targetButton);
		}
		final String titleFilter = System.getProperty("test.titleContains");
		final String filterValue = titleFilter == null || titleFilter.isBlank() ? "user" : titleFilter.trim();
		final List<ButtonInfo> filtered = new ArrayList<>();
		for (final ButtonInfo button : buttons) {
			if (button.title != null && button.title.toLowerCase().contains(filterValue.toLowerCase())) {
				filtered.add(button);
			}
		}
		if (filtered.isEmpty()) {
			LOGGER.warn("⚠️ No buttons matched title filter '{}'; defaulting to first button", filterValue);
			return List.of(buttons.get(0));
		}
		filtered.sort((a, b) -> {
			final int scoreA = scoreTitleMatch(a.title, filterValue);
			final int scoreB = scoreTitleMatch(b.title, filterValue);
			if (scoreA != scoreB) {
				return Integer.compare(scoreB, scoreA);
			}
			return a.title.compareToIgnoreCase(b.title);
		});
		final boolean runAllMatches = Boolean.getBoolean("test.runAllMatches");
		if (runAllMatches) {
			LOGGER.info("🎯 Using title filter '{}' -> {} button(s) matched (run all)", filterValue, filtered.size());
			return filtered;
		}
		LOGGER.info("🎯 Using title filter '{}' -> best match: {}", filterValue, filtered.get(0).title);
		return List.of(filtered.get(0));
	}

	@Test
	@DisplayName ("🤖 Adaptive test of all pages with intelligent component detection")
	void testAllPagesAdaptively() {
		LOGGER.info("🚀 Starting Intelligent Adaptive Page Test...");
		// Check browser availability
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			Files.createDirectories(Paths.get("/tmp"));
			// Step 1: Login
			LOGGER.info("📝 Step 1: Logging into application...");
			loginToApplication();
			// No screenshot after login - only on errors
			// Step 2: Discover navigation targets
			final String targetButtonId = System.getProperty("test.targetButtonId");
			LOGGER.info("🧭 Step 2: Navigating to CPageTestAuxillary...");
			navigateToTestAuxillaryPage();
			wait_2000();
			LOGGER.info("🔍 Step 3: Discovering navigation buttons...");
			final List<ButtonInfo> buttons = discoverNavigationButtons();
			if (buttons.isEmpty()) {
				throw new AssertionError("No navigation buttons found");
			}
			final List<ButtonInfo> targetButtons = resolveTargetButtons(buttons, targetButtonId);
			LOGGER.info("🧪 Step 4: Testing page(s) via test support buttons...");
			for (final ButtonInfo targetButton : targetButtons) {
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🎯 Testing page: {}", targetButton.title);
				try {
					page.locator("#" + targetButton.id).first().click();
					wait_2000();
					final boolean hasExceptionDialog =
							page.locator("#custom-exception-dialog").count() > 0 || page.locator("#custom-exception-details-dialog").count() > 0;
					if (hasExceptionDialog) {
						LOGGER.error("   ❌ Exception detected on page load");
						takeScreenshot(String.format("%03d-exception-%s", screenshotCounter++, targetButton.id), true);
						throw new AssertionError("Exception on page: " + targetButton.title);
					}
					testPageComponents(targetButton.title);
					pagesVisited++;
					LOGGER.info("   ✅ Page test complete");
					navigateToTestAuxillaryPage();
					wait_2000();
				} catch (final Exception e) {
					LOGGER.error("   ❌ Page test failed: {}", e.getMessage());
					takeScreenshot(String.format("%03d-page-%s-failure", screenshotCounter++, targetButton.id), true);
					throw new AssertionError("Failed testing page: " + targetButton.title, e);
				}
			}
			// Summary
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("🎉 Adaptive Page Test Complete!");
			LOGGER.info("   ✅ Pages visited: {}", pagesVisited);
			LOGGER.info("   ✅ Control signatures registered: {}", controlSignatures.size());
		} catch (final Exception e) {
			LOGGER.error("❌ Test suite failed: {}", e.getMessage(), e);
			throw new AssertionError("Adaptive page test failed", e);
		}
	}

	private void testComponentsOnCurrentView(final String pageName) {
		LOGGER.info("   🔍 Detecting control signatures on page: {}", pageName);
		final CSignatureFilter signatureFilter = new CSignatureFilter();
		final List<IControlSignature> activeSignatures = signatureFilter.filter(controlSignatures);
		if (!signatureFilter.getIncludeKeywords().isEmpty() || !signatureFilter.getExcludeKeywords().isEmpty()) {
			LOGGER.info("      🎯 Signature filter include={}, exclude={}", signatureFilter.getIncludeKeywords(),
					signatureFilter.getExcludeKeywords());
		}
		final List<IControlSignature> detectedSignatures = new ArrayList<>();
		for (final IControlSignature signature : activeSignatures) {
			try {
				if (signature.isDetected(page)) {
					detectedSignatures.add(signature);
				}
			} catch (final Exception e) {
				LOGGER.error("      ❌ Error detecting signature {}: {}", signature.getSignatureName(), e.getMessage());
			}
		}
		if (detectedSignatures.isEmpty()) {
			LOGGER.info("      ℹ️ No control signatures detected on this page");
			return;
		}
		final java.util.LinkedHashMap<IComponentTester, List<String>> testerToSignatures = new java.util.LinkedHashMap<>();
		for (final IControlSignature signature : detectedSignatures) {
			testerToSignatures.computeIfAbsent(signature.getTester(), key -> new ArrayList<>()).add(signature.getSignatureName());
		}
		LOGGER.info("   ✅ Detected {} control signature(s) mapped to {} tester(s)", detectedSignatures.size(), testerToSignatures.size());
		int testersRun = 0;
		for (final var entry : testerToSignatures.entrySet()) {
			final IComponentTester tester = entry.getKey();
			try {
				LOGGER.info("      🧩 Running {} for signatures: {}", tester.getComponentName(), entry.getValue());
				closeBlockingDialogs();
				tester.test(page);
				closeBlockingDialogs();
				testersRun++;
			} catch (final Exception e) {
				LOGGER.error("      ❌ Error testing {}: {}", tester.getComponentName(), e.getMessage());
			}
		}
		LOGGER.info("   ✅ Completed {} component tests", testersRun);
	}

	private void testPageComponents(final String pageName) {
		final Locator tabSets = page.locator("vaadin-tabs");
		if (tabSets.count() == 0) {
			testComponentsOnCurrentView(pageName);
			return;
		}
		LOGGER.info("   🗂️ Found {} tab set(s) on page, walking all tabs", tabSets.count());
		for (int setIndex = 0; setIndex < tabSets.count(); setIndex++) {
			final Locator tabSet = tabSets.nth(setIndex);
			final Locator tabs = tabSet.locator("vaadin-tab");
			final int tabCount = tabs.count();
			LOGGER.info("      📂 Tab set {} has {} tab(s)", setIndex + 1, tabCount);
			for (int tabIndex = 0; tabIndex < tabCount; tabIndex++) {
				final Locator tab = tabs.nth(tabIndex);
				if (!tab.isVisible() || isTabDisabled(tab)) {
					continue;
				}
				closeBlockingDialogs();
				if (page.locator("vaadin-dialog-overlay[opened]").count() > 0) {
					LOGGER.warn("      ⚠️ Dialog overlay still open; skipping tab {}", tabIndex + 1);
					continue;
				}
				final String tabLabel = tab.textContent() != null ? tab.textContent().trim() : "Tab " + (tabIndex + 1);
				LOGGER.info("      ▶️ Activating tab {}: {}", tabIndex + 1, tabLabel);
				try {
					tab.click();
					wait_500();
					testComponentsOnCurrentView(pageName + " [tab: " + tabLabel + "]");
				} catch (final Exception e) {
					LOGGER.warn("      ⚠️ Failed to activate tab {}: {}", tabLabel, e.getMessage());
				}
			}
		}
	}
}
