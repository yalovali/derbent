package tech.derbent.api.grid.domain;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.interfaces.IHasDragControl;
import tech.derbent.api.interfaces.IStateOwnerComponent;
import tech.derbent.api.interfaces.drag.CDragEndEvent;
import tech.derbent.api.interfaces.drag.CDragStartEvent;
import tech.derbent.api.interfaces.drag.CDropEvent;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.enhanced.CPictureSelector;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.api.utils.Check;

/** CGrid - Base grid class for consistent field width management based on data types. Layer: View (MVC) Follows the project's coding guidelines by
 * providing a base class for all grids to ensure consistent column widths based on field types: - ID fields: Very small width (80px) - Integer
 * fields: Small width (100px) - BigDecimal fields: Medium width (120px) - Date fields: Medium width (150px) - Boolean/Status fields: Small-Medium
 * width (100px) - Short text fields: Medium width (200px) - Long text fields: Large width (300px+) - Reference fields: Medium width (200px) */
// public class CGrid<EntityClass extends CEntityDB<EntityClass>> extends Grid<EntityClass> {
public class CGrid<EntityClass> extends Grid<EntityClass> implements IStateOwnerComponent, IHasDragControl {

	private static final long serialVersionUID = 1L;
	public static final String WIDTH_BOOLEAN = "100px";
	public static final String WIDTH_DATE = "150px";
	public static final String WIDTH_DECIMAL = "120px";
	public static final String WIDTH_ID = "80px";
	public static final String WIDTH_IMAGE = "60px";
	public static final String WIDTH_INTEGER = "100px";
	public static final String WIDTH_LONG_TEXT = "300px";
	public static final String WIDTH_REFERENCE = "200px";
	public static final String WIDTH_SHORT_TEXT = "200px";

	/** Prefer calling ref.getName(); fall back to toString() if not present. */
	private static String entityName(final CEntityDB<?> ref) {
		try {
			final Method m = ref.getClass().getMethod("getName");
			final Object v = m.invoke(ref);
			return v == null ? "" : v.toString();
		} catch (final ReflectiveOperationException ignore) {
			return String.valueOf(ref);
		}
	}

	public static <T> void setupGrid(final Grid<T> grid) {
		Check.notNull(grid, "Grid cannot be null when setting up relational component");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		final GridSingleSelectionModel<T> sm = (GridSingleSelectionModel<T>) grid.getSelectionModel();
		sm.setDeselectAllowed(false);
		grid.getStyle().set("border-radius", "8px");
		grid.getStyle().set("border", "1px solid #E0E0E0");
		grid.setWidthFull();
		CAuxillaries.setId(grid);
	}

	public static <T> Column<T> styleColumnHeader(final Column<T> column, final String header) {
		Check.notNull(column, "Column cannot be null when styling header");
		Check.notBlank(header, "Header text cannot be blank when styling header");
		column.setHeader(CColorUtils.createStyledHeader(header, CColorUtils.CRUD_READ_COLOR));
		column.setResizable(true);
		return column;
	}

	// Drag control state
	private boolean dragEnabled = false;
	private final List<ComponentEventListener<CDragEndEvent>> dragEndListeners = new ArrayList<>();
	private final List<ComponentEventListener<CDragStartEvent<?>>> dragStartListeners = new ArrayList<>();
	private boolean dropEnabled = false;
	private final List<ComponentEventListener<CDropEvent<?>>> dropListeners = new ArrayList<>();
	/** Constructor for CGrid with entity class.
	 * @param entityClass The entity class for the grid */
	Class<EntityClass> clazz;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	/** Map to store widget providers for columns that create components implementing IStateOwnerComponent. Key: Column key, Value: Widget provider
	 * function */
	private final Map<String, Function<EntityClass, ? extends Component>> widgetProviders = new HashMap<>();

	@SuppressWarnings ("unchecked")
	public CGrid(final Class<?> class1) {
		super((Class<EntityClass>) class1, false);
		setEmptyStateText("No entites ...");
		setClazz(class1);
		initializeGrid();
	}

