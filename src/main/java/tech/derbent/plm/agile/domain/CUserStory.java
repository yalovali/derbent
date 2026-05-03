package tech.derbent.plm.agile.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.projects.domain.CProject;

/**
 * Leaf-oriented agile execution item.
 *
 * <p>User stories inherit the full agile work-item payload from {@link CAgileEntity}, so this class
 * keeps only the concrete type field needed for metadata-driven forms and workflow lookup.</p>
 */
@Entity
@Table (name = "cuserstory")
@PrimaryKeyJoinColumn (name = "userstory_id")
@DiscriminatorValue ("USER_STORY")
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
	}

	public CUserStory(final String name, final CProject<?> project) {
		super(CUserStory.class, name, project);
	}

	@Override
	public CUserStoryType getTypedEntityType() { return entityType; }
	
	@Override
	protected void setTypedEntityType(final CUserStoryType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

}
