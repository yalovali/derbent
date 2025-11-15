package tech.derbent.app.assets.assettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cassettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cassettype_id"))
public class CAssetType extends CTypeEntity<CAssetType> {

	public static final String DEFAULT_COLOR = "#FF5722";
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String VIEW_NAME = "Asset Type Management";

	/** Default constructor for JPA. */
	public CAssetType() {
		super();
	}

	public CAssetType(final String name, final CProject project) {
		super(CAssetType.class, name, project);
	}

	@Override
	public void initializeAllFields() {}
}
