package tech.derbent.app.risks.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "crisktype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "crisktype_id"))
public class CRiskType extends CTypeEntity<CRiskType> {

	public static final String DEFAULT_COLOR = "#a712b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String VIEW_NAME = "Risk Type Management";

	/** Default constructor for JPA. */
	public CRiskType() {
		super();
	}

	public CRiskType(final String name, final CProject project) {
		super(CRiskType.class, name, project);
	}

	@Override
	public void initializeAllFields() {}
}
