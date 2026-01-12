package tech.derbent.api.workflow.view;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.api.workflow.service.CWorkflowEntityService;
import tech.derbent.base.session.service.ISessionService;

/** Component for managing status transitions within a workflow (Workflow->Status direction). This component displays all status transitions defined
 * for a specific workflow and allows: - Adding new transitions - Editing existing transitions - Removing transitions The component automatically
 * updates when the current workflow changes. */
public class CComponentWorkflowStatusRelations extends CComponentWorkflowStatusRelationBase<CWorkflowEntity, CWorkflowStatusRelation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CComponentWorkflowStatusRelations.class);
	private static final long serialVersionUID = 1L;

	public CComponentWorkflowStatusRelations(final CWorkflowEntityService entityService, ISessionService sessionService) throws Exception {
		super(CWorkflowEntity.class, entityService, sessionService);
		initComponent();
	}

	@Override
	public CEntityDB<?> createNewEntityInstance() throws Exception {
		return null;
	}

	public List<CProjectItemStatus> getAvailableStatuses() {
		final CWorkflowEntity workflow = getValue();
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
			new CDialogWorkflowStatusRelation(this, (CWorkflowEntityService) entityService, statusService, workflowStatusRelationService, null,
					getValue(), this::onSettingsSaved).open();
		} catch (final Exception e) {
			CNotificationService.showWarning("Failed to open add dialog: " + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void openEditDialog() throws Exception {
		try {
			new CDialogWorkflowStatusRelation(this, (CWorkflowEntityService) entityService, statusService, workflowStatusRelationService,
					getSelectedRelation(), getValue(), this::onSettingsSaved).open();
		} catch (final Exception e) {
			CNotificationService.showWarning("Failed to open edit dialog: " + e.getMessage());
			throw e;
		}
	}

	@Override
	protected void setupDataAccessors() {
		createStandardDataAccessors(() -> workflowStatusRelationService.findByWorkflow(getValue()), () -> entityService.save(getValue()));
	}
}
