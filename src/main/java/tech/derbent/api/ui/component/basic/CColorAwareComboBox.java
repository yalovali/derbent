package tech.derbent.api.ui.component.basic;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.annotations.CDataProviderResolver;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasIcon;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

public class CColorAwareComboBox<T extends CEntityDB<T>> extends ComboBox<T> {

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

	@SuppressWarnings ("unchecked")
	public CColorAwareComboBox(IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder,
			CDataProviderResolver dataProviderResolver) throws Exception {
		try {
			entityType = (Class<T>) fieldInfo.getFieldTypeClass();
			Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
			// LOGGER.debug("Creating CColorAwareComboBox for field: {}", fieldInfo.getFieldName());
			// Initialize this instance properly instead of creating a new one
			initializeComboBox();
			CAuxillaries.setId(this);
			updateFromInfo(fieldInfo);
			List<T> items = null;
			Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
			items = dataProviderResolver.resolveDataList(contentOwner, fieldInfo);
			Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
			if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
				setValue(null);
			}
			setItems(items);
			if (!items.isEmpty()) {
				if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
					// For entity types, try to find by name or toString match
					final T defaultItem = items.stream().filter(item -> {
						final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
						return fieldInfo.getDefaultValue().equals(itemDisplay);
					}).findFirst().orElse(null);
					if (defaultItem != null) {
						setValue(defaultItem);
					}
				} else if (fieldInfo.isAutoSelectFirst()) {
					setValue(items.get(0));
				}
			}
			if (binder != null) {
				// this is valid
				binder.bind(this, fieldInfo.getFieldName());
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind ComboBox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
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
			} catch (final Exception e) {
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
		setupSelectedValueDisplay();
	}

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

	/** Sets up a value change listener to update the prefix component with the selected item's icon and applies styling to show the color. This
	 * ensures the selected value also displays with color and icon, not just the dropdown items. */
	private void setupSelectedValueDisplay() {
		addValueChangeListener(event -> {
			Icon icon;
			try {
				final T selectedItem = event.getValue();
				if (selectedItem == null) {
					setPrefixComponent(null);
					getElement().getStyle().remove("--vaadin-input-field-background");
					getElement().getStyle().remove("background-color");
					getElement().getStyle().remove("color");
					return;
				}
				if (selectedItem instanceof IHasIcon) {
					icon = CColorUtils.getIconForEntity(selectedItem);
					CColorUtils.styleIcon(icon);
					try {
						final String color = CColorUtils.getColorFromEntity(selectedItem);
						if ((color != null) && !color.isEmpty()) {
							icon.getElement().getStyle().set("color", color);
						}
					} catch (final Exception colorEx) {
						// Entity doesn't have color, ignore
					}
					setPrefixComponent(icon);
					final String backgroundColor = CColorUtils.getColorFromEntity(selectedItem);
					if ((backgroundColor != null) && !backgroundColor.isEmpty()) {
						getElement().getStyle().set("background-color", backgroundColor);
						if (autoContrast) {
							final String textColor = CColorUtils.getContrastTextColor(backgroundColor);
							getElement().getStyle().set("color", textColor);
						}
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		});
	}
	// Getter and setter methods for styling configuration

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
