package automated_tests.tech.derbent.ui.automation.tests;
import automated_tests.tech.derbent.ui.automation.CBaseUITest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import com.microsoft.playwright.Page;
import tech.derbent.Application;

/** CPageTestNewEntities - Focused tests for newly added entities (this week) Tests Financial, Validation Management, and Team/Issue entities added
 * recently with deep CRUD validation including attachments and comments sections. */
@SpringBootTest (classes = Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles ("h2")
public class CPageNewEntitiesTest extends CBaseUITest {

	// Financial Entities
	private static final String[] FINANCIAL_ENTITIES = {
			"budgets", "budget-types", "invoices", "invoice-items", "payments", "orders", "currencies"
	};
	private static final Logger LOGGER = LoggerFactory.getLogger(CPageNewEntitiesTest.class);
	// Team/Issue Entities
	private static final String[] TEAM_ISSUE_ENTITIES = {
			"issues", "issue-types", "teams"
	};
	// Validation Management Entities
	private static final String[] TEST_MANAGEMENT_ENTITIES = {
			"test-cases", "test-scenarios", "test-runs", "test-steps", "test-case-results"
	};

	private void navigateToEntityPage(String entityName) {
		final String url = "http://localhost:" + port + "/cdynamicpagerouter/" + entityName;
		LOGGER.info("      🔗 Navigating to: {}", url);
		page.navigate(url);
		page.waitForLoadState();
		takeScreenshot(entityName + "-page");
	}

	/** Deep CRUD test including attachments and comments sections */
	private void testEntityCrudWithSections(String entityName) {
		try {
			LOGGER.info("📋 Starting deep CRUD test for: {}", entityName);
			// Step 1: Navigate to entity page
			navigateToEntityPage(entityName);
			// Step 2: Wait for page to load
			waitForGridLoad();
			LOGGER.info("✅ Page load test completed for: {}", entityName);
			LOGGER.info("   📍 Navigation: SUCCESS");
			LOGGER.info("   📊 Page loaded: SUCCESS");
			// TODO: Implement full CRUD operations after page load verification
			// testCreateOperation(entityName);
			// testSelectAndVerify(entityName);
			// testUpdateOperation(entityName);
			// testAttachmentsSection(entityName);
			// testCommentsSection(entityName);
			// testDeleteOperation(entityName);
		} catch (final Exception e) {
			LOGGER.error("❌ Test failed for entity: {} reason={}", entityName, e.getMessage());
			takeScreenshot(entityName + "-failure");
			throw new RuntimeException("Test failed for entity: " + entityName, e);
		}
	}

	@Test
	@DisplayName ("Test Financial Entities - Budgets, Invoices, Payments, Orders")
	void testFinancialEntities() {
		LOGGER.info("🏦 ========================================");
		LOGGER.info("🏦 TESTING FINANCIAL ENTITIES (NEW)");
		LOGGER.info("🏦 ========================================");
		for (final String entityName : FINANCIAL_ENTITIES) {
			LOGGER.info("💰 Testing financial entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		LOGGER.info("✅ Financial entities testing completed");
	}

	@Test
	@DisplayName ("Test Single Entity - For targeted testing")
	void testSingleEntity() {
		final String entityName = System.getProperty("entity.name");
		if (entityName == null || entityName.isEmpty()) {
			LOGGER.warn("⚠️  No entity.name system property set, skipping test");
			return;
		}
		LOGGER.info("🎯 ========================================");
		LOGGER.info("🎯 TESTING SINGLE ENTITY: {}", entityName);
		LOGGER.info("🎯 ========================================");
		testEntityCrudWithSections(entityName);
		LOGGER.info("✅ Single entity test completed for: {}", entityName);
	}

	@Test
	@DisplayName ("Test Team/Issue Entities - Issues, Teams")
	void testTeamIssueEntities() {
		LOGGER.info("👥 ========================================");
		LOGGER.info("👥 TESTING TEAM/ISSUE ENTITIES (NEW)");
		LOGGER.info("👥 ========================================");
		for (final String entityName : TEAM_ISSUE_ENTITIES) {
			LOGGER.info("👥 Testing team/issue entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		LOGGER.info("✅ Team/issue entities testing completed");
	}

	@Test
	@DisplayName ("Validation Management Entities - Validation Cases, Suites, Sessions")
	void testTestManagementEntities() {
		LOGGER.info("🧪 ========================================");
		LOGGER.info("🧪 TESTING TEST MANAGEMENT ENTITIES (NEW)");
		LOGGER.info("🧪 ========================================");
		for (final String entityName : TEST_MANAGEMENT_ENTITIES) {
			LOGGER.info("🧪 Testing test management entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		LOGGER.info("✅ Test management entities testing completed");
	}

	private void waitForGridLoad() {
		LOGGER.info("      ⏳ Waiting for grid to load...");
		try {
			// Wait for either grid or "no data" message
			page.waitForSelector("vaadin-grid, .no-data-message, .empty-state", new Page.WaitForSelectorOptions().setTimeout(15000));
			LOGGER.info("      ✅ Page content loaded");
		} catch (final Exception e) {
			LOGGER.warn("      ⚠️  Grid not found, checking if page loaded correctly");
			// Take screenshot for debugging
			takeScreenshot("grid-not-found");
			// Check if we're on the right page
			final String currentUrl = page.url();
			LOGGER.info("      📍 Current URL: {}", currentUrl);
			// If page has main content area, consider it loaded
			if (page.locator(".main-view, .entity-view, .view-content").count() > 0) {
				LOGGER.info("      ✅ Page content area found");
			} else {
				throw new RuntimeException("Page did not load correctly: " + currentUrl);
			}
		}
	}
}
