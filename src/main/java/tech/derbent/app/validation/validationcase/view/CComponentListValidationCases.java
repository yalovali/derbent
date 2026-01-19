package tech.derbent.app.validation.validationcase.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationcase.service.CValidationCaseService;
import tech.derbent.app.validation.validationsuite.domain.CValidationSuite;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListValidationCases - Component for managing validation cases in validation suites.
 * <p>
 * Displays validation cases with priority, severity, and status. Supports CRUD operations.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListValidationCases component = new CComponentListValidationCases(service, sessionService);
 * component.setMasterEntity(validationSuite);
 * </pre>
 */
public class CComponentListValidationCases extends CVerticalLayout
		implements IContentOwner, IGridComponent<CValidationCase>, IGridRefreshListener<CValidationCase>, IPageServiceAutoRegistrable {

	public static final String ID_GRID = "custom-validationcases-grid";
	public static final String ID_HEADER = "custom-validationcases-header";
	public static final String ID_ROOT = "custom-validationcases-component";
	public static final String ID_TOOLBAR = "custom-validationcases-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListValidationCases.class);
	private static final long serialVersionUID = 1L;
	private final CValidationCaseService validationCaseService;
	private CButton buttonAdd;
	private CButton buttonDelete;
	private CButton buttonEdit;
	private CGrid<CValidationCase> grid;
	private CHorizontalLayout layoutToolbar;
	private CValidationSuite masterEntity;
	private final List<Consumer<CValidationCase>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for validation case list component.
	 * @param validationCaseService the validation case service
	 * @param sessionService  the session service */
	public CComponentListValidationCases(final CValidationCaseService validationCaseService, final ISessionService sessionService) {
		Check.notNull(validationCaseService, "ValidationCaseService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.validationCaseService = validationCaseService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CValidationCase> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing validation cases");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		updateButtonStates(null);
		updateCompactMode(true);
	}

	/** Configure grid columns. */
	@Override
	public void configureGrid(final CGrid<CValidationCase> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Name column
			grid1.addCustomColumn(CValidationCase::getName, "Name", "200px", "name", 0);
			// Priority column
			grid1.addCustomColumn(tc -> tc.getPriority() != null ? tc.getPriority().toString() : "-", "Priority", "100px", "priority", 1);
			// Severity column
			grid1.addCustomColumn(tc -> tc.getSeverity() != null ? tc.getSeverity().toString() : "-", "Severity", "100px", "severity", 2);
			// Status column
			grid1.addCustomColumn(tc -> tc.getStatus() != null ? tc.getStatus().getName() : "-", "Status", "120px", "status", 3);
			// Automated column
			grid1.addCustomColumn(tc -> Boolean.TRUE.equals(tc.getAutomated()) ? "Yes" : "No", "Automated", "100px", "automated", 4);
			LOGGER.debug("Grid columns configured for validation cases");
		} catch (final Exception e) {
			LOGGER.error("Error configuring validation case grid columns", e);
			CNotificationService.showException("Error configuring grid", e);
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		Check.notNull(masterEntity, "Master entity must be set to create new validation case");
		Check.notNull(masterEntity.getProject(), "Project must be set to create new validation case");
		final CValidationCase validationCase = new CValidationCase("New Validation Case", masterEntity.getProject());
		validationCase.setValidationSuite(masterEntity);
		return validationCase;
	}

	@Override
	public String getComponentName() { return "validationCases"; }

	@Override
	public String getCurrentEntityIdString() {
		final CValidationCase selected = grid.asSingleSelect().getValue();
		return selected != null && selected.getId() != null ? selected.getId().toString() : null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return validationCaseService; }

	@Override
	public CGrid<CValidationCase> getGrid() { return grid; }

	@Override
	public CEntityDB<?> getValue() { return grid.asSingleSelect().getValue(); }

	/** Create and initialize the component. */
	private void initializeComponent() {
		try {
			setId(ID_ROOT);
			setPadding(false);
			setSpacing(false);
			setWidthFull();
			// Header
			final CH3 header = new CH3("Validation Cases");
			header.setId(ID_HEADER);
			add(header);
			// Toolbar
			layoutToolbar = new CHorizontalLayout();
			layoutToolbar.setId(ID_TOOLBAR);
			layoutToolbar.setSpacing(true);
			buttonAdd = new CButton("Add", VaadinIcon.PLUS.create(), event -> on_add_clicked());
			buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
			buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create(), event -> on_edit_clicked());
			buttonEdit.addThemeVariants(ButtonVariant.LUMO_SMALL);
			buttonEdit.setEnabled(false);
			buttonDelete = new CButton("Delete", VaadinIcon.TRASH.create(), event -> on_delete_clicked());
			buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
			buttonDelete.setEnabled(false);
			layoutToolbar.add(buttonAdd, buttonEdit, buttonDelete);
			add(layoutToolbar);
			// Grid
			grid = new CGrid<>(CValidationCase.class);
			grid.setId(ID_GRID);
			grid.setHeight("300px");
			configureGrid(grid);
			grid.asSingleSelect().addValueChangeListener(event -> {
				final CValidationCase selected = event.getValue();
				updateButtonStates(selected);
			});
			add(grid);
			LOGGER.debug("Validation cases component initialized");
		} catch (final Exception e) {
			LOGGER.error("Error initializing validation cases component", e);
			add(new Span("Error initializing component: " + e.getMessage()));
		}
	}

	@Override
	public void notifyRefreshListeners(final CValidationCase entity) {
		Check.notNull(entity, "Entity cannot be null when notifying listeners");
		refreshListeners.forEach(listener -> {
			try {
				listener.accept(entity);
			} catch (final Exception e) {
				LOGGER.error("Error in refresh listener for validation case: {}", entity.getId(), e);
			}
		});
	}

	/** Handle add button click. */
	private void on_add_clicked() {
		try {
			Check.notNull(masterEntity, "Validation suite must be set before adding validation cases");
			// Navigate to validation case page for creation
			final Long scenarioId = masterEntity.getId();
			Check.notNull(scenarioId, "Validation suite must be saved before adding validation cases");
			getUI().ifPresent(ui -> {
				// Open validation case page with suite pre-selected
				ui.navigate("cdynamicpagerouter/CValidationCase?validationSuite=" + scenarioId);
			});
			LOGGER.debug("Navigating to add validation case for scenario: {}", masterEntity.getName());
		} catch (final Exception e) {
			LOGGER.error("Error adding validation case", e);
			CNotificationService.showException("Error adding validation case", e);
		}
	}

	/** Handle delete button click. */
	private void on_delete_clicked() {
		try {
			final CValidationCase selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation case selected for deletion");
			Check.notNull(masterEntity, "Master entity cannot be null");
			// Remove from scenario
			masterEntity.getValidationCases().remove(selected);
			// Delete the validation case
			validationCaseService.delete(selected);
			refreshGrid();
			CNotificationService.showSuccess("Validation case deleted successfully");
			LOGGER.info("Deleted validation case: {}", selected.getName());
		} catch (final Exception e) {
			LOGGER.error("Error deleting validation case", e);
			CNotificationService.showException("Error deleting validation case", e);
		}
	}

	/** Handle edit button click. */
	private void on_edit_clicked() {
		try {
			final CValidationCase selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation case selected for editing");
			final Long validationCaseId = selected.getId();
			Check.notNull(validationCaseId, "Validation case must be saved before editing");
			getUI().ifPresent(ui -> {
				// Navigate to validation case page for editing
				ui.navigate("cdynamicpagerouter/CValidationCase/" + validationCaseId);
			});
			LOGGER.debug("Navigating to edit validation case: {}", selected.getName());
		} catch (final Exception e) {
			LOGGER.error("Error editing validation case", e);
			CNotificationService.showException("Error editing validation case", e);
		}
	}

	public void onGridRefresh() {
		refreshGrid();
	}

	@Override
	public void populateForm() {
		// Grid-based component, no form to populate
	}

	public void refreshComponent() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		try {
			if (masterEntity == null) {
				LOGGER.debug("No master entity set, clearing grid");
				clearGrid();
				return;
			}
			final List<CValidationCase> validationCases = new ArrayList<>(masterEntity.getValidationCases());
			validationCases.sort(Comparator.comparing(CValidationCase::getId, Comparator.nullsLast(Comparator.reverseOrder())));
			grid.setItems(validationCases);
			updateCompactMode(validationCases.isEmpty());
			LOGGER.debug("Refreshed validation cases grid with {} items", validationCases.size());
		} catch (final Exception e) {
			LOGGER.error("Error refreshing validation cases grid", e);
			CNotificationService.showException("Error refreshing validation cases", e);
		}
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		// Auto-registration with page service if needed
		LOGGER.debug("Registering validation cases component with page service");
	}

	/** Set the master entity (validation suite).
	 * @param scenario the validation suite */
	public void setMasterEntity(final CValidationSuite scenario) {
		Check.notNull(scenario, "Validation suite cannot be null");
		masterEntity = scenario;
		refreshGrid();
		LOGGER.debug("Master entity set for validation cases component: {}", scenario.getName());
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity instanceof CValidationCase) {
			grid.asSingleSelect().setValue((CValidationCase) entity);
		} else {
			grid.asSingleSelect().clear();
		}
	}

	/** Update button states based on selection.
	 * @param selected the selected validation case */
	private void updateButtonStates(final CValidationCase selected) {
		final boolean hasSelection = selected != null;
		buttonEdit.setEnabled(hasSelection);
		buttonDelete.setEnabled(hasSelection);
	}

	/** Update component height based on content.
	 * @param isEmpty true if no validation cases exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			grid.setHeight("200px");
			setHeight("250px");
			LOGGER.debug("Compact mode: No validation cases");
		} else {
			grid.setHeight("400px");
			setHeight("auto");
			LOGGER.debug("Normal mode: Has validation cases");
		}
	}
}
