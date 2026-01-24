package tech.derbent.plm.links.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.CEntityRegistry;

/** CLink - Company-scoped domain entity representing bidirectional links between entities. Links are bidirectional: creating a link from A to B
 * automatically creates a reverse link from B to A. This ensures consistency and allows navigation in both directions. The link stores entity type as
 * String (simple class name) and entity ID as Long, making it flexible to work with any entity that implements IHasLinks. Pattern:
 * Unidirectional @OneToMany from parent entities. Parent entities (Activity, Risk, Meeting, Issue, etc.) have:
 * @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
 * @JoinColumn(name = "source_entity_activity_id") // or risk_id, issue_id, etc. private Set<CLink> links = new HashSet<>(); CLink has NO
 *                  back-reference to parents (clean unidirectional pattern). Layer: Domain (MVC) */
@Entity
@Table (name = "clink")
@AttributeOverride (name = "id", column = @Column (name = "link_id"))
public class CLink extends CEntityOfCompany<CLink> {

	public static final String DEFAULT_COLOR = "#8B4513"; // X11 SaddleBrown - connection
	public static final String DEFAULT_ICON = "vaadin:connect";
	public static final String ENTITY_TITLE_PLURAL = "Links";
	public static final String ENTITY_TITLE_SINGULAR = "Link";
	private static final Logger LOGGER = LoggerFactory.getLogger(CLink.class);
	public static final String VIEW_NAME = "Links View";
	// Description/notes about the link
	@Column (name = "link_description", nullable = true, length = 500)
	@Size (max = 500)
	@AMetaData (
			displayName = "Description", required = false, readOnly = false, description = "Optional description of the link", hidden = false,
			maxLength = 500
	)
	private String description;
	// Link type for categorization (e.g., "Related", "Depends On", "Blocks", etc.)
	@Column (name = "link_type", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Link Type", required = false, readOnly = false, description = "Type/category of the link", hidden = false, maxLength = 50
	)
	private String linkType;
	// Source entity ID
	@Column (name = "source_entity_id", nullable = false)
	@NotNull
	@AMetaData (displayName = "Source ID", required = true, readOnly = true, description = "ID of the source entity", hidden = false)
	private Long sourceEntityId;
	// Source entity type (simple class name, e.g., "CActivity", "CRisk")
	@Column (name = "source_entity_type", nullable = false, length = 100)
	@NotNull
	@Size (max = 100)
	@AMetaData (displayName = "Source Type", required = true, readOnly = true, description = "Type of the source entity", hidden = false)
	private String sourceEntityType;
	// Target entity ID
	@Column (name = "target_entity_id", nullable = false)
	@NotNull
	@AMetaData (displayName = "Target ID", required = true, readOnly = false, description = "ID of the target entity", hidden = false)
	private Long targetEntityId;
	// Target entity type (simple class name, e.g., "CActivity", "CRisk")
	@Column (name = "target_entity_type", nullable = false, length = 100)
	@NotNull
	@Size (max = 100)
	@AMetaData (displayName = "Target Type", required = true, readOnly = false, description = "Type of the target entity", hidden = false)
	private String targetEntityType;

	/** Default constructor for JPA. */
	public CLink() {
		super();
		initializeDefaults();
	}

	/** Constructor for creating a link between two entities.
	 * @param sourceType type of the source entity
	 * @param sourceId   ID of the source entity
	 * @param targetType type of the target entity
	 * @param targetId   ID of the target entity
	 * @param linkType   optional link type/category */
	public CLink(final String sourceType, final Long sourceId, final String targetType, final Long targetId, final String linkType) {
		super(CLink.class, "link", null);
		sourceEntityType = sourceType;
		sourceEntityId = sourceId;
		targetEntityType = targetType;
		targetEntityId = targetId;
		this.linkType = linkType;
		initializeDefaults();
	}

	/** Copy link fields to target entity.
	 * @param target        the target entity
	 * @param serviceTarget the service for the target entity
	 * @param options       copy options to control copying behavior */
	@Override
	protected void copyEntityTo(final CEntityDB<?> target,
			@SuppressWarnings ("rawtypes") final tech.derbent.api.entity.service.CAbstractService serviceTarget, final CCloneOptions options) {
		// Call parent first
		super.copyEntityTo(target, serviceTarget, options);
		// Type-check and cast
		if (target instanceof CLink) {
			final CLink targetLink = (CLink) target;
			// Copy basic fields
			copyField(this::getSourceEntityType, targetLink::setSourceEntityType);
			copyField(this::getSourceEntityId, targetLink::setSourceEntityId);
			copyField(this::getTargetEntityType, targetLink::setTargetEntityType);
			copyField(this::getTargetEntityId, targetLink::setTargetEntityId);
			copyField(this::getLinkType, targetLink::setLinkType);
			copyField(this::getDescription, targetLink::setDescription);
			LOGGER.debug("Successfully copied link fields to target");
		}
	}

	@Override
	public String getDescription() { return description; }

	public String getLinkType() { return linkType; }

	public Long getSourceEntityId() { return sourceEntityId; }

	/** Get the display name for the source entity.
	 * @return entity name or fallback string */
	public String getSourceEntityName() {
		if (sourceEntityType == null || sourceEntityId == null) {
			return "Unknown";
		}
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(sourceEntityType);
			final String displayName = CEntityRegistry.getEntityTitleSingular(entityClass);
			return displayName + " #" + sourceEntityId;
		} catch (final Exception e) {
			LOGGER.debug("Could not load source entity name: {}", e.getMessage());
			return sourceEntityType + " #" + sourceEntityId;
		}
	}

	public String getSourceEntityType() { return sourceEntityType; }

	public Long getTargetEntityId() { return targetEntityId; }

	/** Get the display name for the target entity. Attempts to load the entity and get its name.
	 * @return entity name or fallback string */
	public String getTargetEntityName() {
		if (targetEntityType == null || targetEntityId == null) {
			return "Unknown";
		}
		try {
			// Try to get entity name from registry
			final Class<?> entityClass = CEntityRegistry.getEntityClass(targetEntityType);
			final String displayName = CEntityRegistry.getEntityTitleSingular(entityClass);
			return displayName + " #" + targetEntityId;
		} catch (final Exception e) {
			LOGGER.debug("Could not load target entity name: {}", e.getMessage());
			return targetEntityType + " #" + targetEntityId;
		}
	}

	public String getTargetEntityType() { return targetEntityType; }

	private final void initializeDefaults() {
		linkType = "Related";
		description = "";
		linkType = "";
		sourceEntityId = null;
		sourceEntityType = "";
		targetEntityId = null;
		targetEntityType = "";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setDescription(final String description) { this.description = description; }

	public void setLinkType(final String linkType) { this.linkType = linkType; }

	public void setSourceEntityId(final Long sourceEntityId) { this.sourceEntityId = sourceEntityId; }

	public void setSourceEntityType(final String sourceEntityType) { this.sourceEntityType = sourceEntityType; }

	public void setTargetEntityId(final Long targetEntityId) { this.targetEntityId = targetEntityId; }

	public void setTargetEntityType(final String targetEntityType) { this.targetEntityType = targetEntityType; }

	@Override
	public String toString() {
		return String.format("CLink{id=%d, %s#%d -> %s#%d, type=%s}", getId(), sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
				linkType);
	}
}
