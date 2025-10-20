package tech.derbent.api.services;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** Abstract service class for managing entity-to-entity relationships. Provides common operations for relationship entities.
 * @param <RelationEntity> The relationship entity type */
public abstract class CAbstractEntityRelationService<RelationEntity extends CEntityDB<RelationEntity>> extends CAbstractService<RelationEntity> {

	public CAbstractEntityRelationService(final IAbstractRepository<RelationEntity> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Create a new relationship between two entities */
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

	/** Create a new relationship instance with the given parent and child IDs */
	protected abstract RelationEntity createRelationshipInstance(final Long parentEntityId, final Long childEntityId);

	/** Delete a relationship between two entities */
	@Transactional
	public void deleteRelationship(final Long parentEntityId, final Long childEntityId) {
		LOGGER.debug("Deleting relationship between parent {} and child {}", parentEntityId, childEntityId);
		final RelationEntity relationship = findRelationship(parentEntityId, childEntityId)
				.orElseThrow(() -> new RuntimeException("No relationship exists between the specified entities"));
		delete(relationship);
	}

	/** Find all relationships for a specific child entity by ID */
	@Transactional (readOnly = true)
	public abstract List<RelationEntity> findByChildEntityId(final Long childEntityId);
	/** Find all relationships for a specific parent entity by ID */
	@Transactional (readOnly = true)
	public abstract List<RelationEntity> findByParentEntityId(final Long parentEntityId);
	/** Find a specific relationship between two entities */
	@Transactional (readOnly = true)
	public abstract Optional<RelationEntity> findRelationship(final Long parentEntityId, final Long childEntityId);
	/** Check if a relationship already exists between two entities */
	@Transactional (readOnly = true)
	public abstract boolean relationshipExists(final Long parentEntityId, final Long childEntityId);

	/** Update relationship with new data */
	@Transactional
	public RelationEntity updateRelationship(final RelationEntity relationship) {
		validateRelationship(relationship);
		return save(relationship);
	}

	/** Validate a relationship entity before saving */
	protected void validateRelationship(final RelationEntity relationship) {
		Check.notNull(relationship, "Relationship cannot be null");
	}
}
