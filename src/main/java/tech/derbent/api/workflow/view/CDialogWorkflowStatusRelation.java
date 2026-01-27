package tech.derbent.api.workflow.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.ui.dialogs.CDialogDBRelation;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;

/** Dialog for managing workflow status transitions (Workflow->Status direction). This dialog allows defining status transitions for a specific
 * workflow with role-based permissions. */
public class CDialogWorkflowStatusRelation extends CDialogDBRelation<CWorkflowStatusRelation, CWorkflowEntity, CProjectItemStatus> {

	private static final long serialVersionUID = 1L;
	protected final CProjectItemStatusService statusService;
	protected final CWorkflowEntityService workflowService;
	protected final CWorkflowStatusRelationService workflowStatusRelationService;

	public CDialogWorkflowStatusRelation(IContentOwner parentContent, final CWorkflowEntityService workflowService,
			final CProjectItemStatusService statusService, final CWorkflowStatusRelationService workflowStatusRelationService,
			final CWorkflowStatusRelation relation, final CWorkflowEntity workflow, final Consumer<CWorkflowStatusRelation> onSave) throws Exception {
		super(parentContent, relation != null ? relation : new CWorkflowStatusRelation(true), workflow, workflowService, statusService,
				workflowStatusRelationService, onSave, relation == null);
		// Store services for easy access
		this.workflowService = workflowService;
		this.statusService = statusService;
		this.workflowStatusRelationService = workflowStatusRelationService;
		// Set the appropriate entity reference using reflection-based method from parent
		setupEntityRelation(workflow);
		// Apply colorful styling to make the dialog more visually appealing
		setupDialog();
		populateForm();
	}

	@Override
	protected String getEditDialogTitle() { return "Edit Workflow Status Transition"; }

	@Override
	protected String getEditFormTitle() { return "Edit Status Transition"; }

	@Override
	protected List<String> getFormFields() { return List.of("fromStatus", "toStatus", "roles"); }

	@Override
	protected String getNewDialogTitle() { return "Add Workflow Status Transition"; }

	@Override
	protected String getNewFormTitle() { return "Define Status Transition for Workflow"; }

	/** Validates the form before saving. Checks for: 1. Required fields (fromStatus, toStatus) 2. fromStatus and toStatus must be different 3. No
	 * duplicate transitions (same workflow, fromStatus, toStatus combination) 4. Warns if a status is missing (though this is
	 * application-dependent) */
	@Override
	protected void validateForm() {
		LOGGER.debug("Validating workflow status relation form");
		// Check entity is not null
		Check.notNull(getEntity(), "Workflow status relation cannot be null");
		// Validate fromStatus is set
		if (getEntity().getFromStatus() == null) {
			throw new IllegalArgumentException("'From Status' is required. Please select a status.");
		}
		// Validate toStatus is set
		if (getEntity().getToStatus() == null) {
			throw new IllegalArgumentException("'To Status' is required. Please select a status.");
		}
		// Check that fromStatus and toStatus are different
		if (getEntity().getFromStatus().equals(getEntity().getToStatus())) {
			throw new IllegalArgumentException(String.format("'From Status' and 'To Status' cannot be the same. You selected '%s' for both fields.",
					getEntity().getFromStatus().getName()));
		}
		// Check for duplicate transition (only for new entities or if statuses changed)
		final CWorkflowEntity workflow = getEntity().getWorkflowEntity();
		if (workflow != null && workflow.getId() != null && getEntity().getFromStatus().getId() != null
				&& getEntity().getToStatus().getId() != null) {
			// For new entities, check if transition already exists
			if (isNew) {
				if (workflowStatusRelationService.relationshipExists(workflow.getId(), getEntity().getFromStatus().getId(),
						getEntity().getToStatus().getId())) {
					throw new IllegalArgumentException(
							String.format("A transition from '%s' to '%s' already exists for this workflow. Please choose different statuses.",
									getEntity().getFromStatus().getName(), getEntity().getToStatus().getName()));
				}
			} else {
				// For existing entities, check if transition exists with different ID
				// This prevents duplicate transitions when editing
				workflowStatusRelationService
						.findRelationshipByStatuses(workflow.getId(), getEntity().getFromStatus().getId(), getEntity().getToStatus().getId())
						.ifPresent(existing -> {
							if (!existing.getId().equals(getEntity().getId())) {
								throw new IllegalArgumentException("A transition from '%s' to '%s' already exists for this workflow. Please choose different statuses.".formatted(getEntity().getFromStatus().getName(), getEntity().getToStatus().getName()));
							}
						});
			}
		}
		// Optional: Warn if one of the statuses might be missing from workflow
		// This is a soft validation - we log a warning but don't block the save
		if (getEntity().getFromStatus() != null) {
			LOGGER.debug("From status '{}' will be part of this workflow's transition graph", getEntity().getFromStatus().getName());
		}
		if (getEntity().getToStatus() != null) {
			LOGGER.debug("To status '{}' will be part of this workflow's transition graph", getEntity().getToStatus().getName());
		}
		LOGGER.info("Workflow status relation form validation passed");
	}
}
