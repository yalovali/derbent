package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.users.domain.CUser;

/** Test to verify that the CUserProjectSettingsDialog project field combobox properly retrieves data from the content owner's getAvailableProjects
 * method. */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🎯 Content Owner Project Field Test")
public class CContentOwnerProjectFieldTest {

	@Autowired
	private CDataProviderResolver dataProviderResolver;

	@BeforeEach
	public void setUp() {
		// Clear caches to ensure fresh test
		dataProviderResolver.clearCaches();
	}

	@Test
	@DisplayName ("Test project field data resolution with content owner")
	public void testProjectFieldDataResolution() throws Exception {
		// Create mock content owner that simulates CUsersView
		IContentOwner mockContentOwner = new MockContentOwnerWithProjects();
		// Create EntityFieldInfo that simulates the project field from CUserProjectSettings
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("project");
		fieldInfo.setFieldTypeClass(CProject.class);
		fieldInfo.setDataProviderOwner("content");
		fieldInfo.setDataProviderMethod("getAvailableProjects");
		// Test the data provider resolution
		List<CProject> projects = dataProviderResolver.resolveData(mockContentOwner, fieldInfo);
		// Verify the results
		assertNotNull(projects, "Projects list should not be null");
		assertEquals(2, projects.size(), "Should have 2 projects");
		assertEquals("Test Project 1", projects.get(0).getName(), "First project name should match");
		assertEquals("Test Project 2", projects.get(1).getName(), "Second project name should match");
	}

	@Test
	@DisplayName ("Test project field data resolution with null content owner")
	public void testProjectFieldDataResolutionWithNullContentOwner() throws Exception {
		// Create EntityFieldInfo that simulates the project field from CUserProjectSettings
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("project");
		fieldInfo.setFieldTypeClass(CProject.class);
		fieldInfo.setDataProviderOwner("content");
		fieldInfo.setDataProviderMethod("getAvailableProjects");
		// Test with null content owner
		List<CProject> projects = dataProviderResolver.resolveData(null, fieldInfo);
		// Should return empty list, not fail
		assertNotNull(projects, "Projects list should not be null");
		assertEquals(0, projects.size(), "Should have 0 projects when content owner is null");
	}

	/** Mock implementation of IContentOwner that simulates CUsersView behavior. This includes the getAvailableProjects method that should be called
	 * by the data provider. */
	private static class MockContentOwnerWithProjects implements IContentOwner {

		private CUser currentUser;

		public MockContentOwnerWithProjects() {
			// Create a test user
			currentUser = new CUser();
			currentUser.setName("Test User");
			currentUser.setLogin("testuser");
			currentUser.setEmail("test@example.com");
		}

		@Override
		public Object getCurrentEntity() { return currentUser; }

		@Override
		public void setCurrentEntity(Object entity) { this.currentUser = (CUser) entity; }

		@Override
		public void populateForm(Object entity) {
			setCurrentEntity(entity);
			populateForm();
		}

		@Override
		public void populateForm() {
			// Mock implementation - do nothing
		}

		/** Implementation of getAvailableProjects method that should be called by the data provider resolver. This method simulates the behavior
		 * expected from CUsersView.getAvailableProjects(). */
		public List<CProject> getAvailableProjects() {
			// Create test projects
			CProject project1 = new CProject();
			project1.setName("Test Project 1");
			project1.setDescription("Description for Test Project 1");
			CProject project2 = new CProject();
			project2.setName("Test Project 2");
			project2.setDescription("Description for Test Project 2");
			return List.of(project1, project2);
		}
	}
}
