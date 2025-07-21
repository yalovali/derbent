package tech.derbent.abstracts.annotations;

import java.lang.reflect.Field;
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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * CEntityFormBuilder - Utility class for building forms from entity classes
 * using MetaData annotations. Supports automatic form generation for various
 * field types including text, boolean, enum, numeric, and entity references.
 * Layer: Utility (MVC)
 */
public final class CEntityFormBuilder {

	/**
	 * Interface for providing data to ComboBox components. Implementations should
	 * provide lists of entities for specific entity types.
	 */
	public interface ComboBoxDataProvider {

		/**
		 * Gets items for a specific entity type.
		 * @param entityType the class type of the entity
		 * @return list of entities to populate the ComboBox
		 */
		<T extends CEntityDB> List<T> getItems(Class<T> entityType);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityFormBuilder.class);
	protected static final String LabelMinWidth_210PX = "210px";

	public static <EntityClass> Div buildForm(final Class<?> entityClass, final BeanValidationBinder<EntityClass> binder) {
		return buildForm(entityClass, binder, null);
	}

	public static <EntityClass> Div buildForm(final Class<?> entityClass, final BeanValidationBinder<EntityClass> binder, final ComboBoxDataProvider dataProvider) {
		LOGGER.info("Building form for entity class: {}", entityClass.getSimpleName());
		if (entityClass == null) {
			throw new IllegalArgumentException("Entity class cannot be null");
		}
		if (binder == null) {
			throw new IllegalArgumentException("Binder cannot be null");
		}
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		final FormLayout formLayout = new FormLayout();
		// Collect all fields from the class hierarchy
		final List<Field> allFields = new ArrayList<>();
		Class<?> current = entityClass;
		while ((current != null) && (current != Object.class)) {
			allFields.addAll(Arrays.asList(current.getDeclaredFields()));
			current = current.getSuperclass();
		}
		// print declared fields for debugging
		for (final Field field : allFields) {
			LOGGER.debug("Field: {} Type: {} ", field.getName(), field.getType().getSimpleName());
			LOGGER.debug("Modifiers: {}", java.lang.reflect.Modifier.toString(field.getModifiers()));
			LOGGER.debug("Annotations: {}", Arrays.toString(field.getDeclaredAnnotations()));
		}
		// Get all fields and sort by MetaData order
		final List<Field> sortedFields = allFields.stream().filter(field -> !java.lang.reflect.Modifier.isStatic(field.getModifiers())).filter(field -> field.getAnnotation(MetaData.class) != null)
			.filter(field -> !field.getAnnotation(MetaData.class).hidden()).sorted(Comparator.comparingInt(field -> field.getAnnotation(MetaData.class).order())).collect(Collectors.toList());
		for (final Field field : sortedFields) {
			final MetaData meta = field.getAnnotation(MetaData.class);
			try {
				final Component component = createComponentForField(field, meta, binder, dataProvider);
				if (component != null) {
					final HorizontalLayout horizontalLayout = createFieldLayout(meta, component);
					formLayout.add(horizontalLayout);
				}
			} catch (final Exception e) {
				LOGGER.error("Error creating component for field: {}", field.getName(), e);
			}
		}
		panel.add(formLayout);
		return panel;
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final Checkbox checkbox = new Checkbox();
		checkbox.setRequiredIndicatorVisible(meta.required());
		checkbox.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			checkbox.setHelperText(meta.description());
		}
		if (!meta.defaultValue().isEmpty()) {
			checkbox.setValue(Boolean.parseBoolean(meta.defaultValue()));
		}
		setComponentWidth(checkbox, meta);
		binder.bind(checkbox, field.getName());
		return checkbox;
	}

	@SuppressWarnings("unchecked")
	private static <T extends CEntityDB> ComboBox<T> createComboBox(final Field field, final MetaData meta, final BeanValidationBinder<?> binder, final ComboBoxDataProvider dataProvider) {
		final ComboBox<T> comboBox = new ComboBox<>();
		comboBox.setRequiredIndicatorVisible(meta.required());
		comboBox.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			comboBox.setHelperText(meta.description());
		}
		comboBox.setClassName("form-field-combobox");
		setComponentWidth(comboBox, meta);
		comboBox.setItemLabelGenerator(item -> item.toString()); // <-- burası düzeltildi
		try {
			final List<T> items = dataProvider.getItems((Class<T>) field.getType());
			if ((items == null) || items.isEmpty()) {
				LOGGER.warn("DataProvider returned empty list for field: {}", field.getName());
			}
			comboBox.setItems(items != null ? items : List.of());
		} catch (final Exception e) {
			LOGGER.error("Error loading data for combobox field: {}", field.getName(), e);
			comboBox.setItems(List.of());
		}
		binder.bind(comboBox, field.getName());
		return comboBox;
	}

	private static Component createComponentForField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder, final ComboBoxDataProvider dataProvider) {
		final Class<?> fieldType = field.getType();
		// Handle different field types
		if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			return createCheckbox(field, meta, binder);
		}
		else if (fieldType == String.class) {
			return createTextField(field, meta, binder);
		}
		else if ((fieldType == Integer.class) || (fieldType == int.class) || (fieldType == Long.class) || (fieldType == long.class) || (fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)) {
			return createNumberField(field, meta, binder);
		}
		else if (fieldType == LocalDate.class) {
			return createDatePicker(field, meta, binder);
		}
		else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			return createDateTimePicker(field, meta, binder);
		}
		else if (fieldType.isEnum()) {
			return createEnumComponent(field, meta, binder);
		}
		else if (CEntityDB.class.isAssignableFrom(fieldType) && (dataProvider != null)) {
			return createComboBox(field, meta, binder, dataProvider);
		}
		else {
			LOGGER.warn("Unsupported field type: {} for field: {}", fieldType, field.getName());
			return null;
		}
	}

	private static DatePicker createDatePicker(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		datePicker.setRequiredIndicatorVisible(meta.required());
		datePicker.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			datePicker.setHelperText(meta.description());
		}
		datePicker.setClassName("form-field-date");
		setComponentWidth(datePicker, meta);
		binder.bind(datePicker, field.getName());
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();
		dateTimePicker.setRequiredIndicatorVisible(meta.required());
		dateTimePicker.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			dateTimePicker.setHelperText(meta.description());
		}
		dateTimePicker.setClassName("form-field-datetime");
		setComponentWidth(dateTimePicker, meta);
		// For Instant fields, we need a custom converter
		if (field.getType() == Instant.class) {
			binder.forField(dateTimePicker).withConverter(localDateTime -> localDateTime != null ? localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant() : null,
				instant -> instant != null ? instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null).bind(field.getName());
		}
		else {
			binder.bind(dateTimePicker, field.getName());
		}
		return dateTimePicker;
	}

	@SuppressWarnings({
		"unchecked", "rawtypes" })
	private static Component createEnumComponent(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final Class<? extends Enum> enumType = (Class<? extends Enum>) field.getType();
		final Enum[] enumConstants = enumType.getEnumConstants();
		if (meta.useRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			radioGroup.setRequiredIndicatorVisible(meta.required());
			radioGroup.setReadOnly(meta.readOnly());
			if (!meta.description().isEmpty()) {
				radioGroup.setHelperText(meta.description());
			}
			setComponentWidth(radioGroup, meta);
			binder.bind(radioGroup, field.getName());
			return radioGroup;
		}
		else {
			final ComboBox<Enum> comboBox = new ComboBox<>();
			comboBox.setItems(enumConstants);
			comboBox.setItemLabelGenerator(Enum::name);
			comboBox.setRequiredIndicatorVisible(meta.required());
			comboBox.setReadOnly(meta.readOnly());
			if (!meta.description().isEmpty()) {
				comboBox.setHelperText(meta.description());
			}
			comboBox.setClassName("form-field-enum");
			setComponentWidth(comboBox, meta);
			binder.bind(comboBox, field.getName());
			return comboBox;
		}
	}

	private static HorizontalLayout createFieldLayout(final MetaData meta, final Component component) {
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setPadding(false);
		horizontalLayout.setSpacing(false);
		horizontalLayout.setMargin(false);
		horizontalLayout.setJustifyContentMode(JustifyContentMode.START);
		horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
		final Div labelDiv = new Div(meta.displayName());
		labelDiv.setMinWidth(LabelMinWidth_210PX);
		if (meta.required()) {
			labelDiv.getStyle().set("font-weight", "bold");
		}
		horizontalLayout.add(labelDiv);
		horizontalLayout.add(component);
		return horizontalLayout;
	}

	private static NumberField createNumberField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final NumberField numberField = new NumberField();
		numberField.setRequiredIndicatorVisible(meta.required());
		numberField.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			numberField.setHelperText(meta.description());
		}
		if (meta.min() != Double.MIN_VALUE) {
			numberField.setMin(meta.min());
		}
		if (meta.max() != Double.MAX_VALUE) {
			numberField.setMax(meta.max());
		}
		if (!meta.defaultValue().isEmpty()) {
			try {
				numberField.setValue(Double.parseDouble(meta.defaultValue()));
			} catch (final NumberFormatException e) {
				LOGGER.warn("Invalid default numeric value: {} for field: {}", meta.defaultValue(), field.getName());
			}
		}
		numberField.setClassName("form-field-number");
		setComponentWidth(numberField, meta);
		binder.bind(numberField, field.getName());
		return numberField;
	}

	private static TextField createTextField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		final TextField textField = new TextField();
		textField.setRequiredIndicatorVisible(meta.required());
		textField.setReadOnly(meta.readOnly());
		if (!meta.description().isEmpty()) {
			textField.setHelperText(meta.description());
		}
		if (!meta.defaultValue().isEmpty()) {
			textField.setValue(meta.defaultValue());
		}
		if (meta.maxLength() > 0) {
			textField.setMaxLength(meta.maxLength());
		}
		textField.setClassName("form-field-text");
		setComponentWidth(textField, meta);
		binder.bind(textField, field.getName());
		return textField;
	}

	private static void setComponentWidth(final Component component, final MetaData meta) {
		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize = (com.vaadin.flow.component.HasSize) component;
			if (!meta.width().isEmpty()) {
				hasSize.setWidth(meta.width());
			}
			else {
				hasSize.setWidthFull();
			}
		}
	}

	private CEntityFormBuilder() {
		// Utility class - prevent instantiation
	}
}
