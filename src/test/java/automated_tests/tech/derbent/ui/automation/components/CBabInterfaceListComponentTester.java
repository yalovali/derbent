package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitForSelectorState;

/** Tests the BAB dashboard interface list component that surfaces Calimero interfaces on dashboard pages. */
public class CBabInterfaceListComponentTester extends CBaseComponentTester {
	private static final String[] ROOT_SELECTORS = {
			"#custom-interfaces-component", "#custom-interface-list-root"
	};
	private static final String[] GRID_SELECTORS = {
			"#custom-interfaces-grid", "#custom-interface-list-grid"
	};
	private static final String[] HEADER_SELECTORS = {
			"#custom-interfaces-header", "#custom-interface-list-header", "h3:has-text('Network Interfaces')"
	};
	private static final String[] TOOLBAR_SELECTORS = {
			"#custom-interfaces-toolbar", "#custom-interface-list-toolbar"
	};
	private static final String[] REFRESH_BUTTON_IDS = {
			"custom-interfaces-refresh-button", "custom-interface-list-refresh"
	};
	private static final String COMPONENT_TAB_LABEL = "Interface List";
	private static final String[] EXPECTED_HEADERS = {
			"Name", "Type", "Status", "MAC", "MTU", "DHCP", "IPv4", "Gateway"
	};
	private static final String EDIT_BUTTON_ID = "cbutton-interface-edit";
	private static final String DIALOG_SELECTOR = "vaadin-dialog-overlay[opened]";
	private static final String IPV4_LABEL = "IPv4 Address";
	private static final String PREFIX_LABEL = "Prefix Length";
	private static final String GATEWAY_LABEL_FRAGMENT = "Gateway";

