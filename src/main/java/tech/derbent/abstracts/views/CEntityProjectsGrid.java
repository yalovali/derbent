package tech.derbent.abstracts.views;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

public class CEntityProjectsGrid<T extends CEntityDB> extends VerticalLayout {

	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	private final Grid<CProject> grid = new Grid<>(CProject.class, false);
	private Supplier<Set<CProject>> getProjects;
	private Consumer<Set<CProject>> setProjects;
	private Runnable saveEntity;
	private final ComboBox<CProject> projectComboBox;

	public CEntityProjectsGrid(final CProjectService projectService) {
		LOGGER.info("CEntityProjectsGrid constructor called for {}", getClass().getSimpleName());
		grid.addColumn(CProject::getName).setHeader("Project Name").setAutoWidth(true);
		add(grid);
		// ComboBox to select a project
		projectComboBox = new ComboBox<>("Select Project");
		projectComboBox.setItemLabelGenerator(CProject::getName);
		projectComboBox.setItems(projectService.findAll()); // Assumes findAll() returns all projects
		add(projectComboBox);
		// Add/remove buttons
		final Button addProject = new Button("Add Project", e -> {
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
		final Button removeProject = new Button("Remove Selected", e -> {
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
		if (getProjects != null) {
			grid.setItems(getProjects.get());
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
