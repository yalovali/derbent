package tech.derbent.gannt.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;

@Entity
@Table (name = "cganntview")
@AttributeOverride (name = "id", column = @Column (name = "ganntview_id"))
public class CGanntViewEntity extends CEntityOfProject<CGanntViewEntity> {

	public static final String DEFAULT_COLOR = "#fd7e14";
	public static final String DEFAULT_ICON = "vaadin:timeline";
	public static final String VIEW_NAME = "GanntEntity View";

	public CGanntViewEntity() {
		super();
	}

	public CGanntViewEntity(final String name, final CProject project) {
		super(CGanntViewEntity.class, name, project);
	}

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}
}
