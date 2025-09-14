package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tech.derbent.abstracts.views.components.CButton;
import tech.derbent.screens.view.CComponentGridEntity;
import tech.derbent.screens.view.CFieldSelectionDialog;
import tech.derbent.screens.domain.CGridEntity;

public class CComponentDetailsMasterToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDetailsMasterToolbar.class);
	private static final long serialVersionUID = 1L;
	private CButton btnEditGrid;
	private TextField searchField;
	private CComponentGridEntity grid;

	public CComponentDetailsMasterToolbar(CComponentGridEntity grid) {
		this.grid = grid;
		setSpacing(true);
		setPadding(true);
		addClassName("crud-toolbar");
		setWidthFull(); // Make toolbar take full width
		createToolbarButtons();
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		// Search field for grid filtering
		searchField = new TextField();
		searchField.setPlaceholder("Search...");
		searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
		searchField.setClearButtonVisible(true);
		searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.LAZY);
		searchField.addValueChangeListener(e -> handleSearch(e.getValue()));
		// Edit Grid Columns Button
		btnEditGrid = CButton.createPrimary("Edit Columns", VaadinIcon.GRID_V.create(), e -> handleEditGridEntity());
		btnEditGrid.getElement().setAttribute("title", "Edit Grid Columns");
		add(searchField, btnEditGrid);
		updateButtonStates();
	}

	private void updateButtonStates() {
		// Enable edit button only if grid is available
		btnEditGrid.setEnabled(grid != null);
	}

	/** Handles search field value changes */
	private void handleSearch(String searchValue) {
		if (grid != null) {
			// Apply search filter to grid
			grid.setSearchFilter(searchValue);
		}
	}

	private void handleEditGridEntity() {
		try {
			if (grid != null) {
				// Get current grid entity information
				CGridEntity gridEntity = getCurrentGridEntity();
				if (gridEntity != null) {
					String entityType = extractEntityTypeFromService(gridEntity.getDataServiceBeanName());
					String currentSelections = gridEntity.getSelectedFields();
					// Open field selection dialog
					CFieldSelectionDialog dialog = new CFieldSelectionDialog(entityType, currentSelections, selectedFields -> {
						// Update grid entity with new field selection
						String newSelectionString = selectedFields.stream().map(fs -> fs.getFieldInfo().getFieldName() + ":" + fs.getOrder())
								.reduce((a, b) -> a + "," + b).orElse("");
						gridEntity.setSelectedFields(newSelectionString);
						// Refresh grid
						grid.refreshGrid();
						Notification.show("Grid columns updated successfully");
					});
					dialog.open();
				} else {
					Notification.show("No grid entity found for editing", 3000, Notification.Position.MIDDLE);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error opening grid editor", e);
			Notification.show("Error opening grid editor: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
	}

	/** Gets the current grid entity being displayed */
	private CGridEntity getCurrentGridEntity() {
		// This would normally be provided by the parent context
		// For now, return null - this needs to be connected to the actual grid context
		LOGGER.warn("getCurrentGridEntity() not implemented - needs integration with parent view");
		return null;
	}

	/** Extracts entity type from service bean name */
	private String extractEntityTypeFromService(String serviceBeanName) {
		if (serviceBeanName != null && serviceBeanName.endsWith("Service")) {
			// Convert CActivityService -> CActivity
			return serviceBeanName.substring(0, serviceBeanName.length() - "Service".length());
		}
		return null;
	}

	/** Sets the grid entity for editing */
	public void setGridEntity(CGridEntity gridEntity) {
		// This method can be called by the parent to set the current grid entity
		// Implementation would depend on the parent view structure
	}
}
