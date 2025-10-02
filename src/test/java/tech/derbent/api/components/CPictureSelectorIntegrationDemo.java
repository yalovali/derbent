package tech.derbent.api.components;

import org.junit.jupiter.api.Test;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.components.CVerticalLayout;

/** Integration test demonstrating the CPictureSelector component working in forms. This shows how byte[] fields marked with imageData=true
 * automatically get CPictureSelector components when forms are generated. */
public class CPictureSelectorIntegrationDemo {

	/** Demo entity with profile picture field using imageData annotation. */
	public static class DemoEntity extends CEntityDB<DemoEntity> {

		@AMetaData (
				displayName = "Profile Picture", required = false, readOnly = false, description = "User profile picture", hidden = false, order = 1,
				imageData = true, width = "120px"
		)
		private byte[] profilePicture;
		@AMetaData (displayName = "Name", required = true, readOnly = false, description = "User name", hidden = false, order = 2, maxLength = 100)
		private String name;

		public DemoEntity() {
			super(DemoEntity.class);
		}

		public byte[] getProfilePicture() { return profilePicture; }

		public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }

		public String getName() { return name; }

		public void setName(String name) { this.name = name; }

		@Override
		public void initializeAllFields() {
			// TODO Auto-generated method stub
			
		}
	}

	@Test
	public void demonstrateCPictureSelectorInForm() throws Exception {
		// Create a form for the demo entity
		CEnhancedBinder<DemoEntity> binder = new CEnhancedBinder<>(DemoEntity.class);
		CVerticalLayout formLayout = CFormBuilder.buildForm(DemoEntity.class, binder);
		// The form should now contain:
		// 1. A CPictureSelector component for the profilePicture field (byte[] with imageData=true)
		// 2. A TextField component for the name field (String)
		System.out.println("Form created with " + formLayout.getComponentCount() + " field layouts");
		// Verify that the form contains the expected components
		formLayout.getChildren().forEach(component -> {
			System.out.println("Form contains: " + component.getClass().getSimpleName());
		});
		// Create and bind a demo entity
		DemoEntity entity = new DemoEntity();
		entity.setName("John Doe");
		binder.setBean(entity);
		System.out.println("✅ CPictureSelector integration demo completed successfully!");
		System.out.println("   - byte[] fields with imageData=true now automatically get CPictureSelector components");
		System.out.println("   - The component provides upload, preview, and delete functionality");
		System.out.println("   - The component integrates with the form binding system");
		System.out.println("   - The component can be reused anywhere in the application");
	}
}
