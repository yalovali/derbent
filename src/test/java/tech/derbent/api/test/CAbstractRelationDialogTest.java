package tech.derbent.api.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.vaadin.flow.component.combobox.ComboBox;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.views.dialogs.CDBRelationDialog;

/** Abstract base class for testing relationship dialog components. Provides common test patterns for CRUD operations, form validation, and dialog
 * behavior.
 * @param <RelationshipClass>  The relationship entity type
 * @param <MainEntityClass>    The main entity type
 * @param <RelatedEntityClass> The related entity type
 * @param <DialogClass>        The dialog class type */
@ExtendWith (MockitoExtension.class)
public abstract class CAbstractRelationDialogTest<RelationshipClass extends CEntityDB<RelationshipClass>,
		MainEntityClass extends CEntityDB<MainEntityClass>, RelatedEntityClass extends CEntityDB<RelatedEntityClass>,
		DialogClass extends CDBRelationDialog<RelationshipClass, MainEntityClass, RelatedEntityClass>> {

	@Mock
	protected CAbstractService<MainEntityClass> mainEntityService;
	@Mock
	protected CAbstractService<RelatedEntityClass> relatedEntityService;
	@Mock
	protected Consumer<RelationshipClass> onSaveCallback;
	protected DialogClass dialog;
	protected MainEntityClass mainEntity;
	protected RelatedEntityClass relatedEntity;
	protected RelationshipClass relationshipEntity;

	/** Set up common test fixtures. */
	@BeforeEach
	protected void setUp() {
		// Initialize entities
		mainEntity = createMainEntity();
		relatedEntity = createRelatedEntity();
		relationshipEntity = createRelationshipEntity();
		// Setup mocks
		setupMockServices();
	}

	/** Test dialog creation for new relationships. */
	@Test
	void testCreateNewRelationshipDialog() throws Exception {
		// When
		dialog = createNewRelationshipDialog();
		// Then
		assertNotNull(dialog, "Dialog should be created");
		// assertTrue(dialog.isNew(), "Dialog should be in new mode");
		assertDialogPropertiesForNew();
	}

	/** Test dialog creation for editing existing relationships. */
	@Test
	void testCreateEditRelationshipDialog() throws Exception {
		// When
		dialog = createEditRelationshipDialog();
		// Then
		assertNotNull(dialog, "Dialog should be created");
		// assertFalse(dialog.isNew(), "Dialog should be in edit mode");
		assertDialogPropertiesForEdit();
	}

	/** Test form field initialization and population. */
	@Test
	void testFormFieldInitialization() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When - dialog should auto-populate form fields
		// Then
		assertFormFieldsInitialized();
		assertFormFieldsConfigured();
	}

	/** Test form population with existing data. */
	@Test
	void testFormPopulationWithExistingData() throws Exception {
		// Given
		setupExistingRelationshipData();
		dialog = createEditRelationshipDialog();
		// When - form should be populated with existing data
		// Then
		assertFormPopulatedWithExistingData();
	}

	/** Test form validation with valid data. */
	@Test
	void testFormValidationWithValidData() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When
		populateFormWithValidData();
		// Then
		assertTrue(isFormValid(), "Form should be valid with correct data");
		assertNoValidationErrors();
	}

	/** Test form validation with invalid data. */
	@Test
	void testFormValidationWithInvalidData() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When
		populateFormWithInvalidData();
		// Then
		assertFalse(isFormValid(), "Form should be invalid with incorrect data");
		assertValidationErrorsPresent();
	}

	/** Test CRUD operation - CREATE. */
	@Test
	void testCrudCreate() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		populateFormWithValidData();
		// When
		performSaveOperation();
		// Then
		verify(onSaveCallback).accept(any()); // Callback should be invoked
		assertRelationshipCreated();
		assertDialogClosed();
	}

	/** Test CRUD operation - READ (form population). */
	@Test
	void testCrudRead() throws Exception {
		// Given
		setupExistingRelationshipData();
		// When
		dialog = createEditRelationshipDialog();
		// Then
		assertExistingDataDisplayed();
		assertReadOnlyFieldsIfApplicable();
	}

	/** Test CRUD operation - UPDATE. */
	@Test
	void testCrudUpdate() throws Exception {
		// Given
		setupExistingRelationshipData();
		dialog = createEditRelationshipDialog();
		// When
		modifyFormData();
		performSaveOperation();
		// Then
		verify(onSaveCallback).accept(any());
		assertRelationshipUpdated();
		assertDialogClosed();
	}

	/** Test CRUD operation - DELETE (if supported). */
	@Test
	void testCrudDelete() throws Exception {
		// Given
		setupExistingRelationshipData();
		dialog = createEditRelationshipDialog();
		// When
		if (supportsDelete()) {
			performDeleteOperation();
			// Then
			assertRelationshipDeleted();
			assertDialogClosed();
		}
	}

	/** Test cancel operation. */
	@Test
	void testCancelOperation() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		populateFormWithValidData();
		// When
		performCancelOperation();
		// Then
		verify(onSaveCallback, never()).accept(any());
		assertDialogClosed();
		assertNoChangesApplied();
	}

	/** Test entity selection via ComboBox. */
	@Test
	void testEntitySelection() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		ComboBox<RelatedEntityClass> entityComboBox = getEntityComboBox();
		// When
		selectEntityInComboBox(relatedEntity);
		// Then
		assertEquals(relatedEntity, entityComboBox.getValue(), "Selected entity should be set");
		assertEntitySelectionEffects();
	}

	/** Test role and permission field handling. */
	@Test
	void testRoleAndPermissionFields() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When
		setRoleField("TEST_ROLE");
		setPermissionField("READ,WRITE");
		// Then
		assertEquals("TEST_ROLE", getRoleFieldValue());
		assertEquals("READ,WRITE", getPermissionFieldValue());
		assertRolePermissionValidation();
	}

	/** Test reflection-based field management (if applicable). */
	@Test
	void testReflectionBasedFieldManagement() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When - test reflection-based field access
		if (supportsReflectionBasedFields()) {
			testReflectionFieldAccess();
			// Then
			assertReflectionFieldsWork();
		}
	}

	/** Test context-aware suggestions (if applicable). */
	@Test
	void testContextAwareSuggestions() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When
		if (supportsContextSuggestions()) {
			triggerContextSuggestions();
			// Then
			assertContextSuggestionsProvided();
		}
	}

	/** Test dialog lifecycle and cleanup. */
	@Test
	void testDialogLifecycleAndCleanup() throws Exception {
		// Given
		dialog = createNewRelationshipDialog();
		// When
		dialog.open();
		assertTrue(dialog.isOpened(), "Dialog should be open");
		// Then - test close and cleanup
		dialog.close();
		assertFalse(dialog.isOpened(), "Dialog should be closed");
		assertResourcesCleanedUp();
	}
	// Abstract methods that subclasses must implement

	/** Create main entity for testing. */
	protected abstract MainEntityClass createMainEntity();
	/** Create related entity for testing. */
	protected abstract RelatedEntityClass createRelatedEntity();
	/** Create relationship entity for testing. */
	protected abstract RelationshipClass createRelationshipEntity();
	/** Create dialog for new relationship. */
	protected abstract DialogClass createNewRelationshipDialog() throws Exception;
	/** Create dialog for editing existing relationship. */
	protected abstract DialogClass createEditRelationshipDialog() throws Exception;
	/** Setup mock services behavior. */
	protected abstract void setupMockServices();
	/** Setup existing relationship data for testing. */
	protected abstract void setupExistingRelationshipData();
	/** Populate form with valid test data. */
	protected abstract void populateFormWithValidData();
	/** Populate form with invalid test data. */
	protected abstract void populateFormWithInvalidData();
	/** Check if form is currently valid. */
	protected abstract boolean isFormValid();
	/** Perform save operation. */
	protected abstract void performSaveOperation();
	/** Perform cancel operation. */
	protected abstract void performCancelOperation();
	/** Get entity selection ComboBox. */
	protected abstract ComboBox<RelatedEntityClass> getEntityComboBox();
	/** Select entity in ComboBox. */
	protected abstract void selectEntityInComboBox(RelatedEntityClass entity);
	/** Set role field value. */
	protected abstract void setRoleField(String role);
	/** Set permission field value. */
	protected abstract void setPermissionField(String permission);
	/** Get role field value. */
	protected abstract String getRoleFieldValue();
	/** Get permission field value. */
	protected abstract String getPermissionFieldValue();
	// Optional methods with default implementations

	/** Check if delete operation is supported. */
	protected boolean supportsDelete() {
		return false;
	}

	/** Perform delete operation. */
	protected void performDeleteOperation() {
		// Default implementation - subclasses can override
	}

	/** Check if reflection-based field management is supported. */
	protected boolean supportsReflectionBasedFields() {
		return false;
	}

	/** Test reflection-based field access. */
	protected void testReflectionFieldAccess() {
		// Default implementation - subclasses can override
	}

	/** Check if context-aware suggestions are supported. */
	protected boolean supportsContextSuggestions() {
		return false;
	}

	/** Trigger context suggestions. */
	protected void triggerContextSuggestions() {
		// Default implementation - subclasses can override
	}

	/** Modify form data for update testing. */
	protected void modifyFormData() {
		setRoleField("MODIFIED_ROLE");
		setPermissionField("READ,WRITE,DELETE");
	}
	// Assertion methods with default implementations

	/** Assert dialog properties for new relationship mode. */
	protected void assertDialogPropertiesForNew() {
		assertNotNull(dialog.getDialogTitleString());
		assertTrue(dialog.getDialogTitleString().toLowerCase().contains("add") || dialog.getDialogTitleString().toLowerCase().contains("new"));
	}

	/** Assert dialog properties for edit relationship mode. */
	protected void assertDialogPropertiesForEdit() {
		assertNotNull(dialog.getDialogTitleString());
		assertTrue(dialog.getDialogTitleString().toLowerCase().contains("edit") || dialog.getDialogTitleString().toLowerCase().contains("update"));
	}

	/** Assert form fields are initialized. */
	protected void assertFormFieldsInitialized() {
		ComboBox<RelatedEntityClass> entityComboBox = getEntityComboBox();
		assertNotNull(entityComboBox, "Entity ComboBox should be initialized");
		// Additional field checks can be added by subclasses
	}

	/** Assert form fields are properly configured. */
	protected void assertFormFieldsConfigured() {
		ComboBox<RelatedEntityClass> entityComboBox = getEntityComboBox();
		assertTrue(entityComboBox.isRequired(), "Entity ComboBox should be required");
	}

	/** Assert form is populated with existing data. */
	protected void assertFormPopulatedWithExistingData() {
		// Default implementation - subclasses should override with specific checks
		assertNotNull(getEntityComboBox().getValue(), "Entity should be selected");
	}

	/** Assert no validation errors are present. */
	protected void assertNoValidationErrors() {
		// Default implementation - subclasses can override
		assertTrue(isFormValid(), "Form should have no validation errors");
	}

	/** Assert validation errors are present. */
	protected void assertValidationErrorsPresent() {
		// Default implementation - subclasses can override
		assertFalse(isFormValid(), "Form should have validation errors");
	}

	/** Assert relationship was created. */
	protected void assertRelationshipCreated() {
		// Default implementation - verified through callback invocation
		verify(onSaveCallback).accept(any());
	}

	/** Assert relationship was updated. */
	protected void assertRelationshipUpdated() {
		// Default implementation - verified through callback invocation
		verify(onSaveCallback).accept(any());
	}

	/** Assert relationship was deleted. */
	protected void assertRelationshipDeleted() {
		// Default implementation - subclasses should override
	}

	/** Assert dialog is closed. */
	protected void assertDialogClosed() {
		// Default implementation - subclasses can override
		// In real UI tests, would check dialog.isOpened() == false
	}

	/** Assert no changes were applied. */
	protected void assertNoChangesApplied() {
		verify(onSaveCallback, never()).accept(any());
	}

	/** Assert existing data is displayed correctly. */
	protected void assertExistingDataDisplayed() {
		// Default implementation - subclasses should override with specific checks
		assertNotNull(getEntityComboBox().getValue());
	}

	/** Assert read-only fields if applicable. */
	protected void assertReadOnlyFieldsIfApplicable() {
		// Default implementation - subclasses can override
	}

	/** Assert entity selection effects. */
	protected void assertEntitySelectionEffects() {
		// Default implementation - subclasses can override
	}

	/** Assert role and permission validation. */
	protected void assertRolePermissionValidation() {
		// Default implementation - subclasses can override
		assertNotNull(getRoleFieldValue());
		assertNotNull(getPermissionFieldValue());
	}

	/** Assert reflection-based fields work correctly. */
	protected void assertReflectionFieldsWork() {
		// Default implementation - subclasses can override
	}

	/** Assert context suggestions are provided. */
	protected void assertContextSuggestionsProvided() {
		// Default implementation - subclasses can override
	}

	/** Assert resources are properly cleaned up. */
	protected void assertResourcesCleanedUp() {
		// Default implementation - subclasses can override
	}
}
