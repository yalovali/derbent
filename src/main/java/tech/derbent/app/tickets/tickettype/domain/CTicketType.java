package tech.derbent.app.tickets.tickettype.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.app.projects.domain.CProject;

@Entity
@Table (name = "ctickettype", uniqueConstraints = @UniqueConstraint (columnNames = {
		"name", "project_id"
}))
@AttributeOverride (name = "id", column = @Column (name = "ctickettype_id"))
public class CTicketType extends CTypeEntity<CTicketType> {

	public static final String DEFAULT_COLOR = "#FFC107";
	public static final String DEFAULT_ICON = "vaadin:ticket";
	public static final String ENTITY_TITLE_PLURAL = "Ticket Types";
	public static final String ENTITY_TITLE_SINGULAR = "Ticket Type";
	public static final String VIEW_NAME = "Ticket Type Management";

	/** Default constructor for JPA. */
	public CTicketType() {
		super();
	}

	public CTicketType(final String name, final CProject project) {
		super(CTicketType.class, name, project);
	}
}
