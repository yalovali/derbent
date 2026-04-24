package tech.derbent.api.parentrelation.domain;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.ProxyUtils;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.COneToOneRelationBase;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.registry.CEntityRegistry;

/** CParentRelation - Hierarchy tracking component owned by any entity (CActivity/CMeeting/etc.).
 *
 * <p>
 * KEYWORDS: HierarchyRelation, ParentRelation, parentItemId, parentItemType, ProxyUtils.getUserClass, setParent, clearParent,
 * placeHolder_createComponentParentChildren, CComponentAgileChildren
 * </p>
 * <p>
 * <strong>OWNERSHIP AND LIFECYCLE:</strong>
 * </p>
 * <p>
 * Parent relations are OWNED by their parent entities via @OneToOne with CASCADE.ALL and orphanRemoval=true. This means:
 * </p>
 * <ul>
 * <li>Parent relations are created ONCE when entity is created</li>
 * <li>Parent relations are NEVER deleted independently - only when owner is deleted</li>
 * <li>Parent relations are NEVER replaced - only their properties are modified</li>
 * <li>Deleting a parent relation will CASCADE DELETE its owner entity</li>
 * </ul>
 * <p>
 * <strong>HIERARCHY SEMANTICS (LEVEL-BASED):</strong>
 * </p>
 * <p>
 * The parentItem field determines the hierarchy relationship using CTypeEntity.getLevel():
 * </p>
 * <ul>
 * <li><strong>Level 0 (e.g. Epic)</strong>: Cannot have a parent (must be root level, parentItem = NULL)</li>
 * <li><strong>Level N &gt; 0 (e.g. Feature, UserStory)</strong>: Parent must have level N-1</li>
 * <li><strong>Level -1 (leaf entities)</strong>: Can have any non-leaf entity as parent</li>
 * </ul>
 * <p>
 * <strong>DATA STORAGE:</strong>
 * </p>
 * <p>
 * Stores hierarchy data (parent item reference). Enforces level-based parent constraints for all entity types.
 * NOTE: DB table name kept as "cagile_parent_relation" for backward compatibility.
 * </p>
 * @see tech.derbent.api.interfaces.IHasParentRelation */
@Entity
@Table (name = "cagile_parent_relation")
@AttributeOverride (name = "id", column = @Column (name = "agile_parent_relation_id"))
public class CParentRelation extends COneToOneRelationBase<CParentRelation> {

	public static final String DEFAULT_COLOR = "#8B7355"; // OpenWindows Border - hierarchy relations
	public static final String DEFAULT_ICON = "vaadin:cluster";
	public static final String ENTITY_TITLE_PLURAL = "Parent Relations";
	public static final String ENTITY_TITLE_SINGULAR = "Parent Relation";
	private static final Logger LOGGER = LoggerFactory.getLogger(CParentRelation.class);
	public static final String VIEW_NAME = "Parent Relations View";
	// Parent item reference - stores ID only due to polymorphism constraints
	// @MappedSuperclass types (CProjectItem) cannot be used as @ManyToOne targets
	// Resolution is done via IHasParentRelation.getParentItem() helper
	@Column (name = "parent_item_id", nullable = true)
	@AMetaData (
			displayName = "Parent Item ID", required = false, readOnly = false, description = "ID of the parent in the hierarchy", hidden = true
	)
	private Long parentItemId = 0L;
	@Column (name = "parent_item_type", nullable = true, length = 100)
	@AMetaData (displayName = "Parent Item Type", required = false, readOnly = false, description = "Type of the parent entity", hidden = true)
	private String parentItemType = "";

	/** Default constructor for JPA. */
	protected CParentRelation() {}

	/** Constructor with owner item. */
	public CParentRelation(final CProjectItem<?> ownerItem) {
		super(ownerItem);
		initializeDefaults();
	}

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public Icon getIcon() { return new Icon(VaadinIcon.CLUSTER); }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	/** Get the parent item in the hierarchy.
	 * @return the parent project item, or null if this is a root item */
	public CProjectItem<?> getParentItem() {
		if (parentItemId == null || parentItemType == null || parentItemType.isEmpty() || parentItemId == 0L) {
			return null;
		}
		try {
			// Use the entity registry to resolve the entity
			final Class<?> entityClass = CEntityRegistry.getEntityClass(parentItemType);
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			final CAbstractService<?> service = (CAbstractService<?>) CSpringContext.getBean(serviceClass);
			final Optional<?> entityOpt = service.getById(parentItemId);
			if (entityOpt.isPresent()) {
				return (CProjectItem<?>) entityOpt.get();
			}
		} catch (final Exception e) {
			// Log warning but return null - this prevents cascading failures
			LOGGER.warn("Could not resolve parent item with ID {} and type {}: {}", parentItemId, parentItemType, e.getMessage());
		}
		return null;
	}

	public Long getParentItemId() { return parentItemId; }

	public String getParentItemType() { return parentItemType; }

	/** Check if this item has a parent in the hierarchy.
	 * @return true if parentItem is set, false otherwise */
	public boolean hasParent() {
		return parentItemId != null;
	}

	private final void initializeDefaults() {
		try {
			CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
		} catch (final Exception e) {
			// In BAB profile or if service is not available, skip service initialization
			// CParentRelation is a composition entity and doesn't always need service initialization
			LOGGER.debug("Service initialization skipped for CParentRelation: {}", e.getMessage());
		}
	}

	@Override
	public void setColor(String color) {
		// Not used
	}

	/** Set the parent item in the hierarchy.
	 * @param parentItem the parent project item, or null to make this a root item */
	public void setParentItem(final CProjectItem<?> parentItem) {
		if (parentItem == null) {
			parentItemId = null;
			parentItemType = null;
		} else {
			parentItemId = parentItem.getId();
			parentItemType = ProxyUtils.getUserClass(parentItem.getClass()).getSimpleName();
		}
	}
}