	/** Override to hook into Vaadin Grid's drag start events and convert to our custom event type.
	 * <p>
	 * This is where CGrid bridges Vaadin Grid events to our unified IHasDragControl event system. */
	@Override
	@SuppressWarnings ("unchecked")
	public void addEventListener_dragStart(final ComponentEventListener<CDragStartEvent<?>> listener) {
		dragStartListeners.add(listener);
		// Only register with Vaadin Grid once (on first listener)
		if (dragStartListeners.size() == 1) {
			super.addDragStartListener(gridEvent -> {
				// Convert Vaadin GridDragStartEvent to our CDragStartEvent
				final CDragStartEvent<EntityClass> customEvent = new CDragStartEvent<>(this, new ArrayList<>(gridEvent.getDraggedItems()),
						gridEvent.isFromClient());
				// Notify all our listeners
				notifyDragStartListeners(customEvent);
			});
		}
	}

	/** Override to hook into Vaadin Grid's drag end events and convert to our custom event type. */
	@Override
	public void addEventListener_dragEnd(final ComponentEventListener<CDragEndEvent> listener) {
		dragEndListeners.add(listener);
		// Only register with Vaadin Grid once (on first listener)
		if (dragEndListeners.size() == 1) {
			super.addDragEndListener(gridEvent -> {
				// Convert Vaadin GridDragEndEvent to our CDragEndEvent
				final CDragEndEvent customEvent = new CDragEndEvent(this, gridEvent.isFromClient());
				// Notify all our listeners
				notifyDragEndListeners(customEvent);
			});
		}
	}

	/** Override to hook into Vaadin Grid's drop events and convert to our custom event type. */
	@Override
	@SuppressWarnings ("unchecked")
	public void addEventListener_dragDrop(final ComponentEventListener<CDropEvent<?>> listener) {
		dropListeners.add(listener);
		// Only register with Vaadin Grid once (on first listener)
		if (dropListeners.size() == 1) {
			super.addDropListener(gridEvent -> {
				// Convert Vaadin GridDropEvent to our CDropEvent
				// Note: We need to track dragged items from drag start - for now pass empty list
				// The drag source tracking is handled by CPageService
				final CDropEvent<EntityClass> customEvent = new CDropEvent<>(this, new ArrayList<>(), // Dragged items tracked elsewhere
						gridEvent.getSource(), // Drag source
						gridEvent.getDropTargetItem().orElse(null), // Target item
						gridEvent.getDropLocation(), // Drop location
						gridEvent.isFromClient());
				// Notify all our listeners
				notifyDropListeners(customEvent);
			});
		}
	}

