package tech.derbent.projects.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CPanelProjectBase - Abstract base class for all CProject-related accordion panels.
 * Layer: View (MVC)
 * Provides common functionality for project entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelProjectBase extends CAccordionDescription<CProject> {

	private static final long serialVersionUID = 1L;

	public CPanelProjectBase(final CProject currentEntity,
		final BeanValidationBinder<CProject> beanValidationBinder,
		final CProjectService entityService) {
		super(currentEntity, beanValidationBinder, CProject.class, entityService);
		createPanelContent();
		closePanel();
	}

	/**
	 * Constructor with custom panel title.
	 * @param title custom title for the panel
	 * @param currentEntity current project entity
	 * @param beanValidationBinder validation binder
	 * @param entityService project service
	 */
	public CPanelProjectBase(final String title, final CProject currentEntity,
		final BeanValidationBinder<CProject> beanValidationBinder,
		final CProjectService entityService) {
		super(title, currentEntity, beanValidationBinder, CProject.class, entityService);
		createPanelContent();
		closePanel();
	}

	@Override
	protected void createPanelContent() {
		updatePanelEntityFields(); // Set the entity fields first
		getBaseLayout().add(CEntityFormBuilder.buildForm(CProject.class, getBinder(),
			getEntityFields()));
	}
}