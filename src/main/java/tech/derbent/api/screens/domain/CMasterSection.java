package tech.derbent.api.screens.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;

@Entity
@Table (name = "cmastersection")
@AttributeOverride (name = "id", column = @Column (name = "master_section_id"))
public class CMasterSection extends CEntityOfProject<CMasterSection> {

	public static final String DEFAULT_COLOR = "#000a0e";
	public static final String DEFAULT_ICON = "vaadin:cloud";
	public static final String ENTITY_TITLE_PLURAL = "Master Sections";
	public static final String ENTITY_TITLE_SINGULAR = "Master Section";
	public static final String VIEW_NAME = "Master Section View";
	@Column (name = "section_db_name", nullable = true, length = 200)
	@Size (max = 200)
	@AMetaData (displayName = "Section DB Name", required = true, readOnly = false, description = "Section DB Name", hidden = false, maxLength = 200)
	private String sectionDBName;
	@Column (name = "section_type", nullable = false, length = 200)
	@Size (max = 200)
	@AMetaData (
			displayName = "Section Type", required = true, readOnly = false, description = "Section Type", hidden = false, maxLength = 200,
			dataProviderBean = "CMasterSectionService", dataProviderMethod = "getComboValuesOfType"
	)
	private String sectionType = "";

	protected CMasterSection() {}

	public CMasterSection(final String name, final CProject<?> project) {
		super(CMasterSection.class, name, project);
		initializeDefaults();
	}

	public String getSectionDBName() { return sectionDBName; }

	public String getSectionType() { return sectionType; }

	private final void initializeDefaults() {
		// sectionDBName and sectionType now initialized at declaration
		// Note: Screen entities are internal framework classes, not registered in entity registry
	}

	public void setSectionDBName(final String sectionDBName) { this.sectionDBName = sectionDBName; }

	public void setSectionType(final String sectionType) { this.sectionType = sectionType; }
}
