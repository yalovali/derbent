package tech.derbent.abstracts.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.function.ValueProvider;

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
public class CGrid<T extends CEntityDB> extends Grid<T> {

	private static final long serialVersionUID = 1L;

	// Width constants for different field types
	public static final String WIDTH_ID = "80px";

	public static final String WIDTH_INTEGER = "100px";

	public static final String WIDTH_DECIMAL = "120px";

	public static final String WIDTH_DATE = "150px";

	public static final String WIDTH_BOOLEAN = "100px";

	public static final String WIDTH_SHORT_TEXT = "200px";

	public static final String WIDTH_LONG_TEXT = "300px";

	public static final String WIDTH_REFERENCE = "200px";

	/** Width for image columns (profile pictures, etc.) */
	public static final String WIDTH_IMAGE = "60px";

	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor for CGrid with entity class.
	 * @param beanType The entity class for the grid
	 */
	public CGrid(final Class<T> beanType) {
		super(beanType, false);
		LOGGER.info("CGrid constructor called for entity type: {}",
			beanType.getSimpleName());
		initializeGrid();
	}

	/**
	 * Constructor for CGrid with entity class and automatic column creation.
	 * @param beanType          The entity class for the grid
	 * @param autoCreateColumns Whether to automatically create columns
	 */
	public CGrid(final Class<T> beanType, final boolean autoCreateColumns) {
		super(beanType, autoCreateColumns);
		LOGGER.info(
			"CGrid constructor called for entity type: {} with autoCreateColumns: {}",
			beanType.getSimpleName(), autoCreateColumns);
		initializeGrid();
	}

	public Column<T> addBooleanColumn(final ValueProvider<T, Boolean> valueProvider,
		final String header, final String trueText, final String falseText) {
		LOGGER.info("Adding boolean column: {} with width: {}", header, WIDTH_BOOLEAN);
		final Column<T> column = addColumn(entity -> {
			final Boolean value = valueProvider.apply(entity);
			return (value != null) && value ? trueText : falseText;
		}).setHeader(header).setWidth(WIDTH_BOOLEAN).setFlexGrow(0).setSortable(true);
		return column;
	}

	public Column<T> addColumn(final ValueProvider<T, String> valueProvider,
		final String header, final String key) {
		// flexglow set to 1 to allow the column to grow and fill available space
		final Column<T> column =
			addColumn(valueProvider).setHeader(header).setFlexGrow(1).setSortable(true);

		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	public Column<T> addColumnByProperty(final String propertyName, final String header) {
		LOGGER.info("Adding column by property: {} with header: {}", propertyName,
			header);
		final Column<T> column =
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

	public Column<T> addCustomColumn(final ValueProvider<T, ?> valueProvider,
		final String header, final String width, final String key, final int flexGrow) {
		LOGGER.info("Adding custom column: {} with width: {} and flexGrow: {}", header,
			width, flexGrow);
		final Column<T> column = addColumn(valueProvider).setHeader(header)
			.setWidth(width).setFlexGrow(flexGrow).setSortable(true);

		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	public Column<T> addDateColumn(final ValueProvider<T, LocalDate> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<T> addDateTimeColumn(
		final ValueProvider<T, LocalDateTime> valueProvider, final String header,
		final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DATE, key, 0);
	}

	public Column<T> addDecimalColumn(final ValueProvider<T, BigDecimal> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_DECIMAL, key, 0);
	}

	public Column<T> addIdColumn(final ValueProvider<T, ?> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_ID, key, 0);
	}

	/**
	 * Adds an image column with circular styling for profile pictures.
	 * @param imageDataProvider Provider that returns byte array of image data
	 * @param header            Column header text
	 * @return The created column
	 */
	public Column<T> addImageColumn(final ValueProvider<T, byte[]> imageDataProvider,
		final String header) {
		LOGGER.info("Adding image column: {} with width: {}", header, WIDTH_IMAGE);
		final Column<T> column = addComponentColumn(entity -> {
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

	public Column<T> addIntegerColumn(final ValueProvider<T, Integer> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_INTEGER, key, 0);
	}

	public Column<T> addLongTextColumn(final ValueProvider<T, String> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_LONG_TEXT, key, 0);
	}

	public Column<T> addReferenceColumn(final ValueProvider<T, String> valueProvider,
		final String header) {
		return addCustomColumn(valueProvider, header, WIDTH_REFERENCE, null, 0);
	}

	public Column<T> addShortTextColumn(final ValueProvider<T, String> valueProvider,
		final String header, final String key) {
		return addCustomColumn(valueProvider, header, WIDTH_SHORT_TEXT, key, 0);
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

	@Override
	public void select(final T entity) {
		LOGGER.info("Selecting entity in grip: {}", entity);
		super.select(entity);
	}
}