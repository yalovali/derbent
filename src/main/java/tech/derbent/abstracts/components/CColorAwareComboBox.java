package tech.derbent.abstracts.components;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import tech.derbent.abstracts.annotations.ColorAwareComboBox;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CColorUtils;

/**
 * CColorAwareComboBox - Specialized ComboBox superclass for status entities with
 * color-aware rendering.
 * <p>
 * This class extends the standard Vaadin ComboBox to provide automatic color rendering
 * for status entities. It detects status entities and renders them with colored
 * backgrounds based on their color properties.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable superclass
 * for all color-aware ComboBox components, ensuring consistent styling and behavior
 * across the application.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * CColorAwareComboBox<CDecisionStatus> statusComboBox =
 * 	new CColorAwareComboBox<>(CDecisionStatus.class);
 * statusComboBox.setItems(statusList);
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
	 * Configures the color-aware renderer for status entities.
	 */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {
			final Span span = new Span();

			if (item == null) {
				span.setText("N/A");
				return span;
			}
			// Set the text content
			final String displayText = CColorUtils.getDisplayTextFromEntity(item);
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
	 * Initializes the ComboBox with color-aware rendering if applicable.
	 */
	private void initializeComboBox() {
		LOGGER.debug("Initializing CColorAwareComboBox for entity type: {}",
			entityType.getSimpleName());
		// Following coding guidelines: All selective ComboBoxes must be selection only
		setAllowCustomValue(false);
		// Set up item label generator
		setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));

		// Check if this is a status entity and configure color-aware rendering
		if (CColorUtils.isStatusEntity(entityType)) {
			LOGGER.debug("Configuring color-aware rendering for status entity type: {}",
				entityType.getSimpleName());
			configureColorRenderer();
		}
		else {
			LOGGER.debug(
				"Entity type {} is not a status entity, using standard rendering",
				entityType.getSimpleName());
		}
	}
	// Getter and setter methods for styling configuration

	public boolean isAutoContrast() { return autoContrast; }

	/**
	 * Checks if this ComboBox is configured for color-aware rendering.
	 * @return true if color-aware rendering is enabled
	 */
	public boolean isColorAware() { return CColorUtils.isStatusEntity(entityType); }

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