package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

/** CPanelActivityTimeTracking - Panel for grouping time tracking related fields of CActivity entity. Layer: View (MVC) Groups fields: estimatedHours,
 * actualHours, remainingHours, startDate, dueDate, completionDate */
public class CPanelActivityTimeTracking extends CPanelActivityBase {
	private static final long serialVersionUID = 1L;

	public CPanelActivityTimeTracking(final CActivity currentEntity, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) throws Exception {
		super("Time Tracking", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Time Tracking fields - hours estimation, tracking and scheduling
		setEntityFields(List.of("estimatedHours", "actualHours", "remainingHours", "startDate", "dueDate", "completionDate"));
	}
}
