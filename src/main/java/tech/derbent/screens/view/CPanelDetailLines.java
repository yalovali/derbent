package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.grids.CGrid;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.domain.CDetailSection;
import tech.derbent.screens.service.CDetailLinesService;
import tech.derbent.screens.service.CDetailSectionService;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CViewsService;

public class CPanelDetailLines extends CPanelDetailSectionBase {

	private static final long serialVersionUID = 1L;
	private final CDetailLinesService detailLinesService;
	private CGrid<CDetailLines> grid;
	private CDetailLines selectedLine;

	public CPanelDetailLines(final CDetailSection currentEntity, final CEnhancedBinder<CDetailSection> beanValidationBinder,
			final CDetailSectionService entityService, final CDetailLinesService screenLinesService, final CEntityFieldService entityFieldService,
			final CViewsService viewsService) throws Exception {
		super("Screen Lines", currentEntity, beanValidationBinder, entityService);
		this.detailLinesService = screenLinesService;
		initPanel();
		createScreenLinesLayout();
	}

	private void createLinesGrid() {
		grid = new CGrid<CDetailLines>(CDetailLines.class);
		grid.setHeightFull();
		grid.addColumn(CDetailLines::getLineOrder).setHeader("Order").setWidth("80px");
		grid.addColumn(CDetailLines::getFieldCaption).setHeader("Caption").setAutoWidth(true);
		grid.addColumn(CDetailLines::getEntityProperty).setHeader("Field Name").setAutoWidth(true);
		grid.addColumn(line -> line.getIsRequired() ? "Yes" : "No").setHeader("Required").setWidth("80px");
		grid.addColumn(line -> line.getIsActive() ? "Active" : "Inactive").setHeader("Status").setWidth("80px");
		grid.setMinHeight("300px");
	}

	private HorizontalLayout createLinesToolbar() {
		final HorizontalLayout toolbar = new HorizontalLayout();
		toolbar.setSpacing(true);
		final Button addButton = new Button("Add Screen Field Description", VaadinIcon.PLUS.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(e -> {
			try {
				openAddFieldDialog();
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (final Exception e1) {
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
		final Button moveDownButton = new Button("Move Down", VaadinIcon.ARROW_DOWN.create());
		moveDownButton.addClickListener(e -> moveLineDown());
		moveDownButton.setEnabled(false);
		// Enable/disable buttons based on selection
		grid.asSingleSelect().addValueChangeListener(e -> {
			try {
				LOGGER.debug("Selected line: {}", e.getValue());
				final boolean hasSelection = e.getValue() != null;
				deleteButton.setEnabled(hasSelection);
				moveUpButton.setEnabled(hasSelection);
				moveDownButton.setEnabled(hasSelection);
				if (hasSelection) {
					selectedLine = e.getValue();
					// Open edit dialog on selection openEditFieldDialog(selectedLine);
				} else {
					selectedLine = null;
				}
			} catch (final Exception ex) {
				Notification.show("Error processing selection: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
			}
		});
		grid.addItemDoubleClickListener(e -> {
			try {
				LOGGER.debug("Double-clicked line: {}", e.getItem());
				// Enable buttons based on double-clicked item
				final boolean hasSelection = e.getItem() != null;
				deleteButton.setEnabled(hasSelection);
				moveUpButton.setEnabled(hasSelection);
				moveDownButton.setEnabled(hasSelection);
				if (hasSelection) {
					selectedLine = e.getItem();
					openEditFieldDialog(selectedLine);
				} else {
					selectedLine = null;
				}
			} catch (final Exception ex) {
				Notification.show("Error processing selection: " + ex.getMessage(), 5000, Notification.Position.MIDDLE);
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
				detailLinesService.delete(selectedLine);
				refreshLinesGrid();
				// Clear selection
				grid.asSingleSelect().clear();
				Notification.show("Line deleted successfully", 3000, Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error deleting line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
			}
		}
	}

	private void moveLineDown() {
		if (selectedLine != null) {
			try {
				detailLinesService.moveLineDown(selectedLine);
				refreshLinesGrid();
				Notification.show("Line moved down", 2000, Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error moving line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
			}
		}
	}

	private void moveLineUp() {
		if (selectedLine != null) {
			try {
				detailLinesService.moveLineUp(selectedLine);
				refreshLinesGrid();
				Notification.show("Line moved up", 2000, Notification.Position.BOTTOM_START);
			} catch (final Exception e) {
				Notification.show("Error moving line: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
			}
		}
	}

	/** Opens the add field dialog for creating a new screen field.
	 * @throws Exception */
	private void openAddFieldDialog() throws Exception {
		if ((getCurrentEntity() == null) || (getCurrentEntity().getId() == null)) {
			Notification.show("Please save the screen first before adding fields", 3000, Notification.Position.MIDDLE);
			return;
		}
		final CDetailLines newLine = detailLinesService.newEntity(getCurrentEntity(), CEntityFieldService.THIS_CLASS, "name");
		final CDetailLinesEditDialog dialog = new CDetailLinesEditDialog(newLine, this::saveScreenLine, true, currentEntity);
		dialog.open();
	}

	/** Opens the edit dialog for an existing screen field.
	 * @throws Exception */
	private void openEditFieldDialog(final CDetailLines detailLine) throws Exception {
		if (detailLine == null) {
			return;
		}
		final CDetailLinesEditDialog dialog = new CDetailLinesEditDialog(detailLine, this::saveScreenLine, false, currentEntity);
		dialog.open();
	}

	@Override
	public void populateForm(final CDetailSection entity) {
		super.populateForm(entity);
		refreshLinesGrid();
	}

	private void refreshLinesGrid() {
		final CDetailSection detailSection = getCurrentEntity();
		if (detailSection != null) {
			final List<CDetailLines> lines = detailLinesService.findByMaster(detailSection);
			grid.setItems(lines);
		} else {
			grid.setItems();
		}
	}

	/** Saves a screen line and refreshes the grid. */
	private void saveScreenLine(final CDetailLines detailLine) {
		try {
			detailLinesService.save(detailLine);
			refreshLinesGrid();
			// Clear selection to avoid confusion
			grid.asSingleSelect().clear();
		} catch (final Exception e) {
			Notification.show("Error saving field: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of());
	}
}
