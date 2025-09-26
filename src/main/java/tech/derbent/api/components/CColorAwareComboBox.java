package tech.derbent.api.components;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

public class CColorAwareComboBox<T extends CEntityDB<T>> extends ComboBox<T> {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CColorAwareComboBox.class);
	private static final long serialVersionUID = 1L;
	private Boolean autoContrast = Boolean.TRUE;
	private final Class<T> entityType;
	private String minWidth = "100%";
	private String padding = "4px 8px";
	// Styling configuration
	private Boolean roundedCorners = Boolean.TRUE;

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
		entityType = (Class<T>) fieldInfo.getFieldTypeClass();
		initializeComboBox();
		CAuxillaries.setId(this);
		updateFromInfo(fieldInfo);
	}

	/** Configures the enhanced renderer for entities with colors and/or icons. Now uses the new CEntityLabel base class for consistent rendering. */
	private void configureColorRenderer() {
		setRenderer(new ComponentRenderer<>(item -> {
			try {
				if (item == null) {
					return new Span("N/A");
				}
				if ((item instanceof CEntityNamed) == false) {
					return new Span("Invalid Entity");
				}
				// Use the new CEntityLabel for consistent entity display
				return new CEntityLabel((CEntityNamed<?>) item, padding, autoContrast, roundedCorners);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
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
