package tech.derbent.activities.view;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;

public class CPanelActivityHierarchy extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityHierarchy(IContentOwner parentContent, final CActivity currentEntity, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) throws Exception {
		super("Hierarchy", parentContent,beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		// Hierarchy fields - parent-child relationships
		// setEntityFields(List.of("parentActivity"));
	}
}
