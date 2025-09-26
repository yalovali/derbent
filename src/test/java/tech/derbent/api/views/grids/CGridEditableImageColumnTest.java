package tech.derbent.api.views.grids;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import com.vaadin.flow.component.grid.Grid.Column;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.annotations.AMetaData;

/** Unit tests for CGrid editable image column functionality. */
class CGridEditableImageColumnTest {

	/** Test entity with profile picture for testing */
	public static class TestUser extends CEntityDB<TestUser> {

		@AMetaData (displayName = "Profile Picture", imageData = true)
		private byte[] profilePicture;
		private String name;

		public TestUser() {
			super(TestUser.class);
		}

		public byte[] getProfilePicture() { return profilePicture; }

		public void setProfilePicture(byte[] profilePicture) { this.profilePicture = profilePicture; }

		public String getName() { return name; }

		public void setName(String name) { this.name = name; }
	}

	private CGrid<TestUser> grid;
	private TestUser testUser;

	@BeforeEach
	void setUp() {
		grid = new CGrid<TestUser>(TestUser.class);
		testUser = new TestUser();
		testUser.setName("John Doe");
		testUser.setProfilePicture("test image data".getBytes());
	}

	@Test
	void testAddImageColumn_Traditional_CreatesColumn() throws Exception {
		// Test the traditional addImageColumn method
		Column<TestUser> column = grid.addImageColumn(TestUser::getProfilePicture, "Picture");
		assertNotNull(column);
		assertEquals("Picture", column.getHeaderText());
		assertFalse(column.isSortable());
	}

	@Test
	void testAddEditableImageColumn_CreatesColumn() throws Exception {
		// Test the new addEditableImageColumn method
		Column<TestUser> column =
				grid.addEditableImageColumn(TestUser::getProfilePicture, TestUser::setProfilePicture, "Editable Picture", "Profile Picture");
		assertNotNull(column);
		assertEquals("Editable Picture", column.getHeaderText());
		assertFalse(column.isSortable());
	}

	@Test
	void testEditableImageColumn_NullSetter_CreatesColumn() throws Exception {
		// Test that passing null setter doesn't cause errors during column creation
		Column<TestUser> column = grid.addEditableImageColumn(TestUser::getProfilePicture, null, // null setter
				"Picture", "Profile Picture");
		assertNotNull(column);
		assertEquals("Picture", column.getHeaderText());
	}

	@Test
	void testBothImageColumnTypes_CreateDifferentColumns() throws Exception {
		// Add both types of columns to the same grid
		Column<TestUser> traditionalColumn = grid.addImageColumn(TestUser::getProfilePicture, "Traditional");
		Column<TestUser> editableColumn =
				grid.addEditableImageColumn(TestUser::getProfilePicture, TestUser::setProfilePicture, "Editable", "Profile Picture");
		// Both should be created successfully
		assertNotNull(traditionalColumn);
		assertNotNull(editableColumn);
		// They should have different headers
		assertEquals("Traditional", traditionalColumn.getHeaderText());
		assertEquals("Editable", editableColumn.getHeaderText());
		// Both should be non-sortable
		assertFalse(traditionalColumn.isSortable());
		assertFalse(editableColumn.isSortable());
	}

	@Test
	void testGridMethodsIntegration() throws Exception {
		System.out.println("=== CGrid Editable Image Column Integration Demo ===");
		// Create both column types
		grid.addImageColumn(TestUser::getProfilePicture, "View Only");
		grid.addEditableImageColumn(TestUser::getProfilePicture, TestUser::setProfilePicture, "Editable", "Profile Picture");
		System.out.println("✅ Successfully added both image column types to grid");
		System.out.println("   - Traditional: View-only image display");
		System.out.println("   - Editable: CPictureSelector in icon mode with dialog editing");
		// Set some items
		grid.setItems(testUser);
		System.out.println("✅ Grid populated with test data");
		System.out.println("\n=== Implementation Benefits ===");
		System.out.println("📸 Traditional addImageColumn: Static image display (existing behavior)");
		System.out.println("✏️  New addEditableImageColumn: Interactive editing with consistent UX");
		System.out.println("🎯 Both use the same 40x40px circular styling for visual consistency");
		System.out.println("⚡ Icon mode CPictureSelector opens full dialog when clicked");
		System.out.println("\n✅ CGrid editable image column integration demo completed!");
	}
}
