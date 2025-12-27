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
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.ui.component.enhanced.CComponentFilterToolbar;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprint;

/** CComponentKanbanBoardFilterToolbar - Filtering toolbar for Kanban board items.
 * <p>
 * Supports filtering by responsible user and entity type.
 * </p>
 */
public class CComponentKanbanBoardFilterToolbar extends CComponentFilterToolbar {

	public static class FilterCriteria {

                private Class<?> entityType;
                private ResponsibleFilterMode responsibleMode = ResponsibleFilterMode.ALL;
                private CSprint sprint;

                public Class<?> getEntityType() { return entityType; }

                public ResponsibleFilterMode getResponsibleMode() { return responsibleMode; }

                public CSprint getSprint() { return sprint; }

                public void setEntityType(final Class<?> entityType) { this.entityType = entityType; }

                public void setResponsibleMode(final ResponsibleFilterMode responsibleMode) { this.responsibleMode = responsibleMode; }

                public void setSprint(final CSprint sprint) { this.sprint = sprint; }
        }

	public enum ResponsibleFilterMode {

		ALL("All items"), CURRENT_USER("My items");

		private final String label;

		ResponsibleFilterMode(final String label) {
			this.label = label;
		}

		public String getLabel() { return label; }
	}

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
	}

	private static final long serialVersionUID = 1L;

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

	public void addKanbanFilterChangeListener(final Consumer<FilterCriteria> listener) {
		Check.notNull(listener, "Filter listener cannot be null");
		listeners.add(listener);
	}

        private Button buildClearButton() {
                final Button button = new Button("Clear", VaadinIcon.CLOSE_SMALL.create());
                button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
                button.addClickListener(event -> clearFilters());
		button.setTooltipText("Clear Kanban filters");
		return button;
	}

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

        private CColorAwareComboBox<CSprint> buildSprintCombo() {
                final CColorAwareComboBox<CSprint> combo = new CColorAwareComboBox<>(CSprint.class, "Sprint");
                combo.addValueChangeListener(event -> {
                        currentCriteria.setSprint(event.getValue());
                        notifyListeners();
                });
                return combo;
        }

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

        public FilterCriteria getCurrentCriteria() { return currentCriteria; }

	private void notifyListeners() {
		for (final Consumer<FilterCriteria> listener : listeners) {
			listener.accept(currentCriteria);
		}
	}

        public void setAvailableItems(final List<CProjectItem<?>> items) {
                Check.notNull(items, "Items cannot be null");
                updateTypeOptions(items);
        }

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

	private void updateTypeOptions(final List<CProjectItem<?>> items) {
		final Map<Class<?>, TypeOption> options = new LinkedHashMap<>();
		for (final CProjectItem<?> item : items) {
			if (item == null) {
				continue;
			}
			final Class<?> entityClass = item.getClass();
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
}