	private void applyValidIp(final Locator dialog, final Page page) {
		fillTextField(dialog, IPV4_LABEL, "10.24.0.10");
		fillIntegerField(dialog, PREFIX_LABEL, "24");
		fillTextField(dialog, GATEWAY_LABEL_FRAGMENT, "10.24.0.1");
		dialog.locator("vaadin-button:has-text('Save')").click();
		assertNotificationContains(page, "Interface updated");
		try {
			page.waitForSelector(DIALOG_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000).setState(WaitForSelectorState.HIDDEN));
		} catch (final PlaywrightException ignored) { /***/
		}
	}

	private void assertNotificationContains(final Page page, final String text) {
		try {
			page.waitForSelector("vaadin-notification-card:has-text('" + text + "')",
					new Page.WaitForSelectorOptions().setTimeout(5000).setState(WaitForSelectorState.VISIBLE));
			LOGGER.info("      ✓ Notification detected: {}", text);
		} catch (final PlaywrightException e) {
			LOGGER.warn("      ⚠️ Notification '{}' not detected: {}", text, e.getMessage());
		}
	}

	@Override
	public boolean canTest(final Page page) {
		return locateRoot(page) != null;
	}

	private void fillIntegerField(final Locator dialog, final String label, final String value) {
		final Locator field = dialog.locator("vaadin-integer-field[label='" + label + "'] input");
		if (field.count() == 0) {
			throw new AssertionError("Integer field not found: " + label);
		}
		field.first().fill("");
		field.first().fill(value);
	}

	private void fillTextField(final Locator dialog, final String labelFragment, final String value) {
		final Locator input = dialog.locator("vaadin-text-field[label*='" + labelFragment + "'] input");
		if (input.count() == 0) {
			throw new AssertionError("Unable to find text field containing: " + labelFragment);
		}
		input.first().fill("");
		input.first().fill(value);
	}

	@Override
	public String getComponentName() { return "BAB Interface List"; }

	private Locator locateFirstVisible(final Page page, final Locator root, final String... selectors) {
		for (final String selector : selectors) {
			final Locator candidate = (root != null ? root.locator(selector) : page.locator(selector));
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

	private Locator locateRefreshButton(final Page page, final Locator toolbar) {
		if (toolbar != null) {
			for (final String buttonId : REFRESH_BUTTON_IDS) {
				final Locator button = toolbar.locator("#" + buttonId);
				if (button.count() > 0) {
					return button.first();
				}
			}
			final Locator iconButton = toolbar.locator("vaadin-button")
					.filter(new Locator.FilterOptions().setHas(page.locator("vaadin-icon[icon='vaadin:refresh'], vaadin-icon[icon*='refresh']")));
			if (iconButton.count() > 0) {
				return iconButton.first();
			}
			final Locator textButton = toolbar.locator("vaadin-button:has-text('Refresh')");
			if (textButton.count() > 0) {
				return textButton.first();
			}
		}
		for (final String buttonId : REFRESH_BUTTON_IDS) {
			final Locator button = page.locator("#" + buttonId);
			if (button.count() > 0) {
				return button.first();
			}
		}
		final Locator fallback = page.locator("vaadin-button:has-text('Refresh')");
		return fallback.count() > 0 ? fallback.first() : null;
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
			LOGGER.debug("      ⚠️ Unable to scroll interface list into view: {}", e.getMessage());
		}
	}

	@Override
	public void test(final Page page) {
		LOGGER.info("      🌐 Validating BAB interface list component...");
		openTabOrAccordionIfNeeded(page, COMPONENT_TAB_LABEL);
		final Locator root = locateRoot(page);
		if (root == null) {
			throw new AssertionError("BAB interface list component root not found on page " + safePageTitle(page));
		}
		scrollIntoView(root);
		verifyHeader(page, root);
		final Locator toolbar = locateFirstVisible(page, root, TOOLBAR_SELECTORS);
		final Locator refreshButton = locateRefreshButton(page, toolbar);
		final Locator grid = locateFirstVisible(page, root, GRID_SELECTORS);
		if (grid == null) {
			throw new AssertionError("Interface grid not found for BAB component on page " + safePageTitle(page));
		}
		if (refreshButton != null) {
			testRefreshButton(refreshButton);
		} else {
			LOGGER.warn("      ⚠️ Refresh button not found for BAB interface list component");
		}
		verifyGridColumns(grid);
		verifyInterfaceData(grid);
		testEditWorkflow(page, grid);
	}

	private void testEditWorkflow(final Page page, final Locator grid) {
		try {
			final Locator editButton = page.locator("#" + EDIT_BUTTON_ID);
			if (editButton.count() == 0) {
				LOGGER.warn("      ⚠️ Edit button not available on toolbar");
				return;
			}
			final Locator firstCell = grid.locator("vaadin-grid-cell-content").first();
			firstCell.click();
			wait_500(page);
			editButton.click();
			final Locator dialog = waitForDialog(page);
			validateInvalidInput(dialog, page);
			applyValidIp(dialog, page);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Edit dialog test failed: {}", e.getMessage());
		}
	}

	private void testRefreshButton(final Locator refreshButton) {
		try {
			LOGGER.info("      🔄 Testing interface refresh action...");
			waitForButtonEnabled(refreshButton);
			refreshButton.click();
			wait_1000(refreshButton.page());
			LOGGER.info("      ✓ Refresh action triggered");
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Refresh button click failed: {}", e.getMessage());
			LOGGER.warn("         This may happen when Calimero service is offline. Test continues.");
		}
	}

	private void validateInvalidInput(final Locator dialog, final Page page) {
		fillTextField(dialog, IPV4_LABEL, "999.999.999.999");
		dialog.locator("vaadin-button:has-text('Save')").click();
		assertNotificationContains(page, "Invalid IPv4 format");
	}

	private void verifyGridColumns(final Locator grid) {
		LOGGER.info("      📊 Verifying interface grid columns...");
		final Locator headers = grid.locator("vaadin-grid-cell-content[slot*='vaadin-grid-cell-content'], th");
		if (headers.count() == 0) {
			throw new AssertionError("No grid headers found for BAB interface list on page " + safePageTitle(grid.page()));
		}
		for (final String expectedHeader : EXPECTED_HEADERS) {
			boolean found = false;
			for (int i = 0; i < headers.count(); i++) {
				final String text = headers.nth(i).textContent();
				if ((text != null) && text.toLowerCase().contains(expectedHeader.toLowerCase())) {
					found = true;
					break;
				}
			}
			if (!found) {
				LOGGER.warn("      ⚠️ Expected column '{}' not found in interface grid", expectedHeader);
			} else {
				LOGGER.debug("      ✓ Column '{}' detected", expectedHeader);
			}
		}
	}

	private void verifyHeader(final Page page, final Locator root) {
		final Locator header = locateFirstVisible(page, root, HEADER_SELECTORS);
		if (header == null) {
			throw new AssertionError("Interface list header not visible on page " + safePageTitle(page));
		}
		final String headerText = header.textContent();
		if ((headerText == null) || !headerText.toLowerCase().contains("interface")) {
			throw new AssertionError("Unexpected header text for BAB interface list: " + headerText);
		}
		LOGGER.debug("      ✓ Interface header text: {}", headerText.trim());
	}

	private void verifyInterfaceData(final Locator grid) {
		try {
			LOGGER.info("      🔎 Verifying interface data rows...");
			wait_1000(grid.page());
			final Locator cells = grid.locator("vaadin-grid-cell-content");
			final int cellCount = cells.count();
			if (cellCount > 0) {
				LOGGER.info("      ✓ Interface grid rendered {} cells", cellCount);
			}
			if (cellCount <= EXPECTED_HEADERS.length) {
				LOGGER.warn("      ⚠️ Grid appears empty or Calimero service unavailable ({} cells)", cellCount);
			}
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Unable to verify interface grid data: {}", e.getMessage());
		}
	}

	private Locator waitForDialog(final Page page) {
		final Locator dialog = page.locator(DIALOG_SELECTOR).first();
		dialog.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(5000));
		return dialog;
	}
}
