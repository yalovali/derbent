package tech.derbent.abstracts.services;

import java.util.Optional;

/** CEagerLoadingCapable - Interface for repositories that support eager loading of relationships. Provides standardized method for loading entities
 * with their relationships to prevent N+1 queries. Layer: Repository (MVC) */
public interface CEagerLoadingCapable<T> {

	/** Finds an entity by ID with eagerly loaded relationships. Implementations should define which relationships to eagerly load based on entity
	 * needs.
	 * @param id the entity ID
	 * @return Optional containing the entity with loaded relationships */
	Optional<T> findByIdWithEagerLoading(Long id);
}
