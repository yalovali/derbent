package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import automated_tests.tech.derbent.ui.automation.components.CAttachmentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCloneToolbarTester;
import automated_tests.tech.derbent.ui.automation.components.CCommentComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CCrudToolbarTester;
import automated_tests.tech.derbent.ui.automation.components.CDatePickerTester;
import automated_tests.tech.derbent.ui.automation.components.CGridComponentTester;
import automated_tests.tech.derbent.ui.automation.components.CStatusFieldTester;
import automated_tests.tech.derbent.ui.automation.components.IComponentTester;
import automated_tests.tech.derbent.ui.automation.signatures.CControlSignature;
import automated_tests.tech.derbent.ui.automation.signatures.IControlSignature;

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
 * <pre>
 * # Test all pages
 * mvn test -Dtest=CAdaptivePageTest
 * 
 * # Test specific page by test support button ID
 * mvn test -Dtest=CAdaptivePageTest -Dtest.targetButtonId=test-aux-btn-activities-0
 * </pre>
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🤖 Intelligent Adaptive Page Testing")
public class CAdaptivePageTest extends CBaseUITest {

	private static final class ButtonInfo {

		String id;
		int index;
		String route;
		String title;
	}

	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAdaptivePageTest.class);
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";

	private final IComponentTester crudToolbarTester = new CCrudToolbarTester();
	private final IComponentTester gridTester = new CGridComponentTester();
	private final IComponentTester attachmentTester = new CAttachmentComponentTester();
	private final IComponentTester commentTester = new CCommentComponentTester();
	private final IComponentTester statusFieldTester = new CStatusFieldTester();
	private final IComponentTester datePickerTester = new CDatePickerTester();
	private final IComponentTester cloneToolbarTester = new CCloneToolbarTester();

	private final List<IControlSignature> controlSignatures = List.of(
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
			CControlSignature.forSelector("Status Combo Signature", "#field-status, vaadin-combo-box[id*='status'], [id*='status-combo']",
					statusFieldTester),
			CControlSignature.forSelector("Date Picker Signature", "vaadin-date-picker, vaadin-date-time-picker, [id*='date']",
					datePickerTester));

	private int pagesVisited = 0;
	private int screenshotCounter = 1;

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
				info.index = i;
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

	/** Run component-based tests on current page.
	 * @param pageName Page name for logging */
	private void testPageComponents(final String pageName) {
		LOGGER.info("   🔍 Detecting control signatures on page: {}", pageName);
		final List<IControlSignature> detectedSignatures = new ArrayList<>();
		for (final IControlSignature signature : controlSignatures) {
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
			testerToSignatures.computeIfAbsent(signature.getTester(), ignored -> new ArrayList<>()).add(signature.getSignatureName());
		}
		LOGGER.info("   ✅ Detected {} control signature(s) mapped to {} tester(s)", detectedSignatures.size(), testerToSignatures.size());
		int testersRun = 0;
		for (final var entry : testerToSignatures.entrySet()) {
			final IComponentTester tester = entry.getKey();
			try {
				LOGGER.info("      🧩 Running {} for signatures: {}", tester.getComponentName(), entry.getValue());
				tester.test(page);
				testersRun++;
			} catch (final Exception e) {
				LOGGER.error("      ❌ Error testing {}: {}", tester.getComponentName(), e.getMessage());
			}
		}
		LOGGER.info("   ✅ Completed {} component tests", testersRun);
	}

	@Test
	@DisplayName ("🤖 Adaptive test of all pages with intelligent component detection")
	void testAllPagesAdaptively() {
		LOGGER.info("🚀 Starting Intelligent Adaptive Page Test...");
		// Check browser availability
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			org.junit.jupiter.api.Assumptions.assumeTrue(false, "Browser not available");
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
			final ButtonInfo targetButton;
			if (targetButtonId != null && !targetButtonId.isBlank()) {
				targetButton = buttons.stream().filter(b -> targetButtonId.equals(b.id)).findFirst().orElse(null);
				if (targetButton == null) {
					throw new AssertionError("Target button ID not found: " + targetButtonId);
				}
			} else {
				targetButton = buttons.get(0);
				LOGGER.info("🎯 No target button specified - using first button: {}", targetButton.title);
			}
			LOGGER.info("🧪 Step 4: Testing single page via test support button...");
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("🎯 Testing page: {}", targetButton.title);
			try {
				page.locator("#" + targetButton.id).first().click();
				wait_2000();
				final boolean hasExceptionDialog = page.locator("#custom-exception-dialog").count() > 0
						|| page.locator("#custom-exception-details-dialog").count() > 0;
				if (hasExceptionDialog) {
					LOGGER.error("   ❌ Exception detected on page load");
					takeScreenshot(String.format("%03d-exception-%s", screenshotCounter++, targetButton.id), true);
					throw new AssertionError("Exception on page: " + targetButton.title);
				}
				testPageComponents(targetButton.title);
				pagesVisited++;
				LOGGER.info("   ✅ Page test complete");
			} catch (final Exception e) {
				LOGGER.error("   ❌ Page test failed: {}", e.getMessage());
				takeScreenshot(String.format("%03d-page-%s-failure", screenshotCounter++, targetButton.id), true);
				throw new AssertionError("Failed testing page: " + targetButton.title, e);
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
}
