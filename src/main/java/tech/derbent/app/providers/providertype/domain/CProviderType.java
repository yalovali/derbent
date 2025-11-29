package tech.derbent.app.providers.providertype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "cprovidertype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cprovidertype_id"))
public class CProviderType extends CTypeEntity<CProviderType> {

	public static final String DEFAULT_COLOR = "#B5B5B5"; // CDE Background Gray - provider types
	public static final String DEFAULT_ICON = "vaadin:handshake";
	public static final String ENTITY_TITLE_PLURAL = "Provider Types";
	public static final String ENTITY_TITLE_SINGULAR = "Provider Type";
	public static final String VIEW_NAME = "Provider Type Management";

	/** Default constructor for JPA. */
	public CProviderType() {
		super();
	}

	public CProviderType(final String name, final CProject project) {
		super(CProviderType.class, name, project);
	}
}
