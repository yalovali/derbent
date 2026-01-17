package tech.derbent.app.assets.asset.domain;

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
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.api.workflow.domain.CWorkflowEntity;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.app.assets.assettype.domain.CAssetType;
import tech.derbent.app.attachments.domain.CAttachment;
import tech.derbent.app.attachments.domain.IHasAttachments;
import tech.derbent.app.comments.domain.CComment;
import tech.derbent.app.comments.domain.IHasComments;

@Entity
@Table (name = "\"casset\"")
@AttributeOverride (name = "id", column = @Column (name = "asset_id"))
public class CAsset extends CProjectItem<CAsset> implements IHasStatusAndWorkflow<CAsset>, IHasAttachments, IHasComments {

	public static final String DEFAULT_COLOR = "#708090"; // X11 SlateGray - owned items (darker)
	public static final String DEFAULT_ICON = "vaadin:briefcase";
	public static final String ENTITY_TITLE_PLURAL = "Assets";
	public static final String ENTITY_TITLE_SINGULAR = "Asset";
	private static final Logger LOGGER = LoggerFactory.getLogger(CAsset.class);
	public static final String VIEW_NAME = "Asset View";
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Asset Type", required = false, readOnly = false, description = "Type category of the asset", hidden = false, 
			dataProviderBean = "CAssetTypeService", setBackgroundFromColor = true, useIcon = true
	)
	private CAssetType entityType;
	// One-to-Many relationship with attachments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "asset_id")
	@AMetaData (
			displayName = "Attachments", required = false, readOnly = false, description = "Attachments for this asset", hidden = false,
			dataProviderBean = "CAttachmentService", createComponentMethod = "createComponent"
	)
	private Set<CAttachment> attachments = new HashSet<>();
	// One-to-Many relationship with comments - cascade delete enabled
	@OneToMany (cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
	@JoinColumn (name = "asset_id")
	@AMetaData (
			displayName = "Comments", required = false, readOnly = false, description = "Comments for this asset", hidden = false,
			dataProviderBean = "CCommentService", createComponentMethod = "createComponent"
	)
	private Set<CComment> comments = new HashSet<>();

	/** Default constructor for JPA. */
	public CAsset() {
		super();
		initializeDefaults();
	}

	public CAsset(final String name, final CProject project) {
		super(CAsset.class, name, project);
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
	public CAssetType getEntityType() { return entityType; }

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
	public void setEntityType(final CTypeEntity<?> typeEntity) {
		Check.notNull(typeEntity, "Type entity must not be null");
		Check.instanceOf(typeEntity, CAssetType.class, "Type entity must be an instance of CAssetType");
		Check.notNull(getProject(), "Project must be set before assigning asset type");
		Check.notNull(getProject().getCompany(), "Project company must be set before assigning asset type");
		Check.notNull(typeEntity.getCompany(), "Type entity company must be set before assigning asset type");
		Check.isTrue(typeEntity.getCompany().getId().equals(getProject().getCompany().getId()),
				"Type entity company id " + typeEntity.getCompany().getId() + " does not match asset project company id "
						+ getProject().getCompany().getId());
		entityType = (CAssetType) typeEntity;
		updateLastModified();
	}

	/**
	 * Creates a clone of this asset with the specified options.
	 * This implementation follows the recursive cloning pattern:
	 * 1. Calls parent's createClone() to handle inherited fields (CProjectItem)
	 * 2. Clones asset-specific fields based on options
	 * 3. Recursively clones collections (comments, attachments) if requested
	 * 
	 * Cloning behavior:
	 * - Basic fields (strings, numbers, enums) are always cloned
	 * - Workflow field is cloned only if options.isCloneWorkflow()
	 * - Comments collection is recursively cloned if options.includesComments()
	 * - Attachments collection is recursively cloned if options.includesAttachments()
	 * 
	 * @param options the cloning options determining what to clone
	 * @return a new instance of the asset with cloned data
	 * @throws CloneNotSupportedException if cloning fails
	 */
	@Override
	public CAsset createClone(final CCloneOptions options) throws CloneNotSupportedException {
		// Get parent's clone (CProjectItem -> CEntityOfProject -> CEntityNamed -> CEntityDB)
		final CAsset clone = super.createClone(options);

		// Clone entity type (asset type)
		clone.entityType = this.entityType;
		
		// Clone workflow if requested
		if (options.isCloneWorkflow() && this.getWorkflow() != null) {
			// Workflow is obtained via entityType.getWorkflow() - already cloned via entityType
		}
		
		// Clone comments if requested
		if (options.includesComments() && this.comments != null && !this.comments.isEmpty()) {
			clone.comments = new HashSet<>();
			for (final CComment comment : this.comments) {
				try {
					final CComment commentClone = comment.createClone(options);
					clone.comments.add(commentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone comment: {}", e.getMessage());
				}
			}
		}
		
		// Clone attachments if requested
		if (options.includesAttachments() && this.attachments != null && !this.attachments.isEmpty()) {
			clone.attachments = new HashSet<>();
			for (final CAttachment attachment : this.attachments) {
				try {
					final CAttachment attachmentClone = attachment.createClone(options);
					clone.attachments.add(attachmentClone);
				} catch (final Exception e) {
					LOGGER.warn("Could not clone attachment: {}", e.getMessage());
				}
			}
		}
		
		LOGGER.debug("Successfully cloned asset '{}' with options: {}", this.getName(), options);
		return clone;
	}
}
