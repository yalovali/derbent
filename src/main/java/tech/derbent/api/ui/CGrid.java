package tech.derbent.api.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSingleSelectionModel;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.function.ValueProvider;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.CGridCell;
import tech.derbent.api.ui.component.CPictureSelector;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.api.utils.Check;

/** CGrid - Base grid class for consistent field width management based on data types. Layer: View (MVC) Follows the project's coding guidelines by
 * providing a base class for all grids to ensure consistent column widths based on field types: - ID fields: Very small width (80px) - Integer
 * fields: Small width (100px) - BigDecimal fields: Medium width (120px) - Date fields: Medium width (150px) - Boolean/Status fields: Small-Medium
 * width (100px) - Short text fields: Medium width (200px) - Long text fields: Large width (300px+) - Reference fields: Medium width (200px) */
// public class CGrid<EntityClass extends CEntityDB<EntityClass>> extends Grid<EntityClass> {
public class CGrid<EntityClass> extends Grid<EntityClass> {

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
	// private static <T> void preventDeselection(final Grid<T> grid) {
	// final AtomicReference<T> lastSelection = new AtomicReference<>();
	// grid.asSingleSelect().addValueChangeListener(event -> {
	// // If selection is being cleared and we had a previous selection, restore it
	// if (event.getValue() == null && lastSelection.get() != null) {
	// grid.asSingleSelect().setValue(lastSelection.get());
	// } else {
	// lastSelection.set(event.getValue());
	// }
	// });
	// }

	public static <T> void setupGrid(final Grid<T> grid) {
		Check.notNull(grid, "Grid cannot be null when setting up relational component");
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		GridSingleSelectionModel<T> sm = (GridSingleSelectionModel<T>) grid.getSelectionModel();
		sm.setDeselectAllowed(false);
		grid.getStyle().set("border-radius", "8px");
		grid.getStyle().set("border", "1px solid #E0E0E0");
		grid.setWidthFull();
		CAuxillaries.setId(grid);
		// Prevent deselection when clicking on already-selected item
		// preventDeselection(grid);
	}

	/** Constructor for CGrid with entity class.
	 * @param entityClass The entity class for the grid */
	Class<EntityClass> clazz;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected boolean showIconInStatusColumns = true;

	public CGrid(final Class<EntityClass> entityClass) {
		super(entityClass, false);
		clazz = entityClass;
		initializeGrid();
	}

