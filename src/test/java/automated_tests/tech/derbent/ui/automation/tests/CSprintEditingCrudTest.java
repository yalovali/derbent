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
import org.springframework.data.domain.Sort;
import org.springframework.test.context.TestPropertySource;

import com.microsoft.playwright.Locator;

import automated_tests.tech.derbent.ui.automation.CBaseUITest;
import tech.derbent.Application;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.service.ISprintRepository;

// KEYWORDS: Sprint, Editing, Agile, Backlog, Playwright
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
@DisplayName ("🧪 Sprint editing")
public class CSprintEditingCrudTest extends CBaseUITest {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintEditingCrudTest.class);


	@Test
	@DisplayName ("✅ Edit sprint color persists")
	void testCreateAndEditSprintName() {
		if (!isBrowserAvailable()) {
			LOGGER.warn("⚠️ Browser not available - skipping UI test");
			Assumptions.assumeTrue(false, "Browser not available");
			return;
		}

		loginToApplication();

		final ISprintRepository repo = CSpringContext.getBean(ISprintRepository.class);
		final CSprint sprintToEdit = repo.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().findFirst().orElse(null);
		assertNotNull(sprintToEdit, "No sprint sample data available");
		assertNotNull(sprintToEdit.getId(), "Sample sprint has no id");
		final Long sprintId = sprintToEdit.getId();
		final String originalColor = sprintToEdit.getColor();
		final String updatedColor = "#123ABC";
		Assumptions.assumeTrue(originalColor == null || !updatedColor.equalsIgnoreCase(originalColor), "Updated color must differ");

		assertTrue(navigateToDynamicPageByEntityType("Sprints"), "Could not navigate to Sprints page");
		page.waitForSelector("vaadin-grid");

		// Sprint Management uses Grid.Editor inline editing.
		final Locator idCell = page.locator("vaadin-grid-cell-content:not([aria-hidden='true'])")
				.filter(new Locator.FilterOptions().setHasText(String.valueOf(sprintId)))
				.first();
		assertTrue(idCell.count() > 0, "Could not locate sprint in grid by id: " + sprintId);
		idCell.click();
		wait_500();

		final Locator textInputs = page.locator("vaadin-grid vaadin-text-field input");
		Assumptions.assumeTrue(textInputs.count() > 0, "No text field editor visible for sprint inline edit");

		Locator colorInput = textInputs.first();
		if (originalColor != null) {
			for (int i = 0; i < textInputs.count(); i++) {
				final Locator candidate = textInputs.nth(i);
				final String current = candidate.inputValue();
				if (current != null && current.trim().equalsIgnoreCase(originalColor.trim())) {
					colorInput = candidate;
					break;
				}
			}
		}
		colorInput.fill(updatedColor);
		wait_500();

		// Close editor by switching rows (triggers editor close listener → DB save)
		final Locator otherRowCell = page.locator("vaadin-grid-cell-content:not([aria-hidden='true'])")
				.filter(new Locator.FilterOptions().setHasText("Sprint 1"))
				.first();
		if (otherRowCell.count() > 0) {
			otherRowCell.click(new Locator.ClickOptions().setForce(true));
		}
		wait_2000();

		String persistedColor = null;
		for (int i = 0; i < 10; i++) {
			persistedColor = repo.findById(sprintId).map(CSprint::getColor).orElse(null);
			if (updatedColor.equalsIgnoreCase(persistedColor)) {
				break;
			}
			wait_500();
		}
		assertNotNull(persistedColor, "Sprint not found in DB after edit: " + sprintId);
		assertEquals(updatedColor.toUpperCase(), persistedColor.toUpperCase(), "Sprint color not persisted after inline edit");
	}
}
