package tech.derbent.activities.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;

public abstract class CPanelActivityBase extends CAccordionDescription<CActivity> {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBase(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super(currentEntity, beanValidationBinder, CActivity.class, entityService);
		createPanelContent();
		closePanel();
	}

	@Override
	protected void createPanelContent() {
		getBaseLayout().add(CEntityFormBuilder.buildForm(CActivity.class, getBinder(),
			getEntityFields()));
	}
}
