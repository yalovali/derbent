package tech.derbent.api.ui.component.filter;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CComboBox;

/** CEntityTypeFilter - Dynamic entity type filter component.
 * <p>
 * Automatically discovers entity types from provided items and presents them as filter options. Includes an "All types" option to show all entities.
 * </p>
 * <p>
 * This filter solves the problem of missing entity types (like Meeting) in kanban boards by dynamically discovering all available types from the
 * actual data.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 *
 * <pre>
 * CEntityTypeFilter filter = new CEntityTypeFilter();
 * filter.setAvailableEntityTypes(sprintItems); // Auto-discovers Activity, Meeting, Sprint, etc.
 * toolbar.addFilterComponent(filter);
 * </pre>
 * </p>
 */
public class CEntityTypeFilter extends CAbstractFilterComponent<Class<?>> {

	/** Internal class to represent type options with label and class. */
	private static class TypeOption {

		private final Class<?> entityClass;
		private final String label;

		TypeOption(final String label, final Class<?> entityClass) {
			this.label = label;
			this.entityClass = entityClass;
		}

		@Override
		public boolean equals(final Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof TypeOption)) {
				return false;
			}
			final TypeOption option = (TypeOption) other;
			return Objects.equals(entityClass, option.entityClass) && Objects.equals(label, option.label);
		}

		public Class<?> getEntityClass() { return entityClass; }

		public String getLabel() { return label; }

		@Override
		public int hashCode() {
			return Objects.hash(entityClass, label);
		}

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

	/** Creates an entity type filter.
	 * <p>
	 * The filter is initialized with "All types" option by default. Call {@link #setAvailableEntityTypes(List)} to populate with actual entity types.
	 * </p>
	 */
	public CEntityTypeFilter() {
		super(FILTER_KEY);
		allTypesOption = new TypeOption(ALL_TYPES_LABEL, null);
		comboBox = new CComboBox<>("Type");
		comboBox.setItemLabelGenerator(TypeOption::getLabel);
		comboBox.setItems(allTypesOption);
		comboBox.setValue(allTypesOption);
		// Enable automatic persistence in CComboBox
		comboBox.enablePersistence("entityTypeFilter_" + FILTER_KEY, className -> {
			// Find matching TypeOption by entity class name
			if (className == null || className.isBlank()) {
				return null;
			}
			return comboBox.getListDataView().getItems().filter(option -> {
				if (option.getEntityClass() == null) {
					return "AllTypes".equals(className);
				}
				return option.getEntityClass().getName().equals(className);
			}).findFirst().orElse(null);
		});
		// Notify listeners on value change
		comboBox.addValueChangeListener(event -> {
			final TypeOption option = event.getValue() != null ? event.getValue() : allTypesOption;
			notifyChangeListeners(option.getEntityClass());
		});
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(allTypesOption);
	}

	@Override
	protected Component createComponent() {
		return comboBox;
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

	@Override
	protected void updateComponentValue(final Class<?> value) {
		if (value == null) {
			comboBox.setValue(allTypesOption);
		} else {
			// Find the matching type option
			comboBox.getListDataView().getItems().filter(option -> value.equals(option.getEntityClass())).findFirst().ifPresent(comboBox::setValue);
		}
	}

	/** Refreshes the type options list from the provided items. Also ensures Activity and Meeting types are always included.
	 * @param items List of entities to analyze */
	private void updateTypeOptions(final List<?> items) {
		// Discover unique entity types from items
		final Map<Class<?>, TypeOption> options = new LinkedHashMap<>();
		for (final Object item : items) {
			if (item == null) {
				continue;
			}
			final Class<?> entityClass = item.getClass();
			options.putIfAbsent(entityClass, new TypeOption(resolveEntityTypeLabel(entityClass), entityClass));
		}
		// CRITICAL: Always include Activity and Meeting types even if not present in items
		// This ensures these core types are always available in kanban board filters
		try {
			final Class<?> activityClass = Class.forName("tech.derbent.app.activities.domain.CActivity");
			if (!options.containsKey(activityClass)) {
				options.put(activityClass, new TypeOption(resolveEntityTypeLabel(activityClass), activityClass));
			}
		} catch (@SuppressWarnings ("unused") final ClassNotFoundException e) {
			// Activity class not available - skip
		}
		try {
			final Class<?> meetingClass = Class.forName("tech.derbent.app.meetings.domain.CMeeting");
			if (!options.containsKey(meetingClass)) {
				options.put(meetingClass, new TypeOption(resolveEntityTypeLabel(meetingClass), meetingClass));
			}
		} catch (@SuppressWarnings ("unused") final ClassNotFoundException e) {
			// Meeting class not available - skip
		}
		// Build sorted list with "All types" first
		final List<TypeOption> typeOptions =
				options.values().stream().sorted(Comparator.comparing(option -> option.getLabel().toLowerCase())).collect(Collectors.toList());
		typeOptions.add(0, allTypesOption);
		// Capture current value BEFORE setItems() to check if it's still valid afterwards
		final TypeOption oldValue = comboBox.getValue();
		// Update ComboBox items (this clears the current value temporarily)
		comboBox.setItems(typeOptions);
		// Check if old value is still valid in new options - if not, clear and select default
		// Value persistence will restore the saved value automatically if it's still in the list
		if (oldValue != null && !typeOptions.contains(oldValue)) {
			// Old value no longer valid - clear it
			comboBox.clear();
			notifyChangeListeners(null);
		}
		// If no value is currently set, select "All types" as default
		if (comboBox.getValue() == null) {
			comboBox.setValue(allTypesOption);
			notifyChangeListeners(null);
		}
		// Value persistence will restore saved value after this method completes
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		// Persistence is now handled automatically by CComboBox.enablePersistence()
		// This method remains for interface compatibility but does nothing
		LOGGER.debug("[FilterPersistence] enableValuePersistence called with storageId: {} (CComboBox handles persistence)", storageId);
	}
}
