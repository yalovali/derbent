package automated_tests.tech.derbent.ui.automation.tests;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

import tech.derbent.Application;
import tech.derbent.plm.agile.view.CDialogAgileChildTypeSelection;
import static org.junit.jupiter.api.Assertions.assertEquals;

// KEYWORDS: AgileHierarchy, AgileChildren, placeHolder_createComponentAgileChildren, Playwright CRUD, AddExistingChild, CreateNewChild, RemoveChild
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=default",
		"server.port=0",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧩 Agile Children CRUD Test")
public class CAgileChildrenCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CAgileChildrenCrudTest.class);

	private int screenshotCounter = 1;

	private void selectComboBoxOptionByText(final Locator comboHost, final String optionText) {
		Locator combo = comboHost;
		final Locator embeddedCombo = comboHost.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
		if (embeddedCombo.count() > 0) {
			combo = embeddedCombo.first();
		}
		final Locator input = combo.locator("input");
		if (input.count() > 0) {
			input.first().click();
		} else {
			combo.click();
		}
		wait_500();
		if (input.count() > 0) {
			input.first().fill(optionText);
			wait_500();
		}
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		final Locator match = options.filter(new Locator.FilterOptions().setHasText(optionText));
		assertTrue(match.count() > 0, "ComboBox option not found: " + optionText);
		match.first().click();
		wait_500();
	}

	private Locator locateAgileChildrenSelection() {
		final Locator selection = page.locator("#custom-agile-children-selection");
		assertTrue(selection.count() > 0, "Agile children selection component not found");
		return selection.first();
	}

	private Locator locateSelectionGrid(final Locator selection) {
		final Locator grid = selection.locator("vaadin-grid");
		assertTrue(grid.count() > 0, "Agile children grid not found");
		return grid.first();
	}

	private Locator locateNameFilter(final Locator selection) {
		final Locator field = selection.locator("vaadin-text-field[placeholder='Filter by name or description...']");
		assertTrue(field.count() > 0, "Name/Desc filter field not found");
		return field.first().locator("input");
	}

	@Test
	@DisplayName ("✅ UserStory children: New/Remove/AddExisting/Filter")
	void testUserStoryChildrenCrud() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			loginToApplication();
			takeScreenshot("%03d-login".formatted(screenshotCounter++), false);

			// Navigate via Test Support Page to avoid menu/hierarchy variability
			page.navigate("http://localhost:" + port + "/cpagetestauxillary");
			page.waitForSelector("#test-auxillary-metadata",
					new Page.WaitForSelectorOptions().setTimeout(20000).setState(WaitForSelectorState.ATTACHED));
			final Locator userStoryButton = page.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("User Stories"));
			assertTrue(userStoryButton.count() > 0, "User Stories button not found on Test Support Page");
			userStoryButton.first().click();
			waitForDynamicPageLoad();
			page.waitForSelector("vaadin-grid", new Page.WaitForSelectorOptions().setTimeout(20000));
			clickFirstGridRow();
			wait_1000();
			openTabOrAccordionIfNeeded("Agile Hierarchy");
			openTabOrAccordionIfNeeded("Children");

			page.waitForSelector("#custom-agile-children-component", new Page.WaitForSelectorOptions().setTimeout(15000));
			takeScreenshot("%03d-children-open".formatted(screenshotCounter++), false);
			performFailFastCheck("Children section opened");

			// Create new child (Meeting)
			page.locator("#custom-agile-children-add-new-button").first().click();
			page.waitForSelector("#" + CDialogAgileChildTypeSelection.ID_COMBOBOX_TYPE, new Page.WaitForSelectorOptions().setTimeout(15000));
			selectComboBoxOptionByText(page.locator("#" + CDialogAgileChildTypeSelection.ID_COMBOBOX_TYPE), "Meeting");
			page.locator("#" + CDialogAgileChildTypeSelection.ID_BUTTON_CREATE).click();
			page.waitForSelector("vaadin-dialog-overlay[opened]", new Page.WaitForSelectorOptions().setTimeout(20000));
			final Locator meetingDialog = page.locator("vaadin-dialog-overlay[opened]").last();
			meetingDialog.locator("#cbutton-save").waitFor(new Locator.WaitForOptions().setTimeout(20000));
			takeScreenshot("%03d-new-meeting-dialog".formatted(screenshotCounter++), false);
			meetingDialog.locator("#cbutton-save").click();
			page.waitForFunction("() => document.querySelectorAll('vaadin-dialog-overlay[opened]').length === 0");
			wait_1000();
			performFailFastCheck("After creating new Meeting child");

			final Locator selection = locateAgileChildrenSelection();
			final Locator typeCombo = selection.locator("vaadin-combo-box").first();
			selectComboBoxOptionByText(typeCombo, "Meeting");

			final Locator filterInput = locateNameFilter(selection);
			filterInput.fill("Meeting");
			filterInput.press("Enter");
			wait_1000();

			final Locator grid = locateSelectionGrid(selection);
			waitForGridCellText(grid, "Meeting");
			takeScreenshot("%03d-meeting-visible".formatted(screenshotCounter++), false);

			final Locator meetingCell = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			assertTrue(meetingCell.count() > 0, "Meeting child should be listed in children grid");
			meetingCell.first().click();
			wait_500();

			// Remove child from parent
			page.locator("#custom-agile-children-remove-button").first().click();
			wait_2000();
			performFailFastCheck("After removing Meeting child");

			filterInput.fill("Meeting");
			filterInput.press("Enter");
			wait_1000();
			final Locator afterRemove = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			assertEquals(0, afterRemove.count(), "Meeting should no longer be a child after Remove");
			takeScreenshot("%03d-meeting-removed".formatted(screenshotCounter++), false);

			// Add existing (the just-removed Meeting should be available now)
			page.locator("#custom-agile-children-add-existing-button").first().click();
			final Locator addExistingDialog = waitForDialogWithText("Add Existing Child");
			final Locator dialogTypeCombo = addExistingDialog.locator("vaadin-combo-box").first();
			selectComboBoxOptionByText(dialogTypeCombo, "Meeting");

			final Locator dialogFilter = addExistingDialog.locator("vaadin-text-field[placeholder='Filter by name or description...'] input");
			if (dialogFilter.count() > 0) {
				dialogFilter.first().fill("Meeting");
				dialogFilter.first().press("Enter");
				wait_1000();
			}
			final Locator dialogGrid = addExistingDialog.locator("vaadin-grid").first();
			waitForGridCellText(dialogGrid, "Meeting");
			final Locator dialogMeetingCell =
				dialogGrid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			assertTrue(dialogMeetingCell.count() > 0, "Meeting should be available in Add Existing dialog");
			dialogMeetingCell.first().click();
			wait_500();
			final Locator buttonSelectExisting = addExistingDialog.locator("#custom-entity-selection-select-button");
			waitForButtonEnabled(buttonSelectExisting);
			buttonSelectExisting.click();
			waitForDialogToClose();
			wait_1000();

			filterInput.fill("Meeting");
			filterInput.press("Enter");
			wait_1000();
			final Locator afterAddExisting = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			assertTrue(afterAddExisting.count() > 0, "Meeting should be re-attached as child after Add Existing");
			takeScreenshot("%03d-meeting-readded".formatted(screenshotCounter++), false);

			// Filter behavior
			filterInput.click();
			filterInput.press("Control+A");
			filterInput.type("zzzz-not-found");
			filterInput.press("Enter");
			wait_1000();
			takeScreenshot("%03d-filter-not-found".formatted(screenshotCounter++), false);
			final Locator filteredOut = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			boolean anyVisibleMeeting = false;
			final int maxCheck = Math.min(filteredOut.count(), 25);
			for (int i = 0; i < maxCheck; i++) {
				if (filteredOut.nth(i).isVisible()) {
					anyVisibleMeeting = true;
					break;
				}
			}
			assertTrue(!anyVisibleMeeting, "Filter should hide Meeting rows");
			filterInput.click();
			filterInput.press("Control+A");
			filterInput.press("Backspace");
			filterInput.press("Enter");
			wait_1000();
			final Locator filterRestored = grid.locator("vaadin-grid-cell-content").filter(new Locator.FilterOptions().setHasText("Meeting"));
			assertTrue(filterRestored.count() > 0, "Clearing filter should restore Meeting rows");
			takeScreenshot("%03d-filter-tested".formatted(screenshotCounter++), false);

			performFailFastCheck("Agile children CRUD finished");
		} catch (final Exception e) {
			LOGGER.error("Agile children CRUD test failed: {}", e.getMessage());
			takeScreenshot("%03d-agile-children-error".formatted(screenshotCounter++), true);
			throw new AssertionError("Agile children CRUD test failed", e);
		}
	}
}
