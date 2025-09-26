package tech.derbent.api.test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.time.Clock;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.session.service.CSessionService;

/** Abstract base class for testing entity relationship services. Provides common test patterns and utilities for relationship management testing.
 * @param <RelationEntity> The relationship entity type
 * @param <ParentEntity>   The parent entity type
 * @param <ChildEntity>    The child entity type
 * @param <Service>        The relationship service type */
@ExtendWith (MockitoExtension.class)
public abstract class CAbstractEntityRelationshipTest<RelationEntity extends CEntityDB<RelationEntity>, ParentEntity extends CEntityDB<ParentEntity>,
		ChildEntity extends CEntityDB<ChildEntity>, Service extends CAbstractEntityRelationService<RelationEntity>> {

	@Mock
	protected Clock clock;
	@Mock
	protected CSessionService sessionService;
	protected Service service;
	protected ParentEntity parentEntity;
	protected ChildEntity childEntity;
	protected RelationEntity relationshipEntity;

	/** Set up common test fixtures. Subclasses should call super.setUp() and then initialize their specific entities and service. */
	@BeforeEach
	protected void setUp() {
		// Initialize entities - subclasses must implement these
		parentEntity = createParentEntity();
		childEntity = createChildEntity();
		relationshipEntity = createRelationshipEntity();
		// Initialize service - subclasses must implement this
		service = createService();
	}

	/** Test basic relationship creation with bidirectional handling. */
	@Test
	void testCreateRelationship_BidirectionalHandling() {
		// Given
		setupValidEntityIds();
		// When - Create relationship and maintain bidirectional links
		addRelationshipToEntities();
		// Then - Verify relationship is properly established
		assertRelationshipEstablished();
		assertBidirectionalRelationshipMaintained();
	}

	/** Test relationship creation with null arguments. */
	@Test
	void testCreateRelationship_NullArguments() {
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> addNullParentToRelationship());
		assertThrows(IllegalArgumentException.class, () -> addNullChildToRelationship());
	}

	/** Test relationship creation with invalid entity IDs. */
	@Test
	void testCreateRelationship_InvalidIds() {
		// Given - entities without IDs
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> addRelationshipWithInvalidIds());
	}

	/** Test relationship removal with bidirectional cleanup. */
	@Test
	void testRemoveRelationship_BidirectionalCleanup() {
		// Given - establish relationship first
		setupValidEntityIds();
		addRelationshipToEntities();
		assertRelationshipEstablished();
		// When - remove relationship
		removeRelationshipFromEntities();
		// Then - verify clean removal
		assertRelationshipRemoved();
		assertBidirectionalRelationshipCleaned();
	}

	/** Test relationship update functionality. */
	@Test
	void testUpdateRelationship_PropertiesAndBidirectional() {
		// Given
		setupValidEntityIds();
		addRelationshipToEntities();
		// When - update relationship properties
		updateRelationshipProperties();
		// Then - verify updates are maintained
		assertRelationshipUpdated();
		assertBidirectionalRelationshipMaintained();
	}

	/** Test duplicate relationship prevention. */
	@Test
	void testPreventDuplicateRelationships() {
		// Given
		setupValidEntityIds();
		addRelationshipToEntities();
		// When - attempt to add duplicate
		// Then - should handle gracefully without duplicates
		addRelationshipToEntities(); // Second time
		assertNoDuplicateRelationships();
	}

	/** Test relationship validation. */
	@Test
	void testRelationshipValidation() {
		// Given - invalid relationship
		RelationEntity invalidRelationship = createInvalidRelationshipEntity();
		// When & Then
		assertThrows(IllegalArgumentException.class, () -> validateRelationshipEntity(invalidRelationship));
	}

	/** Test collection initialization and null safety. */
	@Test
	void testCollectionInitializationAndNullSafety() {
		// Given - new entities
		ParentEntity newParent = createParentEntity();
		ChildEntity newChild = createChildEntity();
		// When & Then - collections should be initialized and safe
		assertCollectionsInitialized(newParent, newChild);
		assertNullSafeOperations(newParent, newChild);
	}

	/** Test relationship queries and existence checks. */
	@Test
	void testRelationshipQueries() {
		// Given
		setupValidEntityIds();
		// When - no relationship exists
		assertFalse(checkRelationshipExists(), "Relationship should not exist initially");
		// When - add relationship
		addRelationshipToEntities();
		// Then - relationship should be found
		assertTrue(checkRelationshipExists(), "Relationship should exist after creation");
		assertRelationshipCanBeFound();
	}
	// Abstract methods that subclasses must implement

	/** Create a parent entity for testing. */
	protected abstract ParentEntity createParentEntity();
	/** Create a child entity for testing. */
	protected abstract ChildEntity createChildEntity();
	/** Create a relationship entity for testing. */
	protected abstract RelationEntity createRelationshipEntity();
	/** Create an invalid relationship entity for validation testing. */
	protected abstract RelationEntity createInvalidRelationshipEntity();
	/** Create the service instance for testing. */
	protected abstract Service createService();
	/** Set up valid IDs for entities (mock or set real IDs). */
	protected abstract void setupValidEntityIds();
	/** Add the relationship to both entities (bidirectional). */
	protected abstract void addRelationshipToEntities();
	/** Remove the relationship from both entities (bidirectional). */
	protected abstract void removeRelationshipFromEntities();
	/** Update relationship properties for testing. */
	protected abstract void updateRelationshipProperties();
	/** Add null parent to relationship (for exception testing). */
	protected abstract void addNullParentToRelationship();
	/** Add null child to relationship (for exception testing). */
	protected abstract void addNullChildToRelationship();
	/** Add relationship with invalid IDs (for exception testing). */
	protected abstract void addRelationshipWithInvalidIds();
	/** Validate a relationship entity. */
	protected abstract void validateRelationshipEntity(RelationEntity relationship);
	/** Check if relationship exists between entities. */
	protected abstract boolean checkRelationshipExists();
	// Assertion methods that subclasses can override or extend

	/** Assert that the relationship is properly established. */
	protected void assertRelationshipEstablished() {
		assertNotNull(relationshipEntity, "Relationship entity should not be null");
		assertEquals(parentEntity, getParentFromRelationship(), "Parent should be set correctly");
		assertEquals(childEntity, getChildFromRelationship(), "Child should be set correctly");
	}

	/** Assert that bidirectional relationships are maintained. */
	protected void assertBidirectionalRelationshipMaintained() {
		assertTrue(getParentRelationships().contains(relationshipEntity), "Parent should contain the relationship");
		assertTrue(getChildRelationships().contains(relationshipEntity), "Child should contain the relationship");
	}

	/** Assert that the relationship is removed. */
	protected void assertRelationshipRemoved() {
		// Default implementation - subclasses can override
		assertNull(getParentFromRelationship(), "Parent reference should be null");
		assertNull(getChildFromRelationship(), "Child reference should be null");
	}

	/** Assert that bidirectional relationships are cleaned up. */
	protected void assertBidirectionalRelationshipCleaned() {
		assertFalse(getParentRelationships().contains(relationshipEntity), "Parent should not contain the removed relationship");
		assertFalse(getChildRelationships().contains(relationshipEntity), "Child should not contain the removed relationship");
	}

	/** Assert that relationship updates are applied. */
	protected void assertRelationshipUpdated() {
		// Default implementation - subclasses should override with specific checks
		assertNotNull(relationshipEntity, "Relationship should still exist after update");
	}

	/** Assert that no duplicate relationships exist. */
	protected void assertNoDuplicateRelationships() {
		// Count relationships and ensure no duplicates
		long parentRelationshipCount = getParentRelationships().stream().filter(r -> r.equals(relationshipEntity)).count();
		long childRelationshipCount = getChildRelationships().stream().filter(r -> r.equals(relationshipEntity)).count();
		assertEquals(1, parentRelationshipCount, "Parent should have exactly one instance of the relationship");
		assertEquals(1, childRelationshipCount, "Child should have exactly one instance of the relationship");
	}

	/** Assert that collections are properly initialized. */
	protected void assertCollectionsInitialized(ParentEntity parent, ChildEntity child) {
		assertNotNull(getParentRelationships(parent), "Parent relationships collection should be initialized");
		assertNotNull(getChildRelationships(child), "Child relationships collection should be initialized");
	}

	/** Assert that operations are null-safe. */
	protected void assertNullSafeOperations(ParentEntity parent, ChildEntity child) {
		// Should not throw exceptions when operating on empty collections
		assertDoesNotThrow(() -> getParentRelationships(parent).size());
		assertDoesNotThrow(() -> getChildRelationships(child).size());
	}

	/** Assert that relationship can be found through queries. */
	protected void assertRelationshipCanBeFound() {
		// Default implementation - subclasses can override
		assertTrue(checkRelationshipExists(), "Relationship should be findable");
	}
	// Helper methods that subclasses must implement for collection access

	/** Get parent entity from relationship. */
	protected abstract ParentEntity getParentFromRelationship();
	/** Get child entity from relationship. */
	protected abstract ChildEntity getChildFromRelationship();
	/** Get relationships collection from parent entity. */
	protected abstract Collection<RelationEntity> getParentRelationships();
	/** Get relationships collection from child entity. */
	protected abstract Collection<RelationEntity> getChildRelationships();
	/** Get relationships collection from specific parent entity. */
	protected abstract Collection<RelationEntity> getParentRelationships(ParentEntity parent);
	/** Get relationships collection from specific child entity. */
	protected abstract Collection<RelationEntity> getChildRelationships(ChildEntity child);
}
