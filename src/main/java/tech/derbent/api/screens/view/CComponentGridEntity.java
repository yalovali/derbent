package tech.derbent.api.screens.view;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IPageServiceAutoRegistrable;
import tech.derbent.api.interfaces.IProjectChangeListener;
import tech.derbent.api.interfaces.drag.CDragDropEvent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CEvent;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.domain.CGridEntity.FieldConfig;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.services.pageservice.CPageService;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;

public class CComponentGridEntity extends CDiv implements IProjectChangeListener, IHasContentOwner, IHasDragControl, IPageServiceAutoRegistrable {

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

	/** Creates an error cell to display when widget creation fails. */
	private static Component createErrorCell(String message) {
		final CLabelEntity labelEntity = new CLabelEntity();
		labelEntity.setText(message);
		labelEntity.getStyle().set("color", "#666");
		labelEntity.getStyle().set("font-style", "italic");
		return labelEntity;
	}

	private static Field findField(Class<?> entityClass, String fieldName) {
		Class<?> currentClass = entityClass;
		while (currentClass != null) {
			try {
				return currentClass.getDeclaredField(fieldName);
			} catch (@SuppressWarnings ("unused") final NoSuchFieldException e) {
				currentClass = currentClass.getSuperclass();
			}
		}
		return null;
	}

	/** Finds the widget provider method on the bean class.
	 * @param beanClass  the bean class
	 * @param methodName the method name to find
	 * @param entityType the entity type for method parameter matching
	 * @return the Method object, or null if not found */
	private static Method findWidgetProviderMethod(Class<?> beanClass, String methodName, Class<?> entityType) {
		try {
			// Define parameter types to try in order of preference
			final Class<?>[] parameterTypeCandidates = {
					entityType, // Exact entity type
					CEntityDB.class, // Base entity type
					Object.class // Generic object type
			};
			// Try each parameter type candidate
			for (final Class<?> paramType : parameterTypeCandidates) {
				try {
					return beanClass.getMethod(methodName, paramType);
				} catch (@SuppressWarnings ("unused") final NoSuchMethodException e) {
					// Continue to next candidate
				}
			}
			// Fallback: search through all methods for a compatible one
			for (final Method method : beanClass.getMethods()) {
				if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
					final Class<?> paramType = method.getParameterTypes()[0];
					if (paramType.isAssignableFrom(entityType)) {
						return method;
					}
				}
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error finding widget provider method {}: {}", methodName, e.getMessage());
			return null;
		}
	}

	private static Class<?> getEntityClassFromService(CAbstractService<?> service) throws Exception {
		try {
			Class<?> serviceClass = service.getClass();
			if (serviceClass.getName().contains("$$SpringCGLIB$$")) {
				serviceClass = serviceClass.getSuperclass();
			}
			final Method getEntityClassMethod = serviceClass.getDeclaredMethod("getEntityClass");
			getEntityClassMethod.setAccessible(true);
			return (Class<?>) getEntityClassMethod.invoke(service);
		} catch (final Exception e) {
			LOGGER.error("Could not get entity class from service: {}", e.getMessage());
			throw e;
		}
	}

	private static Class<?> getEntityClassFromService(String serviceBeanName) throws Exception {
		try {
			final CAbstractService<?> abstractService = CSpringContext.<CAbstractService<?>>getBean(serviceBeanName);
			return getEntityClassFromService(abstractService);
		} catch (final Exception e) {
			LOGGER.error("Error getting entity class from service {}: {}", serviceBeanName, e.getMessage());
			throw e;
		}
	}

