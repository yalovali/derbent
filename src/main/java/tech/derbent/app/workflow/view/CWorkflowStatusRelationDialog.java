package tech.derbent.app.workflow.view;

import java.util.List;
import java.util.function.Consumer;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.dialogs.CDBRelationDialog;
import tech.derbent.app.activities.domain.CActivityStatus;
import tech.derbent.app.activities.service.CActivityStatusService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;

/** Dialog for managing workflow status transitions (Workflow->Status direction). This dialog allows defining status transitions for a specific
 * workflow with role-based permissions. */
public class CWorkflowStatusRelationDialog extends CDBRelationDialog<CWorkflowStatusRelation, CWorkflowEntity, CActivityStatus> {

	private static final long serialVersionUID = 1L;
	protected final CActivityStatusService statusService;
	protected final CWorkflowEntityService workflowService;
	protected final CWorkflowStatusRelationService workflowStatusRelationService;

	public CWorkflowStatusRelationDialog(IContentOwner parentContent, final CWorkflowEntityService workflowService,
			final CActivityStatusService statusService, final CWorkflowStatusRelationService workflowStatusRelationService,
			final CWorkflowStatusRelation relation, final CWorkflowEntity workflow, final Consumer<CWorkflowStatusRelation> onSave) throws Exception {
		super(parentContent, relation != null ? relation : new CWorkflowStatusRelation(), workflow, workflowService, statusService,
				workflowStatusRelationService, onSave, relation == null);
		// Store services for easy access
		this.workflowService = workflowService;
		this.statusService = statusService;
		this.workflowStatusRelationService = workflowStatusRelationService;
		// Set the appropriate entity reference
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

	/** Override populateForm to ensure comboboxes are properly refreshed with current entity values. */
	@Override
	protected void populateForm() {
		// Call parent implementation to read bean into binder
		super.populateForm();
		// Explicitly refresh ComboBox values to ensure they display current entity values
		// This is necessary because the binder may not automatically update ComboBox display values
		try {
			// Get the fromStatus ComboBox and refresh it
			if (formBuilder != null && getEntity() != null) {
				try {
					@SuppressWarnings ("unchecked")
					ComboBox<CActivityStatus> fromStatusComboBox = (ComboBox<CActivityStatus>) formBuilder.getComponent("fromStatus");
					if (fromStatusComboBox != null && getEntity().getFromStatus() != null) {
						fromStatusComboBox.setValue(getEntity().getFromStatus());
						LOGGER.debug("Refreshed fromStatus ComboBox with value: {}", getEntity().getFromStatus().getName());
					}
				} catch (Exception e) {
					LOGGER.warn("Could not refresh fromStatus ComboBox: {}", e.getMessage());
				}
				try {
					@SuppressWarnings ("unchecked")
					ComboBox<CActivityStatus> toStatusComboBox = (ComboBox<CActivityStatus>) formBuilder.getComponent("toStatus");
					if (toStatusComboBox != null && getEntity().getToStatus() != null) {
						toStatusComboBox.setValue(getEntity().getToStatus());
						LOGGER.debug("Refreshed toStatus ComboBox with value: {}", getEntity().getToStatus().getName());
					}
				} catch (Exception e) {
					LOGGER.warn("Could not refresh toStatus ComboBox: {}", e.getMessage());
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error refreshing comboboxes: {}", e.getMessage());
		}
	}

	/** Sets up the entity relation based on the workflow entity. */
	protected void setupEntityRelation(CWorkflowEntity workflow) {
		getEntity().setWorkflow(workflow);
	}

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
		final CWorkflowEntity workflow = getEntity().getWorkflow();
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
								throw new IllegalArgumentException(String.format(
										"A transition from '%s' to '%s' already exists for this workflow. Please choose different statuses.",
										getEntity().getFromStatus().getName(), getEntity().getToStatus().getName()));
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
