package tech.derbent.abstracts.views;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.function.ValueProvider;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CGrid - Base grid class for consistent field width management based on data types.
 * Layer: View (MVC)
 * 
 * Follows the project's coding guidelines by providing a base class for all grids
 * to ensure consistent column widths based on field types:
 * - ID fields: Very small width (80px)
 * - Integer fields: Small width (100px) 
 * - BigDecimal fields: Medium width (120px)
 * - Date fields: Medium width (150px)
 * - Boolean/Status fields: Small-Medium width (100px)
 * - Short text fields: Medium width (200px)
 * - Long text fields: Large width (300px+)
 * - Reference fields: Medium width (200px)
 */
public class CGrid<T extends CEntityDB> extends Grid<T> {

    private static final long serialVersionUID = 1L;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // Width constants for different field types
    public static final String WIDTH_ID = "80px";
    public static final String WIDTH_INTEGER = "100px";
    public static final String WIDTH_DECIMAL = "120px";
    public static final String WIDTH_DATE = "150px";
    public static final String WIDTH_BOOLEAN = "100px";
    public static final String WIDTH_SHORT_TEXT = "200px";
    public static final String WIDTH_LONG_TEXT = "300px";
    public static final String WIDTH_REFERENCE = "200px";

    /**
     * Constructor for CGrid with entity class.
     * 
     * @param beanType The entity class for the grid
     */
    public CGrid(final Class<T> beanType) {
        super(beanType, false);
        LOGGER.info("CGrid constructor called for entity type: {}", beanType.getSimpleName());
        initializeGrid();
    }

    /**
     * Constructor for CGrid with entity class and automatic column creation.
     * 
     * @param beanType The entity class for the grid
     * @param autoCreateColumns Whether to automatically create columns
     */
    public CGrid(final Class<T> beanType, final boolean autoCreateColumns) {
        super(beanType, autoCreateColumns);
        LOGGER.info("CGrid constructor called for entity type: {} with autoCreateColumns: {}", 
                beanType.getSimpleName(), autoCreateColumns);
        initializeGrid();
    }

    /**
     * Initialize grid with common settings and styling.
     */
    private void initializeGrid() {
        addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        addThemeVariants(GridVariant.LUMO_COMPACT);
        setHeightFull();
    }

    /**
     * Add an ID column with appropriate small width.
     * 
     * @param valueProvider Value provider for the ID field
     * @param header Column header text
     * @param key Column key for sorting
     * @return The created column
     */
    public Column<T> addIdColumn(final ValueProvider<T, ?> valueProvider, final String header, final String key) {
        LOGGER.info("Adding ID column: {} with width: {}", header, WIDTH_ID);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setKey(key)
                .setWidth(WIDTH_ID)
                .setFlexGrow(0)
                .setSortable(true);
        return column;
    }

