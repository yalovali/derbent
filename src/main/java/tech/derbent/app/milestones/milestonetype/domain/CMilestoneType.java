package tech.derbent.app.milestones.milestonetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cmilestonetype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cmilestonetype_id"))
public class CMilestoneType extends CTypeEntity<CMilestoneType> {

	public static final String DEFAULT_COLOR = "#2196F3";
	public static final String DEFAULT_ICON = "vaadin:flag";
	public static final String ENTITY_TITLE_PLURAL = "Milestone Types";
	public static final String ENTITY_TITLE_SINGULAR = "Milestone Type";
	public static final String VIEW_NAME = "Milestone Type Management";

	/** Default constructor for JPA. */
	public CMilestoneType() {
		super();
	}

	public CMilestoneType(final String name, final CProject project) {
		super(CMilestoneType.class, name, project);
	}
}
