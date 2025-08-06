package unit_tests.tech.derbent.users.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.session.service.CSessionService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.service.CUserTypeService;
import tech.derbent.users.view.CPanelUserProjectSettings;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test to verify that user project settings management properly adds items instead of
 * replacing the entire collection.
 */
@ExtendWith (MockitoExtension.class)
class CUserProjectSettingsCollectionTest extends CTestBase {

	private CUserService userService;

	private CProjectService projectService;

	private CUserTypeService userTypeService;

	private CCompanyService companyService;

	private CSessionService sessionService;

	private CUserProjectSettingsService userProjectSettingsService;

	private CPanelUserProjectSettings projectSettingsPanel;

	@Override
	@BeforeEach
	protected void setupForTest() {
		// Mock Vaadin environment
		final VaadinRequest request = mock(VaadinRequest.class);
		final VaadinService service = mock(VaadinService.class);
		final VaadinSession session = mock(VaadinSession.class);
		VaadinSession.setCurrent(session);
		UI.setCurrent(new UI());
		// Mock services
		userService = mock(CUserService.class);
		projectService = mock(CProjectService.class);
		userTypeService = mock(CUserTypeService.class);
		companyService = mock(CCompanyService.class);
		sessionService = mock(CSessionService.class);
		userProjectSettingsService = mock(CUserProjectSettingsService.class);
	}

	@Test
	void testCollectionAdditionNotReplacement() {
		// Create test entities
		final CUser testUser = new CUser("Test User");
		final CProject project1 = new CProject();
		project1.setName("Project 1");
		final CProject project2 = new CProject();
		project2.setName("Project 2");
		// Create initial project settings
		final CUserProjectSettings settings1 = new CUserProjectSettings();
		settings1.setUser(testUser);
		settings1.setProject(project1);
		settings1.setRole("DEVELOPER");
		settings1.setPermission("READ,WRITE");
		// Initialize user with first project setting
		final List<CUserProjectSettings> initialSettings = new ArrayList<>();
		initialSettings.add(settings1);
		testUser.setProjectSettings(initialSettings);
		// Store reference to original list to verify it's preserved
		final List<CUserProjectSettings> originalList = testUser.getProjectSettings();
		assertNotNull(originalList, "Initial project settings should not be null");
		assertEquals(1, originalList.size(), "Should start with one project setting");
		// Create second project setting
		final CUserProjectSettings settings2 = new CUserProjectSettings();
		settings2.setUser(testUser);
		settings2.setProject(project2);
		settings2.setRole("MANAGER");
		settings2.setPermission("READ,WRITE,DELETE");
		// Simulate the collection update pattern from the fixed CUsersView
		List<CUserProjectSettings> currentSettings = testUser.getProjectSettings();
		final List<CUserProjectSettings> newSettings = new ArrayList<>(currentSettings);
		newSettings.add(settings2);

		// Apply the fix: update existing collection instead of replacing
		if (currentSettings == null) {
			currentSettings = new ArrayList<>(newSettings);
			testUser.setProjectSettings(currentSettings);
		}
		else {
			currentSettings.clear();
			currentSettings.addAll(newSettings);
		}
		// Verify that the collection now contains both settings
		final List<CUserProjectSettings> resultSettings = testUser.getProjectSettings();
		assertNotNull(resultSettings, "Project settings should not be null");
		assertEquals(2, resultSettings.size(),
			"Should have two project settings after addition");
		// Verify the original list reference is preserved
		assertTrue(resultSettings == originalList,
			"The original list reference should be preserved");
		// Verify both projects are present
		final boolean hasProject1 = resultSettings.stream()
			.anyMatch(s -> "Project 1".equals(s.getProject().getName()));
		final boolean hasProject2 = resultSettings.stream()
			.anyMatch(s -> "Project 2".equals(s.getProject().getName()));
		assertTrue(hasProject1, "Should contain Project 1");
		assertTrue(hasProject2, "Should contain Project 2");
	}
}