package tech.derbent.plm.orders.type.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cordertype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "order_type_id"))
public class COrderType extends CTypeEntity<COrderType> {

	public static final String DEFAULT_COLOR = "#17a2b8";
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Order Types";
	public static final String ENTITY_TITLE_SINGULAR = "Order Type";
	public static final String VIEW_NAME = "Order Type View";

	/** Default constructor for JPA. */
										/** Default constructor for JPA. */
	protected COrderType() {}

	/** Constructor with name and company.
	 * @param name    the name of the order type
	 * @param company the company this type belongs to */
	public COrderType(final String name, final CCompany company) {
		super(COrderType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