	public Column<EntityClass> addBooleanColumn(final ValueProvider<EntityClass, Boolean> valueProvider, final String header, final String trueText,
			final String falseText) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		Check.notBlank(trueText, "True text cannot be null or blank");
		Check.notBlank(falseText, "False text cannot be null or blank");
		final Column<EntityClass> column = addColumn(entity -> {
			final Boolean value = valueProvider.apply(entity);
			return value != null && value ? trueText : falseText;
		}).setWidth(WIDTH_BOOLEAN).setFlexGrow(0).setSortable(true).setResizable(true);
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addColumn(final ValueProvider<EntityClass, String> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(valueProvider).setFlexGrow(1).setSortable(true).setResizable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addColumnByProperty(final String propertyName, final String header) {
		Check.notBlank(propertyName, "Property name cannot be null or blank");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(propertyName).setSortable(true).setResizable(true);
		Check.notNull(column, "Column creation failed for property: " + propertyName);
		// Apply width based on property name patterns
		if (propertyName.toLowerCase().contains("id")) {
			column.setWidth(WIDTH_ID).setFlexGrow(0);
		} else if (propertyName.toLowerCase().contains("percentage") || propertyName.toLowerCase().contains("progress")) {
			column.setWidth(WIDTH_INTEGER).setFlexGrow(0);
		} else if (propertyName.toLowerCase().contains("date")) {
			column.setWidth(WIDTH_DATE).setFlexGrow(0);
		} else if (propertyName.toLowerCase().contains("email") || propertyName.toLowerCase().contains("description")) {
			column.setWidth(WIDTH_LONG_TEXT).setFlexGrow(0);
		} else {
			// Default to short text width
			column.setWidth(WIDTH_SHORT_TEXT).setFlexGrow(0);
		}
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addColumnEntityCollection(final ValueProvider<EntityClass, ? extends Collection<?>> valueProvider,
			final String header) {
		Check.notNull(valueProvider, "Value provider cannot be null for header: " + header);
		Check.notBlank(header, "Header cannot be null or blank");
		// compute display string from the collection - now supports both CEntityDB and other types (e.g., String)
		final ValueProvider<EntityClass, String> namesProvider = entity -> {
			try {
				Check.notNull(entity, "Entity cannot be null when rendering collection column for header: " + header);
				final Collection<?> items = valueProvider.apply(entity);
				// Check if collection is null or not initialized (lazy loading)
				if (items == null || !Hibernate.isInitialized(items)) {
					LOGGER.debug("Collection for header '{}' is null or not initialized for entity ID: {}", header, entity.toString());
					return "No " + header.toLowerCase(); // e.g. "No participants"
				}
				// Safe to call isEmpty() now since collection is initialized
				if (items.isEmpty()) {
					LOGGER.debug("Collection for header '{}' is empty for entity ID: {}", header, entity.toString());
					return "No " + header.toLowerCase();
				}
				// Handle different collection item types
				return items.stream().map(item -> {
					try {
						Check.notNull(item, "Collection item cannot be null in header: " + header);
						// If item is a CEntityDB, use entityName to get the name
						if (item instanceof CEntityDB<?>) {
							final CEntityDB<?> ref = (CEntityDB<?>) item;
							final String name = entityName(ref);
							return name != null && !name.isBlank() ? name : "Entity#" + ref.getId();
						}
						// For non-entity types (e.g., String), use toString()
						final String value = item.toString();
						Check.notBlank(value, "Collection item string representation cannot be blank in header: " + header);
						return value;
					} catch (final Exception itemEx) {
						LOGGER.error("Error rendering collection item for header '{}': {}", header, itemEx.getMessage(), itemEx);
						return "[Error: " + (item != null ? item.getClass().getSimpleName() : "null") + "]";
					}
				}).collect(Collectors.joining(", "));
			} catch (final Exception e) {
				LOGGER.error("Error rendering collection column for header '{}' on entity ID {}: {}", header,
						entity != null ? entity.toString() : "null", e.getMessage(), e);
				return "[Error rendering collection]";
			}
		};
		final Column<EntityClass> column = addColumn(namesProvider).setAutoWidth(true).setSortable(false).setResizable(true).setFlexGrow(1);
		Check.notNull(column, "Column creation failed for header: " + header);
		LOGGER.debug("Successfully created collection column for header: {}", header);
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addColumnEntityNamed(final ValueProvider<EntityClass, ? extends CEntityDB<?>> valueProvider, final String header) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final ValueProvider<EntityClass, String> nameProvider = entity -> {
			final CEntityDB<?> ref = valueProvider.apply(entity);
			return ref == null ? "" : entityName(ref);
		};
		final Column<EntityClass> column =
				addColumn(nameProvider).setWidth(WIDTH_REFERENCE).setFlexGrow(0).setSortable(true).setResizable(true).setComparator(nameProvider);
		Check.notNull(column, "Column creation failed for header: " + header);
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addCustomColumn(final ValueProvider<EntityClass, ?> valueProvider, final String header, final String width,
			final String key, final int flexGrow) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		Check.notBlank(width, "Width cannot be null or blank");
		Check.isTrue(flexGrow >= 0, "Flex grow must be non-negative");
		final Column<EntityClass> column = addColumn(valueProvider).setWidth(width).setFlexGrow(flexGrow).setSortable(true).setResizable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addDateColumn(final ValueProvider<EntityClass, LocalDate> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<EntityClass> addDateTimeColumn(final ValueProvider<EntityClass, LocalDateTime> valueProvider, final String header,
			final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<EntityClass> addDecimalColumn(final ValueProvider<EntityClass, BigDecimal> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_DECIMAL, key, 0);
	}

	/** Adds an editable image column using CPictureSelector in icon mode. Clicking on the profile picture opens a dialog for editing.
	 * @param imageDataProvider Provider that returns byte array of image data
	 * @param imageDataSetter   Setter function to update the image data when changed
	 * @param header            Column header text
	 * @param fieldDisplayName  Display name for the field (used in edit dialog)
	 * @return The created column */
	public Column<EntityClass> addEditableImageColumn(final ValueProvider<EntityClass, byte[]> imageDataProvider,
			final java.util.function.BiConsumer<EntityClass, byte[]> imageDataSetter, final String header, final String fieldDisplayName) {
		final Column<EntityClass> column = addComponentColumn(entity -> {
			// Create field info for the picture selector
			final EntityFieldInfo fieldInfo = new EntityFieldInfo();
			fieldInfo.setFieldName("imageData");
			fieldInfo.setDisplayName(fieldDisplayName);
			fieldInfo.setDescription("Click to edit " + fieldDisplayName.toLowerCase());
			fieldInfo.setImageData(true);
			fieldInfo.setWidth("40px");
			fieldInfo.setReadOnly(false);
			// Create CPictureSelector in icon mode
			final CPictureSelector selector = new CPictureSelector(fieldInfo, true);
			// Set current value
			final byte[] imageData = imageDataProvider.apply(entity);
			selector.setValue(imageData);
			// Add value change listener to update the entity
			selector.addValueChangeListener(event -> {
				if (imageDataSetter != null) {
					imageDataSetter.accept(entity, event.getValue());
				}
			});
			return selector;
		}).setWidth(WIDTH_IMAGE).setFlexGrow(0).setSortable(false);
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addEntityColumn(final ValueProvider<EntityClass, ?> valueProvider, final String header, final String key,
			final Class<?> returnType) throws Exception {
		try {
			Check.notNull(valueProvider, "Value provider cannot be null");
			Check.notBlank(header, "Header cannot be null or blank");
			Field field;
			// final Method applyMethod = valueProvider.getClass().getMethod("apply", Object.class);
			// final Class<?> returnType = applyMethod.getReturnType();
			field = CEntityFieldService.getEntityField(clazz, key);
			// field = CEntityFieldService.getEntityField(clazz, key);
			final AMetaData meta = field.getAnnotation(AMetaData.class);
			Check.notNull(meta, "AMetaData annotation is missing on field: " + key + " in class: " + clazz.getSimpleName());
			final String width = getColumnWidth(field, meta);
			final Column<EntityClass> column = addComponentColumn(item -> {
				final CLabelEntity labelEntity = new CLabelEntity();
				try {
					final Object value = valueProvider.apply(item);
					if (value instanceof CEntityDB) {
						labelEntity.setValue((CEntityDB<?>) value, true);
					} else {
						labelEntity.setText(value != null ? value.toString() : "");
					}
				} catch (final Exception e) {
					LOGGER.error("Error setting entity column value: {}", e.getMessage());
					labelEntity.setText("Error");
				}
				return labelEntity;
			}).setWidth(width).setFlexGrow(0).setSortable(true).setResizable(true);
			return styleColumnHeader(column, header);
		} catch (final Exception e) {
			LOGGER.error("Error adding entity column for header: {}: {}, key: {} clazz:", header, e.getMessage(), key, clazz.getSimpleName());
			throw e;
		}
	}

	/** Adds a long text column that expands to fill available space (flexGrow = 1).
	 * @param valueProvider Value provider for the column
	 * @param header        Column header text
	 * @param key           Column key for identification
	 * @return The created column */
	public Column<EntityClass> addExpandingLongTextColumn(final ValueProvider<EntityClass, String> valueProvider, final String header,
			final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(valueProvider).setFlexGrow(1).setSortable(true).setResizable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return styleColumnHeader(column, header);
	}

	/** Adds a short text column that expands to fill available space (flexGrow = 1).
	 * @param valueProvider Value provider for the column
	 * @param header        Column header text
	 * @param key           Column key for identification
	 * @return The created column */
	public Column<EntityClass> addExpandingShortTextColumn(final ValueProvider<EntityClass, String> valueProvider, final String header,
			final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(valueProvider).setFlexGrow(1).setSortable(true).setResizable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addIdColumn(final ValueProvider<EntityClass, ?> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_ID, key, 0);
	}

	/** Adds an image column with circular styling for profile pictures.
	 * @param imageDataProvider Provider that returns byte array of image data
	 * @param header            Column header text
	 * @return The created column */
	public Column<EntityClass> addImageColumn(final ValueProvider<EntityClass, byte[]> imageDataProvider, final String header) {
		final Column<EntityClass> column = addComponentColumn(entity -> {
			final byte[] imageData = imageDataProvider.apply(entity);
			final Image image = new Image();
			image.setWidth("40px");
			image.setHeight("40px");
			image.getStyle().set("border-radius", "50%");
			image.getStyle().set("object-fit", "cover");
			if (imageData != null && imageData.length > 0) {
				final String dataUrl = CImageUtils.createDataUrl(imageData);
				if (dataUrl != null) {
					image.setSrc(dataUrl);
				} else {
					image.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
				}
			} else {
				image.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
			}
			return image;
		}).setWidth(WIDTH_IMAGE).setFlexGrow(0).setSortable(false).setResizable(true);
		return styleColumnHeader(column, header);
	}

	public Column<EntityClass> addIntegerColumn(final ValueProvider<EntityClass, Integer> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_INTEGER, key, 0);
	}

	public Column<EntityClass> addLongTextColumn(final ValueProvider<EntityClass, String> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_LONG_TEXT, key, 0);
	}

	public Column<EntityClass> addReferenceColumn(final ValueProvider<EntityClass, String> valueProvider, final String header) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_REFERENCE, null, 0);
	}

	public Column<EntityClass> addShortTextColumn(final ValueProvider<EntityClass, String> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_SHORT_TEXT, key, 0);
	}

