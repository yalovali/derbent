package tech.derbent.plm.project.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.kanban.kanbanline.domain.CKanbanLine;

/** CProject_Derbent - Derbent-specific project with Kanban support. Layer: Domain (MVC) Active when: default profile or 'derbent' profile (NOT 'bab'
 * profile) */
@Entity
@DiscriminatorValue ("DERBENT")
public class CProject_Derbent extends CProject<CProject_Derbent> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#6B5FA7"; // CDE Purple - organizational entity
	public static final String DEFAULT_ICON = "vaadin:folder-open";
	public static final String ENTITY_TITLE_PLURAL = "Derbent Projects";
	public static final String ENTITY_TITLE_SINGULAR = "Derbent Project";
	public static final String VIEW_NAME = "Derbent Projects View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Project documentation and files", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "project_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this project", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "kanban_line_id")
	@AMetaData (
			displayName = "Kanban Line", required = false, readOnly = false, description = "Default Kanban line used to visualize project sprints",
			hidden = false
	)
	private CKanbanLine kanbanLine;

	/** Default constructor for JPA. */
	protected CProject_Derbent() {}

	public CProject_Derbent(final String name, final CCompany company) {
		super(CProject_Derbent.class, name, company);
		initializeDefaults();
	}

	@Override
	protected void copyEntityTo(final CEntityDB<?> target, @SuppressWarnings ("rawtypes") final CAbstractService serviceTarget,
			final CCloneOptions options) {
		super.copyEntityTo(target, serviceTarget, options);
		if (target instanceof final CProject_Derbent targetDerbent) {
			if (options.includesRelations()) {
				copyField(this::getKanbanLine, targetDerbent::setKanbanLine);
				copyCollection(this::getAttachments, collection -> targetDerbent.setAttachments(new HashSet<>(collection)), true);
				copyCollection(this::getComments, collection -> targetDerbent.setComments(new HashSet<>(collection)), true);
			}
		}
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	public CKanbanLine getKanbanLine() { return kanbanLine; }

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) { this.comments = comments; }

	@Override
	public void setCompany(final CCompany company) {
		Check.notNull(company, "Company cannot be null for a project");
		super.setCompany(company);
		if (kanbanLine != null) {
			Check.isSameCompany(this, kanbanLine);
		}
	}

	public void setKanbanLine(final CKanbanLine kanbanLine) {
		if (kanbanLine != null) {
			Check.isSameCompany(this, kanbanLine);
		}
		this.kanbanLine = kanbanLine;
		updateLastModified();
	}
}
