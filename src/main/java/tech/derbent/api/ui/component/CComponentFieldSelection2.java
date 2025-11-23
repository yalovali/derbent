package tech.derbent.api.ui.component;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** CComponentFieldSelection2 - Component for selecting and ordering items using dual grid interface. Extends CComponentOrderedListBase to leverage
 * common ordered list functionality.
 * <p>
 * Features:
 * <ul>
 * <li>Dual grid interface (available items on left, selected items on right)</li>
 * <li>Color-aware rendering for CEntityNamed entities</li>
 * <li>Add/Remove buttons for moving items between grids</li>
 * <li>Up/Down buttons for reordering selected items</li>
 * <li>Double-click support for quick add/remove</li>
 * <li>Integration with CDataProviderResolver for dynamic data loading</li>
 * </ul>
 * @param <MasterEntity> The master entity type (for context)
 * @param <DetailEntity> The detail entity type being selected */
public class CComponentFieldSelection2<MasterEntity, DetailEntity> extends CComponentOrderedListBase<DetailEntity> {

	private static final String DEFAULT_GRID_HEIGHT = "250px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentFieldSelection2.class);
	private static final long serialVersionUID = 1L;
	// UI Components
	private CButton addButton;
	private Grid<DetailEntity> availableGrid;
	private CButton downButton;
	private CButton removeButton;
	private Grid<DetailEntity> selectedGrid;
	private CButton upButton;

	/** Default constructor for framework use. */
	public CComponentFieldSelection2() {
		this(null, null, null, "Available Items", "Selected Items");
	}

	/** Constructor with full configuration.
	 * @param dataProviderResolver Resolver for loading data dynamically
	 * @param contentOwner         Owner of this component
	 * @param fieldInfo            Field information for this component
	 * @param availableTitle       Title for available items grid
	 * @param selectedTitle        Title for selected items grid */
	public CComponentFieldSelection2(CDataProviderResolver dataProviderResolver, IContentOwner contentOwner, EntityFieldInfo fieldInfo,
			String availableTitle, String selectedTitle) {
		super();
		Check.notBlank(availableTitle, "Available title cannot be null or blank");
		Check.notBlank(selectedTitle, "Selected title cannot be null or blank");
		initializeUI(availableTitle, selectedTitle);
		setupEventHandlers();
		setupDoubleClickSupport();
	}

	/** Configures a grid column with color-aware rendering.
	 * @param grid   The Grid to configure
	 * @param header The column header text */
	private void configureGridColumn(Grid<DetailEntity> grid, String header) {
		Check.notNull(grid, "Grid cannot be null");
		grid.addComponentColumn(item -> {
			try {
				if (item == null) {
					LOGGER.warn("Rendering null item in grid - returning N/A placeholder");
					return new Span("N/A");
				}
				// Check if item is a CEntityNamed (has color and icon support)
				if (item instanceof CEntityNamed) {
					try {
						return new CEntityLabel((CEntityNamed<?>) item);
					} catch (Exception e) {
						LOGGER.error("Failed to create CEntityLabel for entity: {}", item, e);
						throw new IllegalStateException("Failed to render entity with color: " + item, e);
					}
				} else {
					// Fall back to text rendering for non-entity types
					String text = itemLabelGenerator != null ? itemLabelGenerator.apply(item) : item.toString();
					return new Span(text);
				}
			} catch (Exception e) {
				LOGGER.error("Error rendering item in field selection: {}", item, e);
				String fallbackText = item != null ? item.toString() : "Error";
				return new Span(fallbackText);
			}
		}).setHeader(CColorUtils.createStyledHeader(header, "#1565C0")).setAutoWidth(true).setFlexGrow(1);
	}

	/** Creates and configures a grid for field selection.
	 * @param header The header text for the grid column
	 * @return Configured Grid instance */
	private Grid<DetailEntity> createAndSetupGrid(String header) {
		Grid<DetailEntity> grid = new Grid<>();
		CGrid.setupGrid(grid);
		grid.setHeight(DEFAULT_GRID_HEIGHT);
		configureGridColumn(grid, header);
		return grid;
	}

	@Override
	protected void initializeUI() {
		// This is called by parent but we override with custom signature
		// The actual UI initialization happens in initializeUI(String, String)
	}

