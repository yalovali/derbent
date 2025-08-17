package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;

import tech.derbent.abstracts.components.CBinderFactory;
import tech.derbent.abstracts.components.CColorAwareComboBox;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CAuxillaries;
import tech.derbent.abstracts.utils.CColorUtils;
import tech.derbent.abstracts.utils.Check;

@org.springframework.stereotype.Component
public final class CEntityFormBuilder implements ApplicationContextAware {

	/**
	 * Interface for providing data to ComboBox components. Implementations should provide
	 * lists of entities for specific entity types.
	 * <p>
	 * <strong>Note:</strong> This interface is maintained for backward compatibility. New
	 * implementations should use the annotation-based approach with MetaData annotations.
	 * </p>
	 * @see MetaData#dataProviderBean()
	 * @see MetaData#dataProviderClass()
	 * @see CDataProviderResolver
	 */
	public interface ComboBoxDataProvider {

		/**
		 * Gets items for a specific entity type.
		 * @param entityType the class type of the entity
		 * @return list of entities to populate the ComboBox
		 */
		<T extends CEntityDB<T>> List<T> getItems(Class<T> entityType);
	}

	private static final Logger LOGGER =
		LoggerFactory.getLogger(CEntityFormBuilder.class);

	protected static final String LabelMinWidth_210PX = "210px";

	private static CDataProviderResolver dataProviderResolver;

