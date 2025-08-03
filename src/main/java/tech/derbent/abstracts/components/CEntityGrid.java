package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.function.ValueProvider;

import tech.derbent.abstracts.annotations.ColorAwareGrid;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.views.CGrid;

/**
 * CColorAwareGrid - Specialized Grid superclass for entities with color-aware status
 * column rendering.
 * <p>
 * This class extends the CGrid base class to provide automatic color rendering for status
 * columns. It detects status entities and renders them with colored backgrounds based on
 * their color properties.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable superclass
 * for all color-aware Grid components, ensuring consistent styling and behavior across
 * the application.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * CColorAwareGrid<CDecision> grid = new CColorAwareGrid<>(CDecision.class);
 * grid.addColorAwareStatusColumn(CDecision::getStatus, "Status", "status");
 * }</pre>
 *
 * @param <EntityClass> the entity type that extends CEntityDB
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.ColorAwareGrid
 * @see tech.derbent.abstracts.utils.CColorUtils
 * @see tech.derbent.abstracts.views.CGrid
 */
public class CEntityGrid<EntityClass extends CEntityDB<EntityClass>>
	extends CGrid<EntityClass> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityGrid.class);

	// Default styling configuration
	private boolean roundedCorners = true;

	private String padding = "4px 8px";

	private boolean autoContrast = true;

	private String minWidth = "80px";

	private boolean centerAlign = true;

	private String fontWeight = "500";

	private boolean showIcon = true; // New: Enable/disable icon display in status cells

	/**
	 * Constructor for CColorAwareGrid with entity class.
	 * @param entityClass the entity class for the grid
	 */
	public CEntityGrid(final Class<EntityClass> entityClass) {
		super(entityClass);
	}

	/**
	 * Adds a color-aware status column with enhanced rendering. This method creates a
	 * column that displays status entities with their associated colors as background.
	 * @param <S>            the status entity type
	 * @param statusProvider Provider that returns the status entity
	 * @param header         Column header text
	 * @param key            Column key for identification
	 * @return The created column
	 */
	public <S extends CEntityDB<S>> Column<EntityClass> addColorAwareStatusColumn(
		final ValueProvider<EntityClass, S> statusProvider, final String header,
		final String key) {
		return addColorAwareStatusColumn(statusProvider, header, key, null);
	}

	/**
	 * Adds a color-aware status column with annotation-based configuration. This method
	 * creates a column that displays status entities with their associated colors as
	 * background.
	 * @param <S>            the status entity type
	 * @param statusProvider Provider that returns the status entity
	 * @param header         Column header text
	 * @param key            Column key for identification
	 * @param annotation     Optional annotation for styling configuration
	 * @return The created column
	 */
	public <S extends CEntityDB<S>> Column<EntityClass> addColorAwareStatusColumn(
		final ValueProvider<EntityClass, S> statusProvider, final String header,
		final String key, final ColorAwareGrid annotation) {
		LOGGER.info("Adding color-aware status column: {} with enhanced color rendering",
			header);

		// Configure styling from annotation if provided
		if (annotation != null) {
			this.roundedCorners = annotation.roundedCorners();
			this.padding = annotation.padding();
			this.autoContrast = annotation.autoContrast();
			this.minWidth = annotation.minWidth();
			this.centerAlign = annotation.centerAlign();
			this.fontWeight = annotation.fontWeight();
		}
		final Column<EntityClass> column = addComponentColumn(entity -> {
			final S status = statusProvider.apply(entity);
			final CGridCell statusCell = new CGridCell();

			// Configure styling from annotation if provided
			if (annotation != null) {
				// Apply annotation-based configuration to the cell
				statusCell.setAutoContrast(this.autoContrast);
			}
			// Configure icon display (always from grid setting)
			statusCell.setShowIcon(this.showIcon);
			statusCell.setStatusValue(status);
			return statusCell;
		}).setHeader(header).setWidth(WIDTH_REFERENCE).setFlexGrow(0).setSortable(true);

		if (key != null) {
			column.setKey(key);
		}
		return column;
	}

	/**
	 * Legacy method for backward compatibility. Delegates to the new
	 * addColorAwareStatusColumn method.
	 * @param <S>            the status entity type
	 * @param statusProvider Provider that returns the status entity
	 * @param header         Column header text
	 * @param key            Column key for identification
	 * @return The created column
	 */
	@Override
	public <S extends CEntityDB<S>> Column<EntityClass> addStatusColumn(
		final ValueProvider<EntityClass, S> statusProvider, final String header,
		final String key) {
		LOGGER.debug(
			"Legacy addStatusColumn called, delegating to addColorAwareStatusColumn");
		return addColorAwareStatusColumn(statusProvider, header, key);
	}

	public String getFontWeight() { return fontWeight; }

	@Override
	public String getMinWidth() { return minWidth; }

	public String getPadding() { return padding; }

	public boolean isAutoContrast() { return autoContrast; }

	public boolean isCenterAlign() { return centerAlign; }

	public boolean isRoundedCorners() { return roundedCorners; }

	public boolean isShowIcon() { return showIcon; }

	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;
	}

	public void setCenterAlign(final boolean centerAlign) {
		this.centerAlign = centerAlign;
	}

	public void setFontWeight(final String fontWeight) { this.fontWeight = fontWeight; }

	@Override
	public void setMinWidth(final String minWidth) { this.minWidth = minWidth; }

	public void setPadding(final String padding) { this.padding = padding; }

	public void setRoundedCorners(final boolean roundedCorners) {
		this.roundedCorners = roundedCorners;
	}

	public void setShowIcon(final boolean showIcon) { this.showIcon = showIcon; }
}