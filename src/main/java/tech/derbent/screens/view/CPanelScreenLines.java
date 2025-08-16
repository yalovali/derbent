package tech.derbent.screens.view;

import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.screens.service.CScreenService;

public class CPanelScreenLines extends CPanelScreenBase {

	private static final long serialVersionUID = 1L;

	private final CScreenLinesService screenLinesService;

	private final CEntityFieldService entityFieldService;

	private Grid<CScreenLines> linesGrid;

	private CScreenLines selectedLine;

	public CPanelScreenLines(final CScreen currentEntity,
		final CEnhancedBinder<CScreen> beanValidationBinder,
		final CScreenService entityService, final CScreenLinesService screenLinesService,
		final CEntityFieldService entityFieldService) {
		super("Screen Lines", currentEntity, beanValidationBinder, entityService);
		this.screenLinesService = screenLinesService;
		this.entityFieldService = entityFieldService;
		initPanel();
		createScreenLinesLayout();
	}

	private void addNewLine() {

		if ((getCurrentEntity() == null) || (getCurrentEntity().getId() == null)) {
			Notification.show("Please save the screen first before adding lines", 3000,
				Notification.Position.MIDDLE);
			return;
		}
		final CScreenLines newLine =
			screenLinesService.newEntity(getCurrentEntity(), "New Field", "newField");
		selectedLine = newLine;
		// Removed inline form population - now using dialog
	}

	private VerticalLayout createInstructionsPanel() {
		final VerticalLayout panel = new VerticalLayout();
		panel.setPadding(true);
		panel.setSpacing(true);
		final H3 title = new H3("Instructions");
		panel.add(title);
		final Div instructions = new Div();
		instructions.getStyle().set("color", "var(--lumo-secondary-text-color)");
		instructions.getElement().setProperty("innerHTML",
			"• Click 'Add Screen Field Description' to create new fields<br/>"
				+ "• Click any field in the grid to edit it<br/>"
				+ "• Use Delete button to remove selected field<br/>"
				+ "• Use Move Up/Down to reorder fields");
		panel.add(instructions);
		return panel;
	}

	private void createLinesGrid() {
		linesGrid = new Grid<>(CScreenLines.class, false);
		linesGrid.setHeightFull();
		linesGrid.addColumn(CScreenLines::getLineOrder).setHeader("Order")
			.setWidth("80px");
		linesGrid.addColumn(CScreenLines::getFieldCaption).setHeader("Caption")
			.setAutoWidth(true);
		linesGrid.addColumn(CScreenLines::getEntityFieldName).setHeader("Field Name")
			.setAutoWidth(true);
		linesGrid.addColumn(CScreenLines::getFieldType).setHeader("Type")
			.setWidth("100px");
		linesGrid.addColumn(line -> line.getIsRequired() ? "Yes" : "No")
			.setHeader("Required").setWidth("80px");
		linesGrid.addColumn(line -> line.getIsActive() ? "Active" : "Inactive")
			.setHeader("Status").setWidth("80px");
	}

	private HorizontalLayout createLinesToolbar() {
		final HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		final Button addButton =
			new Button("Add Screen Field Description", VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> openAddFieldDialog());
		final Button deleteButton = new Button("Delete", VaadinIcon.TRASH.create());
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		deleteButton.addClickListener(e -> deleteSelectedLine());
		deleteButton.setEnabled(false);
		final Button moveUpButton = new Button("Move Up", VaadinIcon.ARROW_UP.create());
		moveUpButton.addClickListener(e -> moveLineUp());
		moveUpButton.setEnabled(false);
		final Button moveDownButton =
			new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		moveDownButton.addClickListener(e -> moveLineDown());
		moveDownButton.setEnabled(false);
		// Enable/disable buttons based on selection
		linesGrid.asSingleSelect().addValueChangeListener(e -> {
			final boolean hasSelection = e.getValue() != null;
			deleteButton.setEnabled(hasSelection);
			moveUpButton.setEnabled(hasSelection);
			moveDownButton.setEnabled(hasSelection);

			if (hasSelection) {
				selectedLine = e.getValue();
				// Open edit dialog on selection
				openEditFieldDialog(selectedLine);
			}
			else {
				selectedLine = null;
			}
		});
		toolbar.add(addButton, deleteButton, moveUpButton, moveDownButton);
		return toolbar;
	}

