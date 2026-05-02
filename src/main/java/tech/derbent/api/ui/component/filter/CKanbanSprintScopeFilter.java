package tech.derbent.api.ui.component.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBoxVariant;

import tech.derbent.api.ui.component.basic.CComboBox;
import tech.derbent.api.utils.CValueStorageHelper;
import tech.derbent.plm.sprints.domain.CSprint;

/** CKanbanSprintScopeFilter — single combo that controls the kanban dataset scope.
 * <p>
 * Order: Status only → All sprints → sprints (newest first as provided by caller).
 * </p> */
public class CKanbanSprintScopeFilter extends CAbstractFilterComponent<CKanbanSprintScopeFilter.CSprintScope> {

	public static final String FILTER_KEY = "kanbanSprintScope";

	public enum EScopeMode {
		STATUS_ONLY,
		ALL_SPRINTS,
		SPRINT
	}

	public record CSprintScope(EScopeMode mode, CSprint sprint) {
		public static CSprintScope statusOnly() { return new CSprintScope(EScopeMode.STATUS_ONLY, null); }

		public static CSprintScope allSprints() { return new CSprintScope(EScopeMode.ALL_SPRINTS, null); }

		public static CSprintScope sprint(final CSprint sprint) { return new CSprintScope(EScopeMode.SPRINT, sprint); }

		public String getLabel() {
			return switch (mode) {
			case STATUS_ONLY -> "Status only";
			case ALL_SPRINTS -> "All sprints";
			case SPRINT -> sprint != null ? sprint.getName() : "Sprint";
			};
		}
	}

	private final CComboBox<CSprintScope> comboBox;
	private List<CSprint> availableSprints = List.of();

	public CKanbanSprintScopeFilter() {
		super(FILTER_KEY);
		comboBox = new CComboBox<>("Sprint");
		comboBox.setWidth("180px");
		comboBox.getStyle().set("min-width", "0");
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setAllowCustomValue(false);
		comboBox.setItemLabelGenerator(scope -> scope != null ? scope.getLabel() : "All sprints");
		comboBox.addValueChangeListener(event -> notifyChangeListeners(event.getValue()));
	}

	public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
		Objects.requireNonNull(sprints, "Sprints list cannot be null");
		availableSprints = List.copyOf(sprints);
		final List<CSprintScope> options = new ArrayList<>();
		options.add(CSprintScope.statusOnly());
		options.add(CSprintScope.allSprints());
		for (final CSprint sprint : availableSprints) {
			options.add(CSprintScope.sprint(sprint));
		}
		comboBox.setItems(options);

		final CSprintScope current = comboBox.getValue();
		if (current != null && current.mode() == EScopeMode.SPRINT && current.sprint() != null
				&& availableSprints.stream().noneMatch(s -> s != null && s.getId() != null && s.getId().equals(current.sprint().getId()))) {
			comboBox.setValue(CSprintScope.allSprints());
			return;
		}

		if (current == null) {
			if (defaultSprint != null && defaultSprint.getId() != null) {
				comboBox.setValue(CSprintScope.sprint(defaultSprint));
			} else {
				comboBox.setValue(CSprintScope.allSprints());
			}
		}
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(CSprintScope.allSprints());
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	@Override
	protected void updateComponentValue(final CSprintScope value) {
		comboBox.setValue(value);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		CValueStorageHelper.valuePersist_enable(comboBox, storageId + "_" + FILTER_KEY,
				value -> {
					if (value == null) {
						return "ALL";
					}
					return switch (value.mode()) {
					case STATUS_ONLY -> "STATUS_ONLY";
					case ALL_SPRINTS -> "ALL";
					case SPRINT -> value.sprint() != null && value.sprint().getId() != null ? "SPRINT:" + value.sprint().getId() : "ALL";
					};
				},
				stored -> {
					if (stored == null || stored.isBlank() || "ALL".equalsIgnoreCase(stored)) {
						return CSprintScope.allSprints();
					}
					if ("STATUS_ONLY".equalsIgnoreCase(stored)) {
						return CSprintScope.statusOnly();
					}
					if (stored.startsWith("SPRINT:")) {
						final String idStr = stored.substring("SPRINT:".length());
						try {
							final Long sprintId = Long.parseLong(idStr);
							return comboBox.getListDataView().getItems()
									.filter(o -> o != null && o.mode() == EScopeMode.SPRINT && o.sprint() != null && sprintId.equals(o.sprint().getId()))
									.findFirst().orElse(CSprintScope.allSprints());
						} catch (final NumberFormatException e) {
							return CSprintScope.allSprints();
						}
					}
					return CSprintScope.allSprints();
				});
	}
}
