package tech.derbent.plm.validation.validationsession.view;

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
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.api.ui.component.basic.CVerticalLayout;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.validation.validationsession.domain.CValidationCaseResult;
import tech.derbent.plm.validation.validationsession.domain.CValidationResult;
import tech.derbent.plm.validation.validationsession.domain.CValidationSession;
import tech.derbent.plm.validation.validationsession.domain.CValidationStepResult;
import tech.derbent.plm.validation.validationsession.service.CValidationCaseResultService;
import tech.derbent.base.session.service.ISessionService;

/** CComponentListValidationCaseResults - Component for displaying validation case execution results.
 * <p>
 * Read-only display of validation case results within a validation session. Shows result status with colored badges, execution time, and detailed
 * information. Users can view full details including validation step results in a dialog.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListValidationCaseResults component = new CComponentListValidationCaseResults(service, sessionService);
 * component.setMasterEntity(validationSession);
 * </pre>
 */
public class CComponentListValidationCaseResults extends CVerticalLayout
		implements IContentOwner, IGridComponent<CValidationCaseResult>, IGridRefreshListener<CValidationCaseResult>, IPageServiceAutoRegistrable {

	public static final String ID_GRID = "custom-validationcaseresults-grid";
	public static final String ID_HEADER = "custom-validationcaseresults-header";
	public static final String ID_ROOT = "custom-validationcaseresults-component";
	public static final String ID_TOOLBAR = "custom-validationcaseresults-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListValidationCaseResults.class);
	private static final long serialVersionUID = 1L;

	/** Format duration in milliseconds to human-readable string.
	 * @param durationMs duration in milliseconds
	 * @return formatted duration string */
	private static String formatDuration(final Long durationMs) {
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

	private CButton buttonRefresh;
	private CGrid<CValidationCaseResult> grid;
	private CHorizontalLayout layoutToolbar;
	private CValidationSession masterEntity;
	private final List<Consumer<CValidationCaseResult>> refreshListeners = new ArrayList<>();
	private final CValidationCaseResultService validationCaseResultService;

	/** Constructor for validation case results component.
	 * @param validationCaseResultService the validation case result service
	 * @param sessionService              the session service */
	public CComponentListValidationCaseResults(final CValidationCaseResultService validationCaseResultService, final ISessionService sessionService) {
		Check.notNull(validationCaseResultService, "ValidationCaseResultService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.validationCaseResultService = validationCaseResultService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CValidationCaseResult> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing validation case results");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		updateCompactMode(true);
	}

	
	@Override
	public void configureGrid(final CGrid<CValidationCaseResult> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Execution order column
			grid1.addCustomColumn(CValidationCaseResult::getExecutionOrder, "Order", "80px", "executionOrder", 0);
			// Validation case name column
			grid1.addCustomColumn(result -> {
				if (result.getValidationCase() != null) {
					return result.getValidationCase().getName();
				}
				return "";
			}, "Validation Case", "250px", "validationCase", 0);
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
				btnView.addClickListener(event -> showDetailsDialog(result));
				return btnView;
			})).setHeader("Actions").setWidth("100px").setFlexGrow(0);
			// Enable click to select
			grid1.addItemClickListener(event -> {
				grid1.select(event.getItem());
			});
			// Enable double-click to view details
			grid1.addItemDoubleClickListener(event -> showDetailsDialog(event.getItem()));
		} catch (final Exception e) {
			LOGGER.error("Error configuring validation case results grid", e);
			CNotificationService.showException("Error configuring validation case results grid", e);
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

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Validation case results are managed via validation execution.");
	}

	/** Create result status badge with appropriate color.
	 * @param result the validation case result
	 * @return colored badge component */
	private Span createResultBadge(final CValidationCaseResult result) {
		final CValidationResult status = result.getResult() != null ? result.getResult() : CValidationResult.NOT_EXECUTED;
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
	 * @param stepResult the validation step result
	 * @return colored badge component */
	private Span createStepResultBadge(final CValidationStepResult stepResult) {
		final CValidationResult status = stepResult.getResult() != null ? stepResult.getResult() : CValidationResult.NOT_EXECUTED;
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

	/** Create validation step results grid for details dialog.
	 * @param validationCaseResult the parent validation case result
	 * @return grid of validation step results */
	private Grid<CValidationStepResult> createStepResultsGrid(final CValidationCaseResult validationCaseResult) {
		final Grid<CValidationStepResult> stepGrid = new Grid<>(CValidationStepResult.class, false);
		stepGrid.setHeight("300px");
		stepGrid.addColumn(stepResult -> {
			if (stepResult.getValidationStep() != null) {
				return stepResult.getValidationStep().getStepOrder();
			}
			return "";
		}).setHeader("Step").setWidth("60px").setFlexGrow(0);
		stepGrid.addColumn(stepResult -> {
			if (stepResult.getValidationStep() != null) {
				final String action = stepResult.getValidationStep().getAction();
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
		final List<CValidationStepResult> stepResults = new ArrayList<>(validationCaseResult.getValidationStepResults());
		stepResults.sort(Comparator.comparingInt(sr -> {
			if (sr.getValidationStep() != null) {
				return sr.getValidationStep().getStepOrder();
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
		buttonRefresh.addClickListener(event -> on_buttonRefresh_clicked());
		layoutToolbar.add(buttonRefresh);
	}

	@Override
	public String getComponentName() { return "validationCaseResults"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity != null) {
			return masterEntity.getId() != null ? masterEntity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return validationCaseResultService; }

	@Override
	public CGrid<CValidationCaseResult> getGrid() { return grid; }

	/** Get sorted list of results by execution order.
	 * @return sorted list of validation case results */
	private List<CValidationCaseResult> getSortedResults() {
		final List<CValidationCaseResult> results = new ArrayList<>(masterEntity.getValidationCaseResults());
		results.sort(Comparator.comparing(CValidationCaseResult::getExecutionOrder, Comparator.nullsLast(Comparator.naturalOrder())));
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
		final CH3 header = new CH3("Validation Case Results");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CValidationCaseResult.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(event -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("400px");
		add(grid);
		// Set initial compact mode
		updateCompactMode(true);
	}

	@Override
	public void notifyRefreshListeners(final CValidationCaseResult changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CValidationCaseResult> listener : refreshListeners) {
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
			CNotificationService.showSuccess("Validation case results refreshed");
		} catch (final Exception e) {
			CNotificationService.showException("Error refreshing validation case results", e);
		}
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing validation case results");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load results from parent entity's collection
		final List<CValidationCaseResult> results = getSortedResults();
		grid.setItems(results);
		grid.asSingleSelect().clear();
		updateCompactMode(results.isEmpty());
		LOGGER.debug("Loaded {} validation case results for validation session", results.size());
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", getClass().getSimpleName(), getComponentName());
	}

	@Override
	public void removeRefreshListener(final Consumer<CValidationCaseResult> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CValidationSession) {
			masterEntity = (CValidationSession) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity is not a CValidationSession: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param validationSession the validation session that owns the results */
	public void setMasterEntity(final CValidationSession validationSession) {
		masterEntity = validationSession;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof CValidationSession) {
			masterEntity = (CValidationSession) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Show detailed dialog for a validation case result.
	 * @param result the validation case result to display */
	
	protected void showDetailsDialog(final CValidationCaseResult result) {
		try {
			Check.notNull(result, "Validation case result cannot be null");
			final Dialog dialog = new Dialog();
			// Responsive dialog pattern (AGENTS.md 6.2) - Large size for grid layout
			dialog.setWidth("900px");
			dialog.setHeight("700px");
			dialog.setModal(true);
			dialog.setDraggable(true);
			dialog.setResizable(true);
			// Main layout with responsive pattern
			final CVerticalLayout mainLayout = new CVerticalLayout();
			mainLayout.setPadding(true);
			mainLayout.setSpacing(false);
			mainLayout.getStyle().set("gap", "12px");
			mainLayout.setSizeFull();
			// Header
			final CH4 header = new CH4("Validation Case Result Details");
			header.getStyle().set("margin-top", "0");
			mainLayout.add(header);
			// Validation case info section
			final CVerticalLayout infoSection = new CVerticalLayout();
			infoSection.setPadding(true);
			infoSection.setSpacing(false);
			infoSection.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "4px");
			// Validation case name
			final Div nameDiv = new Div();
			nameDiv.add(createBoldSpan("Validation Case: "));
			if (result.getValidationCase() != null) {
				nameDiv.add(new Span(result.getValidationCase().getName()));
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
			// Expected result (from validation case)
			if (result.getValidationCase() != null && result.getValidationCase().getDescription() != null
					&& !result.getValidationCase().getDescription().isEmpty()) {
				final CVerticalLayout expectedSection = new CVerticalLayout();
				expectedSection.setPadding(true);
				expectedSection.setSpacing(false);
				expectedSection.getStyle().set("background-color", "#E8F5E9").set("border-radius", "4px");
				final Span expectedLabel = createBoldSpan("Validation Case Description:");
				expectedLabel.getStyle().set("margin-bottom", "4px").set("display", "block");
				expectedSection.add(expectedLabel);
				final CSpan expectedText = new CSpan(result.getValidationCase().getDescription());
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
			// Validation step results section
			final Set<CValidationStepResult> stepResults = result.getValidationStepResults();
			if (stepResults != null && !stepResults.isEmpty()) {
				final CH4 stepsHeader = new CH4("Validation Step Results");
				stepsHeader.getStyle().set("margin-top", "16px").set("margin-bottom", "8px");
				mainLayout.add(stepsHeader);
				final Grid<CValidationStepResult> stepGrid = createStepResultsGrid(result);
				mainLayout.add(stepGrid);
			}
			// Close button
			final CHorizontalLayout buttonLayout = new CHorizontalLayout();
			buttonLayout.setSpacing(true);
			final CButton closeButton = new CButton("Close", VaadinIcon.CLOSE.create());
			closeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			closeButton.addClickListener(event -> dialog.close());
			buttonLayout.add(closeButton);
			mainLayout.add(buttonLayout);
			dialog.add(mainLayout);
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error showing validation case result details", e);
			CNotificationService.showException("Error showing validation case result details", e);
		}
	}

	/** Update component height based on content.
	 * @param isEmpty true if no validation case results exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			grid.setHeight("200px");
			setHeight("250px");
			LOGGER.debug("Compact mode: No validation case results");
		} else {
			grid.setHeight("400px");
			setHeight("auto");
			LOGGER.debug("Normal mode: Has validation case results");
		}
	}
}
