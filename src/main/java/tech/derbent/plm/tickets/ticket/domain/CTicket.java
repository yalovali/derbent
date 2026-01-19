package tech.derbent.plm.tickets.ticket.domain;

import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;
import tech.derbent.plm.tickets.tickettype.domain.CTicketType;

@Entity
@Table (name = "\"cticket\"")
@AttributeOverride (name = "id", column = @Column (name = "ticket_id"))
public class CTicket extends CProjectItem<CTicket> implements IHasStatusAndWorkflow<CTicket>, IHasAttachments, IHasComments, IHasLinks {

	public static final String DEFAULT_COLOR = "#3A5791"; // Darker blue - support items
	public static final String DEFAULT_ICON = "vaadin:ticket";
	public static final String ENTITY_TITLE_PLURAL = "Tickets";
	public static final String ENTITY_TITLE_SINGULAR = "Ticket";
	private static final Logger LOGGER = LoggerFactory.getLogger(CTicket.class);
	public static final String VIEW_NAME = "Ticket View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this ticket", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this ticket", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Ticket Type", required = false, readOnly = false, description = "Type category of the ticket", hidden = false,
			dataProviderBean = "CTicketTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CTicketType entityType;
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "ticket_id")
	@AMetaData (
			displayName = "Links", required = false, readOnly = false, description = "Related entities linked to this cticket", hidden = false,
			dataProviderBean = "CLinkService", createComponentMethod = "createComponent"
	)
	private Set<CLink> links = new HashSet<>();

	/** Default constructor for JPA. */
	public CTicket() {
		super();
		initializeDefaults();
	}

	public CTicket(final String name, final CProject<?> project) {
		super(CTicket.class, name, project);
		initializeDefaults();
	}

	@Override
	public Set<CAttachment> getAttachments() {
		if (attachments == null) {
			attachments = new HashSet<>();
		}
		return attachments;
	}

	@Override
	public Set<CComment> getComments() {
		if (comments == null) {
			comments = new HashSet<>();
		}
		return comments;
	}

	@Override
	public CTypeEntity<?> getEntityType() { return entityType; }

	@Override
	public Set<CLink> getLinks() {
		if (links == null) {
			links = new HashSet<>();
		}
		return links;
	}

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
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CTicketType.class, "Type entity must be an instance of CTicketType");
		Check.notNull(getProject(), "Project must be set before assigning ticket type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning ticket type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning ticket type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()), "Type entity company id "
				+ typeEntity.getCompany().getId() + " does not match ticket project company id " + getProject().getCompany().getId());
		entityType = (CTicketType) typeEntity;
		updateLastModified();
	}

	@Override
	public void setLinks(final Set<CLink> links) { this.links = links; }
}
