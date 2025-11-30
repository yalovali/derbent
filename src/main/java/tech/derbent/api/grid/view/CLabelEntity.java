package tech.derbent.api.grid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.utils.CColorUtils;

public class CLabelEntity extends Div {

	private static final Logger LOGGER = LoggerFactory.getLogger(CLabelEntity.class);
	private static final long serialVersionUID = 1L;
	private Boolean autoContrast = Boolean.TRUE;
	private Boolean showIcon = Boolean.FALSE;

	/** Default constructor for CGridCell. */
	public CLabelEntity() {
		super();
		initializeCell();
	}

	/** Constructor for CGridCell with entity value.
	 * @param entity the entity to display in the cell */
	public CLabelEntity(final CEntityDB<?> entity) {
		super();
		setEntityValue(entity);
		initializeCell();
	}

	/** Constructor for CGridCell with text content.
	 * @param text the text content for the cell */
	public CLabelEntity(final String text) {
		super();
		setText(text);
		initializeCell();
	}

	/** Apply color-aware styling for entities with colors.
	 * @param entity      the entity providing the color
	 * @param color       the background color to apply
	 * @param displayText the text to display
	 * @throws Exception */
	private void applyColorStyling(final CEntityDB<?> entity, final String color, final String displayText) throws Exception {
		// Apply background color
		getStyle().set("background-color", color);
		// Apply contrasting text color if auto-contrast is enabled
		if (Boolean.TRUE.equals(autoContrast)) {
			final String textColor = CColorUtils.getContrastTextColor(color);
			getStyle().set("color", textColor);
		}
		// Create content with icon if enabled
		if (Boolean.TRUE.equals(showIcon)) {
			final Icon icon = CColorUtils.getIconForEntity(entity);
			if (icon != null) {
				add(icon);
			}
		}
		// Add text content
		add(displayText);
	}

	/** Apply default styling for status cells when color is not available. */
	private void applyDefaultStatusStyling() {
		getStyle().set("background-color", "#f8f9fa");
		getStyle().set("color", "#495057");
		getStyle().set("border", "1px solid #dee2e6");
	}

	/** Apply default styling to the cell. */
	private void applyDefaultStyling() {
		getStyle().set("width", "100%");
		getStyle().set("height", "100%");
		getStyle().set("display", "flex");
		getStyle().set("align-items", "center");
		getStyle().set("padding", "4px 8px");
		getStyle().set("box-sizing", "border-box");
	}

	/** Initialize the cell with default configuration. */
	private void initializeCell() {
		applyDefaultStyling();
	}

	/** Check if auto-contrast is enabled for color-aware cells.
	 * @return true if auto-contrast is enabled */
	public boolean isAutoContrast() { return autoContrast; }

	/** Check if icon display is enabled.
	 * @return true if icons are displayed alongside text */
	public boolean isShowIcon() { return showIcon; }

	/** Set entity value and display text with optional color rendering.
	 * @param entity the entity to display */
	public void setEntityValue(final CEntityDB<?> entity) {
		removeAll(); // Clear any existing content
		if (entity == null) {
			setText("N/A");
			getStyle().set("color", "#666666");
			getStyle().set("font-style", "italic");
			return;
		}
		try {
			final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
			final String color = CColorUtils.getColorFromEntity(entity);
			if ((color != null) && !color.isBlank()) {
				// Color-aware rendering
				applyColorStyling(entity, color, displayText);
			} else {
				// Regular text rendering
				setText(displayText);
				// Reset any color styling
				getStyle().remove("background-color");
				getStyle().remove("color");
				getStyle().remove("font-style");
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting entity value: {}", e.getMessage());
			setText("Error");
			getStyle().set("color", "#dc3545");
		}
	}

	/** Enable or disable icon display.
	 * @param showIcon true to show icons alongside text */
	public void setShowIcon(final Boolean showIcon) {
		this.showIcon = showIcon != null ? showIcon : Boolean.FALSE;
	}

	/** Set status entity value with color-aware rendering and optional icon display. This method provides the same functionality as the removed
	 * CGridCellStatus.
	 * @param entity the status entity to display */
	public void setStatusValue(final CEntityDB<?> entity) {
		removeAll(); // Clear any existing content
		if (entity == null) {
			setText("No Status");
			applyDefaultStatusStyling();
			return;
		}
		try {
			final String displayText = CColorUtils.getDisplayTextFromEntity(entity);
			final String color = CColorUtils.getColorFromEntity(entity);
			if ((color != null) && !color.isBlank()) {
				// Color-aware rendering
				applyColorStyling(entity, color, displayText);
			} else {
				// Regular text rendering
				setText(displayText);
				// Reset any color styling
				getStyle().remove("background-color");
				getStyle().remove("color");
				getStyle().remove("font-style");
			}
			// Add status-specific styling
			getStyle().set("border-radius", "4px");
			getStyle().set("font-weight", "500");
		} catch (final Exception e) {
			LOGGER.error("Error applying color to status cell: {}", e.getMessage());
			setEntityValue(entity);
			applyDefaultStatusStyling();
		}
	}
}
