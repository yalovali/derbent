package tech.derbent.app.validation.validationstep.view;
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
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.app.validation.validationcase.domain.CValidationCase;
import tech.derbent.app.validation.validationstep.domain.CValidationStep;
import tech.derbent.app.validation.validationstep.service.CValidationStepService;
import tech.derbent.base.session.service.ISessionService;
import com.vaadin.flow.component.html.Div;

/** CComponentListValidationSteps - Component for managing validation steps in validation cases.
 * <p>
 * Displays ordered validation steps with action, expected result, and validation data. Supports CRUD operations and reordering
 * (move up/down) with automatic step numbering.
 * <p>
 * Usage:
 *
 * <pre>
 * CComponentListValidationSteps component = new CComponentListValidationSteps(service, sessionService);
 * component.setMasterEntity(validationCase);
 * </pre>
 */
public class CComponentListValidationSteps extends CVerticalLayout
		implements IContentOwner, IGridComponent<CValidationStep>, IGridRefreshListener<CValidationStep>, IPageServiceAutoRegistrable {

	public static final String ID_GRID = "custom-teststeps-grid";
	public static final String ID_HEADER = "custom-teststeps-header";
	public static final String ID_ROOT = "custom-teststeps-component";
	public static final String ID_TOOLBAR = "custom-teststeps-toolbar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentListValidationSteps.class);
	private static final long serialVersionUID = 1L;
	private final CValidationStepService validationStepService;
	private CButton buttonAdd;
	private CButton buttonDelete;
	private CButton buttonEdit;
	private CButton buttonMoveDown;
	private CButton buttonMoveUp;
	private CGrid<CValidationStep> grid;
	private CHorizontalLayout layoutToolbar;
	private CValidationCase masterEntity;
	private final List<Consumer<CValidationStep>> refreshListeners = new ArrayList<>();
	private final ISessionService sessionService;

	/** Constructor for validation step list component.
	 * @param validationStepService the validation step service
	 * @param sessionService  the session service */
	public CComponentListValidationSteps(final CValidationStepService validationStepService, final ISessionService sessionService) {
		Check.notNull(validationStepService, "ValidationStepService cannot be null");
		Check.notNull(sessionService, "SessionService cannot be null");
		this.validationStepService = validationStepService;
		this.sessionService = sessionService;
		initializeComponent();
	}

	@Override
	public void addRefreshListener(final Consumer<CValidationStep> listener) {
		Check.notNull(listener, "Refresh listener cannot be null");
		refreshListeners.add(listener);
	}

	@Override
	public void clearGrid() {
		Check.notNull(grid, "Grid cannot be null when clearing validation steps");
		grid.setItems(List.of());
		grid.asSingleSelect().clear();
		updateButtonStates(null);
		updateCompactMode(true);
	}

	/** Configure grid columns. */
	@Override
	public void configureGrid(final CGrid<CValidationStep> grid1) {
		try {
			Check.notNull(grid1, "Grid cannot be null");
			// Step order column
			grid1.addCustomColumn(CValidationStep::getStepOrder, "Step", "80px", "stepOrder", 0);
			// Action column (expanding short text)
			grid1.addExpandingShortTextColumn(CValidationStep::getAction, "Action", "action");
			// Expected result column (truncated)
			grid1.addCustomColumn(step -> {
				if (step.getExpectedResult() == null || step.getExpectedResult().isEmpty()) {
					return "";
				}
				final String result = step.getExpectedResult();
				return result.length() > 50 ? result.substring(0, 47) + "..." : result;
			}, "Expected Result", "200px", "expectedResult", 0);
			// Action buttons column
			grid1.addColumn(new ComponentRenderer<>(step -> {
				final CHorizontalLayout buttons = new CHorizontalLayout();
				buttons.setSpacing(true);
				buttons.setPadding(false);
				// Edit button
				final CButton btnEdit = new CButton(VaadinIcon.EDIT.create());
				btnEdit.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
				btnEdit.setTooltipText("Edit step");
				btnEdit.addClickListener(e -> {
					grid1.select(step);
					on_buttonEdit_clicked();
				});
				buttons.add(btnEdit);
				// Delete button
				final CButton btnDelete = new CButton(VaadinIcon.TRASH.create());
				btnDelete.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
				btnDelete.setTooltipText("Delete step");
				btnDelete.addClickListener(e -> {
					grid1.select(step);
					on_buttonDelete_clicked();
				});
				buttons.add(btnDelete);
				return buttons;
			})).setHeader("Actions").setWidth("120px").setFlexGrow(0);
			// Enable double-click to expand details
			grid1.setItemDetailsRenderer(new ComponentRenderer<>(step -> {
				final CVerticalLayout detailsLayout = new CVerticalLayout();
				detailsLayout.setPadding(true);
				detailsLayout.setSpacing(true);
				detailsLayout.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-left",
						"3px solid var(--lumo-primary-color)");
				// Action
				if (step.getAction() != null && !step.getAction().isEmpty()) {
					final Div actionDiv = new Div();
					actionDiv.add(createBoldSpan("Action: "));
					actionDiv.add(new Span(step.getAction()));
					actionDiv.getStyle().set("margin-bottom", "0.5rem");
					detailsLayout.add(actionDiv);
				}
				// Expected result
				if (step.getExpectedResult() != null && !step.getExpectedResult().isEmpty()) {
					final Div resultDiv = new Div();
					resultDiv.add(createBoldSpan("Expected Result: "));
					resultDiv.add(new Span(step.getExpectedResult()));
					resultDiv.getStyle().set("margin-bottom", "0.5rem");
					detailsLayout.add(resultDiv);
				}
				// Validation data
				if (step.getTestData() != null && !step.getTestData().isEmpty()) {
					final Div dataDiv = new Div();
					dataDiv.add(createBoldSpan("Validation Data: "));
					dataDiv.add(new Span(step.getTestData()));
					dataDiv.getStyle().set("margin-bottom", "0.5rem");
					detailsLayout.add(dataDiv);
				}
				// Notes
				if (step.getNotes() != null && !step.getNotes().isEmpty()) {
					final Div notesDiv = new Div();
					notesDiv.add(createBoldSpan("Notes: "));
					notesDiv.add(new Span(step.getNotes()));
					detailsLayout.add(notesDiv);
				}
				return detailsLayout;
			}));
			// Enable click to expand/collapse
			grid1.addItemClickListener(event -> {
				final CValidationStep step = event.getItem();
				if (grid1.isDetailsVisible(step)) {
					grid1.setDetailsVisible(step, false);
				} else {
					grid1.setDetailsVisible(step, true);
				}
			});
		} catch (final Exception e) {
			LOGGER.error("Error configuring validation steps grid", e);
			CNotificationService.showException("Error configuring validation steps grid", e);
		}
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		throw new UnsupportedOperationException("Validation steps are managed via dialog.");
	}

	/** Create toolbar buttons. */
	private void createToolbarButtons() {
		// Add button
		buttonAdd = new CButton(VaadinIcon.PLUS.create());
		buttonAdd.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonAdd.setTooltipText("Add validation step");
		buttonAdd.addClickListener(e -> on_buttonAdd_clicked());
		layoutToolbar.add(buttonAdd);
		// Edit button
		buttonEdit = new CButton(VaadinIcon.EDIT.create());
		buttonEdit.setTooltipText("Edit validation step");
		buttonEdit.addClickListener(e -> on_buttonEdit_clicked());
		buttonEdit.setEnabled(false);
		layoutToolbar.add(buttonEdit);
		// Delete button
		buttonDelete = new CButton(VaadinIcon.TRASH.create());
		buttonDelete.addThemeVariants(ButtonVariant.LUMO_ERROR);
		buttonDelete.setTooltipText("Delete validation step");
		buttonDelete.addClickListener(e -> on_buttonDelete_clicked());
		buttonDelete.setEnabled(false);
		layoutToolbar.add(buttonDelete);
		// Move up button
		buttonMoveUp = new CButton(VaadinIcon.ARROW_UP.create());
		buttonMoveUp.setTooltipText("Move step up");
		buttonMoveUp.addClickListener(e -> on_buttonMoveUp_clicked());
		buttonMoveUp.setEnabled(false);
		layoutToolbar.add(buttonMoveUp);
		// Move down button
		buttonMoveDown = new CButton(VaadinIcon.ARROW_DOWN.create());
		buttonMoveDown.setTooltipText("Move step down");
		buttonMoveDown.addClickListener(e -> on_buttonMoveDown_clicked());
		buttonMoveDown.setEnabled(false);
		layoutToolbar.add(buttonMoveDown);
	}

	@Override
	public String getComponentName() { return "validationSteps"; }

	@Override
	public String getCurrentEntityIdString() {
		if (masterEntity != null) {
			return masterEntity.getId() != null ? masterEntity.getId().toString() : null;
		}
		return null;
	}

	@Override
	public CAbstractService<?> getEntityService() { return validationStepService; }

	@Override
	public CGrid<CValidationStep> getGrid() { return grid; }

	@Override
	public CEntityDB<?> getValue() {
		return masterEntity;
	}

	/** Initialize the component layout and grid. */
	private void initializeComponent() {
		setId(ID_ROOT);
		setPadding(false);
		setSpacing(true);
		// Header
		final CH3 header = new CH3("Validation Steps");
		header.setId(ID_HEADER);
		add(header);
		// Toolbar
		layoutToolbar = new CHorizontalLayout();
		layoutToolbar.setId(ID_TOOLBAR);
		layoutToolbar.setSpacing(true);
		createToolbarButtons();
		add(layoutToolbar);
		// Grid
		grid = new CGrid<>(CValidationStep.class);
		grid.setId(ID_GRID);
		CGrid.setupGrid(grid);
		grid.setRefreshConsumer(e -> refreshGrid());
		configureGrid(grid);
		grid.setHeight("400px"); // Default height
		grid.asSingleSelect().addValueChangeListener(e -> on_grid_selectionChanged(e.getValue()));
		// Add double-click to edit
		grid.addItemDoubleClickListener(e -> on_grid_doubleClicked(e.getItem()));
		add(grid);
		// Set initial compact mode
		updateCompactMode(true);
	}

	private void linkStepToMaster(final CValidationStep step) {
		Check.notNull(step, "Validation step cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		step.setValidationCase(masterEntity);
		Set<CValidationStep> steps = masterEntity.getValidationSteps();
		if (steps == null) {
			steps = new java.util.HashSet<>();
			masterEntity.setValidationSteps(steps);
		}
		// Check if step already exists
		final Long stepId = step.getId();
		final boolean exists = steps.stream().anyMatch(existing -> {
			if (stepId != null && existing != null && existing.getId() != null) {
				return stepId.equals(existing.getId());
			}
			return existing == step;
		});
		if (!exists) {
			steps.add(step);
		}
		if (masterEntity.getId() != null) {
			saveMasterEntity(masterEntity);
		} else {
			LOGGER.warn("Master entity has no ID; step will persist when the parent entity is saved");
		}
	}

	@Override
	public void notifyRefreshListeners(final CValidationStep changedItem) {
		if (!refreshListeners.isEmpty()) {
			for (final Consumer<CValidationStep> listener : refreshListeners) {
				try {
					listener.accept(changedItem);
				} catch (final Exception e) {
					LOGGER.error("Error notifying refresh listener", e);
				}
			}
		}
	}

	/** Handle add button click. */
	protected void on_buttonAdd_clicked() {
		try {
			if (masterEntity == null) {
				CNotificationService.showWarning("Please select a validation case first");
				return;
			}
			// Calculate next step order
			final List<CValidationStep> steps = new ArrayList<>(masterEntity.getValidationSteps());
			final int nextOrder = steps.isEmpty() ? 1 : steps.stream().mapToInt(CValidationStep::getStepOrder).max().orElse(0) + 1;
			// Create new step
			final CValidationStep newStep = new CValidationStep(masterEntity, nextOrder);
			final CDialogValidationStep dialog = new CDialogValidationStep(validationStepService, sessionService, newStep, step -> {
				try {
					linkStepToMaster(step);
					refreshGrid();
					notifyRefreshListeners(step);
				} catch (final Exception e) {
					LOGGER.error("Error refreshing grid after adding validation step", e);
				}
			}, true);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening validation step dialog", e);
		}
	}

	/** Handle delete button click. */
	protected void on_buttonDelete_clicked() {
		try {
			final CValidationStep selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation step selected");
			CNotificationService.showConfirmationDialog("Delete this validation step?", () -> {
				try {
					unlinkStepFromMaster(selected);
					validationStepService.delete(selected);
					renumberSteps();
					refreshGrid();
					notifyRefreshListeners(selected);
					CNotificationService.showDeleteSuccess();
				} catch (final Exception e) {
					CNotificationService.showException("Error deleting validation step", e);
				}
			});
		} catch (final Exception e) {
			CNotificationService.showException("Failed to delete validation step", e);
		}
	}

	/** Handle edit button click. */
	protected void on_buttonEdit_clicked() {
		try {
			final CValidationStep selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation step selected");
			final CDialogValidationStep dialog = new CDialogValidationStep(validationStepService, sessionService, selected, step -> {
				try {
					validationStepService.save(step);
					refreshGrid();
					notifyRefreshListeners(step);
				} catch (final Exception e) {
					LOGGER.error("Error saving validation step", e);
					CNotificationService.showException("Error saving validation step", e);
				}
			}, false);
			dialog.open();
		} catch (final Exception e) {
			CNotificationService.showException("Error opening edit dialog", e);
		}
	}

	/** Handle move down button click. */
	protected void on_buttonMoveDown_clicked() {
		try {
			final CValidationStep selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation step selected");
			final List<CValidationStep> steps = getSortedSteps();
			final int currentIndex = steps.indexOf(selected);
			if (currentIndex < steps.size() - 1) {
				// Swap with next step
				final CValidationStep nextStep = steps.get(currentIndex + 1);
				final int tempOrder = selected.getStepOrder();
				selected.setStepOrder(nextStep.getStepOrder());
				nextStep.setStepOrder(tempOrder);
				validationStepService.save(selected);
				validationStepService.save(nextStep);
				refreshGrid();
				grid.select(selected);
				notifyRefreshListeners(selected);
			}
		} catch (final Exception e) {
			CNotificationService.showException("Error moving validation step", e);
		}
	}

	/** Handle move up button click. */
	protected void on_buttonMoveUp_clicked() {
		try {
			final CValidationStep selected = grid.asSingleSelect().getValue();
			Check.notNull(selected, "No validation step selected");
			final List<CValidationStep> steps = getSortedSteps();
			final int currentIndex = steps.indexOf(selected);
			if (currentIndex > 0) {
				// Swap with previous step
				final CValidationStep prevStep = steps.get(currentIndex - 1);
				final int tempOrder = selected.getStepOrder();
				selected.setStepOrder(prevStep.getStepOrder());
				prevStep.setStepOrder(tempOrder);
				validationStepService.save(selected);
				validationStepService.save(prevStep);
				refreshGrid();
				grid.select(selected);
				notifyRefreshListeners(selected);
			}
		} catch (final Exception e) {
			CNotificationService.showException("Error moving validation step", e);
		}
	}

	/** Handle grid double-click to edit. */
	protected void on_grid_doubleClicked(final CValidationStep step) {
		if (step != null) {
			on_buttonEdit_clicked();
		}
	}

	/** Handle grid selection changes. */
	private void on_grid_selectionChanged(final CValidationStep selected) {
		updateButtonStates(selected);
	}

	@Override
	public void populateForm() {
		refreshGrid();
	}

	@Override
	public void refreshGrid() {
		Check.notNull(grid, "Grid cannot be null when refreshing validation steps");
		if (masterEntity == null) {
			LOGGER.debug("Master entity is null, clearing grid");
			clearGrid();
			return;
		}
		// Load steps from parent entity's collection
		final List<CValidationStep> steps = getSortedSteps();
		grid.setItems(steps);
		grid.asSingleSelect().clear();
		updateCompactMode(steps.isEmpty());
		LOGGER.debug("Loaded {} validation steps for validation case", steps.size());
	}

	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		pageService.registerComponent(getComponentName(), this);
		LOGGER.debug("[BindDebug] {} auto-registered with page service as '{}'", getClass().getSimpleName(), getComponentName());
	}

	@Override
	public void removeRefreshListener(final Consumer<CValidationStep> listener) {
		if (listener != null) {
			refreshListeners.remove(listener);
		}
	}

	/** Renumber all steps after deletion. */
	private void renumberSteps() {
		if (masterEntity == null) {
			return;
		}
		final List<CValidationStep> steps = getSortedSteps();
		for (int i = 0; i < steps.size(); i++) {
			steps.get(i).setStepOrder(i + 1);
			validationStepService.save(steps.get(i));
		}
		saveMasterEntity(masterEntity);
	}

	private void saveMasterEntity(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity cannot be null");
		try {
			saveMasterEntityTyped(entity);
		} catch (final Exception e) {
			LOGGER.error("Failed to save master entity after validation step update", e);
			CNotificationService.showException("Failed to save validation step to parent entity", e);
		}
	}

	@SuppressWarnings ("unchecked")
	private <T extends CEntityDB<T>> void saveMasterEntityTyped(final CEntityDB<?> entity) {
		final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entity.getClass());
		final CAbstractService<T> service = (CAbstractService<T>) CSpringContext.getBean(serviceClass);
		service.save((T) entity);
	}

	public void setEntity(final Object entity) {
		if (entity == null) {
			setValue(null);
			return;
		}
		if (entity instanceof CValidationCase) {
			masterEntity = (CValidationCase) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("Entity is not a CValidationCase: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	/** Set the master entity for this component.
	 * @param validationCase the validation case that owns the steps */
	public void setMasterEntity(final CValidationCase validationCase) {
		this.masterEntity = validationCase;
		refreshGrid();
	}

	@Override
	public void setValue(final CEntityDB<?> entity) {
		if (entity == null) {
			masterEntity = null;
			clearGrid();
			return;
		}
		if (entity instanceof CValidationCase) {
			masterEntity = (CValidationCase) entity;
			refreshGrid();
			return;
		}
		LOGGER.warn("setValue called with unexpected entity type: {}", entity.getClass().getSimpleName());
		masterEntity = null;
		clearGrid();
	}

	private void unlinkStepFromMaster(final CValidationStep step) {
		Check.notNull(step, "Validation step cannot be null");
		Check.notNull(masterEntity, "Master entity cannot be null");
		Check.notNull(masterEntity.getId(), "Master entity must be saved before deleting validation steps");
		final Set<CValidationStep> steps = masterEntity.getValidationSteps();
		Check.notNull(steps, "Validation steps list cannot be null");
		final Long stepId = step.getId();
		final boolean removed = steps.removeIf(existing -> {
			if (stepId != null && existing != null) {
				return stepId.equals(existing.getId());
			}
			return existing == step;
		});
		Check.isTrue(removed, "Validation step not found in master entity");
		saveMasterEntity(masterEntity);
	}

	/** Get sorted list of steps by stepOrder. */
	private List<CValidationStep> getSortedSteps() {
		final List<CValidationStep> steps = new ArrayList<>(masterEntity.getValidationSteps());
		steps.sort(Comparator.comparingInt(CValidationStep::getStepOrder));
		return steps;
	}

	/** Update button states based on selection and position.
	 * @param selected the selected validation step */
	private void updateButtonStates(final CValidationStep selected) {
		final boolean hasSelection = selected != null;
		buttonEdit.setEnabled(hasSelection);
		buttonDelete.setEnabled(hasSelection);
		if (hasSelection) {
			final List<CValidationStep> steps = getSortedSteps();
			final int index = steps.indexOf(selected);
			buttonMoveUp.setEnabled(index > 0);
			buttonMoveDown.setEnabled(index < steps.size() - 1);
		} else {
			buttonMoveUp.setEnabled(false);
			buttonMoveDown.setEnabled(false);
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
	 * @param isEmpty true if no validation steps exist */
	private void updateCompactMode(final boolean isEmpty) {
		if (isEmpty) {
			grid.setHeight("200px");
			setHeight("250px");
			LOGGER.debug("Compact mode: No validation steps");
		} else {
			grid.setHeight("400px");
			setHeight("auto");
			LOGGER.debug("Normal mode: Has validation steps");
		}
	}
}
