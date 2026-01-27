package tech.derbent.api.annotations;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import tech.derbent.api.components.CBinderFactory;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.service.CEntityFieldService;
import tech.derbent.api.screens.service.CEntityFieldService.EntityFieldInfo;
import tech.derbent.api.ui.component.basic.CColorPickerComboBox;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.component.basic.CHorizontalLayout;
import tech.derbent.api.ui.component.basic.CNavigableComboBox;
import tech.derbent.api.ui.component.basic.CVerticalLayoutTop;
import tech.derbent.api.ui.component.enhanced.CComponentFieldSelection;
import tech.derbent.api.ui.component.enhanced.CComponentListSelection;
import tech.derbent.api.ui.component.enhanced.CPictureSelector;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

@org.springframework.stereotype.Component
public final class CFormBuilder<EntityClass> implements ApplicationContextAware {

	public interface IComboBoxDataProvider {

		<T extends CEntityDB<T>> List<T> getItems(Class<T> entityType);
	}

	private static CDataProviderResolver dataProviderResolver;
	protected static final String LabelMinWidth_210PX = "210px";
	private static final Logger LOGGER = LoggerFactory.getLogger(CFormBuilder.class);

	private static void assignDeterministicComponentId(final Component component, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		if (component == null || fieldInfo == null) {
			return;
		}
		String entityPart = "entity";
		if (binder != null) {
			try {
				final Class<?> beanType = binder.getBeanType();
				if (beanType != null) {
					entityPart = beanType.getSimpleName();
				}
			} catch (final Exception e) {
				LOGGER.debug("Could not resolve binder bean type for deterministic ID: {}", e.getMessage());
			}
		}
		final String rawId = String.format("field-%s-%s", entityPart, fieldInfo.getFieldName());
		final String normalized = rawId.replaceAll("([a-z])([A-Z])", "$1-$2").replaceAll("[^a-zA-Z0-9-]", "-").replaceAll("-{2,}", "-")
				.replaceAll("(^-|-$)", "").toLowerCase();
		if (!normalized.isEmpty()) {
			component.setId(normalized);
		}
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayoutTop buildEnhancedForm(final Class<?> entityClass) throws Exception {
		final CEnhancedBinder<EntityClass> enhancedBinder = CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, null);
	}

	@SuppressWarnings ("unchecked")
	public static <EntityClass> CVerticalLayoutTop buildEnhancedForm(final Class<?> entityClass, final List<String> entityFields) throws Exception {
		final CEnhancedBinder<EntityClass> enhancedBinder = CBinderFactory.createEnhancedBinder((Class<EntityClass>) entityClass);
		return buildForm(entityClass, enhancedBinder, entityFields);
	}

