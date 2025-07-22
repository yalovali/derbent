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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

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
 * <p>
 * <strong>Enhanced ComboBox Data Provider Support:</strong> This form builder
 * now supports annotation-based data provider configuration, making it more
 * generic and maintainable. ComboBox fields can specify their data providers
 * using MetaData annotations in three ways:
 * </p>
 * <ol>
 * <li><strong>Bean Name:</strong>
 * {@code @MetaData(dataProviderBean = "activityTypeService")}</li>
 * <li><strong>Bean Class:</strong>
 * {@code @MetaData(dataProviderClass = CActivityTypeService.class)}</li>
 * <li><strong>Automatic:</strong> Automatically resolves service by entity type
 * naming convention</li>
 * </ol>
 * <p>
 * <strong>Backward Compatibility:</strong> The traditional ComboBoxDataProvider
 * approach is still supported and takes precedence when provided. This allows
 * existing code to continue working while new code can benefit from the
 * annotation-based approach.
 * </p>
 * <p>
 * <strong>Usage Examples:</strong>
 * </p>
 *
 * <pre>
 * 
 * {
 * 	&#64;code
 * 	// New annotation-based approach - no data provider needed in view code
 * 	public class CActivity extends CEntityOfProject {
 * 
 * 		&#64;MetaData(displayName = "Activity Type", dataProviderBean = "activityTypeService")
 * 		private CActivityType activityType;
 * 		@MetaData(displayName = "Assigned User", dataProviderClass = CUserService.class, dataProviderMethod = "findAllActive")
 * 		private CUser assignedUser;
 * 	}
 * 	// In view - much simpler now
 * 	Div form = CEntityFormBuilder.buildForm(CActivity.class, binder); // No data provider needed!
 * 	// Legacy approach still works
 * 	ComboBoxDataProvider provider = new ComboBoxDataProvider() {
 * 
 * 		public List getItems(Class entityType) {
 * 			return customLogic(entityType);
 * 		}
 * 	};
 * 	Div form = CEntityFormBuilder.buildForm(CActivity.class, binder, provider);
 * }
 * </pre>
 *
 * Layer: Utility (MVC)
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.MetaData
 * @see tech.derbent.abstracts.annotations.CDataProviderResolver
 */
@org.springframework.stereotype.Component
public final class CEntityFormBuilder implements ApplicationContextAware {

	/**
	 * Interface for providing data to ComboBox components. Implementations should
	 * provide lists of entities for specific entity types.
	 * <p>
	 * <strong>Note:</strong> This interface is maintained for backward
	 * compatibility. New implementations should use the annotation-based approach
	 * with MetaData annotations.
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
		<T extends CEntityDB> List<T> getItems(Class<T> entityType);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityFormBuilder.class);
	protected static final String LabelMinWidth_210PX = "210px";
	/**
	 * Spring application context for accessing the data provider resolver. Set via
	 * ApplicationContextAware interface.
	 */
	private static ApplicationContext applicationContext;
	/**
	 * Cached instance of the data provider resolver for performance.
	 */
	private static CDataProviderResolver dataProviderResolver;

	/**
	 * Builds a form layout for the specified entity class using automatic data
	 * provider resolution.
	 * <p>
	 * This method uses the new annotation-based approach to automatically resolve
	 * data providers for ComboBox fields based on their MetaData annotations. No
	 * explicit data provider is required.
	 * </p>
	 * <p>
	 * <strong>ComboBox Data Resolution:</strong> ComboBox fields will have their
	 * data automatically resolved using:
	 * </p>
	 * <ul>
	 * <li>MetaData.dataProviderBean() - specified Spring bean name</li>
	 * <li>MetaData.dataProviderClass() - specified Spring bean class</li>
	 * <li>Automatic resolution based on entity type naming convention</li>
	 * </ul>
	 * @param <EntityClass> the entity class type for the binder
	 * @param entityClass   the class of the entity to create a form for
	 * @param binder        the Vaadin binder for data binding
	 * @return a Div containing the generated form layout
	 * @throws IllegalArgumentException if entityClass or binder is null
	 * @see #buildForm(Class, BeanValidationBinder, ComboBoxDataProvider)
	 */
	public static <EntityClass> Div buildForm(final Class<?> entityClass, final BeanValidationBinder<EntityClass> binder) {
		return buildForm(entityClass, binder, null);
	}

