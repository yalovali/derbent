package tech.derbent.app.sprints.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.ValueProvider;
import tech.derbent.api.grid.widget.CComponentWidgetEntityOfProject;
import tech.derbent.api.utils.Check;
import tech.derbent.app.sprints.domain.CSprint;

/** CComponentWidgetSprint - Widget component for displaying Sprint entities in grids.
 * <p>
 * This widget displays sprint information in a three-row layout:
 * <ul>
 * <li><b>Row 1:</b> Sprint name (12pt font)</li>
 * <li><b>Row 2:</b> Short description and status badge (10pt font)</li>
 * <li><b>Row 3:</b> Assigned user, start date, end date (10pt font)</li>
 * </ul>
 * </p>
 *
 * @author Derbent Framework
 * @since 1.0
 * @see CComponentWidgetEntityOfProject */
public class CComponentWidgetSprint extends CComponentWidgetEntityOfProject<CSprint> {

	private static final long serialVersionUID = 1L;

	/** Creates a static component from a sprint entity.
	 * @param item the sprint to display
	 * @return a new CComponentWidgetSprint component */
	public static Component create(final CSprint item) {
		Check.notNull(item, "Sprint cannot be null when creating widget");
		return new CComponentWidgetSprint(item);
	}

	/** Value provider for end date display.
	 * @return a value provider that formats the end date */
	public static ValueProvider<CSprint, String> endDateProvider() {
		return dateValueProvider(CSprint::getEndDate);
	}

	/** Value provider for item count display.
	 * @return a value provider that returns the item count */
	public static ValueProvider<CSprint, String> itemCountProvider() {
		return item -> {
			if (item == null) {
				return "";
			}
			final Integer count = item.getItemCount();
			return count != null ? count + " items" : "0 items";
		};
	}

	/** Value provider for progress percentage display.
	 * @return a value provider that formats the progress percentage */
	public static ValueProvider<CSprint, String> progressProvider() {
		return item -> {
			if (item == null) {
				return "";
			}
			final Integer progress = item.getProgressPercentage();
			return progress != null ? progress + "%" : "0%";
		};
	}

	/** Value provider for responsible user name display.
	 * @return a value provider that returns the assigned user's name */
	public static ValueProvider<CSprint, String> responsibleNameProvider() {
		return item -> {
			if (item == null || item.getResponsible() == null) {
				return "";
			}
			return item.getResponsible().getName();
		};
	}

	/** Value provider for start date display.
	 * @return a value provider that formats the start date */
	public static ValueProvider<CSprint, String> startDateProvider() {
		return dateValueProvider(CSprint::getStartDate);
	}

	/** Value provider for status name display.
	 * @return a value provider that returns the status name */
	public static ValueProvider<CSprint, String> statusNameProvider() {
		return item -> {
			if (item == null || item.getStatus() == null) {
				return "";
			}
			return item.getStatus().getName();
		};
	}

	/** Value provider for sprint type name display.
	 * @return a value provider that returns the type name */
	public static ValueProvider<CSprint, String> typeNameProvider() {
		return item -> {
			if (item == null || item.getEntityType() == null) {
				return "";
			}
			return item.getEntityType().getName();
		};
	}

	/** Value provider for widget component display.
	 * @return a value provider that creates a widget component */
	public static ValueProvider<CSprint, Component> widgetProvider() {
		return item -> create(item);
	}

	/** Creates a new sprint widget for the specified sprint.
	 * @param item the sprint to display in the widget */
	public CComponentWidgetSprint(final CSprint item) {
		super(item);
		addEditAction();
		addDeleteAction();
	}

	@Override
	protected String getEntityDescription() { return getEntity().getDescription(); }
}
