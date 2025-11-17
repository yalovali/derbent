package tech.derbent.api.screens.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CGridCell;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.domain.CGridEntity.FieldConfig;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.CDiv;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public class CComponentGridEntity extends CDiv implements IProjectChangeListener {

	// --- Custom Event Definition ---
	public static class SelectionChangeEvent extends ComponentEvent<CComponentGridEntity> {

		private static final long serialVersionUID = 1L;
		private final CEntityDB<?> selectedItem;

		public SelectionChangeEvent(CComponentGridEntity source, CEntityDB<?> selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public CEntityDB<?> getSelectedItem() { return selectedItem; }
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentGridEntity.class);
	private static final long serialVersionUID = 1L;
	protected CProject currentProject;
	private boolean enableSelectionChangeListener;
	private CGrid<?> grid;
	private CGridEntity gridEntity;
	private ISessionService sessionService;

	public CComponentGridEntity(CGridEntity gridEntity, ISessionService sessionService) {
		super();
		try {
			this.sessionService = sessionService;
			this.gridEntity = gridEntity;
			Check.notNull(sessionService, "SessionService is required for CComponentGridEntity");
			Check.notNull(gridEntity, "GridEntity configuration is required for CComponentGridEntity");
			enableSelectionChangeListener = true;
			setSizeFull();
			createContent();
		} catch (Exception e) {
			LOGGER.error("Error initializing CComponentGridEntity: {}", e.getMessage());
			throw e;
		}
	}

	/** Adds a selection change listener to receive notifications when the grid selection changes */
	public Registration addSelectionChangeListener(com.vaadin.flow.component.ComponentEventListener<SelectionChangeEvent> listener) {
		return addListener(SelectionChangeEvent.class, listener);
	}

	/** Applies search filter to the grid data */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void applySearchFilter(String searchText) {
		try {
			String serviceBeanName = gridEntity.getDataServiceBeanName();
			Check.notBlank(serviceBeanName, "Service bean name is blank for search filtering");
			// Get the service and entity class
			CEntityOfProjectService<?> projectService = CSpringContext.<CEntityOfProjectService<?>>getBean(serviceBeanName);
			CProject currentProject = sessionService != null
					? sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found.")) : null;
			// Get all entities for the current project - note: using raw types due to grid constraints
			List allEntities = projectService.listByProject(currentProject, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
			// Filter entities based on search text
			List filteredEntities = (List) allEntities.stream().filter(entity -> {
				try {
					return matchesSearchText(entity, searchText);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return false;
				}
			}).collect(Collectors.toList());
			// Update grid with filtered data
			grid.setItems(filteredEntities);
			LOGGER.debug("Applied search filter '{}' - {} results out of {} total", searchText, filteredEntities.size(), allEntities.size());
		} catch (Exception e) {
			LOGGER.error("Error applying search filter.");
			// Fallback to refresh data on error
			refreshGridData();
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void createColumnForField(FieldConfig fieldConfig) {
		Field field = fieldConfig.getField();
		EntityFieldInfo fieldInfo = fieldConfig.getFieldInfo();
		String fieldName = field.getName();
		String displayName = fieldInfo.getDisplayName();
		Class<?> fieldType = field.getType();
		try {
			// Handle different field types using appropriate CGrid methods
			if (CEntityDB.class.isAssignableFrom(fieldType)) {
				// Entity reference - check if it's a status entity or has setBackgroundFromColor
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing entity field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				// Check if this field should use color rendering
				// Either it's a status entity OR it has setBackgroundFromColor = true
				if (CColorUtils.isStatusEntity(fieldType) || fieldInfo.isSetBackgroundFromColor()) {
					// Use addStatusColumn for color-enabled entity fields
					grid.addStatusColumn(valueProvider, displayName, fieldName);
				} else {
					// Use addEntityColumn for regular entity references
					grid.addEntityColumn(valueProvider, displayName, fieldName);
				}
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				// Collection field - use addColumnEntityCollection if it contains entities
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						Object value = field.get(entity);
						return value instanceof Collection ? (Collection) value : Collections.emptyList();
					} catch (Exception e) {
						LOGGER.error("Error accessing collection field {}: {}", fieldName, e.getMessage());
						return Collections.emptyList();
					}
				};
				grid.addColumnEntityCollection(valueProvider, displayName);
			} else if (fieldName.toLowerCase().contains("id")
					&& (fieldType == Long.class || fieldType == long.class || fieldType == Integer.class || fieldType == int.class)) {
				// ID fields - use addIdColumn for consistent ID formatting
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing ID field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addIdColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Integer.class || fieldType == int.class) {
				// Integer fields - use addIntegerColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Integer) field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing integer field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addIntegerColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == BigDecimal.class) {
				// BigDecimal fields - use addDecimalColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (BigDecimal) field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing decimal field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDecimalColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDate.class) {
				// LocalDate fields - use addDateColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDate) field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing date field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDateTime.class) {
				// LocalDateTime fields - use addDateTimeColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDateTime) field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing datetime field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateTimeColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Boolean.class || fieldType == boolean.class) {
				// Boolean fields - use addBooleanColumn with appropriate true/false text
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Boolean) field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing boolean field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addBooleanColumn(valueProvider, displayName, "Yes", "No");
			} else if (fieldName.toLowerCase().contains("description") || fieldName.toLowerCase().contains("comment")
					|| (fieldInfo.getMaxLength() > 100)) {
				// Long text fields - use addLongTextColumn
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						Object value = field.get(entity);
						return value != null ? value.toString() : "";
					} catch (Exception e) {
						LOGGER.error("Error accessing long text field {}: {}", fieldName, e.getMessage());
						return "";
					}
				};
				grid.addLongTextColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == String.class) {
				// Check if this is a color field that should be displayed with color background
				if (fieldInfo.isColorField()) {
					// Color field - create a custom component column to display the color with background
					ValueProvider valueProvider = entity -> {
						try {
							field.setAccessible(true);
							return field.get(entity);
						} catch (Exception e) {
							LOGGER.error("Error accessing color field {}: {}", fieldName, e.getMessage());
							return null;
						}
					};
					// Create a component column that shows the color value as background
					grid.addComponentColumn(entity -> {
						String colorValue = (String) valueProvider.apply(entity);
						CGridCell colorCell = new CGridCell();
						if (colorValue != null && !colorValue.trim().isEmpty()) {
							// Display the color value as text with background color
							colorCell.setText(colorValue);
							colorCell.getStyle().set("background-color", colorValue);
							// Apply contrasting text color for readability
							try {
								String textColor = CColorUtils.getContrastTextColor(colorValue);
								colorCell.getStyle().set("color", textColor);
							} catch (Exception e) {
								// Fallback to simple contrast logic
								colorCell.getStyle().set("color", isColorLight(colorValue) ? "#000000" : "#ffffff");
							}
							// Add some styling to make it look like a color swatch
							colorCell.getStyle().set("padding", "8px 12px");
							colorCell.getStyle().set("border-radius", "4px");
							colorCell.getStyle().set("text-align", "center");
							colorCell.getStyle().set("font-family", "monospace");
							colorCell.getStyle().set("font-weight", "bold");
						} else {
							colorCell.setText("No Color");
							colorCell.getStyle().set("color", "#666");
							colorCell.getStyle().set("font-style", "italic");
						}
						return colorCell;
					}).setHeader(CColorUtils.createStyledHeader(displayName, "#2a61Cf")).setWidth("150px").setFlexGrow(0).setSortable(true)
							.setKey(fieldName);
				} else {
					// Short text fields - use addShortTextColumn
					ValueProvider valueProvider = entity -> {
						try {
							field.setAccessible(true);
							Object value = field.get(entity);
							return value != null ? value.toString() : "";
						} catch (Exception e) {
							LOGGER.error("Error accessing text field {}: {}", fieldName, e.getMessage());
							return "";
						}
					};
					grid.addShortTextColumn(valueProvider, displayName, fieldName);
				}
			} else {
				// For any other type, use addEntityColumn which provides metadata-based styling
				ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (Exception e) {
						LOGGER.error("Error accessing field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addEntityColumn(valueProvider, displayName, fieldName);
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to create column for field {}, falling back to addColumnByProperty: {}", fieldName, e.getMessage());
			// Fallback to addColumnByProperty for simple property-based columns
			grid.addColumnByProperty(fieldName, displayName);
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void createContent() {
		try {
			if (gridEntity == null) {
				add(new Div("No grid configuration provided"));
				return;
			}
			String serviceBeanName = gridEntity.getDataServiceBeanName();
			Check.notNull(serviceBeanName, "Data service bean name is not set in grid entity");
			// Get the entity class from the service bean
			Class<?> entityClass = getEntityClassFromService(serviceBeanName);
			Check.notNull(entityClass, "Could not determine entity class from service: " + serviceBeanName);
			grid = new CGrid(entityClass);
			grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
			createGridColumns();
			// Load data from the service
			// loadDataFromService(serviceBeanName, gridEntity.getProject());
			refreshGridData();
			this.add(grid);
		} catch (Exception e) {
			LOGGER.error("Error creating grid content.");
			add(new Div("Error creating grid: " + e.getMessage()));
		}
	}

	public void createGridColumns() throws Exception {
		try {
			// clear existing columns
			grid.removeAllColumns();
			String serviceBeanName = gridEntity.getDataServiceBeanName();
			Class<?> entityClass = getEntityClassFromService(serviceBeanName);
			Check.notNull(entityClass, "Could not determine entity class from service: " + serviceBeanName);
			List<FieldConfig> fieldConfigs = parseSelectedFields(gridEntity.getColumnFields(), entityClass);
			fieldConfigs.forEach(fc -> createColumnForField(fc));
			// Configure sorting - sort by first column (ID) initially
			// Get the first column (ID column) and sort by it
			if (grid.getColumns().size() > 0) {
				Grid.Column<?> firstColumn = grid.getColumns().get(0);
				// Make it sortable and set as sorted
				firstColumn.setSortable(true);
			}
		} catch (Exception e) {
			LOGGER.warn("Could not configure sorting on first column: {}", e.getMessage());
			throw e;
		}
	}

	private Field findField(Class<?> entityClass, String fieldName) {
		Class<?> currentClass = entityClass;
		while (currentClass != null) {
			try {
				return currentClass.getDeclaredField(fieldName);
			} catch (NoSuchFieldException e) {
				currentClass = currentClass.getSuperclass();
			}
		}
		return null;
	}

	private Class<?> getEntityClassFromService(CAbstractService<?> service) throws Exception {
		try {
			Class<?> serviceClass = service.getClass();
			if (serviceClass.getName().contains("$$SpringCGLIB$$")) {
				serviceClass = serviceClass.getSuperclass();
			}
			Method getEntityClassMethod = serviceClass.getDeclaredMethod("getEntityClass");
			getEntityClassMethod.setAccessible(true);
			return (Class<?>) getEntityClassMethod.invoke(service);
		} catch (Exception e) {
			LOGGER.error("Could not get entity class from service: {}", e.getMessage());
			throw e;
		}
	}

	private Class<?> getEntityClassFromService(String serviceBeanName) throws Exception {
		try {
			CAbstractService<?> abstractService = CSpringContext.<CAbstractService<?>>getBean(serviceBeanName);
			return getEntityClassFromService(abstractService);
		} catch (Exception e) {
			LOGGER.error("Error getting entity class from service {}: {}", serviceBeanName, e.getMessage());
			throw e;
		}
	}

	public CGridEntity getGridEntity() { return gridEntity; }

	/** Gets the currently selected item from the grid */
	public CEntityDB<?> getSelectedItem() {
		if (grid != null) {
			return (CEntityDB<?>) grid.asSingleSelect().getValue();
		}
		return null;
	}

	/** Helper method to determine if a color is light or dark for text contrast
	 * @param color hex color string (e.g., "#FF0000")
	 * @return true if the color is light, false if dark */
	private boolean isColorLight(String color) {
		if (color == null || color.trim().isEmpty()) {
			return true; // Default to light
		}
		try {
			// Remove # if present
			String hex = color.startsWith("#") ? color.substring(1) : color;
			if (hex.length() == 3) {
				// Convert 3-digit hex to 6-digit
				hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) + hex.charAt(2) + hex.charAt(2);
			}
			if (hex.length() != 6) {
				return true; // Invalid format, default to light
			}
			// Calculate brightness using standard formula
			int r = Integer.parseInt(hex.substring(0, 2), 16);
			int g = Integer.parseInt(hex.substring(2, 4), 16);
			int b = Integer.parseInt(hex.substring(4, 6), 16);
			// Calculate brightness (0-255)
			double brightness = (r * 0.299 + g * 0.587 + b * 0.114);
			return brightness > 127; // Threshold for light vs dark
		} catch (Exception e) {
			return true; // Default to light on error
		}
	}

	public boolean isEnableSelectionChangeListener() { return enableSelectionChangeListener; }

	/** Checks if an entity matches the search text using reflection
	 * @throws Exception */
	private boolean matchesSearchText(Object entity, String searchText) throws Exception {
		if (entity == null || searchText == null || searchText.isEmpty()) {
			return true;
		}
		try {
			Class<?> entityClass = entity.getClass();
			// Search in common string fields using reflection
			Field[] fields = entityClass.getDeclaredFields();
			for (Field field : fields) {
				field.setAccessible(true);
				Object value = field.get(entity);
				if (value != null) {
					String stringValue = null;
					// Handle different field types
					if (value instanceof String) {
						stringValue = (String) value;
					} else if (value instanceof CEntityNamed) {
						// For related entities, search in their name
						CEntityNamed<?> namedEntity = (CEntityNamed<?>) value;
						stringValue = namedEntity.getName();
					}
					// Check if the string value contains the search text
					if (stringValue != null && stringValue.toLowerCase().contains(searchText)) {
						return true;
					}
				}
			}
			// Also check inherited fields from superclasses
			Class<?> superClass = entityClass.getSuperclass();
			while (superClass != null && !superClass.equals(Object.class)) {
				Field[] superFields = superClass.getDeclaredFields();
				for (Field field : superFields) {
					field.setAccessible(true);
					Object value = field.get(entity);
					if (value instanceof String) {
						String stringValue = (String) value;
						if (stringValue.toLowerCase().contains(searchText)) {
							return true;
						}
					}
				}
				superClass = superClass.getSuperclass();
			}
		} catch (Exception e) {
			LOGGER.error("Error checking search match for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
		return false;
	}

	@Override
	protected void onAttach(AttachEvent attachEvent) {
		super.onAttach(attachEvent);
		Check.notNull(sessionService, "SessionService is not available for project change notifications");
		// Register for project change notifications
		sessionService.addProjectChangeListener(this);
	}

	@Override
	protected void onDetach(DetachEvent detachEvent) {
		super.onDetach(detachEvent);
		Check.notNull(sessionService, "SessionService is not available for project change notifications");
		// Unregister from project change notifications to prevent memory leaks
		sessionService.removeProjectChangeListener(this);
	}

	@Override
	public void onProjectChanged(CProject newProject) {
		LOGGER.debug("Project change notification received in CComponentGridEntity: {} old {}", newProject != null ? newProject.getName() : "null",
				currentProject != null ? currentProject.getName() : "null");
		// Refresh grid data with new project
		if (currentProject != null && newProject != null && currentProject.getId().equals(newProject.getId())) {
			return;
		}
		currentProject = newProject;
		if (gridEntity != null) {
			refreshGridData();
		}
	}

	/** Handles grid selection changes and fires SelectionChangeEvent */
	protected void onSelectionChange(ValueChangeEvent<?> event) {
		if (!enableSelectionChangeListener) {
			return;
		}
		CEntityDB<?> selectedEntity = (CEntityDB<?>) event.getValue();
		fireEvent(new SelectionChangeEvent(this, selectedEntity));
	}

	private List<FieldConfig> parseSelectedFields(List<String> list, Class<?> entityClass) {
		List<FieldConfig> fieldConfigs = new ArrayList<>();
		Check.notNull(entityClass, "Entity class is null for parsing selected fields");
		Check.notNull(list, "Selected fields string is null");
		if (list.isEmpty()) {
			return fieldConfigs;
		}
		int order = 0;
		for (String fieldName : list) {
			Field field = findField(entityClass, fieldName);
			Check.notNull(field, "Field not found in entity class: " + fieldName);
			EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
			fieldConfigs.add(new FieldConfig(fieldInfo, order, field));
			order++;
		}
		// Sort by order
		fieldConfigs.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		return fieldConfigs;
	}

	/** Refresh grid data based on current project */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	public void refreshGridData() {
		LOGGER.debug("Refreshing grid data for grid entity: {}", gridEntity != null ? gridEntity.getName() : "null");
		try {
			boolean old_enableSelectionChangeListener = enableSelectionChangeListener;
			enableSelectionChangeListener = false;
			// first get the selected item to restore selection later
			CEntityDB<?> selectedItem = getSelectedItem();
			//
			CAbstractService<?> serviceBean = (CAbstractService<?>) CSpringContext.getBean(gridEntity.getDataServiceBeanName());
			Check.instanceOf(serviceBean, CAbstractService.class,
					"Service bean does not extend CAbstractService: " + gridEntity.getDataServiceBeanName());
			// Use pageable to get data - limit to first 1000 records for performance
			PageRequest pageRequest = PageRequest.of(0, 1000);
			List data;
			// Check if this is a project-specific service and filter by the gridEntity's project
			if (serviceBean instanceof CEntityOfProjectService) {
				CProject project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found."));
				Check.notNull(project, "Project is null");
				CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) serviceBean;
				data = projectService.listByProject(project, pageRequest).getContent();
			} else if (serviceBean instanceof CEntityOfCompanyService) {
				CCompany company = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company found."));
				Check.notNull(company, "Company is null");
				CEntityOfCompanyService<?> projectService = (CEntityOfCompanyService<?>) serviceBean;
				data = projectService.findByCompany(company, pageRequest).getContent();
			} else {
				// For non-project or company services, use regular list method
				data = serviceBean.list(pageRequest).getContent();
			}
			Check.notNull(data, "Data loaded from service is null");
			grid.setItems(data);
			enableSelectionChangeListener = old_enableSelectionChangeListener;
			selectEntity(selectedItem);
		} catch (Exception e) {
			LOGGER.error("Error loading data from service {}: {}", gridEntity.getDataServiceBeanName(), e.getMessage());
			grid.setItems(Collections.emptyList());
		}
	}

	/** Scrolls the grid to make the specified entity visible */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void scrollToEntity(CEntityDB<?> entity) {
		if (entity == null) {
			return;
		}
		try {
			CGrid rawGrid = grid;
			// Get all items and find the index of the entity
			List items = (List) rawGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
			int index = -1;
			for (int i = 0; i < items.size(); i++) {
				CEntityDB<?> item = (CEntityDB<?>) items.get(i);
				if (item.getId().equals(entity.getId())) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				// Scroll to the index to make the item visible
				rawGrid.scrollToIndex(index);
				LOGGER.debug("Scrolled to entity at index: {}", index);
			}
		} catch (Exception e) {
			LOGGER.error("Error scrolling to entity: {}", e.getMessage());
			throw e;
		}
	}

	/** Selects a specific entity in the grid */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void selectEntity(CEntityDB<?> entity) {
		try {
			// Use unchecked cast to work with generic grid constraints
			CGrid rawGrid = grid;
			rawGrid.select(entity);
			// Scroll to the selected entity to make it visible
			scrollToEntity(entity);
		} catch (Exception e) {
			LOGGER.error("Error selecting entity in grid: {}", e.getMessage());
			throw e;
		}
	}

	/** Selects the first item in the grid */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void selectFirstItem() {
		Check.notNull(grid, "Grid is not initialized");
		try {
			CGrid rawGrid = grid;
			grid.getDataProvider().fetch(new Query<>()).findFirst().ifPresent(entity -> {
				rawGrid.select(entity);
				// LOGGER.debug("Selected first item in grid");
			});
		} catch (Exception e) {
			LOGGER.error("Error selecting first item in grid: {}", e.getMessage());
			throw e;
		}
	}

	/** Selects the next item in the grid (useful after deletion) */
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public void selectNextItem() {
		Check.notNull(grid, "Grid is not initialized");
		CEntityDB<?> currentSelection = getSelectedItem();
		CGrid rawGrid = grid;
		List items = (List) rawGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
		if (items == null) {
			return;
		}
		if (currentSelection != null) {
			// Find the current item's index and select the next one
			int currentIndex = -1;
			for (int i = 0; i < items.size(); i++) {
				CEntityDB<?> item = (CEntityDB<?>) items.get(i);
				if (item.getId().equals(currentSelection.getId())) {
					currentIndex = i;
					break;
				}
			}
			// Select next item, or first item if we were at the end
			int nextIndex = (currentIndex + 1) < items.size() ? (currentIndex + 1) : 0;
			Object nextItem = items.get(nextIndex);
			rawGrid.select(nextItem);
			// LOGGER.debug("Selected next item at index: {}", nextIndex);
		} else {
			// No current selection, select first item
			selectFirstItem();
		}
	}

	public void setEnableSelectionChangeListener(boolean enableSelectionChangeListener) {
		if (this.enableSelectionChangeListener == enableSelectionChangeListener) {
			LOGGER.debug("Selection change listener already set to {}", enableSelectionChangeListener);
		}
		this.enableSelectionChangeListener = enableSelectionChangeListener;
	}

	public void setGridEntity(CGridEntity gridEntity) { this.gridEntity = gridEntity; }

	/** Sets a search filter on the grid */
	public void setSearchFilter(String searchValue) {
		Check.notNull(grid, "Grid is not set");
		// Apply filter to grid
		if (searchValue == null || searchValue.trim().isEmpty()) {
			// Clear filter by refreshing data
			refreshGridData();
		} else {
			// Apply search filter
			LOGGER.info("Search filter applied: {}", searchValue);
			applySearchFilter(searchValue.trim().toLowerCase());
		}
	}
}
