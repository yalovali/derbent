package tech.derbent.api.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;

/** Test to verify that panels and dialogs work correctly with data sources and the improved IContentOwner pattern. */
@DisplayName ("🔧 Panel and Dialog Integration Test")
public class CPanelDialogIntegrationTest {

	@Mock
	private CUserService userService;
	@Mock
	private CProjectService projectService;
	@Mock
	private CUserProjectSettingsService userProjectSettingsService;
	@Mock
	private CEnhancedBinder<CUser> userBinder;
	@Mock
	private IContentOwner parentContent;
	private CUser testUser;
	private CProject testProject;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create test entities
		testUser = new CUser();
		testUser.setName("Test User");
		testUser.setLogin("testuser");
		testProject = new CProject();
		testProject.setName("Test Project");
		// Mock parent content owner to return current user
		when(parentContent.getCurrentEntity()).thenReturn(testUser);
		when(parentContent.getContextValue("currentEntity")).thenReturn(testUser);
		when(parentContent.getContextValue("currentUser")).thenReturn(testUser);
	}

	@Test
	@DisplayName ("Test IContentOwner context access patterns")
	void testContentOwnerContextAccess() {
		// Test the basic context access patterns
		assertEquals(testUser, parentContent.getCurrentEntity());
		assertEquals(testUser, parentContent.getContextValue("currentEntity"));
		assertEquals(testUser, parentContent.getContextValue("currentUser"));
		// Verify context owner functionality
		assertNotNull(parentContent);
	}

	@Test
	@DisplayName ("Test improved IContentOwner interface methods")
	void testImprovedContentOwnerInterface() {
		// Create a test implementation to verify new methods
		IContentOwner testOwner = new IContentOwner() {

			@Override
			public Object getCurrentEntity() { return testUser; }

			@Override
			public Object getContextValue(String contextName) {
				if ("testValue".equals(contextName)) {
					return "testResult";
				}
				return IContentOwner.super.getContextValue(contextName);
			}
		};
		// Test the enhanced interface
		assertEquals(testUser, testOwner.getCurrentEntity());
		assertEquals(testUser, testOwner.getContextValue("currentEntity")); // default implementation
		assertEquals("testResult", testOwner.getContextValue("testValue"));
		assertNull(testOwner.getContextValue("nonexistent"));
		assertNull(testOwner.getParentContentOwner()); // default implementation
	}

	@Test
	@DisplayName ("Test hierarchical context resolution")
	void testHierarchicalContextResolution() {
		// Create a mock parent content owner
		IContentOwner grandParent = mock(IContentOwner.class);
		when(grandParent.getContextValue("grandParentData")).thenReturn("grandParentValue");
		when(grandParent.resolveContextValue("grandParentData")).thenReturn("grandParentValue");
		// Create a child content owner that delegates to grandParent
		IContentOwner childContent = new IContentOwner() {

			@Override
			public Object getCurrentEntity() { return testUser; }

			@Override
			public IContentOwner getParentContentOwner() { return grandParent; }

			@Override
			public Object getContextValue(String contextName) {
				if ("childData".equals(contextName)) {
					return "childValue";
				}
				return IContentOwner.super.getContextValue(contextName);
			}
		};
		// Test resolving context values up the hierarchy
		assertEquals("childValue", childContent.resolveContextValue("childData"));
		assertEquals("grandParentValue", childContent.resolveContextValue("grandParentData"));
		assertEquals(testUser, childContent.resolveContextValue("currentEntity"));
		assertNull(childContent.resolveContextValue("nonexistent"));
	}
}
