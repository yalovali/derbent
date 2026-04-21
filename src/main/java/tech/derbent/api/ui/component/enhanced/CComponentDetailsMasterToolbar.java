package tech.derbent.api.ui.component.enhanced;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.pagequery.domain.CPageViewFilterVisibility;
import tech.derbent.api.pagequery.ui.IDetailsMasterToolbarExtensionFactory;
import tech.derbent.api.pagequery.ui.IDetailsMasterToolbarExtensionInstance;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.view.CComponentGridEntity;
import tech.derbent.api.screens.view.CDialogFieldSelection;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.ui.component.basic.CButton;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.ui.component.filter.CFilterToolbarSupport;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;

public class CComponentDetailsMasterToolbar extends HorizontalLayout {

	public static final String ID_ACTIONS_GROUP = "custom-master-toolbar-actions";
	public static final String ID_BUTTON_CLEAR_FILTERS = "custom-master-toolbar-clear-filters";
	public static final String ID_FIELD_SEARCH = "custom-master-toolbar-search";
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
	private CButton buttonClearFilters;
	private final List<IDetailsMasterToolbarExtensionInstance> filterExtensions = new ArrayList<>();
	private final CPageViewFilterVisibility filterVisibility;
	private final CComponentGridEntity grid;
	private final CGridEntityService gridEntityService;
	private CTextField searchField;
	private boolean searchInitialized = false;
	private final ISessionService sessionService;

	public CComponentDetailsMasterToolbar(final CComponentGridEntity grid, final CGridEntityService gridEntityService) {
		this(grid, gridEntityService, CPageViewFilterVisibility.autoFor(grid.getEntityClass()));
	}

	public CComponentDetailsMasterToolbar(final CComponentGridEntity grid, final CGridEntityService gridEntityService,
			final CPageViewFilterVisibility filterVisibility) {
		try {
			this.grid = grid;
			this.gridEntityService = gridEntityService;
			this.filterVisibility = filterVisibility;
			sessionService = CSpringContext.getBean(ISessionService.class);
			CFilterToolbarSupport.configureWrappingToolbar(this, "crud-toolbar");
			createToolbarButtons();
			// Mark search as initialized after everything is set up
			searchInitialized = true;
		} catch (final Exception e) {
			LOGGER.error("Error initializing toolbar {}", e.getMessage());
			throw e;
		}
	}

	private void addExtensionFilterComponents(final List<Component> components) {
		try {
			final var factories = CSpringContext.getBeansOfType(IDetailsMasterToolbarExtensionFactory.class);
			for (final IDetailsMasterToolbarExtensionFactory factory : factories.values()) {
				try {
					if (!factory.supports(grid.getEntityClass())) {
						continue;
					}
					final IDetailsMasterToolbarExtensionInstance instance = factory.create(grid, filterVisibility, sessionService);
					if (instance == null) {
						continue;
					}
					filterExtensions.add(instance);
					instance.addComponents(components);
				} catch (final Exception e) {
					LOGGER.error("Filter extension skipped: {}", e.getMessage());
				}
			}
		} catch (final Exception e) {
			LOGGER.error("No master toolbar filter extensions: {}", e.getMessage());
		}
	}

	private Component createLabeledGroup(final String caption, final Component content, final String id) {
		final Div wrapper = new Div();
		wrapper.setId(id);
		wrapper.getStyle().set("display", "flex");
		wrapper.getStyle().set("flex-direction", "column");
		wrapper.getStyle().set("min-width", "0");
		final Span label = new Span(caption);
		label.getStyle().set("font-size", "var(--lumo-font-size-xs)");
		label.getStyle().set("color", "var(--lumo-secondary-text-color)");
		label.getStyle().set("line-height", "1");
		label.getStyle().set("padding-left", "var(--lumo-space-xs)");
		wrapper.add(label, content);
		return wrapper;
	}

	/** Creates all the CRUD toolbar buttons. */
	private void createToolbarButtons() {
		try {
			final List<Component> components = new ArrayList<>();
			// Search field for grid filtering (caption required for consistent height vs. labeled filters)
			searchField = CFilterToolbarSupport.createSearchField("Search", "Search...", VaadinIcon.SEARCH, "220px", ValueChangeMode.EAGER, 300,
					this::handleSearch);
			searchField.setId(ID_FIELD_SEARCH);
			components.add(searchField);
			addExtensionFilterComponents(components);
			// Actions grouped under a caption to avoid "floating buttons" next to captioned ComboBoxes.
			buttonClearFilters = CButton.createTertiary("Clear", VaadinIcon.CLOSE_SMALL.create(), event -> on_clearFilters_clicked());
			buttonClearFilters.setId(ID_BUTTON_CLEAR_FILTERS);
			buttonClearFilters.addThemeVariants(ButtonVariant.LUMO_SMALL);
			btnEditGrid = CButton.createTertiary("Columns", VaadinIcon.GRID_V.create(), event -> {
				try {
					handleEditGridEntity();
				} catch (final Exception e) {
					e.printStackTrace();
				}
			});
			btnEditGrid.addThemeVariants(ButtonVariant.LUMO_SMALL);
			btnEditGrid.getElement().setAttribute("title", "Edit Grid Columns");
			final CHorizontalLayout actionsRow = new CHorizontalLayout(buttonClearFilters, btnEditGrid);
			actionsRow.setPadding(false);
			actionsRow.setSpacing(true);
			actionsRow.getStyle().set("gap", "var(--lumo-space-xs)");
			final Component actionsGroup = createLabeledGroup("Actions", actionsRow, ID_ACTIONS_GROUP);
			components.add(actionsGroup);
			add(components.toArray(new Component[0]));
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
			LOGGER.error("Error opening grid editor reason={}", e.getMessage());
			throw e;
		}
	}

	/** Handles search field value changes */
	private void handleSearch(final String searchValue) {
		// Ignore search events during initialization
		if (!searchInitialized) {
			LOGGER.debug("Ignoring search event during toolbar initialization");
			return;
		}
		Check.notNull(searchValue, "Search value is null");
		Check.notNull(grid, "Grid component is not set");
		// Apply search filter to grid
		grid.setSearchFilter(searchValue);
	}

	private void on_clearFilters_clicked() {
		try {
			if (searchField != null) {
				searchField.clear();
			}
			if (grid != null) {
				grid.clearPageViewFilters();
			}
			filterExtensions.forEach(IDetailsMasterToolbarExtensionInstance::clear);
			if (grid != null) {
				grid.refreshGrid();
			}
		} catch (final Exception e) {
			LOGGER.debug("Failed to clear master toolbar filters: {}", e.getMessage());
		}
	}

	private void updateButtonStates() {
		// Enable edit button only if grid is available
		btnEditGrid.setEnabled(grid != null);
	}
}
