package tech.derbent.app.workflow.view;

import org.springframework.context.ApplicationContext;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.views.components.CComponentRelationPanelBase;
import tech.derbent.app.activities.service.CActivityStatusService;
import tech.derbent.app.roles.service.CUserProjectRoleService;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.app.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;

/** Generic base class for Workflow-Status relationship components. This class provides common functionality for workflow status transition management
 * components, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterClass>     The main entity type (CWorkflowEntity for workflow-centric)
 * @param <RelationalClass> The relationship entity type (always CWorkflowStatusRelation) */
public abstract class CComponentWorkflowStatusRelationBase<MasterClass extends CEntityNamed<MasterClass>,
		RelationalClass extends CEntityDB<RelationalClass>> extends CComponentRelationPanelBase<MasterClass, CWorkflowStatusRelation> {

	private static final long serialVersionUID = 1L;
	protected final CUserProjectRoleService roleService;
	protected final CActivityStatusService statusService;
	protected final CWorkflowStatusRelationService workflowStatusRelationService;

	public CComponentWorkflowStatusRelationBase(final String title, final Class<MasterClass> entityClass,
			final CAbstractService<MasterClass> entityService, ISessionService sessionService, final ApplicationContext applicationContext) {
		super(title, entityClass, CWorkflowStatusRelation.class, entityService, applicationContext.getBean(CWorkflowStatusRelationService.class),
				sessionService, applicationContext);
		workflowStatusRelationService = applicationContext.getBean(CWorkflowStatusRelationService.class);
		statusService = applicationContext.getBean(CActivityStatusService.class);
		roleService = applicationContext.getBean(CUserProjectRoleService.class);
	}

	@Override
	protected void deleteRelation(CWorkflowStatusRelation selected) throws Exception {
		workflowStatusRelationService.deleteByWorkflowAndStatuses(selected.getWorkflow(), selected.getFromStatus(), selected.getToStatus());
	}

	@Override
	protected String getDeleteConfirmationMessage(final CWorkflowStatusRelation selected) {
		Check.notNull(selected, "Selected relation cannot be null");
		Check.notNull(selected.getFromStatus(), "From status cannot be null");
		Check.notNull(selected.getToStatus(), "To status cannot be null");
		final String fromStatusName = selected.getFromStatus().getName();
		final String toStatusName = selected.getToStatus().getName();
		final String rolesText = selected.getRoles() != null && !selected.getRoles().isEmpty()
				? selected.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.joining(", ")) : "All Roles";
		return String.format("Are you sure you want to delete the transition from '%s' to '%s' for roles: %s? This action cannot be undone.",
				fromStatusName, toStatusName, rolesText);
	}

	@Override
	protected String getDisplayText(final CWorkflowStatusRelation relation, final String type) {
		Check.notNull(relation, "Relation cannot be null when getting display text");
		try {
			switch (type) {
			case "workflow":
				Check.notNull(relation.getWorkflow(), "Workflow cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getWorkflow());
			case "fromStatus":
				Check.notNull(relation.getFromStatus(), "From status cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getFromStatus());
			case "toStatus":
				Check.notNull(relation.getToStatus(), "To status cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getToStatus());
			case "roles":
				return relation.getRoles() != null && !relation.getRoles().isEmpty() ? relation.getRoles().stream()
						.map(r -> CColorUtils.getDisplayTextFromEntity(r)).collect(java.util.stream.Collectors.joining(", ")) : "All Roles";
			default:
				return "";
			}
		} catch (Exception e) {
			LOGGER.error("Failed to get display text for type {}: {}", type, e.getMessage());
			return "";
		}
	}

	protected CWorkflowStatusRelation getSelectedRelation() { return getSelectedSetting(); }

	protected boolean isWorkflowMaster() {
		// return true if MasterClass is CWorkflowEntity
		return CWorkflowEntity.class.equals(getEntityClass());
	}

	/** Abstract methods that subclasses must implement */
	@Override
	protected void onSettingsSaved(final CWorkflowStatusRelation relation) {
		try {
			Check.notNull(relation, "Relation cannot be null when saving");
			LOGGER.debug("Saving workflow status relation: {}", relation);
			final CWorkflowStatusRelation savedRelation =
					relation.getId() == null ? workflowStatusRelationService.addStatusTransition(relation.getWorkflow(), relation.getFromStatus(),
							relation.getToStatus(), relation.getRoles()) : workflowStatusRelationService.save(relation);
			LOGGER.info("Successfully saved workflow status relation: {}", savedRelation);
			populateForm();
		} catch (final Exception e) {
			LOGGER.error("Error saving workflow status relation.");
			throw e;
		}
	}

	@Override
	protected abstract void openAddDialog() throws Exception;
	@Override
	protected abstract void openEditDialog() throws Exception;
	/** Abstract method for setting up data accessors - subclasses provide specific implementations */
	@Override
	protected abstract void setupDataAccessors();

	/** Sets up the grid with enhanced visual styling including colors, icons and consistent headers. Uses entity decorations with colors and icons
	 * for better visual representation of workflow status transitions. */
	@Override
	protected void setupGrid(final Grid<CWorkflowStatusRelation> grid) {
		try {
			super.setupGrid(grid);
			LOGGER.debug("Setting up grid for Workflow Status Relation component.");
			// Add workflow column if workflow is not the master entity (only show in status-centric view)
			if (!isWorkflowMaster()) {
				grid.addComponentColumn(relation -> {
					try {
						return CColorUtils.getEntityWithIcon(relation.getWorkflow());
					} catch (Exception e) {
						LOGGER.error("Failed to create workflow component.");
						return new com.vaadin.flow.component.html.Span(getDisplayText(relation, "workflow"));
					}
				}).setHeader(CColorUtils.createStyledHeader("Workflow", "#2E7D32")).setAutoWidth(true).setSortable(true);
			}
			// Add From Status column with color and icon
			grid.addComponentColumn(relation -> {
				try {
					return CColorUtils.getEntityWithIcon(relation.getFromStatus());
				} catch (Exception e) {
					LOGGER.error("Failed to create from status component.");
					return new com.vaadin.flow.component.html.Span(getDisplayText(relation, "fromStatus"));
				}
			}).setHeader(CColorUtils.createStyledHeader("From Status", "#1565C0")).setAutoWidth(true).setSortable(true);
			// Add To Status column with color and icon
			grid.addComponentColumn(relation -> {
				try {
					return CColorUtils.getEntityWithIcon(relation.getToStatus());
				} catch (Exception e) {
					LOGGER.error("Failed to create to status component.");
					return new com.vaadin.flow.component.html.Span(getDisplayText(relation, "toStatus"));
				}
			}).setHeader(CColorUtils.createStyledHeader("To Status", "#F57F17")).setAutoWidth(true).setSortable(true);
			// Add Roles column with color and icon (can be empty for "All Roles")
			grid.addComponentColumn(relation -> {
				try {
					if (relation.getRoles() != null && !relation.getRoles().isEmpty()) {
						// Display roles as comma-separated list with icons
						com.vaadin.flow.component.orderedlayout.HorizontalLayout rolesLayout =
								new com.vaadin.flow.component.orderedlayout.HorizontalLayout();
						rolesLayout.setSpacing(true);
						for (int i = 0; i < relation.getRoles().size(); i++) {
							rolesLayout.add(CColorUtils.getEntityWithIcon(relation.getRoles().get(i)));
							if (i < relation.getRoles().size() - 1) {
								rolesLayout.add(new com.vaadin.flow.component.html.Span(", "));
							}
						}
						return rolesLayout;
					} else {
						com.vaadin.flow.component.html.Span span = new com.vaadin.flow.component.html.Span("All Roles");
						span.getStyle().set("font-style", "italic");
						span.getStyle().set("color", "#666");
						return span;
					}
				} catch (Exception e) {
					LOGGER.error("Failed to create roles component.");
					return new com.vaadin.flow.component.html.Span(getDisplayText(relation, "roles"));
				}
			}).setHeader(CColorUtils.createStyledHeader("Roles", "#8E24AA")).setAutoWidth(true).setSortable(true);
		} catch (Exception e) {
			LOGGER.error("Failed to setup grid.");
			throw e;
		}
	}
}
