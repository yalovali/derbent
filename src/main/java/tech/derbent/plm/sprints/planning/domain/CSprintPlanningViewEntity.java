package tech.derbent.plm.sprints.planning.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.gnnt.gnntviewentity.domain.EGnntGridType;

/**
 * CSprintPlanningViewEntity - Project scoped configuration entity for the Sprint Planning Board (v2).
 *
 * <p>The entity is intentionally lightweight. The board UI is rendered through a transient
 * placeholder field (same pattern as {@code CGnntViewEntity}) so we can iterate on the
 * planning experience without changing the core sprint domain model.</p>
 */
@Entity
@Table(name = "csprintplanningview")
@AttributeOverride(name = "id", column = @Column(name = "sprintplanningview_id"))
public class CSprintPlanningViewEntity extends CEntityOfProject<CSprintPlanningViewEntity> {

	public static final String DEFAULT_COLOR = "#2E7D32"; // green - planning/iteration
	public static final String DEFAULT_ICON = "vaadin:clipboard-text";
	public static final String ENTITY_TITLE_PLURAL = "Sprint Planning Views";
	public static final String ENTITY_TITLE_SINGULAR = "Sprint Planning View";
	public static final String VIEW_NAME = "Sprint Planning Views View";

	@Transient
	@AMetaData(
			displayName = "Sprint Planning Board", required = false, readOnly = false,
			description = "Timeline + drag/drop sprint planning board (v2)", hidden = false,
			createComponentMethod = "createSprintPlanningBoardComponent", dataProviderBean = "pageservice", captionVisible = false)
	private final CSprintPlanningViewEntity sprintPlanningBoard = null;

	@Column(nullable = false, length = 16)
	@Enumerated(EnumType.STRING)
	@AMetaData(
			displayName = "Backlog Grid Type", required = true, readOnly = false,
			description = "Backlog rendering mode (flat or tree)", hidden = false)
	private EGnntGridType backlogGridType = EGnntGridType.TREE;

	/** Default constructor for JPA. */
	protected CSprintPlanningViewEntity() {
		super();
	}

	public CSprintPlanningViewEntity(final String name, final CProject<?> project) {
		super(CSprintPlanningViewEntity.class, name, project);
		initializeDefaults();
	}

	public CSprintPlanningViewEntity getSprintPlanningBoard() {
		// Return the entity itself - binder uses this getter to bind the transient placeholder.
		return this;
	}

	public EGnntGridType getBacklogGridType() {
		return backlogGridType != null ? backlogGridType : EGnntGridType.TREE;
	}

	private final void initializeDefaults() {
		// Default to TREE because sprint planning benefits from ancestor context (Epic → Story → Task).
		backlogGridType = EGnntGridType.TREE;
		// Context-dependent defaults (project/company/status) are set by the service.
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setBacklogGridType(final EGnntGridType backlogGridType) {
		this.backlogGridType = backlogGridType != null ? backlogGridType : EGnntGridType.TREE;
	}
}
