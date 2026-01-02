package tech.derbent.app.kanban.kanbanline.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.VaadinIcon;
import tech.derbent.api.interfaces.IHasSelectedValueStorage;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.enhanced.CComponentFilterToolbar;
import tech.derbent.api.utils.CValueStorageHelper;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprint;
import tech.derbent.app.sprints.domain.CSprintItem;

/** CComponentKanbanBoardFilterToolbar - Filtering toolbar for Kanban board items.
 * <p>
 * Supports filtering by responsible user and entity type.
 * </p>
 */
public class CComponentKanbanBoardFilterToolbar extends CComponentFilterToolbar implements IHasSelectedValueStorage {

	public static class FilterCriteria {

                private Class<?> entityType;
                private ResponsibleFilterMode responsibleMode = ResponsibleFilterMode.ALL;
                private CSprint sprint;

		/** Returns the selected entity type filter. */
                public Class<?> getEntityType() { return entityType; }

		/** Returns the selected responsible filter mode. */
                public ResponsibleFilterMode getResponsibleMode() { return responsibleMode; }

		/** Returns the selected sprint filter. */
                public CSprint getSprint() { return sprint; }

		/** Sets the entity type filter. */
                public void setEntityType(final Class<?> entityType) { this.entityType = entityType; }

		/** Sets the responsible filter mode. */
                public void setResponsibleMode(final ResponsibleFilterMode responsibleMode) { this.responsibleMode = responsibleMode; }

		/** Sets the sprint filter. */
                public void setSprint(final CSprint sprint) { this.sprint = sprint; }
        }

	public enum ResponsibleFilterMode {

		ALL("All items"), CURRENT_USER("My items");

		private final String label;

		/** Creates a responsible filter entry with a label. */
		ResponsibleFilterMode(final String label) {
			this.label = label;
		}

		/** Returns the display label for the mode. */
		public String getLabel() { return label; }
	}

	private static class TypeOption {

		private final Class<?> entityClass;
		private final String label;

		/** Creates a type option with label and class. */
		TypeOption(final String label, final Class<?> entityClass) {
			this.label = label;
			this.entityClass = entityClass;
		}

		/** Compares type options by class and label. */
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

		/** Returns the entity class for this option. */
		public Class<?> getEntityClass() { return entityClass; }

		/** Returns the display label for this option. */
		public String getLabel() { return label; }

		/** Computes hash based on class and label. */
		@Override
		public int hashCode() {
			return Objects.hash(entityClass, label);
		}
	}

	private static final long serialVersionUID = 1L;

	/** Resolves a display label for an entity class. */
	private static String resolveEntityTypeLabel(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		final String registeredTitle = CEntityRegistry.getEntityTitleSingular(entityClass);
		if (registeredTitle != null && !registeredTitle.isBlank()) {
			return registeredTitle;
		}
		return entityClass.getSimpleName();
	}

        private final Button clearButton;
        private final FilterCriteria currentCriteria;
        private final ComboBox<ResponsibleFilterMode> comboResponsibleMode;
        private final CColorAwareComboBox<CSprint> comboSprint;
        private final ComboBox<TypeOption> comboType;
        private CSprint defaultSprint;
        private final List<Consumer<FilterCriteria>> listeners;
        private final TypeOption typeAllOption;

	/** Builds the filter toolbar and its components. */
        public CComponentKanbanBoardFilterToolbar() {
                super(new ToolbarConfig().hideAll());
                currentCriteria = new FilterCriteria();
                listeners = new ArrayList<>();
                typeAllOption = new TypeOption("All types", null);
                comboSprint = buildSprintCombo();
                comboResponsibleMode = buildResponsibleModeCombo();
                comboType = buildTypeCombo();
                clearButton = buildClearButton();
                addFilterComponents(comboSprint, comboResponsibleMode, comboType, clearButton);
                setAlignItems(Alignment.CENTER);
        }

	/** Registers a listener that reacts to filter changes. */
	public void addKanbanFilterChangeListener(final Consumer<FilterCriteria> listener) {
		Check.notNull(listener, "Filter listener cannot be null");
		listeners.add(listener);
	}

	/** Builds the clear button for the toolbar. */
        private Button buildClearButton() {
                final Button button = new Button("Clear", VaadinIcon.CLOSE_SMALL.create());
                button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                button.addClickListener(event -> clearFilters());
		button.setTooltipText("Clear Kanban filters");
		return button;
	}

	/** Builds the responsible filter combo box. */
        private ComboBox<ResponsibleFilterMode> buildResponsibleModeCombo() {
                final ComboBox<ResponsibleFilterMode> combo = new ComboBox<>("Responsible");
		combo.setItems(ResponsibleFilterMode.values());
		combo.setItemLabelGenerator(ResponsibleFilterMode::getLabel);
		combo.setValue(ResponsibleFilterMode.ALL);
		combo.addValueChangeListener(event -> {
			final ResponsibleFilterMode value = event.getValue() != null ? event.getValue() : ResponsibleFilterMode.ALL;
			currentCriteria.setResponsibleMode(value);
			notifyListeners();
		});
                return combo;
        }

	/** Builds the sprint filter combo box. */
        private CColorAwareComboBox<CSprint> buildSprintCombo() {
                final CColorAwareComboBox<CSprint> combo = new CColorAwareComboBox<>(CSprint.class, "Sprint");
                combo.addValueChangeListener(event -> {
                        currentCriteria.setSprint(event.getValue());
                        notifyListeners();
                });
                return combo;
        }

