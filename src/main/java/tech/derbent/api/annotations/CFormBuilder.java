package tech.derbent.api.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CColorAwareComboBox;
import tech.derbent.api.components.CColorPickerComboBox;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.components.CPictureSelector;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.interfaces.IHasContentOwner;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.components.CHorizontalLayout;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.screens.domain.CDetailLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

@org.springframework.stereotype.Component
public final class CFormBuilder<EntityClass> implements ApplicationContextAware {

	public interface IComboBoxDataProvider {

		<T extends CEntityDB<T>> List<T> getItems(Class<T> entityType);
	}

	private static ApplicationContext applicationContext;
	private static CDataProviderResolver dataProviderResolver;
	protected static final String LabelMinWidth_210PX = "210px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CFormBuilder.class);

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayout buildEnhancedForm(final Class<?> entityClass) throws Exception {
		final CEnhancedBinder<EntityClass> enhancedBinder = CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, null);
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayout buildEnhancedForm(final Class<?> entityClass, final List<String> entityFields) throws Exception {
		final CEnhancedBinder<EntityClass> enhancedBinder = CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, entityFields);
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder) throws Exception {
		return buildForm(entityClass, binder, null, null, null, new CVerticalLayout(false, false, false));
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields) throws Exception {
		return buildForm(entityClass, binder, entityFields, null, null, new CVerticalLayout(false, false, false));
	}

	/** Builds a form with content owner support and specific entity fields.
	 * @param <EntityClass> the entity class type
	 * @param entityClass   the entity class
	 * @param binder        the enhanced binder
	 * @param entityFields  the list of field names to include
	 * @param contentOwner  the content owner (page) for context-aware data providers
	 * @return the form layout
	 * @throws Exception if form building fails */
	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields, final IContentOwner contentOwner) throws Exception {
		return buildForm(entityClass, binder, entityFields, null, null, new CVerticalLayout(false, false, false), contentOwner);
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			List<String> entityFields, final Map<String, Component> mapComponents, final Map<String, CHorizontalLayout> mapHorizontalLayouts,
			final CVerticalLayout formLayout) throws Exception {
		return buildForm(entityClass, binder, entityFields, mapComponents, mapHorizontalLayouts, formLayout, null);
	}

	public static <EntityClass> CVerticalLayout buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			List<String> entityFields, final Map<String, Component> mapComponents, final Map<String, CHorizontalLayout> mapHorizontalLayouts,
			final CVerticalLayout formLayout, final IContentOwner contentOwner) throws Exception {
		Check.notNull(entityClass, "Entity class cannot be null");
		// Set content owner in data provider resolver context
		// final FormLayout formLayout = new FormLayout();
		final List<Field> allFields = new ArrayList<>();
		getListOfAllFields(entityClass, allFields);
		final List<Field> sortedFields = getSortedFilteredFieldsList(allFields);
		LOGGER.info("Processing {} visible fields for form generation", sortedFields.size());
		// Create components with enhanced error handling and logging
		if (entityFields == null) {
			entityFields = sortedFields.stream().map(Field::getName).collect(Collectors.toList());
		}
		for (final String fieldName : entityFields) {
			final Field field = sortedFields.stream().filter(f -> f.getName().equals(fieldName)).findFirst().orElse(null);
			if (field == null) {
				LOGGER.warn("Field '{}' not found in entity class {}", fieldName, entityClass.getSimpleName());
			}
			Check.notNull(field, "Field '" + fieldName + "' not found in entity class " + entityClass.getSimpleName());
			final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
			processField(contentOwner, binder, formLayout, mapHorizontalLayouts, fieldInfo, mapComponents);
		}
		return formLayout;
	}

	/** Builds a form with content owner support for context-aware data providers.
	 * @param <EntityClass> the entity class type
	 * @param entityClass   the entity class
	 * @param binder        the enhanced binder
	 * @param contentOwner  the content owner (page) for context-aware data providers
	 * @return the form layout
	 * @throws Exception if form building fails */
	public static <EntityClass> CVerticalLayout buildFormWithOwner(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final IContentOwner contentOwner) throws Exception {
		return buildForm(entityClass, binder, null, contentOwner);
	}

	private static List<String> callStringDataMethod(final Object serviceBean, final String methodName, final String fieldName)
			throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		// Try to find and call the method
		try {
			final Method method = serviceBean.getClass().getMethod(methodName);
			Check.notNull(method, "Method '" + methodName + "' on service bean for field '" + fieldName + "'");
			final Object result = method.invoke(serviceBean);
			Check.notNull(result, "Result of method '" + methodName + "' on service bean for field '" + fieldName + "'");
			Check.isTrue(result instanceof List, "Method '" + methodName + "' on service bean for field '" + fieldName + "' did not return a List");
			final List<?> rawList = (List<?>) result;
			// Convert to List<String> if possible
			final List<String> stringList = rawList.stream().filter(item -> item instanceof String).map(item -> (String) item).toList();
			return stringList;
		} catch (final Exception e) {
			LOGGER.error("Failed to call method '{}' on service bean for field '{}': {}", methodName, fieldName, e.getMessage());
			throw e;
		}
	}

	// call stringdatamethod with one parameter
	private static List<String> callStringDataMethod(final Object serviceBean, final String methodName, final String fieldName,
			final Object parameter) throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
		try {
			// Try to find and call the method with one parameter
			final Method method = serviceBean.getClass().getMethod(methodName, Object.class);
			Check.notNull(method, "Method '" + methodName + "' on service bean for field '" + fieldName + "'");
			final Object result = method.invoke(serviceBean, parameter);
			Check.notNull(result, "Result of method '" + methodName + "' on service bean for field '" + fieldName + "'");
			Check.isTrue(result instanceof List, "Method '" + methodName + "' on service bean for field '" + fieldName + "' did not return a List");
			final List<?> rawList = (List<?>) result;
			// Convert to List<String> if possible
			final List<String> stringList = rawList.stream().filter(item -> item instanceof String).map(item -> (String) item).toList();
			return stringList;
		} catch (final Exception e) {
			LOGGER.error("Failed to call method '{}' on service bean for field '{}': {}", methodName, fieldName, e.getMessage());
			throw e;
		}
	}

	private static NumberField createBigDecimalField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for BigDecimal field creation");
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(0.01); // Set decimal step for BigDecimal fields
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			try {
				final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
				numberField.setValue(defaultVal);
			} catch (final NumberFormatException e) {
				LOGGER.error("Failed to parse default value '{}' as number for field '{}': {}", fieldInfo.getDefaultValue(),
						fieldInfo.getDisplayName(), e.getMessage());
				throw new IllegalArgumentException("Invalid default number value: " + fieldInfo.getDefaultValue(), e);
			}
		}
		try {
			// Use converter to handle BigDecimal conversion
			binder.forField(numberField).withConverter(value -> value != null ? BigDecimal.valueOf(value) : null,
					value -> value != null ? value.doubleValue() : null, "Invalid decimal value").bind(fieldInfo.getFieldName());
			LOGGER.debug("Successfully bound NumberField with BigDecimal converter for field '{}'", fieldInfo.getFieldName());
		} catch (final Exception e) {
			LOGGER.error("Failed to bind BigDecimal field for field '{}': {} - using fallback binding", fieldInfo.getFieldName(), e.getMessage());
			// Fallback to simple binding without converter
			try {
				safeBindComponent(binder, numberField, fieldInfo.getFieldName(), "NumberField(BigDecimal-fallback)");
			} catch (final Exception fallbackException) {
				LOGGER.error("Fallback binding also failed for BigDecimal field '{}': {}", fieldInfo.getFieldName(), fallbackException.getMessage());
			}
		}
		return numberField;
	}

	private static Checkbox createCheckbox(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for checkbox creation");
		final Checkbox checkbox = new Checkbox();
		// Set ID for better test automation
		CAuxillaries.setId(checkbox);
		// Safe null checking and parsing for default value
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			try {
				checkbox.setValue(Boolean.parseBoolean(fieldInfo.getDefaultValue()));
				// LOGGER.debug("Set default value for checkbox '{}': {}",
				// field.getName(), meta.defaultValue());
			} catch (final Exception e) {
				LOGGER.error("Invalid boolean default value '{}' for field '{}': {}", fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
						e.getMessage());
				throw new IllegalArgumentException("Invalid boolean default value: " + fieldInfo.getDefaultValue(), e);
			}
		}
		try {
			safeBindComponent(binder, checkbox, fieldInfo.getFieldName(), "Checkbox");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind checkbox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw new RuntimeException("Failed to bind checkbox for field: " + fieldInfo.getFieldName(), e);
		}
		return checkbox;
	}

	private static Component createColorPicker(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		CColorPickerComboBox colorPicker = new CColorPickerComboBox(fieldInfo);
		safeBindComponent(binder, colorPicker, fieldInfo.getFieldName(), "ColorPicker");
		return colorPicker;
	}

	public static <T extends CEntityDB<T>> ComboBox<T> createComboBox(IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
		LOGGER.debug("Creating CColorAwareComboBox for field: {}", fieldInfo.getFieldName());
		final ComboBox<T> comboBox = new CColorAwareComboBox<>(fieldInfo);
		comboBox.setItemLabelGenerator(item -> CColorUtils.getDisplayTextFromEntity(item));
		// Data provider resolution using CDataProviderResolver
		List<T> items = null;
		Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
		items = dataProviderResolver.resolveData(contentOwner, fieldInfo);
		Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
		}
		comboBox.setItems(items);
		if (!items.isEmpty()) {
			if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
				// For entity types, try to find by name or toString match
				final T defaultItem = items.stream().filter(item -> {
					final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
					return fieldInfo.getDefaultValue().equals(itemDisplay);
				}).findFirst().orElse(null);
				if (defaultItem != null) {
					comboBox.setValue(defaultItem);
				}
			} else if (fieldInfo.isAutoSelectFirst()) {
				comboBox.setValue(items.get(0));
			}
		}
		safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "ComboBox");
		return comboBox;
	}

	@SuppressWarnings ("unchecked")
	private static <T> MultiSelectComboBox<T> createComboBoxMultiSelect(IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
		LOGGER.debug("Creating MultiSelectComboBox for field: {}", fieldInfo.getFieldName());
		final MultiSelectComboBox<T> comboBox = new MultiSelectComboBox<T>(fieldInfo.getDisplayName());
		comboBox.setItemLabelGenerator(item -> {
			if (item instanceof CEntityNamed<?>) {
				return ((CEntityNamed<?>) item).getName();
			}
			if (item instanceof CEntityDB<?>) {
				return CColorUtils.getDisplayTextFromEntity(item);
			}
			if (item instanceof String) {
				return (String) item;
			}
			return "Unknown Item: " + String.valueOf(item);
		});
		// --- Data provider ---
		Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
		// DİKKAT: Diziye çevirip Set.of(...) kullanmıyoruz — bu, Set<CEntityDB[]> üretip tür çıkarımını bozuyordu.
		final List<?> rawList = dataProviderResolver.resolveData(contentOwner, fieldInfo);
		Check.notNull(rawList, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
		// Tip güvenli toplama: LinkedHashSet ile sıralı ve benzersiz
		final LinkedHashSet<T> items = rawList.stream().map(e -> (T) e) // runtime cast; provider sözleşmesine güveniyoruz
				.collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.clear(); // Set.of() vermek yerine clear()
		}
		comboBox.setItems(items);
		// (İsteğe bağlı) Varsayılan değer atama burada yapılabilir.
		safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "ComboBox(MultiSelect)");
		return comboBox;
	}

	private static Component createComponentForField(IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder)
			throws Exception {
		Component component = null;
		Check.notNull(fieldInfo, "Field");
		final Class<?> fieldType = fieldInfo.getFieldTypeClass();
		Check.notNull(fieldType, "Field type for field " + fieldInfo.getDisplayName());
		// Check for custom component creation method first (highest priority)
		if (fieldInfo.getCreateComponentMethod() != null && !fieldInfo.getCreateComponentMethod().trim().isEmpty()) {
			component = createCustomComponent(contentOwner, fieldInfo, binder);
			Check.notNull(component, "Custom component for field " + fieldInfo.getFieldName());
			return component;
		}
		if (fieldInfo.getFieldName().equals("approvals") || fieldInfo.getFieldName().equals("status")) {
			LOGGER.info("Skipping field 'approvals' as it is handled separately");
		}
		// Check if field should be rendered as ComboBox based on metadata
		final boolean hasDataProvider = (fieldInfo.getDataProviderBean() != null) && !fieldInfo.getDataProviderBean().trim().isEmpty();
		if (hasDataProvider && (fieldType == String.class)) {
			// gets strings from a method in a spring bean
			component = createStringComboBox(contentOwner, fieldInfo, binder);
		} else if (hasDataProvider && fieldInfo.getJavaType().equals("Set")) {
			component = createComboBoxMultiSelect(contentOwner, fieldInfo, binder);
		} else if (hasDataProvider || CEntityDB.class.isAssignableFrom(fieldType)) {
			// it has a dataprovider or entity type
			component = createComboBox(contentOwner, fieldInfo, binder);
		} else if ((fieldType == Boolean.class) || (fieldType == boolean.class)) {
			component = createCheckbox(fieldInfo, binder);
		} else if ((fieldType == String.class)) {
			if (fieldInfo.isColorField()) {
				component = createColorPicker(fieldInfo, binder);
			} else if (fieldInfo.isUseIcon()) {
				component = createIconComboBox(fieldInfo, binder);
			} else if (fieldInfo.getMaxLength() >= CEntityConstants.MAX_LENGTH_DESCRIPTION) {
				component = createTextArea(fieldInfo, binder);
			} else if (fieldInfo.isPasswordField()) {
				component = createTextPasswordField(fieldInfo);
			} else {
				component = createTextField(fieldInfo, binder);
			}
		} else if ((fieldType == Integer.class) || (fieldType == int.class) || (fieldType == Long.class) || (fieldType == long.class)) {
			// Integer types
			component = createIntegerField(fieldInfo, binder);
		} else if (fieldType == BigDecimal.class) {
			component = createBigDecimalField(fieldInfo, binder);
		} else if ((fieldType == Double.class) || (fieldType == double.class) || (fieldType == Float.class) || (fieldType == float.class)) {
			// Floating-point types
			component = createFloatingPointField(fieldInfo, binder);
		} else if (fieldType == LocalDate.class) {
			component = createDatePicker(fieldInfo, binder);
		} else if ((fieldType == LocalDateTime.class) || (fieldType == Instant.class)) {
			component = createDateTimePicker(fieldInfo, binder);
		} else if (fieldType.isEnum()) {
			component = createEnumComponent(fieldInfo, binder);
		} else if ((fieldType == byte[].class) && fieldInfo.isImageData()) {
			component = createPictureSelector(fieldInfo, binder);
		} else {
			Check.isTrue(false, "Unsupported field type: " + fieldType.getSimpleName() + " for field: " + fieldInfo.getDisplayName());
		}
		Check.notNull(component, "Component for field " + fieldInfo.getFieldName() + " of type " + fieldType.getSimpleName());
		setRequiredIndicatorVisible(fieldInfo, component);
		// dont use helper text for Checkbox components setHelperText(meta, component);
		setComponentWidth(component, fieldInfo.getWidth());
		// setclass name for styling in format of form-field{ComponentType}
		component.setClassName("form-field-" + component.getClass().getSimpleName());
		// Create field
		return component;
	}

	private static DatePicker createDatePicker(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final DatePicker datePicker = new DatePicker();
		CAuxillaries.setId(datePicker);
		safeBindComponent(binder, datePicker, fieldInfo.getFieldName(), "DatePicker");
		return datePicker;
	}

	private static DateTimePicker createDateTimePicker(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final DateTimePicker dateTimePicker = new DateTimePicker();
		CAuxillaries.setId(dateTimePicker);
		safeBindComponent(binder, dateTimePicker, fieldInfo.getFieldName(), "DateTimePicker");
		return dateTimePicker;
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CEnhancedBinder<EntityClass> createEnhancedBinder(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class for enhanced binder");
		return CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
	}

	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	private static Component createEnumComponent(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final Class<? extends Enum> enumType = (Class<? extends Enum>) fieldInfo.getFieldTypeClass();
		final Enum[] enumConstants = enumType.getEnumConstants();
		if (fieldInfo.isUseRadioButtons()) {
			final RadioButtonGroup<Enum> radioGroup = new RadioButtonGroup<>();
			radioGroup.setItems(enumConstants);
			radioGroup.setItemLabelGenerator(Enum::name);
			safeBindComponent(binder, radioGroup, fieldInfo.getFieldName(), "RadioButtonGroup");
			return radioGroup;
		} else {
			final ComboBox<Enum> comboBox = new ComboBox<>();
			// Set ID for better test automation
			CAuxillaries.setId(comboBox);
			// Following coding guidelines: All selective ComboBoxes must be selection
			// only (user must not be able to type arbitrary text)
			comboBox.setAllowCustomValue(false);
			comboBox.setItems(enumConstants);
			comboBox.setItemLabelGenerator(Enum::name);
			safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "ComboBox(Enum)");
			return comboBox;
		}
	}

	private static CHorizontalLayout createFieldLayout(final EntityFieldInfo fieldInfo, final Component component) {
		Check.notNull(fieldInfo, "AMetaData for field layout");
		Check.notNull(component, "Component for field layout" + fieldInfo.getFieldName());
		final CHorizontalLayout horizontalLayout = CHorizontalLayout.forForm();
		final CDiv labelDiv = new CDiv(fieldInfo.getDisplayName());
		labelDiv.setClassName("form-field-label");
		if (fieldInfo.isRequired()) {
			labelDiv.getStyle().set("font-weight", "bold");
		}
		horizontalLayout.add(labelDiv, component);
		return horizontalLayout;
	}

	private static NumberField createFloatingPointField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for floating point fields
		numberField.setStep(0.01);
		// Set default value if specified
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		safeBindComponent(binder, numberField, fieldInfo.getFieldName(), "NumberField");
		return numberField;
	}

	private static ComboBox<String> createIconComboBox(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "Field for Icon ComboBox creation");
		final ComboBox<String> comboBox = new ComboBox<>();
		// Configure basic properties from metadata
		comboBox.setLabel(fieldInfo.getDisplayName());
		comboBox.setPlaceholder(fieldInfo.getPlaceholder().isEmpty() ? "Select an icon" : fieldInfo.getPlaceholder());
		comboBox.setAllowCustomValue(fieldInfo.isAllowCustomValue());
		comboBox.setReadOnly(fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly());
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			comboBox.setWidth(fieldInfo.getWidth());
		}
		// Get list of all Vaadin icons
		final List<String> iconNames = getVaadinIconNames();
		comboBox.setItems(iconNames);
		// Set up custom renderer to show icon with name
		comboBox.setRenderer(new ComponentRenderer<>(iconName -> {
			if (iconName == null || iconName.isEmpty()) {
				return new Span("No icon selected");
			}
			HorizontalLayout layout = new HorizontalLayout();
			layout.setAlignItems(com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment.CENTER);
			layout.setSpacing(true);
			try {
				// Parse the icon name (remove "vaadin:" prefix if present)
				String cleanIconName = iconName.startsWith("vaadin:") ? iconName.substring(7) : iconName;
				VaadinIcon vaadinIcon = VaadinIcon.valueOf(cleanIconName.toUpperCase().replace("-", "_"));
				Icon icon = vaadinIcon.create();
				icon.setSize("16px");
				layout.add(icon);
			} catch (Exception e) {
				// If icon cannot be created, show a placeholder
				Span placeholder = new Span("?");
				placeholder.getStyle().set("width", "16px").set("text-align", "center");
				layout.add(placeholder);
			}
			Span label = new Span(iconName);
			layout.add(label);
			return layout;
		}));
		// Set item label generator for text representation
		comboBox.setItemLabelGenerator(iconName -> iconName);
		// Handle default value
		final boolean hasDefaultValue = (fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty();
		if (hasDefaultValue) {
			String defaultIcon = fieldInfo.getDefaultValue();
			if (iconNames.contains(defaultIcon)) {
				comboBox.setValue(defaultIcon);
				LOGGER.debug("Set Icon ComboBox default value for field '{}': '{}'", fieldInfo.getFieldName(), defaultIcon);
			} else {
				LOGGER.warn("Default icon '{}' not found in available icons for field '{}'", defaultIcon, fieldInfo.getFieldName());
			}
		} else if (fieldInfo.isAutoSelectFirst() && !iconNames.isEmpty()) {
			// Auto-select first item if configured
			comboBox.setValue(iconNames.get(0));
			LOGGER.debug("Auto-selected first icon for field '{}': '{}'", fieldInfo.getFieldName(), iconNames.get(0));
		}
		// Bind to field
		safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "Icon ComboBox");
		return comboBox;
	}

	private static NumberField createIntegerField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(1);
		// Set default value if specified
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		// Handle different integer types with proper conversion
		final Class<?> fieldType = fieldInfo.getFieldTypeClass();
		final String propertyName = fieldInfo.getFieldName();
		if ((fieldType == Integer.class) || (fieldType == int.class)) {
			binder.forField(numberField).withConverter(value -> value != null ? value.intValue() : null,
					value -> value != null ? value.doubleValue() : null, "Invalid integer value").bind(propertyName);
			LOGGER.debug("Successfully bound NumberField with Integer converter for field '{}'", fieldInfo.getFieldName());
		} else if ((fieldType == Long.class) || (fieldType == long.class)) {
			binder.forField(numberField).withConverter(value -> value != null ? value.longValue() : null,
					value -> value != null ? value.doubleValue() : null, "Invalid long value").bind(propertyName);
			LOGGER.debug("Successfully bound NumberField with Long converter for field '{}'", fieldInfo.getFieldName());
		} else {
			// Fallback for other number types (Double, etc.)
			binder.bind(numberField, propertyName);
		}
		return numberField;
	}

	private static Component createPictureSelector(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		LOGGER.debug("Creating CPictureSelector for field: {}", fieldInfo.getFieldName());
		CPictureSelector pictureSelector = new CPictureSelector(fieldInfo);
		safeBindComponent(binder, pictureSelector, fieldInfo.getFieldName(), "PictureSelector");
		LOGGER.debug("Successfully created CPictureSelector for field: {}", fieldInfo.getFieldName());
		return pictureSelector;
	}

	private static ComboBox<String> createStringComboBox(IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder)
			throws Exception {
		Check.notNull(fieldInfo, "Field for String ComboBox creation");
		final ComboBox<String> comboBox = new ComboBox<>();
		// Configure basic properties from metadata
		comboBox.setLabel(fieldInfo.getDisplayName());
		comboBox.setPlaceholder(fieldInfo.getPlaceholder());
		comboBox.setAllowCustomValue(fieldInfo.isAllowCustomValue());
		comboBox.setReadOnly(fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly());
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			comboBox.setWidth(fieldInfo.getWidth());
		}
		// Resolve String data using data provider
		final List<String> items = resolveStringData(fieldInfo);
		comboBox.setItems(items);
		// Handle clearOnEmptyData configuration
		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
		}
		// Handle default value
		final boolean hasDefaultValue = (fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty();
		if (hasDefaultValue) {
			// For String ComboBox, try to match default value exactly
			if (items.contains(fieldInfo.getDefaultValue())) {
				comboBox.setValue(fieldInfo.getDefaultValue());
				LOGGER.debug("Set String ComboBox default value for field '{}': '{}'", fieldInfo.getFieldName(), fieldInfo.getDefaultValue());
			} else {
				LOGGER.warn("Default value '{}' not found in items for String field '{}'", fieldInfo.getDefaultValue(), fieldInfo.getFieldName());
			}
		} else if (fieldInfo.isAutoSelectFirst() && !items.isEmpty()) {
			// Auto-select first item if configured
			comboBox.setValue(items.get(0));
			LOGGER.debug("Auto-selected first string item for field '{}': '{}'", fieldInfo.getFieldName(), items.get(0));
		}
		// Bind to field
		safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "String ComboBox");
		return comboBox;
	}

	private static TextArea createTextArea(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final TextArea item = new TextArea();
		if (fieldInfo.getMaxLength() > 0) {
			item.setMaxLength(fieldInfo.getMaxLength());
		}
		item.setWidthFull();
		item.setMinHeight("100px");
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			try {
				item.setValue(fieldInfo.getDefaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}", fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
						e.getMessage());
			}
		}
		try {
			safeBindComponent(binder, item, fieldInfo.getFieldName(), "TextArea");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text area for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw new RuntimeException("Failed to bind text area for field: " + fieldInfo.getFieldName(), e);
		}
		return item;
	}

	private static TextField createTextField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final TextField item = new TextField();
		// Set ID for better test automation
		CAuxillaries.setId(item);
		item.setClassName("plain-look-textfield");
		if (fieldInfo.getMaxLength() > 0) {
			item.setMaxLength(fieldInfo.getMaxLength());
		}
		item.setWidthFull();
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			try {
				item.setValue(fieldInfo.getDefaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text area '{}': {}", fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
						e.getMessage());
			}
		}
		try {
			safeBindComponent(binder, item, fieldInfo.getFieldName(), "TextField");
		} catch (final Exception e) {
			LOGGER.error("Failed to bind text field for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw new RuntimeException("Failed to bind text field for field: " + fieldInfo.getFieldName(), e);
		}
		return item;
	}

	private static PasswordField createTextPasswordField(final EntityFieldInfo fieldInfo) {
		final PasswordField item = new PasswordField();
		// Set ID for better test automation
		CAuxillaries.setId(item);
		item.setClassName("plain-look-textfield");
		if (fieldInfo.getMaxLength() > 0) {
			item.setMaxLength(fieldInfo.getMaxLength());
		}
		item.setWidthFull();
		if ((fieldInfo.getDefaultValue() != null) && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			item.setValue(fieldInfo.getDefaultValue());
		}
		item.setRevealButtonVisible(fieldInfo.isPasswordRevealButton());
		return item;
	}

	private static void getListOfAllFields(final Class<?> entityClass, final List<Field> allFields) {
		Class<?> current = entityClass;
		while ((current != null) && (current != Object.class)) {
			final Field[] declaredFields = current.getDeclaredFields();
			allFields.addAll(Arrays.asList(declaredFields));
			current = current.getSuperclass();
		}
	}

	private static List<Field> getSortedFilteredFieldsList(final List<Field> allFields) {
		return allFields.stream().filter(field -> {
			Check.notNull(field, "Field in sorted filtered fields list");
			return !Modifier.isStatic(field.getModifiers());
		}).filter(field -> {
			final AMetaData metaData = field.getAnnotation(AMetaData.class);
			if (metaData == null) {
				return false;
			}
			return true;
		}).filter(field -> {
			final AMetaData metaData = field.getAnnotation(AMetaData.class);
			if (metaData.hidden()) {
				return false;
			}
			return true;
		}).sorted(Comparator.comparingInt(field -> {
			final AMetaData metaData = field.getAnnotation(AMetaData.class);
			return metaData != null ? metaData.order() : Integer.MAX_VALUE;
		})).collect(Collectors.toList());
	}

	private static List<String> getVaadinIconNames() {
		List<String> iconNames = new ArrayList<>();
		// Add all VaadinIcon enum values as "vaadin:iconname" format
		for (VaadinIcon icon : VaadinIcon.values()) {
			String iconName = "vaadin:" + icon.name().toLowerCase().replace("_", "-");
			iconNames.add(iconName);
		}
		// Sort the list for better user experience
		iconNames.sort(String::compareTo);
		return iconNames;
	}

	public static <EntityClass> Component processField(final IContentOwner contentOwner, final CEnhancedBinder<EntityClass> binder,
			final VerticalLayout formLayout, final Map<String, CHorizontalLayout> mapHorizontalLayouts, final EntityFieldInfo fieldInfo,
			final Map<String, Component> mapComponents) throws Exception {
		Check.notNull(fieldInfo, "field");
		final Component component = createComponentForField(contentOwner, fieldInfo, binder);
		final CHorizontalLayout horizontalLayout = createFieldLayout(fieldInfo, component);
		formLayout.add(horizontalLayout);
		if (mapHorizontalLayouts != null) {
			mapHorizontalLayouts.put(fieldInfo.getFieldName(), horizontalLayout);
		}
		if ((component != null) && (mapComponents != null)) {
			mapComponents.put(fieldInfo.getFieldName(), component);
		}
		return component;
	}

	/** Recursively searches for ComboBox components and resets them to their first item. */
	@SuppressWarnings ("unchecked")
	private static void resetComboBoxesRecursively(final com.vaadin.flow.component.HasComponents container) {
		container.getElement().getChildren().forEach(element -> {
			// Get the component from the element
			if (element.getComponent().isPresent()) {
				final com.vaadin.flow.component.Component component = element.getComponent().get();
				if (component instanceof ComboBox) {
					final ComboBox<Object> comboBox = (ComboBox<Object>) component;
					try {
						// Get the first item from the ComboBox data provider
						final java.util.Optional<Object> firstItem =
								comboBox.getDataProvider().fetch(new com.vaadin.flow.data.provider.Query<>()).findFirst();
						if (firstItem.isPresent()) {
							comboBox.setValue(firstItem.get());
							LOGGER.debug("Reset ComboBox to first item: {}", firstItem.get());
						} else {
							LOGGER.debug("ComboBox has no items to reset to");
						}
					} catch (final Exception e) {
						LOGGER.error("Error resetting ComboBox to first item: {}", e.getMessage());
					}
				} else if (component instanceof com.vaadin.flow.component.HasComponents) {
					// Recursively check child components
					resetComboBoxesRecursively((com.vaadin.flow.component.HasComponents) component);
				}
			}
		});
	}

	public static void resetComboBoxesToFirstItem(final com.vaadin.flow.component.HasComponents container) {
		Check.notNull(container, "Container for resetting ComboBoxes to first item");
		resetComboBoxesRecursively(container);
	}

	private static List<String> resolveStringData(final EntityFieldInfo fieldInfo) throws Exception {
		Check.notNull(fieldInfo, "EntityFieldInfo for String data resolution");
		// Try to resolve data provider bean
		final String sourceClassName = fieldInfo.getDataProviderBean();
		final String methodName = fieldInfo.getDataProviderMethod();
		if (sourceClassName.equals("none")) {
			return List.of(); // No data provider configured, return empty list
		}
		if (applicationContext.containsBean(sourceClassName)) {
			final Object serviceBean = applicationContext.getBean(sourceClassName);
			// IT is a BEAN!
			if ((fieldInfo.getDataProviderParamMethod() != null) && (fieldInfo.getDataProviderParamMethod().trim().length() > 0)) {
				Object param = null;
				try {
					// call dataprovider param method, returning Object as result
					final String methodstr = fieldInfo.getDataProviderParamMethod();
					final Method method = serviceBean.getClass().getMethod(methodstr);
					Check.notNull(method, "Method '" + methodName + "' on service bean for field '" + fieldInfo.getFieldName() + "'");
					param = method.invoke(serviceBean);
				} catch (final NoSuchMethodException e) {
					LOGGER.error("Data provider method '{}' not found on service bean for field '{}': {}", fieldInfo.getDataProviderParamMethod(),
							fieldInfo.getFieldName(), e.getMessage());
					throw e;
				}
				return callStringDataMethod(serviceBean, methodName, fieldInfo.getFieldName(), param);
			} else {
				return callStringDataMethod(serviceBean, methodName, fieldInfo.getFieldName());
			}
		} else {
			return CAuxillaries.invokeStaticMethodOfList(sourceClassName, methodName);
		}
	}

	/** Safely binds a component to a field, ensuring no incomplete bindings are left. This method prevents the "All bindings created with forField
	 * must be completed" error. */
	private static void safeBindComponent(final CEnhancedBinder<?> binder, final HasValueAndElement<?, ?> component, final String fieldName,
			final String componentType) {
		if (binder == null) {
			LOGGER.warn("Binder is null, wont bind component of type '{}' for field '{}'", componentType, fieldName);
			return;
		}
		Check.notNull(component, "Component for safe binding");
		Check.notNull(fieldName, "Field name for safe binding");
		try {
			binder.bind(component, fieldName);
		} catch (final Exception e) {
			LOGGER.error("Failed to bind {} for field '{}': {} - this may cause incomplete bindings", componentType, fieldName, e.getMessage(), e);
			// Don't throw - just log the error to prevent form generation failure
			// The incomplete binding will be handled by CEnhancedBinder's validateBindingsComplete() method
		}
	}

	private static void setComponentWidth(final Component component, final String width) {
		if ((component == null) || (width == null)) {
			return;
		}
		if (component instanceof com.vaadin.flow.component.HasSize) {
			final com.vaadin.flow.component.HasSize hasSize = (com.vaadin.flow.component.HasSize) component;
			if ((width != null) && !width.trim().isEmpty()) {
				try {
					hasSize.setWidth(width);
				} catch (final Exception e) {
					LOGGER.warn("Failed to set component width '{}': {}", width, e.getMessage());
					// Fall back to full width
					hasSize.setWidthFull();
				}
			} else {
				hasSize.setWidthFull();
			}
		}
	}

	private static void setRequiredIndicatorVisible(final EntityFieldInfo fieldInfo, final Component field) {
		((HasValueAndElement<?, ?>) field).setReadOnly(fieldInfo.isReadOnly());
		((HasValueAndElement<?, ?>) field).setRequiredIndicatorVisible(fieldInfo.isRequired());
	}

	private CEnhancedBinder<?> binder;
	final Map<String, Component> componentMap;
	final CVerticalLayout formLayout;
	final Map<String, CHorizontalLayout> horizontalLayoutMap;

	public CFormBuilder() {
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
		formLayout = new CVerticalLayout(false, false, false);
	}

	public CFormBuilder(IContentOwner contentOwner, final Class<?> entityClass) throws Exception {
		this(contentOwner, entityClass, createEnhancedBinder(entityClass), List.of());
	}

	public CFormBuilder(IContentOwner contentOwner, final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder) throws Exception {
		this(contentOwner, entityClass, binder, List.of());
	}

	/** Constructor with content owner support for context-aware data providers.
	 * @param entityClass  the entity class
	 * @param binder       the enhanced binder
	 * @param entityFields the list of field names to include
	 * @param contentOwner the content owner for context-aware data providers
	 * @throws Exception if form building fails */
	public CFormBuilder(IContentOwner contentOwner, final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields) throws Exception {
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
		formLayout = new CVerticalLayout(false, false, false);
		this.binder = binder;
		CFormBuilder.buildForm(entityClass, binder, entityFields, componentMap, horizontalLayoutMap, formLayout, contentOwner);
	}

	/** Populates the form with entity data using the internal binder.
	 * @param entity the entity to populate the form with */
	@SuppressWarnings ("unchecked")
	public void populateForm(Object entity) {
		if (binder != null) {
			LOGGER.debug("Populating form with entity: {}", entity);
			((CEnhancedBinder<Object>) binder).setBean(entity);
		}
	}

	/** Clears the form by setting the binder bean to null. */
	@SuppressWarnings ("unchecked")
	public void populateForm() {
		if (binder != null) {
			LOGGER.debug("Clearing form - setting bean to null");
			((CEnhancedBinder<Object>) binder).setBean(null);
		}
	}

	/** Gets the internal binder for this form builder.
	 * @return the enhanced binder */
	public CEnhancedBinder<?> getBinder() { return binder; }

	public Component addFieldLine(final EntityFieldInfo fieldInfo) throws Exception {
		return CFormBuilder.processField(null, binder, formLayout, horizontalLayoutMap, fieldInfo, componentMap);
	}

	public Component addFieldLine(IContentOwner contentOwner, final String screenClassType, final CDetailLines line, final VerticalLayout layout,
			Map<String, Component> componentMap2, Map<String, CHorizontalLayout> horizontalLayoutMap2) throws Exception {
		final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(screenClassType, line);
		return CFormBuilder.processField(contentOwner, binder, layout, horizontalLayoutMap, fieldInfo, componentMap);
	}

	public CVerticalLayout build(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder, final List<String> entityFields)
			throws Exception {
		return CFormBuilder.buildForm(entityClass, binder, entityFields, componentMap, horizontalLayoutMap, formLayout);
	}

	public Component getComponent(final String fieldName) {
		Check.notNull(fieldName, "Field name for component retrieval");
		final Component component = componentMap.get(fieldName);
		Check.notNull(component, "Component for field " + fieldName + " not found in form builder map");
		return component;
	}

	public CVerticalLayout getFormLayout() { return formLayout; }

	public CHorizontalLayout getHorizontalLayout(final String fieldName) {
		Check.notNull(fieldName, "Field name for horizontal layout retrieval");
		final CHorizontalLayout layout = horizontalLayoutMap.get(fieldName);
		Check.notNull(layout, "HorizontalLayout for field " + fieldName + " not found in form builder map");
		return layout;
	}

	/** Creates a custom component by invoking the method specified in createComponentMethod. Supports comma-separated method names and will try each
	 * method until one succeeds.
	 * @param contentOwner the content owner (page) for context
	 * @param fieldInfo    field information containing the method name(s)
	 * @param binder       the enhanced binder for form binding
	 * @return the custom component or null if creation fails */
	private static Component createCustomComponent(IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		try {
			Check.notNull(fieldInfo, "FieldInfo for custom component creation");
			Check.notNull(fieldInfo.getCreateComponentMethod(), "CreateComponentMethod for custom component creation");
			Check.notNull(fieldInfo.getDataProviderBean(), "DataProviderBean for custom component creation");
			// Check.notNull(contentOwner, "ContentOwner for custom component creation");
			// use first method only
			final String methodName = fieldInfo.getCreateComponentMethod().split(",")[0].trim();
			final Component component = invokeCustomComponentMethod(contentOwner, methodName, fieldInfo, binder);
			Check.notNull(component, "Custom component created by method " + methodName + " for field " + fieldInfo.getFieldName());
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating custom component for field {}: {}", fieldInfo.getFieldName(), e.getMessage(), e);
			return null;
		}
	}

	/** Invokes a single custom component method.
	 * @param contentOwner the content owner (page) for context
	 * @param methodName   the method name to invoke
	 * @param fieldInfo    field information
	 * @param binder       the enhanced binder
	 * @return the custom component or null if method fails
	 * @throws Exception */
	private static Component invokeCustomComponentMethod(IContentOwner contentOwner, final String methodName, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		try {
			// contentowner can be null
			String sourceClassName = fieldInfo.getDataProviderBean();
			Check.isTrue(applicationContext.containsBean(sourceClassName),
					"Data provider bean '" + sourceClassName + "' not found for field '" + fieldInfo.getFieldName() + "'");
			final Object serviceBean = applicationContext.getBean(sourceClassName);
			Check.notNull(serviceBean, "Service bean '" + sourceClassName + "' for field '" + fieldInfo.getFieldName() + "'");
			final Method method = serviceBean.getClass().getMethod(methodName);
			Check.notNull(method, "Method '" + methodName + "' on service bean for field '" + fieldInfo.getFieldName() + "'");
			Object result = method.invoke(serviceBean);
			Check.notNull(result, "Result of custom component method " + methodName + " for field " + fieldInfo.getFieldName());
			Check.isTrue(result instanceof Component, "Result of method " + methodName + " is not a Component");
			final Component component = (Component) result;
			if (component instanceof IHasContentOwner) {
				((IHasContentOwner) component).setContentOwner(contentOwner);
			}
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to invoke custom component method {}: {}", methodName, e.getMessage());
			throw e;
		}
	}

	/** Sets the application context and initializes the data provider resolver. This method is called automatically by Spring.
	 * @param context the Spring application context */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		// Store the application context for String data provider resolution
		CFormBuilder.applicationContext = context;
		try {
			CFormBuilder.dataProviderResolver = context.getBean(CDataProviderResolver.class);
		} catch (final Exception e) {
			LOGGER.warn("Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}", e.getMessage());
			CFormBuilder.dataProviderResolver = null;
		}
	}
}
