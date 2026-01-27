package tech.derbent.api.ui.component.filter;

import java.util.List;
import java.util.Objects;
import com.vaadin.flow.component.Component;
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
		if (defaultSprint != null && sprints.contains(defaultSprint)) {
			comboBox.setValue(defaultSprint);
			notifyChangeListeners(defaultSprint);
		} else if (comboBox.getValue() != null && !sprints.contains(comboBox.getValue())) {
			comboBox.clear();
			notifyChangeListeners(null);
		}
		if (comboBox.getValue() == null && !sprints.isEmpty()) {
			comboBox.setValue(sprints.get(0));
			notifyChangeListeners(sprints.get(0));
		}
	}

	@Override
	protected void updateComponentValue(final CSprint value) {
		comboBox.setValue(value);
	}

	@Override
	public void valuePersist_enable(final String storageId) {
		// Enable persistence for Sprint ComboBox
		CValueStorageHelper.valuePersist_enable(comboBox, storageId + "_" + FILTER_KEY, sprint -> {
			// Converter: find sprint by ID
			if (sprint == null || sprint.isBlank()) {
				return null;
			}
			try {
				final Long sprintId = Long.parseLong(sprint);
				return comboBox.getListDataView().getItems().filter(s -> s.getId() != null && s.getId().equals(sprintId)).findFirst().orElse(null);
			} catch (final NumberFormatException e) {
				return null;
			}
		});
	}
}
