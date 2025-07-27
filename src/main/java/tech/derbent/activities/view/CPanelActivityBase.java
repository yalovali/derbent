package tech.derbent.activities.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
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

	/**
	 * Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current activity entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        activity service
	 */
	public CPanelActivityBase(final String title, final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService) {
		super(title, currentEntity, beanValidationBinder, CActivity.class, entityService);
		createPanelContent();
		closePanel();
	}

	@Override
	protected ComboBoxDataProvider createComboBoxDataProvider() {
		return null;
	}

	@Override
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(CEntityFormBuilder.buildForm(CActivity.class, getBinder(),
			getEntityFields()));
	}
}
