package tech.derbent.api.roles.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

@MappedSuperclass
public abstract class CRole<EntityClass> extends CEntityOfCompany<EntityClass> {

	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (name = "sort_order", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false
	)
	private Integer sortOrder = 100;

	protected CRole() {
		super();
		initializeDefaults();
	}

	protected CRole(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name, company);
		initializeDefaults();
	}

	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	public String getColor() { return color; }

	public Integer getSortOrder() { return sortOrder; }

	private final void initializeDefaults() {
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	public void setAttributeNonDeletable(final boolean attributeNonDeletable) { this.attributeNonDeletable = attributeNonDeletable; }

	public void setColor(final String color) { this.color = color; }

	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }
}
