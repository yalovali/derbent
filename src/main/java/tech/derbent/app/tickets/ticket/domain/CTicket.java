package tech.derbent.app.tickets.ticket.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.tickets.tickettype.domain.CTicketType;
import tech.derbent.app.workflow.domain.CWorkflowEntity;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflow;

@Entity
@Table (name = "\"cticket\"")
@AttributeOverride (name = "id", column = @Column (name = "ticket_id"))
public class CTicket extends CProjectItem<CTicket> implements IHasStatusAndWorkflow<CTicket> {

	public static final String DEFAULT_COLOR = "#FFB300";
	public static final String DEFAULT_ICON = "vaadin:ticket";
	public static final String VIEW_NAME = "Ticket View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Ticket Type", required = false, readOnly = false, description = "Type category of the ticket", hidden = false, 
			dataProviderBean = "CTicketTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CTicketType entityType;

	/** Default constructor for JPA. */
	public CTicket() {
		super();
		initializeDefaults();
	}

	public CTicket(final String name, final CProject project) {
		super(CTicket.class, name, project);
		initializeDefaults();
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.instanceOf(typeEntity, CTicketType.class, "Type entity must be an instance of CTicketType");
		entityType = (CTicketType) typeEntity;
		updateLastModified();
	}
}
