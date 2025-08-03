package tech.derbent.abstracts.views;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.function.ValueProvider;

import tech.derbent.abstracts.annotations.MetaData;
import tech.derbent.abstracts.components.CGridCell;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CAuxillaries;
import tech.derbent.base.utils.CImageUtils;

/**
 * CGrid - Base grid class for consistent field width management based on data types.
 * Layer: View (MVC) Follows the project's coding guidelines by providing a base class for
 * all grids to ensure consistent column widths based on field types: - ID fields: Very
 * small width (80px) - Integer fields: Small width (100px) - BigDecimal fields: Medium
 * width (120px) - Date fields: Medium width (150px) - Boolean/Status fields: Small-Medium
 * width (100px) - Short text fields: Medium width (200px) - Long text fields: Large width
 * (300px+) - Reference fields: Medium width (200px)
 */
public class CGrid<EntityClass extends CEntityDB<EntityClass>> extends Grid<EntityClass> {

	private static final long serialVersionUID = 1L;

	public static final String WIDTH_ID = "80px";

	public static final String WIDTH_INTEGER = "100px";

	public static final String WIDTH_DECIMAL = "120px";

	public static final String WIDTH_DATE = "150px";

	public static final String WIDTH_BOOLEAN = "100px";

	public static final String WIDTH_SHORT_TEXT = "200px";

	public static final String WIDTH_LONG_TEXT = "300px";

	public static final String WIDTH_REFERENCE = "200px";

	public static final String WIDTH_IMAGE = "60px";

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	protected boolean showIconInStatusColumns = true;

	/**
	 * Constructor for CGrid with entity class.
	 * @param entityClass The entity class for the grid
	 */
	Class<EntityClass> clazz;

	public CGrid(final Class<EntityClass> entityClass) {
		super(entityClass, false);
		clazz = entityClass;
		initializeGrid();
	}

	public Column<EntityClass> addBooleanColumn(
		final ValueProvider<EntityClass, Boolean> valueProvider, final String header,
		final String trueText, final String falseText) {
		final Column<EntityClass> column = addColumn(entity -> {
			final Boolean value = valueProvider.apply(entity);
			return (value != null) && value ? trueText : falseText;
		}).setHeader(header).setWidth(WIDTH_BOOLEAN).setFlexGrow(0).setSortable(true);
		return column;
	}

	public Column<EntityClass> addColumn(
		final ValueProvider<EntityClass, String> valueProvider, final String header,
		final String key) {
		// flexglow set to 1 to allow the column to grow and fill available space
		final Column<EntityClass> column =
			addColumn(valueProvider).setHeader(header).setFlexGrow(1).setSortable(true);

		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	public Column<EntityClass> addColumnByProperty(final String propertyName,
		final String header) {
		LOGGER.info("Adding column by property: {} with header: {}", propertyName,
			header);
		final Column<EntityClass> column =
			addColumn(propertyName).setHeader(header).setSortable(true);

		// Apply width based on property name patterns
		if (propertyName.toLowerCase().contains("id")) {
			column.setWidth(WIDTH_ID).setFlexGrow(0);
		}
		else if (propertyName.toLowerCase().contains("percentage")
			|| propertyName.toLowerCase().contains("progress")) {
			column.setWidth(WIDTH_INTEGER).setFlexGrow(0);
		}
		else if (propertyName.toLowerCase().contains("date")) {
			column.setWidth(WIDTH_DATE).setFlexGrow(0);
		}
		else if (propertyName.toLowerCase().contains("email")
			|| propertyName.toLowerCase().contains("description")) {
			column.setWidth(WIDTH_LONG_TEXT).setFlexGrow(0);
		}
		else {
			// Default to short text width
			column.setWidth(WIDTH_SHORT_TEXT).setFlexGrow(0);
		}
		return column;
	}

	public Column<EntityClass> addCustomColumn(
		final ValueProvider<EntityClass, ?> valueProvider, final String header,
		final String width, final String key, final int flexGrow) {
		final Column<EntityClass> column = addColumn(valueProvider).setHeader(header)
			.setWidth(width).setFlexGrow(flexGrow).setSortable(true);

		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	public Column<EntityClass> addDateColumn(
		final ValueProvider<EntityClass, LocalDate> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<EntityClass> addDateTimeColumn(
		final ValueProvider<EntityClass, LocalDateTime> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<EntityClass> addDecimalColumn(
		final ValueProvider<EntityClass, BigDecimal> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DECIMAL, key, 0);
	}

	@SuppressWarnings ("unchecked")
	public Column<EntityClass> addEntityColumn(
		final ValueProvider<EntityClass, ?> valueProvider, final String header,
		final String key) {
		Field field;

		try {
			field = getFieldRecursive(clazz, key);
		} catch (final RuntimeException e) {
			LOGGER.warn("Field not found: {} in class {}", key, clazz.getSimpleName(), e);
			return addShortTextColumn((ValueProvider<EntityClass, String>) valueProvider,
				header, key);
		}
		final MetaData meta = field.getAnnotation(MetaData.class);
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
			}
			else {
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

				// Set the value which will handle color rendering if entity has color
				if (value instanceof CEntityDB) {
					cell.setEntityValue((CEntityDB<?>) value);
				}
				else {
					cell.setText(value != null ? value.toString() : "");
				}
				return cell;
			}).setHeader(header).setWidth(width).setFlexGrow(0).setSortable(true);
		}
		// Normal sütun
		return addCustomColumn(valueProvider, header, width, key, 0);
	}

