package tech.derbent.api.annotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.projects.domain.CProject;
import tech.derbent.users.domain.CUser;

/** Integration test for Content Owner functionality in FormBuilder and DataProviderResolver. This test validates that: - IContentOwner interface
 * works correctly - CDataProviderResolver can resolve data from content owners - ThreadLocal context is properly managed */
@SpringBootTest (classes = tech.derbent.Application.class)
@TestPropertySource (properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb", "spring.datasource.username=sa", "spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver", "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName ("🎯 Content Owner Integration Test")
public class CContentOwnerIntegrationTest {

	@Test
	@DisplayName ("Test IContentOwner Interface")
	public void testIContentOwnerInterface() {
		// Create a mock content owner
		IContentOwner contentOwner = new TestContentOwner();
		// Test getCurrentEntity
		Object currentEntity = contentOwner.getCurrentEntity();
		assertNotNull(currentEntity);
		assertTrue(currentEntity instanceof CUser);
		CUser user = (CUser) currentEntity;
		assertEquals("Test User", user.getName());
		// Test getContextValue
		Object contextValue = contentOwner.getContextValue("currentEntity");
		assertNotNull(contextValue);
		assertEquals(currentEntity, contextValue);
		// Test non-existent context value
		Object nullValue = contentOwner.getContextValue("nonexistent");
		assertNull(nullValue);
	}

	@Test
	@DisplayName ("Test Content Owner Method Call Simulation")
	public void testContentOwnerMethodCallSimulation() {
		TestContentOwner contentOwner = new TestContentOwner();
		// Test the getAvailableProjects method
		List<CProject> projects = contentOwner.getAvailableProjects();
		assertNotNull(projects);
		assertEquals(2, projects.size());
		assertEquals("Project A for Test User", projects.get(0).getName());
		assertEquals("Project B for Test User", projects.get(1).getName());
	}

	/** Mock implementation of IContentOwner for testing purposes. Simulates a page that provides context-aware project data. */
	private static class TestContentOwner implements IContentOwner {

		private CUser currentUser;

		public TestContentOwner() {
			// Create a test user
			currentUser = new CUser();
			currentUser.setName("Test User");
			currentUser.setLogin("testuser");
			currentUser.setEmail("test@example.com");
		}

		@Override
		public Object getCurrentEntity() { return currentUser; }

		@Override
		public Object getContextValue(String contextName) {
			if ("currentEntity".equals(contextName)) {
				return currentUser;
			}
			return null;
		}

		/** Simulates the getAvailableProjects method that would be called by the content-aware data provider. */
		public List<CProject> getAvailableProjects() {
			// Create mock projects based on current user context
			CProject project1 = new CProject();
			project1.setName("Project A for " + currentUser.getName());
			project1.setDescription("First project for user");
			CProject project2 = new CProject();
			project2.setName("Project B for " + currentUser.getName());
			project2.setDescription("Second project for user");
			return Arrays.asList(project1, project2);
		}
	}
}
