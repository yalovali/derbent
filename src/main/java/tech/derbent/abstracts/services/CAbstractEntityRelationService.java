package tech.derbent.abstracts.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.domains.CEntityDB;

/**
 * Abstract service class for managing entity-to-entity relationships. Provides common operations for relationship
 * entities.
 * 
 * @param <RelationEntity>
 *            The relationship entity type
 */
public abstract class CAbstractEntityRelationService<RelationEntity extends CEntityDB<RelationEntity>>
        extends CAbstractService<RelationEntity> {

    public CAbstractEntityRelationService(final CAbstractRepository<RelationEntity> repository, final Clock clock) {
        super(repository, clock);
    }

    /**
     * Find all relationships for a specific parent entity by ID
     */
    @Transactional(readOnly = true)
    public abstract List<RelationEntity> findByParentEntityId(final Long parentEntityId);

    /**
     * Find all relationships for a specific child entity by ID
     */
    @Transactional(readOnly = true)
    public abstract List<RelationEntity> findByChildEntityId(final Long childEntityId);

    /**
     * Check if a relationship already exists between two entities
     */
    @Transactional(readOnly = true)
    public abstract boolean relationshipExists(final Long parentEntityId, final Long childEntityId);

    /**
     * Create a new relationship between two entities
     */
    @Transactional
    public RelationEntity createRelationship(final Long parentEntityId, final Long childEntityId) {
        LOGGER.debug("Creating relationship between parent {} and child {}", parentEntityId, childEntityId);

        if (relationshipExists(parentEntityId, childEntityId)) {
            throw new IllegalArgumentException("Relationship already exists between entities");
        }

        final RelationEntity relationship = createRelationshipInstance(parentEntityId, childEntityId);
        validateRelationship(relationship);
        return save(relationship);
    }

    /**
     * Delete a relationship between two entities
     */
    @Transactional
    public void deleteRelationship(final Long parentEntityId, final Long childEntityId) {
        LOGGER.debug("Deleting relationship between parent {} and child {}", parentEntityId, childEntityId);

        final Optional<RelationEntity> relationship = findRelationship(parentEntityId, childEntityId);
        if (relationship.isPresent()) {
            delete(relationship.get());
        }
    }

    /**
     * Find a specific relationship between two entities
     */
    @Transactional(readOnly = true)
    public abstract Optional<RelationEntity> findRelationship(final Long parentEntityId, final Long childEntityId);

    /**
     * Create a new relationship instance with the given parent and child IDs
     */
    protected abstract RelationEntity createRelationshipInstance(final Long parentEntityId, final Long childEntityId);

    /**
     * Validate a relationship entity before saving
     */
    protected void validateRelationship(final RelationEntity relationship) {
        if (relationship == null) {
            throw new IllegalArgumentException("Relationship cannot be null");
        }
        // Override in subclasses for specific validation
    }

    /**
     * Update relationship with new data
     */
    @Transactional
    public RelationEntity updateRelationship(final RelationEntity relationship) {
        LOGGER.debug("Updating relationship: {}", relationship);

        validateRelationship(relationship);
        return save(relationship);
    }

    /**
     * Count relationships for a parent entity
     */
    @Transactional(readOnly = true)
    public long countByParentEntity(final Long parentEntityId) {
        return findByParentEntityId(parentEntityId).size();
    }

    /**
     * Count relationships for a child entity
     */
    @Transactional(readOnly = true)
    public long countByChildEntity(final Long childEntityId) {
        return findByChildEntityId(childEntityId).size();
    }
}