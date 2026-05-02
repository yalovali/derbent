package tech.derbent.api.ui.component.filter;

import java.lang.reflect.Modifier;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import tech.derbent.api.interfaces.ISprintableItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CComboBox;

/** CEntityTypeFilter - Universal entity type selector component.
 * <p>
 * THE STANDARD component for entity type selection/filtering throughout the application. Automatically discovers entity types and presents them with
 * human-friendly names from CEntityRegistry.
 * </p>
 * <p>
 * <b>Two Modes:</b>
 * <ul>
 * <li><b>Filter Mode</b> (default): Includes "All types" option for filtering - use in toolbars</li>
 * <li><b>Selection Mode</b>: No "All types", required selection - use in dialogs/forms</li>
 * </ul>
 * </p>
 * <p>
 * <b>Usage Examples:</b>
 *
 * <pre>
 * // Filter mode (with "All types")
 * CEntityTypeFilter filter = new CEntityTypeFilter();
 * filter.setAvailableEntityTypes(sprintItems);
 * toolbar.addFilterComponent(filter);
 * // Selection mode (required selection)
 * CEntityTypeFilter selector = new CEntityTypeFilter(false);
 * selector.setLabel("Entity Type");
 * selector.setRequired(true);
 * selector.setAvailableEntityClasses(List.of(CActivity.class, CMeeting.class));
 * </pre>
 * </p>
 */
public class CEntityTypeFilter extends CAbstractFilterComponent<Class<?>> {

	/** Internal class to represent type options with label and class. */
	private record TypeOption(String label, Class<?> entityClass) {

		public String getLabel() { return label; }

		@Override
		public String toString() {
			// Return class name for persistence, or "AllTypes" for the "All types" option
			return entityClass != null ? entityClass.getName() : "AllTypes";
		}
	}