	/** Builds the entity type filter combo box. */
	private ComboBox<TypeOption> buildTypeCombo() {
		final ComboBox<TypeOption> combo = new ComboBox<>("Type");
		combo.setItemLabelGenerator(TypeOption::getLabel);
		combo.setItems(typeAllOption);
		combo.setValue(typeAllOption);
		combo.addValueChangeListener(event -> {
			final TypeOption option = event.getValue() != null ? event.getValue() : typeAllOption;
			currentCriteria.setEntityType(option.getEntityClass());
			notifyListeners();
		});
		return combo;
	}

	/** Resets filters to defaults and notifies listeners. */
        @Override
        public void clearFilters() {
                comboSprint.setValue(defaultSprint);
                currentCriteria.setSprint(defaultSprint);
                comboResponsibleMode.setValue(ResponsibleFilterMode.ALL);
                comboType.setValue(typeAllOption);
                currentCriteria.setResponsibleMode(ResponsibleFilterMode.ALL);
                currentCriteria.setEntityType(null);
                notifyListeners();
        }

	/** Returns the active filter criteria. */
        public FilterCriteria getCurrentCriteria() { return currentCriteria; }

	/** Notifies listeners of a filter change. */
	private void notifyListeners() {
		for (final Consumer<FilterCriteria> listener : listeners) {
			listener.accept(currentCriteria);
		}
	}

	/** Updates available type options based on item list. */
        public void setAvailableItems(final List<CSprintItem> items) {
                Check.notNull(items, "Items cannot be null");
                updateTypeOptions(items);
        }

	/** Updates sprint options and selects a default. */
        public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
                comboSprint.setItems(sprints);
                this.defaultSprint = defaultSprint;
                if (defaultSprint != null && sprints.contains(defaultSprint)) {
                        comboSprint.setValue(defaultSprint);
                        currentCriteria.setSprint(defaultSprint);
                } else if (comboSprint.getValue() != null && !sprints.contains(comboSprint.getValue())) {
                        comboSprint.clear();
                        currentCriteria.setSprint(null);
                }
                if (comboSprint.getValue() == null && !sprints.isEmpty()) {
                        comboSprint.setValue(sprints.get(0));
                        currentCriteria.setSprint(sprints.get(0));
                }
                notifyListeners();
        }

	/** Refreshes the type options list. */
	private void updateTypeOptions(final List<CSprintItem> items) {
		final Map<Class<?>, TypeOption> options = new LinkedHashMap<>();
		for (final CSprintItem sprintItem : items) {
			if (sprintItem == null || sprintItem.getItem() == null) {
				continue;
			}
			final Class<?> entityClass = sprintItem.getItem().getClass();
			options.putIfAbsent(entityClass, new TypeOption(resolveEntityTypeLabel(entityClass), entityClass));
		}
		final List<TypeOption> typeOptions =
				options.values().stream().sorted(Comparator.comparing(option -> option.getLabel().toLowerCase())).collect(Collectors.toList());
		typeOptions.add(0, typeAllOption);
		comboType.setItems(typeOptions);
		if (comboType.getValue() != null && !typeOptions.contains(comboType.getValue())) {
			comboType.setValue(typeAllOption);
			currentCriteria.setEntityType(null);
		}
	}

	// ==================== IHasSelectedValueStorage Implementation ====================

	/**
	 * Enables automatic value persistence for all filter ComboBoxes.
	 * <p>
	 * This method should be called by the parent component (kanban board) to enable
	 * automatic saving and restoring of filter selections:
	 * <ul>
	 * <li>Sprint filter</li>
	 * <li>Type filter</li>
	 * <li>Responsible mode filter</li>
	 * </ul>
	 * </p>
	 */
	public void enableValuePersistence() {
		// Enable persistence for Sprint ComboBox
		CValueStorageHelper.enableAutoPersistence(comboSprint, getStorageId() + "_sprint", sprint -> {
			// Converter: find sprint by ID
			if (sprint == null || sprint.isBlank()) {
				return null;
			}
			try {
				final Long sprintId = Long.parseLong(sprint);
				return comboSprint.getListDataView().getItems()
						.filter(s -> s.getId() != null && s.getId().equals(sprintId))
						.findFirst()
						.orElse(null);
			} catch (final NumberFormatException e) {
				return null;
			}
		});

		// Enable persistence for Type ComboBox
		CValueStorageHelper.enableAutoPersistence(comboType, getStorageId() + "_type", label -> {
			// Converter: find TypeOption by label
			return comboType.getListDataView().getItems()
					.filter(option -> option.getLabel().equals(label))
					.findFirst()
					.orElse(null);
		});

		// Enable persistence for Responsible Mode ComboBox
		CValueStorageHelper.enableAutoPersistence(comboResponsibleMode, getStorageId() + "_responsible", modeName -> {
			// Converter: find ResponsibleFilterMode by name
			try {
				return ResponsibleFilterMode.valueOf(modeName);
			} catch (final IllegalArgumentException e) {
				return ResponsibleFilterMode.ALL;
			}
		});
	}

	@Override
	public String getStorageId() {
		return "kanbanBoardFilter_" + getId().orElse(generateId());
	}

	/**
	 * Generates a unique ID for this component instance.
	 */
	private String generateId() {
		return getClass().getSimpleName() + "_" + System.identityHashCode(this);
	}

	@Override
	public void restoreCurrentValue() {
		// Restoration is handled automatically by CValueStorageHelper when components attach
		// This method is here for interface compliance
	}

	@Override
	public void saveCurrentValue() {
		// Saving is handled automatically by CValueStorageHelper on value changes
		// This method is here for interface compliance
	}
}
