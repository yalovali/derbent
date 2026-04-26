package tech.derbent.plm.gnnt.gnntviewentity.domain;

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

/** Project-scoped configuration entity for the new Gnnt board experience.
 * <p>
 * The entity itself is lightweight; the actual board is rendered through the transient self placeholder, following the active Kanban dynamic-page
 * pattern.
 * </p>
 */
@Entity
@Table (name = "cgnntview")
@AttributeOverride (name = "id", column = @Column (name = "gnntview_id"))
public class CGnntViewEntity extends CEntityOfProject<CGnntViewEntity> {

	public static final String DEFAULT_COLOR = "#204382";
	public static final String DEFAULT_ICON = "vaadin:line-chart"; // Timeline-style chart fits Gantt/Gnnt screens better
	public static final String ENTITY_TITLE_PLURAL = "Gannt Charts";
	public static final String ENTITY_TITLE_SINGULAR = "Gannt Chart";
	public static final String VIEW_NAME = "Gannt Chart View";
	@Transient
	@AMetaData (
			displayName = "Gnnt Board", required = false, readOnly = false, description = "Timeline board for agile hierarchy items", hidden = false,
			createComponentMethod = "createGnntBoardComponent", dataProviderBean = "pageservice", captionVisible = false
	)
	private final CGnntViewEntity gnntBoard = null;
	@Column (nullable = false, length = 16)
	@Enumerated (EnumType.STRING)
	@AMetaData (
			displayName = "Grid Type", required = true, readOnly = false, description = "Select whether this Gnnt board uses flat or tree mode",
			hidden = false
	)
	private EGnntGridType gridType = EGnntGridType.FLAT;

	/** Default constructor for JPA. */
	protected CGnntViewEntity() {
		super();
	}

	public CGnntViewEntity(final String name, final CProject<?> project) {
		super(CGnntViewEntity.class, name, project);
		initializeDefaults();
	}

	public CGnntViewEntity getGnntBoard() { return this; }

	public EGnntGridType getGridType() { return gridType != null ? gridType : EGnntGridType.FLAT; }

	private final void initializeDefaults() {
		gridType = EGnntGridType.FLAT;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	public void setGridType(final EGnntGridType gridType) { this.gridType = gridType != null ? gridType : EGnntGridType.FLAT; }
}