	private static final String ALL_TYPES_LABEL = "All types";
	public static final String FILTER_KEY = "entityType";
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityTypeFilter.class);

	/** Resolves a display label for an entity class.
	 * <p>
	 * Tries to get the registered entity title from CEntityRegistry first. Falls back to simple class name if not registered.
	 * </p>
	 * <p>
	 * <strong>FAIL-FAST</strong>: Throws IllegalArgumentException if entityClass is null.
	 * </p>
	 * @param entityClass The entity class (must not be null)
	 * @return The display label (from entity registry or simple name)
	 * @throws IllegalArgumentException if entityClass is null */
	private static String resolveEntityTypeLabel(final Class<?> entityClass) {
		Objects.requireNonNull(entityClass, "Entity class cannot be null for label resolution");
		final String registeredTitle = CEntityRegistry.getEntityTitleSingular(entityClass);
		if (registeredTitle != null && !registeredTitle.isBlank()) {
			return registeredTitle;
		}
		return entityClass.getSimpleName();
	}

	private final TypeOption allTypesOption;
	private final CComboBox<TypeOption> comboBox;
	private final boolean includeAllTypesOption;
	private boolean suppressEvents = false;

	/** Creates an entity type filter in FILTER MODE (with "All types" option).
	 * <p>
	 * The filter is initialized with "All types" option by default. Call {@link #setAvailableEntityTypes(List)} to populate with actual entity types.
	 * </p>
	 */
	public CEntityTypeFilter() {
		this(true);
	}

	/** Creates an entity type filter with configurable mode.
	 * @param includeAllTypesOption If true, creates FILTER MODE with "All types" option. If false, creates SELECTION MODE (required selection). */
	public CEntityTypeFilter(final boolean includeAllTypesOption) {
		super(FILTER_KEY);
		this.includeAllTypesOption = includeAllTypesOption;
		allTypesOption = includeAllTypesOption ? new TypeOption(ALL_TYPES_LABEL, null) : null;
		comboBox = new CComboBox<>("Type");
		// Compact layout for board header toolbars (align with sprint planning).
		comboBox.setWidth("160px");
		comboBox.getStyle().set("min-width", "0");
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setItemLabelGenerator(TypeOption::getLabel);
		if (includeAllTypesOption) {
			comboBox.setItems(allTypesOption);
			comboBox.setValue(allTypesOption);
		}
		// Enable automatic persistence in CComboBox
		comboBox.enablePersistence("entityTypeFilter_" + FILTER_KEY, className -> {
			// Find matching TypeOption by entity class name
			if (className == null || className.isBlank()) {
				return null;
			}
			return comboBox.getListDataView().getItems().filter(option -> {
				if (option.entityClass() == null) {
					return "AllTypes".equals(className);
				}
				return option.entityClass().getName().equals(className);
			}).findFirst().orElse(null);
		});
		// Notify listeners on value change
		comboBox.addValueChangeListener(event -> {
			if (!suppressEvents) {
				final TypeOption option = event.getValue();
				notifyChangeListeners(option != null ? option.entityClass() : null);
			}
		});
	}

	@Override
	public void clearFilter() {
		if (includeAllTypesOption && allTypesOption != null) {
			comboBox.setValue(allTypesOption);
		} else {
			comboBox.clear();
		}
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	/** Gets the underlying ComboBox component for direct access if needed.
	 * @return The ComboBox component */
	public CComboBox<TypeOption> getComboBox() { return comboBox; }

	/** Gets the currently selected entity class.
	 * @return The selected entity class, or null if "All types" is selected or nothing is selected */
	public Class<?> getSelectedEntityClass() {
		final TypeOption option = comboBox.getValue();
		return option != null ? option.entityClass() : null;
	}

	/** Sets the available entity types from a list of entity classes.
	 * <p>
	 * Use this method in SELECTION MODE when you want to specify exact entity types to show.
	 * </p>
	 * <p>
	 * <strong>Example:</strong>
	 *
	 * <pre>
	 * filter.setAvailableEntityClasses(List.of(CActivity.class, CMeeting.class));
	 * </pre>
	 * </p>
	 * @param entityClasses List of entity classes to display (must not be null) */
	public void setAvailableEntityClasses(final List<Class<?>> entityClasses) {
		Objects.requireNonNull(entityClasses, "Entity classes list cannot be null");
		final Map<Class<?>, TypeOption> options = new LinkedHashMap<>();
		for (final Class<?> entityClass : entityClasses) {
			if (entityClass != null) {
				options.put(entityClass, new TypeOption(resolveEntityTypeLabel(entityClass), entityClass));
			}
		}
		updateTypeOptionsFromMap(options);
	}

	/** Sets the available entity types based on a list of entities.
	 * <p>
	 * This method automatically discovers all unique entity types from the provided list and populates the filter dropdown with:
	 * <ul>
	 * <li>"All types" option (to show everything)</li>
	 * <li>Individual entity type options (Activity, Meeting, Sprint, etc.)</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>FAIL-FAST</strong>: Throws IllegalArgumentException if items list is null.
	 * </p>
	 * @param items List of entities to analyze for types (must not be null)
	 * @throws IllegalArgumentException if items is null */
	public void setAvailableEntityTypes(final List<?> items) {
		Objects.requireNonNull(items, "Items list cannot be null for entity type discovery");
		updateTypeOptions(items);
	}

	/** Sets the label of the combobox.
	 * @param label The label to display */
	public void setLabel(final String label) {
		comboBox.setLabel(label);
	}

	/** Sets whether this field is required.
	 * @param required If true, the field must have a value */
	public void setRequired(final boolean required) {
		comboBox.setRequired(required);
	}

	/** Sets the width of the combobox.
	 * @param width The width (e.g., "150px", "100%") */
	public void setWidth(final String width) {
		comboBox.setWidth(width);
	}

	@Override
	protected void updateComponentValue(final Class<?> value) {
		if (value == null) {
			comboBox.setValue(allTypesOption);
		} else {
			// Find the matching type option
			comboBox.getListDataView().getItems().filter(option -> value.equals(option.entityClass())).findFirst().ifPresent(comboBox::setValue);
		}
	}

	/** Refreshes the type options list from the provided items.
	 * Also ensures all registered ISprintableItem types are always included so sprint-capable types remain filterable
	 * even when the current item list is empty or doesn't contain all types.
	 * @param items List of entities to analyze */
	private void updateTypeOptions(final List<?> items) {
		final Map<Class<?>, TypeOption> options = new LinkedHashMap<>();
		for (final Object item : items) {
			if (item == null) {
				continue;
			}
			final Class<?> entityClass = item.getClass();
			options.putIfAbsent(entityClass, new TypeOption(resolveEntityTypeLabel(entityClass), entityClass));
		}
		// Always include all registered concrete ISprintableItem types (Activity, Meeting, Issue, Epic, Feature, UserStory, …)
		// so filter options remain stable as items are added/removed from the view.
		for (final String key : CEntityRegistry.getAllRegisteredEntityKeys()) {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(key);
			if (entityClass != null && !entityClass.isInterface() && !Modifier.isAbstract(entityClass.getModifiers())
					&& ISprintableItem.class.isAssignableFrom(entityClass)) {
				options.putIfAbsent(entityClass, new TypeOption(resolveEntityTypeLabel(entityClass), entityClass));
			}
		}
		updateTypeOptionsFromMap(options);
	}

	/** Updates the combobox with type options from a map.
	 * @param options Map of entity class to TypeOption */
	private void updateTypeOptionsFromMap(final Map<Class<?>, TypeOption> options) {
		final List<TypeOption> typeOptions =
				options.values().stream().sorted(Comparator.comparing(option -> option.getLabel().toLowerCase())).collect(Collectors.toList());
		if (includeAllTypesOption && allTypesOption != null) {
			typeOptions.add(0, allTypesOption);
		}
		final TypeOption oldValue = comboBox.getValue();
		suppressEvents = true;
		try {
			comboBox.setItems(typeOptions);
			if (oldValue != null && !typeOptions.contains(oldValue)) {
				comboBox.clear();
			}
			if (comboBox.getValue() == null) {
				if (includeAllTypesOption && allTypesOption != null) {
					comboBox.setValue(allTypesOption);
				} else if (!typeOptions.isEmpty()) {
					comboBox.setValue(typeOptions.get(0));
				}
			}
		} finally {
			suppressEvents = false;
		}
		final TypeOption finalValue = comboBox.getValue();
		notifyChangeListeners(finalValue != null ? finalValue.entityClass() : null);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		// Persistence is now handled automatically by CComboBox.enablePersistence()
		// This method remains for interface compatibility but does nothing
		LOGGER.debug("[FilterPersistence] enableValuePersistence called with storageId: {} (CComboBox handles persistence)", storageId);
	}
}
