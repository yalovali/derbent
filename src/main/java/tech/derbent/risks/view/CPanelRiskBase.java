package tech.derbent.risks.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.views.CAccordionDBEntity;
import tech.derbent.risks.domain.CRisk;
import tech.derbent.risks.service.CRiskService;

/**
 * CPanelRiskBase - Abstract base class for all CRisk-related accordion panels. Layer: View (MVC) Provides common
 * functionality for risk entity panels following the same pattern as CPanelActivityBase.
 */
public abstract class CPanelRiskBase extends CAccordionDBEntity<CRisk> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with custom panel title.
     * 
     * @param title
     *            custom title for the panel
     * @param currentEntity
     *            current risk entity
     * @param beanValidationBinder
     *            validation binder
     * @param entityService
     *            risk service
     */
    public CPanelRiskBase(final String title, final CRisk currentEntity,
            final BeanValidationBinder<CRisk> beanValidationBinder, final CRiskService entityService) {
        super(title, currentEntity, beanValidationBinder, CRisk.class, entityService);
        createPanelContent();
        closePanel();
    }
}