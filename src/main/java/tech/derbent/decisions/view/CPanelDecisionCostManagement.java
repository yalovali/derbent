package tech.derbent.decisions.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;

/**
 * CPanelDecisionCostManagement - Panel for decision cost and financial information. Layer: View (MVC) Displays and
 * allows editing of decision cost estimation and financial impact.
 */
public class CPanelDecisionCostManagement extends CPanelDecisionBase {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor for decision cost management panel.
     * 
     * @param currentEntity
     *            the current decision entity
     * @param beanValidationBinder
     *            validation binder for the decision
     * @param entityService
     *            decision service for data operations
     */
    public CPanelDecisionCostManagement(final CDecision currentEntity,
            final CEnhancedBinder<CDecision> beanValidationBinder, final CDecisionService entityService) {
        super("Cost & Financial Impact", currentEntity, beanValidationBinder, entityService);
        initPanel();
    }

    @Override
    protected void updatePanelEntityFields() {
        // Cost and financial management fields
        setEntityFields(List.of("estimatedCost"));
    }
}