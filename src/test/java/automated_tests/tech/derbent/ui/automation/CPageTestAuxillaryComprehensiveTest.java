package automated_tests.tech.derbent.ui.automation;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Page;
import tech.derbent.app.components.componentversion.domain.CProjectComponentVersion;
import tech.derbent.app.products.productversion.domain.CProductVersion;

/** Comprehensive test suite for CPageTestAuxillary that dynamically tests all pages accessible via navigation buttons.
 * <p>
 * This test suite:
 * <ul>
 * <li>Navigates to CPageTestAuxillary page after login
 * <li>Dynamically discovers all navigation buttons
 * <li>For each button, extracts the target route from data-route attribute
 * <li>Navigates directly to each route URL (more reliable than clicking JavaScript handlers)
 * <li>Runs conditional tests based on page content:
 * <ul>
 * <li>Grid tests if grid is present
 * <li>CRUD toolbar tests if toolbar exists
 * </ul>
 * <li>Uses generic, reusable check.* functions for validation
 * <li>Handles dynamic number of buttons without hardcoding
 * </ul>
 * <p>
 * Design Philosophy:
 * <ul>
 * <li><b>Fast execution</b>: Reasonable timeouts, no excessive waits
 * <li><b>Complete coverage</b>: Tests ALL buttons, no skipping
 * <li><b>Generic testing</b>: Reusable functions work with any page type
 * <li><b>Direct navigation</b>: Uses URL navigation instead of clicking for reliability
 * <li><b>Detailed logging</b>: Clear progress indicators and error messages
 * </ul>
 */
