package tech.derbent.api.screens.view;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.data.value.ValueChangeMode;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.screens.domain.CGridEntity.FieldConfig;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.basic.CDatePicker;
import tech.derbent.api.ui.component.basic.CTextField;
import tech.derbent.api.utils.CColorUtils;

/**
 * Factory for creating Vaadin editor components used by the Grid.Editor inline-edit architecture.
 *
 * Design: one component per editable column, shared across all rows.
 * Memory cost is O(editable_columns), not O(rows × columns).
 *
 * Field-type → component mapping:
 *   String       → CTextField       (respects @AMetaData.maxLength)
 *   Integer/int  → IntegerField      (step buttons; min/max from @AMetaData when set)
 *   BigDecimal   → BigDecimalField
 *   LocalDate    → CDatePicker
 *   boolean      → Checkbox
 *   CEntityDB    → CColorAwareComboBox (items from @AMetaData.dataProviderBean)
 *
 * @see CComponentGridEntity#setupGridEditor()
 */
public final class CGridEditorFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGridEditorFactory.class);

	/** Upper bound on ComboBox items loaded per entity-reference column; prevents OOM on huge tables. */
	private static final int COMBO_MAX_ITEMS = 500;

	private CGridEditorFactory() {}

	/**
	 * Creates the appropriate Vaadin editor component for the given FieldConfig.
	 *
	 * Returns null when:
	 *   - @AMetaData.readOnly is true
	 *   - The field type has no supported mapping
	 *   - An entity-reference ComboBox cannot be created (missing dataProviderBean)
	 *
	 * @param fieldConfig FieldConfig with Java Field reference and @AMetaData-derived metadata
	 * @return a HasValue component for Grid.Editor, or null if the field is not editable
	 */
	@SuppressWarnings ({"unchecked", "rawtypes"})
	public static HasValue<?, ?> createEditorComponent(final FieldConfig fieldConfig) {
		final Field field = fieldConfig.getField();
		final EntityFieldInfo fieldInfo = fieldConfig.getFieldInfo();
		final Class<?> fieldType = field.getType();

		if (fieldInfo.isReadOnly()) {
			LOGGER.debug("Skipping read-only field for Grid.Editor: {}", field.getName());
			return null;
		}

		if (fieldType == String.class) {
			return buildTextField(fieldInfo);
		}
		if (fieldType == Integer.class || fieldType == int.class) {
			return buildIntegerField(fieldInfo);
		}
		if (fieldType == BigDecimal.class) {
			return buildBigDecimalField();
		}
		if (fieldType == LocalDate.class) {
			return buildDatePickerField();
		}
		if (fieldType == Boolean.class || fieldType == boolean.class) {
			return new Checkbox();
		}
		if (CEntityDB.class.isAssignableFrom(fieldType)) {
			return buildEntityComboBox(fieldInfo, fieldType);
		}

		LOGGER.debug("No editor component mapped for field type '{}' on '{}'", fieldType.getName(), field.getName());
		return null;
	}

	// ---------------------------------------------------------------------------
	// Simple-type component builders
	// ---------------------------------------------------------------------------

	/** CTextField with optional maxLength enforcement from @AMetaData. */
	private static CTextField buildTextField(final EntityFieldInfo fieldInfo) {
		final CTextField tf = new CTextField();
		tf.setWidthFull();
		tf.setClearButtonVisible(true);
		// Inline editors should sync immediately so row-switch close listeners persist the latest text.
		tf.setValueChangeMode(ValueChangeMode.EAGER);
		// Honour @AMetaData.maxLength so the editor matches domain constraints
		if (fieldInfo.getMaxLength() > 0) {
			tf.setMaxLength(fieldInfo.getMaxLength());
		}
		return tf;
	}

	/** IntegerField with step arrows. */
	private static IntegerField buildIntegerField(@SuppressWarnings ("unused") final EntityFieldInfo fieldInfo) {
		final IntegerField nf = new IntegerField();
		nf.setWidthFull();
		nf.setStepButtonsVisible(true);
		return nf;
	}

	/** BigDecimalField for decimal numeric fields. */
	private static BigDecimalField buildBigDecimalField() {
		final BigDecimalField bf = new BigDecimalField();
		bf.setWidthFull();
		return bf;
	}

	/** CDatePicker (project wrapper) with clear button. */
	private static CDatePicker buildDatePickerField() {
		final CDatePicker dp = new CDatePicker();
		dp.setWidthFull();
		dp.setClearButtonVisible(true);
		return dp;
	}

	// ---------------------------------------------------------------------------
	// Entity-reference ComboBox builder
	// ---------------------------------------------------------------------------

	/**
	 * Builds a CColorAwareComboBox for entity-reference fields.
	 *
	 * Items are loaded once at editor-setup time from the service named in
	 * @AMetaData.dataProviderBean. listForPageView honours project/company session scoping.
	 * CColorAwareComboBox renders each option with its colour and icon, matching the rest of the UI.
	 *
	 * @param fieldInfo metadata carrying dataProviderBean and field name
	 * @param fieldType Java Class of the entity reference (e.g. CUser.class)
	 * @return colour-aware ComboBox with items loaded, or null if bean unavailable
	 */
	@SuppressWarnings ({"unchecked", "rawtypes"})
	private static CColorAwareComboBox buildEntityComboBox(final EntityFieldInfo fieldInfo,
			final Class<?> fieldType) {
		final String beanName = fieldInfo.getDataProviderBean();
		if (beanName == null || beanName.isBlank()) {
			LOGGER.warn("Entity-ref field '{}' has no @AMetaData.dataProviderBean – editor skipped",
					fieldInfo.getFieldName());
			return null;
		}
		try {
			final CAbstractService service = CSpringContext.<CAbstractService>getBean(beanName);
			final List items = service.listForPageView(
					PageRequest.of(0, COMBO_MAX_ITEMS), null).getContent();
			// CColorAwareComboBox renders entity name + colour badge, matching the grid read view
			final CColorAwareComboBox comboBox = new CColorAwareComboBox(fieldType);
			comboBox.setItems(items);
			comboBox.setItemLabelGenerator(CColorUtils::getDisplayTextFromEntity);
			comboBox.setWidthFull();
			comboBox.setClearButtonVisible(true);
			LOGGER.debug("Built CColorAwareComboBox for '{}' with {} items from '{}'",
					fieldInfo.getFieldName(), items.size(), beanName);
			return comboBox;
		} catch (final Exception e) {
			LOGGER.warn("Could not build ComboBox for dataProviderBean='{}': {}", beanName, e.getMessage());
			return null;
		}
	}
}
