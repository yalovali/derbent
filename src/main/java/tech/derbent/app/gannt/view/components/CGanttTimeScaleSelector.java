package tech.derbent.app.gannt.view.components;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

/** CGanttTimeScaleSelector - Component for selecting the time scale for Gantt timeline display. Allows users to switch between Day, Week, Month, and
 * Year views. */
public class CGanttTimeScaleSelector extends HorizontalLayout {

	public enum TimeScale {

		DAY("Day", 1), WEEK("Week", 7), MONTH("Month", 30), YEAR("Year", 365);

		private final int days;
		private final String label;

		TimeScale(final String label, final int days) {
			this.label = label;
			this.days = days;
		}

		public int getDays() { return days; }

		public String getLabel() { return label; }

		@Override
		public String toString() {
			return label;
		}
	}

	private static final long serialVersionUID = 1L;
	private final ComboBox<TimeScale> scaleSelector;

	public CGanttTimeScaleSelector() {
		setSpacing(true);
		setPadding(false);
		setAlignItems(Alignment.CENTER);
		// Label
		final Span label = new Span("Time Scale:");
		label.getStyle().set("font-weight", "500");
		label.getStyle().set("margin-right", "8px");
		// Selector
		scaleSelector = new ComboBox<>();
		scaleSelector.setItems(TimeScale.values());
		scaleSelector.setValue(TimeScale.WEEK); // Default to week view
		scaleSelector.setWidth("120px");
		scaleSelector.setPlaceholder("Select scale");
		add(label, scaleSelector);
	}

	/** Add listener for time scale changes.
	 * @param listener The listener to invoke when scale changes */
	public void addTimeScaleChangeListener(final com.vaadin.flow.component.HasValue.ValueChangeListener<
			com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent<ComboBox<TimeScale>, TimeScale>> listener) {
		scaleSelector.addValueChangeListener(listener);
	}

	/** Get the currently selected time scale.
	 * @return The selected time scale */
	public TimeScale getSelectedTimeScale() { return scaleSelector.getValue(); }

	/** Set the selected time scale.
	 * @param scale The time scale to select */
	public void setSelectedTimeScale(final TimeScale scale) {
		scaleSelector.setValue(scale);
	}
}
