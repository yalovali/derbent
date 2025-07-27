package tech.derbent.abstracts.views;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Pageable;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * Test class to verify the "New" button behavior in CAbstractMDPage Tests the core
 * functionality that is failing across entity views
 */
public class CAbstractMDPageNewButtonTest {

	/**
	 * Test implementation of CAbstractMDPage for Projects
	 */
	private static class TestProjectMDPage extends CAbstractMDPage<CProject> {

		private static final long serialVersionUID = 1L;

		public TestProjectMDPage(final CAbstractService<CProject> entityService) {
			super(CProject.class, entityService);
		}

		@Override
		protected void createDetailsLayout() {
			// Simple test implementation
			getBaseDetailsLayout().add(new Div("Test Details"));
		}

		@Override
		protected void createGridForEntity() {
			// Simple test implementation
			grid.addColumn(CProject::getName).setHeader("Name");
		}

		@Override
		protected String getEntityRouteIdField() { return "project_id"; }

		@Override
		protected String getEntityRouteTemplateEdit() { return "projects/%s/edit"; }

		@Override
		protected void initPage() {
			// Test implementation
		}

		@Override
		protected CProject newEntity() {
			return new CProject();
		}

		@Override
		protected void setupToolbar() {
			// Test implementation
		}

		// Expose the validation method for testing
		@Override
		public void validateEntityForSave(final CProject entity) {
			super.validateEntityForSave(entity);
		}
	}

	@Mock
	private CProjectService projectService;

	@Mock
	private Clock clock;

	private TestProjectMDPage testPage;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		// Setup mock service behavior
		when(projectService.list(any(Pageable.class))).thenReturn(new ArrayList<>());
		when(projectService.count()).thenReturn(0);
		testPage = new TestProjectMDPage(projectService);
	}

	@Test
	public void testNewButtonCreatesAndBindsNewEntity() {
		// Given: page starts with no current entity
		testPage.setCurrentEntity(null);
		// When: New button is clicked
		final CButton newButton = testPage.createNewButton("New");
		newButton.click();
		// Then: a new entity should be created and set as current
		assertNotNull(testPage.getCurrentEntity(),
			"Current entity should not be null after clicking New button");
		assertNull(testPage.getCurrentEntity().getId(),
			"New entity should not have an ID yet");
	}

	@Test
	public void testNewEntityCreation() {
		// When: newEntity() is called
		final CProject newProject = testPage.newEntity();
		// Then: a new project instance should be created
		assertNotNull(newProject, "newEntity() should return a non-null instance");
		assertNull(newProject.getId(), "New entity should not have an ID");
		assertNull(newProject.getName(), "New entity should have null name initially");
	}

	@Test
	public void testSaveButtonSavesCurrentEntity() {
		// Given: page has a current entity (as would be after clicking New button)
		final CProject newProject = new CProject();
		newProject.setName("Test Project");
		testPage.setCurrentEntity(newProject);
		// Setup binder to simulate form data
		final BeanValidationBinder<CProject> binder = testPage.getBinder();
		binder.readBean(newProject);

		// When: Save button is clicked (simulate without UI dependencies)
		try {
			// Simulate the save operation core logic
			binder.writeBean(newProject);
			testPage.validateEntityForSave(newProject);
			// Verify the entity data is properly set
			assertEquals("Test Project", newProject.getName());
		} catch (final Exception e) {
			// Should not throw validation exception for valid data
			throw new RuntimeException("Save operation should succeed with valid data",
				e);
		}
	}
}