	/** Initialize the UI with custom titles.
	 * @param availableTitle Title for available items panel
	 * @param selectedTitle  Title for selected items panel */
	private void initializeUI(String availableTitle, String selectedTitle) {
		setSpacing(true);
		setWidthFull();
		// Left side: Available items
		CVerticalLayout leftLayout = new CVerticalLayout(false, false, false);
		leftLayout.setWidth("50%");
		availableGrid = createAndSetupGrid(availableTitle);
		leftLayout.add(availableGrid);
		// Add button
		addButton = new CButton("Add", VaadinIcon.ARROW_RIGHT.create());
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.setEnabled(false);
		addButton.setTooltipText("Add selected item to the list");
		CHorizontalLayout controlButtons = new CHorizontalLayout(addButton);
		controlButtons.setSpacing(true);
		leftLayout.add(controlButtons);
		// Right side: Selected items
		CVerticalLayout rightLayout = new CVerticalLayout(false, false, false);
		rightLayout.setWidth("50%");
		selectedGrid = createAndSetupGrid(selectedTitle);
		rightLayout.add(selectedGrid);
		// Order control buttons
		upButton = new CButton("Move Up", VaadinIcon.ARROW_UP.create());
		upButton.setEnabled(false);
		upButton.setTooltipText("Move selected item up in the order");
		downButton = new CButton("Move Down", VaadinIcon.ARROW_DOWN.create());
		downButton.setEnabled(false);
		downButton.setTooltipText("Move selected item down in the order");
		removeButton = new CButton("Remove", VaadinIcon.ARROW_LEFT.create());
		removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
		removeButton.setEnabled(false);
		removeButton.setTooltipText("Remove selected item from the list");
		CHorizontalLayout orderButtons = new CHorizontalLayout(removeButton, upButton, downButton);
		orderButtons.setSpacing(true);
		rightLayout.add(orderButtons);
		// Add layouts to main component
		this.add(leftLayout, rightLayout);
	}

	@Override
	protected void refreshDisplay() {
		try {
			LOGGER.debug("Refreshing grid displays - {} selected, {} available", selectedItems.size(), availableItems.size());
			// Update available grid
			availableGrid.setItems(availableItems);
			// Update selected grid
			selectedGrid.setItems(selectedItems);
			// Update button states
			updateButtonStates();
		} catch (Exception e) {
			LOGGER.error("Error refreshing display", e);
		}
	}

	@Override
	public void setItemLabelGenerator(ItemLabelGenerator<DetailEntity> generator) {
		super.setItemLabelGenerator(generator);
		// Refresh grid columns to use new label generator
		if (availableGrid != null && selectedGrid != null) {
			refreshDisplay();
		}
	}

	@Override
	public void setReadOnly(boolean readOnly) {
		super.setReadOnly(readOnly);
		updateButtonStates();
	}

	/** Set the source items that can be selected from.
	 * @param items List of available items */
	public void setSourceItems(List<DetailEntity> items) {
		setItems(items != null ? items : new ArrayList<>());
	}

	/** Setup double-click support for quick add/remove. */
	private void setupDoubleClickSupport() {
		// Double-click on available grid to add
		availableGrid.addItemDoubleClickListener(event -> {
			DetailEntity item = event.getItem();
			if (item != null) {
				addSelectedItem(item, -1);
				selectedGrid.asSingleSelect().setValue(item);
			}
		});
		// Double-click on selected grid to remove
		selectedGrid.addItemDoubleClickListener(event -> {
			DetailEntity item = event.getItem();
			if (item != null) {
				removeSelectedItem(item);
			}
		});
	}

	/** Setup event handlers for buttons and grid selections. */
	private void setupEventHandlers() {
		// Available grid selection changes
		availableGrid.asSingleSelect().addValueChangeListener(event -> {
			updateButtonStates();
		});
		// Selected grid selection changes
		selectedGrid.asSingleSelect().addValueChangeListener(event -> {
			updateButtonStates();
		});
		// Add button click
		addButton.addClickListener(event -> {
			DetailEntity selected = availableGrid.asSingleSelect().getValue();
			if (selected != null) {
				DetailEntity currentSelection = selectedGrid.asSingleSelect().getValue();
				int index = currentSelection != null ? selectedItems.indexOf(currentSelection) + 1 : -1;
				if (addSelectedItem(selected, index)) {
					selectedGrid.asSingleSelect().setValue(selected);
				}
			}
		});
		// Remove button click
		removeButton.addClickListener(event -> {
			DetailEntity selected = selectedGrid.asSingleSelect().getValue();
			if (selected != null) {
				removeSelectedItem(selected);
			}
		});
		// Up button click
		upButton.addClickListener(event -> {
			DetailEntity selected = selectedGrid.asSingleSelect().getValue();
			if (selected != null && moveItemUp(selected)) {
				selectedGrid.asSingleSelect().setValue(selected);
			}
		});
		// Down button click
		downButton.addClickListener(event -> {
			DetailEntity selected = selectedGrid.asSingleSelect().getValue();
			if (selected != null && moveItemDown(selected)) {
				selectedGrid.asSingleSelect().setValue(selected);
			}
		});
	}

	/** Update button enabled states based on current selections. */
	private void updateButtonStates() {
		if (readOnly) {
			addButton.setEnabled(false);
			removeButton.setEnabled(false);
			upButton.setEnabled(false);
			downButton.setEnabled(false);
			return;
		}
		DetailEntity availableSelected = availableGrid.asSingleSelect().getValue();
		DetailEntity selectedItem = selectedGrid.asSingleSelect().getValue();
		addButton.setEnabled(availableSelected != null);
		removeButton.setEnabled(selectedItem != null);
		if (selectedItem != null) {
			int index = selectedItems.indexOf(selectedItem);
			upButton.setEnabled(index > 0);
			downButton.setEnabled(index < selectedItems.size() - 1);
		} else {
			upButton.setEnabled(false);
			downButton.setEnabled(false);
		}
	}
}
