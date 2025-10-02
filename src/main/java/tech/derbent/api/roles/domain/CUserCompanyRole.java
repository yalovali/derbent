package tech.derbent.api.roles.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.companies.domain.CCompany;

@Entity
@Table (name = "cusercompanyrole", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cusercompanyrole_id"))
public class CUserCompanyRole extends CNonProjectType<CUserCompanyRole> {

	public static final String DEFAULT_COLOR = "#163f1d";
	public static final String DEFAULT_ICON = "vaadin:book";
	public static final int MAX_LENGTH_NAME = 255;
	public static final String VIEW_NAME = "User Company Roles View";
	@Column (name = "role", length = MAX_LENGTH_NAME, nullable = false)
	@AMetaData (
			displayName = "Role", required = true, readOnly = false, description = "Role identifier", hidden = false, order = 1,
			maxLength = MAX_LENGTH_NAME
	)
	private String role;

	public CUserCompanyRole() {
		super();
	}

	public CUserCompanyRole(final String name, CCompany company) {
		super(CUserCompanyRole.class, name, company);
	}

	@Override
	public void initializeAllFields() {
		// TODO Auto-generated method stub
	}
}
