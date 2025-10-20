package tech.derbent.app.roles.domain;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityNamed;
import tech.derbent.app.companies.domain.CCompany;

@MappedSuperclass
public abstract class CNonProjectType<EntityClass> extends CEntityNamed<EntityClass> {

	// Many risks belong to one project
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "company_id", nullable = false)
	@AMetaData (displayName = "Company", required = true, readOnly = true, description = "Company of this entity", hidden = false, order = 10)
	private CCompany company;
	@Column (nullable = false)
	@AMetaData (
			displayName = "Non Deletable", required = false, readOnly = false, defaultValue = "true",
			description = "Whether this type entity cannot be deleted by users (system configuration)", hidden = false, order = 82
	)
	private boolean attributeNonDeletable = true;
	@Column (name = "color", nullable = true, length = 7)
	@Size (max = 7)
	@AMetaData (
			displayName = "Color", required = false, readOnly = false, defaultValue = "#4A90E2", colorField = true,
			description = "Hex color code for type visualization (e.g., #4A90E2)", hidden = false, order = 3, maxLength = 7
	)
	private String color = "#4A90E2";
	@Column (name = "sort_order", nullable = false)
	@NotNull
	@AMetaData (
			displayName = "Sort Order", required = true, readOnly = false, defaultValue = "100", description = "Display order for type sorting",
			hidden = false, order = 4
	)
	private Integer sortOrder = 100;

	/** Default constructor for JPA. */
	protected CNonProjectType() {
		super();
		// Initialize with default values for JPA
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	/** Constructor with required fields.
	 * @param name    the name of the type entity
	 * @param company the project this type belongs to */
	public CNonProjectType(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name);
		this.company = company;
		color = "#4A90E2";
		sortOrder = 100;
		attributeNonDeletable = false;
	}

	@Override
	public boolean equals(final Object obj) {
		return super.equals(obj);
	}

	/** Gets whether this type entity is non-deletable.
	 * @return true if this entity cannot be deleted */
	public boolean getAttributeNonDeletable() { return attributeNonDeletable; }

	/** Gets the color code for this type.
	 * @return the hex color code */
	public String getColor() { return color; }

	/** Gets the company this type belongs to.
	 * @return the company */
	public CCompany getCompany() { return company; }

	/** Gets the sort order for this type.
	 * @return the sort order */
	public Integer getSortOrder() { return sortOrder; }

	@Override
	public int hashCode() {
		// Use the superclass hashCode method for consistency with equals method
		return super.hashCode();
	}

	/** Sets whether this type entity is non-deletable.
	 * @param attributeNonDeletable true if this entity cannot be deleted */
	public void setAttributeNonDeletable(final boolean attributeNonDeletable) {
		this.attributeNonDeletable = attributeNonDeletable;
	}

	/** Sets the color code for this type.
	 * @param color the hex color code to set */
	public void setColor(final String color) { this.color = color; }

	/** Sets the company this type belongs to.
	 * @param company the company to set */
	public void setCompany(final CCompany company) { this.company = company; }

	/** Sets the sort order for this type.
	 * @param sortOrder the sort order to set */
	public void setSortOrder(final Integer sortOrder) { this.sortOrder = sortOrder; }
}
