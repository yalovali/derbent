package tech.derbent.api.agileparentrelation.service;

import tech.derbent.api.interfaces.IHasAgileParentRelation;

public interface IHasAgileParentRelationService {

	default void initializeNewEntity_IHasAgileParentRelation(@SuppressWarnings ("unused") final IHasAgileParentRelation entity) {}
}
