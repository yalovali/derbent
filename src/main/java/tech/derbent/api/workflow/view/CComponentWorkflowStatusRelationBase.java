package tech.derbent.api.workflow.view;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.grid.Grid;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.grid.domain.CGrid;
import tech.derbent.api.grid.view.CLabelEntity;
import tech.derbent.api.ui.component.enhanced.CComponentRelationPanelBase;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;
import tech.derbent.api.roles.service.CUserProjectRoleService;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.domain.CWorkflowStatusRelation;
import tech.derbent.api.workflow.service.CWorkflowStatusRelationService;
import tech.derbent.base.session.service.ISessionService;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.stream.Collectors;

/** Generic base class for Workflow-Status relationship components. This class provides common functionality for workflow status transition management
 * components, reducing code duplication while maintaining flexibility for specific implementations.
 * @param <MasterClass>     The main entity type (CWorkflowEntity for workflow-centric)
 * @param <RelationalClass> The relationship entity type (always CWorkflowStatusRelation) */
public abstract class CComponentWorkflowStatusRelationBase<MasterClass extends CEntityNamed<MasterClass>,
		RelationalClass extends CEntityDB<RelationalClass>> extends CComponentRelationPanelBase<MasterClass, CWorkflowStatusRelation> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CWorkflowStatusRelation.class);
	private static final long serialVersionUID = 1L;
	protected final CUserProjectRoleService roleService;
	protected final CProjectItemStatusService statusService;
	protected final CWorkflowStatusRelationService workflowStatusRelationService;

	public CComponentWorkflowStatusRelationBase(final Class<MasterClass> entityClass, final CAbstractService<MasterClass> entityService,
			ISessionService sessionService) {
		super(entityClass, CWorkflowStatusRelation.class, entityService,
				CSpringContext.<CWorkflowStatusRelationService>getBean(CWorkflowStatusRelationService.class), sessionService);
		workflowStatusRelationService = CSpringContext.<CWorkflowStatusRelationService>getBean(CWorkflowStatusRelationService.class);
		statusService = CSpringContext.<CProjectItemStatusService>getBean(CProjectItemStatusService.class);
		roleService = CSpringContext.<CUserProjectRoleService>getBean(CUserProjectRoleService.class);
	}

	@Override
	protected void deleteRelation(CWorkflowStatusRelation selected) throws Exception {
		workflowStatusRelationService.deleteByWorkflowAndStatuses(selected.getWorkflowEntity(), selected.getFromStatus(), selected.getToStatus());
	}

	@Override
	protected String getDeleteConfirmationMessage(final CWorkflowStatusRelation selected) {
		Check.notNull(selected, "Selected relation cannot be null");
		Check.notNull(selected.getFromStatus(), "From status cannot be null");
		Check.notNull(selected.getToStatus(), "To status cannot be null");
		final String fromStatusName = selected.getFromStatus().getName();
		final String toStatusName = selected.getToStatus().getName();
		final String rolesText = selected.getRoles() != null && !selected.getRoles().isEmpty()
				? selected.getRoles().stream().map(r -> r.getName()).collect(Collectors.joining(", ")) : "All Roles";
		return String.format("Are you sure you want to delete the transition from '%s' to '%s' for roles: %s? This action cannot be undone.",
				fromStatusName, toStatusName, rolesText);
	}

	@Override
	protected String getDisplayText(final CWorkflowStatusRelation relation, final String type) {
		Check.notNull(relation, "Relation cannot be null when getting display text");
		try {
			switch (type) {
                        case "workflowEntity":
				Check.notNull(relation.getWorkflowEntity(), "Workflow cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getWorkflowEntity());
			case "fromStatus":
				Check.notNull(relation.getFromStatus(), "From status cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getFromStatus());
			case "toStatus":
				Check.notNull(relation.getToStatus(), "To status cannot be null");
				return CColorUtils.getDisplayTextFromEntity(relation.getToStatus());
			case "roles":
				return relation.getRoles() != null && !relation.getRoles().isEmpty() ? relation.getRoles().stream()
						.map(r -> CColorUtils.getDisplayTextFromEntity(r)).collect(Collectors.joining(", ")) : "All Roles";
			default:
				return "";
			}
		} catch (final Exception e) {
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
					relation.getId() == null ? workflowStatusRelationService.addStatusTransition(relation.getWorkflowEntity(),
							relation.getFromStatus(), relation.getToStatus(), relation.getRoles()) : workflowStatusRelationService.save(relation);
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
			if (!isWorkflowMaster()) {
				CGrid.styleColumnHeader(grid.addComponentColumn(relation -> {
					try {
						return new CLabelEntity(relation.getWorkflowEntity());
					} catch ( final Exception e) {
						LOGGER.error("Failed to create workflow component.");
                                                return new Span(getDisplayText(relation, "workflowEntity"));
					}
				}).setAutoWidth(true).setSortable(true), "Workflow");
			}
			CGrid.styleColumnHeader(grid.addComponentColumn(relation -> {
				try {
					return new CLabelEntity(relation.getFromStatus());
				} catch ( final Exception e) {
					LOGGER.error("Failed to create from status component.");
					return new Span(getDisplayText(relation, "fromStatus"));
				}
			}).setAutoWidth(true).setSortable(true), "From Status");
			CGrid.styleColumnHeader(grid.addComponentColumn(relation -> {
				try {
					return new CLabelEntity(relation.getToStatus());
				} catch ( final Exception e) {
					LOGGER.error("Failed to create to status component.");
					return new Span(getDisplayText(relation, "toStatus"));
				}
			}).setAutoWidth(true).setSortable(true), "To Status");
			CGrid.styleColumnHeader(grid.addComponentColumn(relation -> {
				try {
					if (relation.getRoles() != null && !relation.getRoles().isEmpty()) {
						final HorizontalLayout rolesLayout =
								new HorizontalLayout();
						rolesLayout.setSpacing(true);
						for (int i = 0; i < relation.getRoles().size(); i++) {
							rolesLayout.add(new CLabelEntity(relation.getRoles().get(i)));
							if (i < relation.getRoles().size() - 1) {
								rolesLayout.add(new Span(", "));
							}
						}
						return rolesLayout;
					}
					final Span span = new Span("All Roles");
					span.getStyle().set("font-style", "italic");
					span.getStyle().set("color", "#666");
					return span;
				} catch ( final Exception e) {
					LOGGER.error("Failed to create roles component.");
					return new Span(getDisplayText(relation, "roles"));
				}
			}).setAutoWidth(true).setSortable(true), "Roles");
		} catch (final Exception e) {
			LOGGER.error("Failed to setup grid.");
			throw e;
		}
	}
}
