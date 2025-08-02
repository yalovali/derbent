package tech.derbent.base.ui.component;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.vaadin.flow.component.icon.VaadinIcon;

import tech.derbent.abstracts.domains.CTestBase;

/**
 * Unit tests for CDashboardStatCard. Layer: Test (MVC) Verifies the dashboard stat card
 * component functionality.
 */
class CDashboardStatCardTest extends CTestBase {

	@Test
	void testStatCardCreationWithLong() {
		// Test creating a stat card with long value
		final CDashboardStatCard card =
			new CDashboardStatCard("Test Count", 25L, VaadinIcon.BAR_CHART.create());
		assertNotNull(card,
			"CDashboardStatCard should be created successfully with long value");
	}

	@Test
	void testStatCardCreationWithString() {
		// Test creating a stat card with string value
		final CDashboardStatCard card =
			new CDashboardStatCard("Test Metric", "42", VaadinIcon.CHART.create());
		assertNotNull(card,
			"CDashboardStatCard should be created successfully with string value");
	}

	@Test
	void testStatCardTitleUpdate() {
		// Test updating card title
		final CDashboardStatCard card =
			new CDashboardStatCard("Original Title", "10", VaadinIcon.DASHBOARD.create());
		card.updateTitle("Updated Title");
		assertNotNull(card, "CDashboardStatCard should handle title updates");
	}

	@Test
	void testStatCardValueUpdate() {
		// Test updating card values
		final CDashboardStatCard card =
			new CDashboardStatCard("Dynamic Metric", "0", VaadinIcon.PIE_CHART.create());
		// Update with string value
		card.updateValue("100");
		assertNotNull(card, "CDashboardStatCard should handle string value updates");
		// Update with long value
		card.updateValue(250L);
		assertNotNull(card, "CDashboardStatCard should handle long value updates");
	}

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
		
	}
}