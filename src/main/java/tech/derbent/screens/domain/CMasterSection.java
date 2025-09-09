package tech.derbent.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.view.CMasterSectionView;

@Entity
@Table (name = "cmastersection")
@AttributeOverride (name = "id", column = @Column (name = "master_section_id"))
public class CMasterSection extends CEntityOfProject<CMasterSection> {

	public static String getEntityColorCode() { return getIconColorCode(); }

	public static String getIconColorCode() { return "#6f42c1"; }

	public static String getIconFilename() { return "vaadin:viewport"; }

	public static Class<?> getViewClassStatic() { return CMasterSectionView.class; }

	@Column (name = "is_active", nullable = false)
	@AMetaData (
			displayName = "Active", required = false, readOnly = false, description = "Whether this screen definition is active", hidden = false,
			order = 20, defaultValue = "true"
	)
	private final Boolean isActive = true;

	public CMasterSection() {
		super();
	}

	public CMasterSection(final String name, final CProject project) {
		super(CMasterSection.class, name, project);
	}

	@Override
	public String getDisplayName() { // TODO Auto-generated method stub
		return "Master Section: " + getName();
	}
}
