package tech.derbent.app.budgets.budget.domain;

import java.util.HashSet;
import java.util.Set;
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
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.budgets.budgettype.domain.CBudgetType;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;

@Entity
@Table (name = "\"cbudget\"")
@AttributeOverride (name = "id", column = @Column (name = "budget_id"))
public class CBudget extends CProjectItem<CBudget> implements IHasStatusAndWorkflow<CBudget>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#8B4513"; // X11 SaddleBrown - financial planning (darker)
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String ENTITY_TITLE_PLURAL = "Budgets";
	public static final String ENTITY_TITLE_SINGULAR = "Budget";
	public static final String VIEW_NAME = "Budget View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Budget Type", required = false, readOnly = false, description = "Type category of the budget", hidden = false,
			dataProviderBean = "CBudgetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CBudgetType entityType;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this budget", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "budget_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this budget", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CBudget() {
		super();
		initializeDefaults();
	}

	public CBudget(final String name, final CProject project) {
		super(CBudget.class, name, project);
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
	public CWorkflowEntity getWorkflow() {
		Check.notNull(entityType, "Entity type cannot be null when retrieving workflow");
		return entityType.getWorkflow();
	}

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
	}

	@Override
	public void setEntityType(CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CBudgetType.class, "Type entity must be an instance of CBudgetType");
		Check.notNull(getProject(), "Project must be set before assigning budget type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning budget type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning budget type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match budget project company id "
						+ getProject().getCompany().getId());
		entityType = (CBudgetType) typeEntity;
		updateLastModified();
	}
}
