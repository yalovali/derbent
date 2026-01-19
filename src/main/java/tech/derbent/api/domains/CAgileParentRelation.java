package tech.derbent.api.domains;

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
import tech.derbent.app.activities.domain.CActivity;

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
 * The parentActivity field determines the agile hierarchy relationship:
 * </p>
 * <ul>
 * <li><strong>parentActivity = NULL</strong>: Item is at root level (Epic, or standalone item)</li>
 * <li><strong>parentActivity = CActivity</strong>: Item is child of that activity (Story under Epic, Task under Story, etc.)</li>
 * </ul>
 * <p>
 * <strong>CORRECT USAGE PATTERNS:</strong>
 * </p>
 *
 * <pre>
 * // ✅ CORRECT: Modify parent reference to establish hierarchy
 * entity.getAgileParentRelation().setParentActivity(epicActivity); // Link to Epic
 * entity.getAgileParentRelation().setParentActivity(null); // Clear parent (make root)
 * // ❌ WRONG: These patterns violate ownership and cause data loss
 * entity.setAgileParentRelation(new CAgileParentRelation()); // Creates orphaned relation
 * agileParentRelationService.delete(agileParentRelation); // Deletes owner entity
 * entity.setAgileParentRelation(null); // Orphans relation, causes constraint violation
 * </pre>
 * <p>
 * <strong>HIERARCHY OPERATIONS:</strong>
 * </p>
 * <p>
 * All hierarchy operations that move items within the agile structure MUST:
 * </p>
 * <ul>
 * <li>Use CAgileParentRelationService for unified handling</li>
 * <li>Set parentActivity field to NULL for root items, or target activity for child items</li>
 * <li>NEVER delete parent relations during hierarchy changes</li>
 * <li>NEVER call item.setAgileParentRelation() to replace the parent relation</li>
 * <li>Validate circular dependencies before establishing relationships</li>
 * </ul>
 * <p>
 * <strong>DATA STORAGE:</strong>
 * </p>
 * <p>
 * Stores agile hierarchy data (parent activity reference). Enables Epic → User Story → Task relationships for any entity type (Activities, Meetings,
 * Issues, etc.).
 * </p>
 * @see CActivity
 * @see tech.derbent.app.meetings.domain.CMeeting */
@Entity
@Table (name = "cagile_parent_relation")
@AttributeOverride (name = "id", column = @Column (name = "agile_parent_relation_id"))
public class CAgileParentRelation extends COneToOneRelationBase<CAgileParentRelation> {

	public static final String DEFAULT_COLOR = "#8B7355"; // OpenWindows Border - hierarchy relations
	public static final String DEFAULT_ICON = "vaadin:cluster";
	public static final String ENTITY_TITLE_PLURAL = "Agile Parent Relations";
	public static final String ENTITY_TITLE_SINGULAR = "Agile Parent Relation";
	public static final String VIEW_NAME = "Agile Parent Relations View";
	// Parent activity reference - nullable to support root-level items
	// Uses CActivity as the parent type to establish Epic/Story/Task hierarchy
	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "parent_activity_id", nullable = true)
	@AMetaData (
			displayName = "Parent Activity", required = false, readOnly = false,
			description = "The parent activity in the agile hierarchy (Epic, User Story, etc.)", hidden = false, dataProviderBean = "CActivityService"
	)
	private CActivity parentActivity;

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

	/** Get the parent activity in the agile hierarchy.
	 * @return the parent activity, or null if this is a root item */
	public CActivity getParentActivity() { return parentActivity; }

	/** Check if this item has a parent in the agile hierarchy.
	 * @return true if parentActivity is set, false otherwise */
	public boolean hasParent() {
		return parentActivity != null;
	}

	@Override
	public void setColor(String color) {
		// Not used
	}

	/** Set the parent activity in the agile hierarchy.
	 * @param parentActivity the parent activity, or null to make this a root item */
	public void setParentActivity(final CActivity parentActivity) { this.parentActivity = parentActivity; }
}
