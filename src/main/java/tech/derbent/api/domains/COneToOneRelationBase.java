package tech.derbent.api.domains;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasIcon;

/**
 * COneToOneRelationBase - Generic base class for all one-to-one composition pattern entities.
 * <p>
 * This abstract class provides the foundation for entities that are owned via @OneToOne CASCADE.ALL
 * by other entities, following the composition pattern (like CSprintItem, CAgileParentRelation).
 * </p>
 * <p>
 * <strong>OWNERSHIP AND LIFECYCLE:</strong>
 * </p>
 * <ul>
 * <li>Owned entities are created ONCE when owner entity is created</li>
 * <li>Owned entities are NEVER deleted independently - only when owner is deleted</li>
 * <li>Owned entities are NEVER replaced - only their properties are modified</li>
 * <li>Deleting an owned entity will CASCADE DELETE its owner entity</li>
 * </ul>
 * <p>
 * <strong>IMPLEMENTATION REQUIREMENTS:</strong>
 * </p>
 * <ul>
 * <li>Subclass must be annotated with @Entity and @Table</li>
 * <li>Owner entity must have @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)</li>
 * <li>Subclass should implement the specific interface (e.g., IHasAgileParentRelation)</li>
 * </ul>
 * <p>
 * <strong>USAGE PATTERN:</strong>
 * </p>
 * <pre>
 * // ✅ CORRECT: Modify owned entity's properties
 * entity.getOwnedRelation().setSomeProperty(value);
 * entityService.save(entity);  // Cascades to owned entity
 * 
 * // ❌ WRONG: These patterns violate ownership
 * entity.setOwnedRelation(new COwnedEntity());  // Creates orphan
 * ownedEntityService.delete(ownedEntity);       // Deletes owner!
 * entity.setOwnedRelation(null);                // Orphans entity
 * </pre>
 *
 * @param <T> The concrete type of the relation entity
 * @author Derbent Framework
 * @see CAgileParentRelation
 * @see tech.derbent.app.sprints.domain.CSprintItem
 */
@MappedSuperclass
public abstract class COneToOneRelationBase<T extends COneToOneRelationBase<T>> extends CEntityDB<T> implements IHasIcon {

    /**
     * Transient back-reference to owner entity.
     * Set by parent after loading to enable display in widgets/forms.
     * Must be set via setOwnerItem() after entity load or construction.
     */
    @Transient
    private CProjectItem<?> ownerItem;

    /**
     * Default constructor for JPA.
     */
    protected COneToOneRelationBase() {
        super();
    }

    /**
     * Get the owner item (CActivity/CMeeting/CMilestone/etc.).
     * 
     * @return the owner item
     * @throws IllegalStateException if ownerItem is null (must be set by owner entity)
     */
    public CProjectItem<?> getOwnerItem() {
        if (ownerItem == null) {
            throw new IllegalStateException("ownerItem must be set by parent entity after loading. " +
                "Ensure owner entity's @PostLoad or constructor calls setOwnerItem(this).");
        }
        return ownerItem;
    }

    /**
     * Set the owner item (CActivity/CMeeting/CMilestone/etc.).
     * This method MUST be called by the owner entity in:
     * - Constructor (after creating the relation)
     * - @PostLoad method (after JPA loads the entity)
     * 
     * @param ownerItem the owner item
     */
    public void setOwnerItem(final CProjectItem<?> ownerItem) {
        this.ownerItem = ownerItem;
    }

    @Override
    public String toString() {
        return String.format(
            "%s{id=%d, owner=%s}",
            getClass().getSimpleName(),
            getId(),
            ownerItem != null ? ownerItem.getName() : "not set"
        );
    }
}
