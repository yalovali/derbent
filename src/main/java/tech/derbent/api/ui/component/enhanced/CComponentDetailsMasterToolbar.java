package tech.derbent.api.ui.component.enhanced;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.screens.view.CDialogFieldSelection;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

public class CComponentDetailsMasterToolbar extends HorizontalLayout {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentDetailsMasterToolbar.class);
	private static final long serialVersionUID = 1L;

	/** Extracts entity type from service bean name */
	private static String extractEntityTypeFromService(final String serviceBeanName) {
		Check.notNull(serviceBeanName, "Service bean name is null");
		Check.isTrue(serviceBeanName.endsWith("Service"), "Service bean name does not end with 'Service'");
		// Convert activityService -> CActivity or CActivityService -> CActivity
		final String baseName = serviceBeanName.substring(0, serviceBeanName.length() - "Service".length());
		// If it's already in the format "CActivity", return as is
		if (baseName.startsWith("C") && baseName.length() > 1 && Character.isUpperCase(baseName.charAt(1))) {
			return baseName;
		}
		// Convert from camelCase bean name to proper class name
		// activityService -> CActivity, meetingService -> CMeeting, etc.
		if (baseName.length() > 0) {
			return "C" + Character.toUpperCase(baseName.charAt(0)) + baseName.substring(1);
		}
		return null;
	}

	private CButton btnEditGrid;
	private final CComponentGridEntity grid;
	private final CGridEntityService gridEntityService;
	private TextField searchField;

	public CComponentDetailsMasterToolbar(final CComponentGridEntity grid, CGridEntityService gridEntityService) {
		try {
			this.grid = grid;
			this.gridEntityService = gridEntityService;
			setSpacing(true);
			setPadding(true);
			addClassName("crud-toolbar");
			setWidthFull(); // Make toolbar take full width
			createToolbarButtons();
		} catch (final Exception e) {
			LOGGER.error("Error initializing toolbar {}", e.getMessage());
			throw e;
		}
	}

	/** Creates all the CRUD toolbar buttons. */
	@SuppressWarnings ("unused")
	private void createToolbarButtons() {
		try {
			// Search field for grid filtering
			searchField = new TextField();
			searchField.setPlaceholder("Search...");
			searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
			searchField.setClearButtonVisible(true);
			searchField.setValueChangeMode(ValueChangeMode.LAZY);
			searchField.addValueChangeListener(e -> handleSearch(e.getValue()));
			// Edit Grid Columns Button
			btnEditGrid = CButton.createPrimary("Edit Columns", VaadinIcon.GRID_V.create(), event -> {
				try {
					handleEditGridEntity();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			});
			btnEditGrid.getElement().setAttribute("title", "Edit Grid Columns");
			add(searchField, btnEditGrid);
			updateButtonStates();
		} catch (final Exception e) {
			LOGGER.error("Error creating toolbar buttons {}", e.getMessage());
			throw e;
		}
	}

	/** Gets the current grid entity being displayed */
	private CGridEntity getCurrentGridEntity() {
		return grid != null ? grid.getGridEntity() : null;
	}

	private void handleEditGridEntity() throws Exception {
		try {
			Check.notNull(grid, "Grid component is not set");
			// Get current grid entity information
			final CGridEntity gridEntity = getCurrentGridEntity();
			Check.notNull(gridEntity, "No grid entity found for the current grid");
			final String entityType = extractEntityTypeFromService(gridEntity.getDataServiceBeanName());
			final List<String> currentColumnFields = gridEntity.getColumnFields();
			// Open field selection dialog
			final CDialogFieldSelection dialog = new CDialogFieldSelection(entityType, currentColumnFields, selectedFields -> {
				try {
					// Update grid entity with new field selection
					final List<String> newSelectionString = selectedFields.stream().map(fs -> fs.getFieldInfo().getFieldName()).toList();
					gridEntity.setColumnFields(newSelectionString);
					gridEntityService.save(gridEntity);
					// Refresh grid
					grid.createGridColumns();
					grid.refreshGrid();
					CNotificationService.showSuccess("Grid columns updated successfully");
				} catch (final Exception e) {
					CNotificationService.showException("Error saving grid columns", e);
				}
			});
			dialog.open();
		} catch (final Exception e) {
			LOGGER.error("Error opening grid editor", e);
			throw e;
		}
	}

	/** Handles search field value changes */
	private void handleSearch(final String searchValue) {
		Check.notNull(searchValue, "Search value is null");
		Check.notNull(grid, "Grid component is not set");
		// Apply search filter to grid
		grid.setSearchFilter(searchValue);
	}

	private void updateButtonStates() {
		// Enable edit button only if grid is available
		btnEditGrid.setEnabled(grid != null);
	}
}
