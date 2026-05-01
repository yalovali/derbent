package automated_tests.tech.derbent.ui.automation.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.plm.sprints.domain.CSprintItem;
import tech.derbent.plm.sprints.service.ISprintItemRepository;

// KEYWORDS: Kanban, DragDrop, SprintItem, Backlog, Agile, Playwright
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
@DisplayName ("🧪 Kanban drag-drop")
public class CKanbanDragDropTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanDragDropTest.class);

	@Test
	@DisplayName ("✅ Drag post-it between columns persists sprintItem.kanbanColumnId")
	void testDragBetweenColumnsPersists() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();
		assertTrue(navigateToDynamicPageByEntityType("Kanban Lines"), "Could not navigate to Kanban Lines page");
		page.waitForSelector("vaadin-grid");
		clickFirstGridRow();
		wait_2000();

		final Locator postits = page.locator(".kanban-postit[data-sprint-item-id]");
		if (postits.count() == 0) {
			Assumptions.assumeTrue(false, "No kanban post-its rendered - sample data missing");
			return;
		}
		final Locator postit = postits.first();
		final String sprintItemIdRaw = postit.getAttribute("data-sprint-item-id");
		assertNotNull(sprintItemIdRaw, "data-sprint-item-id missing on post-it");
		final Long sprintItemId = Long.valueOf(sprintItemIdRaw);

		final Locator sourceColumn = postit.locator("xpath=ancestor::*[contains(@class,'kanban-column')][1]");
		final String sourceColumnIdRaw = sourceColumn.getAttribute("data-kanban-column-id");
		if (sourceColumnIdRaw == null || sourceColumnIdRaw.isBlank()) {
			Assumptions.assumeTrue(false, "Selected post-it is in backlog column (no column id) - cannot test column-to-column drag");
			return;
		}
		final Long sourceColumnId = Long.valueOf(sourceColumnIdRaw);

		final Locator columns = page.locator(".kanban-column[data-kanban-column-id]");
		assertTrue(columns.count() >= 2, "Need at least 2 kanban columns to test drag-drop");

		Locator targetColumn = null;
		Long targetColumnId = null;
		for (int i = 0; i < columns.count(); i++) {
			final Locator candidate = columns.nth(i);
			final String candidateIdRaw = candidate.getAttribute("data-kanban-column-id");
			if (candidateIdRaw == null || candidateIdRaw.isBlank()) {
				continue;
			}
			final Long candidateId = Long.valueOf(candidateIdRaw);
			if (!candidateId.equals(sourceColumnId)) {
				targetColumn = candidate;
				targetColumnId = candidateId;
				break;
			}
		}
		assertNotNull(targetColumn, "Could not resolve a target column different from source");
		assertNotNull(targetColumnId, "Target column id missing");

		LOGGER.info("Dragging sprintItemId={} from column {} to column {}", sprintItemId, sourceColumnId, targetColumnId);
		postit.dragTo(targetColumn.locator(".kanban-column-items").first());
		wait_1000();

		final Locator statusDialog = page.locator("vaadin-dialog-overlay[opened]")
				.filter(new Locator.FilterOptions().setHasText("Select Status"));
		if (statusDialog.count() > 0) {
			final Locator firstStatusButton = statusDialog.first().locator("vaadin-button").first();
			firstStatusButton.click();
			wait_1000();
		}

		wait_2000();

		final ISprintItemRepository repo = CSpringContext.getBean(ISprintItemRepository.class);
		final CSprintItem persisted = repo.findById(sprintItemId).orElse(null);
		assertNotNull(persisted, "Sprint item not found in DB: " + sprintItemId);
		assertEquals(targetColumnId, persisted.getKanbanColumnId(),
				"Drag-drop did not persist kanbanColumnId for sprint item " + sprintItemId);
	}
}
