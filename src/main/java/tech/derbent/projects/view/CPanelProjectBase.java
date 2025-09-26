package tech.derbent.projects.view;

import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.views.CAccordionDBEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/** CPanelProjectBase - Abstract base class for all CProject-related accordion panels. Layer: View (MVC) Provides common functionality for project
 * entity panels following the same pattern as CPanelActivityBase. */
public abstract class CPanelProjectBase extends CAccordionDBEntity<CProject> {

	private static final long serialVersionUID = 1L;

	public CPanelProjectBase(final String title, IContentOwner parentContent, final CEnhancedBinder<CProject> beanValidationBinder,
			final CProjectService entityService) {
		super(title, parentContent, beanValidationBinder, CProject.class, entityService);
	}
}
