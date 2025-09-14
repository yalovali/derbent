package tech.derbent.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.abstracts.annotations.AMetaData;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.view.CMasterSectionView;

@Entity
@Table (name = "cmastersection")
@AttributeOverride (name = "id", column = @Column (name = "master_section_id"))
public class CMasterSection extends CEntityOfProject<CMasterSection> {

	public static String getStaticEntityColorCode() { return getStaticIconColorCode(); }

	public static String getStaticIconColorCode() { return "#6f42c1"; }

	public static String getStaticIconFilename() { return "vaadin:viewport"; }

	public static Class<? extends CAbstractEntityDBPage<?>> getViewClassStatic() { return CMasterSectionView.class; }

	@Column (name = "section_db_name", nullable = true, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Section DB Name", required = true, readOnly = false, description = "Section DB Name", hidden = false, order = 10,
			maxLength = 200
	)
	private String sectionDBName;
	@Column (name = "section_type", nullable = false, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Section Type", required = true, readOnly = false, description = "Section Type", hidden = false, order = 10,
			maxLength = 200, dataProviderBean = "CMasterSectionService", dataProviderMethod = "getAvailableTypes"
	)
	private String sectionType;

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

	public String getSectionDBName() { return sectionDBName; }

	public void setSectionDBName(final String sectionDBName) { this.sectionDBName = sectionDBName; }

	public String getSectionType() { return sectionType; }

	public void setSectionType(final String sectionType) { this.sectionType = sectionType; }

	@Override
	public Class<? extends CAbstractEntityDBPage<?>> getViewClass() { // TODO Auto-generated method stub
		return CMasterSection.getViewClassStatic();
	}
}
