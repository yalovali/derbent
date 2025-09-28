package tech.derbent.api.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.view.CUserProjectSettingsDialog;

/** Comprehensive test to demonstrate and validate the IContentOwner pattern implementation in CUserProjectSettingsDialog. This test verifies that the
 * dialog correctly integrates with content owners for context-aware data resolution. */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🎯 CUserProjectSettingsDialog Pattern Integration Test")
public class CUserProjectSettingsDialogPatternTest {

	@Autowired
	private CUserService userService;
	@Autowired
	private CProjectService projectService;
	@Autowired
	private CUserProjectSettingsService userProjectSettingsService;
	private MockContentOwnerForDialog mockContentOwner;
	private CUser testUser;
	private List<CProject> testProjects;

	@BeforeEach
	public void setUp() {
		// Create test user
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("testuser");
		testUser.setEmail("test@example.com");
		testUser = userService.save(testUser);
		// Create test projects
		CProject project1 = new CProject();
		project1.setName("Project Alpha");
		project1.setDescription("First test project");
		project1 = projectService.save(project1);
		CProject project2 = new CProject();
		project2.setName("Project Beta");
		project2.setDescription("Second test project");
		project2 = projectService.save(project2);
		testProjects = List.of(project1, project2);
		// Create mock content owner
		mockContentOwner = new MockContentOwnerForDialog(testUser, testProjects);
	}

	@Test
	@DisplayName ("Test dialog creation with content owner integration")
	public void testDialogCreationWithContentOwner() throws Exception {
		// Arrange
		AtomicReference<CUserProjectSettings> savedSettings = new AtomicReference<>();
		Consumer<CUserProjectSettings> onSave = savedSettings::set;
		// Act - Create dialog with content owner
		CUserProjectSettingsDialog dialog =
				new CUserProjectSettingsDialog(mockContentOwner, userService, projectService, userProjectSettingsService, null, testUser, onSave);
		// Assert
		assertNotNull(dialog, "Dialog should be created successfully");
		assertNotNull(dialog.getEntity(), "Dialog entity should not be null");
		assertEquals(testUser, dialog.getEntity().getUser(), "Dialog entity should have correct user set");
	}

	@Test
	@DisplayName ("Test dialog with existing settings")
	public void testDialogWithExistingSettings() throws Exception {
		// Arrange - Create existing settings
		CUserProjectSettings existingSettings = new CUserProjectSettings();
		existingSettings.setUser(testUser);
		existingSettings.setProject(testProjects.get(0));
		existingSettings.setPermission("READ_WRITE");
		existingSettings = userProjectSettingsService.save(existingSettings);
		AtomicReference<CUserProjectSettings> savedSettings = new AtomicReference<>();
		Consumer<CUserProjectSettings> onSave = savedSettings::set;
		// Act - Create dialog with existing settings
		CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(mockContentOwner, userService, projectService, userProjectSettingsService,
				existingSettings, testUser, onSave);
		// Assert
		assertNotNull(dialog, "Dialog should be created successfully");
		assertEquals(existingSettings, dialog.getEntity(), "Dialog should use existing settings");
		assertEquals(testUser, dialog.getEntity().getUser(), "Dialog entity should have correct user");
		assertEquals("READ_WRITE", dialog.getEntity().getPermission(), "Dialog should preserve existing permission");
	}

	@Test
	@DisplayName ("Test dialog title strings")
	public void testDialogTitleStrings() throws Exception {
		// Arrange
		Consumer<CUserProjectSettings> onSave = settings -> {};
		// Act - Test new dialog
		CUserProjectSettingsDialog newDialog =
				new CUserProjectSettingsDialog(mockContentOwner, userService, projectService, userProjectSettingsService, null, testUser, onSave);
		// Create existing settings for edit dialog
		CUserProjectSettings existingSettings = new CUserProjectSettings();
		existingSettings.setUser(testUser);
		existingSettings.setProject(testProjects.get(0));
		existingSettings = userProjectSettingsService.save(existingSettings);
		CUserProjectSettingsDialog editDialog = new CUserProjectSettingsDialog(mockContentOwner, userService, projectService,
				userProjectSettingsService, existingSettings, testUser, onSave);
		// Assert
		assertEquals("Add Project Assignment", newDialog.getDialogTitleString(), "New dialog should have 'Add' title");
		assertEquals("Edit Project Assignment", editDialog.getDialogTitleString(), "Edit dialog should have 'Edit' title");
	}

	@Test
	@DisplayName ("Test content owner method availability")
	public void testContentOwnerMethodAvailability() throws Exception {
		// Act - Verify content owner has required methods
		List<CProject> availableProjects = mockContentOwner.getAvailableProjects();
		// Assert
		assertNotNull(availableProjects, "Content owner should provide available projects");
		assertEquals(2, availableProjects.size(), "Should return correct number of projects");
		assertEquals("Project Alpha", availableProjects.get(0).getName(), "Should return correct project names");
		assertEquals("Project Beta", availableProjects.get(1).getName(), "Should return correct project names");
	}

	@Test
	@DisplayName ("Test content owner entity management")
	public void testContentOwnerEntityManagement() throws Exception {
		// Arrange
		CUser anotherUser = new CUser();
		anotherUser.setName("Another User");
		anotherUser.setLogin("anotheruser");
		// Act
		Object originalEntity = mockContentOwner.getCurrentEntity();
		mockContentOwner.setCurrentEntity(anotherUser);
		Object newEntity = mockContentOwner.getCurrentEntity();
		// Assert
		assertEquals(testUser, originalEntity, "Should return original test user");
		assertEquals(anotherUser, newEntity, "Should return newly set user");
	}

	/** Mock content owner implementation that demonstrates the IContentOwner pattern for dialog integration. This mock simulates how a real view
	 * (like CUsersView) would provide context-aware data for the dialog's form fields. */
	private static class MockContentOwnerForDialog implements IContentOwner {

		private CUser currentUser;
		private final List<CProject> availableProjects;

		public MockContentOwnerForDialog(CUser user, List<CProject> projects) {
			this.currentUser = user;
			this.availableProjects = projects;
		}

		@Override
		public Object getCurrentEntity() { return currentUser; }

		@Override
		public void setCurrentEntity(Object entity) { this.currentUser = (CUser) entity; }

		@Override
		public void populateForm() {
			// Mock implementation - do nothing
		}

		/** Content owner method that provides available projects for the user project settings dialog. This method demonstrates how the IContentOwner
		 * pattern enables context-aware data resolution.
		 * @return list of projects available for assignment to the current user */
		public List<CProject> getAvailableProjects() {
			// In real implementation, this could filter projects based on:
			// - User's company
			// - User's role/permissions
			// - Currently active projects
			// - User's existing assignments
			return availableProjects;
		}
	}
}
