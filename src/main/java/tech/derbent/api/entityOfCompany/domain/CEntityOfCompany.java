package tech.derbent.api.entityOfCompany.domain;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.app.companies.domain.CCompany;

@MappedSuperclass
public abstract class CEntityOfCompany<EntityClass> extends CEntityNamed<EntityClass> {

	@ManyToOne (fetch = FetchType.LAZY)
	@JoinColumn (name = "company_id", nullable = true)
	@AMetaData (
			displayName = "Company", required = false, readOnly = false, description = "User's company", hidden = false, order = 15,
			setBackgroundFromColor = true, useIcon = true
	)
	private CCompany company;

	/** Default constructor for JPA. */
	protected CEntityOfCompany() {
		super();
		// Initialize with default values for JPA
		company = null;
	}

	public CEntityOfCompany(final Class<EntityClass> clazz, final String name, final CCompany company) {
		super(clazz, name);
		this.company = company;
	}

	public CCompany getCompany() { return company; }

	@Override
	public void initializeAllFields() {
		if (company != null) {
			company.getName(); // Trigger company loading
		}
	}

	public void setCompany(final CCompany company) { this.company = company; }
}
