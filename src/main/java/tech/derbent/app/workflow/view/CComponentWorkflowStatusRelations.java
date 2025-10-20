package tech.derbent.app.workflow.view;

import java.util.List;
import org.springframework.context.ApplicationContext;
import tech.derbent.api.ui.dialogs.CWarningDialog;
import tech.derbent.app.activities.domain.CActivityStatus;
import tech.derbent.app.activities.service.CActivityStatusService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowEntityService;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;

/** Component for managing status transitions within a workflow (Workflow->Status direction). This component displays all status transitions defined
 * for a specific workflow and allows: - Adding new transitions - Editing existing transitions - Removing transitions The component automatically
 * updates when the current workflow changes. */
public class CComponentWorkflowStatusRelations extends CComponentWorkflowStatusRelationBase<CWorkflowEntity, CWorkflowStatusRelation> {

	private static final long serialVersionUID = 1L;
	private final CActivityStatusService statusService;

	public CComponentWorkflowStatusRelations(final CWorkflowEntityService entityService, ISessionService sessionService,
			ApplicationContext applicationContext) throws Exception {
		super("Status Transitions", CWorkflowEntity.class, entityService, sessionService, applicationContext);
		statusService = applicationContext.getBean(CActivityStatusService.class);
		initComponent();
	}

	public List<CActivityStatus> getAvailableStatuses() {
		final CWorkflowEntity workflow = getCurrentEntity();
		LOGGER.debug("Getting available statuses for workflow: {}", workflow != null ? workflow.getName() : "null");
		if (workflow == null) {
			LOGGER.warn("Current workflow is null, returning empty status list");
			return List.of();
		}
		// Return all activity statuses for the workflow's project
		return statusService.findAll();
	}

	@Override
	protected void openAddDialog() throws Exception {
		try {
			new CWorkflowStatusRelationDialog(this, (CWorkflowEntityService) entityService, statusService, workflowStatusRelationService, null,
					getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open add dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CWorkflowStatusRelationDialog(this, (CWorkflowEntityService) entityService, statusService, workflowStatusRelationService,
					getSelectedRelation(), getCurrentEntity(), this::onSettingsSaved).open();
		} catch (Exception e) {
			new CWarningDialog("Failed to open edit dialog: " + e.getMessage()).open();
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> workflowStatusRelationService.findByWorkflow(getCurrentEntity()),
				() -> entityService.save(getCurrentEntity()));
	}
}
