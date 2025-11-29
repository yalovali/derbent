package tech.derbent.app.projectincomes.projectincometype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cprojectincometype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprojectincometype_id"))
public class CProjectIncomeType extends CTypeEntity<CProjectIncomeType> {

	public static final String DEFAULT_COLOR = "#F5DEB3"; // X11 Wheat - income types
	public static final String DEFAULT_ICON = "vaadin:money-deposit";
	public static final String VIEW_NAME = "Project Income Type Management";

	/** Default constructor for JPA. */
	public CProjectIncomeType() {
		super();
	}

	public CProjectIncomeType(final String name, final CProject project) {
		super(CProjectIncomeType.class, name, project);
	}
}
