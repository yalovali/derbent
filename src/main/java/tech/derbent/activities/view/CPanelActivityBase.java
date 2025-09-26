package tech.derbent.activities.view;

import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;

public abstract class CPanelActivityBase extends CAccordionDBEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBase(final String title, IContentOwner parentContent, final CEnhancedBinder<CActivity> beanValidationBinder,
			final CActivityService entityService) {
		super(title, parentContent, beanValidationBinder, CActivity.class, entityService);
	}
}
