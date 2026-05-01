package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.service.IUserStoryRepository;

// KEYWORDS: Workflow, Status, ComboBox, Agile, UserStory, TypeEditing, Playwright
@SpringBootTest (webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestPropertySource (properties = {
		"spring.profiles.active=derbent",
		"server.port=0",
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
		"spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🧪 Workflow status editing")
public class CWorkflowStatusEditPersistenceTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusEditPersistenceTest.class);

	private long getMaxUserStoryId(final IUserStoryRepository repo) {
		return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
				.map(CUserStory::getId)
				.filter(id -> id != null)
				.findFirst()
				.orElse(0L);
	}

	private Long findNewestUserStoryIdAfter(final IUserStoryRepository repo, final long maxBefore) {
		return repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
				.map(CUserStory::getId)
				.filter(id -> id != null && id > maxBefore)
				.findFirst()
				.orElse(null);
	}

	@Test
	@DisplayName ("✅ Changing workflow status via toolbar ComboBox persists")
	void testWorkflowStatusChangePersists() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		final IUserStoryRepository repo = CSpringContext.getBean(IUserStoryRepository.class);
		final long maxBeforeCreate = getMaxUserStoryId(repo);
		final String name = "WorkflowStatus-Story";

		loginToApplication();
		assertTrue(navigateToDynamicPageByEntityType("User Stories"), "Could not navigate to User Stories page");
		page.waitForSelector("vaadin-grid");

		clickNew();
		wait_1000();
		fillFieldById(CUserStory.class, "name", name);
		clickSave();
		wait_2000();
		clickRefresh();
		wait_1000();

		final Long createdId = findNewestUserStoryIdAfter(repo, maxBeforeCreate);
		assertNotNull(createdId, "Created user story not found in DB");
		final CUserStory createdStory = repo.findById(createdId).orElse(null);
		assertNotNull(createdStory, "Created user story not found by id: " + createdId);
		final Long oldStatusId = createdStory.getStatus() != null ? createdStory.getStatus().getId() : null;
		assertNotNull(oldStatusId, "Created user story has no initial status");

		final tech.derbent.api.entityOfCompany.service.CProjectItemStatusService statusService =
				CSpringContext.getBean(tech.derbent.api.entityOfCompany.service.CProjectItemStatusService.class);
		final var nextStatuses = statusService.getValidNextStatuses(createdStory);
		Assumptions.assumeTrue(nextStatuses != null && !nextStatuses.isEmpty(), "No valid next statuses for created user story");

		// Select the row in grid by id to ensure toolbar is bound to the created entity
		final Locator idCell = page.locator("vaadin-grid-cell-content")
				.filter(new Locator.FilterOptions().setHasText(String.valueOf(createdId)))
				.first();
		assertTrue(idCell.count() > 0, "Could not locate created user story in grid by id: " + createdId);
		idCell.click();
		wait_1000();

		final Locator statusCombo = page.locator("#workflow-status-combobox");
		assertTrue(statusCombo.count() > 0, "Workflow status combobox not found");
		final Locator input = statusCombo.locator("input").first();
		final String currentUiStatus = input.inputValue();

		final var targetStatus = nextStatuses.stream()
				.filter(s -> s != null && s.getId() != null && s.getName() != null)
				.filter(s -> !s.getId().equals(oldStatusId))
				.filter(s -> currentUiStatus == null || !s.getName().trim().equalsIgnoreCase(currentUiStatus.trim()))
				.findFirst()
				.orElse(null);
		Assumptions.assumeTrue(targetStatus != null, "No alternative valid next status different from UI current value");
		final Long targetStatusId = targetStatus.getId();
		final String targetStatusName = targetStatus.getName().trim();
		Assumptions.assumeTrue(!targetStatusName.isBlank(), "Target status has no name");

		LOGGER.info("Workflow status transition: UI current='{}' (dbId={}), target='{}' (targetId={})", currentUiStatus, oldStatusId,
				targetStatusName, targetStatusId);

		input.click();
		wait_500();
		input.fill(targetStatusName);
		wait_500();
		final Locator options = page.locator("vaadin-combo-box-overlay[opened] vaadin-combo-box-item");
		assertTrue(options.count() > 0, "No status options rendered");
		options.filter(new Locator.FilterOptions().setHasText(targetStatusName)).first().click();

		// Ensure UI value changed (otherwise the listener may not have fired)
		boolean uiChanged = false;
		for (int i = 0; i < 10; i++) {
			final String uiValue = input.inputValue();
			if (uiValue != null && uiValue.trim().equalsIgnoreCase(targetStatusName)) {
				uiChanged = true;
				break;
			}
			wait_500();
		}
		assertTrue(uiChanged, "UI did not update combobox value to target status");

		// Poll DB briefly for the status change (server-side action triggers save)
		Long newStatusId = oldStatusId;
		for (int i = 0; i < 20; i++) {
			newStatusId = repo.findById(createdId).map(story -> story.getStatus() != null ? story.getStatus().getId() : null).orElse(null);
			if (newStatusId != null && newStatusId.equals(targetStatusId)) {
				break;
			}
			wait_500();
		}
		assertNotNull(newStatusId, "New status id not found after change");
		assertNotEquals(oldStatusId, newStatusId, "Workflow status did not change after selecting a new option");
		assertEquals(targetStatusId, newStatusId, "Workflow status did not persist to the selected next status");
	}
}
