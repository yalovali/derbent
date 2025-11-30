package tech.derbent.app.activities.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.function.ValueProvider;
import tech.derbent.api.grid.widget.CComponentWidgetEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.app.activities.domain.CActivity;

public class CComponentWidgetActivity extends CComponentWidgetEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	public static ValueProvider<CActivity, String> actualHoursProvider() {
		return hoursValueProvider(CActivity::getActualHours);
	}

	public static ValueProvider<CActivity, String> assignedToNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getAssignedTo() == null)) {
				return "";
			}
			return activity.getAssignedTo().getName();
		};
	}

	public static Component create(final CActivity activity) {
		Check.notNull(activity, "Activity cannot be null when creating widget");
		return new CComponentWidgetActivity(activity);
	}

	public static ValueProvider<CActivity, String> dueDateProvider() {
		return dateValueProvider(CActivity::getDueDate);
	}

	public static ValueProvider<CActivity, String> estimatedHoursProvider() {
		return hoursValueProvider(CActivity::getEstimatedHours);
	}

	public static ValueProvider<CActivity, String> priorityNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getPriority() == null)) {
				return "";
			}
			return activity.getPriority().getName();
		};
	}

	public static ValueProvider<CActivity, String> remainingHoursProvider() {
		return hoursValueProvider(CActivity::getRemainingHours);
	}

	public static ValueProvider<CActivity, String> startDateProvider() {
		return dateValueProvider(CActivity::getStartDate);
	}

	public static ValueProvider<CActivity, String> statusNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getStatus() == null)) {
				return "";
			}
			return activity.getStatus().getName();
		};
	}

	public static ValueProvider<CActivity, String> typeNameProvider() {
		return activity -> {
			if ((activity == null) || (activity.getEntityType() == null)) {
				return "";
			}
			return activity.getEntityType().getName();
		};
	}

	public static ValueProvider<CActivity, Component> widgetProvider() {
		return activity -> create(activity);
	}

	public CComponentWidgetActivity(final CActivity activity) {
		super(activity);
		addEditAction();
		addDeleteAction();
	}

	@Override
	protected String getEntityDescription() { return getEntity().getDescription(); }
}