	public static <EntityClass> CVerticalLayoutTop buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder) throws Exception {
		return buildForm(entityClass, binder, null, null, null, new CVerticalLayoutTop(false, false, false));
	}

	public static <EntityClass> CVerticalLayoutTop buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields) throws Exception {
		return buildForm(entityClass, binder, entityFields, null, null, new CVerticalLayoutTop(false, false, false));
	}

	public static <EntityClass> CVerticalLayoutTop buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields, final IContentOwner contentOwner) throws Exception {
		return buildForm(entityClass, binder, entityFields, null, null, new CVerticalLayoutTop(false, false, false), contentOwner);
	}

	public static <EntityClass> CVerticalLayoutTop buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields, final Map<String, Component> mapComponents, final Map<String, CHorizontalLayout> mapHorizontalLayouts,
			final CVerticalLayoutTop formLayout) throws Exception {
		return buildForm(entityClass, binder, entityFields, mapComponents, mapHorizontalLayouts, formLayout, null);
	}

	public static <EntityClass> CVerticalLayoutTop buildForm(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields, final Map<String, Component> mapComponents, final Map<String, CHorizontalLayout> mapHorizontalLayouts,
			final CVerticalLayoutTop formLayout, final IContentOwner contentOwner) throws Exception {
		try {
			Check.notNull(entityClass, "Entity class cannot be null");
			// Set content owner in data provider resolver context
			// final FormLayout formLayout = new FormLayout();
			final List<Field> allFields = new ArrayList<>();
			getListOfAllFields(entityClass, allFields);
			final List<Field> sortedFields = getSortedFilteredFieldsList(allFields);
			// LOGGER.info("Processing {} visible fields for form generation", sortedFields.size());
			// Create components with enhanced error handling and logging
			final List<String> resolvedEntityFields =
					entityFields != null ? entityFields : sortedFields.stream().map(Field::getName).collect(Collectors.toList());
			for (final String fieldName : resolvedEntityFields) {
				final Field field = sortedFields.stream().filter(f -> f.getName().equals(fieldName)).findFirst().orElse(null);
				if (field == null) {
					LOGGER.warn("Field '{}' not found in entity class {}", fieldName, entityClass.getSimpleName());
				}
				Check.notNull(field, "Field '" + fieldName + "' not found in entity class " + entityClass.getSimpleName());
				final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(field);
				processField(contentOwner, binder, formLayout, mapHorizontalLayouts, fieldInfo, mapComponents);
			}
			return formLayout;
		} catch (final Exception e) {
			LOGGER.error("Error building form for entity class '{}': {}", entityClass.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	/** Builds a form with content owner support for context-aware data providers.
	 * @param <EntityClass> the entity class type
	 * @param entityClass   the entity class
	 * @param binder        the enhanced binder
	 * @param contentOwner  the content owner (page) for context-aware data providers
	 * @return the form layout
	 * @throws Exception if form building fails */
	public static <EntityClass> CVerticalLayoutTop buildFormWithOwner(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final IContentOwner contentOwner) throws Exception {
		return buildForm(entityClass, binder, null, contentOwner);
	}

	private static NumberField createBigDecimalField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "FieldInfo for BigDecimal field creation");
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(0.01); // Set decimal step for BigDecimal fields
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
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
			// LOGGER.debug("Successfully bound NumberField with BigDecimal converter for field '{}'", fieldInfo.getFieldName());
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
		try {
			Check.notNull(fieldInfo, "FieldInfo for checkbox creation");
			final Checkbox checkbox = new Checkbox();
			// Set ID for better test automation
			CAuxillaries.setId(checkbox);
			// Safe null checking and parsing for default value
			if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
				checkbox.setValue(Boolean.parseBoolean(fieldInfo.getDefaultValue()));
			}
			safeBindComponent(binder, checkbox, fieldInfo.getFieldName(), "Checkbox");
			return checkbox;
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind checkbox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	private static Component createColorPicker(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		try {
			final CColorPickerComboBox colorPicker = new CColorPickerComboBox(fieldInfo);
			safeBindComponent(binder, colorPicker, fieldInfo.getFieldName(), "ColorPicker");
			return colorPicker;
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind color picker for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	public static <T extends CEntityDB<T>> CNavigableComboBox<T> createComboBox(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		try {
			Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
			// Create navigable combobox with navigation button - it's a CustomField so binding is done on it, not on internal combobox
			final CNavigableComboBox<T> navigableComboBox = new CNavigableComboBox<>(contentOwner, fieldInfo, dataProviderResolver);
			// Data provider resolution using CDataProviderResolver
			List<T> items = null;
			Check.notNull(dataProviderResolver, "DataProviderResolver for field " + fieldInfo.getFieldName());
			items = dataProviderResolver.resolveDataList(contentOwner, fieldInfo);
			Check.notNull(items, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
			if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
				navigableComboBox.setValue(null);
			}
			navigableComboBox.setItems(items);
			if (!items.isEmpty()) {
				if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
					// For entity types, try to find by name or toString match
					final T defaultItem = items.stream().filter(item -> {
						final String itemDisplay = CColorUtils.getDisplayTextFromEntity(item);
						return fieldInfo.getDefaultValue().equals(itemDisplay);
					}).findFirst().orElse(null);
					if (defaultItem != null) {
						navigableComboBox.setValue(defaultItem);
					}
				} else if (fieldInfo.isAutoSelectFirst()) {
					navigableComboBox.setValue(items.get(0));
				}
			}
			// Bind the CustomField (not the internal combobox)
			if (binder != null) {
				safeBindComponent(binder, navigableComboBox, fieldInfo.getFieldName(), "NavigableComboBox");
			}
			return navigableComboBox;
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind NavigableComboBox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	@SuppressWarnings ("unchecked")
	private static <T> MultiSelectComboBox<T> createComboBoxMultiSelect(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		try {
			Check.notNull(fieldInfo, "FieldInfo for ComboBox creation");
			LOGGER.debug("Creating MultiSelectComboBox for field: {}", fieldInfo.getFieldName());
			final MultiSelectComboBox<T> comboBox = new MultiSelectComboBox<>(fieldInfo.getIsCaptionVisible() ? fieldInfo.getDisplayName() : "");
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
			final List<?> rawList = dataProviderResolver.resolveDataList(contentOwner, fieldInfo);
			Check.notNull(rawList, "Items for field " + fieldInfo.getFieldName() + " of type " + fieldInfo.getJavaType());
			// Tip güvenli toplama: LinkedHashSet ile sıralı ve benzersiz
			final LinkedHashSet<T> items = rawList.stream().map(e -> (T) e) // runtime cast; provider sözleşmesine güveniyoruz
					.collect(Collectors.toCollection(LinkedHashSet::new));
			if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
				comboBox.clear(); // Set.of() vermek yerine clear()
			}
			comboBox.setItems(items);
			// (İsteğe bağlı) Varsayılan değer atama burada yapılabilir.
			safeBindComponent(binder, comboBox, fieldInfo.getFieldName(), "ComboBox(MultiSelect)");
			return comboBox;
		} catch (final Exception e) {
			LOGGER.error("Failed to create or bind MultiSelectComboBox for field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	private static Component createComponentForField(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		try {
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
			if ("approvals".equals(fieldInfo.getFieldName()) || "status".equals(fieldInfo.getFieldName())) {
				LOGGER.info("Skipping field 'approvals' as it is handled separately");
			}
			// Check if field should be rendered as ComboBox based on metadata
			final boolean hasDataProvider = hasValidDataProvider(fieldInfo.getDataProviderBean());
			if (hasDataProvider && fieldType == String.class) {
				// gets strings from a method in a spring bean
				component = createStringComboBox(contentOwner, fieldInfo, binder);
			} else if (hasDataProvider && "Set".equals(fieldInfo.getJavaType())) {
				// Check if should use grid selection, dual list selector, or multiselect combobox
				if (fieldInfo.isUseGridSelection()) {
					component = createGridListSelector(contentOwner, fieldInfo, binder);
				} else if (fieldInfo.isUseDualListSelector()) {
					component = createDualListSelector2(contentOwner, fieldInfo, binder);
				} else {
					component = createComboBoxMultiSelect(contentOwner, fieldInfo, binder);
				}
			} else if (hasDataProvider && fieldType == List.class) {
				// Check if should use grid selection, dual list selector, or multiselect combobox
				if (fieldInfo.isUseGridSelection()) {
					component = createGridListSelector(contentOwner, fieldInfo, binder);
				} else if (fieldInfo.isUseDualListSelector()) {
					component = createDualListSelector2(contentOwner, fieldInfo, binder);
				} else {
					component = createComboBoxMultiSelect(contentOwner, fieldInfo, binder);
				}
			} else if (!hasDataProvider && (Set.class.isAssignableFrom(fieldType) || List.class.isAssignableFrom(fieldType)
					|| Collection.class.isAssignableFrom(fieldType))) {
				// Collection fields without data provider (e.g., OneToMany relationships like attachments, comments)
				// These should be handled by separate specialized components, not in the main form
				LOGGER.debug("Skipping collection field '{}' of type {} - handled by separate component", fieldInfo.getFieldName(),
						fieldType.getSimpleName());
				return null; // Return null to skip this field in form
			} else if (fieldType == Integer.class || fieldType == int.class || fieldType == Long.class || fieldType == long.class) {
				// Integer types
				component = createIntegerField(fieldInfo, binder);
			} else if (fieldType == BigDecimal.class) {
				component = createBigDecimalField(fieldInfo, binder);
			} else if (fieldType == Double.class || fieldType == double.class || fieldType == Float.class || fieldType == float.class) {
				// Floating-point types
				component = createFloatingPointField(fieldInfo, binder);
			} else if (fieldType == LocalDate.class) {
				component = createDatePicker(fieldInfo, binder);
			} else if (fieldType == LocalTime.class) {
				component = createTimePicker(fieldInfo, binder);
			} else if (fieldType == LocalDateTime.class || fieldType == Instant.class) {
				component = createDateTimePicker(fieldInfo, binder);
			} else if (fieldType.isEnum()) {
				component = createEnumComponent(fieldInfo, binder);
			} else if (fieldType == byte[].class && fieldInfo.isImageData()) {
				component = createPictureSelector(fieldInfo, binder);
			} else if (hasDataProvider || CEntityDB.class.isAssignableFrom(fieldType)) {
				// it has a dataprovider or entity type
				// dont mark everything as a list, when they have a data provider
				// dataprovider can also return single items !!!
				if (!hasValidDataProvider(fieldInfo.getDataProviderBean())) {
					fieldInfo.setDataProviderBean(fieldType.getSimpleName() + "Service");
				}
				component = createComboBox(contentOwner, fieldInfo, binder);
			} else if (fieldType == Boolean.class || fieldType == boolean.class) {
				component = createCheckbox(fieldInfo, binder);
			} else if (fieldType == String.class) {
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
			} else {
				Check.isTrue(false, "Component field [" + fieldInfo.getFieldName() + "], unsupported field type [" + fieldType.getSimpleName()
						+ "] for field [" + fieldInfo.getDisplayName() + "]");
			}
			// Allow null component for fields that should be skipped (handled by separate components)
			if (component == null) {
				return null;
			}
			setRequiredIndicatorVisible(fieldInfo, component);
			// dont use helper text for Checkbox components setHelperText(meta, component);
			setComponentWidth(component, fieldInfo.getWidth());
			// setclass name for styling in format of form-field{ComponentType}
			component.setClassName("form-field-" + component.getClass().getSimpleName());
			// Create field
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating component for field '{}", fieldInfo.getFieldName());
			throw e;
		}
	}

	/** Creates a custom component by invoking the method specified in createComponentMethod. Supports comma-separated method names and will try each
	 * method until one succeeds.
	 * @param contentOwner the content owner (page) for context
	 * @param fieldInfo    field information containing the method name(s)
	 * @param binder       the enhanced binder for form binding
	 * @return the custom component or null if creation fails
	 * @throws Exception */
	private static Component createCustomComponent(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder)
			throws Exception {
		try {
			Check.notNull(fieldInfo, "FieldInfo for custom component creation");
			Check.notNull(fieldInfo.getCreateComponentMethod(), "CreateComponentMethod for custom component creation");
			// binder may be null in some call paths, but when provided we should attempt to bind the custom component
			// Object bean = dataProviderResolver.resolveBean(fieldInfo.getDataProviderBean(), contentOwner);
			// use first method only
			final String methodName = fieldInfo.getCreateComponentMethod();
			final String oldBeanMethod = fieldInfo.getDataProviderMethod();
			fieldInfo.setDataProviderMethod(methodName); // geçici olarak method adını değiştir
			//
			final Component component = dataProviderResolver.resolveDataComponent(contentOwner, fieldInfo);
			fieldInfo.setDataProviderMethod(oldBeanMethod); // orijinal method adına geri dön
			Check.notNull(component, "Custom component created by method " + methodName + " for field " + fieldInfo.getFieldName());
			// Set id for better test automation (consistent with other creators)
			CAuxillaries.setId(component);
			// Attempt to bind if component exposes HasValueAndElement (bindable)
			if (binder != null) {
				if (component instanceof HasValueAndElement) {
					final HasValueAndElement<?, ?> bindable = (HasValueAndElement<?, ?>) component;
					safeBindComponent(binder, bindable, fieldInfo.getFieldName(), "CustomComponent");
				} else if (component instanceof IContentOwner) {
					// If the custom component is a content owner, let it receive the entity value via setValue when populating
					LOGGER.debug("Custom component for field '{}' is an IContentOwner and will not be auto-bound by binder",
							fieldInfo.getFieldName());
				} else {
					LOGGER.debug("Custom component for field '{}' is not bindable (no HasValueAndElement) - skipping binder binding",
							fieldInfo.getFieldName());
				}
			}
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating custom component for field {}: {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
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

	private static <EntityClass, DetailClass> CComponentFieldSelection<EntityClass, DetailClass> createDualListSelector2(
			final IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) throws Exception {
		Check.notNull(fieldInfo, "FieldInfo for DualListSelector creation");
		LOGGER.debug("Creating CComponentFieldSelection for field: {}", fieldInfo.getFieldName());
		final CComponentFieldSelection<EntityClass, DetailClass> dualListSelector = new CComponentFieldSelection<>(
				dataProviderResolver, contentOwner, fieldInfo, "Available " + fieldInfo.getDisplayName(), "Selected " + fieldInfo.getDisplayName());
		// Set item label generator based on entity type
		dualListSelector.setItemLabelGenerator(item -> {
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
		if (binder != null && fieldInfo.getFieldTypeClass() != null && Set.class.isAssignableFrom(fieldInfo.getFieldTypeClass())) {
			@SuppressWarnings ("unchecked")
			final CEnhancedBinder<Object> typedBinder = (CEnhancedBinder<Object>) binder;
			final Converter<List<DetailClass>, Set<DetailClass>> converter = new Converter<>() {

				@Override
				public Result<Set<DetailClass>> convertToModel(final List<DetailClass> value, final ValueContext context) {
					if (value == null) {
						return Result.ok(new LinkedHashSet<>());
					}
					return Result.ok(new LinkedHashSet<>(value));
				}

				@Override
				public List<DetailClass> convertToPresentation(final Set<DetailClass> value, final ValueContext context) {
					if (value == null) {
						return new ArrayList<>();
					}
					return new ArrayList<>(value);
				}
			};
			typedBinder.forField(dualListSelector).withConverter(converter).bind(fieldInfo.getFieldName());
		} else if (binder != null && fieldInfo.getFieldTypeClass() != null && Collection.class.isAssignableFrom(fieldInfo.getFieldTypeClass())
				&& !List.class.isAssignableFrom(fieldInfo.getFieldTypeClass())) {
			@SuppressWarnings ("unchecked")
			final CEnhancedBinder<Object> typedBinder = (CEnhancedBinder<Object>) binder;
			final Converter<List<DetailClass>, Collection<DetailClass>> converter = new Converter<>() {

				@Override
				public Result<Collection<DetailClass>> convertToModel(final List<DetailClass> value, final ValueContext context) {
					if (value == null) {
						return Result.ok(new ArrayList<>());
					}
					return Result.ok(new ArrayList<>(value));
				}

				@Override
				public List<DetailClass> convertToPresentation(final Collection<DetailClass> value, final ValueContext context) {
					if (value == null) {
						return new ArrayList<>();
					}
					return new ArrayList<>(value);
				}
			};
			typedBinder.forField(dualListSelector).withConverter(converter).bind(fieldInfo.getFieldName());
		} else {
			safeBindComponent(binder, dualListSelector, fieldInfo.getFieldName(), "CComponentFieldSelection");
		}
		return dualListSelector;
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
		}
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

	private static CHorizontalLayout createFieldLayout(final EntityFieldInfo fieldInfo, final Component component) {
		Check.notNull(fieldInfo, "AMetaData for field layout");
		Check.notNull(component, "Component for field layout" + fieldInfo.getFieldName());
		final CHorizontalLayout horizontalLayout = CHorizontalLayout.forForm();
		if (fieldInfo.getIsCaptionVisible() && !fieldInfo.getDisplayName().isBlank()) {
			// if label is empty, we do not show it
			final CDiv labelDiv = new CDiv(fieldInfo.getDisplayName());
			labelDiv.setClassName("form-field-label");
			if (fieldInfo.isRequired()) {
				labelDiv.getStyle().set("font-weight", "bold");
			}
			horizontalLayout.add(labelDiv);
		}
		horizontalLayout.add(component);
		return horizontalLayout;
	}

	private static NumberField createFloatingPointField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final NumberField numberField = new NumberField();
		// Set ID for better test automation
		CAuxillaries.setId(numberField);
		// Set step for floating point fields
		numberField.setStep(0.01);
		// Set default value if specified
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		safeBindComponent(binder, numberField, fieldInfo.getFieldName(), "NumberField");
		return numberField;
	}

	private static <EntityClass, DetailClass> CComponentListSelection<EntityClass, DetailClass> createGridListSelector(
			final IContentOwner contentOwner, final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) throws Exception {
		Check.notNull(fieldInfo, "FieldInfo for GridListSelector creation");
		LOGGER.debug("Creating CComponentListSelection for field: {}", fieldInfo.getFieldName());
		final CComponentListSelection<EntityClass, DetailClass> gridListSelector = new CComponentListSelection<>(
				dataProviderResolver, contentOwner, fieldInfo, fieldInfo.getDisplayName(), fieldInfo.getFieldTypeClass());
		// Set item label generator based on entity type
		gridListSelector.setItemLabelGenerator(item -> {
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
		safeBindComponent(binder, gridListSelector, fieldInfo.getFieldName(), "CComponentListSelection");
		return gridListSelector;
	}

	private static ComboBox<String> createIconComboBox(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		Check.notNull(fieldInfo, "Field for Icon ComboBox creation");
		final ComboBox<String> comboBox = new ComboBox<>();
		// Configure basic properties from metadata
		comboBox.setLabel(fieldInfo.getIsCaptionVisible() ? fieldInfo.getDisplayName() : "");
		comboBox.setPlaceholder(fieldInfo.getPlaceholder().isEmpty() ? "Select an icon" : fieldInfo.getPlaceholder());
		comboBox.setAllowCustomValue(fieldInfo.isAllowCustomValue());
		comboBox.setReadOnly(fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly());
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			comboBox.setWidth(fieldInfo.getWidth());
		}
		// Get list of all Vaadin icons
		final List<String> iconItems = new ArrayList<>(getVaadinIconNames());
		final ListDataProvider<String> dataProvider = new ListDataProvider<>(iconItems);
		comboBox.setItems(dataProvider);
		// Set up custom renderer to show icon with name
		comboBox.setRenderer(new ComponentRenderer<>(iconName -> {
			if (iconName == null || iconName.isEmpty()) {
				return new Span("No icon selected");
			}
			final HorizontalLayout layout = new HorizontalLayout();
			layout.setAlignItems(FlexComponent.Alignment.CENTER);
			layout.setSpacing(true);
			try {
				// Parse the icon name (remove "vaadin:" prefix if present)
				final String cleanIconName = iconName.startsWith("vaadin:") ? iconName.substring(7) : iconName;
				final VaadinIcon vaadinIcon = VaadinIcon.valueOf(cleanIconName.toUpperCase().replace("-", "_"));
				final Icon icon = CColorUtils.styleIcon(vaadinIcon.create());
				layout.add(icon);
			} catch (final Exception e) {
				// If icon cannot be created, show a placeholder
				final Span placeholder = new Span("?");
				placeholder.getStyle().set("width", "16px").set("text-align", "center");
				layout.add(placeholder);
			}
			final Span label = new Span(iconName);
			layout.add(label);
			return layout;
		}));
		// Set item label generator for text representation
		comboBox.setItemLabelGenerator(iconName -> iconName);
		// Handle default value
		final boolean hasDefaultValue = fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty();
		if (hasDefaultValue) {
			final String defaultIcon = fieldInfo.getDefaultValue();
			if (iconItems.contains(defaultIcon)) {
				comboBox.setValue(defaultIcon);
				LOGGER.debug("Set Icon ComboBox default value for field '{}': '{}'", fieldInfo.getFieldName(), defaultIcon);
			} else {
				LOGGER.warn("Default icon '{}' not found in available icons for field '{}'", defaultIcon, fieldInfo.getFieldName());
			}
		} else if (fieldInfo.isAutoSelectFirst() && !iconItems.isEmpty()) {
			// Auto-select first item if configured
			comboBox.setValue(iconItems.get(0));
			LOGGER.debug("Auto-selected first icon for field '{}': '{}'", fieldInfo.getFieldName(), iconItems.get(0));
		}
		comboBox.addCustomValueSetListener(event -> {
			final String customValue = event.getDetail();
			if (customValue != null && !customValue.isBlank() && !iconItems.contains(customValue)) {
				iconItems.add(customValue);
				dataProvider.refreshAll();
			}
			comboBox.setValue(customValue);
		});
		// Bind to field with a converter that accepts non-standard icons already stored in the database.
		final Converter<String, String> iconConverter = new Converter<>() {

			@Override
			public Result<String> convertToModel(final String value, final ValueContext context) {
				return Result.ok(value);
			}

			@Override
			public String convertToPresentation(final String value, final ValueContext context) {
				if (value != null && !value.isBlank() && !iconItems.contains(value)) {
					iconItems.add(value);
					dataProvider.refreshAll();
				}
				return value;
			}
		};
		binder.forField(comboBox).withConverter(iconConverter).bind(fieldInfo.getFieldName());
		return comboBox;
	}

	private static NumberField createIntegerField(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final NumberField numberField = new NumberField();
		CAuxillaries.setId(numberField);
		numberField.setStep(1);
		// Set default value if specified
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			final double defaultVal = Double.parseDouble(fieldInfo.getDefaultValue());
			numberField.setValue(defaultVal);
		}
		// Handle different integer types with proper conversion
		final Class<?> fieldType = fieldInfo.getFieldTypeClass();
		final String propertyName = fieldInfo.getFieldName();
		if (fieldType == Integer.class || fieldType == int.class) {
			binder.forField(numberField).withConverter(value -> value != null ? value.intValue() : null,
					value -> value != null ? value.doubleValue() : null, "Invalid integer value").bind(propertyName);
			// LOGGER.debug("Successfully bound NumberField with Integer converter for field '{}'", fieldInfo.getFieldName());
		} else if (fieldType == Long.class || fieldType == long.class) {
			binder.forField(numberField).withConverter(value -> value != null ? value.longValue() : null,
					value -> value != null ? value.doubleValue() : null, "Invalid long value").bind(propertyName);
			// LOGGER.debug("Successfully bound NumberField with Long converter for field '{}'", fieldInfo.getFieldName());
		} else {
			// Fallback for other number types (Double, etc.)
			binder.bind(numberField, propertyName);
		}
		return numberField;
	}

	private static Component createPictureSelector(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		// LOGGER.debug("Creating CPictureSelector for field: {}", fieldInfo.getFieldName());
		final CPictureSelector pictureSelector = new CPictureSelector(fieldInfo);
		safeBindComponent(binder, pictureSelector, fieldInfo.getFieldName(), "PictureSelector");
		// LOGGER.debug("Successfully created CPictureSelector for field: {}", fieldInfo.getFieldName());
		return pictureSelector;
	}

	private static ComboBox<String> createStringComboBox(final IContentOwner contentOwner, final EntityFieldInfo fieldInfo,
			final CEnhancedBinder<?> binder) throws Exception {
		Check.notNull(fieldInfo, "Field for String ComboBox creation");
		final ComboBox<String> comboBox = new ComboBox<>();
		// Configure basic properties from metadata
		comboBox.setLabel(fieldInfo.getIsCaptionVisible() ? fieldInfo.getDisplayName() : "");
		comboBox.setPlaceholder(fieldInfo.getPlaceholder());
		comboBox.setAllowCustomValue(fieldInfo.isAllowCustomValue());
		comboBox.setReadOnly(fieldInfo.isComboboxReadOnly() || fieldInfo.isReadOnly());
		// Set width if specified
		if (!fieldInfo.getWidth().trim().isEmpty()) {
			comboBox.setWidth(fieldInfo.getWidth());
		}
		// Resolve String data using data provider
		// final List<String> items = resolveStringData(fieldInfo);
		final List<String> items = dataProviderResolver.<String>resolveDataList(contentOwner, fieldInfo);
		comboBox.setItems(items);
		// Handle clearOnEmptyData configuration
		if (fieldInfo.isClearOnEmptyData() && items.isEmpty()) {
			comboBox.setValue(null);
		}
		// Handle default value
		final boolean hasDefaultValue = fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty();
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
		item.setMinWidth("200px");
		item.setMaxWidth("800px");
		item.setMinHeight("100px");
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
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
		item.setMinWidth("200px");
		item.setMaxWidth("800px");
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			try {
				item.setValue(fieldInfo.getDefaultValue());
			} catch (final Exception e) {
				LOGGER.error("Failed to set default value '{}' for text field '{}': {}", fieldInfo.getDefaultValue(), fieldInfo.getFieldName(),
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
		item.setMinWidth("200px");
		item.setMaxWidth("800px");
		if (fieldInfo.getDefaultValue() != null && !fieldInfo.getDefaultValue().trim().isEmpty()) {
			item.setValue(fieldInfo.getDefaultValue());
		}
		item.setRevealButtonVisible(fieldInfo.isPasswordRevealButton());
		return item;
	}

	private static TimePicker createTimePicker(final EntityFieldInfo fieldInfo, final CEnhancedBinder<?> binder) {
		final TimePicker timePicker = new TimePicker();
		CAuxillaries.setId(timePicker);
		safeBindComponent(binder, timePicker, fieldInfo.getFieldName(), "TimePicker");
		return timePicker;
	}

	private static void getListOfAllFields(final Class<?> entityClass, final List<Field> allFields) {
		Class<?> current = entityClass;
		while (current != null && current != Object.class) {
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
		}).collect(Collectors.toList());
		// .sorted(Comparator.comparingInt(field -> {
		// final AMetaData metaData = field.getAnnotation(AMetaData.class);
		// return metaData != null ? metaData.order() : Integer.MAX_VALUE;
	}

	private static List<String> getVaadinIconNames() {
		final List<String> iconNames = new ArrayList<>();
		// Add all VaadinIcon enum values as "vaadin:iconname" format
		for (final VaadinIcon icon : VaadinIcon.values()) {
			final String iconName = "vaadin:" + icon.name().toLowerCase().replace("_", "-");
			iconNames.add(iconName);
		}
		// Sort the list for better user experience
		iconNames.sort(String::compareTo);
		return iconNames;
	}

	private static boolean hasValidDataProvider(final String dataProviderBean) {
		return dataProviderBean != null && !dataProviderBean.trim().isEmpty();
		// && !"none".equalsIgnoreCase(dataProviderBean.trim());
	}

	public static <EntityClass> Component processField(final IContentOwner contentOwner, final CEnhancedBinder<EntityClass> binder,
			final VerticalLayout formLayout, final Map<String, CHorizontalLayout> mapHorizontalLayouts, final EntityFieldInfo fieldInfo,
			final Map<String, Component> mapComponents) throws Exception {
		try {
			Check.notNull(fieldInfo, "field");
			final Component component = createComponentForField(contentOwner, fieldInfo, binder);
			// Allow null components for fields that should be skipped (e.g., collection fields handled separately)
			if (component == null) {
				LOGGER.debug("Skipping field '{}' - component creation returned null (handled separately)", fieldInfo.getFieldName());
				return null;
			}
			assignDeterministicComponentId(component, fieldInfo, binder);
			// Navigation button is now integrated into CNavigableComboBox
			final CHorizontalLayout horizontalLayout = createFieldLayout(fieldInfo, component);
			formLayout.add(horizontalLayout);
			if (mapHorizontalLayouts != null) {
				mapHorizontalLayouts.put(fieldInfo.getFieldName(), horizontalLayout);
			}
			if (mapComponents != null) {
				// LOGGER.debug("Adding component for field '{}' to component map of type:{}",
				// fieldInfo.getFieldName(),component.getClass().getSimpleName());
				mapComponents.put(fieldInfo.getFieldName(), component);
			}
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error processing field '{}': {}", fieldInfo.getFieldName(), e.getMessage());
			throw e;
		}
	}

	/** Recursively searches for ComboBox components and resets them to their first item. */
	@SuppressWarnings ("unchecked")
	private static void resetComboBoxesRecursively(final HasComponents container) {
		container.getElement().getChildren().forEach(element -> {
			// Get the component from the element
			if (element.getComponent().isPresent()) {
				final Component component = element.getComponent().get();
				if (component instanceof ComboBox) {
					final ComboBox<Object> comboBox = (ComboBox<Object>) component;
					try {
						// Get the first item from the ComboBox data provider
						final Optional<Object> firstItem = comboBox.getDataProvider().fetch(new Query<>()).findFirst();
						if (firstItem.isPresent()) {
							comboBox.setValue(firstItem.get());
							LOGGER.debug("Reset ComboBox to first item: {}", firstItem.get());
						} else {
							LOGGER.debug("ComboBox has no items to reset to");
						}
					} catch (final Exception e) {
						LOGGER.error("Error resetting ComboBox to first item: {}", e.getMessage());
					}
				} else if (component instanceof HasComponents) {
					// Recursively check child components
					resetComboBoxesRecursively((HasComponents) component);
				}
			}
		});
	}

	public static void resetComboBoxesToFirstItem(final HasComponents container) {
		Check.notNull(container, "Container for resetting ComboBoxes to first item");
		resetComboBoxesRecursively(container);
	}

	private static void safeBindComponent(final CEnhancedBinder<?> binder, final HasValueAndElement<?, ?> component, final String fieldName,
			final String componentType) {
		try {
			if (binder == null) {
				LOGGER.warn("Binder is null, wont bind component of type '{}' for field '{}'", componentType, fieldName);
				return;
			}
			Check.notNull(component, "Component for safe binding");
			Check.notNull(fieldName, "Field name for safe binding");
			binder.bind(component, fieldName);
		} catch (final Exception e) {
			LOGGER.error("Failed to bind {}:{} for field '{}': {} - this may cause incomplete bindings", componentType, component, fieldName,
					e.getMessage());
			throw e;
		}
	}

	private static void setComponentWidth(final Component component, final String width) {
		try {
			Check.notNull(width, "Width for component width setting");
			Check.isTrue(!width.trim().isEmpty() || width.trim().isEmpty(), "Width format for component width setting");
			Check.notNull(component, "Component for width setting");
			if (!(component instanceof HasSize)) {
				return;
			}
			final HasSize hasSize = (HasSize) component;
			if (width.trim().isEmpty()) {
				// Set full width with min and max constraints for better responsiveness
				hasSize.setWidthFull();
				hasSize.setMinWidth("200px");
				hasSize.setMaxWidth("800px");
			} else {
				// Use specified width but still add min width for usability
				hasSize.setWidth(width);
				hasSize.setMinWidth("150px");
			}
		} catch (final Exception e) {
			LOGGER.warn("Failed to set component width '{}': {}", width, e.getMessage());
			throw e;
		}
	}

	private static void setRequiredIndicatorVisible(final EntityFieldInfo fieldInfo, final Component field) {
		((HasValueAndElement<?, ?>) field).setReadOnly(fieldInfo.isReadOnly());
		((HasValueAndElement<?, ?>) field).setRequiredIndicatorVisible(fieldInfo.isRequired());
	}

	private CEnhancedBinder<?> binder;
	private final Map<String, Component> componentMap;
	final CVerticalLayoutTop formLayout;
	final Map<String, CHorizontalLayout> horizontalLayoutMap;

	public CFormBuilder() {
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
		formLayout = new CVerticalLayoutTop(false, false, false);
	}

	public CFormBuilder(final IContentOwner contentOwner, final Class<?> entityClass) throws Exception {
		this(contentOwner, entityClass, createEnhancedBinder(entityClass), List.of());
	}

	public CFormBuilder(final IContentOwner contentOwner, final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder) throws Exception {
		this(contentOwner, entityClass, binder, List.of());
	}

	public CFormBuilder(final IContentOwner contentOwner, final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final List<String> entityFields) throws Exception {
		componentMap = new HashMap<>();
		horizontalLayoutMap = new HashMap<>();
		formLayout = new CVerticalLayoutTop(false, false, false);
		this.binder = binder;
		CFormBuilder.buildForm(entityClass, binder, entityFields, getComponentMap(), horizontalLayoutMap, formLayout, contentOwner);
	}

	/** Constructor that accepts an external component map for centralized component management.
	 * @param contentOwner         the content owner (page) for context
	 * @param entityClass          the entity class
	 * @param binder               the enhanced binder
	 * @param externalComponentMap the external component map to use instead of creating a new one
	 * @throws Exception if form building fails */
	public CFormBuilder(final IContentOwner contentOwner, final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
			final Map<String, Component> externalComponentMap) throws Exception {
		Check.notNull(externalComponentMap, "External component map cannot be null");
		componentMap = externalComponentMap;
		horizontalLayoutMap = new HashMap<>();
		formLayout = new CVerticalLayoutTop(false, false, false);
		this.binder = binder;
		CFormBuilder.buildForm(entityClass, binder, List.of(), getComponentMap(), horizontalLayoutMap, formLayout, contentOwner);
	}

	public Component addFieldLine(final EntityFieldInfo fieldInfo) throws Exception {
		return CFormBuilder.processField(null, binder, formLayout, horizontalLayoutMap, fieldInfo, getComponentMap());
	}

	public Component addFieldLine(final IContentOwner contentOwner, final String screenClassType, final CDetailLines line,
			final VerticalLayout layout, final Map<String, Component> componentMap2, final Map<String, CHorizontalLayout> horizontalLayoutMap2)
			throws Exception {
		final EntityFieldInfo fieldInfo = CEntityFieldService.createFieldInfo(screenClassType, line);
		// Use the provided componentMap2 instead of getComponentMap() to support centralized component maps
		return CFormBuilder.processField(contentOwner, binder, layout, horizontalLayoutMap2, fieldInfo, componentMap2);
	}

	public CVerticalLayoutTop build(final Class<?> entityClass, final CEnhancedBinder<EntityClass> ebinder, final List<String> entityFields)
			throws Exception {
		return CFormBuilder.buildForm(entityClass, ebinder, entityFields, getComponentMap(), horizontalLayoutMap, formLayout);
	}

	/** Gets the internal binder for this form builder.
	 * @return the enhanced binder */
	public CEnhancedBinder<?> getBinder() { return binder; }

	public Component getComponent(final String fieldName) {
		Check.notNull(fieldName, "Field name for component retrieval");
		final Component component = getComponentMap().get(fieldName);
		Check.notNull(component, "Component for field " + fieldName + " not found in form builder map");
		return component;
	}

	public Map<String, Component> getComponentMap() { return componentMap; }

	public CVerticalLayoutTop getFormLayout() { return formLayout; }

	public CHorizontalLayout getHorizontalLayout(final String fieldName) {
		Check.notNull(fieldName, "Field name for horizontal layout retrieval");
		final CHorizontalLayout layout = horizontalLayoutMap.get(fieldName);
		Check.notNull(layout, "HorizontalLayout for field " + fieldName + " not found in form builder map");
		return layout;
	}

	/** Clears the form by setting the binder bean to null. */
	public void populateForm() {
		// go through all components and call populate if available
		getComponentMap().values().forEach(component -> {
			if (component instanceof IContentOwner) {
				try {
					((IContentOwner) component).populateForm();
				} catch (final org.hibernate.LazyInitializationException e) {
					LOGGER.error("LazyInitializationException populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(),
							e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService
							.showError("Failed to load " + component.getClass().getSimpleName() + ": Data not available in current session"));
					throw new RuntimeException("LazyInitializationException in " + component.getClass().getSimpleName(), e);
				} catch (final Exception e) {
					LOGGER.error("Error populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(), e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService.showError("Error loading " + component.getClass().getSimpleName()));
					throw new RuntimeException("Error populating form component " + component.getClass().getSimpleName(), e);
				}
			}
		});
	}

	/** Populates the form with entity data using the internal binder.
	 * @param entity the entity to populate the form with */
	@SuppressWarnings ("unchecked")
	public void populateForm(final CEntityDB<?> entity) {
		Check.notNull(entity, "Entity for form population");
		Check.notNull(binder, "Binder for form population");
		LOGGER.debug("Populating form with entity: {}", entity);
		((CEnhancedBinder<Object>) binder).setBean(entity);
		getComponentMap().values().forEach(component -> {
			if (component instanceof IContentOwner) {
				try {
					((IContentOwner) component).setValue(entity);
					((IContentOwner) component).populateForm();
				} catch (final org.hibernate.LazyInitializationException e) {
					LOGGER.error("LazyInitializationException populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(),
							e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService
							.showError("Failed to load " + component.getClass().getSimpleName() + ": Data not available in current session"));
					throw new RuntimeException("LazyInitializationException in " + component.getClass().getSimpleName(), e);
				} catch (final Exception e) {
					LOGGER.error("Error populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(), e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService.showError("Error loading " + component.getClass().getSimpleName()));
					throw new RuntimeException("Error populating form component " + component.getClass().getSimpleName(), e);
				}
			}
		});
	}

	/** Sets the application context and initializes the data provider resolver. This method is called automatically by Spring.
	 * @param context the Spring application context */
	@Override
	public void setApplicationContext(final ApplicationContext context) {
		try {
			CFormBuilder.dataProviderResolver = context.getBean(CDataProviderResolver.class);
		} catch (final Exception e) {
			LOGGER.warn("Failed to initialize CDataProviderResolver - annotation-based providers will not work: {}", e.getMessage());
			CFormBuilder.dataProviderResolver = null;
		}
	}

	public void setValue(final CEntityDB<?> entity) {
		getComponentMap().values().forEach(component -> {
			if (component instanceof IContentOwner) {
				try {
					((IContentOwner) component).setValue(entity);
				} catch (final org.hibernate.LazyInitializationException e) {
					LOGGER.error("LazyInitializationException populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(),
							e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService
							.showError("Failed to load " + component.getClass().getSimpleName() + ": Data not available in current session"));
					throw new RuntimeException("LazyInitializationException in " + component.getClass().getSimpleName(), e);
				} catch (final Exception e) {
					LOGGER.error("Error populating form component {}: {}", component.getClass().getSimpleName(), e.getMessage(), e);
					// Show notification to user
					UI.getCurrent().access(() -> CNotificationService.showError("Error loading " + component.getClass().getSimpleName()));
					throw new RuntimeException("Error populating form component " + component.getClass().getSimpleName(), e);
				}
			}
		});
	}
}
