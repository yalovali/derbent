package tech.derbent.activities.view;

import java.util.List;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;

public class CPanelActivityProject extends CPanelActivityBase {

	private static final long serialVersionUID = 1L;

	public CPanelActivityProject(IContentOwner parentContent, final CActivity currentEntity, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) throws Exception {
		super("Proje", parentContent, beanValidationBinder, entityService);
		initPanel();
	}

	@Override
	protected void updatePanelEntityFields() {
		setEntityFields(List.of("project"));
	}
}