	/** Helper method to determine if a color is light or dark for text contrast
	 * @param color hex color string (e.g., "#FF0000")
	 * @return true if the color is light, false if dark */
	private static boolean isColorLight(String color) {
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
			final int r = Integer.parseInt(hex.substring(0, 2), 16);
			final int g = Integer.parseInt(hex.substring(2, 4), 16);
			final int b = Integer.parseInt(hex.substring(4, 6), 16);
			// Calculate brightness (0-255)
			final double brightness = r * 0.299 + g * 0.587 + b * 0.114;
			return brightness > 127; // Threshold for light vs dark
		} catch (@SuppressWarnings ("unused") final Exception e) {
			return true; // Default to light on error
		}
	}

	/** Checks if an entity matches the search text. This method now uses the entity's matchesFilter() method which provides hierarchical filtering.
	 * If the entity is a CEntityDB (which all domain entities extend), it uses the built-in filtering. Otherwise, falls back to simple string
	 * comparison.
	 * @param entity     the entity to check
	 * @param searchText the text to search for
	 * @return true if the entity matches the search text
	 * @throws Exception if there's an error during matching */
	private static boolean matchesSearchText(Object entity, String searchText) throws Exception {
		if (entity == null || searchText == null || searchText.isEmpty()) {
			return true;
		}
		try {
			// Use the new matchesFilter method if the entity is a CEntityDB
			if (entity instanceof CEntityDB) {
				final CEntityDB<?> entityDB = (CEntityDB<?>) entity;
				// Search in all common fields: id, name, description
				// This will automatically delegate through the hierarchy
				// Use mutable list for matchesFilter (it calls remove() internally)
				return entityDB.matchesFilter(searchText, new ArrayList<>(List.of("id", "name", "description")));
			}
			// Fallback for non-CEntityDB entities (shouldn't happen in normal usage)
			LOGGER.warn("Entity {} is not a CEntityDB instance, using fallback search", entity.getClass().getSimpleName());
			return entity.toString().toLowerCase().contains(searchText.toLowerCase());
		} catch (final Exception e) {
			LOGGER.error("Error checking search match for entity {}: {}", entity.getClass().getSimpleName(), e.getMessage());
			throw e;
		}
	}

	private static List<FieldConfig> parseSelectedFields(List<String> list, Class<?> entityClass) {
		final List<FieldConfig> fieldConfigs = new ArrayList<>();
		Check.notNull(entityClass, "Entity class is null for parsing selected fields");
		Check.notNull(list, "Selected fields string is null");
		if (list.isEmpty()) {
			return fieldConfigs;
		}
		int order = 0;
		for (final String fieldName : list) {
			final Field field = findField(entityClass, fieldName);
			Check.notNull(field, "Field not found in entity class: " + fieldName);
			final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
			fieldConfigs.add(new FieldConfig(fieldInfo, order, field));
			order++;
		}
		// Sort by order
		fieldConfigs.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));
		return fieldConfigs;
	}

	// Drag-drop event notification methods now provided by IHasDragControl interface default methods
	// notifyDragStartListeners(), notifyDragEndListeners(), notifyDropListeners() are inherited
	private IContentOwner contentOwner;
	protected CProject currentProject;
	// Drag control state
	private final Set<ComponentEventListener<CDragEndEvent>> dragEndListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragStartEvent>> dragStartListeners = new HashSet<>();
	private final Set<ComponentEventListener<CDragDropEvent>> dropListeners = new HashSet<>();
	private boolean enableSelectionChangeListener;
	private Class<?> entityClass;
	// Track components created in grid cells for event propagation
	private final Map<Object, Component> entityToWidgetMap = new HashMap<>();
	private CGrid<?> grid;
	private CGridEntity gridEntity;
	private ISessionService sessionService;
	private int widgetComponentCounter = 0;

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
		} catch (final Exception e) {
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
			final String serviceBeanName = gridEntity.getDataServiceBeanName();
			Check.notBlank(serviceBeanName, "Service bean name is blank for search filtering");
			// Get the service and entity class
			final CEntityOfProjectService<?> projectService = CSpringContext.<CEntityOfProjectService<?>>getBean(serviceBeanName);
			final CProject currentProject1 = sessionService != null
					? sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found.")) : null;
			// Get all entities for the current project - note: using raw types due to grid constraints
			final List allEntities = projectService.listByProject(currentProject1, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
			// Filter entities based on search text
			final List filteredEntities = (List) allEntities.stream().filter(entity -> {
				try {
					return matchesSearchText(entity, searchText);
				} catch (final Exception e) {
					e.printStackTrace();
					return false;
				}
			}).collect(Collectors.toList());
			// Update grid with filtered data
			grid.setItems(filteredEntities);
			LOGGER.debug("Applied search filter '{}' - {} results out of {} total", searchText, filteredEntities.size(), allEntities.size());
		} catch (final Exception e) {
			LOGGER.error("Error applying search filter. {}", e.getMessage());
			throw e;
		}
	}

	/** Creates a component column for CComponentWidgetEntity fields using dataProviderBean and dataProviderMethod.
	 * <p>
	 * When the field type is CComponentWidgetEntity or its subclass, this method:
	 * <ol>
	 * <li>Reaches the current page via contentOwner (which should be IPageServiceImplementer)</li>
	 * <li>Gets the CPageService from the page</li>
	 * <li>Invokes the method specified by dataProviderMethod (e.g., "getComponentWidget")</li>
	 * <li>The method receives the current grid item and returns a widget component</li>
	 * <li>If the widget implements IHasDragStart/IHasDragEnd, registers it with the page service for event binding</li>
	 * </ol>
	 * Example annotation: dataProviderBean = "pageservice", dataProviderMethod = "getComponentWidget"
	 * </p>
	 * @param fieldInfo   the field metadata containing dataProviderBean and dataProviderMethod
	 * @param displayName the column display name
	 * @param fieldName   the field name for the column key */
	@SuppressWarnings ({})
	private void createColumnForComponentWidgetEntity(EntityFieldInfo fieldInfo, String displayName, String fieldName) {
		try {
			final String beanName = fieldInfo.getDataProviderBean();
			final String methodName = fieldInfo.getDataProviderMethod();
			// Validate required annotations
			if (beanName == null || beanName.isBlank()) {
				LOGGER.warn("CComponentWidgetEntity field {} requires dataProviderBean annotation", fieldName);
				return;
			}
			if (methodName == null || methodName.isBlank()) {
				LOGGER.warn("CComponentWidgetEntity field {} requires dataProviderMethod annotation", fieldName);
				return;
			}
			// Create a component column that invokes the data provider method for each row
			final var column = grid.addComponentColumn(entity -> {
				try {
					// Resolve the bean based on beanName
					final Object bean = resolveWidgetProviderBean(beanName);
					if (bean == null) {
						LOGGER.warn("Could not resolve widget provider bean: {}", beanName);
						return createErrorCell("Bean not found");
					}
					// Find and invoke the method on the bean
					final Method method = findWidgetProviderMethod(bean.getClass(), methodName, entity.getClass());
					if (method == null) {
						LOGGER.warn("Could not find widget provider method {} on bean {}", methodName, beanName);
						return createErrorCell("Method not found");
					}
					// Try to invoke the method; use setAccessible only if method is not already accessible
					if (!method.canAccess(bean)) {
						method.setAccessible(true);
					}
					final Object result = method.invoke(bean, entity);
					if (result instanceof Component) {
						final Component component = (Component) result;
						if (result instanceof IHasDragControl) {
							// Set up forwarding from the widget (child) to this grid entity (parent)
							setupChildDragDropForwarding((IHasDragControl) result);
						}
						return component;
					} else if (result == null) {
						return createErrorCell("Null widget");
					} else {
						LOGGER.warn("Widget provider method {} returned non-Component type: {}", methodName, result.getClass().getName());
						return createErrorCell("Invalid widget type");
					}
				} catch (final Exception e) {
					LOGGER.error("Error invoking widget provider method {} for entity: {}", methodName, e.getMessage());
					return createErrorCell("Widget error");
				}
			});
			CGrid.styleColumnHeader(column.setAutoWidth(true).setFlexGrow(1).setKey(fieldName), displayName);
			LOGGER.debug("Created component widget column for field {} using bean {} method {}", fieldName, beanName, methodName);
		} catch (final Exception e) {
			LOGGER.error("Error creating column for CComponentWidgetEntity field {}: {}", fieldName, e.getMessage());
		}
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private void createColumnForField(FieldConfig fieldConfig) {
		final Field field = fieldConfig.getField();
		final EntityFieldInfo fieldInfo = fieldConfig.getFieldInfo();
		final String fieldName = field.getName();
		final String displayName = fieldInfo.getDisplayName();
		final Class<?> fieldType = field.getType();
		try {
			// Check if field type is CComponentWidgetEntity or its subclass - handle via dataProviderBean/Method
			if (CComponentWidgetEntity.class.isAssignableFrom(fieldType)) {
				createColumnForComponentWidgetEntity(fieldInfo, displayName, fieldName);
				return;
			}
			// Handle different field types using appropriate CGrid methods
			if (CEntityDB.class.isAssignableFrom(fieldType)) {
				// Entity reference - use addEntityColumn for consistent display with CLabelEntity
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing entity field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addEntityColumn(valueProvider, displayName, fieldName, entityClass);
			} else if (Collection.class.isAssignableFrom(fieldType)) {
				// Collection field - use addColumnEntityCollection if it contains entities
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						final Object value = field.get(entity);
						return value instanceof Collection ? (Collection) value : Collections.emptyList();
					} catch (final Exception e) {
						LOGGER.error("Error accessing collection field {}: {}", fieldName, e.getMessage());
						return Collections.emptyList();
					}
				};
				grid.addColumnEntityCollection(valueProvider, displayName);
			} else if (fieldName.toLowerCase().contains("id")
					&& (fieldType == Long.class || fieldType == long.class || fieldType == Integer.class || fieldType == int.class)) {
				// ID fields - use addIdColumn for consistent ID formatting
				final ValueProvider valueProvider = entity -> {
					return ((CEntityDB) entity).getId();
				};
				grid.addIdColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Integer.class || fieldType == int.class) {
				// Integer fields - use addIntegerColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Integer) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing integer field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addIntegerColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == BigDecimal.class) {
				// BigDecimal fields - use addDecimalColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (BigDecimal) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing decimal field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDecimalColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDate.class) {
				// LocalDate fields - use addDateColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDate) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing date field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalDateTime.class) {
				// LocalDateTime fields - use addDateTimeColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalDateTime) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing datetime field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addDateTimeColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == LocalTime.class) {
				// LocalDateTime fields - use addDateTimeColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (LocalTime) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing datetime field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addTimeColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == Boolean.class || fieldType == boolean.class) {
				// Boolean fields - use addBooleanColumn with appropriate true/false text
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						return (Boolean) field.get(entity);
					} catch (final Exception e) {
						LOGGER.error("Error accessing boolean field {}: {}", fieldName, e.getMessage());
						return null;
					}
				};
				grid.addBooleanColumn(valueProvider, displayName, "Yes", "No");
			} else if (fieldName.toLowerCase().contains("description") || fieldName.toLowerCase().contains("comment")
					|| fieldInfo.getMaxLength() > 100) {
				// Long text fields - use addLongTextColumn
				final ValueProvider valueProvider = entity -> {
					try {
						field.setAccessible(true);
						final Object value = field.get(entity);
						return value != null ? value.toString() : "";
					} catch (final Exception e) {
						LOGGER.error("Error accessing long text field {}: {}", fieldName, e.getMessage());
						return "";
					}
				};
				grid.addLongTextColumn(valueProvider, displayName, fieldName);
			} else if (fieldType == String.class) {
				// Check if this is a color field that should be displayed with color background
				if (fieldInfo.isColorField()) {
					// Color field - create a custom component column to display the color with background
					final ValueProvider valueProvider = entity -> {
						try {
							field.setAccessible(true);
							return field.get(entity);
						} catch (final Exception e) {
							LOGGER.error("Error accessing color field {}: {}", fieldName, e.getMessage());
							return null;
						}
					};
					// Create a component column that shows the color value as background
					final var column = grid.addComponentColumn(entity -> {
						final String colorValue = (String) valueProvider.apply(entity);
						final CLabelEntity labelEntity = new CLabelEntity();
						if (colorValue != null && !colorValue.trim().isEmpty()) {
							// Display the color value as text with background color
							labelEntity.setText(colorValue);
							labelEntity.getStyle().set("background-color", colorValue);
							// Apply contrasting text color for readability
							try {
								final String textColor = CColorUtils.getContrastTextColor(colorValue);
								labelEntity.getStyle().set("color", textColor);
							} catch (@SuppressWarnings ("unused") final Exception e) {
								// Fallback to simple contrast logic
								labelEntity.getStyle().set("color", isColorLight(colorValue) ? "#000000" : "#ffffff");
							}
							// Add some styling to make it look like a color swatch
							labelEntity.getStyle().set("padding", "8px 12px");
							labelEntity.getStyle().set("border-radius", "4px");
							labelEntity.getStyle().set("text-align", "center");
							labelEntity.getStyle().set("font-family", "monospace");
							labelEntity.getStyle().set("font-weight", "bold");
						} else {
							labelEntity.setText("No Color");
							labelEntity.getStyle().set("color", "#666");
							labelEntity.getStyle().set("font-style", "italic");
						}
						return labelEntity;
					});
					CGrid.styleColumnHeader(column.setWidth("150px").setFlexGrow(0).setSortable(true).setKey(fieldName), displayName);
				} else {
					// Short text fields - use addShortTextColumn
					final ValueProvider valueProvider = entity -> {
						try {
							field.setAccessible(true);
							final Object value = field.get(entity);
							return value != null ? value.toString() : "";
						} catch (final Exception e) {
							LOGGER.error("Error accessing text field {}: {}", fieldName, e.getMessage());
							return "";
						}
					};
					grid.addShortTextColumn(valueProvider, displayName, fieldName);
				}
			} else {
				Check.fail("Unsupported field type for column creation: " + fieldType.getName());
				// For any other type, use addEntityColumn which provides metadata-based styling
				// final ValueProvider valueProvider = entity -> {
				// try {
				// field.setAccessible(true);
				// return field.get(entity);
				// } catch (final Exception e) {
				// LOGGER.error("Error accessing field {}: {}", fieldName, e.getMessage());
				// return null;
				// }
				// };
				// grid.addEntityColumn(valueProvider, displayName, fieldName);
			}
		} catch (final Exception e) {
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
			final String serviceBeanName = gridEntity.getDataServiceBeanName();
			Check.notNull(serviceBeanName, "Data service bean name is not set in grid entity");
			// Get the entity class from the service bean
			entityClass = getEntityClassFromService(serviceBeanName);
			Check.notNull(entityClass, "Could not determine entity class from service: " + serviceBeanName);
			grid = new CGrid(entityClass);
			grid.asSingleSelect().addValueChangeListener(this::onSelectionChange);
			// Add drag-drop listeners to propagate events to this component's listeners
			setupGridDragDropListeners();
			createGridColumns();
			grid.setRefreshConsumer(e -> grid_refresh_consumer());
			refreshGrid();
			this.add(grid);
		} catch (final Exception e) {
			LOGGER.error("Error creating grid content.");
			add(new Div("Error creating grid: " + e.getMessage()));
		}
	}

	public void createGridColumns() throws Exception {
		try {
			// clear existing columns
			grid.removeAllColumns();
			final String serviceBeanName = gridEntity.getDataServiceBeanName();
			final Class<?> entityClass1 = getEntityClassFromService(serviceBeanName);
			Check.notNull(entityClass1, "Could not determine entity class from service: " + serviceBeanName);
			// Check if widget mode is enabled
			// Traditional column-based mode
			final List<FieldConfig> fieldConfigs = parseSelectedFields(gridEntity.getColumnFields(), entityClass1);
			fieldConfigs.forEach(fc -> createColumnForField(fc));
			// Configure sorting - sort by first column (ID) initially
			// Get the first column (ID column) and sort by it
			if (grid.getColumns().size() > 0) {
				final Grid.Column<?> firstColumn = grid.getColumns().get(0);
				// Make it sortable and set as sorted
				firstColumn.setSortable(true);
			}
		} catch (final Exception e) {
			LOGGER.warn("Could not configure sorting on first column: {}", e.getMessage());
			throw e;
		}
	}

	@Override
	public void drag_checkEventAfterPass(CEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void drag_checkEventBeforePass(CEvent event) {
		// LOGGER.debug("Drag event check before pass: {} comp id:{} event type:{}", event, getId(), event.getClass().getSimpleName());
	}

	@Override
	public Set<ComponentEventListener<CDragEndEvent>> drag_getDragEndListeners() {
		return dragEndListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragStartEvent>> drag_getDragStartListeners() {
		return dragStartListeners;
	}

	@Override
	public Set<ComponentEventListener<CDragDropEvent>> drag_getDropListeners() {
		return dropListeners;
	}

	/** Generates a unique component name for a widget component.
	 * @param component the component to generate a name for
	 * @param entity    the entity associated with the component
	 * @return a unique component name */
	private String generateWidgetComponentName(final Component component, final Object entity) {
		// Use entity ID if available, otherwise use a counter
		final String entityId =
				entity instanceof CEntityDB ? String.valueOf(((CEntityDB<?>) entity).getId()) : String.valueOf(widgetComponentCounter++);
		// Use component class simple name (e.g., "CComponentWidgetSprint") and entity ID
		final String componentTypeName = component.getClass().getSimpleName().replaceFirst("^C", "").toLowerCase();
		return componentTypeName + "_" + entityId;
	}

	/** Returns the component name for method binding.
	 * <p>
	 * Default name is "masterGrid" for master grid-level event handlers like:
	 * <ul>
	 * <li>on_masterGrid_drop(Component, Object) - handle drop events</li>
	 * </ul>
	 * <p>
	 * Note: dragStart and dragEnd handlers are rarely needed in application code since all drag data is carried in events. Use drop handlers with
	 * event.getDraggedItem() and event.getSourceList() instead.
	 * <p>
	 * The name "masterGrid" is used because this grid represents the master component in a master-detail view pattern.
	 * @return The component name "masterGrid" */
	@Override
	public String getComponentName() { return "masterGrid"; }

	@Override
	public IContentOwner getContentOwner() { return contentOwner; }
	// ==================== IHasDragStart, IHasDragEnd, IHasDrop Implementation ====================

	public CGridEntity getGridEntity() { return gridEntity; }

	/** Gets the currently selected item from the grid */
	public CEntityDB<?> getSelectedItem() {
		if (grid != null) {
			return (CEntityDB<?>) grid.asSingleSelect().getValue();
		}
		return null;
	}

	/** Refresh grid data based on current project */
	@SuppressWarnings ({
			"rawtypes", "unchecked"
	})
	public void grid_refresh_consumer() {
		LOGGER.debug("Refreshing grid data for grid entity: {}", gridEntity != null ? gridEntity.getName() : "null");
		try {
			final boolean old_enableSelectionChangeListener = enableSelectionChangeListener;
			enableSelectionChangeListener = false;
			// first get the selected item to restore selection later
			// Clear the widget component map before refreshing to prevent memory leaks
			unregisterAllWidgetComponents();
			//
			final CAbstractService<?> serviceBean = (CAbstractService<?>) CSpringContext.getBean(gridEntity.getDataServiceBeanName());
			Check.instanceOf(serviceBean, CAbstractService.class,
					"Service bean does not extend CAbstractService: " + gridEntity.getDataServiceBeanName());
			// Use pageable to get data - limit to first 1000 records for performance
			final PageRequest pageRequest = PageRequest.of(0, 1000);
			List data;
			// Check if this is a project-specific service and filter by the gridEntity's project
			if (serviceBean instanceof CEntityOfProjectService) {
				final CProject project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project found."));
				Check.notNull(project, "Project is null");
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) serviceBean;
				data = projectService.listByProject(project, pageRequest).getContent();
			} else if (serviceBean instanceof CEntityOfCompanyService) {
				final CCompany company = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company found."));
				Check.notNull(company, "Company is null");
				final CEntityOfCompanyService<?> projectService = (CEntityOfCompanyService<?>) serviceBean;
				data = projectService.findByCompany(company, pageRequest).getContent();
			} else {
				// For non-project or company services, use regular list method
				data = serviceBean.list(pageRequest).getContent();
			}
			Check.notNull(data, "Data loaded from service is null");
			grid.setItems(data);
			enableSelectionChangeListener = old_enableSelectionChangeListener;
		} catch (final Exception e) {
			LOGGER.error("Error loading data from service {}: {}", gridEntity.getDataServiceBeanName(), e.getMessage());
			grid.setItems(Collections.emptyList());
		}
	}

	public boolean isEnableSelectionChangeListener() { return enableSelectionChangeListener; }

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
			refreshGrid();
		}
	}

	/** Handles grid selection changes and fires SelectionChangeEvent */
	protected void onSelectionChange(ValueChangeEvent<?> event) {
		if (!enableSelectionChangeListener) {
			return;
		}
		final CEntityDB<?> selectedEntity = (CEntityDB<?>) event.getValue();
		fireEvent(new SelectionChangeEvent(this, selectedEntity));
	}

	@Override
	public void populateForm() throws Exception {
		// Grid component doesn't have a form to populate; implementation exists for interface compliance
	}

	public void refreshGrid() {
		LOGGER.debug("Refreshing grid data...");
		final CEntityDB<?> selectedItem = getSelectedItem();
		grid.refreshGrid();
		selectEntity(selectedItem);
	}

	/** Registers this grid component with the page service for automatic event binding.
	 * <p>
	 * This enables page service handlers matching the pattern on_grid_{action} to be automatically bound to this grid component for drag-drop and
	 * selection events.
	 * <p>
	 * Note: This method only registers the component. The actual method binding happens when CPageService.bind() is called, which occurs once during
	 * page initialization.
	 * @param pageService The page service to register with */
	@Override
	public void registerWithPageService(final CPageService<?> pageService) {
		Check.notNull(pageService, "Page service cannot be null");
		final String componentName = getComponentName();
		pageService.registerComponent(componentName, this);
		LOGGER.debug("[BindDebug] CComponentGridEntity auto-registered with page service as '{}' (binding will occur during CPageService.bind())",
				componentName);
	}

	/** Resolves the widget provider bean based on the beanName.
	 * @param beanName the bean name ("pageservice" for CPageService, or a Spring bean name)
	 * @return the resolved bean, or null if not found */
	private Object resolveWidgetProviderBean(String beanName) {
		try {
			if ("pageservice".equals(beanName)) {
				// For "pageservice" bean, get the CPageService from the IPageServiceImplementer
				if (contentOwner instanceof final IPageServiceImplementer<?> pageServiceImplementer) {
					return pageServiceImplementer.getPageService();
				}
				LOGGER.warn("contentOwner is not IPageServiceImplementer - cannot use 'view' as dataProviderBean");
				return null;
			} else if ("context".equals(beanName)) {
				// For "context" bean, return the content owner itself
				return contentOwner;
			} else {
				// For other beans, get from Spring context
				return CSpringContext.getBean(beanName);
			}
		} catch (final Exception e) {
			LOGGER.error("Error resolving widget provider bean {}: {}", beanName, e.getMessage());
			return null;
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
			final CGrid rawGrid = grid;
			// Get all items and find the index of the entity
			final List items = (List) rawGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
			int index = -1;
			for (int i = 0; i < items.size(); i++) {
				final CEntityDB<?> item = (CEntityDB<?>) items.get(i);
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
		} catch (final Exception e) {
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
			final CGrid rawGrid = grid;
			rawGrid.select(entity);
			// Scroll to the selected entity to make it visible
			scrollToEntity(entity);
		} catch (final Exception e) {
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
			final CGrid rawGrid = grid;
			grid.getDataProvider().fetch(new Query<>()).findFirst().ifPresent(entity -> {
				rawGrid.select(entity);
				// LOGGER.debug("Selected first item in grid");
			});
		} catch (final Exception e) {
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
		final CEntityDB<?> currentSelection = getSelectedItem();
		final CGrid rawGrid = grid;
		final List items = (List) rawGrid.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
		if (items == null) {
			return;
		}
		if (currentSelection != null) {
			// Find the current item's index and select the next one
			int currentIndex = -1;
			for (int i = 0; i < items.size(); i++) {
				final CEntityDB<?> item = (CEntityDB<?>) items.get(i);
				if (item.getId().equals(currentSelection.getId())) {
					currentIndex = i;
					break;
				}
			}
			// Select next item, or first item if we were at the end
			final int nextIndex = currentIndex + 1 < items.size() ? currentIndex + 1 : 0;
			final Object nextItem = items.get(nextIndex);
			rawGrid.select(nextItem);
			// LOGGER.debug("Selected next item at index: {}", nextIndex);
		} else {
			// No current selection, select first item
			selectFirstItem();
		}
	}

	@Override
	public void setContentOwner(IContentOwner parentContent) { contentOwner = parentContent; }
	// IPageServiceAutoRegistrable interface implementation

	/** Enables or disables drag-and-drop functionality for the grid.
	 * <p>
	 * When enabled, rows in the grid can be dragged. When disabled, drag operations are blocked but the grid can still receive drop events if drop is
	 * enabled.
	 * @param enabled true to enable drag, false to disable */
	@Override
	public void setDragEnabled(final boolean enabled) {
		grid.setDragEnabled(enabled); // Use CGrid's IHasDragControl method
		// LOGGER.debug("[DragDebug] Drag {} for CComponentGridEntity", enabled ? "enabled" : "disabled");
	}

	/** Enables or disables drop functionality for the grid.
	 * <p>
	 * When enabled, the grid can accept drop operations. When disabled, drops are blocked. This is independent of drag functionality - a grid can
	 * accept drops without being draggable.
	 * @param enabled true to enable drop, false to disable */
	@Override
	public void setDropEnabled(final boolean enabled) {
		grid.setDropEnabled(enabled); // Use CGrid's IHasDragControl method
		// LOGGER.debug("[DragDebug] Drop {} for CComponentGridEntity", enabled ? "enabled" : "disabled");
	}
	// IHasDragControl interface implementation

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
			refreshGrid();
		} else {
			// Apply search filter
			LOGGER.info("Search filter applied: {}", searchValue);
			applySearchFilter(searchValue.trim().toLowerCase());
		}
	}

	/** Sets up drag-drop listeners on the grid to propagate events to this component's listeners.
	 * <p>
	 * This method binds the grid's drag start, drag end, and drop events to propagate to this component's registered listeners. The contentOwner
	 * (parent) should register to this component's notifications, not vice versa.
	 * </p>
	 */
	@SuppressWarnings ({})
	/** Sets up drag-drop event forwarding from grid to parent component.
	 * <p>
	 * Uses the interface's default setupChildDragDropForwarding() method to eliminate code duplication. */
	private void setupGridDragDropListeners() {
		Check.notNull(grid, "Grid must be created before setting up drag-drop listeners");
		// Use interface's default method for forwarding - forwards grid events to this component's listeners
		setupChildDragDropForwarding(grid);
	}

	@Override
	public String toString() {
		return "CComponentGridEntity for " + (entityClass != null ? entityClass.getSimpleName() : "Unknown Entity");
	}

	/** Unregisters all widget components from the page service to prevent memory leaks. */
	private void unregisterAllWidgetComponents() {
		try {
			// Only unregister if contentOwner is a page service implementer
			if (!(contentOwner instanceof IPageServiceImplementer<?>)) {
				return;
			}
			final IPageServiceImplementer<?> pageServiceImpl = (IPageServiceImplementer<?>) contentOwner;
			// Unregister all widget components from the page service
			for (final Map.Entry<Object, Component> entry : entityToWidgetMap.entrySet()) {
				final Component component = entry.getValue();
				final String componentName = generateWidgetComponentName(component, entry.getKey());
				pageServiceImpl.getPageService().unregisterComponent(componentName);
				LOGGER.debug("Unregistered widget component '{}' from page service", componentName);
			}
			// Clear the map
			entityToWidgetMap.clear();
			LOGGER.debug("Cleared all widget component registrations");
		} catch (final Exception e) {
			LOGGER.error("Error unregistering widget components: {}", e.getMessage());
		}
	}
}
