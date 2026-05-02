package tech.derbent.api.ui.component.filter;

import java.util.List;
import java.util.Objects;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBoxVariant;
import tech.derbent.api.ui.component.basic.CColorAwareComboBox;
import tech.derbent.api.utils.CValueStorageHelper;
import tech.derbent.plm.sprints.domain.CSprint;

/** CSprintFilter - Sprint selection filter component.
 * <p>
 * Allows filtering by sprint with color-aware dropdown display.
 * </p>
 */
public class CSprintFilter extends CAbstractFilterComponent<CSprint> {

	public static final String FILTER_KEY = "sprint";
	private final CColorAwareComboBox<CSprint> comboBox;
	private CSprint defaultSprint;

	/** Creates a sprint filter.
	 * <p>
	 * The filter is initialized empty. Call {@link #setAvailableSprints(List, CSprint)} to populate with actual sprints.
	 * </p>
	 */
	public CSprintFilter() {
		super(FILTER_KEY);
		comboBox = new CColorAwareComboBox<>(CSprint.class, "Sprint");
		// Keep the sprint filter compact; sprint planning header is our size baseline.
		comboBox.setWidth("160px");
		comboBox.getStyle().set("min-width", "0");
		comboBox.addThemeVariants(ComboBoxVariant.LUMO_SMALL);
		comboBox.setClearButtonVisible(true);
		comboBox.setPlaceholder("All sprints");
		comboBox.addValueChangeListener(event -> notifyChangeListeners(event.getValue()));
	}

	@Override
	public void clearFilter() {
		comboBox.setValue(defaultSprint);
	}

	@Override
	protected Component createComponent() {
		return comboBox;
	}

	/** Sets the available sprints and default sprint.
	 * <p>
	 * <strong>FAIL-FAST</strong>: Throws IllegalArgumentException if sprints list is null.
	 * </p>
	 * @param sprints       Available sprints (must not be null, can be empty)
	 * @param defaultSprint Default sprint to select (can be null)
	 * @throws IllegalArgumentException if sprints is null */
	public void setAvailableSprints(final List<CSprint> sprints, final CSprint defaultSprint) {
		Objects.requireNonNull(sprints, "Sprints list cannot be null");
		comboBox.setItems(sprints);
		this.defaultSprint = defaultSprint;
		if (comboBox.getValue() != null && !sprints.contains(comboBox.getValue())) {
			comboBox.clear();
			notifyChangeListeners(null);
		}
		// Keep null as a valid "All sprints" selection; do not auto-select a sprint programmatically.


	}

	/** Shows or hides this filter based on the current board mode. */
	public void setVisible(final boolean visible) {
		comboBox.setVisible(visible);
	}

	@Override
	protected void updateComponentValue(final CSprint value) {
		comboBox.setValue(value);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		CValueStorageHelper.valuePersist_enable(comboBox, storageId + "_" + FILTER_KEY,
				value -> value != null && value.getId() != null ? value.getId().toString() : "ALL",
				stored -> {
					if (stored == null || stored.isBlank() || "ALL".equalsIgnoreCase(stored)) {
						return null;
					}
					try {
						final Long sprintId = Long.parseLong(stored);
						return comboBox.getListDataView().getItems()
								.filter(s -> s != null && s.getId() != null && s.getId().equals(sprintId))
								.findFirst().orElse(null);
					} catch (final NumberFormatException e) {
						return null;
					}
				});
	}
}
