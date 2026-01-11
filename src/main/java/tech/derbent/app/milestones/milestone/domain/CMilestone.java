package tech.derbent.app.milestones.milestone.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.milestones.milestonetype.domain.CMilestoneType;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cmilestone\"")
@AttributeOverride (name = "id", column = @Column (name = "milestone_id"))
public class CMilestone extends CProjectItem<CMilestone> implements IHasStatusAndWorkflow<CMilestone> {

	public static final String DEFAULT_COLOR = "#4B4382"; // CDE Titlebar Purple - key achievements
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String ENTITY_TITLE_PLURAL = "Milestones";
	public static final String ENTITY_TITLE_SINGULAR = "Milestone";
	public static final String VIEW_NAME = "Milestone View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Milestone Type", required = false, readOnly = false, description = "Type category of the milestone", hidden = false,
			 dataProviderBean = "CMilestoneTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CMilestoneType entityType;

	/** Default constructor for JPA. */
	public CMilestone() {
		super();
		initializeDefaults();
	}

	public CMilestone(final String name, final CProject project) {
		super(CMilestone.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CMilestoneType.class, "Type entity must be an instance of CMilestoneType");
		Check.notNull(getProject(), "Project must be set before assigning milestone type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning milestone type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning milestone type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match milestone project company id "
						+ getProject().getCompany().getId());
		entityType = (CMilestoneType) typeEntity;
		updateLastModified();
	}
}
