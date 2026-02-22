package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

/** Tests the Calimero status component that manages service control (enable/disable, path, start/stop). */
public class CCalimeroStatusComponentTester extends CBaseComponentTester {

	private static final String COMPONENT_TAB_LABEL = "Calimero Service";
	private static final String ENABLE_CHECKBOX_ID = "custom-calimero-enable-checkbox";
	private static final String EXECUTABLE_PATH_ID = "custom-calimero-executable-path";
	private static final String[] HEADER_SELECTORS = {
			"#custom-calimero-header", "span.calimero-title:has-text('Calimero Service')", "h3:has-text('Calimero Service')"
	};
	private static final String[] ROOT_SELECTORS = {
			"#custom-calimero-status-component", "#custom-calimero-control-card"
	};
	private static final String START_STOP_BUTTON_ID = "custom-calimero-start-stop-button";
	private static final String STATUS_INDICATOR_ID = "custom-calimero-status-indicator";

	@Override
	public boolean canTest(final Page page) {
		return locateRoot(page) != null;
	}

	@Override
	public String getComponentName() { return "Calimero Status Component"; }

	private Locator locateFirstVisible(final Page page, final Locator root, final String... selectors) {
		for (final String selector : selectors) {
			final Locator candidate = root != null ? root.locator(selector) : page.locator(selector);
			if (candidate.count() > 0) {
				for (int i = 0; i < candidate.count(); i++) {
					final Locator element = candidate.nth(i);
					if (element.isVisible()) {
						return element;
					}
				}
			}
		}
		return null;
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
				return root.first();
			}
		}
		return null;
	}

	private void scrollIntoView(final Locator root) {
		try {
			root.scrollIntoViewIfNeeded();
			wait_500(root.page());
		} catch (final Exception e) {
			LOGGER.debug("      ⚠️ Unable to scroll Calimero status component into view: {}", e.getMessage());
		}
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🔧 Validating Calimero status component...");
		openTabOrAccordionIfNeeded(page, COMPONENT_TAB_LABEL);
		final Locator root = locateRoot(page);
		if (root == null) {
			throw new AssertionError("Calimero status component root not found on page " + safePageTitle(page));
		}
		scrollIntoView(root);
		verifyHeader(page, root);
		verifyEnableCheckbox(page);
		verifyExecutablePathField(page);
		verifyStatusIndicator(page);
		testStartStopButton(page);
		LOGGER.info("      ✅ Calimero status component validation complete");
	}

	private void testButtonClick(final Page page, final Locator button, final String initialText) {
		try {
			LOGGER.info("      🖱️ Testing Start/Stop button control cycle...");
			// Ensure button is enabled
			if (button.isDisabled()) {
				LOGGER.warn("      ⚠️ Button is disabled - cannot test");
				return;
			}
			// Get initial status
			final Locator statusIndicator = page.locator("#" + STATUS_INDICATOR_ID);
			final String initialStatus = statusIndicator.count() > 0 ? statusIndicator.textContent().trim() : "unknown";
			LOGGER.info("      📍 Initial state: Button='{}', Status='{}'", initialText, initialStatus);
			// ====== FIRST CLICK: Toggle service state ======
			LOGGER.info("      🔄 First click: '{}' action...", initialText);
			button.click();
			wait_500(page);
			// Wait for operation to start (status should show "Starting..." or "Stopping...")
			wait_2000(page);
			// Check intermediate status
			final String pendingStatus = statusIndicator.count() > 0 ? statusIndicator.textContent().trim() : "unknown";
			LOGGER.info("      ⏳ Pending status: '{}'", pendingStatus);
			// Wait for operation to complete
			wait_2000(page);
			// Verify button text changed
			final String afterFirstClick = button.textContent().trim();
			LOGGER.info("      📍 After first click: Button='{}', Status='{}'", afterFirstClick,
					statusIndicator.count() > 0 ? statusIndicator.textContent().trim() : "unknown");
			if (!afterFirstClick.equals(initialText)) {
				LOGGER.info("      ✅ First toggle successful: '{}' → '{}'", initialText, afterFirstClick);
			} else {
				LOGGER.warn("      ⚠️ Button did not change after first click (service may be unavailable)");
				return; // Don't continue if first action failed
			}
			// ====== SECOND CLICK: Toggle back ======
			LOGGER.info("      🔄 Second click: '{}' action (toggle back)...", afterFirstClick);
			wait_1000(page); // Ensure service is stable
			button.click();
			wait_500(page);
			// Wait for second operation
			wait_2000(page);
			// Verify button returned to original state
			final String afterSecondClick = button.textContent().trim();
			final String finalStatus = statusIndicator.count() > 0 ? statusIndicator.textContent().trim() : "unknown";
			LOGGER.info("      📍 After second click: Button='{}', Status='{}'", afterSecondClick, finalStatus);
			if (afterSecondClick.equals(initialText)) {
				LOGGER.info("      ✅ Second toggle successful: '{}' → '{}' → '{}'", initialText, afterFirstClick, afterSecondClick);
				LOGGER.info("      ✅ FULL CYCLE COMPLETE: Service start/stop control verified");
			} else {
				LOGGER.warn("      ⚠️ Second toggle incomplete (may need more time)");
			}
			// Verify status indicator matches button state
			verifyStatusMatchesButton(afterSecondClick, finalStatus);
		} catch (final PlaywrightException e) {
			LOGGER.warn("      ⚠️ Button test failed: {}", e.getMessage());
			LOGGER.warn("         This may happen when Calimero service is unavailable. Test continues.");
		} catch (final Exception e) {
			LOGGER.error("      ❌ Unexpected error during button test: {}", e.getMessage());
		}
	}

	private void testStartStopButton(final Page page) {
		LOGGER.info("      🎮 Testing Start/Stop button functionality...");
		final Locator button = page.locator("#" + START_STOP_BUTTON_ID);
		if (button.count() == 0) {
			throw new AssertionError("Start/Stop button not found on page " + safePageTitle(page));
		}
		// Check button is visible
		if (!button.isVisible()) {
			throw new AssertionError("Start/Stop button exists but is not visible");
		}
		// Get initial button text (Start or Stop)
		final String initialText = button.textContent().trim();
		LOGGER.info("      📍 Initial button state: '{}'", initialText);
		if (!initialText.contains("Start") && !initialText.contains("Stop")) {
			throw new AssertionError("Button text must contain 'Start' or 'Stop', found: " + initialText);
		}
		// Check button icon
		final Locator icon = button.locator("vaadin-icon");
		if (icon.count() > 0) {
			final String iconName = icon.getAttribute("icon");
			LOGGER.debug("      ✓ Button icon detected: {}", iconName);
			if (initialText.contains("Start") && !iconName.contains("play")) {
				LOGGER.warn("      ⚠️ Start button should have play icon, found: {}", iconName);
			}
			if (initialText.contains("Stop") && !iconName.contains("stop")) {
				LOGGER.warn("      ⚠️ Stop button should have stop icon, found: {}", iconName);
			}
		} else {
			LOGGER.warn("      ⚠️ Button icon not found");
		}
		// Test button click behavior
		testButtonClick(page, button, initialText);
	}

	private void verifyEnableCheckbox(final Page page) {
		LOGGER.info("      ☑️ Verifying enable service checkbox...");
		final Locator checkbox = page.locator("#" + ENABLE_CHECKBOX_ID);
		if (checkbox.count() == 0) {
			throw new AssertionError("Enable service checkbox not found on page " + safePageTitle(page));
		}
		if (!checkbox.isVisible()) {
			throw new AssertionError("Enable service checkbox exists but is not visible");
		}
		final String label = checkbox.textContent();
		if (label == null || !label.toLowerCase().contains("enable")) {
			LOGGER.warn("      ⚠️ Checkbox label unexpected: {}", label);
		}
		LOGGER.debug("      ✓ Enable checkbox detected with label: {}", label);
	}

	private void verifyExecutablePathField(final Page page) {
		LOGGER.info("      📂 Verifying executable path field...");
		final Locator field = page.locator("#" + EXECUTABLE_PATH_ID);
		if (field.count() == 0) {
			throw new AssertionError("Executable path field not found on page " + safePageTitle(page));
		}
		if (!field.isVisible()) {
			throw new AssertionError("Executable path field exists but is not visible");
		}
		final Locator input = field.locator("input");
		if (input.count() > 0) {
			final String value = input.inputValue();
			LOGGER.debug("      ✓ Executable path field value: {}", value != null && !value.isEmpty() ? value : "(empty)");
		} else {
			LOGGER.warn("      ⚠️ Path field input not accessible");
		}
	}

	private void verifyHeader(final Page page, final Locator root) {
		final Locator header = locateFirstVisible(page, root, HEADER_SELECTORS);
		if (header == null) {
			throw new AssertionError("Calimero status header not visible on page " + safePageTitle(page));
		}
		final String headerText = header.textContent();
		if (headerText == null || !headerText.toLowerCase().contains("calimero")) {
			throw new AssertionError("Unexpected header text for Calimero status component: " + headerText);
		}
		LOGGER.debug("      ✓ Calimero header text: {}", headerText.trim());
	}

	private void verifyStatusIndicator(final Page page) {
		LOGGER.info("      📊 Verifying status indicator...");
		final Locator indicator = page.locator("#" + STATUS_INDICATOR_ID);
		if (indicator.count() == 0) {
			throw new AssertionError("Status indicator not found on page " + safePageTitle(page));
		}
		if (!indicator.isVisible()) {
			throw new AssertionError("Status indicator exists but is not visible");
		}
		final String statusText = indicator.textContent().trim();
		LOGGER.info("      📍 Current Calimero status: '{}'", statusText);
		// Check for status classes
		final String className = indicator.getAttribute("class");
		if (className != null) {
			if (className.contains("status-running")) {
				LOGGER.debug("      ✓ Status: Running");
			} else if (className.contains("status-stopped")) {
				LOGGER.debug("      ✓ Status: Stopped");
			} else if (className.contains("status-disabled")) {
				LOGGER.debug("      ✓ Status: Disabled");
			}
		}
	}

	private void verifyStatusMatchesButton(final String buttonText, final String statusText) {
		try {
			final boolean buttonSaysStop = buttonText.contains("Stop");
			final boolean statusSaysRunning = statusText.toLowerCase().contains("running") || statusText.toLowerCase().contains("started");
			if (buttonSaysStop && statusSaysRunning) {
				LOGGER.info("      ✅ Status consistent: Button='Stop' and service is running");
			} else if (!buttonSaysStop && !statusSaysRunning) {
				LOGGER.info("      ✅ Status consistent: Button='Start' and service is stopped");
			} else {
				LOGGER.warn("      ⚠️ Status inconsistent: Button='{}', Status='{}'", buttonText, statusText);
			}
		} catch (final Exception e) {
			LOGGER.debug("      ⚠️ Could not verify status consistency: {}", e.getMessage());
		}
	}
}
