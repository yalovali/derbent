package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CGrid;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CScreenLinesService;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.screens.service.CViewsService;

public class CPanelScreenLines extends CPanelScreenBase {

	private static final long serialVersionUID = 1L;

	private final CScreenLinesService screenLinesService;

	private final CEntityFieldService entityFieldService;

	private Grid<CScreenLines> grid;

	private CScreenLines selectedLine;

	public CPanelScreenLines(final CScreen currentEntity,
		final CEnhancedBinder<CScreen> beanValidationBinder,
		final CScreenService entityService, final CScreenLinesService screenLinesService,
		final CEntityFieldService entityFieldService, final CViewsService viewsService)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {
		super("Screen Lines", currentEntity, beanValidationBinder, entityService);
		this.screenLinesService = screenLinesService;
		this.entityFieldService = entityFieldService;
		initPanel();
		createScreenLinesLayout();
	}

	private void createLinesGrid() {
		grid = new CGrid<CScreenLines>(CScreenLines.class);
		grid.setHeightFull();
		grid.addColumn(CScreenLines::getLineOrder).setHeader("Order").setWidth("80px");
		grid.addColumn(CScreenLines::getFieldCaption).setHeader("Caption")
			.setAutoWidth(true);
		grid.addColumn(CScreenLines::getEntityProperty).setHeader("Field Name")
			.setAutoWidth(true);
		grid.addColumn(line -> line.getIsRequired() ? "Yes" : "No").setHeader("Required")
			.setWidth("80px");
		grid.addColumn(line -> line.getIsActive() ? "Active" : "Inactive")
			.setHeader("Status").setWidth("80px");
		grid.setMinHeight("300px");
	}

	private HorizontalLayout createLinesToolbar() {
		final HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		final Button addButton =
			new Button("Add Screen Field Description", VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> {

			try {
				openAddFieldDialog();
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
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
		grid.asSingleSelect().addValueChangeListener(e -> {

			try {
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
			} catch (final Exception ex) {
				Notification.show("Error processing selection: " + ex.getMessage(), 5000,
					Notification.Position.MIDDLE);
			}
		});
		toolbar.add(addButton, deleteButton, moveUpButton, moveDownButton);
		return toolbar;
	}

	private void createScreenLinesLayout() {
		final H3 title = new H3("Screen Field Definitions");
		createLinesGrid();
		final HorizontalLayout toolbar = createLinesToolbar();
		addToContent(title, toolbar, grid);
		refreshLinesGrid();
	}

	private void deleteSelectedLine() {

		if ((selectedLine != null) && (selectedLine.getId() != null)) {

			try {
				screenLinesService.delete(selectedLine);
				refreshLinesGrid();
				// Clear selection
				grid.asSingleSelect().clear();
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
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private void openAddFieldDialog() throws NoSuchMethodException, SecurityException,
		IllegalAccessException, InvocationTargetException {

		if ((getCurrentEntity() == null) || (getCurrentEntity().getId() == null)) {
			Notification.show("Please save the screen first before adding fields", 3000,
				Notification.Position.MIDDLE);
			return;
		}
		final CScreenLines newLine =
			screenLinesService.newEntity(getCurrentEntity(), "New Field", "newField");
		final CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(newLine,
			this::saveScreenLine, true, entityFieldService, currentEntity);
		dialog.open();
	}

	/**
	 * Opens the edit dialog for an existing screen field.
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 */
	private void openEditFieldDialog(final CScreenLines screenLine)
		throws NoSuchMethodException, SecurityException, IllegalAccessException,
		InvocationTargetException {

		if (screenLine == null) {
			return;
		}
		final CScreenLinesEditDialog dialog = new CScreenLinesEditDialog(screenLine,
			this::saveScreenLine, false, entityFieldService, currentEntity);
		dialog.open();
	}

	private void refreshLinesGrid() {

		if (getCurrentEntity() != null) {
			final List<CScreenLines> lines =
				screenLinesService.findByScreenOrderByLineOrder(getCurrentEntity());
			grid.setItems(lines);
		}
		else {
			grid.setItems();
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
			grid.asSingleSelect().clear();
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