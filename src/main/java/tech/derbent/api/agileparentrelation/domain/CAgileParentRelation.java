package tech.derbent.api.agileparentrelation.domain;

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.domains.COneToOneRelationBase;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.plm.agile.domain.CAgileEntity;
import tech.derbent.plm.activities.domain.CActivity;

/** CAgileParentRelation - Agile hierarchy tracking component owned by any entity (CActivity/CMeeting/etc.).
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
 * <strong>AGILE HIERARCHY SEMANTICS:</strong>
 * </p>
 * <p>
 * The parentItem field determines the agile hierarchy relationship following strict type rules:
 * </p>
 * <ul>
 * <li><strong>Epic</strong>: Cannot have a parent (must be root level, parentItem = NULL)</li>
 * <li><strong>Feature</strong>: Can only have Epic as parent</li>
 * <li><strong>UserStory</strong>: Can only have Feature as parent</li>
 * <li><strong>Activity/Meeting/Risk/etc.</strong>: Can ONLY have UserStory as parent</li>
 * </ul>
 * <p>
 * <strong>CORRECT USAGE PATTERNS:</strong>
 * </p>
 *
 * <pre>
 * // ✅ CORRECT: Modify parent reference to establish hierarchy
 * epic.getAgileParentRelation().setParentItem(null);  // Epic is root level
 * feature.getAgileParentRelation().setParentItem(epic);  // Feature → Epic
 * userStory.getAgileParentRelation().setParentItem(feature);  // UserStory → Feature
 * activity.getAgileParentRelation().setParentItem(userStory);  // Activity → UserStory ONLY
 * 
 * // ❌ WRONG: These patterns violate ownership and cause data loss
 * entity.setAgileParentRelation(new CAgileParentRelation()); // Creates orphaned relation
 * agileParentRelationService.delete(agileParentRelation); // Deletes owner entity
 * entity.setAgileParentRelation(null); // Orphans relation, causes constraint violation
 * activity.getAgileParentRelation().setParentItem(epic);  // ❌ Activity cannot have Epic as parent!
 * activity.getAgileParentRelation().setParentItem(feature);  // ❌ Activity cannot have Feature as parent!
 * </pre>
 * <p>
 * <strong>HIERARCHY OPERATIONS:</strong>
 * </p>
 * <p>
 * All hierarchy operations that move items within the agile structure MUST:
 * </p>
 * <ul>
 * <li>Use CAgileParentRelationService.setParent() for validated handling</li>
 * <li>Set parentItem field to NULL only for Epic entities</li>
 * <li>Respect type-based parent constraints (enforced by validateParentType)</li>
 * <li>NEVER delete parent relations during hierarchy changes</li>
 * <li>NEVER call item.setAgileParentRelation() to replace the parent relation</li>
 * <li>Validate circular dependencies before establishing relationships</li>
 * </ul>
 * <p>
 * <strong>DATA STORAGE:</strong>
 * </p>
 * <p>
 * Stores agile hierarchy data (parent item reference). Enforces Epic → Feature → UserStory → Activity hierarchy for all entity types.
 * </p>
 * <p>
 * <strong>TYPE DESIGN RATIONALE:</strong>
 * </p>
 * <p>
 * The parentItem field is typed as CProjectItem<?> rather than a more specific type (like IHasAgileParentRelation or CAgileEntity) for these reasons:
 * </p>
 * <ul>
 * <li><strong>Flexibility</strong>: Supports both CAgileEntity subclasses (Epic, Feature, UserStory) and direct CProjectItem subclasses (Activity, Meeting, Risk)</li>
 * <li><strong>Business Logic Validation</strong>: Hierarchy rules are business logic, not type constraints. Runtime validation via validateParentType() is more appropriate than compile-time type checking</li>
 * <li><strong>Database Polymorphism</strong>: JPA/Hibernate handles CProjectItem polymorphism naturally without complex discriminator columns</li>
 * <li><strong>Evolution</strong>: New entity types can be added without changing the core type structure</li>
 * </ul>
 * <p>
 * The strict hierarchy (Epic→Feature→UserStory→Activity) is enforced at runtime in CAgileParentRelationService.validateParentType(),
 * which throws IllegalArgumentException if rules are violated. This provides clear error messages and maintains type flexibility.
 * </p>
 * @see CActivity
 * @see tech.derbent.plm.meetings.domain.CMeeting */
@Entity
@Table (name = "cagile_parent_relation")
@AttributeOverride (name = "id", column = @Column (name = "agile_parent_relation_id"))
public class CAgileParentRelation extends COneToOneRelationBase<CAgileParentRelation> {

	public static final String DEFAULT_COLOR = "#8B7355"; // OpenWindows Border - hierarchy relations
	public static final String DEFAULT_ICON = "vaadin:cluster";
	public static final String ENTITY_TITLE_PLURAL = "Agile Parent Relations";
	public static final String ENTITY_TITLE_SINGULAR = "Agile Parent Relation";
	public static final String VIEW_NAME = "Agile Parent Relations View";
	// Parent item reference - nullable to support root-level items (Epic)
	// Uses CAgileEntity<?, ?> as the parent type because only agile entities can be parents:
	// - Epic, Feature, UserStory all extend CAgileEntity
	// - Activity, Meeting, Risk, Decision are LEAF NODES - they can have parents but cannot BE parents
	// This provides compile-time type safety preventing Activities from being set as parents
	// Hierarchy: Epic (root) → Feature → UserStory → Activity/Meeting/Risk/Decision (leaves)
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "parent_item_id", nullable = true)
	@AMetaData (
			displayName = "Parent Item", required = false, readOnly = false,
			description = "The parent in the agile hierarchy (Epic, Feature, or User Story only)", hidden = false
	)
	private CAgileEntity<?, ?> parentItem;

	/** Default constructor for JPA. */
	public CAgileParentRelation() {
		super();
	}

	@Override
	public String getColor() { return DEFAULT_COLOR; }

	@Override
	public Icon getIcon() { return new Icon(VaadinIcon.CLUSTER); }

	@Override
	public String getIconString() { return DEFAULT_ICON; }

	/** Get the parent item in the agile hierarchy.
	 * @return the parent agile entity (Epic, Feature, or UserStory), or null if this is a root item */
	public CAgileEntity<?, ?> getParentItem() { return parentItem; }
	
	/** Get the parent activity (deprecated - use getParentItem).
	 * @deprecated Use getParentItem() instead for polymorphic parent support
	 * @return the parent item, or null if this is a root item */
	@Deprecated
	public CAgileEntity<?, ?> getParentActivity() { return parentItem; }

	/** Check if this item has a parent in the agile hierarchy.
	 * @return true if parentItem is set, false otherwise */
	public boolean hasParent() {
		return parentItem != null;
	}

	private final void initializeDefaults() {
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
	}

	@Override
	public void setColor(String color) {
		// Not used
	}

	/** Set the parent item in the agile hierarchy.
	 * @param parentItem the parent agile entity (Epic, Feature, or UserStory), or null to make this a root item */
	public void setParentItem(final CAgileEntity<?, ?> parentItem) {
		this.parentItem = parentItem;
	}
	
	/** Set the parent activity (deprecated - use setParentItem).
	 * @deprecated Use setParentItem() instead for polymorphic parent support
	 * @param parentActivity the parent item, or null to make this a root item */
	@Deprecated
	public void setParentActivity(final CAgileEntity<?, ?> parentActivity) {
		this.parentItem = parentActivity;
	}
}