	/**
	 * Builds a form using an enhanced binder for better error reporting.
	 * @param <EntityClass> the entity type
	 * @param entityClass   the entity class
	 * @return a Div containing the form with enhanced binder
	 */
	@SuppressWarnings ("unchecked")
	public static <EntityClass> Div buildEnhancedForm(final Class<?> entityClass) {
		final CEnhancedBinder<EntityClass> enhancedBinder =
			CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, null);
	}

	/**
	 * Builds a form using an enhanced binder with data provider.
	 * @param <EntityClass> the entity type
	 * @param entityClass   the entity class
	 * @param dataProvider  the data provider for ComboBox components
	 * @return a Div containing the form with enhanced binder
	 */
	@SuppressWarnings ("unchecked")
	@Deprecated
	public static <EntityClass> Div buildEnhancedForm(final Class<?> entityClass,
		final ComboBoxDataProvider dataProvider) {
		final CEnhancedBinder<EntityClass> enhancedBinder =
			CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, null);
	}

	/**
	 * Builds a form using an enhanced binder with specified fields.
	 * @param <EntityClass> the entity type
	 * @param entityClass   the entity class
	 * @param entityFields  list of field names to include
	 * @return a Div containing the form with enhanced binder
	 */
	@SuppressWarnings ("unchecked")
	public static <EntityClass> Div buildEnhancedForm(final Class<?> entityClass,
		final List<String> entityFields) {
		final CEnhancedBinder<EntityClass> enhancedBinder =
			CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, entityFields);
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder) {
		return buildForm(entityClass, binder, null);
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder, final List<String> entityFields) {
		Check.notNull(entityClass, "Entity class");
		Check.notNull(binder, "Binder of " + entityClass.getSimpleName());
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		// final FormLayout formLayout = new FormLayout();
		final VerticalLayout formLayout = new VerticalLayout();
		// no spacing, no margin, no padding
		formLayout.setPadding(false);
		formLayout.setMargin(false);
		formLayout.setSpacing(false);
		// formLayout.setLabelsAside(true); formLayout.addFormItem(firstName, "First
		// name"); Collect all fields from the class hierarchy with enhanced logging
		final List<Field> allFields = new ArrayList<>();
		getListOfAllFields(entityClass, allFields);
		// LOGGER.debug("Total fields collected from hierarchy: {}", allFields.size());
		// Filter and sort fields with enhanced null checking and logging
		final List<Field> sortedFields = getSortedFilteredFieldsList(allFields);
		LOGGER.info("Processing {} visible fields for form generation",
			sortedFields.size());
		// Create components with enhanced error handling and logging
		int processedComponents = 0;

		for (final Field field : sortedFields) {
			// skip if entityFields is not null and does not contain the field name or
			// entityFields is empty
			boolean skip = false;

			if (entityFields == null) {
				// skip = false; //already false by default
			}

			if ((entityFields != null) && !entityFields.contains(field.getName())) {
				skip = true;
			}

			if (skip) {
				continue;
			}
			processedComponents =
				processMetaForField(binder, formLayout, processedComponents, field);
		}
		panel.add(formLayout);
		return panel;
	}

	/**
	 * @deprecated Use buildForm(Class, CEnhancedBinder) instead. ComboBoxDataProvider is
	 *             no longer needed.
	 */
	@Deprecated
	public static <EntityClass> Div buildFormAll(final Class<?> entityClass,
		final CEnhancedBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider) {
		return buildForm(entityClass, binder, null);
	}

	private static NumberField createBigDecimalField(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for BigDecimal field creation");
		Check.notNull(meta, "MetaData for BigDecimal field creation");
		Check.notNull(binder, "Binder for BigDecimal field creation");
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(0.01); // Set decimal step for BigDecimal fields

		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				final double defaultVal = Double.parseDouble(meta.defaultValue());
				numberField.setValue(defaultVal);
			} catch (final NumberFormatException e) {
				LOGGER.error(
					"Failed to parse default value '{}' as number for field '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}

		try {
			final String propertyName = getPropertyName(field);
			// Use converter to handle BigDecimal conversion
			binder.forField(numberField)
				.withConverter(value -> value != null ? BigDecimal.valueOf(value) : null,
					value -> value != null ? value.doubleValue() : null,
					"Invalid decimal value")
				.bind(propertyName);
			LOGGER.debug(
				"Successfully bound NumberField with BigDecimal converter for field '{}'",
				field.getName());
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind BigDecimal field for field '{}': {} - using fallback binding",
				field.getName(), e.getMessage());

			// Fallback to simple binding without converter
			try {
				safeBindComponentWithField(binder, numberField, field,
					"NumberField(BigDecimal-fallback)");
			} catch (final Exception fallbackException) {
				LOGGER.error("Fallback binding also failed for BigDecimal field '{}': {}",
					field.getName(), fallbackException.getMessage());
			}
		}
		return numberField;
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createCheckbox - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
		final Checkbox checkbox = new Checkbox();
		// Set ID for better test automation
		CAuxillaries.setId(checkbox);

		// Safe null checking and parsing for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				checkbox.setValue(Boolean.parseBoolean(meta.defaultValue()));
				// LOGGER.debug("Set default value for checkbox '{}': {}",
				// field.getName(), meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.warn("Invalid boolean default value '{}' for field '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, checkbox, field, "Checkbox");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return checkbox;
	}

	@SuppressWarnings ("unchecked")
	private static <T extends CEntityDB<T>> ComboBox<T> createComboBox(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for ComboBox creation");
		Check.notNull(meta, "MetaData for ComboBox creation");
		Check.notNull(binder, "Binder for ComboBox creation");
		final Class<T> fieldType = (Class<T>) field.getType();
		// All ComboBoxes are now color-aware by default
		LOGGER.debug("Creating CColorAwareComboBox for field: {}", field.getName());
		final ComboBox<T> comboBox = new CColorAwareComboBox<>(fieldType, meta);
		// Enhanced item label generator with null safety and proper display formatting
		// Fix for combobox display issue: use getName() for CEntityNamed entities instead
		// of toString()
		comboBox
			.setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		// Data provider resolution using CDataProviderResolver
		List<T> items = null;
		Check.notNull(dataProviderResolver,
			"DataProviderResolver for field " + field.getName());
		items = dataProviderResolver.resolveData(fieldType, meta);
		LOGGER.debug("Resolved {} items for field '{}' of type {}", items.size(),
			field.getName(), fieldType.getSimpleName());
		Check.notNull(items, "Items for field " + field.getName() + " of type "
			+ fieldType.getSimpleName());

		// Handle clearOnEmptyData configuration
		if (meta.clearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
			LOGGER.debug("Cleared ComboBox value for field '{}' due to empty data",
				field.getName());
		}
		comboBox.setItems(items);
		// Enhanced default value handling with autoSelectFirst support
		final boolean hasDefaultValue =
			(meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty();

		if (!items.isEmpty()) {

			if (hasDefaultValue) {
				// For entity types, try to find by name or toString match
				final T defaultItem = items.stream().filter(item -> {
					final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
					return meta.defaultValue().equals(itemDisplay);
				}).findFirst().orElse(null);

				if (defaultItem != null) {
					comboBox.setValue(defaultItem);
					LOGGER.debug("Set ComboBox default value for field '{}': {}",
						field.getName(), defaultItem);
				}
				else {
					LOGGER.warn("Default value '{}' not found in items for field '{}'",
						meta.defaultValue(), field.getName());
				}
			}
			else if (meta.autoSelectFirst()) {
				// Auto-select first item if configured
				comboBox.setValue(items.get(0));
				LOGGER.debug("Auto-selected first item for field '{}': {}",
					field.getName(), items.get(0));
			}
		}
		// Use simple binding for ComboBox to avoid incomplete forField bindings The
		// complex converter logic was causing incomplete bindings
		safeBindComponentWithField(binder, comboBox, field, "ComboBox");
		return comboBox;
	}

	/**
	 * Creates a ComboBox for String fields with data provider configuration.
	 * This method handles String fields that should be rendered as ComboBox instead of TextField
	 * when metadata specifies a data provider.
	 * @param field the field to create the ComboBox for
	 * @param meta the metadata containing data provider configuration
	 * @param binder the binder for form binding
	 * @return configured ComboBox<String> component
	 */
	private static ComboBox<String> createStringComboBox(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for String ComboBox creation");
		Check.notNull(meta, "MetaData for String ComboBox creation");
		Check.notNull(binder, "Binder for String ComboBox creation");
		
		LOGGER.debug("Creating String ComboBox for field: {}", field.getName());
		final ComboBox<String> comboBox = new ComboBox<>();
		
		// Configure basic properties from metadata
		comboBox.setLabel(meta.displayName());
		comboBox.setPlaceholder(meta.placeholder());
		comboBox.setAllowCustomValue(meta.allowCustomValue());
		comboBox.setReadOnly(meta.comboboxReadOnly() || meta.readOnly());
		
		// Set width if specified
		if (!meta.width().trim().isEmpty()) {
			comboBox.setWidth(meta.width());
		}
		
		// TODO: Resolve String data using data provider
		// For now, we'll provide an empty list to avoid null pointer exceptions
		// This will be enhanced when CDataProviderResolver supports String types
		LOGGER.warn("String ComboBox data provider resolution not yet implemented for field '{}'", 
			field.getName());
		comboBox.setItems(); // Empty list for now
		
		// Handle default value
		final boolean hasDefaultValue =
			(meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty();
		if (hasDefaultValue) {
			comboBox.setValue(meta.defaultValue());
			LOGGER.debug("Set String ComboBox default value for field '{}': '{}'",
				field.getName(), meta.defaultValue());
		}
		
		// Bind to field
		safeBindComponentWithField(binder, comboBox, field, "String ComboBox");
		return comboBox;
	}

	private static Component createComponentForField(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		Component component = null;
		Check.notNull(field, "Field");
		Check.notNull(meta, "MetaData " + field.getName());
		Check.notNull(binder, "Binder for field " + field.getName());
		final Class<?> fieldType = field.getType();
		Check.notNull(fieldType, "Field type for field " + field.getName());

		// Check if field should be rendered as ComboBox based on metadata
		final boolean hasDataProvider = (meta.dataProviderBean() != null)
			&& !meta.dataProviderBean().trim().isEmpty();
		
		if (hasDataProvider && (fieldType == String.class)) {
			// String field with data provider should be rendered as String ComboBox
			component = createStringComboBox(field, meta, binder);
		}
		else if (hasDataProvider || CEntityDB.class.isAssignableFrom(fieldType)) {
			// Entity field or non-String field with data provider should be rendered as entity ComboBox
			component = createComboBox(field, meta, binder);
		}
		else if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			component = createCheckbox(field, meta, binder);
		}
		else if ((fieldType == String.class)
			&& (meta.maxLength() >= CEntityConstants.MAX_LENGTH_DESCRIPTION)) {
			component = createTextArea(field, meta, binder);
		}
		else if ((fieldType == String.class)
			&& (meta.maxLength() < CEntityConstants.MAX_LENGTH_DESCRIPTION)) {
			component = createTextField(field, meta, binder);
		}
		else if ((fieldType == Integer.class) || (fieldType == int.class)
			|| (fieldType == Long.class) || (fieldType == long.class)) {
			// Integer types
			component = createIntegerField(field, meta, binder);
		}
		else if (fieldType == BigDecimal.class) {
			component = createBigDecimalField(field, meta, binder);
		}
		else if ((fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)) {
			// Floating-point types
			component = createFloatingPointField(field, meta, binder);
		}
		else if (fieldType == LocalDate.class) {
			component = createDatePicker(field, meta, binder);
		}
		else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			component = createDateTimePicker(field, meta, binder);
		}
		else if (fieldType.isEnum()) {
			component = createEnumComponent(field, meta, binder);
		}
		else {
			Check.condition(false, "Unsupported field type: " + fieldType.getSimpleName()
				+ " for field: " + field.getName());
		}
		Check.notNull(component, "Component for field " + field.getName() + " of type "
			+ fieldType.getSimpleName());
		setRequiredIndicatorVisible(meta, component);
		// dont use helper text for Checkbox components setHelperText(meta, component);
		setComponentWidth(component, meta);
		// setclass name for styling in format of form-field{ComponentType}
		component.setClassName("form-field-" + component.getClass().getSimpleName());
		// Create field
		return component;
	}

	private static DatePicker createDatePicker(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		CAuxillaries.setId(datePicker);
		safeBindComponentWithField(binder, datePicker, field, "DatePicker");
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();
		CAuxillaries.setId(dateTimePicker);
		safeBindComponentWithField(binder, dateTimePicker, field, "DateTimePicker");
		return dateTimePicker;
	}

	/**
	 * Creates an enhanced binder for the specified entity class. This method provides
	 * easy access to enhanced binders without changing existing code structure.
	 * @param <EntityClass> the entity type
	 * @param entityClass   the entity class
	 * @return an enhanced binder with detailed error logging
	 */
	@SuppressWarnings ("unchecked")
	public static <EntityClass> CEnhancedBinder<EntityClass>
		createEnhancedBinder(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class for enhanced binder");
		return CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
	}

	@SuppressWarnings ({
		"unchecked", "rawtypes" }
	)
	private static Component createEnumComponent(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {
		final Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
		final Enum[] enumConstants = enumType.getEnumConstants();

		if (meta.useRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			safeBindComponentWithField(binder, radioGroup, field, "RadioButtonGroup");
			return radioGroup;
		}
		else {
			final ComboBox<Enum> comboBox = new ComboBox<>();
			// Set ID for better test automation
			CAuxillaries.setId(comboBox);
			// Following coding guidelines: All selective ComboBoxes must be selection
			// only (user must not be able to type arbitrary text)
			comboBox.setAllowCustomValue(false);
			comboBox.setItems(enumConstants);
			comboBox.setItemLabelGenerator(Enum::name);
			safeBindComponentWithField(binder, comboBox, field, "ComboBox(Enum)");
			return comboBox;
		}
	}

	private static HorizontalLayout createFieldLayout(final MetaData meta,
		final Component component) {
		Check.notNull(meta, "MetaData for field layout");
		Check.notNull(component,
			("Component for field layout" + meta.displayName()) != null
				? meta.displayName() : "unknown");
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setClassName("form-field-layout");
		horizontalLayout.setWidthFull();
		horizontalLayout.setPadding(false);
		horizontalLayout.setSpacing(false);
		horizontalLayout.setMargin(false);
		horizontalLayout.setJustifyContentMode(JustifyContentMode.START);
		horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
		// Safe handling of display name
		final String displayName =
			((meta.displayName() != null) && !meta.displayName().trim().isEmpty())
				? meta.displayName() : "Field";
		final Div labelDiv = new Div(displayName);
		labelDiv.setClassName("form-field-label");

		if (meta.required()) {
			labelDiv.getStyle().set("font-weight", "bold");
			// LOGGER.debug("Applied bold styling for required field: {}", displayName);
		}
		horizontalLayout.add(labelDiv);
		horizontalLayout.add(component);
		// LOGGER.debug("Successfully created field layout for: {}", displayName);
		return horizontalLayout;
	}

	private static NumberField createFloatingPointField(final Field field,
		final MetaData meta, final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for floating point field creation");
		Check.notNull(meta, "MetaData for floating point field creation");
		Check.notNull(binder, "Binder for floating point field creation");
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for floating point fields
		numberField.setStep(0.01);

		// Set default value if specified
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(meta.defaultValue());
			numberField.setValue(defaultVal);
		}
		safeBindComponent(binder, numberField, field.getName(), "NumberField");
		return numberField;
	}

	private static NumberField createIntegerField(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for integer field creation");
		Check.notNull(meta, "MetaData for integer field creation");
		Check.notNull(binder, "Binder for integer field creation");
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for integer fields
		numberField.setStep(1);

		// Set default value if specified
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				final double defaultVal = Double.parseDouble(meta.defaultValue());
				numberField.setValue(defaultVal);
				LOGGER.debug("Set default value '{}' for integer field '{}'", defaultVal,
					field.getName());
			} catch (final NumberFormatException e) {
				LOGGER.error(
					"Failed to parse default value '{}' as number for field '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}
		// Handle different integer types with proper conversion
		final Class<?> fieldType = field.getType();

		try {
			final String propertyName = getPropertyName(field);

			if ((fieldType == Integer.class) || (fieldType == int.class)) {
				binder.forField(numberField)
					.withConverter(value -> value != null ? value.intValue() : null,
						value -> value != null ? value.doubleValue() : null,
						"Invalid integer value")
					.bind(propertyName);
				LOGGER.debug(
					"Successfully bound NumberField with Integer converter for field '{}'",
					field.getName());
			}
			else if ((fieldType == Long.class) || (fieldType == long.class)) {
				binder.forField(numberField)
					.withConverter(value -> value != null ? value.longValue() : null,
						value -> value != null ? value.doubleValue() : null,
						"Invalid long value")
					.bind(propertyName);
				LOGGER.debug(
					"Successfully bound NumberField with Long converter for field '{}'",
					field.getName());
			}
			else {
				// Fallback for other number types (Double, etc.)
				binder.bind(numberField, propertyName);
			}
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind integer field for field '{}': {} - using fallback binding",
				field.getName(), e.getMessage());

			// Fallback to simple binding without converter
			try {
				safeBindComponentWithField(binder, numberField, field,
					"NumberField(Integer-fallback)");
			} catch (final Exception fallbackException) {
				LOGGER.error("Fallback binding also failed for integer field '{}': {}",
					field.getName(), fallbackException.getMessage());
			}
		}
		return numberField;
	}

	private static TextArea createTextArea(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for text area creation");
		Check.notNull(meta, "MetaData for text area creation");
		Check.notNull(binder, "Binder for text area creation");
		final TextArea item = new TextArea();

		if (meta.maxLength() > 0) {
			item.setMaxLength(meta.maxLength());
		}
		item.setWidthFull();
		item.setMinHeight("100px");

		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				item.setValue(meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, item, field, "TextArea");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text area for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return item;
	}

	private static TextField createTextField(final Field field, final MetaData meta,
		final CEnhancedBinder<?> binder) {
		Check.notNull(field, "Field for text field creation");
		Check.notNull(meta, "MetaData for text field creation");
		Check.notNull(binder, "Binder for text field creation");
		final TextField item = new TextField();
		// Set ID for better test automation
		CAuxillaries.setId(item);
		item.setClassName("plain-look-textfield");

		if (meta.maxLength() > 0) {
			item.setMaxLength(meta.maxLength());
		}
		item.setWidthFull();

		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {

			try {
				item.setValue(meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}",
					meta.defaultValue(), field.getName(), e.getMessage());
			}
		}

		try {
			safeBindComponentWithField(binder, item, field, "TextField");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text field for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return item;
	}

	private static void getListOfAllFields(final Class<?> entityClass,
		final List<Field> allFields) {
		Class<?> current = entityClass;

		while ((current != null) && (current != Object.class)) {
			final Field[] declaredFields = current.getDeclaredFields();

			if (declaredFields != null) {
				allFields.addAll(Arrays.asList(declaredFields));
			}
			else {
				LOGGER.warn("getDeclaredFields() returned null for class: {}",
					current.getSimpleName());
			}
			current = current.getSuperclass();
		}
	}

	/**
	 * Cached instance of the data provider resolver for performance.
	 */
	/**
	 * Determines the correct property name for binding. Simply returns the field name for
	 * most cases.
	 */
	private static String getPropertyName(final Field field) {
		Check.notNull(field, "Field for property name retrieval");
		return field.getName();
	}

	private static List<Field> getSortedFilteredFieldsList(final List<Field> allFields) {
		return allFields.stream().filter(field -> {

			if (field == null) {
				LOGGER.warn("Null field encountered during filtering");
				return false;
			}
			return !java.lang.reflect.Modifier.isStatic(field.getModifiers());
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);

			if (metaData == null) {
				return false;
			}
			return true;
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);

			if (metaData.hidden()) {
				return false;
			}
			return true;
		}).sorted(Comparator.comparingInt(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);
			return metaData != null ? metaData.order() : Integer.MAX_VALUE;
		})).collect(Collectors.toList());
	}

	private static <EntityClass> int processMetaForField(
		final CEnhancedBinder<EntityClass> binder, final VerticalLayout formLayout,
		int processedComponents, final Field field) {
		Check.notNull(field, "field");
		final MetaData meta = field.getAnnotation(MetaData.class);
		Check.notNull(meta, "MetaData for field " + field.getName());
		// LOGGER.debug("Creating component for field '{}' with displayName
		// '{}'",field.getName(), meta.displayName());
		final Component component = createComponentForField(field, meta, binder);
		Check.notNull(component, "Component for field " + field.getName()
			+ " with displayName " + meta.displayName());
		final HorizontalLayout horizontalLayout = createFieldLayout(meta, component);
		Check.notNull(horizontalLayout, "HorizontalLayout for field " + field.getName()
			+ " with displayName " + meta.displayName());
		formLayout.add(horizontalLayout);
		processedComponents++;
		return processedComponents;
	}

	/**
	 * Recursively searches for ComboBox components and resets them to their first item.
	 */
	@SuppressWarnings ("unchecked")
	private static void resetComboBoxesRecursively(
		final com.vaadin.flow.component.HasComponents container) {
		container.getElement().getChildren().forEach(element -> {

			// Get the component from the element
			if (element.getComponent().isPresent()) {
				final com.vaadin.flow.component.Component component =
					element.getComponent().get();

				if (component instanceof ComboBox) {
					final ComboBox<Object> comboBox = (ComboBox<Object>) component;

					try {
						// Get the first item from the ComboBox data provider
						final java.util.Optional<Object> firstItem =
							comboBox.getDataProvider()
								.fetch(new com.vaadin.flow.data.provider.Query<>())
								.findFirst();

						if (firstItem.isPresent()) {
							comboBox.setValue(firstItem.get());
							LOGGER.debug("Reset ComboBox to first item: {}",
								firstItem.get());
						}
						else {
							LOGGER.debug("ComboBox has no items to reset to");
						}
					} catch (final Exception e) {
						LOGGER.warn("Error resetting ComboBox to first item: {}",
							e.getMessage());
					}
				}
				else if (component instanceof com.vaadin.flow.component.HasComponents) {
					// Recursively check child components
					resetComboBoxesRecursively(
						(com.vaadin.flow.component.HasComponents) component);
				}
			}
		});
	}

	/**
	 * Resets all ComboBox components in a container to their first available item. This
	 * method is useful for implementing "New" button behavior where ComboBoxes should
	 * default to their first option instead of being empty.
	 * @param container the container component to search for ComboBoxes
	 */
	public static void resetComboBoxesToFirstItem(
		final com.vaadin.flow.component.HasComponents container) {
		Check.notNull(container, "Container for resetting ComboBoxes to first item");
		resetComboBoxesRecursively(container);
	}

	/**
	 * Safely binds a component to a field, ensuring no incomplete bindings are left. This
	 * method prevents the "All bindings created with forField must be completed" error.
	 */
	private static void safeBindComponent(final CEnhancedBinder<?> binder,
		final HasValueAndElement<?, ?> component, final String fieldName,
		final String componentType) {
		Check.notNull(binder, "Binder for safe binding");
		Check.notNull(component, "Component for safe binding");
		Check.notNull(fieldName, "Field name for safe binding");
		LOGGER.debug("Attempting to bind {} for field '{}'", componentType, fieldName);

		try {
			binder.bind(component, fieldName);
		} catch (final Exception e) {
			LOGGER.error(
				"Failed to bind {} for field '{}': {} - this may cause incomplete bindings",
				componentType, fieldName, e.getMessage(), e);
			// Don't throw - just log the error to prevent form generation failure But
			// warn that this might cause incomplete bindings
			binder.removeBinding(fieldName); // Eğer CEnhancedBinder bunu destekliyorsa
		}
	}

	/**
	 * Safely binds a component to a field using the correct property name, ensuring no
	 * incomplete bindings are left.
	 */
	private static void safeBindComponentWithField(final CEnhancedBinder<?> binder,
		final HasValueAndElement<?, ?> component, final Field field,
		final String componentType) {

		if ((binder == null) || (component == null) || (field == null)) {
			LOGGER.error(
				"Null parameters in safeBindComponentWithField - binder: {}, component: {}, field: {}",
				binder != null ? "present" : "null",
				component != null ? "present" : "null",
				field != null ? field.getName() : "null");
			return;
		}
		final String propertyName = getPropertyName(field);
		safeBindComponent(binder, component, propertyName, componentType);
	}

	private static void setComponentWidth(final Component component,
		final MetaData meta) {

		if ((component == null) || (meta == null)) {
			return;
		}

		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize =
				(com.vaadin.flow.component.HasSize) component;

			if ((meta.width() != null) && !meta.width().trim().isEmpty()) {

				try {
					hasSize.setWidth(meta.width());
				} catch (final Exception e) {
					LOGGER.warn("Failed to set component width '{}': {}", meta.width(),
						e.getMessage());
					// Fall back to full width
					hasSize.setWidthFull();
				}
			}
			else {
				hasSize.setWidthFull();
			}
		}
	}

	private static void setRequiredIndicatorVisible(final MetaData meta,
		final Component field) {
		Check.notNull(meta, "MetaData for required indicator visibility");
		Check.notNull(field, "Field for required indicator visibility");
		((HasValueAndElement<?, ?>) field).setReadOnly(meta.readOnly());
		((HasValueAndElement<?, ?>) field).setRequiredIndicatorVisible(meta.required());
	}

	private CEntityFormBuilder() {
		// Spring component - constructor managed by Spring
	}

	/**
	 * Sets the application context and initializes the data provider resolver. This
	 * method is called automatically by Spring.
	 * @param context the Spring application context
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) {

		try {
			CEntityFormBuilder.dataProviderResolver =
				context.getBean(CDataProviderResolver.class);
		} catch (final Exception e) {
			LOGGER.warn(
				"Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}",
				e.getMessage());
			CEntityFormBuilder.dataProviderResolver = null;
		}
	}
}
