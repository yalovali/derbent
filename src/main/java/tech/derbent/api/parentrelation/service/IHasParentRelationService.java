package tech.derbent.api.parentrelation.service;

import tech.derbent.api.interfaces.IHasParentRelation;

/** Service interface for entities that have a parent relation.
 * Provides initialization hook for IHasParentRelation entities. */
public interface IHasParentRelationService {

	default void initializeNewEntity_IHasParentRelation(@SuppressWarnings ("unused") final IHasParentRelation entity) {}
}
