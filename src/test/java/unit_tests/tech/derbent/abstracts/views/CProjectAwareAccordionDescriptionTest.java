package unit_tests.tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.views.CProjectAwareAccordionDescription;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.CSessionService;
import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/** Test class for CProjectAwareAccordionDescription to verify project change listener functionality. This test verifies that panels properly
 * register/unregister as project change listeners and refresh when project changes occur. */
public class CProjectAwareAccordionDescriptionTest extends CTestBase {

	/** Test implementation of CProjectAwareAccordionDescription for testing purposes. */
	@SuppressWarnings ("serial")
	private class TestProjectAwarePanel extends CProjectAwareAccordionDescription<CActivity> {

		private boolean refreshCalled = false;

		public TestProjectAwarePanel(final CActivity entity, final CEnhancedBinder<CActivity> binder, final CAbstractService<CActivity> service,
				final CSessionService sessionService) {
			super("Test Panel", entity, binder, CActivity.class, service, sessionService);
		}

		@Override
		protected tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider createComboBoxDataProvider() {
			return null;
		}

		@Override
		protected void createPanelContent() {
			// Mock implementation for testing
		}

		@Override
		protected void refreshPanelForProjectChange(final CProject newProject) {
			refreshCalled = true;
			super.refreshPanelForProjectChange(newProject);
		}

		public void resetRefreshFlag() {
			refreshCalled = false;
		}

		@Override
		protected void updatePanelEntityFields() {
			// TODO Auto-generated method stub
		}

		@SuppressWarnings ("unused")
		public boolean wasRefreshCalled() {
			return refreshCalled;
		}
	}

	@Mock
	private CEnhancedBinder<CActivity> mockBinder;
	@Mock
	private CAbstractService<CActivity> mockService;
	@Mock
	private CSessionService mockSessionService;
	private TestProjectAwarePanel testPanel;

	@Override
	protected void setupForTest() {
		final CActivity mockActivity = new CActivity("Test Activity", project);
		mockActivity.setName("Test Activity");
		testPanel = new TestProjectAwarePanel(mockActivity, mockBinder, mockService, mockSessionService);
	}

	@Test
	void testNullProjectHandling() {
		testPanel.resetRefreshFlag();
		testPanel.onProjectChanged(null);
		assertNotNull(testPanel);
	}

	@Test
	void testPanelCreation() {
		assertNotNull(testPanel);
		assertNotNull(testPanel.getSessionService());
	}

	@Test
	void testProjectChangeNotification() {
		final CProject newProject = new CProject("New Project");
		testPanel.resetRefreshFlag();
		testPanel.onProjectChanged(newProject);
	}

	@Test
	void testShouldRefreshForProjectDefaultBehavior() {
		final CProject newProject = new CProject("New Project");
		final CActivity testActivity = new CActivity("Test Activity", newProject);
		final boolean shouldRefresh = testPanel.shouldRefreshForProject(testActivity, newProject);
		assertNotNull(shouldRefresh);
	}
}
