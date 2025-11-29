package tech.derbent.api.grid.widget;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.utils.Check;

/**
 * CWidgetGrid - Grid component that displays entities using display widgets.
 * Extends CGrid and adds support for a widget column that displays entities
 * in a rich format using IEntityDisplayWidget implementations.
 *
 * The grid maintains the same patterns as CGrid for:
 * - Grid binding
 * - Selections
 * - Master-detail relations
 *
 * @param <EntityClass> the entity type
 */
public class CWidgetGrid<EntityClass extends CEntityDB<EntityClass>> extends CGrid<EntityClass> {

	private static final long serialVersionUID = 1L;

	public static final String WIDTH_WIDGET = "400px";

	private IEntityDisplayWidget<EntityClass> displayWidget;

	/**
	 * Constructor for CWidgetGrid with entity class.
	 *
	 * @param entityClass the entity class for the grid
	 */
	public CWidgetGrid(final Class<EntityClass> entityClass) {
		super(entityClass);
	}

	/**
	 * Sets the display widget for rendering entities.
	 *
	 * @param displayWidget the display widget implementation
	 */
	public void setDisplayWidget(final IEntityDisplayWidget<EntityClass> displayWidget) {
		Check.notNull(displayWidget, "Display widget cannot be null");
		this.displayWidget = displayWidget;
	}

	/**
	 * Gets the current display widget.
	 *
	 * @return the display widget or null if not set
	 */
	public IEntityDisplayWidget<EntityClass> getDisplayWidget() {
		return displayWidget;
	}

	/**
	 * Adds an ID column to the grid.
	 *
	 * @return the created column
	 */
	public Column<EntityClass> addIdColumn() {
		return addIdColumn(entity -> entity.getId(), "#", "id");
	}

	/**
	 * Adds a widget column that displays entities using the configured display widget.
	 * The widget column shows the entity in a rich format with multiple pieces of information.
	 *
	 * @param header the column header text
	 * @return the created column
	 */
	public Column<EntityClass> addWidgetColumn(final String header) {
		Check.notNull(displayWidget, "Display widget must be set before adding widget column");
		Check.notBlank(header, "Header cannot be null or blank");

		final Column<EntityClass> column = addComponentColumn(entity -> {
			if (entity == null) {
				return createEmptyCell();
			}
			try {
				final Component widget = displayWidget.createWidget(entity);
				if (widget == null) {
					return createEmptyCell();
				}
				// Wrap in a container for consistent styling
				final Div container = new Div();
				container.addClassName("widget-column-container");
				container.getStyle().set("width", "100%");
				container.add(widget);
				return container;
			} catch (final Exception e) {
				LOGGER.error("Error creating widget for entity: {}", e.getMessage());
				return createErrorCell(e.getMessage());
			}
		}).setHeader(header).setWidth(WIDTH_WIDGET).setFlexGrow(1).setSortable(false);

		return column;
	}

	/**
	 * Adds both ID and widget columns in a standard configuration.
	 * This is a convenience method for the typical use case.
	 *
	 * @param widgetHeader the header for the widget column
	 */
	public void addStandardColumns(final String widgetHeader) {
		addIdColumn();
		addWidgetColumn(widgetHeader);
	}

	/**
	 * Creates an empty cell component for null entities.
	 *
	 * @return the empty cell component
	 */
	private Component createEmptyCell() {
		final Div emptyDiv = new Div();
		emptyDiv.addClassName("widget-cell-empty");
		emptyDiv.setText("No data");
		emptyDiv.getStyle().set("color", "#666");
		emptyDiv.getStyle().set("font-style", "italic");
		emptyDiv.getStyle().set("padding", "8px");
		return emptyDiv;
	}

	/**
	 * Creates an error cell component when widget creation fails.
	 *
	 * @param errorMessage the error message to display
	 * @return the error cell component
	 */
	private Component createErrorCell(final String errorMessage) {
		final Div errorDiv = new Div();
		errorDiv.addClassName("widget-cell-error");
		errorDiv.setText("Error: " + (errorMessage != null ? errorMessage : "Unknown error"));
		errorDiv.getStyle().set("color", "#dc3545");
		errorDiv.getStyle().set("font-size", "12px");
		errorDiv.getStyle().set("padding", "8px");
		return errorDiv;
	}
}
