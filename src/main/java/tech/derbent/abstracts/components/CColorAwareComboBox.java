package tech.derbent.abstracts.components;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import tech.derbent.abstracts.annotations.ColorAwareComboBox;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;

/**
 * CColorAwareComboBox - Specialized ComboBox superclass for entities with
 * color and icon-aware rendering.
 * <p>
 * This class extends the standard Vaadin ComboBox to provide automatic color and icon rendering
 * for entities. It detects status entities and renders them with colored
 * backgrounds based on their color properties. Additionally, it displays appropriate
 * icons for entity types such as users, companies, projects, etc.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable superclass
 * for all enhanced ComboBox components, ensuring consistent styling and behavior
 * across the application.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * // For status entities with colors
 * CColorAwareComboBox<CDecisionStatus> statusComboBox =
 * 	new CColorAwareComboBox<>(CDecisionStatus.class);
 * statusComboBox.setItems(statusList);
 * 
 * // For entities with icons (like users)
 * CColorAwareComboBox<CUser> userComboBox =
 * 	new CColorAwareComboBox<>(CUser.class, "Select User");
 * userComboBox.setItems(userList);
 * }</pre>
 *
 * @param <T> the entity type that extends CEntityDB
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.ColorAwareComboBox
 * @see tech.derbent.abstracts.utils.CColorUtils
 */