	private void createScreenLinesLayout() {
		final VerticalLayout layout = new VerticalLayout();
		layout.setPadding(false);
		layout.setSpacing(true);
		// Title
		final H3 title = new H3("Screen Field Definitions");
		layout.add(title);
		// Lines grid (create first before toolbar so it's available)
		createLinesGrid();
		// Toolbar
		final HorizontalLayout toolbar = createLinesToolbar();
		layout.add(toolbar);
		// Grid and instructions in horizontal layout
		final HorizontalLayout mainLayout = new HorizontalLayout();
		mainLayout.setSizeFull();
		mainLayout.setSpacing(true);
		// Lines grid (takes full width now)
		final Div gridWrapper = new Div(linesGrid);
		gridWrapper.setWidthFull();
		gridWrapper.setHeightFull();
		// Instructions panel (right side)
		final VerticalLayout instructionsLayout = createInstructionsPanel();
		instructionsLayout.setWidth("30%");
		mainLayout.add(gridWrapper, instructionsLayout);
		layout.add(mainLayout);
		// Set content for the accordion panel
		addToContent(layout);
		refreshLinesGrid();
	}

	private void deleteSelectedLine() {

		if ((selectedLine != null) && (selectedLine.getId() != null)) {

			try {
				screenLinesService.delete(selectedLine);
				refreshLinesGrid();
				// Clear selection
				linesGrid.asSingleSelect().clear();
				Notification.show("Line deleted successfully", 3000,
					Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error deleting line: " + e.getMessage(), 5000,
					Notification.Position.MIDDLE);
			}
		}
	}

	private void moveLineDown() {

		if (selectedLine != null) {

			try {
				screenLinesService.moveLineDown(selectedLine);
				refreshLinesGrid();
				Notification.show("Line moved down", 2000,
					Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error moving line: " + e.getMessage(), 5000,
					Notification.Position.MIDDLE);
			}
		}
	}

	private void moveLineUp() {

		if (selectedLine != null) {

			try {
				screenLinesService.moveLineUp(selectedLine);
				refreshLinesGrid();
				Notification.show("Line moved up", 2000,
					Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error moving line: " + e.getMessage(), 5000,
					Notification.Position.MIDDLE);
			}
		}
	}

	/**
	 * Opens the add field dialog for creating a new screen field.
	 */
	private void openAddFieldDialog() {

		if ((getCurrentEntity() == null) || (getCurrentEntity().getId() == null)) {
			Notification.show("Please save the screen first before adding fields", 3000,
				Notification.Position.MIDDLE);
			return;
		}
		final CScreenLines newLine =
			screenLinesService.newEntity(getCurrentEntity(), "New Field", "newField");
		final CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(newLine,
			this::saveScreenLine, true, entityFieldService);
		dialog.open();
	}

	/**
	 * Opens the edit dialog for an existing screen field.
	 */
	private void openEditFieldDialog(final CScreenLines screenLine) {

		if (screenLine == null) {
			return;
		}
		final CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(screenLine,
			this::saveScreenLine, false, entityFieldService);
		dialog.open();
	}

	private void refreshLinesGrid() {

		if (getCurrentEntity() != null) {
			final List<CScreenLines> lines =
				screenLinesService.findByScreenOrderByLineOrder(getCurrentEntity());
			linesGrid.setItems(lines);
		}
		else {
			linesGrid.setItems();
		}
	}

	/**
	 * Saves a screen line and refreshes the grid.
	 */
	private void saveScreenLine(final CScreenLines screenLine) {

		try {
			screenLinesService.save(screenLine);
			refreshLinesGrid();
			// Clear selection to avoid confusion
			linesGrid.asSingleSelect().clear();
		} catch (final Exception e) {
			Notification.show("Error saving field: " + e.getMessage(), 5000,
				Notification.Position.MIDDLE);
		}
	}

	@Override
	protected void updatePanelEntityFields() {
		// This panel doesn't use the standard entity fields approach as it manages
		// CScreenLines separately
		setEntityFields(List.of());
	}
}