	/**
	 * Builds a form layout for the specified entity class with optional legacy data
	 * provider.
	 * <p>
	 * This method supports both the new annotation-based approach and the legacy
	 * ComboBoxDataProvider approach:
	 * </p>
	 * <ul>
	 * <li><strong>Legacy Provider Priority:</strong> If dataProvider is not null,
	 * it takes precedence over annotations</li>
	 * <li><strong>Annotation-Based:</strong> If dataProvider is null, uses MetaData
	 * annotations for data resolution</li>
	 * <li><strong>Fallback:</strong> If neither approach provides data, ComboBox
	 * will be empty</li>
	 * </ul>
	 * @param <EntityClass> the entity class type for the binder
	 * @param entityClass   the class of the entity to create a form for
	 * @param binder        the Vaadin binder for data binding
	 * @param dataProvider  optional legacy data provider (null to use
	 *                      annotation-based approach)
	 * @return a Div containing the generated form layout
	 * @throws IllegalArgumentException if entityClass or binder is null
	 */
	public static <EntityClass> Div buildForm(final Class<?> entityClass, final BeanValidationBinder<EntityClass> binder, final ComboBoxDataProvider dataProvider) {
		// Enhanced null pointer checking with detailed logging
		if (entityClass == null) {
			LOGGER.error("Entity class parameter is null - cannot build form");
			throw new IllegalArgumentException("Entity class cannot be null");
		}
		if (binder == null) {
			LOGGER.error("Binder parameter is null for entity class: {} - cannot build form", entityClass.getSimpleName());
			throw new IllegalArgumentException("Binder cannot be null");
		}
		LOGGER.info("Building form for entity class: {} with dataProvider: {}", entityClass.getSimpleName(), dataProvider != null ? "provided" : "null");
		LOGGER.debug("Entity class details - Name: {}, Package: {}, Modifiers: {}", entityClass.getSimpleName(), entityClass.getPackageName(),
			java.lang.reflect.Modifier.toString(entityClass.getModifiers()));
		final Div panel = new Div();
		panel.setClassName("editor-layout");
		final FormLayout formLayout = new FormLayout();
		// Collect all fields from the class hierarchy with enhanced logging
		final List<Field> allFields = new ArrayList<>();
		Class<?> current = entityClass;
		int hierarchyLevel = 0;
		while ((current != null) && (current != Object.class)) {
			final Field[] declaredFields = current.getDeclaredFields();
			if (declaredFields != null) {
				allFields.addAll(Arrays.asList(declaredFields));
				LOGGER.debug("Hierarchy level {}: Class {} contributed {} fields", hierarchyLevel, current.getSimpleName(), declaredFields.length);
			}
			else {
				LOGGER.warn("getDeclaredFields() returned null for class: {}", current.getSimpleName());
			}
			current = current.getSuperclass();
			hierarchyLevel++;
		}
		LOGGER.debug("Total fields collected from hierarchy: {}", allFields.size());
		// Enhanced field debugging with null checking
		for (final Field field : allFields) {
			if (field != null) {
				LOGGER.debug("Field analysis - Name: {}, Type: {}, Modifiers: {}", field.getName(), field.getType() != null ? field.getType().getSimpleName() : "null",
					java.lang.reflect.Modifier.toString(field.getModifiers()));
				// Log MetaData annotation details if present
				final MetaData metaData = field.getAnnotation(MetaData.class);
				if (metaData != null) {
					LOGGER.debug("MetaData found - DisplayName: '{}', Order: {}, Required: {}, Hidden: {}", metaData.displayName(), metaData.order(), metaData.required(), metaData.hidden());
				}
				else {
					LOGGER.debug("No MetaData annotation found for field: {}", field.getName());
				}
			}
			else {
				LOGGER.warn("Null field encountered in field list");
			}
		}
		// Filter and sort fields with enhanced null checking and logging
		final List<Field> sortedFields = allFields.stream().filter(field -> {
			if (field == null) {
				LOGGER.warn("Null field encountered during filtering");
				return false;
			}
			return !java.lang.reflect.Modifier.isStatic(field.getModifiers());
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);
			if (metaData == null) {
				LOGGER.debug("Field '{}' has no MetaData annotation - excluding from form", field.getName());
				return false;
			}
			return true;
		}).filter(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);
			if (metaData.hidden()) {
				LOGGER.debug("Field '{}' marked as hidden - excluding from form", field.getName());
				return false;
			}
			return true;
		}).sorted(Comparator.comparingInt(field -> {
			final MetaData metaData = field.getAnnotation(MetaData.class);
			return metaData != null ? metaData.order() : Integer.MAX_VALUE;
		})).collect(Collectors.toList());
		LOGGER.info("Processing {} visible fields for form generation", sortedFields.size());
		// Create components with enhanced error handling and logging
		int processedComponents = 0;
		for (final Field field : sortedFields) {
			if (field == null) {
				LOGGER.warn("Null field encountered in sorted fields list");
				continue;
			}
			final MetaData meta = field.getAnnotation(MetaData.class);
			if (meta == null) {
				LOGGER.warn("Field '{}' lost MetaData annotation during processing", field.getName());
				continue;
			}
			try {
				LOGGER.debug("Creating component for field '{}' with displayName '{}'", field.getName(), meta.displayName());
				final Component component = createComponentForField(field, meta, binder, dataProvider);
				if (component != null) {
					final HorizontalLayout horizontalLayout = createFieldLayout(meta, component);
					if (horizontalLayout != null) {
						formLayout.add(horizontalLayout);
						processedComponents++;
						LOGGER.debug("Successfully added component for field '{}'", field.getName());
					}
					else {
						LOGGER.warn("createFieldLayout returned null for field '{}'", field.getName());
					}
				}
				else {
					LOGGER.warn("createComponentForField returned null for field '{}' of type {}", field.getName(), field.getType().getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error("Error creating component for field '{}' of type {} with MetaData displayName '{}': {}", field.getName(), field.getType().getSimpleName(), meta.displayName(),
					e.getMessage(), e);
			}
		}
		LOGGER.info("Form generation completed. Successfully processed {} out of {} components", processedComponents, sortedFields.size());
		panel.add(formLayout);
		return panel;
	}

	private static Checkbox createCheckbox(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error("Null parameters in createCheckbox - field: {}, meta: {}, binder: {}", field != null ? field.getName() : "null", meta != null ? "present" : "null",
				binder != null ? "present" : "null");
			return null;
		}
		LOGGER.debug("Creating checkbox for field '{}' with displayName '{}'", field.getName(), meta.displayName());
		final Checkbox checkbox = new Checkbox();
		checkbox.setRequiredIndicatorVisible(meta.required());
		checkbox.setReadOnly(meta.readOnly());
		// Safe null checking for description
		if ((meta.description() != null) && !meta.description().trim().isEmpty()) {
			checkbox.setHelperText(meta.description());
			LOGGER.debug("Set helper text for checkbox '{}': {}", field.getName(), meta.description());
		}
		// Safe null checking and parsing for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {
			try {
				checkbox.setValue(Boolean.parseBoolean(meta.defaultValue()));
				LOGGER.debug("Set default value for checkbox '{}': {}", field.getName(), meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.warn("Invalid boolean default value '{}' for field '{}': {}", meta.defaultValue(), field.getName(), e.getMessage());
			}
		}
		setComponentWidth(checkbox, meta);
		try {
			binder.bind(checkbox, field.getName());
			LOGGER.debug("Successfully bound checkbox for field '{}'", field.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}", field.getName(), e.getMessage());
			return null;
		}
		return checkbox;
	}

	@SuppressWarnings("unchecked")
	private static <T extends CEntityDB> ComboBox<T> createComboBox(final Field field, final MetaData meta, final BeanValidationBinder<?> binder, final ComboBoxDataProvider dataProvider) {
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
		LOGGER.debug("Creating ComboBox for field '{}' of type '{}'", field.getName(), fieldType.getSimpleName());
		final ComboBox<T> comboBox = new ComboBox<>();
		comboBox.setRequiredIndicatorVisible(meta.required());
		comboBox.setReadOnly(meta.readOnly());
		// Safe null checking for description
		if ((meta.description() != null) && !meta.description().trim().isEmpty()) {
			comboBox.setHelperText(meta.description());
			LOGGER.debug("Set helper text for ComboBox '{}': {}", field.getName(), meta.description());
		}
		comboBox.setClassName("form-field-combobox");
		setComponentWidth(comboBox, meta);
		// Enhanced item label generator with null safety
		comboBox.setItemLabelGenerator(item -> {
			if (item == null) {
				return "N/A";
			}
			try {
				return item.toString();
			} catch (final Exception e) {
				LOGGER.warn("Error generating label for ComboBox item of type {}: {}", item.getClass().getSimpleName(), e.getMessage());
				return "Error: " + item.getClass().getSimpleName();
			}
		});
		// Data provider resolution with priority order: 1. Legacy ComboBoxDataProvider
		// (if provided) - for backward compatibility 2. Annotation-based resolution
		// using CDataProviderResolver 3. Empty list as fallback
		List<T> items = null;
		// Priority 1: Use legacy data provider if provided
		if (dataProvider != null) {
			LOGGER.debug("Using legacy ComboBoxDataProvider for field '{}' of type '{}'", field.getName(), fieldType.getSimpleName());
			try {
				items = dataProvider.getItems(fieldType);
				if (items != null) {
					LOGGER.debug("Legacy data provider returned {} items for field '{}'", items.size(), field.getName());
				}
				else {
					LOGGER.warn("Legacy data provider returned null for field '{}' of type '{}'", field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error("Error using legacy data provider for field '{}' of type '{}': {}", field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}
		// Priority 2: Use annotation-based resolution if legacy provider didn't work
		if ((items == null) && (dataProviderResolver != null)) {
			LOGGER.debug("Attempting annotation-based data resolution for field '{}' of type '{}'", field.getName(), fieldType.getSimpleName());
			try {
				items = dataProviderResolver.resolveData(fieldType, meta);
				if (items != null) {
					LOGGER.debug("Annotation-based resolver returned {} items for field '{}'", items.size(), field.getName());
				}
				else {
					LOGGER.warn("Annotation-based resolver returned null for field '{}' of type '{}'", field.getName(), fieldType.getSimpleName());
				}
			} catch (final Exception e) {
				LOGGER.error("Error using annotation-based data resolver for field '{}' of type '{}': {}", field.getName(), fieldType.getSimpleName(), e.getMessage(), e);
			}
		}
		// Priority 3: Fallback to empty list
		if (items == null) {
			LOGGER.warn("No data provider could supply items for field '{}' of type '{}' - using empty list", field.getName(), fieldType.getSimpleName());
			items = List.of();
		}
		// Set items on ComboBox with validation
		try {
			comboBox.setItems(items);
			LOGGER.debug("Successfully set {} items on ComboBox for field '{}'", items.size(), field.getName());
		} catch (final Exception e) {
			LOGGER.error("Error setting items on ComboBox for field '{}': {}", field.getName(), e.getMessage(), e);
			comboBox.setItems(List.of()); // Fallback to empty list
		}
		// Enhanced converter with better error handling for lazy loading and proxy
		// objects
		binder.forField(comboBox).withConverter(
			// Convert from ComboBox value to entity (forward conversion)
			comboBoxValue -> {
				if (comboBoxValue == null) {
					LOGGER.debug("ComboBox value is null for field '{}' - returning null", field.getName());
					return null;
				}
				LOGGER.debug("Converting ComboBox value to entity for field '{}': {}", field.getName(), comboBoxValue.getClass().getSimpleName());
				return comboBoxValue;
			},
			// Convert from entity to ComboBox value (reverse conversion) - handles lazy
			// loading
			entityValue -> {
				if (entityValue == null) {
					LOGGER.debug("Entity value is null for field '{}' - returning null", field.getName());
					return null;
				}
				LOGGER.debug("Converting entity to ComboBox value for field '{}': entity type {}", field.getName(), entityValue.getClass().getSimpleName());
				try {
					// Get the entity ID for comparison - handles proxy objects safely
					final Long entityId = entityValue.getId();
					if (entityId == null) {
						LOGGER.warn("Entity has null ID for field '{}' - cannot match with ComboBox items", field.getName());
						return null;
					}
					// Find matching item in ComboBox by comparing IDs
					final List<T> allItems = comboBox.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>()).collect(java.util.stream.Collectors.toList());
					final T matchingItem = allItems.stream().filter(item -> {
						if (item == null) {
							return false;
						}
						final Long itemId = item.getId();
						return (itemId != null) && itemId.equals(entityId);
					}).findFirst().orElse(null);
					if (matchingItem != null) {
						LOGGER.debug("Found matching ComboBox item for entity ID {} in field '{}'", entityId, field.getName());
					}
					else {
						LOGGER.warn("No matching ComboBox item found for entity ID {} in field '{}'", entityId, field.getName());
					}
					return matchingItem;
				} catch (final Exception e) {
					LOGGER.error("Error converting entity to ComboBox value for field '{}': {}", field.getName(), e.getMessage(), e);
					return null;
				}
			}).bind(field.getName());
		return comboBox;
	}

	private static Component createComponentForField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder, final ComboBoxDataProvider dataProvider) {
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
		LOGGER.debug("Creating component for field '{}' of type '{}' with displayName '{}'", field.getName(), fieldType.getSimpleName(), meta.displayName());
		// Handle different field types with detailed logging
		if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			LOGGER.debug("Creating checkbox component for boolean field: {}", field.getName());
			return createCheckbox(field, meta, binder);
		}
		else if (fieldType == String.class) {
			LOGGER.debug("Creating text field component for string field: {}", field.getName());
			return createTextField(field, meta, binder);
		}
		else if ((fieldType == Integer.class) || (fieldType == int.class) || (fieldType == Long.class) || (fieldType == long.class) || (fieldType == Double.class) || (fieldType == double.class)
			|| (fieldType == Float.class) || (fieldType == float.class)) {
			LOGGER.debug("Creating number field component for numeric field: {} of type {}", field.getName(), fieldType.getSimpleName());
			return createNumberField(field, meta, binder);
		}
		else if (fieldType == LocalDate.class) {
			LOGGER.debug("Creating date picker component for LocalDate field: {}", field.getName());
			return createDatePicker(field, meta, binder);
		}
		else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			LOGGER.debug("Creating date-time picker component for temporal field: {} of type {}", field.getName(), fieldType.getSimpleName());
			return createDateTimePicker(field, meta, binder);
		}
		else if (fieldType.isEnum()) {
			LOGGER.debug("Creating enum component for enum field: {} of type {}", field.getName(), fieldType.getSimpleName());
			return createEnumComponent(field, meta, binder);
		}
		/*
		 * else if (CEntityDB.class.isAssignableFrom(fieldType)) { if (dataProvider !=
		 * null) {
		 * LOGGER.debug("Creating combobox component for entity field: {} of type {}",
		 * field.getName(), fieldType.getSimpleName()); return createComboBox(field,
		 * meta, binder, dataProvider); } else { LOGGER.
		 * warn("Entity field '{}' of type {} requires dataProvider but none provided",
		 * field.getName(), fieldType.getSimpleName()); return null; } }
		 */
		else if (CEntityDB.class.isAssignableFrom(fieldType)) {
			// Always try to create ComboBox, annotation-based resolver will be used if
			// dataProvider is null
			return createComboBox(field, meta, binder, dataProvider);
		}
		else {
			LOGGER.warn("Unsupported field type '{}' for field '{}' - no component created", fieldType.getSimpleName(), field.getName());
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
		if (meta == null) {
			LOGGER.error("MetaData is null in createFieldLayout");
			return null;
		}
		if (component == null) {
			LOGGER.error("Component is null in createFieldLayout for displayName: {}", meta.displayName() != null ? meta.displayName() : "unknown");
			return null;
		}
		LOGGER.debug("Creating field layout for displayName: '{}'", meta.displayName());
		final HorizontalLayout horizontalLayout = new HorizontalLayout();
		horizontalLayout.setPadding(false);
		horizontalLayout.setSpacing(false);
		horizontalLayout.setMargin(false);
		horizontalLayout.setJustifyContentMode(JustifyContentMode.START);
		horizontalLayout.setAlignItems(FlexComponent.Alignment.BASELINE);
		// Safe handling of display name
		final String displayName = ((meta.displayName() != null) && !meta.displayName().trim().isEmpty()) ? meta.displayName() : "Field";
		final Div labelDiv = new Div(displayName);
		if (labelDiv != null) {
			labelDiv.setMinWidth(LabelMinWidth_210PX);
			if (meta.required()) {
				labelDiv.getStyle().set("font-weight", "bold");
				LOGGER.debug("Applied bold styling for required field: {}", displayName);
			}
			horizontalLayout.add(labelDiv);
		}
		else {
			LOGGER.warn("Failed to create label div for field: {}", displayName);
		}
		horizontalLayout.add(component);
		LOGGER.debug("Successfully created field layout for: {}", displayName);
		return horizontalLayout;
	}

	private static NumberField createNumberField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error("Null parameters in createNumberField - field: {}, meta: {}, binder: {}", field != null ? field.getName() : "null", meta != null ? "present" : "null",
				binder != null ? "present" : "null");
			return null;
		}
		LOGGER.debug("Creating number field for field '{}' with displayName '{}' and type '{}'", field.getName(), meta.displayName(), field.getType().getSimpleName());
		final NumberField numberField = new NumberField();
		numberField.setRequiredIndicatorVisible(meta.required());
		numberField.setReadOnly(meta.readOnly());
		// Safe null checking for description
		if ((meta.description() != null) && !meta.description().trim().isEmpty()) {
			numberField.setHelperText(meta.description());
			LOGGER.debug("Set helper text for number field '{}': {}", field.getName(), meta.description());
		}
		// Validate and set min/max values
		if (meta.min() != Double.MIN_VALUE) {
			if (Double.isFinite(meta.min())) {
				numberField.setMin(meta.min());
				LOGGER.debug("Set minimum value for number field '{}': {}", field.getName(), meta.min());
			}
			else {
				LOGGER.warn("Invalid minimum value {} for field '{}' - not a finite number", meta.min(), field.getName());
			}
		}
		if (meta.max() != Double.MAX_VALUE) {
			if (Double.isFinite(meta.max())) {
				numberField.setMax(meta.max());
				LOGGER.debug("Set maximum value for number field '{}': {}", field.getName(), meta.max());
			}
			else {
				LOGGER.warn("Invalid maximum value {} for field '{}' - not a finite number", meta.max(), field.getName());
			}
		}
		// Validate min/max relationship
		if ((meta.min() != Double.MIN_VALUE) && (meta.max() != Double.MAX_VALUE) && Double.isFinite(meta.min()) && Double.isFinite(meta.max()) && (meta.min() > meta.max())) {
			LOGGER.warn("Invalid range for field '{}' - minimum ({}) is greater than maximum ({})", field.getName(), meta.min(), meta.max());
		}
		// Safe null checking and parsing for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {
			try {
				final double defaultVal = Double.parseDouble(meta.defaultValue());
				if (Double.isFinite(defaultVal)) {
					numberField.setValue(defaultVal);
					LOGGER.debug("Set default value for number field '{}': {}", field.getName(), defaultVal);
				}
				else {
					LOGGER.warn("Default value '{}' for field '{}' is not a finite number", meta.defaultValue(), field.getName());
				}
			} catch (final NumberFormatException e) {
				LOGGER.warn("Invalid numeric default value '{}' for field '{}': {}", meta.defaultValue(), field.getName(), e.getMessage());
			}
		}
		numberField.setClassName("form-field-number");
		setComponentWidth(numberField, meta);
		try {
			binder.bind(numberField, field.getName());
			LOGGER.debug("Successfully bound number field for field '{}'", field.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind number field for field '{}': {}", field.getName(), e.getMessage());
			return null;
		}
		return numberField;
	}

	private static TextField createTextField(final Field field, final MetaData meta, final BeanValidationBinder<?> binder) {
		if ((field == null) || (meta == null) || (binder == null)) {
			LOGGER.error("Null parameters in createTextField - field: {}, meta: {}, binder: {}", field != null ? field.getName() : "null", meta != null ? "present" : "null",
				binder != null ? "present" : "null");
			return null;
		}
		LOGGER.debug("Creating text field for field '{}' with displayName '{}'", field.getName(), meta.displayName());
		final TextField textField = new TextField();
		textField.setRequiredIndicatorVisible(meta.required());
		textField.setReadOnly(meta.readOnly());
		// Safe null checking for description
		if ((meta.description() != null) && !meta.description().trim().isEmpty()) {
			textField.setHelperText(meta.description());
			LOGGER.debug("Set helper text for text field '{}': {}", field.getName(), meta.description());
		}
		// Safe null checking for default value
		if ((meta.defaultValue() != null) && !meta.defaultValue().trim().isEmpty()) {
			textField.setValue(meta.defaultValue());
			LOGGER.debug("Set default value for text field '{}': {}", field.getName(), meta.defaultValue());
		}
		// Validate and set max length
		if (meta.maxLength() > 0) {
			textField.setMaxLength(meta.maxLength());
			LOGGER.debug("Set max length for text field '{}': {}", field.getName(), meta.maxLength());
		}
		else if (meta.maxLength() < -1) {
			LOGGER.warn("Invalid maxLength value {} for field '{}' - should be > 0 or -1", meta.maxLength(), field.getName());
		}
		textField.setClassName("form-field-text");
		setComponentWidth(textField, meta);
		try {
			binder.bind(textField, field.getName());
			LOGGER.debug("Successfully bound text field for field '{}'", field.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text field for field '{}': {}", field.getName(), e.getMessage());
			return null;
		}
		return textField;
	}

	private static void setComponentWidth(final Component component, final MetaData meta) {
		if (component == null) {
			LOGGER.warn("Component is null in setComponentWidth - cannot set width");
			return;
		}
		if (meta == null) {
			LOGGER.warn("MetaData is null in setComponentWidth - using default width");
			return;
		}
		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize = (com.vaadin.flow.component.HasSize) component;
			if ((meta.width() != null) && !meta.width().trim().isEmpty()) {
				try {
					hasSize.setWidth(meta.width());
					LOGGER.debug("Set component width to: {}", meta.width());
				} catch (final Exception e) {
					LOGGER.warn("Failed to set component width '{}': {}", meta.width(), e.getMessage());
					// Fall back to full width
					hasSize.setWidthFull();
				}
			}
			else {
				hasSize.setWidthFull();
				LOGGER.debug("Set component width to full (no width specified)");
			}
		}
		else {
			LOGGER.debug("Component does not implement HasSize - cannot set width");
		}
	}

	private CEntityFormBuilder() {
		// Spring component - constructor managed by Spring
		LOGGER.debug("CEntityFormBuilder instance created by Spring");
	}

	/**
	 * Sets the application context and initializes the data provider resolver. This
	 * method is called automatically by Spring.
	 * @param context the Spring application context
	 */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		CEntityFormBuilder.applicationContext = context;
		try {
			CEntityFormBuilder.dataProviderResolver = context.getBean(CDataProviderResolver.class);
			LOGGER.info("CEntityFormBuilder initialized with Spring context and data provider resolver");
		} catch (final Exception e) {
			LOGGER.warn("Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}", e.getMessage());
			CEntityFormBuilder.dataProviderResolver = null;
		}
	}
}
