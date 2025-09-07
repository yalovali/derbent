package tech.derbent.abstracts.views.grids;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

public class CEntityProjectsGrid<T extends CEntityDB<T>> extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final CGrid<CProject> grid = new CGrid<>(CProject.class);
	private Supplier<Set<CProject>> getProjects;
	private Consumer<Set<CProject>> setProjects;
	private Runnable saveEntity;
	private final ComboBox<CProject> projectComboBox;

	public CEntityProjectsGrid(final CProjectService projectService) {
		LOGGER.debug("CEntityProjectsGrid constructor called for {}", getClass().getSimpleName());
		grid.addShortTextColumn(CProject::getName, "Project Name", null);
		add(grid);
		// ComboBox to select a project
		projectComboBox = new ComboBox<>("Select Project");
		// Following coding guidelines: All selective ComboBoxes must be selection only
		// (user must not be able to type arbitrary text)
		projectComboBox.setAllowCustomValue(false);
		projectComboBox.setItemLabelGenerator(CProject::getName);
		projectComboBox.setItems(projectService.findAll()); // Assumes findAll() returns
															// all projects
		add(projectComboBox);
		// Add/remove buttons
		final CButton addProject = new CButton("Add Project", null, e -> {
			final CProject selectedProject = projectComboBox.getValue();
			if ((selectedProject != null) && (getProjects != null) && (setProjects != null)) {
				final Set<CProject> projects = getProjects.get();
				if (!projects.contains(selectedProject)) {
					projects.add(selectedProject);
					setProjects.accept(projects);
					saveEntity.run();
					refresh();
				}
			}
		});
		final CButton removeProject = new CButton("Remove Selected", null, e -> {
			final CProject selected = grid.asSingleSelect().getValue();
			if ((selected != null) && (getProjects != null) && (setProjects != null)) {
				final Set<CProject> projects = getProjects.get();
				projects.remove(selected);
				setProjects.accept(projects);
				saveEntity.run();
				refresh();
			}
		});
		add(addProject, removeProject);
	}

	public void refresh() {
		LOGGER.info("Refreshing CEntityProjectsGrid for {}", getClass().getSimpleName());
		// Store the currently selected project to preserve selection after refresh
		final CProject selectedProject = grid.asSingleSelect().getValue();
		LOGGER.debug("Currently selected project before refresh: {}", selectedProject != null ? selectedProject.getName() : "null");
		if (getProjects != null) {
			grid.setItems(getProjects.get());
			// Restore selection if there was a previously selected project
			if (selectedProject != null) {
				restoreProjectSelection(selectedProject);
			}
		}
	}

	/** Restores grid selection to the specified project after refresh. This prevents losing the current selection when the grid is refreshed.
	 * @param project The project to select */
	private void restoreProjectSelection(final CProject project) {
		LOGGER.debug("Attempting to restore project selection to: {}", project.getName());
		try {
			// Find the project in the current grid data that matches by ID
			getProjects.get().stream().filter(p -> project.getId().equals(p.getId())).findFirst().ifPresentOrElse(matchedProject -> {
				grid.select(matchedProject);
				LOGGER.debug("Successfully restored selection to project: {}", matchedProject.getName());
			}, () -> LOGGER.debug("Project {} not found in grid after refresh", project.getName()));
		} catch (final Exception e) {
			LOGGER.warn("Error restoring project selection to {}: {}", project.getName(), e.getMessage());
		}
	}

	public void setProjectAccessors(final Supplier<Set<CProject>> getProjects, final Consumer<Set<CProject>> setProjects, final Runnable saveEntity) {
		LOGGER.info("Setting project accessors in CEntityProjectsGrid for {}", getClass().getSimpleName());
		this.getProjects = getProjects;
		this.setProjects = setProjects;
		this.saveEntity = saveEntity;
		refresh();
	}
}
