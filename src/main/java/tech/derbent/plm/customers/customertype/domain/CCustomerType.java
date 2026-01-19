package tech.derbent.plm.customers.customertype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table(name = "ccustomertype", uniqueConstraints = @UniqueConstraint(columnNames = {
		"name", "company_id"
}))
@AttributeOverride(name = "id", column = @Column(name = "ccustomertype_id"))
public class CCustomerType extends CTypeEntity<CCustomerType> {

	public static final String DEFAULT_COLOR = "#4169E1"; // RoyalBlue - customer types
	public static final String DEFAULT_ICON = "vaadin:users";
	public static final String ENTITY_TITLE_PLURAL = "Customer Types";
	public static final String ENTITY_TITLE_SINGULAR = "Customer Type";
	public static final String VIEW_NAME = "Customer Type Management";

	/** Default constructor for JPA. */
	public CCustomerType() {
		super();
	}

	public CCustomerType(final String name, final CCompany company) {
		super(CCustomerType.class, name, company);
	}
}
