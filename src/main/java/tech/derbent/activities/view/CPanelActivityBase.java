package tech.derbent.activities.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.views.CProjectAwareAccordionDescription;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.service.CActivityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.session.service.SessionService;

public abstract class CPanelActivityBase extends CProjectAwareAccordionDescription<CActivity> {

	private static final long serialVersionUID = 1L;

	public CPanelActivityBase(final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final SessionService sessionService) {
		super(currentEntity, beanValidationBinder, CActivity.class, entityService, sessionService);
		createPanelContent();
		closePanel();
	}

	/**
	 * Constructor with custom panel title.
	 * @param title                custom title for the panel
	 * @param currentEntity        current activity entity
	 * @param beanValidationBinder validation binder
	 * @param entityService        activity service
	 * @param sessionService       session service for project change notifications
	 */
	public CPanelActivityBase(final String title, final CActivity currentEntity,
		final BeanValidationBinder<CActivity> beanValidationBinder,
		final CActivityService entityService, final SessionService sessionService) {
		super(title, currentEntity, beanValidationBinder, CActivity.class, entityService, sessionService);
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

	@Override
	protected boolean shouldRefreshForProject(final CActivity entity, final CProject newProject) {
		// Activity panels should refresh if the entity's project doesn't match the new project
		if (entity == null || newProject == null) {
			return true; // Always refresh if either is null
		}
		
		// Check if the activity belongs to the new project
		final CProject activityProject = entity.getProject();
		if (activityProject == null) {
			return true; // Refresh if activity has no project
		}
		
		// Only refresh if the projects are different
		return !activityProject.getId().equals(newProject.getId());
	}
}