	public Column<EntityClass> addIdColumn(
		final ValueProvider<EntityClass, ?> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_ID, key, 0);
	}

	/**
	 * Adds an image column with circular styling for profile pictures.
	 * @param imageDataProvider Provider that returns byte array of image data
	 * @param header            Column header text
	 * @return The created column
	 */
	public Column<EntityClass> addImageColumn(
		final ValueProvider<EntityClass, byte[]> imageDataProvider, final String header) {
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
				}
				else {
					image.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
				}
			}
			else {
				image.setSrc(CImageUtils.getDefaultProfilePictureDataUrl());
			}
			return image;
		}).setHeader(header).setWidth(WIDTH_IMAGE).setFlexGrow(0).setSortable(false);
		return column;
	}

	public Column<EntityClass> addIntegerColumn(
		final ValueProvider<EntityClass, Integer> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_INTEGER, key, 0);
	}

	public Column<EntityClass> addLongTextColumn(
		final ValueProvider<EntityClass, String> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_LONG_TEXT, key, 0);
	}

	public Column<EntityClass> addReferenceColumn(
		final ValueProvider<EntityClass, String> valueProvider, final String header) {
		return addCustomColumn(valueProvider, header, WIDTH_REFERENCE, null, 0);
	}

	public Column<EntityClass> addShortTextColumn(
		final ValueProvider<EntityClass, String> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_SHORT_TEXT, key, 0);
	}

	/**
	 * Adds a status column with color-aware rendering. This method creates a column that
	 * displays status entities with their associated colors as background using
	 * CGridCell.
	 * @param statusProvider Provider that returns the status entity
	 * @param header         Column header text
	 * @param key            Column key for identification
	 * @return The created column
	 */
	public <S extends CEntityDB<S>> Column<EntityClass> addStatusColumn(
		final ValueProvider<EntityClass, S> statusProvider, final String header,
		final String key) {
		final Column<EntityClass> column = addComponentColumn(entity -> {
			final S status = statusProvider.apply(entity);
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

	private Field getFieldRecursive(Class<?> type, final String fieldName) {

		while ((type != null) && (type != Object.class)) {

			try {
				return type.getDeclaredField(fieldName);
			} catch (final NoSuchFieldException e) {
				type = type.getSuperclass(); // bir üst sınıfa bak
			}
		}
		throw new RuntimeException("Field not found: " + fieldName);
	}

	/**
	 * Initialize grid with common settings and styling.
	 */
	private void initializeGrid() {
		addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
		addThemeVariants(GridVariant.LUMO_COMPACT);
		setHeightFull();
		CAuxillaries.setId(this);
	}

	/**
	 * Check if icons are displayed in status columns.
	 * @return true if icons are shown in status columns
	 */
	public boolean isShowIconInStatusColumns() { return showIconInStatusColumns; }

	@Override
	public void select(final EntityClass entity) {
		super.select(entity);
	}

	/**
	 * Enable or disable icon display in status columns. Note: This setting only affects
	 * future status columns created after this call.
	 * @param showIconInStatusColumns true to show icons in status columns
	 */
	public void setShowIconInStatusColumns(final boolean showIconInStatusColumns) {
		this.showIconInStatusColumns = showIconInStatusColumns;
	}
}
