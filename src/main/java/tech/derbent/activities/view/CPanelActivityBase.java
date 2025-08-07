package tech.derbent.activities.view;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

public abstract class CPanelActivityBase extends CAccordionDBEntity<CActivity> {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBase(final String title, final CActivity currentEntity,
		final CEnhancedBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super(title, currentEntity, beanValidationBinder, CActivity.class, entityService);
	}
}
