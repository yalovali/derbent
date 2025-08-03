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
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CEntityConstants;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.utils.CAuxillaries;

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

	/**
	 * Cached instance of the data provider resolver for performance.
	 */
	/**
	 * Safely binds a component to a field, ensuring no incomplete bindings are left.
	 * This method prevents the "All bindings created with forField must be completed" error.
	 */
	private static void safeBindComponent(final BeanValidationBinder<?> binder, 
		final HasValueAndElement<?, ?> component, final String fieldName, 
		final String componentType) {
		if (binder == null || component == null || fieldName == null) {
			LOGGER.error("Null parameters in safeBindComponent - binder: {}, component: {}, fieldName: {}", 
				binder != null ? "present" : "null", 
				component != null ? "present" : "null", 
				fieldName != null ? fieldName : "null");
			return;
		}
		
		try {
			binder.bind(component, fieldName);
			LOGGER.debug("Successfully bound {} for field '{}'", componentType, fieldName);
		} catch (final Exception e) {
			LOGGER.error("Failed to bind {} for field '{}': {} - this may cause incomplete bindings", 
				componentType, fieldName, e.getMessage(), e);
			// Don't throw - just log the error to prevent form generation failure
			// But warn that this might cause incomplete bindings
		}
	}

	private static CDataProviderResolver dataProviderResolver;

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder) {
		return buildForm(entityClass, binder, null, null);
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider, final List<String> entityFields) {

		// Enhanced null pointer checking with detailed logging
		if (entityClass == null) {
			LOGGER.error("Entity class parameter is null - cannot build form");
			throw new IllegalArgumentException("Entity class cannot be null");
		}

		if (binder == null) {
			LOGGER.error(
				"Binder parameter is null for entity class: {} - cannot build form",
				entityClass.getSimpleName());
			throw new IllegalArgumentException("Binder cannot be null");
		}
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
			processedComponents = processMetaForField(binder, dataProvider, formLayout,
				processedComponents, field);
		}
		LOGGER.info(
			"Form generation completed. Successfully processed {} out of {} components",
			processedComponents, sortedFields.size());
		
		panel.add(formLayout);
		return panel;
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder, final List<String> entityFields) {
		return buildForm(entityClass, binder, null, entityFields);
	}

	public static <EntityClass> Div buildFormAll(final Class<?> entityClass,
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider) {
		return buildForm(entityClass, binder, dataProvider, null);
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

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
			safeBindComponent(binder, checkbox, field.getName(), "Checkbox");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return checkbox;
	}

	@SuppressWarnings ("unchecked")
	private static <T extends CEntityDB<T>> ComboBox<T> createComboBox(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder,
		final ComboBoxDataProvider dataProvider) {

		// Enhanced null pointer checking with detailed logging
		if (field == null) {
			LOGGER.error("Field parameter is null in createComboBox");
			return null;
		}

		if (meta == null) {
			LOGGER.error("MetaData parameter is null for field: {}", field.getName());
			return null;
		}

		if (binder == null) {
			LOGGER.error("Binder parameter is null for field: {}", field.getName());
			return null;
		}
		final Class<T> fieldType = (Class<T>) field.getType();
		// Check if this field has @ColorAwareComboBox annotation
		final tech.derbent.abstracts.annotations.ColorAwareComboBox colorAnnotation =
			field.getAnnotation(
				tech.derbent.abstracts.annotations.ColorAwareComboBox.class);
		final ComboBox<T> comboBox;

		// Use specialized color-aware ComboBox if annotation is present or if it's a
		// status entity
		if ((colorAnnotation != null)
			|| tech.derbent.abstracts.utils.CColorUtils.isStatusEntity(fieldType)) {
			LOGGER.debug("Creating CColorAwareComboBox for field: {}", field.getName());
			final tech.derbent.abstracts.components.CColorAwareComboBox<
				T> colorAwareComboBox =
					new tech.derbent.abstracts.components.CColorAwareComboBox<>(
						fieldType);

			if (colorAnnotation != null) {
				colorAwareComboBox.setAnnotationConfig(colorAnnotation);
			}
			comboBox = colorAwareComboBox;
		}
		else {
			LOGGER.debug("Creating standard ComboBox for field: {}", field.getName());
			comboBox = new ComboBox<>();
		}
		// Set ID for better test automation
		CAuxillaries.setId(comboBox);
		// Following coding guidelines: All selective ComboBoxes must be selection only
		// (user must not be able to type arbitrary text)
		comboBox.setAllowCustomValue(false);
		// Enhanced item label generator with null safety and proper display formatting
		// Fix for combobox display issue: use getName() for CEntityNamed entities instead
		// of toString()
		comboBox.setItemLabelGenerator(item -> tech.derbent.abstracts.utils.CColorUtils
			.getDisplayTextFromEntity(item));
		// Data provider resolution with priority order: 1. Legacy ComboBoxDataProvider
		// (if provided) - for backward compatibility 2. Annotation-based resolution using
		// CDataProviderResolver 3. Empty list as fallback
		List<T> items = null;

		// Priority 1: Use legacy data provider if provided
		if (dataProvider != null) {

			try {
				items = dataProvider.getItems(fieldType);

				if (items != null) {}
				else {
					LOGGER.warn(
						"Legacy data provider returned null for field '{}' of type '{}'",
						field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error(
					"Error using legacy data provider for field '{}' of type '{}': {}",
					field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}

		// Priority 2: Use annotation-based resolution if legacy provider didn't work
		if ((items == null) && (dataProviderResolver != null)) {

			try {
				items = dataProviderResolver.resolveData(fieldType, meta);

				if (items != null) {}
				else {
					LOGGER.warn(
						"Annotation-based resolver returned null for field '{}' of type '{}'",
						field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error(
					"Error using annotation-based data resolver for field '{}' of type '{}': {}",
					field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}

		// Priority 3: Fallback to empty list
		if (items == null) {
			LOGGER.warn(
				"No data provider could supply items for field '{}' of type '{}' - using empty list",
				field.getName(), fieldType.getSimpleName());
			items = List.of();
		}

		// Set items on ComboBox with validation
		try {
			comboBox.setItems(items);

			// Set default value to first item if available and no default value specified
			// in metadata This ensures ComboBoxes are not empty when forms are
			// created/cleared
			if (!items.isEmpty() && ((meta.defaultValue() == null)
				|| meta.defaultValue().trim().isEmpty())) {
				comboBox.setValue(items.get(0));
				LOGGER.debug(
					"Set ComboBox default value to first item for field '{}': {}",
					field.getName(), items.get(0));
			}
		} catch (final Exception e) {
			LOGGER.error("Error setting items on ComboBox for field '{}': {}",
				field.getName(), e.getMessage(), e);
			comboBox.setItems(List.of()); // Fallback to empty list
		}
		// Use simple binding for ComboBox to avoid incomplete forField bindings
		// The complex converter logic was causing incomplete bindings
		safeBindComponent(binder, comboBox, field.getName(), "ComboBox");
		return comboBox;
	}

	private static Component createComponentForField(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder,
		final ComboBoxDataProvider dataProvider) {
		Component component = null;

		// Enhanced null pointer checking
		if (field == null) {
			LOGGER.error("Field parameter is null");
			return null;
		}

		if (meta == null) {
			LOGGER.error("MetaData parameter is null for field: {}", field.getName());
			return null;
		}

		if (binder == null) {
			LOGGER.error("Binder parameter is null for field: {}", field.getName());
			return null;
		}
		final Class<?> fieldType = field.getType();

		if (fieldType == null) {
			LOGGER.error("Field type is null for field: {}", field.getName());
			return null;
		}
		LOGGER.debug(
			"Creating component for field '{}' of type '{}' with displayName '{}'",
			field.getName(), fieldType.getSimpleName(), meta.displayName());

		// Handle different field types with detailed logging
		if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
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
		else if ((fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)
			|| (fieldType == BigDecimal.class)) {
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
		else if (CEntityDB.class.isAssignableFrom(fieldType)) {
			component = createComboBox(field, meta, binder, dataProvider);
		}
		else {
			LOGGER.warn(
				"Unsupported field type '{}' for field '{}' - no component created",
				fieldType.getSimpleName(), field.getName());
			return null;
		}

		// Null check before proceeding with component configuration
		if (component == null) {
			LOGGER.error("Component creation failed for field '{}' of type '{}'",
				field.getName(), fieldType.getSimpleName());
			return null;
		}
		setRequiredIndicatorVisible(meta, component);
		// dont use helper text for Checkbox components setHelperText(meta, component);
		setComponentWidth(component, meta);
		// setclass name for styling in format of form-field{ComponentType}
		component.setClassName("form-field-" + component.getClass().getSimpleName());
		// Create field
		return component;
	}

	private static DatePicker createDatePicker(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		// Set ID for better test automation
		CAuxillaries.setId(datePicker);
		try {
			safeBindComponent(binder, datePicker, field.getName(), "DatePicker");
		} catch (final Exception e) {
			LOGGER.error(
				"Error binding DatePicker for field '{}': {}",
				field.getName(), e.getMessage(), e);
		}
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final Field field,
		final MetaData meta, final BeanValidationBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();
		// Set ID for better test automation
		CAuxillaries.setId(dateTimePicker);

		// Use simple binding for now to avoid incomplete forField bindings
		// TODO: Add back converter for Instant fields once binding issue is resolved
		safeBindComponent(binder, dateTimePicker, field.getName(), "DateTimePicker");
		return dateTimePicker;
	}

	@SuppressWarnings ({
		"unchecked", "rawtypes" }
	)
	private static Component createEnumComponent(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {
		final Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
		final Enum[] enumConstants = enumType.getEnumConstants();

		if (meta.useRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			safeBindComponent(binder, radioGroup, field.getName(), "RadioButtonGroup");
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
			safeBindComponent(binder, comboBox, field.getName(), "ComboBox(Enum)");
			return comboBox;
		}
	}

	private static HorizontalLayout createFieldLayout(final MetaData meta,
		final Component component) {

		if (meta == null) {
			LOGGER.error("MetaData is null in createFieldLayout");
			return null;
		}

		if (component == null) {
			LOGGER.error("Component is null in createFieldLayout for displayName: {}",
				meta.displayName() != null ? meta.displayName() : "unknown");
			return null;
		}
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
		final MetaData meta, final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createFloatingPointField - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for floating point fields
		numberField.setStep(0.01);

		// Set default value if specified
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
		// Use simple binding for now to avoid incomplete forField bindings
		// TODO: Add back type-specific converters once binding issue is resolved
		safeBindComponent(binder, numberField, field.getName(), "NumberField");
		return numberField;
	}

	private static NumberField createIntegerField(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createIntegerField - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
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
			if ((fieldType == Integer.class) || (fieldType == int.class)) {
				binder.forField(numberField)
					.withConverter(value -> value != null ? value.intValue() : null,
						value -> value != null ? value.doubleValue() : null)
					.bind(field.getName());
				LOGGER.debug("Successfully bound NumberField with Integer converter for field '{}'", field.getName());
			}
			else if ((fieldType == Long.class) || (fieldType == long.class)) {
				binder.forField(numberField)
					.withConverter(value -> value != null ? value.longValue() : null,
						value -> value != null ? value.doubleValue() : null)
					.bind(field.getName());
				LOGGER.debug("Successfully bound NumberField with Long converter for field '{}'", field.getName());
			}
			else {
				// Fallback for other number types (Double, etc.)
				safeBindComponent(binder, numberField, field.getName(), "NumberField");
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to bind integer field for field '{}': {} - trying simple binding",
				field.getName(), e.getMessage());
			safeBindComponent(binder, numberField, field.getName(), "NumberField");
		}
		return numberField;
	}

	private static TextArea createTextArea(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createTextArea - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
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
			safeBindComponent(binder, item, field.getName(), "TextArea");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text area for field '{}': {}", field.getName(),
				e.getMessage());
			return null;
		}
		return item;
	}

	private static TextField createTextField(final Field field, final MetaData meta,
		final BeanValidationBinder<?> binder) {

		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error(
				"Null parameters in createTextArea - field: {}, meta: {}, binder: {}",
				field != null ? field.getName() : "null",
				meta != null ? "present" : "null", binder != null ? "present" : "null");
			return null;
		}
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
			safeBindComponent(binder, item, field.getName(), "TextField");
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
		final BeanValidationBinder<EntityClass> binder,
		final ComboBoxDataProvider dataProvider, final VerticalLayout formLayout,
		int processedComponents, final Field field) {

		if (field == null) {
			LOGGER.warn("Null field encountered in sorted fields list");
			return processedComponents;
		}
		final MetaData meta = field.getAnnotation(MetaData.class);

		if (meta == null) {
			LOGGER.warn("Field '{}' lost MetaData annotation during processing",
				field.getName());
			return processedComponents;
		}

		try {
			// LOGGER.debug("Creating component for field '{}' with displayName
			// '{}'",field.getName(), meta.displayName());
			final Component component =
				createComponentForField(field, meta, binder, dataProvider);

			if (component != null) {
				final HorizontalLayout horizontalLayout =
					createFieldLayout(meta, component);

				if (horizontalLayout != null) {
					formLayout.add(horizontalLayout);
					processedComponents++;
				}
				else {
					LOGGER.warn("createFieldLayout returned null for field '{}'",
						field.getName());
				}
			}
			else {
				LOGGER.warn(
					"createComponentForField returned null for field '{}' of type {}",
					field.getName(), field.getType().getSimpleName());
			}
		} catch (final Exception e) {
			LOGGER.error(
				"Error creating component for field '{}' of type {} with MetaData displayName '{}': {}",
				field.getName(), field.getType().getSimpleName(), meta.displayName(),
				e.getMessage(), e);
		}
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

		if (container == null) {
			LOGGER.warn("Container is null in resetComboBoxesToFirstItem");
			return;
		}
		resetComboBoxesRecursively(container);
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

		if ((field == null) || (meta == null)
			|| ((field instanceof HasValueAndElement) == false)) {
			LOGGER.warn("cannot set helper text");
			return;
		}
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
