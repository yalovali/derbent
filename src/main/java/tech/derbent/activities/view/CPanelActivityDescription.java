package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

public class CPanelActivityDescription extends CPanelActivityBase {
	private static final long serialVersionUID = 1L;

	public CPanelActivityDescription(final CActivity currentEntity, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) throws Exception {
		super("Basic Information", currentEntity, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Basic Information panel - only fundamental fields
		setEntityFields(List.of("name", "description", "activityType", "project"));
	}
}