@SpringBootTest (webEnvironment = WebEnvironment.DEFINED_PORT, classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 CPageTestAuxillary Comprehensive Page Testing")
public class CPageTestAuxillaryComprehensiveTest extends CBaseUITest {

	// ==========================================
	// HELPER CLASS
	// ==========================================
	/** Helper class to store button information. */
	private static class ButtonInfo {

		String id;
		int index;
		String route;
		String title;
	}

	private static final class FieldValueResult {

		private final String fieldId;
		private final String value;

		private FieldValueResult(final String fieldId, final String value) {
			this.fieldId = fieldId;
			this.value = value;
		}
	}

	private static final String BUTTON_SELECTOR = "[id^='test-aux-btn-']";
	private static final String CONFIRM_YES_BUTTON_ID = "cbutton-yes";
	private static final String CRUD_CANCEL_BUTTON_ID = "cbutton-cancel";
	private static final String CRUD_DELETE_BUTTON_ID = "cbutton-delete";
	private static final String CRUD_NEW_BUTTON_ID = "cbutton-new";
	private static final String CRUD_REFRESH_BUTTON_ID = "cbutton-refresh";
	private static final String CRUD_SAVE_BUTTON_ID = "cbutton-save";
	private static final String FIELD_ID_PREFIX = "field-";
	private static final String GRID_SELECTOR = "vaadin-grid, vaadin-grid-pro, so-grid, c-grid";
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageTestAuxillaryComprehensiveTest.class);
	private static final String METADATA_SELECTOR = "#test-auxillary-metadata";
	private static final String TEST_AUX_PAGE_ROUTE = "cpagetestauxillary";
	private int crudPagesFound = 0;
	private int gridPagesFound = 0;
	private final Map<String, String> lastCreatedFieldIds = new java.util.HashMap<>();
	private final Map<String, String> lastCreatedValues = new java.util.HashMap<>();
	private int pagesVisited = 0;
	private int screenshotCounter = 1;

	/** Check if a specific CRUD button exists.
	 * @param buttonText Button text to check for
	 * @return true if button exists */
	private boolean checkCrudButtonExists(String buttonId) {
		try {
			final Locator button = page.locator("#" + buttonId);
			return button.count() > 0;
		} catch (final Exception e) {
			LOGGER.debug("Error checking for {} button: {}", buttonId, e.getMessage());
			return false;
		}
	}

	/** Check if a CRUD toolbar exists on the current page.
	 * @return true if CRUD toolbar is present */
	private boolean checkCrudToolbarExists() {
		try {
			// Look for common CRUD buttons by deterministic IDs
			final Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			final Locator deleteButton = page.locator("#" + CRUD_DELETE_BUTTON_ID);
			final Locator saveButton = page.locator("#" + CRUD_SAVE_BUTTON_ID);
			final Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
			// If we have at least 2 of these buttons, consider it a CRUD toolbar
			int count = 0;
			if (newButton.count() > 0) {
				count++;
			}
			if (deleteButton.count() > 0) {
				count++;
			}
			if (saveButton.count() > 0) {
				count++;
			}
			if (refreshButton.count() > 0) {
				count++;
			}
			return count >= 2;
		} catch (final Exception e) {
			LOGGER.debug("Error checking for CRUD toolbar: {}", e.getMessage());
			return false;
		}
	}

	// ==========================================
	// GENERIC CHECK FUNCTIONS
	// ==========================================
	/** Check if a grid exists on the current page.
	 * @return true if grid is present */
	private boolean checkGridExists() {
		try {
			final Locator grids = page.locator(GRID_SELECTOR);
			return grids.count() > 0;
		} catch (final Exception e) {
			LOGGER.debug("Error checking for grid: {}", e.getMessage());
			return false;
		}
	}

	/** Check if grid has data.
	 * @return true if grid contains data */
	private boolean checkGridHasData() {
		try {
			final Locator grid = page.locator(GRID_SELECTOR).first();
			if (grid.count() == 0) {
				return false;
			}
			final Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
			final int cellCount = cells.count();
			LOGGER.debug("Grid has {} cells", cellCount);
			return cellCount > 0;
		} catch (final Exception e) {
			LOGGER.debug("Error checking if grid has data: {}", e.getMessage());
			return false;
		}
	}

	/** Check if grid is sortable.
	 * @return true if grid has sortable columns */
	private boolean checkGridIsSortable() {
		try {
			final Locator sorters = page.locator("vaadin-grid-sorter");
			return sorters.count() > 0;
		} catch (final Exception e) {
			LOGGER.debug("Error checking if grid is sortable: {}", e.getMessage());
			return false;
		}
	}

	private void confirmDialogIfPresent() {
		final Locator overlay = page.locator("vaadin-dialog-overlay[opened]");
		if (overlay.count() == 0) {
			return;
		}
		final Locator confirmButton = page.locator("#" + CONFIRM_YES_BUTTON_ID);
		if (confirmButton.count() > 0) {
			confirmButton.first().click();
			wait_500();
		}
	}

	/** Discover all navigation buttons on the CPageTestAuxillary page dynamically.
	 * @return List of button information */
	private List<ButtonInfo> discoverNavigationButtons() {
		final List<ButtonInfo> buttons = new ArrayList<>();
		try {
			// Try to read button count from metadata
			final Locator metadataDiv = page.locator(METADATA_SELECTOR);
			if (metadataDiv.count() > 0) {
				final String buttonCountStr = metadataDiv.getAttribute("data-button-count");
				LOGGER.debug("Metadata indicates {} buttons", buttonCountStr);
			}
			// Discover all buttons with the test-aux-btn- prefix
			final Locator buttonLocators = page.locator(BUTTON_SELECTOR);
			final int buttonCount = buttonLocators.count();
			LOGGER.info("🔍 Discovered {} navigation buttons", buttonCount);
			for (int i = 0; i < buttonCount; i++) {
				final Locator button = buttonLocators.nth(i);
				final ButtonInfo info = new ButtonInfo();
				info.index = i;
				info.id = button.getAttribute("id");
				info.title = button.getAttribute("data-title");
				info.route = button.getAttribute("data-route");
				// Fallback: extract title from button text if not in attributes
				if (info.title == null || info.title.isEmpty()) {
					info.title = button.textContent();
					if (info.title != null) {
						info.title = info.title.trim();
					}
				}
				buttons.add(info);
				LOGGER.debug("   Button {}: {} -> {}", i, info.title, info.route);
			}
			return buttons;
		} catch (final Exception e) {
			throw new AssertionError("Failed to discover navigation buttons: " + e.getMessage(), e);
		}
	}

	private void ensureRequiredComboSelections() {
		final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "']");
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final String fieldId = field.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (isSystemFieldId(fieldId)) {
				continue;
			}
			if (!isComboBoxById(fieldId) || !isFieldEditable(field) || !isFieldRequired(field)) {
				continue;
			}
			final String currentValue = readFieldValueById(fieldId);
			if (currentValue != null && !currentValue.isBlank()) {
				continue;
			}
			try {
				selectFirstComboBoxOptionById(fieldId);
				final String afterValue = readFieldValueById(fieldId);
				if (afterValue == null || afterValue.isBlank()) {
					LOGGER.warn("      ⚠️ Required combo {} still empty after selection", fieldId);
				} else {
					LOGGER.info("      ✓ Selected required combo {}", fieldId);
				}
			} catch (final Exception e) {
				LOGGER.warn("      ⚠️ Failed to select required combo {}: {}", fieldId, e.getMessage());
			}
		}
	}

	private String findEditableFieldId() {
		final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "']");
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final String fieldId = field.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (isSystemFieldId(fieldId) || isEntityIdField(fieldId)) {
				continue;
			}
			if (field.locator("input, textarea").count() > 0) {
				return fieldId;
			}
			final String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
			if (tagName.contains("combo-box")) {
				return fieldId;
			}
		}
		return null;
	}

	private String findPreferredFieldId() {
		final String[] preferredSuffixes = {
				"-name", "-title", "-code"
		};
		for (final String suffix : preferredSuffixes) {
			final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "'][id$='" + suffix + "']");
			if (fields.count() == 0) {
				continue;
			}
			for (int i = 0; i < fields.count(); i++) {
				final String fieldId = fields.nth(i).getAttribute("id");
				if (fieldId == null || fieldId.isBlank()) {
					continue;
				}
				if (isSystemFieldId(fieldId) || isEntityIdField(fieldId)) {
					continue;
				}
				return fieldId;
			}
		}
		return null;
	}

	private int getGridRowCountSafe() {
		final Locator grid = page.locator(GRID_SELECTOR).first();
		if (grid.count() == 0) {
			return 0;
		}
		final Locator rows = grid.locator("[part='row'], tr");
		if (rows.count() > 0) {
			return rows.count();
		}
		return grid.locator("vaadin-grid-cell-content, [part='cell']").count();
	}

	private String getFirstGridCellText() {
		final Locator grid = page.locator(GRID_SELECTOR).first();
		if (grid.count() == 0) {
			return null;
		}
		final Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
		final int maxCells = Math.min(cells.count(), 10);
		for (int i = 0; i < maxCells; i++) {
			final String text = cells.nth(i).textContent();
			if (text != null && !text.isBlank()) {
				return text.trim();
			}
		}
		return null;
	}

	private boolean isComboBoxById(final String fieldId) {
		try {
			final Locator field = locatorById(fieldId);
			final Locator embeddedCombo = field.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
			if (embeddedCombo.count() > 0) {
				return true;
			}
			final String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
			return tagName.contains("combo-box");
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return false;
		}
	}

	@SuppressWarnings ("static-method")
	private boolean isEntityIdField(final String fieldId) {
		return fieldId != null && fieldId.toLowerCase().endsWith("-id");
	}

	@SuppressWarnings ("static-method")
	private boolean isFieldEditable(final Locator field) {
		try {
			if (!field.isEnabled()) {
				return false;
			}
			if (field.getAttribute("readonly") != null || field.getAttribute("disabled") != null) {
				return false;
			}
			final Locator input = field.locator("input, textarea");
			if (input.count() > 0) {
				if (!input.first().isEnabled()) {
					return false;
				}
				final Object readonly = input.first().evaluate("el => el.hasAttribute('readonly') || el.hasAttribute('disabled')");
				return readonly == null || Boolean.FALSE.equals(readonly);
			}
			return true;
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return true;
		}
	}

	@SuppressWarnings ("static-method")
	private boolean isFieldRequired(final Locator field) {
		try {
			final Object required = field.evaluate("el => el.hasAttribute('required') || el.getAttribute('aria-required') === 'true'");
			if (Boolean.TRUE.equals(required)) {
				return true;
			}
			final Locator input = field.locator("input");
			if (input.count() > 0) {
				final Object inputRequired =
						input.first().evaluate("el => el.hasAttribute('required') || el.getAttribute('aria-required') === 'true'");
				return Boolean.TRUE.equals(inputRequired);
			}
			return Boolean.TRUE.equals(required);
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return false;
		}
	}

	@SuppressWarnings ("static-method")
	private boolean isSystemFieldId(final String fieldId) {
		final String lower = fieldId.toLowerCase();
		return lower.contains("-created") || lower.contains("-updated") || lower.contains("-version") || lower.contains("-createdby")
				|| lower.contains("-modified") || lower.contains("-company");
	}

	/** Navigate to the CPageTestAuxillary page. */
	private void navigateToTestAuxillaryPage() {
		try {
			final String url = "http://localhost:" + port + "/" + TEST_AUX_PAGE_ROUTE;
			LOGGER.debug("Navigating to: {}", url);
			page.navigate(url);
			wait_500();
			// Verify page loaded
			page.waitForSelector(BUTTON_SELECTOR + ", " + METADATA_SELECTOR, new Page.WaitForSelectorOptions().setTimeout(5000));
			LOGGER.info("✅ Successfully navigated to CPageTestAuxillary page");
		} catch (final Exception e) {
			throw new AssertionError("Failed to navigate to CPageTestAuxillary page: " + e.getMessage(), e);
		}
	}

	private FieldValueResult populateEditableFields(final String pageName) {
		final String baseValue = "Test-" + pageName;
		final String emailValue = buildEmailValue(pageName);
		final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "']");
		int textIndex = 0;
		String primaryValue = null;
		String primaryFieldId = null;
		if (pageName.toLowerCase().contains("approval")) {
			LOGGER.info("      🔎 Field IDs on {}:", pageName);
			for (int i = 0; i < fields.count(); i++) {
				final String fieldId = fields.nth(i).getAttribute("id");
				if (fieldId != null) {
					LOGGER.info("         - {}", fieldId);
				}
			}
		}
		if (pageName.toLowerCase().contains("grid")) {
			LOGGER.info("      🔎 Field IDs on {}:", pageName);
			for (int i = 0; i < fields.count(); i++) {
				final Locator field = fields.nth(i);
				final String fieldId = field.getAttribute("id");
				if (fieldId == null) {
					continue;
				}
				final String tagName = field.evaluate("el => el.tagName.toLowerCase()").toString();
				LOGGER.info("         - {} ({})", fieldId, tagName);
			}
		}
		if (pageName.toLowerCase().contains("component version")) {
			LOGGER.info("      🔎 Field IDs on {}:", pageName);
			for (int i = 0; i < fields.count(); i++) {
				final String fieldId = fields.nth(i).getAttribute("id");
				if (fieldId != null) {
					LOGGER.info("         - {}", fieldId);
				}
			}
		}
		primaryFieldId = findPreferredFieldId();
		if (primaryFieldId != null) {
			final Locator primaryField = locatorById(primaryFieldId);
			if (isFieldEditable(primaryField)) {
				final String currentValue = readFieldValueById(primaryFieldId);
				if (currentValue == null || currentValue.isBlank()) {
					final String value = isEmailField(primaryFieldId) ? emailValue : baseValue;
					fillFieldById(primaryFieldId, value);
					LOGGER.info("      ✓ Filled primary {} with {}", primaryFieldId, value);
					primaryValue = value;
					textIndex++;
				}
			}
		}
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final String fieldId = field.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (fieldId.equals(primaryFieldId)) {
				continue;
			}
			if (isSystemFieldId(fieldId)) {
				continue;
			}
			if (isEntityIdField(fieldId)) {
				if (isFieldEditable(field)) {
					final String currentValue = readFieldValueById(fieldId);
					if (currentValue == null || currentValue.isBlank()) {
						final String idValue = baseValue + "-id";
						fillFieldById(fieldId, idValue);
						LOGGER.info("      ✓ Filled entity id {} with {}", fieldId, idValue);
					}
				}
				continue;
			}
			if (isComboBoxById(fieldId)) {
				try {
					selectFirstComboBoxOptionById(fieldId);
					LOGGER.info("      ✓ Selected first option for {}", fieldId);
				} catch (final Exception e) {
					LOGGER.debug("      ⚠️ Could not select combo option for {}: {}", fieldId, e.getMessage());
				}
				continue;
			}
			if (field.locator("input").count() > 0 || field.locator("textarea").count() > 0) {
				final String currentValue = readFieldValueById(fieldId);
				if (currentValue == null || currentValue.isBlank()) {
					final String value = isEmailField(fieldId) ? emailValue
							: textIndex == 0 ? baseValue : baseValue + "-" + textIndex;
					fillFieldById(fieldId, value);
					LOGGER.info("      ✓ Filled {} with {}", fieldId, value);
					if (primaryValue == null) {
						primaryValue = value;
					}
					textIndex++;
				}
			}
		}
		if (textIndex == 0) {
			final String fallbackField = findEditableFieldId();
			if (fallbackField != null && !isComboBoxById(fallbackField)) {
				final String value = isEmailField(fallbackField) ? emailValue : baseValue;
				fillFieldById(fallbackField, value);
				LOGGER.info("      ✓ Filled fallback {} with {}", fallbackField, value);
				primaryValue = value;
				primaryFieldId = fallbackField;
			}
		}
		selectComboFieldByIdSubstring("approval-status");
		selectComboFieldByIdSubstring("projectcomponent");
		selectComboFieldByIdSubstring("project-component");
		selectComboFieldByLabel("Component");
		selectComboFieldByLabel("Project Component");
		selectComboFieldByEntityFieldIfPresent(CProjectComponentVersion.class, "projectComponent");
		selectComboFieldByEntityFieldIfPresent(CProductVersion.class, "product");
		selectComboFieldByIdSubstring("data-service");
		selectComboFieldByIdSubstring("service-bean");
		selectComboFieldByLabel("Data Service Bean");
		selectComboFieldByIdSuffix("-order");
		selectComboFieldByIdSuffix("-activity");
		ensureRequiredComboSelections();
		ensureStatusSelections();
		return new FieldValueResult(primaryFieldId, primaryValue);
	}

	private void selectComboFieldByLabel(final String label) {
		try {
			final Locator combo = page.locator("vaadin-combo-box[label='" + label + "'], c-navigable-combo-box[label='" + label + "']").first();
			if (combo.count() == 0) {
				return;
			}
			final String fieldId = combo.getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				return;
			}
			selectFirstComboBoxOptionById(fieldId);
			LOGGER.info("      ✓ Selected combo with label {}", label);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Failed selecting combo with label {}: {}", label, e.getMessage());
		}
	}

	private void selectComboFieldByEntityFieldIfPresent(final Class<?> entityClass, final String fieldName) {
		final String fieldId = computeFieldId(entityClass, fieldName);
		final Locator field = page.locator("#" + fieldId);
		if (field.count() == 0 || !isComboBoxById(fieldId)) {
			return;
		}
		final String currentValue = readFieldValueById(fieldId);
		if (currentValue != null && !currentValue.isBlank()) {
			return;
		}
		try {
			selectFirstComboBoxOptionById(fieldId);
			LOGGER.info("      ✓ Selected required combo {}", fieldId);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Failed to select required combo {}: {}", fieldId, e.getMessage());
		}
	}

	private void ensureStatusSelections() {
		final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "'][id*='status']");
		for (int i = 0; i < fields.count(); i++) {
			final String fieldId = fields.nth(i).getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (!isComboBoxById(fieldId) || !isFieldEditable(fields.nth(i))) {
				continue;
			}
			final String currentValue = readFieldValueById(fieldId);
			if (currentValue != null && !currentValue.isBlank()) {
				continue;
			}
			try {
				selectFirstComboBoxOptionById(fieldId);
				LOGGER.info("      ✓ Selected status combo {}", fieldId);
			} catch (final Exception e) {
				LOGGER.warn("      ⚠️ Failed to select status combo {}: {}", fieldId, e.getMessage());
			}
		}
	}

	private String buildEmailValue(final String pageName) {
		final String slug = pageName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
		final String safeSlug = slug.isBlank() ? "page" : slug;
		return "test-" + safeSlug + "@example.com";
	}

	private boolean isEmailField(final String fieldId) {
		return fieldId != null && fieldId.toLowerCase().contains("email");
	}

	// ==========================================
	// CRUD TOOLBAR TEST FUNCTIONS
	// ==========================================
	/** Run comprehensive CRUD toolbar tests on the current page.
	 * @param pageName Page name for screenshots */
	private void runCrudToolbarTests(String pageName) {
		try {
			// Test what buttons are available
			final boolean hasNew = checkCrudButtonExists(CRUD_NEW_BUTTON_ID);
			final boolean hasDelete = checkCrudButtonExists(CRUD_DELETE_BUTTON_ID);
			final boolean hasSave = checkCrudButtonExists(CRUD_SAVE_BUTTON_ID);
			final boolean hasRefresh = checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID);
			final boolean hasCancel = checkCrudButtonExists(CRUD_CANCEL_BUTTON_ID);
			LOGGER.info("   CRUD Buttons available:");
			LOGGER.info("      New: {}", hasNew);
			LOGGER.info("      Delete: {}", hasDelete);
			LOGGER.info("      Save: {}", hasSave);
			LOGGER.info("      Refresh: {}", hasRefresh);
			LOGGER.info("      Cancel: {}", hasCancel);
			if (hasRefresh) {
				testRefreshButton(pageName);
			}
			if (hasNew && hasSave) {
				testCreateAndSave(pageName);
			} else if (hasNew) {
				testNewButton(pageName);
			}
			if (hasSave && checkGridHasData()) {
				testUpdateAndSave(pageName);
			}
			if (hasSave) {
				testStatusChangeIfPresent(pageName);
			}
			if (hasDelete && checkGridHasData()) {
				testDeleteButton(pageName);
			}
			takeScreenshot(String.format("%03d-page-%s-crud-tested", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  CRUD toolbar tests encountered error: {}", e.getMessage());
		}
	}

	// ==========================================
	// GRID TEST FUNCTIONS
	// ==========================================
	/** Run comprehensive grid tests on the current page.
	 * @param pageName Page name for screenshots */
	private void runGridTests(String pageName) {
		try {
			// Test 1: Check if grid has data
			final boolean hasData = checkGridHasData();
			LOGGER.info("   ✓ Grid has data: {}", hasData);
			// Test 2: Check if grid is sortable
			final boolean isSortable = checkGridIsSortable();
			LOGGER.info("   ✓ Grid is sortable: {}", isSortable);
			if (isSortable) {
				// Test sorting on first column
				testGridSorting(pageName);
			}
			if (hasData) {
				testGridFiltering(pageName);
			}
			// Test 3: Count grid rows
			final int rowCount = getGridRowCount();
			LOGGER.info("   ✓ Grid row count: {}", rowCount);
			// Test 4: Try to select first row if data exists
			if (hasData && rowCount > 0) {
				testGridRowSelection(pageName);
			}
			takeScreenshot(String.format("%03d-page-%s-grid-tested", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Grid tests encountered error: {}", e.getMessage());
		}
	}

	private void selectComboFieldByIdSubstring(final String fragment) {
		final String fragmentLower = fragment == null ? "" : fragment.toLowerCase();
		final Locator fields = page.locator("[id^='field-']");
		for (int i = 0; i < fields.count(); i++) {
			final String fieldId = fields.nth(i).getAttribute("id");
			if (fieldId == null || fieldId.isBlank()) {
				continue;
			}
			if (!fieldId.toLowerCase().contains(fragmentLower)) {
				continue;
			}
			if (isComboBoxById(fieldId)) {
				try {
					selectFirstComboBoxOptionById(fieldId);
					LOGGER.info("      ✓ Selected required combo {}", fieldId);
				} catch (final Exception e) {
					LOGGER.debug("      ⚠️ Failed to select combo {}: {}", fieldId, e.getMessage());
				}
			}
			return;
		}
	}

	private void selectComboFieldByIdSuffix(final String suffix) {
		final Locator fields = page.locator("[id^='field-'][id$='" + suffix + "']");
		if (fields.count() == 0) {
			return;
		}
		final String fieldId = fields.first().getAttribute("id");
		if (fieldId == null || fieldId.isBlank()) {
			return;
		}
		if (isComboBoxById(fieldId)) {
			try {
				selectFirstComboBoxOptionById(fieldId);
				LOGGER.info("      ✓ Selected required combo {}", fieldId);
			} catch (final Exception e) {
				LOGGER.debug("      ⚠️ Failed to select combo {}: {}", fieldId, e.getMessage());
			}
		}
	}

	private String selectDifferentComboBoxOptionById(final String elementId, final String currentValue) {
		final Locator host = locatorById(elementId);
		Locator combo = host;
		final Locator embeddedCombo = host.locator("vaadin-combo-box, c-navigable-combo-box, c-combo-box");
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
		Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		if (options.count() == 0) {
			options = page.locator("vaadin-combo-box-item");
		}
		for (int i = 0; i < options.count(); i++) {
			final Locator option = options.nth(i);
			if (!option.isVisible()) {
				continue;
			}
			final String optionText = option.textContent() != null ? option.textContent().trim() : "";
			if (!optionText.isBlank() && (currentValue == null || !optionText.equals(currentValue.trim()))) {
				option.click();
				wait_500();
				return optionText;
			}
		}
		for (int i = 0; i < options.count(); i++) {
			final Locator option = options.nth(i);
			if (option.isVisible()) {
				option.click();
				wait_500();
				return option.textContent();
			}
		}
		if (input.count() > 0) {
			try {
				input.first().press("ArrowDown");
				input.first().press("Enter");
				wait_500();
				return readFieldValueById(elementId);
			} catch (final PlaywrightException e) {
				LOGGER.debug("Unable to select combo box {} via keyboard: {}", elementId, e.getMessage());
			}
		}
		return null;
	}

	private boolean selectGridRowByCellText(final String text) {
		if (text == null || text.isBlank()) {
			return false;
		}
		try {
			final Locator cells = page.locator("vaadin-grid-cell-content, [part='cell']").filter(new Locator.FilterOptions().setHasText(text));
			if (cells.count() == 0) {
				return false;
			}
			cells.first().click();
			wait_500();
			LOGGER.info("      ✓ Selected row containing '{}'", text);
			return true;
		} catch (final Exception e) {
			LOGGER.debug("      ⚠️ Failed selecting row by '{}': {}", text, e.getMessage());
			return false;
		}
	}

	@Test
	@DisplayName ("✅ Comprehensive test of all CPageTestAuxillary navigation buttons")
	void testAllAuxillaryPages() {
		LOGGER.info("🚀 Starting comprehensive CPageTestAuxillary test suite...");
		try {
			Files.createDirectories(Paths.get("target/screenshots"));
			// Step 1: Login to application
			LOGGER.info("📝 Step 1: Logging into application...");
			loginToApplication();
			takeScreenshot(String.format("%03d-after-login", screenshotCounter++), false);
			// Step 2: Discover navigation targets
			final String targetRoute = System.getProperty("test.targetRoute");
			final String targetButtonId = System.getProperty("test.targetButtonId");
			final String routeKeyword = System.getProperty("test.routeKeyword");
			List<ButtonInfo> buttons;
			if (targetRoute != null && !targetRoute.isBlank()) {
				LOGGER.info("🎯 Targeting single route from test.targetRoute: {}", targetRoute);
				final ButtonInfo info = new ButtonInfo();
				info.index = 0;
				info.id = targetButtonId != null ? targetButtonId : "direct-route";
				info.title = targetRoute;
				info.route = targetRoute;
				buttons = List.of(info);
			} else {
				LOGGER.info("🧭 Step 2: Navigating to CPageTestAuxillary page...");
				navigateToTestAuxillaryPage();
				wait_2000(); // Give time for buttons to be populated
				takeScreenshot(String.format("%03d-test-auxillary-page", screenshotCounter++), false);
				// Step 3: Discover all navigation buttons dynamically
				LOGGER.info("🔍 Step 3: Discovering navigation buttons...");
				buttons = discoverNavigationButtons();
				if (routeKeyword != null && !routeKeyword.isBlank()) {
					final String keyword = routeKeyword.toLowerCase();
					buttons = buttons.stream().filter(b -> b.route != null && b.route.toLowerCase().contains(keyword)
							|| b.title != null && b.title.toLowerCase().contains(keyword)).toList();
					LOGGER.info("🎯 Filtered buttons by test.routeKeyword: {}", routeKeyword);
				}
				if (targetButtonId != null && !targetButtonId.isBlank()) {
					buttons = buttons.stream().filter(b -> targetButtonId.equals(b.id)).toList();
					LOGGER.info("🎯 Filtered buttons by test.targetButtonId: {}", targetButtonId);
				}
			}
			LOGGER.info("📊 Found {} navigation buttons to test", buttons.size());
			if (buttons.isEmpty()) {
				throw new AssertionError("No navigation buttons found for provided target parameters");
			}
			// Step 4: Test each button's target page
			LOGGER.info("🧪 Step 4: Testing each navigation button's target page...");
			LOGGER.info("Will test {} buttons by navigating directly to their routes", buttons.size());
			for (int i = 0; i < buttons.size(); i++) {
				final ButtonInfo button = buttons.get(i);
				LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
				LOGGER.info("🎯 Testing button {}/{}: {}", i + 1, buttons.size(), button.title);
				LOGGER.info("   Route: {}", button.route);
				LOGGER.info("   Button ID: {}", button.id);
				testNavigationButton(button, i + 1, buttons.size());
			}
			// Step 5: Summary
			LOGGER.info("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
			LOGGER.info("✅ Test suite completed successfully!");
			LOGGER.info("📊 Summary:");
			LOGGER.info("   Total buttons tested: {}", buttons.size());
			LOGGER.info("   Pages visited: {}", pagesVisited);
			LOGGER.info("   Pages with grids: {}", gridPagesFound);
			LOGGER.info("   Pages with CRUD toolbars: {}", crudPagesFound);
			LOGGER.info("   Screenshots captured: {}", screenshotCounter - 1);
		} catch (final Exception e) {
			LOGGER.error("❌ Test suite failed: {}", e.getMessage(), e);
			takeScreenshot("error-comprehensive-test", true);
			throw new AssertionError("Comprehensive test suite failed", e);
		}
	}

	private void testCreateAndSave(String pageName) {
		try {
			LOGGER.info("   🧾 Testing New + Save workflow...");
			final Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			if (newButton.count() == 0) {
				return;
			}
			final boolean hasGrid = checkGridExists();
			final int beforeCount = hasGrid ? getGridRowCountSafe() : -1;
			locatorById(CRUD_NEW_BUTTON_ID).click();
			wait_500();
			final FieldValueResult createdResult = populateEditableFields(pageName);
			testRelationMembershipEdits(pageName);
			final String createdMarker = createdResult.value;
			if (createdMarker != null && !createdMarker.isBlank()) {
				lastCreatedValues.put(pageName, createdMarker);
			}
			if (createdResult.fieldId != null && !createdResult.fieldId.isBlank()) {
				lastCreatedFieldIds.put(pageName, createdResult.fieldId);
			}
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_500();
			performFailFastCheck("CRUD Save New");
			if (checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID)) {
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
			}
			if (hasGrid) {
				final int afterCount = getGridRowCountSafe();
				if (afterCount <= beforeCount) {
					LOGGER.warn("      ⚠️ Create did not increase grid row count ({} -> {})", beforeCount, afterCount);
				} else {
					LOGGER.info("      ✓ Created row ({} -> {})", beforeCount, afterCount);
				}
			}
			takeScreenshot(String.format("%03d-page-%s-created", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Create + Save test failed: {}", e.getMessage());
		}
	}

	private void testDeleteButton(String pageName) {
		try {
			LOGGER.info("   🗑️ Testing Delete button...");
			final int beforeCount = getGridRowCountSafe();
			if (beforeCount == 0) {
				LOGGER.warn("      ⚠️ No rows available to delete");
				return;
			}
			final String createdMarker = lastCreatedValues.get(pageName);
			if (createdMarker == null || createdMarker.isBlank()) {
				LOGGER.warn("      ⚠️ No created marker found for {}, skipping delete to avoid removing sample data", pageName);
				return;
			}
			final boolean selected = selectGridRowByCellText(createdMarker);
			if (!selected) {
				LOGGER.warn("      ⚠️ Created row not found for {}, skipping delete", pageName);
				return;
			}
			final String createdFieldId = lastCreatedFieldIds.get(pageName);
			if (createdFieldId != null && !createdFieldId.isBlank()) {
				final String currentValue = readFieldValueById(createdFieldId);
				if (currentValue == null || !currentValue.trim().equals(createdMarker.trim())) {
					LOGGER.warn("      ⚠️ Form selection mismatch for {}, skipping delete", pageName);
					return;
				}
			}
			locatorById(CRUD_DELETE_BUTTON_ID).click();
			wait_500();
			confirmDialogIfPresent();
			wait_500();
			if (checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID)) {
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
			}
			final int afterCount = getGridRowCountSafe();
			if (afterCount >= beforeCount) {
				LOGGER.warn("      ⚠️ Delete did not reduce grid row count ({} -> {})", beforeCount, afterCount);
			} else {
				LOGGER.info("      ✓ Deleted row ({} -> {})", beforeCount, afterCount);
			}
			performFailFastCheck("CRUD Delete");
			takeScreenshot(String.format("%03d-page-%s-deleted", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Delete button test failed: {}", e.getMessage());
		}
	}

	/** Test grid row selection.
	 * @param pageName Page name for screenshots */
	private void testGridRowSelection(String pageName) {
		try {
			LOGGER.info("   🖱️  Testing grid row selection...");
			final Locator grid = page.locator(GRID_SELECTOR).first();
			final Locator cells = grid.locator("vaadin-grid-cell-content, [part='cell']");
			if (cells.count() > 0) {
				if (!cells.first().isVisible()) {
					LOGGER.warn("      ⚠️ First grid cell is not visible, skipping row selection");
					return;
				}
				cells.first().click();
				wait_500();
				LOGGER.info("      ✓ Selected first row");
				takeScreenshot(String.format("%03d-page-%s-row-selected", screenshotCounter++, pageName), false);
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Grid row selection test failed: {}", e.getMessage());
		}
	}

	/** Test grid sorting functionality.
	 * @param pageName Page name for screenshots */
	private void testGridSorting(String pageName) {
		try {
			LOGGER.info("   🔄 Testing grid sorting...");
			final Locator sorters = page.locator("vaadin-grid-sorter");
			if (sorters.count() > 0) {
				// Click first sorter to sort ascending
				sorters.first().click();
				wait_500();
				LOGGER.info("      ✓ Sorted ascending");
				// Click again to sort descending
				sorters.first().click();
				wait_500();
				LOGGER.info("      ✓ Sorted descending");
				takeScreenshot(String.format("%03d-page-%s-sorted", screenshotCounter++, pageName), false);
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Grid sorting test failed: {}", e.getMessage());
		}
	}

	private void testGridFiltering(String pageName) {
		try {
			final Locator searchField = page.locator("vaadin-text-field[placeholder='Search...']");
			if (searchField.count() == 0) {
				return;
			}
			final String searchTerm = getFirstGridCellText();
			if (searchTerm == null || searchTerm.isBlank()) {
				return;
			}
			final int beforeCount = getGridRowCountSafe();
			applyGridSearchFilter(searchTerm);
			wait_500();
			final int afterCount = getGridRowCountSafe();
			LOGGER.info("      ✓ Filtered grid using '{}' ({} -> {})", searchTerm, beforeCount, afterCount);
			applyGridSearchFilter("");
			wait_500();
			takeScreenshot(String.format("%03d-page-%s-filtered", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Grid filtering test failed: {}", e.getMessage());
		}
	}

	private void testRelationMembershipEdits(String pageName) {
		final Locator fields = page.locator("[id^='" + FIELD_ID_PREFIX + "']");
		for (int i = 0; i < fields.count(); i++) {
			final Locator field = fields.nth(i);
			final Locator addButton = field.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Add"));
			final Locator removeButton = field.locator("vaadin-button").filter(new Locator.FilterOptions().setHasText("Remove"));
			if (addButton.count() == 0 || removeButton.count() == 0) {
				continue;
			}
			final Locator grids = field.locator("vaadin-grid");
			if (grids.count() == 0) {
				continue;
			}
			LOGGER.info("   🔗 Testing relation membership controls...");
			try {
				final Locator availableGrid = grids.first();
				final Locator availableCell = availableGrid.locator("vaadin-grid-cell-content, [part='cell']").first();
				if (availableCell.count() == 0) {
					LOGGER.warn("      ⚠️ No available items to add for relation field");
					return;
				}
				availableCell.click();
				wait_500();
				addButton.first().click();
				wait_500();
				final Locator selectedGrid = grids.count() > 1 ? grids.nth(1) : grids.first();
				final Locator selectedCell = selectedGrid.locator("vaadin-grid-cell-content, [part='cell']").first();
				if (selectedCell.count() > 0) {
					selectedCell.click();
					wait_500();
					removeButton.first().click();
					wait_500();
				}
				takeScreenshot(String.format("%03d-page-%s-relation-membership", screenshotCounter++, pageName), false);
			} catch (final Exception e) {
				LOGGER.warn("⚠️  Relation membership test failed: {}", e.getMessage());
			}
			return;
		}
	}

	private boolean hasKanbanBoard() {
		return page.locator(".kanban-column").count() > 0;
	}

	private void runKanbanBoardTests(String pageName) {
		try {
			final Locator columns = page.locator(".kanban-column");
			if (columns.count() == 0) {
				return;
			}
			final Locator postits = page.locator(".kanban-postit");
			if (postits.count() == 0) {
				LOGGER.warn("   ⚠️ Kanban board has no post-it cards to test");
				return;
			}
			editKanbanStoryPoints(pageName, postits.first());
			dragKanbanPostitBetweenColumns(pageName, columns, postits.first());
			dragKanbanPostitToBacklog(pageName, columns, postits.first());
			dragBacklogItemToColumn(pageName, columns);
			takeScreenshot(String.format("%03d-page-%s-kanban-tested", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Kanban board tests failed: {}", e.getMessage());
		}
	}

	private void editKanbanStoryPoints(String pageName, Locator postit) {
		try {
			postit.click();
			wait_500();
			final Locator editor = postit.locator("vaadin-text-field").first();
			if (editor.count() == 0) {
				return;
			}
			final Locator input = editor.locator("input");
			if (input.count() == 0) {
				return;
			}
			input.fill("5");
			input.press("Enter");
			wait_500();
			LOGGER.info("      ✓ Updated kanban story points to 5");
			takeScreenshot(String.format("%03d-page-%s-kanban-storypoints", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Kanban story point edit failed: {}", e.getMessage());
		}
	}

	private void dragKanbanPostitBetweenColumns(String pageName, Locator columns, Locator postit) {
		try {
			if (columns.count() < 2) {
				return;
			}
			Locator targetColumn = null;
			for (int i = 0; i < columns.count(); i++) {
				final Locator column = columns.nth(i);
				final String text = column.textContent();
				if (text != null && text.contains("Backlog")) {
					continue;
				}
				targetColumn = column;
				break;
			}
			if (targetColumn == null) {
				return;
			}
			postit.dragTo(targetColumn);
			wait_1000();
			LOGGER.info("      ✓ Dragged post-it to another kanban column");
			takeScreenshot(String.format("%03d-page-%s-kanban-drag", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Kanban column drag failed: {}", e.getMessage());
		}
	}

	private void dragKanbanPostitToBacklog(String pageName, Locator columns, Locator postit) {
		try {
			final Locator backlogColumn = columns.filter(new Locator.FilterOptions().setHasText("Backlog")).first();
			if (backlogColumn.count() == 0) {
				return;
			}
			postit.dragTo(backlogColumn);
			wait_1000();
			LOGGER.info("      ✓ Dragged post-it to backlog column");
			takeScreenshot(String.format("%03d-page-%s-kanban-backlog", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Kanban backlog drag failed: {}", e.getMessage());
		}
	}

	private void dragBacklogItemToColumn(String pageName, Locator columns) {
		try {
			final Locator backlogColumn = columns.filter(new Locator.FilterOptions().setHasText("Backlog")).first();
			if (backlogColumn.count() == 0) {
				return;
			}
			Locator destination = null;
			for (int i = 0; i < columns.count(); i++) {
				final Locator column = columns.nth(i);
				final String text = column.textContent();
				if (text == null || !text.contains("Backlog")) {
					destination = column;
					break;
				}
			}
			if (destination == null) {
				return;
			}
			final Locator backlogGrid = backlogColumn.locator("vaadin-grid");
			if (backlogGrid.count() == 0) {
				return;
			}
			final Locator backlogCell = backlogGrid.first().locator("vaadin-grid-cell-content, [part='cell']").first();
			if (backlogCell.count() == 0) {
				return;
			}
			backlogCell.dragTo(destination);
			wait_1000();
			LOGGER.info("      ✓ Dragged backlog item to kanban column");
			takeScreenshot(String.format("%03d-page-%s-backlog-to-column", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️ Backlog to column drag failed: {}", e.getMessage());
		}
	}

	/** Test a single navigation button and its target page.
	 * @param button       Button information
	 * @param buttonNum    Button number (1-based)
	 * @param totalButtons Total number of buttons */
	private void testNavigationButton(ButtonInfo button, int buttonNum, int totalButtons) {
		try {
			// Navigate directly to the route instead of clicking the button
			// This is more reliable than clicking Vaadin buttons with JavaScript handlers
			LOGGER.info("🧭 Navigating to: {} (button: {})", button.route, button.title);
			if (button.route == null || button.route.isEmpty()) {
				LOGGER.warn("⚠️  Button has no route: {}", button.title);
				return;
			}
			final String targetUrl = "http://localhost:" + port + "/" + button.route;
			page.navigate(targetUrl);
			try {
				wait_1000(); // Wait for navigation and page load
			} catch (final AssertionError e) {
				throw new AssertionError("Exception dialog detected while navigating to: " + button.title + " (" + button.route + ")", e);
			}
			pagesVisited++;
			// Take initial screenshot
			final String pageNameSafe = sanitizeForFileName(button.title, "page-" + button.index);
			takeScreenshot(String.format("%03d-page-%s-initial", screenshotCounter++, pageNameSafe), false);
			// Check what's on the page and run appropriate tests
			LOGGER.info("🔍 Analyzing page content...");
			final boolean hasGrid = checkGridExists();
			final boolean hasCrudToolbar = checkCrudToolbarExists();
			LOGGER.info("   Grid present: {}", hasGrid);
			LOGGER.info("   CRUD toolbar present: {}", hasCrudToolbar);
			// Run conditional tests based on page content
			if (hasGrid) {
				LOGGER.info("📊 Running grid tests...");
				runGridTests(pageNameSafe);
				gridPagesFound++;
			} else {
				LOGGER.info("ℹ️  No grid found, skipping grid tests");
			}
			if (hasCrudToolbar) {
				LOGGER.info("🔧 Running CRUD toolbar tests...");
				runCrudToolbarTests(pageNameSafe);
				crudPagesFound++;
			} else {
				LOGGER.info("ℹ️  No CRUD toolbar found, skipping CRUD tests");
			}
			if (hasKanbanBoard()) {
				LOGGER.info("🗂️  Running kanban board tests...");
				runKanbanBoardTests(pageNameSafe);
			}
			// Take final screenshot
			takeScreenshot(String.format("%03d-page-%s-final", screenshotCounter++, pageNameSafe), false);
			LOGGER.info("✅ Completed testing button {}/{}: {}", buttonNum, totalButtons, button.title);
		} catch (final Exception e) {
			LOGGER.error("❌ Failed to test button: {} - {}", button.title, e.getMessage(), e);
			takeScreenshot("error-button-" + button.index, true);
			// Don't throw - continue with next button
		}
	}

	/** Test the New button functionality.
	 * @param pageName Page name for screenshots */
	private void testNewButton(String pageName) {
		try {
			LOGGER.info("   ➕ Testing New button...");
			final Locator newButton = page.locator("#" + CRUD_NEW_BUTTON_ID);
			if (newButton.count() > 0) {
				locatorById(CRUD_NEW_BUTTON_ID).click();
				wait_500();
				LOGGER.info("      ✓ Clicked New button");
				takeScreenshot(String.format("%03d-page-%s-new-clicked", screenshotCounter++, pageName), false);
				// Check if a form or dialog appeared
				final boolean hasDialog = page.locator("vaadin-dialog, vaadin-dialog-overlay").count() > 0;
				final boolean hasFormLayout = page.locator("vaadin-form-layout").count() > 0;
				LOGGER.info("      Dialog/Form appeared: {}", hasDialog || hasFormLayout);
				// Try to close dialog/form if opened
				if (hasDialog || hasFormLayout) {
					// Look for Cancel button to close
					final Locator cancelButton = page.locator("#" + CRUD_CANCEL_BUTTON_ID);
					if (cancelButton.count() > 0) {
						cancelButton.first().click();
						wait_500();
						LOGGER.info("      ✓ Closed form via Cancel button");
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️  New button test failed: {}", e.getMessage());
		}
	}

	/** Test the Edit button functionality.
	 * @param pageName Page name for screenshots */
	private void testRefreshButton(String pageName) {
		try {
			LOGGER.info("   🔄 Testing Refresh button...");
			final Locator refreshButton = page.locator("#" + CRUD_REFRESH_BUTTON_ID);
			if (refreshButton.count() > 0) {
				if (!refreshButton.first().isEnabled()) {
					LOGGER.info("      ℹ️ Refresh button is disabled, skipping");
					return;
				}
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
				performFailFastCheck("CRUD Refresh");
				takeScreenshot(String.format("%03d-page-%s-refresh-clicked", screenshotCounter++, pageName), false);
				LOGGER.info("      ✓ Clicked Refresh button");
			}
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Refresh button test failed: {}", e.getMessage());
		}
	}

	private void testStatusChangeIfPresent(String pageName) {
		try {
			final Locator statusFields = page.locator("[id^='" + FIELD_ID_PREFIX + "'][id*='status']");
			if (statusFields.count() == 0) {
				return;
			}
			final String statusFieldId = statusFields.first().getAttribute("id");
			if (statusFieldId == null || statusFieldId.isBlank()) {
				return;
			}
			LOGGER.info("   🟣 Testing status field update via {}", statusFieldId);
			final String before = readFieldValueById(statusFieldId);
			final String selected = selectDifferentComboBoxOptionById(statusFieldId, before);
			if (selected == null) {
				LOGGER.warn("      ⚠️ No alternate status value available for {}", statusFieldId);
				return;
			}
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_500();
			if (checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID)) {
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
			}
			final String after = readFieldValueById(statusFieldId);
			if (after != null && !after.isBlank() && !after.trim().equals(before == null ? "" : before.trim())) {
				LOGGER.info("      ✓ Status updated: {} -> {}", before, after);
			} else {
				LOGGER.warn("      ⚠️ Status value did not change after save for {}", statusFieldId);
			}
			performFailFastCheck("CRUD Status Save");
			takeScreenshot(String.format("%03d-page-%s-status-updated", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Status change test failed: {}", e.getMessage());
		}
	}

	private void testUpdateAndSave(String pageName) {
		try {
			LOGGER.info("   ✏️  Testing Update + Save workflow...");
			if (!checkGridHasData()) {
				return;
			}
			testGridRowSelection(pageName);
			testRelationMembershipEdits(pageName);
			final String fieldId = findEditableFieldId();
			if (fieldId == null) {
				LOGGER.warn("      ⚠️ No editable field found for update workflow");
				return;
			}
			final String beforeValue = readFieldValueById(fieldId);
			if (isComboBoxById(fieldId)) {
				final String selected = selectDifferentComboBoxOptionById(fieldId, beforeValue);
				if (selected == null) {
					LOGGER.warn("      ⚠️ No alternate combo value available for {}", fieldId);
				} else {
					LOGGER.info("      ✓ Updated combo {} to {}", fieldId, selected);
				}
			} else {
				final String updateValue = "Updated-" + pageName;
				fillFieldById(fieldId, updateValue);
				LOGGER.info("      ✓ Updated field {} with {}", fieldId, updateValue);
			}
			locatorById(CRUD_SAVE_BUTTON_ID).click();
			wait_500();
			performFailFastCheck("CRUD Save Update");
			if (checkCrudButtonExists(CRUD_REFRESH_BUTTON_ID)) {
				locatorById(CRUD_REFRESH_BUTTON_ID).click();
				wait_500();
			}
			final String afterValue = readFieldValueById(fieldId);
			if (afterValue != null && beforeValue != null && afterValue.trim().equals(beforeValue.trim())) {
				LOGGER.warn("      ⚠️ Update value did not change for {}", fieldId);
			} else {
				LOGGER.info("      ✓ Updated value for {} ({} -> {})", fieldId, beforeValue, afterValue);
			}
			takeScreenshot(String.format("%03d-page-%s-updated", screenshotCounter++, pageName), false);
		} catch (final Exception e) {
			LOGGER.warn("⚠️  Update + Save test failed: {}", e.getMessage());
		}
	}
}
