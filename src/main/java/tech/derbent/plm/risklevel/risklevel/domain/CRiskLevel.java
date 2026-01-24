package tech.derbent.plm.risklevel.risklevel.domain;

import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.attachments.domain.CAttachment;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

@Entity
@Table (name = "\"crisklevel\"") // Using quoted identifiers for PostgreSQL
@AttributeOverride (name = "id", column = @Column (name = "risklevel_id"))
public class CRiskLevel extends CProjectItem<CRiskLevel> implements IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#7A6E58"; // Darker border - risk levels
	public static final String DEFAULT_ICON = "vaadin:chart-3d";
	public static final String ENTITY_TITLE_PLURAL = "Risk Levels";
	public static final String ENTITY_TITLE_SINGULAR = "Risk Level";
	public static final String VIEW_NAME = "Risk Levels View";
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "risklevel_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "File attachments for this entity", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "risklevel_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this entity", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();
	@Column (nullable = true)
	@AMetaData (
			displayName = "Risk Level", required = false, readOnly = false, defaultValue = "1", description = "Numeric risk level indicator (1-10)",
			hidden = false
	)
	private Integer riskLevel;

	/** Default constructor for JPA. */
	public CRiskLevel() {
		super();
		initializeDefaults();
	}

	public CRiskLevel(final String name, final CProject<?> project) {
		super(CRiskLevel.class, name, project);
		initializeDefaults();
	}

	// IHasAttachments interface methods
	@Override
	public Set<CAttachment> getAttachments() { return attachments; }

	// IHasComments interface methods
	@Override
	public Set<CComment> getComments() { return comments; }

	public Integer getRiskLevel() { return riskLevel; }

	private final void initializeDefaults() {
		riskLevel = 1;
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setAttachments(final Set<CAttachment> attachments) { this.attachments = attachments; }

	@Override
	public void setComments(final Set<CComment> comments) {
		this.comments = comments;
		updateLastModified();
	}

	public void setRiskLevel(final Integer riskLevel) {
		this.riskLevel = riskLevel;
		updateLastModified();
	}
}