	public Column<EntityClass> addBooleanColumn(final ValueProvider<EntityClass, Boolean> valueProvider, final String header, final String trueText,
			final String falseText) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		Check.notBlank(trueText, "True text cannot be null or blank");
		Check.notBlank(falseText, "False text cannot be null or blank");
		final Column<EntityClass> column = addColumn(entity -> {
			final Boolean value = valueProvider.apply(entity);
			return (value != null) && value ? trueText : falseText;
		}).setHeader(header).setWidth(WIDTH_BOOLEAN).setFlexGrow(0).setSortable(true);
		return column;
	}

	public Column<EntityClass> addColumn(final ValueProvider<EntityClass, String> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(valueProvider).setHeader(header).setFlexGrow(1).setSortable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	public Column<EntityClass> addColumnByProperty(final String propertyName, final String header) {
		Check.notBlank(propertyName, "Property name cannot be null or blank");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addColumn(propertyName).setHeader(header).setSortable(true);
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
		return column;
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
							return ((name != null) && !name.isBlank()) ? name : "Entity#" + ref.getId();
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
		final Column<EntityClass> column = addColumn(namesProvider).setHeader(header).setAutoWidth(true).setSortable(false) // usually collection
																															// columns aren't sortable
				.setFlexGrow(1);
		Check.notNull(column, "Column creation failed for header: " + header);
		LOGGER.debug("Successfully created collection column for header: {}", header);
		return column;
	}

	public Column<EntityClass> addColumnEntityNamed(final ValueProvider<EntityClass, ? extends CEntityDB<?>> valueProvider, final String header) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		// one place to compute the display name
		final ValueProvider<EntityClass, String> nameProvider = entity -> {
			final CEntityDB<?> ref = valueProvider.apply(entity);
			return ref == null ? "" : entityName(ref);
		};
		final Column<EntityClass> column = addColumn(nameProvider).setHeader(header).setWidth(WIDTH_REFERENCE).setFlexGrow(0).setSortable(true)
				// ensure server-side sorting by the shown name
				.setComparator(nameProvider);
		Check.notNull(column, "Column creation failed for header: " + header);
		return column;
	}

	public Column<EntityClass> addCustomColumn(final ValueProvider<EntityClass, ?> valueProvider, final String header, final String width,
			final String key, final int flexGrow) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		Check.notBlank(width, "Width cannot be null or blank");
		Check.isTrue(flexGrow >= 0, "Flex grow must be non-negative");
		final Column<EntityClass> column = addColumn(valueProvider).setHeader(header).setWidth(width).setFlexGrow(flexGrow).setSortable(true);
		Check.notNull(column, "Column creation failed for header: " + header);
		if (key != null) {
			column.setKey(key);
		}
		return column;
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
			EntityFieldInfo fieldInfo = new EntityFieldInfo();
			fieldInfo.setFieldName("imageData");
			fieldInfo.setDisplayName(fieldDisplayName);
			fieldInfo.setDescription("Click to edit " + fieldDisplayName.toLowerCase());
			fieldInfo.setImageData(true);
			fieldInfo.setWidth("40px");
			fieldInfo.setReadOnly(false);
			// Create CPictureSelector in icon mode
			CPictureSelector selector = new CPictureSelector(fieldInfo, true);
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
		}).setHeader(header).setWidth(WIDTH_IMAGE).setFlexGrow(0).setSortable(false);
		return column;
	}

	@SuppressWarnings ("unchecked")
	public Column<EntityClass> addEntityColumn(final ValueProvider<EntityClass, ?> valueProvider, final String header, final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		Field field;
		try {
			field = CEntityFieldService.getEntityField(clazz, key);
		} catch (final NoSuchFieldException e) {
			LOGGER.warn("Field not found: {} in class {}", key, clazz.getSimpleName(), e);
			return addShortTextColumn((ValueProvider<EntityClass, String>) valueProvider, header, key);
		}
		final AMetaData meta = field.getAnnotation(AMetaData.class);
		Check.notNull(meta, "AMetaData annotation is missing on field: " + key + " in class: " + clazz.getSimpleName());
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
			if ((meta != null) && (meta.maxLength() > CEntityConstants.MAX_LENGTH_NAME)) {
				width = WIDTH_LONG_TEXT;
			} else {
				width = WIDTH_SHORT_TEXT;
			}
			break;
		default:
			width = WIDTH_SHORT_TEXT;
		}
		// Renkli hücre gerekiyorsa component ile çiz
		if ((meta != null) && meta.setBackgroundFromColor()) {
			return addComponentColumn(item -> {
				final Object value = valueProvider.apply(item);
				final CGridCell cell = new CGridCell();
				// Enable icon display if configured in metadata
				if (meta.useIcon()) {
					cell.setShowIcon(true);
				}
				// Set the value which will handle color rendering if entity has color
				if (value instanceof CEntityDB) {
					cell.setEntityValue((CEntityDB<?>) value);
				} else {
					cell.setText(value != null ? value.toString() : "");
				}
				return cell;
			}).setHeader(header).setWidth(width).setFlexGrow(0).setSortable(true);
		}
		// Normal sütun
		return addCustomColumn(valueProvider, header, width, key, 0);
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
			if ((imageData != null) && (imageData.length > 0)) {
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
		}).setHeader(header).setWidth(WIDTH_IMAGE).setFlexGrow(0).setSortable(false);
		return column;
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

	public <S extends CEntityDB<S>> Column<EntityClass> addStatusColumn(final ValueProvider<EntityClass, S> valueProvider, final String header,
			final String key) {
		Check.notNull(valueProvider, "Value provider cannot be null");
		Check.notBlank(header, "Header cannot be null or blank");
		final Column<EntityClass> column = addComponentColumn(entity -> {
			final S status = valueProvider.apply(entity);
			final CGridCell statusCell = new CGridCell();
			// Configure icon display based on grid setting
			statusCell.setShowIcon(showIconInStatusColumns);
			statusCell.setStatusValue(status);
			return statusCell;
		}).setHeader(header).setWidth(WIDTH_REFERENCE).setFlexGrow(0).setSortable(true);
		if (key != null) {
			column.setKey(key);
		}
		return column;
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

	/** Initialize grid with common settings and styling. */
	private void initializeGrid() {
		addThemeVariants(GridVariant.LUMO_NO_BORDER);
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		addThemeVariants(GridVariant.LUMO_COMPACT);
		setItems(Collections.emptyList());
		getColumns().forEach(this::removeColumn);
		setHeightFull();
		CAuxillaries.setId(this);
		// Prevent deselection when clicking on already-selected item
		// preventDeselection(this);
		// Ensure grid always has a selected row when data is available
		getDataProvider().addDataProviderListener(e -> {
			ensureSelectionWhenDataAvailable();
		});
		GridSingleSelectionModel<EntityClass> sm = (GridSingleSelectionModel<EntityClass>) getSelectionModel();
		sm.setDeselectAllowed(false);
	}

	public boolean isShowIconInStatusColumns() { return showIconInStatusColumns; }

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

	public void setShowIconInStatusColumns(final boolean showIconInStatusColumns) { this.showIconInStatusColumns = showIconInStatusColumns; }
}
