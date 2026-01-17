package tech.derbent.app.testcases.testrun.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IGridComponent;
import tech.derbent.api.interfaces.IGridRefreshListener;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.app.testcases.testrun.domain.CTestCaseResult;
import tech.derbent.app.testcases.testrun.domain.CTestResult;
import tech.derbent.app.testcases.testrun.domain.CTestRun;
import tech.derbent.app.testcases.testrun.domain.CTestStepResult;
import tech.derbent.app.testcases.testrun.service.CTestCaseResultService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListTestCaseResults - Component for displaying test case execution results.
 * <p>
 * Read-only display of test case results within a test run. Shows result status with colored badges, execution time, and detailed information. Users
 * can view full details including test step results in a dialog.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListTestCaseResults component = new CComponentListTestCaseResults(service, sessionService);
 * component.setMasterEntity(testRun);
 * </pre>
 */
public class CComponentListTestCaseResults extends CVerticalLayout
		implements IContentOwner, IGridComponent<CTestCaseResult>, IGridRefreshListener<CTestCaseResult>, IPageServiceAutoRegistrable {

	public static final String ID_GRID = "custom-testcaseresults-grid";
	public static final String ID_HEADER = "custom-testcaseresults-header";
	public static final String ID_ROOT = "custom-testcaseresults-component";
	public static final String ID_TOOLBAR = "custom-testcaseresults-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListTestCaseResults.class);
	private static final long serialVersionUID = 1L;
	private final CTestCaseResultService testCaseResultService;
	private CButton buttonRefresh;
	private CGrid<CTestCaseResult> grid;
	private CHorizontalLayout layoutToolbar;
	private CTestRun masterEntity;
	private final List<Consumer<CTestCaseResult>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for test case results component.
	 * @param testCaseResultService the test case result service
	 * @param sessionService        the session service */
	public CComponentListTestCaseResults(final CTestCaseResultService testCaseResultService, final ISessionService sessionService) {
		Check.notNull(testCaseResultService, "TestCaseResultService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.testCaseResultService = testCaseResultService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CTestCaseResult> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing test case results");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		updateCompactMode(true);
	}

	@Override
	public void configureGrid(final CGrid<CTestCaseResult> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Execution order column
			grid1.addCustomColumn(CTestCaseResult::getExecutionOrder, "Order", "80px", "executionOrder", 0);
			// Test case name column
			grid1.addCustomColumn(result -> {
				if (result.getTestCase() != null) {
					return result.getTestCase().getName();
				}
				return "";
			}, "Test Case", "250px", "testCase", 0);
			// Result status column with colored badge
			grid1.addColumn(new ComponentRenderer<>(this::createResultBadge)).setHeader("Result").setWidth("120px").setFlexGrow(0).setKey("result");
			// Duration column
			grid1.addCustomColumn(result -> {
				if (result.getDurationMs() != null) {
					return formatDuration(result.getDurationMs());
				}
				return "-";
			}, "Duration", "100px", "durationMs", 0);
			// Notes preview column
			grid1.addCustomColumn(result -> {
				if (result.getNotes() == null || result.getNotes().isEmpty()) {
					return "";
				}
				final String notes = result.getNotes();
				return notes.length() > 50 ? notes.substring(0, 47) + "..." : notes;
			}, "Notes", "200px", "notes", 0);
			// View details button column
			grid1.addColumn(new ComponentRenderer<>(result -> {
				final CButton btnView = new CButton(VaadinIcon.EYE.create());
				btnView.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
				btnView.setTooltipText("View details");
				btnView.addClickListener(e -> showDetailsDialog(result));
				return btnView;
			})).setHeader("Actions").setWidth("100px").setFlexGrow(0);
			// Enable click to select
			grid1.addItemClickListener(event -> {
				grid1.select(event.getItem());
			});
			// Enable double-click to view details
			grid1.addItemDoubleClickListener(event -> showDetailsDialog(event.getItem()));
		} catch (final Exception e) {
			LOGGER.error("Error configuring test case results grid", e);
			CNotificationService.showException("Error configuring test case results grid", e);
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Test case results are managed via test execution.");
	}

	/** Create result status badge with appropriate color.
	 * @param result the test case result
	 * @return colored badge component */
	private Span createResultBadge(final CTestCaseResult result) {
		final CTestResult status = result.getResult() != null ? result.getResult() : CTestResult.NOT_EXECUTED;
		final Span badge = new Span(status.toString());
		badge.getStyle().set("padding", "4px 10px").set("border-radius", "12px").set("font-size", "11px").set("font-weight", "600")
				.set("text-transform", "uppercase").set("white-space", "nowrap").set("display", "inline-block");
		switch (status) {
		case PASSED:
			badge.getStyle().set("background-color", "#4CAF50").set("color", "#FFFFFF");
			break;
		case FAILED:
			badge.getStyle().set("background-color", "#F44336").set("color", "#FFFFFF");
			break;
		case BLOCKED:
			badge.getStyle().set("background-color", "#FFC107").set("color", "#000000");
			break;
		case SKIPPED:
			badge.getStyle().set("background-color", "#9E9E9E").set("color", "#FFFFFF");
			break;
		case IN_PROGRESS:
			badge.getStyle().set("background-color", "#2196F3").set("color", "#FFFFFF");
			break;
		case NOT_EXECUTED:
			badge.getStyle().set("background-color", "#E0E0E0").set("color", "#616161");
			break;
		case PARTIAL:
			badge.getStyle().set("background-color", "#FF9800").set("color", "#FFFFFF");
			break;
		default:
			badge.getStyle().set("background-color", "#BDBDBD").set("color", "#424242");
		}
		return badge;
	}

	/** Create step result badge.
	 * @param stepResult the test step result
	 * @return colored badge component */
	private Span createStepResultBadge(final CTestStepResult stepResult) {
		final CTestResult status = stepResult.getResult() != null ? stepResult.getResult() : CTestResult.NOT_EXECUTED;
		final Span badge = new Span(status.toString());
		badge.getStyle().set("padding", "2px 8px").set("border-radius", "10px").set("font-size", "10px").set("font-weight", "600")
				.set("text-transform", "uppercase").set("white-space", "nowrap").set("display", "inline-block");
		switch (status) {
		case PASSED:
			badge.getStyle().set("background-color", "#4CAF50").set("color", "#FFFFFF");
			break;
		case FAILED:
			badge.getStyle().set("background-color", "#F44336").set("color", "#FFFFFF");
			break;
		case BLOCKED:
			badge.getStyle().set("background-color", "#FFC107").set("color", "#000000");
			break;
		case SKIPPED:
			badge.getStyle().set("background-color", "#9E9E9E").set("color", "#FFFFFF");
			break;
		case NOT_EXECUTED:
			badge.getStyle().set("background-color", "#E0E0E0").set("color", "#616161");
			break;
		default:
			badge.getStyle().set("background-color", "#BDBDBD").set("color", "#424242");
		}
		return badge;
	}

	/** Create test step results grid for details dialog.
	 * @param testCaseResult the parent test case result
	 * @return grid of test step results */
	private Grid<CTestStepResult> createStepResultsGrid(final CTestCaseResult testCaseResult) {
		final Grid<CTestStepResult> stepGrid = new Grid<>(CTestStepResult.class, false);
		stepGrid.setHeight("300px");
		stepGrid.addColumn(stepResult -> {
			if (stepResult.getTestStep() != null) {
				return stepResult.getTestStep().getStepOrder();
			}
			return "";
		}).setHeader("Step").setWidth("60px").setFlexGrow(0);
		stepGrid.addColumn(stepResult -> {
			if (stepResult.getTestStep() != null) {
				final String action = stepResult.getTestStep().getAction();
				return action != null ? action.length() > 40 ? action.substring(0, 37) + "..." : action : "";
			}
			return "";
		}).setHeader("Action").setAutoWidth(true);
		stepGrid.addColumn(new ComponentRenderer<>(this::createStepResultBadge)).setHeader("Result").setWidth("110px").setFlexGrow(0);
		stepGrid.addColumn(stepResult -> {
			if (stepResult.getDurationMs() != null) {
				return formatDuration(stepResult.getDurationMs());
			}
			return "-";
		}).setHeader("Duration").setWidth("90px").setFlexGrow(0);
		// Load and sort step results
		final List<CTestStepResult> stepResults = new ArrayList<>(testCaseResult.getTestStepResults());
		stepResults.sort(Comparator.comparingInt(sr -> {
			if (sr.getTestStep() != null) {
				return sr.getTestStep().getStepOrder();
			}
			return 0;
		}));
		stepGrid.setItems(stepResults);
		return stepGrid;
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Refresh button
		buttonRefresh = new CButton(VaadinIcon.REFRESH.create());
		buttonRefresh.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonRefresh.setTooltipText("Refresh results");
		buttonRefresh.addClickListener(e -> on_buttonRefresh_clicked());
		layoutToolbar.add(buttonRefresh);
	}

	/** Format duration in milliseconds to human-readable string.
	 * @param durationMs duration in milliseconds
	 * @return formatted duration string */
	private String formatDuration(final Long durationMs) {
		if (durationMs == null || durationMs == 0) {
			return "0ms";
		}
		if (durationMs < 1000) {
			return durationMs + "ms";
		}
		final long seconds = durationMs / 1000;
		final long ms = durationMs % 1000;
		if (seconds < 60) {
			return String.format("%ds %dms", seconds, ms);
		}
		final long minutes = seconds / 60;
		final long remainingSeconds = seconds % 60;
		return String.format("%dm %ds", minutes, remainingSeconds);
	}

	@Override
	public String getComponentName() { return "testCaseResults"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity != null) {
			return masterEntity.getId() != null ? masterEntity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return testCaseResultService; }

	@Override
	public CGrid<CTestCaseResult> getGrid() { return grid; }

	/** Get sorted list of results by execution order.
	 * @return sorted list of test case results */
	private List<CTestCaseResult> getSortedResults() {
		final List<CTestCaseResult> results = new ArrayList<>(masterEntity.getTestCaseResults());
		results.sort(Comparator.comparing(CTestCaseResult::getExecutionOrder, Comparator.nullsLast(Comparator.naturalOrder())));
		return results;
	}

	@Override
	public CEntityDB<?> getValue() { return masterEntity; }

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		// Header
		final CH3 header = new CH3("Test Case Results");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CTestCaseResult.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(e -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("400px");
		add(grid);
		// Set initial compact mode
		updateCompactMode(true);
	}

	@Override
	public void notifyRefreshListeners(final CTestCaseResult changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CTestCaseResult> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Handle refresh button click. */
	protected void on_buttonRefresh_clicked() {
		try {
			refreshGrid();
			CNotificationService.showSuccess("Test case results refreshed");
		} catch (final Exception e) {
			CNotificationService.showException("Error refreshing test case results", e);
		}
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing test case results");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load results from parent entity's collection
		final List<CTestCaseResult> results = getSortedResults();
		grid.setItems(results);
		grid.asSingleSelect().clear();
		updateCompactMode(results.isEmpty());
		LOGGER.debug("Loaded {} test case results for test run", results.size());
	}

	@Override
	public void registerWithPageService(final tech.derbent.api.services.pageservice.CPageService<?> pageService) {
		tech.derbent.api.utils.Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", getClass().getSimpleName(), getComponentName());
	}

	@Override
	public void removeRefreshListener(final Consumer<CTestCaseResult> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CTestRun) {
			masterEntity = (CTestRun) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity is not a CTestRun: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param testRun the test run that owns the results */
	public void setMasterEntity(final CTestRun testRun) {
		masterEntity = testRun;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof CTestRun) {
			masterEntity = (CTestRun) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Show detailed dialog for a test case result.
	 * @param result the test case result to display */
	protected void showDetailsDialog(final CTestCaseResult result) {
		try {
			Check.notNull(result, "Test case result cannot be null");
			final Dialog dialog = new Dialog();
			dialog.setWidth("900px");
			dialog.setHeight("700px");
			dialog.setModal(true);
			dialog.setDraggable(true);
			dialog.setResizable(true);
			// Main layout
			final CVerticalLayout mainLayout = new CVerticalLayout();
			mainLayout.setPadding(true);
			mainLayout.setSpacing(true);
			mainLayout.setSizeFull();
			// Header
			final CH4 header = new CH4("Test Case Result Details");
			header.getStyle().set("margin-top", "0");
			mainLayout.add(header);
			// Test case info section
			final CVerticalLayout infoSection = new CVerticalLayout();
			infoSection.setPadding(true);
			infoSection.setSpacing(false);
			infoSection.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "4px");
			// Test case name
			final Div nameDiv = new Div();
			nameDiv.add(createBoldSpan("Test Case: "));
			if (result.getTestCase() != null) {
				nameDiv.add(new Span(result.getTestCase().getName()));
			}
			nameDiv.getStyle().set("margin-bottom", "8px");
			infoSection.add(nameDiv);
			// Result status
			final CHorizontalLayout resultLayout = new CHorizontalLayout();
			resultLayout.setSpacing(true);
			resultLayout.getStyle().set("margin-bottom", "8px");
			resultLayout.add(createBoldSpan("Result: "));
			resultLayout.add(createResultBadge(result));
			infoSection.add(resultLayout);
			// Execution info
			final Div executionDiv = new Div();
			executionDiv.add(createBoldSpan("Execution Time: "));
			if (result.getDurationMs() != null) {
				executionDiv.add(new Span(formatDuration(result.getDurationMs())));
			} else {
				executionDiv.add(new Span("-"));
			}
			executionDiv.getStyle().set("margin-bottom", "8px");
			infoSection.add(executionDiv);
			// Execution order
			if (result.getExecutionOrder() != null) {
				final Div orderDiv = new Div();
				orderDiv.add(createBoldSpan("Execution Order: "));
				orderDiv.add(new Span(String.valueOf(result.getExecutionOrder())));
				orderDiv.getStyle().set("margin-bottom", "8px");
				infoSection.add(orderDiv);
			}
			mainLayout.add(infoSection);
			// Expected result (from test case)
			if (result.getTestCase() != null && result.getTestCase().getDescription() != null && !result.getTestCase().getDescription().isEmpty()) {
				final CVerticalLayout expectedSection = new CVerticalLayout();
				expectedSection.setPadding(true);
				expectedSection.setSpacing(false);
				expectedSection.getStyle().set("background-color", "#E8F5E9").set("border-radius", "4px");
				final Span expectedLabel = createBoldSpan("Test Case Description:");
				expectedLabel.getStyle().set("margin-bottom", "4px").set("display", "block");
				expectedSection.add(expectedLabel);
				final CSpan expectedText = new CSpan(result.getTestCase().getDescription());
				expectedText.getStyle().set("white-space", "pre-wrap");
				expectedSection.add(expectedText);
				mainLayout.add(expectedSection);
			}
			// Notes
			if (result.getNotes() != null && !result.getNotes().isEmpty()) {
				final CVerticalLayout notesSection = new CVerticalLayout();
				notesSection.setPadding(true);
				notesSection.setSpacing(false);
				notesSection.getStyle().set("background-color", "#FFF9C4").set("border-radius", "4px");
				final Span notesLabel = createBoldSpan("Notes:");
				notesLabel.getStyle().set("margin-bottom", "4px").set("display", "block");
				notesSection.add(notesLabel);
				final CSpan notesText = new CSpan(result.getNotes());
				notesText.getStyle().set("white-space", "pre-wrap");
				notesSection.add(notesText);
				mainLayout.add(notesSection);
			}
			// Error details
			if (result.getErrorDetails() != null && !result.getErrorDetails().isEmpty()) {
				final CVerticalLayout errorSection = new CVerticalLayout();
				errorSection.setPadding(true);
				errorSection.setSpacing(false);
				errorSection.getStyle().set("background-color", "#FFEBEE").set("border-radius", "4px");
				final Span errorLabel = createBoldSpan("Error Details:");
				errorLabel.getStyle().set("margin-bottom", "4px").set("display", "block");
				errorSection.add(errorLabel);
				final CSpan errorText = new CSpan(result.getErrorDetails());
				errorText.getStyle().set("white-space", "pre-wrap").set("font-family", "monospace");
				errorSection.add(errorText);
				mainLayout.add(errorSection);
			}
			// Test step results section
			final Set<CTestStepResult> stepResults = result.getTestStepResults();
			if (stepResults != null && !stepResults.isEmpty()) {
				final CH4 stepsHeader = new CH4("Test Step Results");
				stepsHeader.getStyle().set("margin-top", "16px").set("margin-bottom", "8px");
				mainLayout.add(stepsHeader);
				final Grid<CTestStepResult> stepGrid = createStepResultsGrid(result);
				mainLayout.add(stepGrid);
			}
			// Close button
			final CHorizontalLayout buttonLayout = new CHorizontalLayout();
			buttonLayout.setSpacing(true);
			final CButton closeButton = new CButton("Close", VaadinIcon.CLOSE.create());
			closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			closeButton.addClickListener(e -> dialog.close());
			buttonLayout.add(closeButton);
			mainLayout.add(buttonLayout);
			dialog.add(mainLayout);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error showing test case result details", e);
			CNotificationService.showException("Error showing test case result details", e);
		}
	}

	/** Create a bold Span element.
	 * @param text text content
	 * @return bold Span */
	private Span createBoldSpan(final String text) {
		final Span span = new Span(text);
		span.getStyle().set("font-weight", "bold");
		return span;
	}

	/** Update component height based on content.
	 * @param isEmpty true if no test case results exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			grid.setHeight("200px");
			setHeight("250px");
			LOGGER.debug("Compact mode: No test case results");
		} else {
			grid.setHeight("400px");
			setHeight("auto");
			LOGGER.debug("Normal mode: Has test case results");
		}
	}
}