public class CColorAwareComboBox<T extends CEntityDB<T>> extends ComboBox<T> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CColorAwareComboBox.class);

	private final Class<T> entityType;

	// Styling configuration
	private boolean roundedCorners = true;

	private String padding = "4px 8px";

	private boolean autoContrast = true;

	private String minWidth = "100%";

	/**
	 * Constructor for CColorAwareComboBox with entity type.
	 * @param entityType the entity class for the ComboBox
	 */
	public CColorAwareComboBox(final Class<T> entityType) {
		super();
		this.entityType = entityType;
		initializeComboBox();
	}

	/**
	 * Constructor for CColorAwareComboBox with entity type and label.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox
	 */
	public CColorAwareComboBox(final Class<T> entityType, final String label) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();
	}

	/**
	 * Constructor for CColorAwareComboBox with entity type, label, and items.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox
	 * @param items      the items to populate the ComboBox
	 */
	public CColorAwareComboBox(final Class<T> entityType, final String label,
		final List<T> items) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();

		if (items != null) {
			setItems(items);
		}
	}

	/**
	 * Configures the enhanced renderer for entities with colors and/or icons.
	 * Creates appropriate rendering based on entity capabilities:
	 * - Icons for entity types like users, companies, projects, etc.
	 * - Background colors for status entities
	 * - Automatic contrast text colors
	 */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {
			if (item == null) {
				final Span span = new Span("N/A");
				return span;
			}

			// Check if item should have an icon
			final Icon icon = CColorUtils.createIconForEntity(item);
			final String displayText = CColorUtils.getDisplayTextFromEntity(item);

			if (icon != null) {
				// Create a horizontal layout to hold both icon and text
				final HorizontalLayout layout = new HorizontalLayout();
				layout.setSpacing(false);
				layout.setPadding(false);
				layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);

				// Style the icon
				icon.getStyle().set("margin-right", "6px");
				icon.getStyle().set("width", "16px");
				icon.getStyle().set("height", "16px");

				// Create span for text
				final Span textSpan = new Span(displayText);

				// Apply background color if available
				try {
					final String color =
						CColorUtils.getColorWithFallback(item, CColorUtils.DEFAULT_COLOR);
					layout.getStyle().set("background-color", color);

					if (autoContrast) {
						layout.getStyle().set("color", CColorUtils.getContrastTextColor(color));
						// Also apply color to icon for consistency
						icon.getStyle().set("color", CColorUtils.getContrastTextColor(color));
					}
					layout.getStyle().set("padding", padding);
					layout.getStyle().set("display", "inline-flex");
					layout.getStyle().set("min-width", minWidth);
					layout.getStyle().set("width", "100%");

					if (roundedCorners) {
						layout.getStyle().set("border-radius", "4px");
					}
					LOGGER.debug("Applied color {} and icon to ComboBox item: {}", color, displayText);
				} catch (final Exception e) {
					LOGGER.warn("Error applying color to ComboBox item: {}", e.getMessage());
				}

				layout.add(icon, textSpan);
				return layout;
			} else {
				// No icon, use original span-based rendering
				final Span span = new Span();
				span.setText(displayText);

				// Apply background color if available
				try {
					final String color =
						CColorUtils.getColorWithFallback(item, CColorUtils.DEFAULT_COLOR);
					span.getStyle().set("background-color", color);

					if (autoContrast) {
						span.getStyle().set("color", CColorUtils.getContrastTextColor(color));
					}
					span.getStyle().set("padding", padding);
					span.getStyle().set("display", "inline-block");
					span.getStyle().set("min-width", minWidth);

					if (roundedCorners) {
						span.getStyle().set("border-radius", "4px");
					}
					LOGGER.debug("Applied color {} to ComboBox item: {}", color, displayText);
				} catch (final Exception e) {
					LOGGER.warn("Error applying color to ComboBox item: {}", e.getMessage());
				}
				return span;
			}
		}));
	}

	/**
	 * Gets the entity type for this ComboBox.
	 * @return the entity type class
	 */
	public Class<T> getEntityType() { return entityType; }

	@Override
	public String getMinWidth() { return minWidth; }

	public String getPadding() { return padding; }

	/**
	 * Checks if this entity type should support icon rendering.
	 * 
	 * @return true if icons should be displayed for this entity type
	 */
	private boolean hasEntityIconSupport() {
		if (entityType == null) {
			return false;
		}
		
		// Check by class name patterns for entities that should have icons
		final String className = entityType.getSimpleName();
		
		// User-related entities
		if (className.contains("User") || className.contains("user")) {
			return true;
		}
		
		// Company-related entities  
		if (className.contains("Company") || className.contains("company")) {
			return true;
		}
		
		// Project-related entities
		if (className.contains("Project") || className.contains("project")) {
			return true;
		}
		
		// Meeting-related entities
		if (className.contains("Meeting") || className.contains("meeting")) {
			return true;
		}
		
		// Activity-related entities
		if (className.contains("Activity") || className.contains("activity")) {
			return true;
		}
		
		// Decision-related entities
		if (className.contains("Decision") || className.contains("decision")) {
			return true;
		}
		
		// Comment-related entities
		if (className.contains("Comment") || className.contains("comment")) {
			return true;
		}
		
		return false;
	}

	/**
	 * Initializes the ComboBox with color-aware rendering if applicable.
	 */
	private void initializeComboBox() {
		LOGGER.debug("Initializing CColorAwareComboBox for entity type: {}",
			entityType.getSimpleName());
		// Following coding guidelines: All selective ComboBoxes must be selection only
		setAllowCustomValue(false);
		// Set up item label generator
		setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));

		// Check if this is a status entity or an entity that should have special rendering
		final boolean isStatusEntity = CColorUtils.isStatusEntity(entityType);
		final boolean hasEntityIcons = hasEntityIconSupport();
		
		if (isStatusEntity || hasEntityIcons) {
			LOGGER.debug("Configuring enhanced rendering for entity type: {} (status: {}, icons: {})",
				entityType.getSimpleName(), isStatusEntity, hasEntityIcons);
			configureColorRenderer();
		}
		else {
			LOGGER.debug(
				"Entity type {} uses standard rendering (no colors or icons)",
				entityType.getSimpleName());
		}
	}
	// Getter and setter methods for styling configuration

	public boolean isAutoContrast() { return autoContrast; }

	/**
	 * Checks if this ComboBox is configured for enhanced rendering (colors and/or icons).
	 * @return true if enhanced rendering is enabled
	 */
	public boolean isColorAware() { 
		return CColorUtils.isStatusEntity(entityType) || hasEntityIconSupport(); 
	}

	public boolean isRoundedCorners() { return roundedCorners; }

	/**
	 * Sets the styling configuration from an annotation.
	 * @param annotation the ColorAwareComboBox annotation
	 */
	public void setAnnotationConfig(final ColorAwareComboBox annotation) {

		if (annotation != null) {
			this.roundedCorners = annotation.roundedCorners();
			this.padding = annotation.padding();
			this.autoContrast = annotation.autoContrast();
			this.minWidth = annotation.minWidth();

			// Reconfigure renderer if this is a status entity
			if (CColorUtils.isStatusEntity(entityType)) {
				configureColorRenderer();
			}
		}
	}

	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;

		if (isColorAware()) {
			configureColorRenderer();
		}
	}

	@Override
	public void setMinWidth(final String minWidth) {
		this.minWidth = minWidth;

		if (isColorAware()) {
			configureColorRenderer();
		}
	}

	public void setPadding(final String padding) {
		this.padding = padding;

		if (isColorAware()) {
			configureColorRenderer();
		}
	}

	public void setRoundedCorners(final boolean roundedCorners) {
		this.roundedCorners = roundedCorners;

		if (isColorAware()) {
			configureColorRenderer();
		}
	}
}