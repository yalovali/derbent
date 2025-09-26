package tech.derbent.api.components;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.dialogs.CConfirmationDialog;
import tech.derbent.companies.domain.CCompany;
import tech.derbent.companies.service.CCompanyService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.domain.CUserCompanySettings;
import tech.derbent.users.domain.CUserProjectSettings;
import tech.derbent.users.service.CUserProjectSettingsService;
import tech.derbent.users.service.CUserService;
import tech.derbent.users.view.CUserProjectSettingsDialog;

/** Creative and comprehensive relationship manager that provides a unified interface for managing all entity relationships using reflection and
 * metadata annotations. This component automatically discovers relationship types and provides appropriate UI components with enhanced visualization
 * and management capabilities. Features: - Accordion-based interface for different relationship types - Enhanced grids with rich visualization -
 * Reflection-based configuration for maximum reusability - Integration with enhanced dialog components - Creative styling and user experience */
public class CCreativeRelationshipManager extends VerticalLayout implements IContentOwner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CCreativeRelationshipManager.class);
	private static final long serialVersionUID = 1L;
	// Core components
	private final CEntityDB<?> mainEntity;
	// UI Components
	private Accordion relationshipAccordion;
	private Map<String, Grid<?>> relationshipGrids;
	private Map<String, Component> relationshipPanels;
	private final Map<String, CAbstractService<?>> serviceRegistry;

	/** Constructor for creative relationship manager */
	public CCreativeRelationshipManager(CEntityDB<?> mainEntity) {
		this.mainEntity = mainEntity;
		serviceRegistry = new HashMap<>();
		relationshipPanels = new HashMap<>();
		relationshipGrids = new HashMap<>();
		setupComponent();
		createUI();
	}

	/** Create action buttons for relationship management */
	private HorizontalLayout createActionButtons(String relationshipType, Runnable addAction, Runnable editAction, Runnable deleteAction) {
		HorizontalLayout actions = new HorizontalLayout();
		actions.setSpacing(true);
		Button addButton = new Button("Add", VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> addAction.run());
		Button editButton = new Button("Edit", VaadinIcon.EDIT.create());
		editButton.setEnabled(false);
		editButton.addClickListener(e -> editAction.run());
		Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		deleteButton.setEnabled(false);
		deleteButton.addClickListener(e -> deleteAction.run());
		// Enable/disable buttons based on selection
		Grid<?> grid = relationshipGrids.get(relationshipType);
		if (grid != null) {
			grid.addSelectionListener(selection -> {
				boolean hasSelection = !selection.getAllSelectedItems().isEmpty();
				editButton.setEnabled(hasSelection);
				deleteButton.setEnabled(hasSelection);
			});
		}
		actions.add(addButton, editButton, deleteButton);
		return actions;
	}

	/** Create enhanced company grid with rich visualization */
	private Grid<CUserCompanySettings> createCompanyGrid() {
		Grid<CUserCompanySettings> grid = new Grid<>(CUserCompanySettings.class, false);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		grid.setHeight("300px");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		// Company column with enhanced rendering
		grid.addColumn(new ComponentRenderer<>(this::createCompanyRenderer)).setHeader("Company").setWidth("300px").setFlexGrow(0);
		// Role column
		grid.addColumn(CUserCompanySettings::getRole).setHeader("Role").setWidth("150px").setFlexGrow(0);
		// Ownership column with enhanced rendering
		grid.addColumn(new ComponentRenderer<>(this::createOwnershipRenderer)).setHeader("Ownership").setWidth("120px").setFlexGrow(0);
		// Status column
		grid.addColumn(new ComponentRenderer<>(this::createStatusRenderer)).setHeader("Status").setWidth("100px").setFlexGrow(1);
		return grid;
	}

	/** Create company relationship panel for user */
	private AccordionPanel createCompanyRelationshipPanel(CUser user) {
		// Panel header with summary
		HorizontalLayout panelHeader = new HorizontalLayout();
		panelHeader.setAlignItems(Alignment.CENTER);
		panelHeader.add(VaadinIcon.BUILDING.create());
		panelHeader.add(new Span("Company Memberships"));
		// Panel summary
		int companyCount = user.getCompanySettings() != null ? user.getCompanySettings().size() : 0;
		Span summary = new Span("(" + companyCount + " companies)");
		summary.getStyle().set("color", "var(--lumo-secondary-text-color)");
		panelHeader.add(summary);
		// Panel content
		VerticalLayout content = new VerticalLayout();
		content.setPadding(false);
		content.setSpacing(true);
		// Action buttons
		HorizontalLayout actions = createActionButtons("company", () -> openAddCompanyDialog(user), () -> openEditCompanyDialog(user),
				() -> deleteSelectedCompanyRelationship(user));
		content.add(actions);
		// Company grid
		Grid<CUserCompanySettings> companyGrid = createCompanyGrid();
		relationshipGrids.put("company", companyGrid);
		content.add(companyGrid);
		// Load data
		loadCompanyRelationships(user, companyGrid);
		AccordionPanel panel = relationshipAccordion.add("Company Memberships (" + companyCount + " companies)", content);
		relationshipPanels.put("company", panel);
		return panel;
	}

	private void createCompanyRelationshipPanels() {
		// Implementation for company-centric relationship management
		H3 placeholder = new H3("Company relationship management coming soon");
		add(placeholder);
	}

	/** Create company renderer with enhanced visualization */
	private Component createCompanyRenderer(CUserCompanySettings settings) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(Alignment.CENTER);
		// Company icon
		VaadinIcon.BUILDING.create().setSize("20px");
		layout.add(VaadinIcon.BUILDING.create());
		// Company info
		VerticalLayout info = new VerticalLayout();
		info.setPadding(false);
		info.setSpacing(false);
		CCompany company = settings.getCompany();
		if (company != null) {
			Span companyName = new Span(company.getName());
			companyName.getStyle().set("font-weight", "bold");
			info.add(companyName);
			// Try to get department using reflection
			String department = getFieldValue(settings, "department");
			if (department != null && !department.isEmpty()) {
				Span deptSpan = new Span(department);
				deptSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");
				info.add(deptSpan);
			}
		}
		layout.add(info);
		return layout;
	}

	/** Create header section with entity information */
	private void createHeaderSection() {
		HorizontalLayout header = new HorizontalLayout();
		header.setWidthFull();
		header.setJustifyContentMode(JustifyContentMode.BETWEEN);
		header.setAlignItems(Alignment.CENTER);
		header.setPadding(true);
		header.getStyle().set("background-color", "var(--lumo-primary-color-10pct)").set("border-radius", "var(--lumo-border-radius-m)")
				.set("margin-bottom", "var(--lumo-space-m)");
		// Entity info
		VerticalLayout entityInfo = new VerticalLayout();
		entityInfo.setPadding(false);
		entityInfo.setSpacing(false);
		H2 title = new H2("Relationship Management");
		title.getStyle().set("margin", "0").set("color", "var(--lumo-primary-color)");
		String entityName = getEntityDisplayName(mainEntity);
		Span subtitle = new Span("Managing relationships for: " + entityName);
		subtitle.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");
		entityInfo.add(title, subtitle);
		// Action buttons
		HorizontalLayout actions = new HorizontalLayout();
		Button refreshButton = new Button("Refresh All", VaadinIcon.REFRESH.create());
		refreshButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
		refreshButton.addClickListener(e -> refreshAll());
		actions.add(refreshButton);
		header.add(entityInfo, actions);
		add(header);
	}

	/** Create ownership renderer with visual indicators */
	private Component createOwnershipRenderer(CUserCompanySettings settings) {
		String ownership = getFieldValue(settings, "ownership", "ownershipLevel");
		if (ownership == null) {
			ownership = "MEMBER";
		}
		Span ownershipSpan = new Span(ownership);
		// Style based on ownership level
		switch (ownership.toUpperCase()) {
		case "OWNER":
			ownershipSpan.getStyle().set("background-color", "var(--lumo-error-color)").set("color", "white");
			break;
		case "ADMIN":
			ownershipSpan.getStyle().set("background-color", "var(--lumo-warning-color)").set("color", "white");
			break;
		case "MEMBER":
			ownershipSpan.getStyle().set("background-color", "var(--lumo-primary-color)").set("color", "white");
			break;
		default:
			ownershipSpan.getStyle().set("background-color", "var(--lumo-contrast-20pct)").set("color", "var(--lumo-body-text-color)");
		}
		ownershipSpan.getStyle().set("padding", "2px 8px").set("border-radius", "12px").set("font-size", "var(--lumo-font-size-xs)")
				.set("font-weight", "bold");
		return ownershipSpan;
	}

	/** Create permission renderer with badges */
	private Component createPermissionRenderer(CUserProjectSettings settings) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		String permissions = settings.getPermission();
		if (permissions != null && !permissions.isEmpty()) {
			String[] permArray = permissions.split(",");
			for (int i = 0; i < Math.min(permArray.length, 3); i++) {
				String perm = permArray[i].trim();
				Span permSpan = new Span(perm);
				permSpan.getStyle().set("background-color", getPermissionColor(perm)).set("color", "white").set("padding", "2px 6px")
						.set("border-radius", "10px").set("font-size", "var(--lumo-font-size-xs)").set("font-weight", "bold");
				layout.add(permSpan);
			}
			if (permArray.length > 3) {
				Span moreSpan = new Span("+" + (permArray.length - 3));
				moreSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-xs)");
				layout.add(moreSpan);
			}
		}
		return layout;
	}

	/** Create enhanced project grid with rich visualization */
	private Grid<CUserProjectSettings> createProjectGrid() {
		Grid<CUserProjectSettings> grid = new Grid<>(CUserProjectSettings.class, false);
		grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		grid.addThemeVariants(GridVariant.LUMO_COMPACT);
		grid.setHeight("300px");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		// Project column with enhanced rendering
		grid.addColumn(new ComponentRenderer<>(this::createProjectRenderer)).setHeader("Project").setWidth("300px").setFlexGrow(0);
		// Role column
		grid.addColumn(CUserProjectSettings::getRole).setHeader("Role").setWidth("150px").setFlexGrow(0);
		// Permissions column
		grid.addColumn(new ComponentRenderer<>(this::createPermissionRenderer)).setHeader("Permissions").setWidth("200px").setFlexGrow(1);
		return grid;
	}

	/** Create project relationship panel for user */
	private AccordionPanel createProjectRelationshipPanel(CUser user) {
		// Panel header with summary
		HorizontalLayout panelHeader = new HorizontalLayout();
		panelHeader.setAlignItems(Alignment.CENTER);
		panelHeader.add(VaadinIcon.TASKS.create());
		panelHeader.add(new Span("Project Assignments"));
		// Panel summary
		int projectCount = user.getProjectSettings() != null ? user.getProjectSettings().size() : 0;
		Span summary = new Span("(" + projectCount + " projects)");
		summary.getStyle().set("color", "var(--lumo-secondary-text-color)");
		panelHeader.add(summary);
		// Panel content
		VerticalLayout content = new VerticalLayout();
		content.setPadding(false);
		content.setSpacing(true);
		// Action buttons
		HorizontalLayout actions = createActionButtons("project", () -> openAddProjectDialog(user), () -> openEditProjectDialog(user),
				() -> deleteSelectedProjectRelationship(user));
		content.add(actions);
		// Project grid
		Grid<CUserProjectSettings> projectGrid = createProjectGrid();
		relationshipGrids.put("project", projectGrid);
		content.add(projectGrid);
		// Load data
		loadProjectRelationships(user, projectGrid);
		AccordionPanel panel = relationshipAccordion.add("Project Assignments (" + projectCount + " projects)", content);
		relationshipPanels.put("project", panel);
		return panel;
	}

	private void createProjectRelationshipPanels() {
		// Implementation for project-centric relationship management
		H3 placeholder = new H3("Project relationship management coming soon");
		add(placeholder);
	}

	/** Create project renderer with enhanced visualization */
	private Component createProjectRenderer(CUserProjectSettings settings) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(Alignment.CENTER);
		// Project icon
		layout.add(VaadinIcon.TASKS.create());
		// Project info
		VerticalLayout info = new VerticalLayout();
		info.setPadding(false);
		info.setSpacing(false);
		CProject project = settings.getProject();
		if (project != null) {
			Span projectName = new Span(project.getName());
			projectName.getStyle().set("font-weight", "bold");
			info.add(projectName);
			// Try to get team using reflection
			String team = getFieldValue(settings, "team", "squad");
			if (team != null && !team.isEmpty()) {
				Span teamSpan = new Span(team);
				teamSpan.getStyle().set("font-size", "var(--lumo-font-size-xs)").set("color", "var(--lumo-secondary-text-color)");
				info.add(teamSpan);
			}
		}
		layout.add(info);
		return layout;
	}

	/** Create relationship accordion with enhanced panels */
	private void createRelationshipAccordion() {
		relationshipAccordion = new Accordion();
		relationshipAccordion.setWidthFull();
		if (mainEntity instanceof CUser) {
			createUserRelationshipPanels();
		} else if (mainEntity instanceof CCompany) {
			createCompanyRelationshipPanels();
		} else if (mainEntity instanceof CProject) {
			createProjectRelationshipPanels();
		}
		// Open first panel by default
		if (relationshipAccordion.getChildren().count() > 0) {
			relationshipAccordion.open(0);
		}
	}

	/** Create status renderer with visual indicators */
	private Component createStatusRenderer(CUserCompanySettings settings) {
		HorizontalLayout layout = new HorizontalLayout();
		layout.setSpacing(true);
		layout.setAlignItems(Alignment.CENTER);
		// Active status
		Boolean isActive = getFieldValue(settings, "isActive", "active");
		if (isActive == null) {
			isActive = true;
		}
		if (isActive) {
			layout.add(VaadinIcon.CHECK_CIRCLE.create());
			layout.getStyle().set("color", "var(--lumo-success-color)");
		} else {
			layout.add(VaadinIcon.CIRCLE_THIN.create());
			layout.getStyle().set("color", "var(--lumo-error-color)");
		}
		// Primary indicator
		Boolean isPrimary = getFieldValue(settings, "isPrimary", "primary");
		if (Boolean.TRUE.equals(isPrimary)) {
			layout.add(VaadinIcon.STAR.create());
		}
		return layout;
	}

	/** Create the main UI with enhanced styling */
	private void createUI() {
		// Header with entity information
		createHeaderSection();
		// Relationship accordion
		createRelationshipAccordion();
		add(relationshipAccordion);
	}

	/** Create user-specific relationship panels */
	private void createUserRelationshipPanels() {
		CUser user = (CUser) mainEntity;
		// Company relationships panel
		AccordionPanel companyPanel = createCompanyRelationshipPanel(user);
		relationshipAccordion.add(companyPanel);
		// Project relationships panel
		AccordionPanel projectPanel = createProjectRelationshipPanel(user);
		relationshipAccordion.add(projectPanel);
	}

	private void deleteSelectedCompanyRelationship(CUser user) {
		@SuppressWarnings ("unchecked")
		Grid<CUserCompanySettings> grid = (Grid<CUserCompanySettings>) relationshipGrids.get("company");
		CUserCompanySettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			String companyName = selected.getCompany() != null ? selected.getCompany().getName() : "Unknown";
			new CConfirmationDialog("Are you sure you want to remove the relationship with " + companyName + "?", () -> {
				user.getCompanySettings().remove(selected);
				CUserService userService = (CUserService) serviceRegistry.get("CUser");
				userService.save(user);
				loadCompanyRelationships(user, grid);
			}).open();
		}
	}

	private void deleteSelectedProjectRelationship(CUser user) {
		@SuppressWarnings ("unchecked")
		Grid<CUserProjectSettings> grid = (Grid<CUserProjectSettings>) relationshipGrids.get("project");
		CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			String projectName = selected.getProject() != null ? selected.getProject().getName() : "Unknown";
			new CConfirmationDialog("Are you sure you want to remove the assignment to " + projectName + "?", () -> {
				user.getProjectSettings().remove(selected);
				CUserService userService = (CUserService) serviceRegistry.get("CUser");
				userService.save(user);
				loadProjectRelationships(user, grid);
			}).open();
		}
	}
	// Callback methods

	@Override
	public Object getCurrentEntity() { // TODO Auto-generated method stub
		return null;
	}

	private String getEntityDisplayName(CEntityDB<?> entity) {
		try {
			Method nameMethod = entity.getClass().getMethod("getName");
			return (String) nameMethod.invoke(entity);
		} catch (Exception e) {
			return entity.getClass().getSimpleName();
		}
	}

	@SuppressWarnings ("unchecked")
	private <T> T getFieldValue(Object obj, String... fieldNames) {
		for (String fieldName : fieldNames) {
			try {
				Field field = obj.getClass().getDeclaredField(fieldName);
				field.setAccessible(true);
				return (T) field.get(obj);
			} catch (Exception e) {
				// Try next field name
			}
		}
		return null;
	}

	/** Get color for permission badge */
	private String getPermissionColor(String permission) {
		switch (permission.toUpperCase()) {
		case "ADMIN":
			return "#ff5722";
		case "WRITE":
			return "#2196f3";
		case "READ":
			return "#4caf50";
		case "DELETE":
			return "#f44336";
		default:
			return "#9e9e9e";
		}
	}
	// Dialog action methods

	private void loadCompanyRelationships(CUser user, Grid<CUserCompanySettings> grid) {
		List<CUserCompanySettings> settings = user.getCompanySettings();
		grid.setItems(settings != null ? settings : new ArrayList<>());
	}

	private void loadProjectRelationships(CUser user, Grid<CUserProjectSettings> grid) {
		List<CUserProjectSettings> settings = user.getProjectSettings();
		grid.setItems(settings != null ? settings : new ArrayList<>());
	}
	// Placeholder methods for other entity types

	private void onProjectRelationshipSaved(CUserProjectSettings relationship) {
		CUser user = (CUser) mainEntity;
		@SuppressWarnings ("unchecked")
		Grid<CUserProjectSettings> grid = (Grid<CUserProjectSettings>) relationshipGrids.get("project");
		loadProjectRelationships(user, grid);
	}
	// Data loading methods

	private void openAddCompanyDialog(CUser user) {
		// TODO: Replace with new role-based dialog
		/* try { CUserService userService = (CUserService) serviceRegistry.get("CUser"); CCompanyService companyService = (CCompanyService)
		 * serviceRegistry.get("CCompany"); CEnhancedUserCompanyDialog dialog = new CEnhancedUserCompanyDialog(userService, companyService, null,
		 * user, this::onCompanyRelationshipSaved); dialog.open(); } catch (Exception e) { LOGGER.error("Error opening add company dialog", e); } */
	}

	private void openAddProjectDialog(CUser user) {
		try {
			CUserService userService = (CUserService) serviceRegistry.get("CUser");
			CProjectService projectService = (CProjectService) serviceRegistry.get("CProject");
			CUserProjectSettingsService userProjectSettingsService = (CUserProjectSettingsService) serviceRegistry.get("CUserProjectSettings");
			CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, userService, projectService, userProjectSettingsService, null,
					user, this::onProjectRelationshipSaved);
			dialog.open();
		} catch (Exception e) {
			LOGGER.error("Error opening add project dialog", e);
		}
	}

	private void openEditCompanyDialog(CUser user) {
		@SuppressWarnings ("unchecked")
		Grid<CUserCompanySettings> grid = (Grid<CUserCompanySettings>) relationshipGrids.get("company");
		CUserCompanySettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			// TODO: Replace with new role-based dialog
			/* try { CUserService userService = (CUserService) serviceRegistry.get("CUser"); CCompanyService companyService = (CCompanyService)
			 * serviceRegistry.get("CCompany"); CEnhancedUserCompanyDialog dialog = new CEnhancedUserCompanyDialog(userService, companyService,
			 * selected, user, this::onCompanyRelationshipSaved); dialog.open(); } catch (Exception e) {
			 * LOGGER.error("Error opening edit company dialog", e); } */
		}
	}

	private void openEditProjectDialog(CUser user) {
		@SuppressWarnings ("unchecked")
		Grid<CUserProjectSettings> grid = (Grid<CUserProjectSettings>) relationshipGrids.get("project");
		CUserProjectSettings selected = grid.asSingleSelect().getValue();
		if (selected != null) {
			try {
				CUserService userService = (CUserService) serviceRegistry.get("CUser");
				CProjectService projectService = (CProjectService) serviceRegistry.get("CProject");
				CUserProjectSettingsService userProjectSettingsService = (CUserProjectSettingsService) serviceRegistry.get("CUserProjectSettings");
				tech.derbent.users.view.CUserProjectSettingsDialog dialog = new CUserProjectSettingsDialog(this, userService, projectService,
						userProjectSettingsService, selected, user, this::onProjectRelationshipSaved);
				dialog.open();
			} catch (Exception e) {
				LOGGER.error("Error opening edit project dialog", e);
			}
		}
	}

	/** Refresh all relationship data */
	public void refreshAll() {
		if (mainEntity instanceof CUser) {
			CUser user = (CUser) mainEntity;
			@SuppressWarnings ("unchecked")
			Grid<CUserCompanySettings> companyGrid = (Grid<CUserCompanySettings>) relationshipGrids.get("company");
			if (companyGrid != null) {
				loadCompanyRelationships(user, companyGrid);
			}
			@SuppressWarnings ("unchecked")
			Grid<CUserProjectSettings> projectGrid = (Grid<CUserProjectSettings>) relationshipGrids.get("project");
			if (projectGrid != null) {
				loadProjectRelationships(user, projectGrid);
			}
		}
	}
	// Utility methods

	/** Register services for different entity types */
	public void registerServices(CUserService userService, CCompanyService companyService, CProjectService projectService,
			CUserProjectSettingsService userProjectSettingsService) {
		serviceRegistry.put("CUser", userService);
		serviceRegistry.put("CCompany", companyService);
		serviceRegistry.put("CProject", projectService);
		serviceRegistry.put("CUserProjectSettings", userProjectSettingsService);
	}

	/** Setup component with creative styling */
	private void setupComponent() {
		setSpacing(true);
		setPadding(true);
		setWidthFull();
		addClassName("creative-relationship-manager");
		getStyle().set("background", "linear-gradient(135deg, var(--lumo-base-color) 0%, var(--lumo-contrast-5pct) 100%)")
				.set("border-radius", "var(--lumo-border-radius-l)").set("box-shadow", "var(--lumo-box-shadow-m)")
				.set("border", "1px solid var(--lumo-contrast-10pct)");
	}
}
