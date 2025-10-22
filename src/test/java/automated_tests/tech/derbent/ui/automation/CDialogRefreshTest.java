package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import com.microsoft.playwright.Locator;
import tech.derbent.Application;
import tech.derbent.api.utils.Check;

/** Playwright test to diagnose and verify dialog box component refresh issues. This test specifically checks if ComboBox and other components in
 * relation dialogs properly display values when editing existing entities. */
@SpringBootTest (classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles ("test")
public class CDialogRefreshTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogRefreshTest.class);

	@Test
	public void testWorkflowStatusRelationDialogRefresh() throws Exception {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			return;
		}
		LOGGER.info("🧪 Testing workflow status relation dialog component refresh");
		// 1. Login to application
		loginToApplication();
		wait_2000();
		// 2. Navigate to workflow management (try different possible routes)
		boolean navigated = navigateToWorkflows();
		if (!navigated) {
			LOGGER.warn("⚠️ Could not navigate to workflows - trying alternative approach");
			// Try navigating via menu
			if (!navigateToViewByText("Workflow")) {
				LOGGER.error("❌ Failed to navigate to Workflows view");
				takeScreenshot("workflow-navigation-failed");
				return;
			}
		}
		wait_2000();
		takeScreenshot("workflow-view-loaded");
		// 3. Check if there are any workflows in the grid
		if (!verifyGridHasData()) {
			LOGGER.info("ℹ️ No workflows found - creating a test workflow first");
			createTestWorkflow();
			wait_2000();
		}
		// 4. Select first workflow
		clickFirstGridRow();
		wait_1000();
		takeScreenshot("workflow-selected");
		// 5. Look for status transitions section or tab
		Locator transitionsSection = findStatusTransitionsSection();
		if (transitionsSection == null) {
			LOGGER.error("❌ Could not find status transitions section");
			takeScreenshot("transitions-section-not-found");
			return;
		}
		// 6. Check if there are existing status transitions
		wait_1000();
		Locator transitionsGrid = page.locator("vaadin-grid").filter(new Locator.FilterOptions().setHas(page.locator("text='From Status'")));
		if (transitionsGrid.count() == 0) {
			LOGGER.info("ℹ️ Looking for transitions grid with alternative methods");
			// Try to find any grid in the transitions section
			transitionsGrid = page.locator("vaadin-grid").nth(1); // Second grid might be transitions
		}
		// 7. Check if we need to create a test transition
		boolean hasTransitions = false;
		if (transitionsGrid.count() > 0) {
			Locator transitionCells = transitionsGrid.locator("vaadin-grid-cell-content");
			hasTransitions = transitionCells.count() > 0;
		}
		if (!hasTransitions) {
			LOGGER.info("ℹ️ No existing status transitions - creating one first");
			createTestStatusTransition();
			wait_2000();
			takeScreenshot("test-transition-created");
		}
		// 8. Now test editing an existing transition to check dialog refresh
		LOGGER.info("🔍 Opening edit dialog for existing status transition");
		// Click on a transition row
		if (transitionsGrid.count() > 0) {
			Locator cells = transitionsGrid.locator("vaadin-grid-cell-content");
			if (cells.count() > 0) {
				cells.first().click();
				wait_500();
			}
		}
		// Click Edit button
		Locator editButton = page.locator("vaadin-button:has-text('Edit')");
		if (editButton.count() > 0) {
			LOGGER.info("✏️ Clicking Edit button");
			editButton.first().click();
			wait_2000(); // Wait for dialog to open and populate
			takeScreenshot("edit-dialog-opened");
			// 9. Check if ComboBoxes in the dialog have values displayed
			LOGGER.info("🔍 Checking if ComboBox components show values");
			Locator comboBoxes = page.locator("vaadin-dialog-overlay[opened] vaadin-combo-box");
			int comboBoxCount = comboBoxes.count();
			LOGGER.info("📊 Found {} ComboBoxes in dialog", comboBoxCount);
			if (comboBoxCount == 0) {
				LOGGER.error("❌ No ComboBoxes found in edit dialog!");
				takeScreenshot("no-comboboxes-in-dialog");
				return;
			}
			// Check each ComboBox to see if it displays a value
			boolean allComboBoxesHaveValues = true;
			for (int i = 0; i < comboBoxCount; i++) {
				Locator comboBox = comboBoxes.nth(i);
				// Get the label of the ComboBox
				String label = "";
				try {
					Locator labelElement = comboBox.locator("[slot='label']");
					if (labelElement.count() > 0) {
						label = labelElement.textContent();
					}
				} catch (Exception e) {
					label = "ComboBox " + i;
				}
				// Check if ComboBox has a displayed value
				Locator inputField = comboBox.locator("input");
				String displayedValue = "";
				if (inputField.count() > 0) {
					displayedValue = inputField.first().inputValue();
				}
				LOGGER.info("📋 ComboBox '{}': displayed value = '{}'", label, displayedValue);
				if (displayedValue == null || displayedValue.trim().isEmpty()) {
					LOGGER.error("❌ ComboBox '{}' has NO displayed value (empty/blank)", label);
					allComboBoxesHaveValues = false;
					takeScreenshot("combobox-" + i + "-empty");
				} else {
					LOGGER.info("✅ ComboBox '{}' has displayed value: {}", label, displayedValue);
				}
			}
			takeScreenshot("dialog-comboboxes-checked");
			// 10. Report findings
			if (allComboBoxesHaveValues) {
				LOGGER.info("✅ All ComboBoxes in dialog have values displayed correctly");
			} else {
				LOGGER.error("❌ ISSUE CONFIRMED: Some ComboBoxes in dialog do NOT have values displayed");
				LOGGER.error("❌ This confirms the dialog refresh issue exists");
			}
			// Close dialog
			Locator cancelButton = page.locator("vaadin-dialog-overlay[opened] vaadin-button:has-text('Cancel')");
			if (cancelButton.count() > 0) {
				cancelButton.first().click();
				wait_1000();
			}
		} else {
			LOGGER.warn("⚠️ Edit button not found");
			takeScreenshot("edit-button-not-found");
		}
		LOGGER.info("✅ Dialog refresh test completed");
	}

	private boolean navigateToWorkflows() {
		LOGGER.info("🧭 Navigating to Workflows view");
		try {
			// Try direct route first
			String[] possibleRoutes = {
					"workflow", "workflows", "workflow-entity", "workflow-management"
			};
			for (String route : possibleRoutes) {
				try {
					page.navigate("http://localhost:" + port + "/" + route);
					wait_2000();
					if (page.locator("vaadin-grid").count() > 0) {
						LOGGER.info("✅ Navigated to workflows via route: {}", route);
						return true;
					}
				} catch (Exception e) {
					LOGGER.debug("⚠️ Route {} failed: {}", route, e.getMessage());
				}
			}
			// Try menu navigation
			if (navigateToViewByText("Workflow")) {
				return true;
			}
			return false;
		} catch (Exception e) {
			LOGGER.error("❌ Failed to navigate to workflows: {}", e.getMessage());
			return false;
		}
	}

	private void createTestWorkflow() {
		LOGGER.info("➕ Creating test workflow");
		try {
			clickNew();
			wait_1000();
			fillFirstTextField("Test Workflow " + System.currentTimeMillis());
			clickSave();
			wait_1000();
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to create test workflow: {}", e.getMessage());
		}
	}

	private Locator findStatusTransitionsSection() {
		LOGGER.info("🔍 Looking for status transitions section");
		// Look for tabs or sections that might contain status transitions
		String[] possibleSelectors = {
				"vaadin-tab:has-text('Status Transitions')", "vaadin-tab:has-text('Transitions')", "text='Status Transitions'",
				"text='Transitions'"
		};
		for (String selector : possibleSelectors) {
			Locator element = page.locator(selector);
			if (element.count() > 0) {
				LOGGER.info("✅ Found transitions section with selector: {}", selector);
				element.first().click();
				wait_1000();
				return element.first();
			}
		}
		// If no tab found, assume the transitions are already visible
		LOGGER.info("ℹ️ No transitions tab found, assuming transitions are visible");
		return page.locator("body").first();
	}

	private void createTestStatusTransition() {
		LOGGER.info("➕ Creating test status transition");
		try {
			// Look for Add/New button in the transitions section
			Locator addButton = page.locator("vaadin-button:has-text('New'), vaadin-button:has-text('Add')");
			if (addButton.count() > 0) {
				addButton.first().click();
				wait_1000();
				takeScreenshot("add-transition-dialog");
				// Select first option in both ComboBoxes (From Status and To Status)
				Locator comboBoxes = page.locator("vaadin-dialog-overlay[opened] vaadin-combo-box");
				if (comboBoxes.count() >= 2) {
					// Select From Status
					comboBoxes.first().click();
					wait_500();
					Locator fromOptions = page.locator("vaadin-combo-box-item");
					if (fromOptions.count() > 0) {
						fromOptions.first().click();
						wait_500();
					}
					// Select To Status (different from From Status)
					comboBoxes.nth(1).click();
					wait_500();
					Locator toOptions = page.locator("vaadin-combo-box-item");
					if (toOptions.count() > 1) {
						toOptions.nth(1).click(); // Select second option to be different
						wait_500();
					}
				}
				// Save
				clickSave();
				wait_1000();
			} else {
				LOGGER.warn("⚠️ Add/New button not found for creating transition");
			}
		} catch (Exception e) {
			LOGGER.warn("⚠️ Failed to create test status transition: {}", e.getMessage());
		}
	}
}
