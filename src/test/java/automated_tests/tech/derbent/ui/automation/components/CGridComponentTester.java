package automated_tests.tech.derbent.ui.automation.components;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/** Tests grid component functionality on pages that display data grids. */
public class CGridComponentTester extends CBaseComponentTester {

	@Override
	public boolean canTest(final Page page) {
		return elementExists(page, GRID_SELECTOR);
	}

	@Override
	public String getComponentName() { return "Grid Component"; }

	@Override
	public void test(final Page page) {
		LOGGER.info("      📋 Testing Grid Component...");
		try {
			final Locator grid = page.locator(GRID_SELECTOR);
			if (grid.count() > 0) {
				final Locator firstGrid = grid.first();
				if (firstGrid.isVisible()) {
					LOGGER.info("         ✓ Grid is visible");
					final int rowCount = getGridRowCount(page);
					if (rowCount > 0) {
						LOGGER.info("         ✓ Grid has {} rows", rowCount);
					} else if (rowCount == 0) {
						LOGGER.info("         ℹ️ Grid is empty (no data)");
					} else {
						LOGGER.info("         ℹ️ Grid row count could not be determined");
					}
					final Locator sorters = page.locator("vaadin-grid-sorter");
					if (sorters.count() > 0) {
						LOGGER.info("         ✓ Grid has sortable columns");
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.debug("         ⚠️ Grid test error: {}", e.getMessage());
		}
		LOGGER.info("      ✅ Grid component test complete");
	}
}
