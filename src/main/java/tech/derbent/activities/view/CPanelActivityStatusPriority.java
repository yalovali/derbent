package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;

/** CPanelActivityStatusPriority - Panel for grouping status and priority related fields of CActivity entity. Layer: View (MVC) Groups fields: status,
 * priority, progressPercentage */
public class CPanelActivityStatusPriority extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityStatusPriority(IContentOwner parentContent, final CActivity currentEntity,
			final CEnhancedBinder<CActivity> beanValidationBinder, final CActivityService entityService) throws Exception {
		super("Status & Priority", parentContent,beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Status & Priority fields - workflow and progress management
		setEntityFields(List.of("status", "priority", "progressPercentage"));
	}
}