	public Column<EntityClass> addTimeColumn(final ValueProvider<EntityClass, LocalDateTime> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	/** Adds a widget column to the grid (non-sortable by default).
	 * @param <T>            the widget type (must be a Component)
	 * @param widgetProvider the function that creates widgets for entities
	 * @return the created column */
	public <T extends Component> Column<EntityClass> addWidgetColumn(final Function<EntityClass, T> widgetProvider) {
		return addWidgetColumn(widgetProvider, null);
	}

	/** Adds a widget column to the grid using the provided widget provider.
	 * <p>
	 * Widget columns support sorting when a comparator is provided. The comparator extracts a comparable value from the entity for sorting purposes.
	 * </p>
	 * @param <T>            the widget type (must be a Component)
	 * @param widgetProvider the function that creates widgets for entities
	 * @param comparator     optional comparator for sorting (null to disable sorting)
	 * @return the created column
	 * @see tech.derbent.api.grid.widget.IComponentWidgetEntityProvider
	 * @see tech.derbent.api.grid.widget.CComponentWidgetEntity */
	public <T extends Component> Column<EntityClass> addWidgetColumn(final Function<EntityClass, T> widgetProvider,
			final Comparator<EntityClass> comparator) {
		Check.notNull(widgetProvider, "Widget provider cannot be null");
		// Generate a unique key for this column
		final String columnKey = "widget_column_" + widgetProviders.size();
		final Column<EntityClass> column = addComponentColumn(entity -> {
			try {
				Check.notNull(entity, "Entity cannot be null when creating widget");
				final T widget = widgetProvider.apply(entity);
				if (widget == null) {
					LOGGER.warn("Widget provider returned null for entity: {}", entity);
					final CLabelEntity labelEntity = new CLabelEntity();
					labelEntity.setText("Widget Error");
					return labelEntity;
				}
				return widget;
			} catch (final Exception e) {
				LOGGER.error("Error creating widget for entity: {}", e.getMessage());
				final CLabelEntity labelEntity = new CLabelEntity();
				labelEntity.setText("Error: " + e.getMessage());
				return labelEntity;
			}
		}).setAutoWidth(true).setFlexGrow(1).setKey(columnKey);
		// Store the widget provider for state management
		widgetProviders.put(columnKey, widgetProvider);
		if (comparator != null) {
			column.setSortable(true).setComparator(comparator);
		} else {
			column.setSortable(false);
		}
		return column;
	}

	@Override
	public void clearStateInformation() {
		LOGGER.debug("[StateOwner] Clearing grid state information");
		try {
			// Clear state from child components (widgets)
			final List<EntityClass> items = getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
			for (final EntityClass item : items) {
				for (final Map.Entry<String, Function<EntityClass, ? extends Component>> entry : widgetProviders.entrySet()) {
					try {
						final Component widget = entry.getValue().apply(item);
						if (widget instanceof IStateOwnerComponent) {
							((IStateOwnerComponent) widget).clearStateInformation();
						}
					} catch (final Exception e) {
						LOGGER.debug("[StateOwner] Error clearing state for widget: {}", e.getMessage());
					}
				}
			}
			LOGGER.debug("[StateOwner] Grid state cleared successfully");
		} catch (final Exception e) {
			LOGGER.debug("[StateOwner] Error clearing grid state: {}", e.getMessage());
		}
	}

	/** Ensures that the grid has a selected row when data is available. This method is called automatically when data changes and follows the coding
	 * guideline that grids should always have a selected row. */
	public void ensureSelectionWhenDataAvailable() {
		try {
			LOGGER.debug("Ensuring selection when data is available");
			// Only auto-select if no current selection and data is available
			if (asSingleSelect().getValue() == null) {
				getDataProvider().fetch(new Query<>()).findFirst().ifPresent(entity -> {
					LOGGER.debug("Auto-selecting first entity: {}", entity.toString());
					select(entity);
				});
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not auto-select first row: {}", e.getMessage());
		}
	}

	private String getColumnWidth(Field field, final AMetaData meta) {
		String width;
		switch (field.getType().getSimpleName()) {
		case "Integer":
		case "int":
			width = WIDTH_INTEGER;
			break;
		case "BigDecimal":
			width = WIDTH_DECIMAL;
			break;
		case "LocalDate":
		case "LocalDateTime":
			width = WIDTH_DATE;
			break;
		case "Boolean":
		case "boolean":
			width = WIDTH_BOOLEAN;
			break;
		case "String":
			if (meta != null && meta.maxLength() > CEntityConstants.MAX_LENGTH_NAME) {
				width = WIDTH_LONG_TEXT;
			} else {
				width = WIDTH_SHORT_TEXT;
			}
			break;
		default:
			width = WIDTH_SHORT_TEXT;
		}
		return width;
	}

	@Override
	public List<ComponentEventListener<CDragEndEvent>> getDragEndListeners() { return dragEndListeners; }
	// ==================== IHasDragStart, IHasDragEnd Implementation ====================

	@Override
	public List<ComponentEventListener<CDragStartEvent<?>>> getDragStartListeners() { return dragStartListeners; }

	@Override
	public List<ComponentEventListener<CDropEvent<?>>> getDropListeners() { return dropListeners; }
	// ==================== IStateOwnerComponent Implementation ====================

	@Override
	public JsonObject getStateInformation() {
		final JsonObject state = saveGridState();
		// Collect state from child components (widget columns)
		final JsonArray childStates = Json.createArray();
		int childIndex = 0;
		try {
			LOGGER.debug("[StateOwner] Collecting state from grid columns and rows...");
			// Get all items currently in the grid
			final List<EntityClass> items = getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
			LOGGER.debug("[StateOwner] Processing {} rows for state collection", items.size());
			// Iterate through each row (item)
			for (int rowIndex = 0; rowIndex < items.size(); rowIndex++) {
				final EntityClass item = items.get(rowIndex);
				final Long itemId = item instanceof CEntityDB ? ((CEntityDB<?>) item).getId() : null;
				LOGGER.debug("[StateOwner] Processing row {}: item ID {}", rowIndex, itemId);
				// Iterate through columns that have widget providers
				for (final Map.Entry<String, Function<EntityClass, ? extends Component>> entry : widgetProviders.entrySet()) {
					final String columnKey = entry.getKey();
					final Function<EntityClass, ? extends Component> widgetProvider = entry.getValue();
					try {
						LOGGER.debug("[StateOwner]   Checking column '{}' for IStateOwnerComponent implementation", columnKey);
						// Create the widget for this item using the provider
						final Component widget = widgetProvider.apply(item);
						if (widget instanceof IStateOwnerComponent) {
							LOGGER.debug("[StateOwner]   Widget implements IStateOwnerComponent, collecting state");
							final IStateOwnerComponent stateOwner = (IStateOwnerComponent) widget;
							final JsonObject widgetState = stateOwner.getStateInformation();
							// Add metadata to identify which row/column this state belongs to
							widgetState.put("rowIndex", rowIndex);
							if (itemId != null) {
								widgetState.put("itemId", itemId.doubleValue());
							}
							widgetState.put("columnKey", columnKey);
							childStates.set(childIndex++, widgetState);
							LOGGER.debug("[StateOwner]   Collected state from widget at row {} column '{}'", rowIndex, columnKey);
						} else {
							LOGGER.debug("[StateOwner]   Widget does not implement IStateOwnerComponent, skipping");
						}
					} catch (final Exception e) {
						LOGGER.debug("[StateOwner]   Error collecting state from column '{}' at row {}: {}", columnKey, rowIndex, e.getMessage());
					}
				}
			}
			if (childStates.length() > 0) {
				state.put("childStates", childStates);
				LOGGER.debug("[StateOwner] Collected state from {} child components", childStates.length());
			} else {
				LOGGER.debug("[StateOwner] No child component states collected");
			}
		} catch (final Exception e) {
			LOGGER.error("[StateOwner] Error collecting child component states: {}", e.getMessage(), e);
		}
		return state;
	}

	/** Initialize grid with common settings and styling. */
	private void initializeGrid() {
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		addThemeVariants(GridVariant.LUMO_COMPACT);
		setItems(Collections.emptyList());
		getColumns().forEach(this::removeColumn);
		setHeightFull();
		CAuxillaries.setId(this);
		getDataProvider().addDataProviderListener(e -> {
			ensureSelectionWhenDataAvailable();
		});
		final GridSingleSelectionModel<EntityClass> sm = (GridSingleSelectionModel<EntityClass>) getSelectionModel();
		sm.setDeselectAllowed(false);
		// Forward Grid's internal drag-drop events to IHasDragControl listeners
		setupChildDragDropForwarding();
	}

	@Override
	public boolean isDragEnabled() { return dragEnabled; }

	@Override
	public boolean isDropEnabled() { return dropEnabled; }

	/** Restores the grid state from a JSON object.
	 * @param state The JSON object containing the saved state */
	private void restoreGridState(final JsonObject state) {
		if (state == null) {
			LOGGER.debug("[StateOwner] No grid state to restore");
			return;
		}
		try {
			// Restore selected item by ID
			if (state.hasKey("selectedItemId")) {
				final double selectedIdDouble = state.getNumber("selectedItemId");
				final Long selectedId = (long) selectedIdDouble;
				LOGGER.debug("[StateOwner] Attempting to restore selected item ID: {}", selectedId);
				// Find and select the item with matching ID
				getDataProvider().fetch(new Query<>()).filter(item -> {
					if (item instanceof CEntityDB) {
						final Long itemId = ((CEntityDB<?>) item).getId();
						return itemId != null && itemId.equals(selectedId);
					}
					return false;
				}).findFirst().ifPresent(item -> {
					select(item);
					LOGGER.debug("[StateOwner] Restored selection to item ID: {}", selectedId);
				});
			}
			LOGGER.debug("[StateOwner] Grid state restored successfully");
		} catch (final Exception e) {
			LOGGER.error("[StateOwner] Error restoring grid state: {}", e.getMessage(), e);
		}
	}
	// ========== IHasDragControl interface implementation ==========

	@Override
	public void restoreStateInformation(final JsonObject state) {
		if (state == null) {
			LOGGER.debug("[StateOwner] No state to restore");
			return;
		}
		// Restore grid state
		restoreGridState(state);
		// Restore child component states
		if (state.hasKey("childStates")) {
			try {
				final JsonArray childStates = state.getArray("childStates");
				LOGGER.debug("[StateOwner] Restoring state for {} child components...", childStates.length());
				// Get all items currently in the grid
				final List<EntityClass> items = getDataProvider().fetch(new Query<>()).collect(Collectors.toList());
				for (int i = 0; i < childStates.length(); i++) {
					final JsonObject childState = childStates.getObject(i);
					try {
						// Extract metadata to identify which widget this state belongs to
						final int rowIndex = childState.hasKey("rowIndex") ? (int) childState.getNumber("rowIndex") : -1;
						final String columnKey = childState.hasKey("columnKey") ? childState.getString("columnKey") : null;
						final Long itemId = childState.hasKey("itemId") ? (long) childState.getNumber("itemId") : null;
						LOGGER.debug("[StateOwner] Processing child state {}: rowIndex={}, columnKey={}, itemId={}", i, rowIndex, columnKey, itemId);
						if (columnKey != null && widgetProviders.containsKey(columnKey)) {
							// Find the matching item
							EntityClass targetItem = null;
							if (itemId != null && rowIndex >= 0 && rowIndex < items.size()) {
								final EntityClass item = items.get(rowIndex);
								if (item instanceof CEntityDB) {
									final Long currentItemId = ((CEntityDB<?>) item).getId();
									if (itemId.equals(currentItemId)) {
										targetItem = item;
									}
								}
							}
							if (targetItem != null) {
								// Create the widget for this item
								final Function<EntityClass, ? extends Component> widgetProvider = widgetProviders.get(columnKey);
								final Component widget = widgetProvider.apply(targetItem);
								if (widget instanceof IStateOwnerComponent) {
									LOGGER.debug("[StateOwner] Restoring state to widget at row {} column '{}'", rowIndex, columnKey);
									((IStateOwnerComponent) widget).restoreStateInformation(childState);
								} else {
									LOGGER.debug("[StateOwner] Widget at row {} column '{}' does not implement IStateOwnerComponent", rowIndex,
											columnKey);
								}
							} else {
								LOGGER.debug("[StateOwner] Could not find matching item for child state {} (itemId={}, rowIndex={})", i, itemId,
										rowIndex);
							}
						} else {
							LOGGER.debug("[StateOwner] Column key '{}' not found in widget providers", columnKey);
						}
					} catch (final Exception e) {
						LOGGER.error("[StateOwner] Error restoring child state at index {}: {}", i, e.getMessage(), e);
					}
				}
			} catch (final Exception e) {
				LOGGER.error("[StateOwner] Error restoring child component states: {}", e.getMessage(), e);
			}
		}
	}

	/** Saves the current grid state including selected item and scroll position.
	 * @return JsonObject containing the grid state */
	private JsonObject saveGridState() {
		final JsonObject state = Json.createObject();
		try {
			// Save selected item ID if entity has getId()
			final EntityClass selectedItem = asSingleSelect().getValue();
			if (selectedItem != null) {
				if (selectedItem instanceof CEntityDB) {
					final Long id = ((CEntityDB<?>) selectedItem).getId();
					if (id != null) {
						state.put("selectedItemId", id.doubleValue());
						LOGGER.debug("[StateOwner] Saved selected item ID: {}", id);
					}
				} else {
					// For non-entity items, try to use toString() or index
					LOGGER.debug("[StateOwner] Selected item is not CEntityDB, cannot save ID");
				}
			}
			// Note: Vaadin Grid does not expose scroll position API directly
			// We save what we can - the selected item will help restore visible area
			LOGGER.debug("[StateOwner] Grid state saved successfully");
		} catch (final Exception e) {
			LOGGER.error("[StateOwner] Error saving grid state: {}", e.getMessage(), e);
		}
		return state;
	}

	@Override
	public void select(final EntityClass entity) {
		if (entity == null) {
			LOGGER.debug("Cannot select null entity, skipping.");
			return;
		}
		LOGGER.debug("Selecting entity: {}", entity != null ? entity.toString() : "null");
		if (entity == getSelectedItems().stream().findFirst().orElse(null)) {
			// LOGGER.debug("Entity is already selected, skipping.");
			return;
		}
		super.select(entity);
	}

	@SuppressWarnings ("unchecked")
	public void setClazz(Class<?> class1) { clazz = (Class<EntityClass>) class1; }

	@Override
	public void setDragEnabled(final boolean enabled) {
		dragEnabled = enabled;
		setRowsDraggable(enabled);
		LOGGER.debug("[DragDebug] CGrid: Drag {} for grid", enabled ? "enabled" : "disabled");
	}

	@Override
	public void setDropEnabled(final boolean enabled) {
		dropEnabled = enabled;
		if (enabled) {
			setDropMode(com.vaadin.flow.component.grid.dnd.GridDropMode.BETWEEN);
		} else {
			setDropMode(null);
		}
		LOGGER.debug("[DragDebug] CGrid: Drop {} for grid", enabled ? "enabled" : "disabled");
	}

	public void setDynamicHeight() {
		setSizeUndefined();
		getStyle().set("height", "auto");
		setMinHeight("60px");
		setWidthFull();
		setAllRowsVisible(true);
	}
}
