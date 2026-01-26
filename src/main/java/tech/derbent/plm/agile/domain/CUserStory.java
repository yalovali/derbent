package tech.derbent.plm.agile.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.projects.domain.CProject;

@Entity
@Table (name = "cuserstory")
@AttributeOverride (name = "id", column = @Column (name = "userstory_id"))
public class CUserStory extends CAgileEntity<CUserStory, CUserStoryType> {

	public static final String DEFAULT_COLOR = "#1F8EFA";
	public static final String DEFAULT_ICON = "vaadin:comment";
	public static final String ENTITY_TITLE_PLURAL = "User Stories";
	public static final String ENTITY_TITLE_SINGULAR = "User Story";
	public static final String VIEW_NAME = "User Stories View";

	@ManyToOne
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "User Story Type", required = false, readOnly = false, description = "Type category of the user story", hidden = false,
			dataProviderBean = "CUserStoryTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CUserStoryType entityType;

	protected CUserStory() {
		super();
	}

	public CUserStory(final String name, final CProject<?> project) {
		super(CUserStory.class, name, project);
		initializeDefaults();
	}

	@Override
	protected CUserStoryType getTypedEntityType() { return entityType; }

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	public CUserStoryType getEntityTypeUserStory() { return entityType; }

	public void setEntityType(final CUserStoryType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
