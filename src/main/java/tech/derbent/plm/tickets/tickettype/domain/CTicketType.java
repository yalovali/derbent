package tech.derbent.plm.tickets.tickettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.CTypeEntity;

@Entity
@Table (name = "ctickettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "company_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "ctickettype_id"))
public class CTicketType extends CTypeEntity<CTicketType> {

	public static final String DEFAULT_COLOR = "#3A5791"; // Darker blue - ticket types
	public static final String DEFAULT_ICON = "vaadin:ticket";
	public static final String ENTITY_TITLE_PLURAL = "Ticket Types";
	public static final String ENTITY_TITLE_SINGULAR = "Ticket Type";
	public static final String VIEW_NAME = "Ticket Type Management";

	/** Default constructor for JPA. */
	/** Default constructor for JPA. */
	protected CTicketType() {
		super();
	}

	public CTicketType(final String name, final CCompany company) {
		super(CTicketType.class, name, company);
		initializeDefaults();
	}

	private final void initializeDefaults() {
		setColor(DEFAULT_COLOR);
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}
}
