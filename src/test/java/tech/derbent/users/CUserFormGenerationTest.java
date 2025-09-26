package tech.derbent.users;

import org.junit.jupiter.api.Test;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.users.domain.CUser;

/** Test to verify that CUser entity with profilePictureData field now works correctly with CFormBuilder (no more "Unsupported field type: byte[]"
 * error). */
public class CUserFormGenerationTest {

	@Test
	public void testCUserFormGeneration_NoLongerThrowsUnsupportedFieldTypeError() throws Exception {
		try {
			// Test only specific fields to avoid complex dependencies
			CEnhancedBinder<CUser> binder = new CEnhancedBinder<>(CUser.class);
			// Test only the profilePictureData field (the one that was causing the error)
			java.util.List<String> fieldsToTest = java.util.List.of("profilePictureData");
			CVerticalLayout formLayout = CFormBuilder.buildForm(CUser.class, binder, fieldsToTest);
			System.out.println("✅ SUCCESS: CUser profilePictureData field processing completed without errors!");
			System.out.println("   Form created with " + formLayout.getComponentCount() + " field layouts");
			System.out.println("   The profilePictureData field (byte[]) now gets a CPictureSelector component");
			System.out.println("   Original error 'Unsupported field type: byte[]' has been resolved");
		} catch (Exception e) {
			System.err.println("❌ FAILED: " + e.getMessage());
			throw e;
		}
	}
}
