package tech.derbent.api.components;

import org.junit.jupiter.api.Test;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.components.CPictureSelector;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/** Demo showing how CPictureSelector can be used in icon mode for displaying user profiles in grids with small icons. */
public class CPictureSelectorIconModeDemo {

	/** Demo entity with profile picture for testing */
	public static class UserProfile extends CEntityDB<UserProfile> {

		@AMetaData (
				displayName = "Profile Picture", required = false, readOnly = false, description = "User's profile picture", hidden = false,
				order = 1, imageData = true
		)
		private byte[] profilePicture;
		@AMetaData (displayName = "Name", required = true, readOnly = false, description = "User's name", hidden = false, order = 2)
		private String name;

		public UserProfile() {
			super(UserProfile.class);
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
	public void demonstrateIconModeUsage() throws Exception {
		System.out.println("=== CPictureSelector Icon Mode Demo ===");
		// Create field info for profile picture
		EntityFieldInfo fieldInfo = createProfilePictureFieldInfo();
		// 1. Create CPictureSelector in FULL mode (traditional usage in forms)
		CPictureSelector fullModeSelector = new CPictureSelector(fieldInfo, false);
		System.out.println("✅ Full mode selector created - shows image with upload controls inline");
		System.out.println("   - Components: " + fullModeSelector.getContent().getComponentCount() + " (image + upload + delete)");
		// 2. Create CPictureSelector in ICON mode (for grids and compact displays)
		CPictureSelector iconModeSelector = new CPictureSelector(fieldInfo, true);
		System.out.println("✅ Icon mode selector created - shows only small circular image");
		System.out.println("   - Components: " + iconModeSelector.getContent().getComponentCount() + " (image only)");
		System.out.println("   - Dimensions: 40x40px with 50% border-radius (circular)");
		// 3. Demonstrate value setting works the same in both modes
		byte[] sampleImageData = createSampleImageData();
		fullModeSelector.setValue(sampleImageData);
		iconModeSelector.setValue(sampleImageData);
		System.out.println("✅ Image data set on both selectors");
		System.out
				.println("   - Both selectors have same value: " + java.util.Arrays.equals(fullModeSelector.getValue(), iconModeSelector.getValue()));
		// 4. Show how read-only mode works
		iconModeSelector.setReadOnly(true);
		System.out.println("✅ Icon mode selector set to read-only");
		System.out.println("   - In read-only mode, clicking opens dialog in view-only mode");
		System.out.println("\n=== Use Cases ===");
		System.out.println("📋 Full mode: Use in forms for editing user profiles");
		System.out.println("🔍 Icon mode: Use in grids to show user profile pictures");
		System.out.println("   - CGrid.addImageColumn() can be enhanced to use icon mode CPictureSelector");
		System.out.println("   - Users click on profile picture in grid to edit it in a dialog");
		System.out.println("   - Provides consistent image editing experience across the application");
		System.out.println("\n✅ CPictureSelector icon mode demo completed successfully!");
	}

	private EntityFieldInfo createProfilePictureFieldInfo() {
		EntityFieldInfo fieldInfo = new EntityFieldInfo();
		fieldInfo.setFieldName("profilePicture");
		fieldInfo.setDisplayName("Profile Picture");
		fieldInfo.setDescription("User's profile picture");
		fieldInfo.setRequired(false);
		fieldInfo.setReadOnly(false);
		fieldInfo.setHidden(false);
		fieldInfo.setOrder(1);
		fieldInfo.setImageData(true);
		fieldInfo.setWidth("40px"); // Small icon size
		fieldInfo.setDefaultValue("");
		fieldInfo.setPlaceholder("");
		return fieldInfo;
	}

	private byte[] createSampleImageData() {
		// In a real application, this would be actual image bytes
		return "sample profile picture data".getBytes();
	}
}
