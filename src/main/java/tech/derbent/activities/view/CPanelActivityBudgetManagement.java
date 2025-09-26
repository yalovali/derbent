package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;

/** CPanelActivityBudgetManagement - Panel for grouping budget management related fields of CActivity entity. Layer: View (MVC) Groups fields:
 * estimatedCost, actualCost, hourlyRate */
public class CPanelActivityBudgetManagement extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBudgetManagement(IContentOwner parentContent, final CActivity currentEntity,
			final CEnhancedBinder<CActivity> beanValidationBinder, final CActivityService entityService) throws Exception {
		super("Budget Management", parentContent,beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Budget Management fields - cost estimation and tracking
		setEntityFields(List.of("estimatedCost", "actualCost", "hourlyRate"));
	}
}
