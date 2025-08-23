package tech.derbent.abstracts.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CAuxillaries;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CColorAwareComboBox<T extends CEntityDB<T>> extends ComboBox<T> {

	private static final long serialVersionUID = 1L;
	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CColorAwareComboBox.class);
	private final Class<T> entityType;
	// Styling configuration
	private Boolean roundedCorners = Boolean.TRUE;
	private String padding = "4px 8px";
	private Boolean autoContrast = Boolean.TRUE;
	private String minWidth = "100%";

	/** Constructor for CColorAwareComboBox with entity type.
	 * @param entityType the entity class for the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType) {
		super();
		this.entityType = entityType;
		initializeComboBox();
		CAuxillaries.setId(this);
	}

	/** Constructor for CColorAwareComboBox with entity type and label.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType, final String label) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();
	}

	/** Constructor for CColorAwareComboBox with entity type, label, and items.
	 * @param entityType the entity class for the ComboBox
	 * @param label      the label for the ComboBox
	 * @param items      the items to populate the ComboBox */
	public CColorAwareComboBox(final Class<T> entityType, final String label, final List<T> items) {
		super(label);
		this.entityType = entityType;
		initializeComboBox();
		if (items != null) {
			setItems(items);
		}
	}

	@SuppressWarnings ("unchecked")
	public CColorAwareComboBox(final EntityFieldInfo fieldInfo) {
		super();
		this.entityType = (Class<T>) fieldInfo.getFieldTypeClass();
		initializeComboBox();
		CAuxillaries.setId(this);
		updateFromInfo(fieldInfo);
	}

	/** Configures the enhanced renderer for entities with colors and/or icons. Now uses the new CEntityLabel base class for consistent rendering. */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {
			if (item == null) {
				return new Span("N/A");
			}
			// Use the new CEntityLabel for consistent entity display
			return new CEntityLabel(item, padding, autoContrast, roundedCorners);
		}));
	}

	/** Gets the entity type for this ComboBox.
	 * @return the entity type class */
	public Class<T> getEntityType() { return entityType; }

	@Override
	public String getMinWidth() { return minWidth; }

	public String getPadding() { return padding; }

	/** Initializes the ComboBox with enhanced rendering for all entities. All entities now use the enhanced rendering with icons and colors. */
	private void initializeComboBox() {
		setAllowCustomValue(false);
		setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		configureColorRenderer();
	}
	// Getter and setter methods for styling configuration

	public boolean isAutoContrast() { return autoContrast; }

	public boolean isRoundedCorners() { return roundedCorners; }

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

	private void updateFromInfo(final EntityFieldInfo fieldInfo) {
		Check.notNull(fieldInfo, "Field info cannot be null");
		setAllowCustomValue(fieldInfo.isAllowCustomValue());
		// Set placeholder text if specified
		if (!fieldInfo.getPlaceholder().trim().isEmpty()) {
			setPlaceholder(fieldInfo.getPlaceholder());
		}
		// Set read-only state for combobox if specified
		if (fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly()) {
			setReadOnly(true);
		}
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			setWidth(fieldInfo.getWidth());
		}
	}
}
