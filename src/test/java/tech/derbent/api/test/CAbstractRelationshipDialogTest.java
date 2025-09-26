package tech.derbent.api.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.views.dialogs.CDBRelationDialog;

/** Abstract base test class for relationship dialog components. Provides common test patterns and utilities for testing relationship dialogs.
 * @param <RelationshipClass>  The relationship entity type
 * @param <MainEntityClass>    The main entity type
 * @param <RelatedEntityClass> The related entity type
 * @param <DialogClass>        The dialog class type */
public abstract class CAbstractRelationshipDialogTest<RelationshipClass extends CEntityDB<RelationshipClass>,
		MainEntityClass extends CEntityDB<MainEntityClass>, RelatedEntityClass extends CEntityDB<RelatedEntityClass>,
		DialogClass extends CDBRelationDialog<RelationshipClass, MainEntityClass, RelatedEntityClass>> {

	@Mock
	protected CAbstractEntityRelationService<RelationshipClass> relationshipService;
	protected DialogClass dialog;
	protected MainEntityClass mainEntity;
	protected RelatedEntityClass relatedEntity;
	protected RelationshipClass existingRelationship;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		// Create test entities
		mainEntity = createMainEntity();
		relatedEntity = createRelatedEntity();
		existingRelationship = createExistingRelationship();
		// Create dialog instance
		dialog = createDialog();
		// Setup common mocks
		setupCommonMocks();
	}

	/** Test dialog initialization with new relationship. */
	@Test
	void testDialogInitialization_NewRelationship() {
		// Given
		DialogClass newDialog = createDialog();
		// When
		initializeDialog(newDialog, mainEntity, relatedEntity, null);
		// Then
		assertTrue(newDialog.isVisible());
		assertNotNull(getMainEntityFromDialog(newDialog));
		assertNotNull(getRelatedEntityFromDialog(newDialog));
		assertNull(getRelationshipFromDialog(newDialog));
	}

	/** Test dialog initialization with existing relationship. */
	@Test
	void testDialogInitialization_ExistingRelationship() {
		// Given
		DialogClass editDialog = createDialog();
		// When
		initializeDialog(editDialog, mainEntity, relatedEntity, existingRelationship);
		// Then
		assertTrue(editDialog.isVisible());
		assertNotNull(getMainEntityFromDialog(editDialog));
		assertNotNull(getRelatedEntityFromDialog(editDialog));
		assertNotNull(getRelationshipFromDialog(editDialog));
		assertEquals(existingRelationship.getId(), getRelationshipFromDialog(editDialog).getId());
	}

	/** Test CRUD operation: Create new relationship. */
	@Test
	void testCreateRelationship() {
		// Given
		when(relationshipService.save(any(getRelationshipClass()))).thenReturn(existingRelationship);
		// When
		RelationshipClass result = performCreateOperation(dialog, mainEntity, relatedEntity);
		// Then
		assertNotNull(result);
		verify(relationshipService).save(any(getRelationshipClass()));
		assertEquals(mainEntity.getId(), getMainEntityId(result));
		assertEquals(relatedEntity.getId(), getRelatedEntityId(result));
	}

	/** Test CRUD operation: Update existing relationship. */
	@Test
	void testUpdateRelationship() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, existingRelationship);
		when(relationshipService.save(any(getRelationshipClass()))).thenReturn(existingRelationship);
		// When
		RelationshipClass result = performUpdateOperation(dialog, existingRelationship);
		// Then
		assertNotNull(result);
		verify(relationshipService).save(any(getRelationshipClass()));
		assertEquals(existingRelationship.getId(), result.getId());
	}

	/** Test CRUD operation: Delete relationship. */
	@Test
	void testDeleteRelationship() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, existingRelationship);
		doNothing().when(relationshipService).delete(any(getRelationshipClass()));
		// When
		performDeleteOperation(dialog, existingRelationship);
		// Then
		verify(relationshipService).delete(any(getRelationshipClass()));
	}

	/** Test field validation with invalid data. */
	@Test
	void testFieldValidation_InvalidData() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, null);
		// When
		boolean isValid = validateDialogFields(dialog, createInvalidFieldData());
		// Then
		assertFalse(isValid);
		assertTrue(hasValidationErrors(dialog));
	}

	/** Test field validation with valid data. */
	@Test
	void testFieldValidation_ValidData() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, null);
		// When
		boolean isValid = validateDialogFields(dialog, createValidFieldData());
		// Then
		assertTrue(isValid);
		assertFalse(hasValidationErrors(dialog));
	}

	/** Test dialog cancel operation. */
	@Test
	void testDialogCancel() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, existingRelationship);
		// When
		performCancelOperation(dialog);
		// Then
		assertFalse(dialog.isVisible());
		verify(relationshipService, never()).save(any());
		// verify(relationshipService, never()).delete(any());
	}

	/** Test role and permission field population. */
	@Test
	void testRolePermissionFields() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, existingRelationship);
		// When
		String role = getRoleFromDialog(dialog);
		String permission = getPermissionFromDialog(dialog);
		// Then
		assertNotNull(role);
		assertNotNull(permission);
		assertEquals(getExpectedRole(existingRelationship), role);
		assertEquals(getExpectedPermission(existingRelationship), permission);
	}

	/** Test reflection-based field access. */
	@Test
	void testReflectionBasedFieldAccess() {
		// Given
		initializeDialog(dialog, mainEntity, relatedEntity, existingRelationship);
		// When
		Object fieldValue = getFieldValueUsingReflection(dialog, getTestFieldName());
		// Then
		assertNotNull(fieldValue);
		assertEquals(getExpectedFieldValue(existingRelationship), fieldValue);
	}
	// Abstract methods to be implemented by concrete test classes

	/** Create the dialog instance for testing. */
	protected abstract DialogClass createDialog();
	/** Create a test main entity. */
	protected abstract MainEntityClass createMainEntity();
	/** Create a test related entity. */
	protected abstract RelatedEntityClass createRelatedEntity();
	/** Create an existing relationship for testing. */
	protected abstract RelationshipClass createExistingRelationship();
	/** Get the relationship class type. */
	protected abstract Class<RelationshipClass> getRelationshipClass();
	/** Initialize the dialog with test data. */
	protected abstract void initializeDialog(DialogClass dialog, MainEntityClass mainEntity, RelatedEntityClass relatedEntity,
			RelationshipClass relationship);
	/** Perform create operation on the dialog. */
	protected abstract RelationshipClass performCreateOperation(DialogClass dialog, MainEntityClass mainEntity, RelatedEntityClass relatedEntity);
	/** Perform update operation on the dialog. */
	protected abstract RelationshipClass performUpdateOperation(DialogClass dialog, RelationshipClass relationship);
	/** Perform delete operation on the dialog. */
	protected abstract void performDeleteOperation(DialogClass dialog, RelationshipClass relationship);
	/** Perform cancel operation on the dialog. */
	protected abstract void performCancelOperation(DialogClass dialog);
	/** Validate dialog fields with given data. */
	protected abstract boolean validateDialogFields(DialogClass dialog, Object testData);
	/** Check if dialog has validation errors. */
	protected abstract boolean hasValidationErrors(DialogClass dialog);
	/** Create invalid field data for testing. */
	protected abstract Object createInvalidFieldData();
	/** Create valid field data for testing. */
	protected abstract Object createValidFieldData();
	/** Get main entity from dialog. */
	protected abstract MainEntityClass getMainEntityFromDialog(DialogClass dialog);
	/** Get related entity from dialog. */
	protected abstract RelatedEntityClass getRelatedEntityFromDialog(DialogClass dialog);
	/** Get relationship from dialog. */
	protected abstract RelationshipClass getRelationshipFromDialog(DialogClass dialog);
	/** Get main entity ID from relationship. */
	protected abstract Long getMainEntityId(RelationshipClass relationship);
	/** Get related entity ID from relationship. */
	protected abstract Long getRelatedEntityId(RelationshipClass relationship);
	/** Get role from dialog. */
	protected abstract String getRoleFromDialog(DialogClass dialog);
	/** Get permission from dialog. */
	protected abstract String getPermissionFromDialog(DialogClass dialog);
	/** Get expected role from relationship. */
	protected abstract String getExpectedRole(RelationshipClass relationship);
	/** Get expected permission from relationship. */
	protected abstract String getExpectedPermission(RelationshipClass relationship);
	/** Get field value using reflection. */
	protected abstract Object getFieldValueUsingReflection(DialogClass dialog, String fieldName);
	/** Get test field name for reflection testing. */
	protected abstract String getTestFieldName();
	/** Get expected field value for reflection testing. */
	protected abstract Object getExpectedFieldValue(RelationshipClass relationship);
	// Protected helper methods

	/** Setup common mocks for all tests. */
	protected void setupCommonMocks() {
		// Override in subclasses for specific setup
	}

	/** Verify common expectations after operations. */
	protected void verifyCommonExpectations() {
		// Override in subclasses for specific verifications
	}
}
