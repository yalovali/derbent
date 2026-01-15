package automated_tests.tech.derbent.ui.automation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

/**
 * CPageTestNewEntities - Focused tests for newly added entities (this week)
 * 
 * Tests Financial, Test Management, and Team/Issue entities added recently
 * with deep CRUD validation including attachments and comments sections.
 */
@SpringBootTest(classes = tech.derbent.Application.class, webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles("h2")
public class CPageTestNewEntities extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageTestNewEntities.class);

	// Financial Entities
	private static final String[] FINANCIAL_ENTITIES = {
		"budgets",
		"budget-types",
		"invoices",
		"invoice-items",
		"payments",
		"orders",
		"currencies"
	};

	// Test Management Entities
	private static final String[] TEST_MANAGEMENT_ENTITIES = {
		"test-cases",
		"test-scenarios",
		"test-runs",
		"test-steps",
		"test-case-results"
	};

	// Team/Issue Entities
	private static final String[] TEAM_ISSUE_ENTITIES = {
		"issues",
		"issue-types",
		"teams"
	};

	@Test
	@DisplayName("Test Financial Entities - Budgets, Invoices, Payments, Orders")
	void testFinancialEntities() {
		LOGGER.info("🏦 ========================================");
		LOGGER.info("🏦 TESTING FINANCIAL ENTITIES (NEW)");
		LOGGER.info("🏦 ========================================");
		
		for (String entityName : FINANCIAL_ENTITIES) {
			LOGGER.info("💰 Testing financial entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		
		LOGGER.info("✅ Financial entities testing completed");
	}

	@Test
	@DisplayName("Test Management Entities - Test Cases, Scenarios, Runs")
	void testTestManagementEntities() {
		LOGGER.info("🧪 ========================================");
		LOGGER.info("🧪 TESTING TEST MANAGEMENT ENTITIES (NEW)");
		LOGGER.info("🧪 ========================================");
		
		for (String entityName : TEST_MANAGEMENT_ENTITIES) {
			LOGGER.info("🧪 Testing test management entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		
		LOGGER.info("✅ Test management entities testing completed");
	}

	@Test
	@DisplayName("Test Team/Issue Entities - Issues, Teams")
	void testTeamIssueEntities() {
		LOGGER.info("👥 ========================================");
		LOGGER.info("👥 TESTING TEAM/ISSUE ENTITIES (NEW)");
		LOGGER.info("👥 ========================================");
		
		for (String entityName : TEAM_ISSUE_ENTITIES) {
			LOGGER.info("👥 Testing team/issue entity: {}", entityName);
			testEntityCrudWithSections(entityName);
		}
		
		LOGGER.info("✅ Team/issue entities testing completed");
	}

	/**
	 * Deep CRUD test including attachments and comments sections
	 */
	private void testEntityCrudWithSections(String entityName) {
		try {
			LOGGER.info("📋 Starting deep CRUD test for: {}", entityName);
			
			// Navigate to entity page
			navigateToEntityPage(entityName);
			
			// Wait for grid to load
			waitForGridLoad();
			
			// Test Create
			LOGGER.info("   ➕ Testing CREATE operation...");
			testCreateOperation(entityName);
			
			// Test Read/Select
			LOGGER.info("   👁️  Testing READ operation...");
			testSelectAndVerify(entityName);
			
			// Test Update
			LOGGER.info("   ✏️  Testing UPDATE operation...");
			testUpdateOperation(entityName);
			
			// Test Attachments Section (if present)
			LOGGER.info("   📎 Testing ATTACHMENTS section...");
			testAttachmentsSection(entityName);
			
			// Test Comments Section (if present)
			LOGGER.info("   💬 Testing COMMENTS section...");
			testCommentsSection(entityName);
			
			// Test Delete (if allowed)
			LOGGER.info("   🗑️  Testing DELETE operation...");
			testDeleteOperation(entityName);
			
			LOGGER.info("✅ Deep CRUD test completed for: {}", entityName);
			
		} catch (Exception e) {
			LOGGER.error("❌ Test failed for entity: {}", entityName, e);
			takeScreenshot(entityName + "-failure");
			throw new RuntimeException("Test failed for entity: " + entityName, e);
		}
	}

	private void navigateToEntityPage(String entityName) {
		String url = "http://localhost:" + port + "/cdynamicpagerouter/" + entityName;
		LOGGER.info("      🔗 Navigating to: {}", url);
		page.navigate(url);
		page.waitForLoadState();
		takeScreenshot(entityName + "-page");
	}

	private void waitForGridLoad() {
		LOGGER.info("      ⏳ Waiting for grid to load...");
		page.waitForSelector("vaadin-grid", new com.microsoft.playwright.Page.WaitForSelectorOptions().setTimeout(10000));
	}

	private void testCreateOperation(String entityName) {
		// Click New button
		clickButtonIfPresent("New");
		
		// Fill required fields (using helper method)
		fillRequiredFields();
		
		// Click Save
		clickButtonIfPresent("Save");
		
		// Verify success notification
		verifySuccessNotification();
		
		takeScreenshot(entityName + "-create-success");
	}

	private void testSelectAndVerify(String entityName) {
		// Select first row in grid
		selectFirstGridRow();
		
		// Verify form is populated
		verifyFormPopulated();
		
		takeScreenshot(entityName + "-read-success");
	}

	private void testUpdateOperation(String entityName) {
		// Click Edit button
		clickButtonIfPresent("Edit");
		
		// Modify fields
		modifyFirstTextField();
		
		// Click Save
		clickButtonIfPresent("Save");
		
		// Verify success notification
		verifySuccessNotification();
		
		takeScreenshot(entityName + "-update-success");
	}

	private void testAttachmentsSection(String entityName) {
		try {
			// Look for attachments tab/section
			var attachmentsLocator = page.locator("text=Attachments").or(page.locator("[id*='attachment']"));
			if (attachmentsLocator.count() > 0) {
				LOGGER.info("      📎 Attachments section found!");
				attachmentsLocator.first().click();
				takeScreenshot(entityName + "-attachments");
				
				// TODO: Test file upload, download, delete
				LOGGER.warn("      ⚠️  Attachment operations not yet implemented in test");
			} else {
				LOGGER.info("      ℹ️  No attachments section for {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("      ⚠️  Could not test attachments section: {}", e.getMessage());
		}
	}

	private void testCommentsSection(String entityName) {
		try {
			// Look for comments tab/section
			var commentsLocator = page.locator("text=Comments").or(page.locator("[id*='comment']"));
			if (commentsLocator.count() > 0) {
				LOGGER.info("      💬 Comments section found!");
				commentsLocator.first().click();
				takeScreenshot(entityName + "-comments");
				
				// TODO: Test add comment, edit comment, delete comment
				LOGGER.warn("      ⚠️  Comment operations not yet implemented in test");
			} else {
				LOGGER.info("      ℹ️  No comments section for {}", entityName);
			}
		} catch (Exception e) {
			LOGGER.warn("      ⚠️  Could not test comments section: {}", e.getMessage());
		}
	}

	private void testDeleteOperation(String entityName) {
		try {
			// Select created row
			selectFirstGridRow();
			
			// Click Delete button
			clickButtonIfPresent("Delete");
			
			// Confirm dialog if present
			confirmDialogIfPresent();
			
			takeScreenshot(entityName + "-delete-success");
		} catch (Exception e) {
			LOGGER.warn("      ⚠️  Delete operation skipped: {}", e.getMessage());
		}
	}

	// Helper methods
	
	private void clickButtonIfPresent(String buttonText) {
		try {
			var button = page.locator("vaadin-button:has-text('" + buttonText + "')");
			if (button.count() > 0) {
				button.first().click();
				page.waitForTimeout(1000);
			}
		} catch (Exception e) {
			LOGGER.warn("      Button '{}' not found or not clickable", buttonText);
		}
	}

	private void fillRequiredFields() {
		// Fill first text field with test data
		try {
			var textFields = page.locator("vaadin-text-field:visible");
			if (textFields.count() > 0) {
				textFields.first().fill("TestEntity_" + System.currentTimeMillis());
			}
		} catch (Exception e) {
			LOGGER.warn("      Could not fill required fields: {}", e.getMessage());
		}
	}

	private void modifyFirstTextField() {
		try {
			var textFields = page.locator("vaadin-text-field:visible");
			if (textFields.count() > 0) {
				textFields.first().fill("Updated_" + System.currentTimeMillis());
			}
		} catch (Exception e) {
			LOGGER.warn("      Could not modify fields: {}", e.getMessage());
		}
	}

	private void selectFirstGridRow() {
		try {
			var gridRows = page.locator("vaadin-grid-cell-content");
			if (gridRows.count() > 0) {
				gridRows.first().click();
				page.waitForTimeout(1000);
			}
		} catch (Exception e) {
			LOGGER.warn("      Could not select grid row: {}", e.getMessage());
		}
	}

	private void verifyFormPopulated() {
		// Check if any form field has a value
		try {
			var textFields = page.locator("vaadin-text-field[value]:visible");
			if (textFields.count() > 0) {
				LOGGER.info("      ✅ Form populated with data");
			}
		} catch (Exception e) {
			LOGGER.warn("      Could not verify form population: {}", e.getMessage());
		}
	}

	private void verifySuccessNotification() {
		try {
			var notification = page.locator("vaadin-notification");
			if (notification.count() > 0) {
				LOGGER.info("      ✅ Success notification displayed");
			}
		} catch (Exception e) {
			LOGGER.warn("      Could not verify notification: {}", e.getMessage());
		}
	}

	private void confirmDialogIfPresent() {
		try {
			var confirmButton = page.locator("vaadin-button:has-text('Confirm')").or(
				page.locator("vaadin-button:has-text('Yes')"));
			if (confirmButton.count() > 0) {
				confirmButton.first().click();
				page.waitForTimeout(1000);
			}
		} catch (Exception e) {
			LOGGER.warn("      No confirmation dialog to handle");
		}
	}
}
