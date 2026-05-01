package tech.derbent.plm.invoices.invoicetype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "cinvoicetype", uniqueConstraints = @jakarta.persistence.UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "cinvoicetype_id"))
public class CInvoiceType extends CTypeEntity<CInvoiceType> {

	public static final String DEFAULT_COLOR = "#FFD700"; // Gold - invoice types
	public static final String DEFAULT_ICON = "vaadin:tag";
	public static final String ENTITY_TITLE_PLURAL = "Invoice Types";
	public static final String ENTITY_TITLE_SINGULAR = "Invoice Type";
	public static final String VIEW_NAME = "Invoice Types View";

	/** Default constructor for JPA. */
	protected CInvoiceType() {}

	public CInvoiceType(final String name, final CCompany company) {
		super(CInvoiceType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		setLevel(-1);
		setCanHaveChildren(false);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