    /**
     * Add an integer column with appropriate small width.
     * 
     * @param valueProvider Value provider for the integer field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addIntegerColumn(final ValueProvider<T, Integer> valueProvider, final String header, 
            final String key, final boolean sortable) {
        LOGGER.info("Adding integer column: {} with width: {}", header, WIDTH_INTEGER);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_INTEGER)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a BigDecimal column with appropriate medium width.
     * 
     * @param valueProvider Value provider for the BigDecimal field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addDecimalColumn(final ValueProvider<T, BigDecimal> valueProvider, final String header,
            final String key, final boolean sortable) {
        LOGGER.info("Adding decimal column: {} with width: {}", header, WIDTH_DECIMAL);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_DECIMAL)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a date column with appropriate medium width.
     * 
     * @param valueProvider Value provider for the date field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addDateColumn(final ValueProvider<T, LocalDate> valueProvider, final String header,
            final String key, final boolean sortable) {
        LOGGER.info("Adding date column: {} with width: {}", header, WIDTH_DATE);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_DATE)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a datetime column with appropriate medium width.
     * 
     * @param valueProvider Value provider for the datetime field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addDateTimeColumn(final ValueProvider<T, LocalDateTime> valueProvider, final String header,
            final String key, final boolean sortable) {
        LOGGER.info("Adding datetime column: {} with width: {}", header, WIDTH_DATE);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_DATE)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a boolean column with appropriate small width, converting boolean to readable text.
     * 
     * @param valueProvider Value provider for the boolean field
     * @param header Column header text
     * @param trueText Text to display for true values
     * @param falseText Text to display for false values
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addBooleanColumn(final ValueProvider<T, Boolean> valueProvider, final String header,
            final String trueText, final String falseText, final boolean sortable) {
        LOGGER.info("Adding boolean column: {} with width: {}", header, WIDTH_BOOLEAN);
        final Column<T> column = addColumn(entity -> {
            final Boolean value = valueProvider.apply(entity);
            return value != null && value ? trueText : falseText;
        })
                .setHeader(header)
                .setWidth(WIDTH_BOOLEAN)
                .setFlexGrow(0)
                .setSortable(sortable);
        return column;
    }

    /**
     * Add a short text column with appropriate medium width.
     * 
     * @param valueProvider Value provider for the text field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addShortTextColumn(final ValueProvider<T, String> valueProvider, final String header,
            final String key, final boolean sortable) {
        LOGGER.info("Adding short text column: {} with width: {}", header, WIDTH_SHORT_TEXT);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_SHORT_TEXT)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a long text column with appropriate large width.
     * 
     * @param valueProvider Value provider for the text field
     * @param header Column header text
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addLongTextColumn(final ValueProvider<T, String> valueProvider, final String header,
            final String key, final boolean sortable) {
        LOGGER.info("Adding long text column: {} with width: {}", header, WIDTH_LONG_TEXT);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_LONG_TEXT)
                .setFlexGrow(0)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Add a reference column (for related entities) with appropriate medium width.
     * 
     * @param valueProvider Value provider that extracts display text from related entity
     * @param header Column header text
     * @param sortable Whether the column should be sortable (usually false for joins)
     * @return The created column
     */
    public Column<T> addReferenceColumn(final ValueProvider<T, String> valueProvider, final String header,
            final boolean sortable) {
        LOGGER.info("Adding reference column: {} with width: {}", header, WIDTH_REFERENCE);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(WIDTH_REFERENCE)
                .setFlexGrow(0)
                .setSortable(sortable);
        return column;
    }

    /**
     * Add a flexible column with custom width and properties.
     * 
     * @param valueProvider Value provider for the field
     * @param header Column header text
     * @param width Custom width (e.g., "150px")
     * @param key Column key for sorting (can be null)
     * @param sortable Whether the column should be sortable
     * @param flexGrow Flex grow value (0 for fixed width, >0 for flexible)
     * @return The created column
     */
    public Column<T> addCustomColumn(final ValueProvider<T, ?> valueProvider, final String header,
            final String width, final String key, final boolean sortable, final int flexGrow) {
        LOGGER.info("Adding custom column: {} with width: {} and flexGrow: {}", header, width, flexGrow);
        final Column<T> column = addColumn(valueProvider)
                .setHeader(header)
                .setWidth(width)
                .setFlexGrow(flexGrow)
                .setSortable(sortable);
        if (key != null) {
            column.setKey(key);
        }
        return column;
    }

    /**
     * Convenience method to add a column using property name with automatic width based on property type.
     * This method inspects the property type and applies appropriate width.
     * 
     * @param propertyName The property name to create column for
     * @param header Column header text
     * @param sortable Whether the column should be sortable
     * @return The created column
     */
    public Column<T> addColumnByProperty(final String propertyName, final String header, final boolean sortable) {
        LOGGER.info("Adding column by property: {} with header: {}", propertyName, header);
        final Column<T> column = addColumn(propertyName)
                .setHeader(header)
                .setSortable(sortable);
        
        // Apply width based on property name patterns
        if (propertyName.toLowerCase().contains("id")) {
            column.setWidth(WIDTH_ID).setFlexGrow(0);
        } else if (propertyName.toLowerCase().contains("percentage") || 
                   propertyName.toLowerCase().contains("progress")) {
            column.setWidth(WIDTH_INTEGER).setFlexGrow(0);
        } else if (propertyName.toLowerCase().contains("date")) {
            column.setWidth(WIDTH_DATE).setFlexGrow(0);
        } else if (propertyName.toLowerCase().contains("email") || 
                   propertyName.toLowerCase().contains("description")) {
            column.setWidth(WIDTH_LONG_TEXT).setFlexGrow(0);
        } else {
            // Default to short text width
            column.setWidth(WIDTH_SHORT_TEXT).setFlexGrow(0);
        }
        
        return column;
    }
}