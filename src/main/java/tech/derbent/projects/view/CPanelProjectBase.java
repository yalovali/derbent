package tech.derbent.projects.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.service.CProjectService;

/**
 * CPanelProjectBase - Abstract base class for all CProject-related accordion panels. Layer: View (MVC) Provides common
 * functionality for project entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelProjectBase extends CAccordionDBEntity<CProject> {

    private static final long serialVersionUID = 1L;

    public CPanelProjectBase(final String title, final CProject currentEntity,
            final BeanValidationBinder<CProject> beanValidationBinder, final CProjectService entityService) {
        super(title, currentEntity, beanValidationBinder, CProject.class, entityService);
        createPanelContent();
        closePanel();
    }
}