package tech.derbent.users.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.users.domain.CUser;
import tech.derbent.users.service.CUserService;

/**
 * CPanelUserBase - Abstract base class for all CUser-related accordion panels.
 * Layer: View (MVC)
 * Provides common functionality for user entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelUserBase extends CAccordionDescription<CUser> {

	private static final long serialVersionUID = 1L;

	public CPanelUserBase(final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService) {
		super(currentEntity, beanValidationBinder, CUser.class, entityService);
		createPanelContent();
		closePanel();
	}

	/**
	 * Constructor with custom panel title.
	 * @param title custom title for the panel
	 * @param currentEntity current user entity
	 * @param beanValidationBinder validation binder
	 * @param entityService user service
	 */
	public CPanelUserBase(final String title, final CUser currentEntity,
		final BeanValidationBinder<CUser> beanValidationBinder,
		final CUserService entityService) {
		super(title, currentEntity, beanValidationBinder, CUser.class, entityService);
		createPanelContent();
		closePanel();
	}

	@Override
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(CEntityFormBuilder.buildForm(CUser.class, getBinder(),
			getEntityFields()));
	}
}