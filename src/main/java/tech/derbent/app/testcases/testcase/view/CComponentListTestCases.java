package tech.derbent.app.testcases.testcase.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testcase.domain.CTestCase;
import tech.derbent.app.testcases.testcase.service.CTestCaseService;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListTestCases - Component for managing test cases in test scenarios.
 * <p>
 * Displays test cases with priority, severity, and status. Supports CRUD operations.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListTestCases component = new CComponentListTestCases(service, sessionService);
 * component.setMasterEntity(testScenario);
 * </pre>
 */
public class CComponentListTestCases extends CVerticalLayout
        implements IContentOwner, IGridComponent<CTestCase>, IGridRefreshListener<CTestCase>, IPageServiceAutoRegistrable {

    public static final String ID_GRID = "custom-testcases-grid";
    public static final String ID_HEADER = "custom-testcases-header";
    public static final String ID_ROOT = "custom-testcases-component";
    public static final String ID_TOOLBAR = "custom-testcases-toolbar";
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListTestCases.class);
    private static final long serialVersionUID = 1L;
    private final CTestCaseService testCaseService;
    private CButton buttonAdd;
    private CButton buttonDelete;
    private CButton buttonEdit;
    private CGrid<CTestCase> grid;
    private CHorizontalLayout layoutToolbar;
    private CTestScenario masterEntity;
    private final List<Consumer<CTestCase>> refreshListeners = new ArrayList<>();
    private final ISessionService sessionService;

    /** Constructor for test case list component.
     * @param testCaseService the test case service
     * @param sessionService  the session service */
    public CComponentListTestCases(final CTestCaseService testCaseService, final ISessionService sessionService) {
        Check.notNull(testCaseService, "TestCaseService cannot be null");
        Check.notNull(sessionService, "SessionService cannot be null");
        this.testCaseService = testCaseService;
        this.sessionService = sessionService;
        initializeComponent();
    }

    @Override
    public void addRefreshListener(final Consumer<CTestCase> listener) {
        Check.notNull(listener, "Refresh listener cannot be null");
        refreshListeners.add(listener);
    }

    @Override
    public void clearGrid() {
        Check.notNull(grid, "Grid cannot be null when clearing test cases");
        grid.setItems(List.of());
        grid.asSingleSelect().clear();
        updateButtonStates(null);
        updateCompactMode(true);
    }

    /** Configure grid columns. */
    @Override
    public void configureGrid(final CGrid<CTestCase> grid1) {
        try {
            Check.notNull(grid1, "Grid cannot be null");
            // Name column
            grid1.addCustomColumn(CTestCase::getName, "Name", "200px", "name", 0);
            // Priority column
            grid1.addCustomColumn(tc -> tc.getPriority() != null ? tc.getPriority().toString() : "-", "Priority", "100px", "priority", 1);
            // Severity column
            grid1.addCustomColumn(tc -> tc.getSeverity() != null ? tc.getSeverity().toString() : "-", "Severity", "100px", "severity", 2);
            // Status column
            grid1.addCustomColumn(tc -> tc.getStatus() != null ? tc.getStatus().getName() : "-", "Status", "120px", "status", 3);
            // Automated column
            grid1.addCustomColumn(tc -> Boolean.TRUE.equals(tc.getAutomated()) ? "Yes" : "No", "Automated", "100px", "automated", 4);
            LOGGER.debug("Grid columns configured for test cases");
        } catch (final Exception e) {
            LOGGER.error("Error configuring test case grid columns", e);
            CNotificationService.showException("Error configuring grid", e);
        }
    }

    @Override
    public CGrid<CTestCase> getGrid() {
        return grid;
    }

    @Override
    public void notifyRefreshListeners(final CTestCase entity) {
        Check.notNull(entity, "Entity cannot be null when notifying listeners");
        refreshListeners.forEach(listener -> {
            try {
                listener.accept(entity);
            } catch (final Exception e) {
                LOGGER.error("Error in refresh listener for test case: {}", entity.getId(), e);
            }
        });
    }

    @Override
    public CEntityDB<?> createNewEntityInstance() throws Exception {
        Check.notNull(masterEntity, "Master entity must be set to create new test case");
        Check.notNull(masterEntity.getProject(), "Project must be set to create new test case");
        final CTestCase testCase = new CTestCase("New Test Case", masterEntity.getProject());
        testCase.setTestScenario(masterEntity);
        return testCase;
    }

    @Override
    public CEntityDB<?> getValue() {
        return grid.asSingleSelect().getValue();
    }

    @Override
    public String getCurrentEntityIdString() {
        final CTestCase selected = grid.asSingleSelect().getValue();
        return selected != null && selected.getId() != null ? selected.getId().toString() : null;
    }

    @Override
    public CAbstractService<?> getEntityService() {
        return testCaseService;
    }

    @Override
    public void setValue(final CEntityDB<?> entity) {
        if (entity instanceof CTestCase) {
            grid.asSingleSelect().setValue((CTestCase) entity);
        } else {
            grid.asSingleSelect().clear();
        }
    }

    @Override
    public void populateForm() {
        // Grid-based component, no form to populate
    }

    @Override
    public String getComponentName() {
        return "testCases";
    }

    @Override
    public void registerWithPageService(final tech.derbent.api.services.pageservice.CPageService<?> pageService) {
        // Auto-registration with page service if needed
        LOGGER.debug("Registering test cases component with page service");
    }

    public void onGridRefresh() {
        refreshGrid();
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
            final List<CTestCase> testCases = new ArrayList<>(masterEntity.getTestCases());
            testCases.sort(Comparator.comparing(CTestCase::getId, Comparator.nullsLast(Comparator.reverseOrder())));
            grid.setItems(testCases);
            updateCompactMode(testCases.isEmpty());
            LOGGER.debug("Refreshed test cases grid with {} items", testCases.size());
        } catch (final Exception e) {
            LOGGER.error("Error refreshing test cases grid", e);
            CNotificationService.showException("Error refreshing test cases", e);
        }
    }

    /** Set the master entity (test scenario).
     * @param scenario the test scenario */
    public void setMasterEntity(final CTestScenario scenario) {
        Check.notNull(scenario, "Test scenario cannot be null");
        this.masterEntity = scenario;
        refreshGrid();
        LOGGER.debug("Master entity set for test cases component: {}", scenario.getName());
    }

    /** Create and initialize the component. */
    private void initializeComponent() {
        try {
            setId(ID_ROOT);
            setPadding(false);
            setSpacing(false);
            setWidthFull();
            // Header
            final CH3 header = new CH3("Test Cases");
            header.setId(ID_HEADER);
            add(header);
            // Toolbar
            layoutToolbar = new CHorizontalLayout();
            layoutToolbar.setId(ID_TOOLBAR);
            layoutToolbar.setSpacing(true);
            buttonAdd = new CButton("Add", VaadinIcon.PLUS.create(), e -> on_add_clicked());
            buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            buttonEdit = new CButton("Edit", VaadinIcon.EDIT.create(), e -> on_edit_clicked());
            buttonEdit.addThemeVariants(ButtonVariant.LUMO_SMALL);
            buttonEdit.setEnabled(false);
            buttonDelete = new CButton("Delete", VaadinIcon.TRASH.create(), e -> on_delete_clicked());
            buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            buttonDelete.setEnabled(false);
            layoutToolbar.add(buttonAdd, buttonEdit, buttonDelete);
            add(layoutToolbar);
            // Grid
            grid = new CGrid<>(CTestCase.class);
            grid.setId(ID_GRID);
            grid.setHeight("300px");
            configureGrid(grid);
            grid.asSingleSelect().addValueChangeListener(event -> {
                final CTestCase selected = event.getValue();
                updateButtonStates(selected);
            });
            add(grid);
            LOGGER.debug("Test cases component initialized");
        } catch (final Exception e) {
            LOGGER.error("Error initializing test cases component", e);
            add(new Span("Error initializing component: " + e.getMessage()));
        }
    }

    /** Handle add button click. */
    private void on_add_clicked() {
        try {
            Check.notNull(masterEntity, "Test scenario must be set before adding test cases");
            // Navigate to test case page for creation
            final Long scenarioId = masterEntity.getId();
            Check.notNull(scenarioId, "Test scenario must be saved before adding test cases");
            getUI().ifPresent(ui -> {
                // Open test case page with scenario pre-selected
                ui.navigate("tests/test-cases?scenario=" + scenarioId);
            });
            LOGGER.debug("Navigating to add test case for scenario: {}", masterEntity.getName());
        } catch (final Exception e) {
            LOGGER.error("Error adding test case", e);
            CNotificationService.showException("Error adding test case", e);
        }
    }

    /** Handle delete button click. */
    private void on_delete_clicked() {
        try {
            final CTestCase selected = grid.asSingleSelect().getValue();
            Check.notNull(selected, "No test case selected for deletion");
            Check.notNull(masterEntity, "Master entity cannot be null");
            // Remove from scenario
            masterEntity.getTestCases().remove(selected);
            // Delete the test case
            testCaseService.delete(selected);
            refreshGrid();
            CNotificationService.showSuccess("Test case deleted successfully");
            LOGGER.info("Deleted test case: {}", selected.getName());
        } catch (final Exception e) {
            LOGGER.error("Error deleting test case", e);
            CNotificationService.showException("Error deleting test case", e);
        }
    }

    /** Handle edit button click. */
    private void on_edit_clicked() {
        try {
            final CTestCase selected = grid.asSingleSelect().getValue();
            Check.notNull(selected, "No test case selected for editing");
            final Long testCaseId = selected.getId();
            Check.notNull(testCaseId, "Test case must be saved before editing");
            getUI().ifPresent(ui -> {
                // Navigate to test case page for editing
                ui.navigate("tests/test-cases/" + testCaseId);
            });
            LOGGER.debug("Navigating to edit test case: {}", selected.getName());
        } catch (final Exception e) {
            LOGGER.error("Error editing test case", e);
            CNotificationService.showException("Error editing test case", e);
        }
    }

    /** Update button states based on selection.
     * @param selected the selected test case */
    private void updateButtonStates(final CTestCase selected) {
        final boolean hasSelection = selected != null;
        buttonEdit.setEnabled(hasSelection);
        buttonDelete.setEnabled(hasSelection);
    }

    /** Update component height based on content.
     * @param isEmpty true if no test cases exist */
    private void updateCompactMode(final boolean isEmpty) {
        if (isEmpty) {
            grid.setHeight("200px");
            setHeight("250px");
            LOGGER.debug("Compact mode: No test cases");
        } else {
            grid.setHeight("400px");
            setHeight("auto");
            LOGGER.debug("Normal mode: Has test cases");
        }
    }
}
