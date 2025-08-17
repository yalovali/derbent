package tech.derbent.decisions.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;

/**
 * CPanelDecisionStatusManagement - Panel for decision status and workflow management. Layer: View (MVC) Displays and
 * allows editing of decision status information and workflow controls.
 */
public class CPanelDecisionStatusManagement extends CPanelDecisionBase {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for decision status management panel.
     * 
     * @param currentEntity
     *            the current decision entity
     * @param beanValidationBinder
     *            validation binder for the decision
     * @param entityService
     *            decision service for data operations
     */
    public CPanelDecisionStatusManagement(final CDecision currentEntity,
            final CEnhancedBinder<CDecision> beanValidationBinder, final CDecisionService entityService) {
        super("Status & Workflow", currentEntity, beanValidationBinder, entityService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Status and workflow management fields
        setEntityFields(List.of("decisionStatus", "implementationDate", "reviewDate"));
    }
}