package tech.derbent.app.workflow.view;

import java.util.List;
import java.util.function.Consumer;
import tech.derbent.api.interfaces.IContentOwner;
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

	@SuppressWarnings ("unchecked")
	public CWorkflowStatusRelationDialog(IContentOwner parentContent, final CWorkflowEntityService workflowService,
			final CActivityStatusService statusService, final CWorkflowStatusRelationService workflowStatusRelationService,
			final CWorkflowStatusRelation relation, final CWorkflowEntity workflow, final Consumer<CWorkflowStatusRelation> onSave) throws Exception {
		super(parentContent, relation != null ? relation : new CWorkflowStatusRelation(), workflow,
				(tech.derbent.api.services.CAbstractService) workflowService, (tech.derbent.api.services.CAbstractService) statusService,
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
	protected List<String> getFormFields() { return List.of("fromStatus", "toStatus", "role"); }

	@Override
	protected String getEditDialogTitle() { return "Edit Workflow Status Transition"; }

	@Override
	protected String getEditFormTitle() { return "Edit Status Transition"; }

	@Override
	protected String getNewDialogTitle() { return "Add Workflow Status Transition"; }

	@Override
	protected String getNewFormTitle() { return "Define Status Transition for Workflow"; }

	/** Sets up the entity relation based on the workflow entity. */
	protected void setupEntityRelation(CWorkflowEntity workflow) {
		getEntity().setWorkflow(workflow);
	}
}
