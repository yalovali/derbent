package tech.derbent.api.agileparentrelation.service;

import tech.derbent.api.interfaces.IHasUserStoryParent;

public interface IHasUserStoryParentService extends IHasAgileParentRelationService {

	default void initializeNewEntity_IHasUserStoryParent(@SuppressWarnings ("unused") final IHasUserStoryParent entity) {}
}
