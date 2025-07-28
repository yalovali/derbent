package tech.derbent.decisions.view;

import com.vaadin.flow.data.binder.BeanValidationBinder;

import tech.derbent.abstracts.annotations.CEntityFormBuilder;
import tech.derbent.abstracts.annotations.CEntityFormBuilder.ComboBoxDataProvider;
import tech.derbent.abstracts.views.CAccordionDescription;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;

/**
 * CPanelDecisionBase - Abstract base class for decision entity panels.
 * Layer: View (MVC)
 * 
 * Provides common functionality for all decision-related panels following
 * the established patterns from CPanelActivityBase.
 */
public abstract class CPanelDecisionBase extends CAccordionDescription<CDecision> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor with default title based on entity.
     * @param currentEntity the current decision entity
     * @param beanValidationBinder validation binder for the decision
     * @param entityService decision service for data operations
     */
    public CPanelDecisionBase(final CDecision currentEntity,
                             final BeanValidationBinder<CDecision> beanValidationBinder,
                             final CDecisionService entityService) {
        super(currentEntity, beanValidationBinder, CDecision.class, entityService);
        createPanelContent();
        closePanel();
    }

    /**
     * Constructor with custom panel title.
     * @param title custom title for the panel
     * @param currentEntity current decision entity
     * @param beanValidationBinder validation binder for the decision
     * @param entityService decision service for data operations
     */
    public CPanelDecisionBase(final String title, final CDecision currentEntity,
                             final BeanValidationBinder<CDecision> beanValidationBinder,
                             final CDecisionService entityService) {
        super(title, currentEntity, beanValidationBinder, CDecision.class, entityService);
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
        getBaseLayout().add(CEntityFormBuilder.buildForm(CDecision.class, getBinder(),
                                                         getEntityFields()));
    }
}