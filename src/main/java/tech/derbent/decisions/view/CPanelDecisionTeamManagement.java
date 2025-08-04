package tech.derbent.decisions.view;

import java.util.List;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.decisions.domain.CDecision;
import tech.derbent.decisions.service.CDecisionService;

/**
 * CPanelDecisionTeamManagement - Panel for decision team and accountability management.
 * Layer: View (MVC) Displays and allows editing of decision team members and accountable
 * personnel.
 */
public class CPanelDecisionTeamManagement extends CPanelDecisionBase {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for decision team management panel.
	 * @param currentEntity        the current decision entity
	 * @param beanValidationBinder validation binder for the decision
	 * @param entityService        decision service for data operations
	 */
	public CPanelDecisionTeamManagement(final CDecision currentEntity,
		final CEnhancedBinder<CDecision> beanValidationBinder,
		final CDecisionService entityService) {
		super("Team & Accountability", currentEntity, beanValidationBinder,
			entityService);
	}

	@Override
	protected void updatePanelEntityFields() {
		// Team and accountability management fields - using fields that exist with
		// @MetaData annotations
		setEntityFields(List.of("accountableUser", "teamMembers", "assignedTo"));
	}
}