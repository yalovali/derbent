package tech.derbent.plm.sprints.service;

import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;

/** Shared helpers for sprint/backlog transitions that must align with workflow initial status rules. */
public final class CSprintItemWorkflowStatusSupport {

	private CSprintItemWorkflowStatusSupport() {
		// Utility.
	}

	public static CProjectItemStatus applyWorkflowInitialStatus(final IHasStatusAndWorkflow<?, ?> item,
			final CProjectItemStatusService statusService) {
		if (item == null || statusService == null) {
			return null;
		}
		final CWorkflowEntity workflow = item.getWorkflow();
		if (workflow == null) {
			return null;
		}
		final CProjectItemStatus resetStatus = statusService.getInitialStatusFromWorkflow(workflow);
		if (resetStatus == null) {
			return null;
		}
		item.setStatus(resetStatus);
		return resetStatus;
	}
}
