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
 * CColorAwareComboBox - Specialized ComboBox superclass for entities with color and
 * icon-aware rendering.
 * <p>
 * This class extends the standard Vaadin ComboBox to provide automatic color and icon
 * rendering for entities. It detects status entities and renders them with colored
 * backgrounds based on their color properties. Additionally, it displays appropriate
 * icons for entity types such as users, companies, projects, etc.
 * </p>
 * <p>
 * The class follows the project's coding guidelines by providing a reusable superclass
 * for all enhanced ComboBox components, ensuring consistent styling and behavior across
 * the application.
 * </p>
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 *
 * <pre>{@code
 * // For status entities with colors
 * CColorAwareComboBox<CDecisionStatus> statusComboBox =
 * 	statusComboBox.setItems(statusList);
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
	 * Configures the enhanced renderer for entities with colors and/or icons. Now uses
	 * the new CEntityLabel base class for consistent rendering.
	 */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {

			if (item == null) {
				return new Span("N/A");
			}
			// Use the new CEntityLabel for consistent entity display
			return new CEntityLabel(item, padding, autoContrast, roundedCorners);
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
	 * Initializes the ComboBox with enhanced rendering for all entities. All entities now
	 * use the enhanced rendering with icons and colors.
	 */
	private void initializeComboBox() {
		LOGGER.debug("Initializing CColorAwareComboBox for entity type: {}",
			entityType.getSimpleName());
		// Following coding guidelines: All selective ComboBoxes must be selection only
		setAllowCustomValue(false);
		// Set up item label generator
		setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		// All entities now use enhanced rendering with the CEntityLabel base class
		LOGGER.debug("Configuring enhanced rendering for entity type: {}",
			entityType.getSimpleName());
		configureColorRenderer();
	}
	// Getter and setter methods for styling configuration

	public boolean isAutoContrast() { return autoContrast; }

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
			// Reconfigure renderer for all entities
			configureColorRenderer();
		}
	}

	public void setAutoContrast(final boolean autoContrast) {
		this.autoContrast = autoContrast;
		configureColorRenderer();
	}

	@Override
	public void setMinWidth(final String minWidth) {
		this.minWidth = minWidth;
		configureColorRenderer();
	}

	public void setPadding(final String padding) {
		this.padding = padding;
		configureColorRenderer();
	}

	public void setRoundedCorners(final boolean roundedCorners) {
		this.roundedCorners = roundedCorners;
		configureColorRenderer();
	}
}