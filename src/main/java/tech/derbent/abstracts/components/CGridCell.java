package tech.derbent.abstracts.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.html.Span;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CGridCell - Base class for custom grid cell components.
 * <p>
 * This class provides a foundation for creating custom grid cell components that can be
 * used in CGrid implementations. It extends Vaadin's Span component to provide a
 * container for cell content with consistent styling and behavior.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable base class
 * for all custom grid cell components, ensuring consistent styling and behavior across
 * the application.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * CGridCell cell = new CGridCell("Sample Text");
 * cell.setDefaultStyling();
 * }</pre>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.components.CGridCellStatus
 * @see tech.derbent.abstracts.views.CGrid
 */
public class CGridCell extends Span {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridCell.class);

	// Default styling configuration
	protected String padding = "4px 8px";

	protected boolean centerAlign = true;

	protected String minWidth = "80px";

	protected String fontWeight = "400";

	protected boolean roundedCorners = false;

	/**
	 * Default constructor for CGridCell.
	 */
	public CGridCell() {
		super();
		initializeCell();
	}

	/**
	 * Constructor for CGridCell with text content.
	 * @param text the text content for the cell
	 */
	public CGridCell(final String text) {
		super(text);
		initializeCell();
	}

	/**
	 * Constructor for CGridCell with entity value.
	 * @param entity the entity to display in the cell
	 */
	public <T extends CEntityDB<T>> CGridCell(final T entity) {
		super();
		setEntityValue(entity);
		initializeCell();
	}

	/**
	 * Apply default styling to the cell.
	 */
	protected void applyDefaultStyling() {
		getStyle().set("display", "inline-block");
		getStyle().set("padding", padding);

		if (centerAlign) {
			getStyle().set("text-align", "center");
		}

		if ((minWidth != null) && !minWidth.trim().isEmpty()) {
			getStyle().set("min-width", minWidth);
		}

		if ((fontWeight != null) && !fontWeight.trim().isEmpty()) {
			getStyle().set("font-weight", fontWeight);
		}

		if (roundedCorners) {
			getStyle().set("border-radius", "4px");
		}
	}

	public String getFontWeight() { return fontWeight; }

	@Override
	public String getMinWidth() { return minWidth; }

	public String getPadding() { return padding; }
	// Getter and setter methods for styling configuration

	/**
	 * Initialize the cell with default configuration.
	 */
	protected void initializeCell() {
		applyDefaultStyling();
	}

	public boolean isCenterAlign() { return centerAlign; }

	public boolean isRoundedCorners() { return roundedCorners; }

	public void setCenterAlign(final boolean centerAlign) {
		this.centerAlign = centerAlign;

		if (centerAlign) {
			getStyle().set("text-align", "center");
		}
		else {
			getStyle().remove("text-align");
		}
	}

	/**
	 * Apply custom styling to the cell.
	 * @param backgroundColor the background color
	 * @param textColor       the text color
	 */
	public void setCustomStyling(final String backgroundColor, final String textColor) {

		if ((backgroundColor != null) && !backgroundColor.trim().isEmpty()) {
			getStyle().set("background-color", backgroundColor);
		}

		if ((textColor != null) && !textColor.trim().isEmpty()) {
			getStyle().set("color", textColor);
		}
	}

	/**
	 * Set entity value and display text.
	 * @param entity the entity to display
	 */
	public <T extends CEntityDB<T>> void setEntityValue(final T entity) {

		if (entity == null) {
			setText("N/A");
			getStyle().set("color", "#666666");
			getStyle().set("font-style", "italic");
		}
		else {
			// Use toString method or getName if available
			String displayText;

			try {
				displayText = entity.toString();

				if ((displayText == null) || displayText.trim().isEmpty()) {
					displayText = "No Name";
				}
			} catch (final Exception e) {
				LOGGER.warn("Error getting display text from entity: {}", e.getMessage());
				displayText = "Error";
			}
			setText(displayText);
			getStyle().remove("color");
			getStyle().remove("font-style");
		}
	}

	public void setFontWeight(final String fontWeight) {
		this.fontWeight = fontWeight;

		if ((fontWeight != null) && !fontWeight.trim().isEmpty()) {
			getStyle().set("font-weight", fontWeight);
		}
	}

	@Override
	public void setMinWidth(final String minWidth) {
		this.minWidth = minWidth;

		if ((minWidth != null) && !minWidth.trim().isEmpty()) {
			getStyle().set("min-width", minWidth);
		}
	}

	public void setPadding(final String padding) {
		this.padding = padding;
		getStyle().set("padding", padding);
	}

	public void setRoundedCorners(final boolean roundedCorners) {
		this.roundedCorners = roundedCorners;

		if (roundedCorners) {
			getStyle().set("border-radius", "4px");
		}
		else {
			getStyle().remove("border-radius");
		}
	}
}