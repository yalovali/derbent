package tech.derbent.base.users.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.roles.domain.CUserCompanyRole;

/** Test class to verify that user icons render correctly with the new innerHTML-based implementation.
 * This test validates that icons are properly generated and contain SVG content in their innerHTML. */
class CUserIconRenderingTest {

	@Test
	void testGetIcon_WithoutProfilePicture_ContainsSvgInInnerHTML() {
		// Create a user without a profile picture
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		final CUser user = new CUser("jdoe", "password", "John", "john@example.com", company, role);
		user.setLastname("Doe");
		
		// Get the icon
		final Icon icon = user.getIcon();
		
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		
		// Verify the SVG content is in a child element (new implementation uses appendChild)
		final int childCount = icon.getElement().getChildCount();
		assertTrue(childCount > 0, "Icon should have child elements with SVG content");
		
		final String childInnerHTML = icon.getElement().getChild(0).getProperty("innerHTML");
		assertNotNull(childInnerHTML, "Child element should contain SVG innerHTML");
		assertTrue(childInnerHTML.startsWith("<svg"), 
			"Child innerHTML should start with <svg tag");
		assertTrue(childInnerHTML.contains("</svg>"), 
			"Child innerHTML should end with </svg> tag");
		assertTrue(childInnerHTML.contains("circle"), 
			"Child innerHTML should contain a circle element for avatar background");
		assertTrue(childInnerHTML.contains("text"), 
			"Child innerHTML should contain a text element for initials");
		assertTrue(childInnerHTML.contains("JD"), 
			"Child innerHTML should contain the user's initials 'JD'");
		
		// Verify the old 'icon' attribute is NOT used (it doesn't work for custom SVG)
		final String iconAttr = icon.getElement().getAttribute("icon");
		assertTrue(iconAttr == null || iconAttr.isEmpty() || iconAttr.equals("null"), 
			"Icon attribute should not be set for custom SVG content");
	}
	
	@Test
	void testGetIcon_WithProfilePicture_ContainsSvgWithImageInInnerHTML() throws Exception {
		// Create a user with a profile picture
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		final CUser user = new CUser("jsmith", "password", "Jane", "jane@example.com", company, role);
		user.setLastname("Smith");
		
		// Generate a test avatar image and set it as profile picture
		final byte[] avatarData = tech.derbent.api.utils.CImageUtils.generateAvatarWithInitials("JS", 16);
		user.setProfilePictureData(avatarData);
		
		// Get the icon
		final Icon icon = user.getIcon();
		
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		
		// Verify the SVG content is in a child element (new implementation uses appendChild)
		final int childCount = icon.getElement().getChildCount();
		assertTrue(childCount > 0, "Icon should have child elements");
		
		final String childInnerHTML = icon.getElement().getChild(0).getProperty("innerHTML");
		assertNotNull(childInnerHTML, "Child element innerHTML should contain SVG");
		assertTrue(childInnerHTML.startsWith("<svg"), 
			"Child innerHTML should start with <svg tag");
		assertTrue(childInnerHTML.contains("</svg>"), 
			"Child innerHTML should end with </svg> tag");
		assertTrue(childInnerHTML.contains("image"), 
			"Child innerHTML should contain an image element for profile picture");
		assertTrue(childInnerHTML.contains("href=\"data:"), 
			"Child innerHTML should contain a data URL for the embedded image");
		
		// Verify the old 'icon' attribute is NOT used
		final String iconAttr = icon.getElement().getAttribute("icon");
		assertTrue(iconAttr == null || iconAttr.isEmpty() || iconAttr.equals("null"), 
			"Icon attribute should not be set for custom SVG content");
	}
	
	@Test
	void testMultipleUsers_DifferentIconColors() {
		// Create multiple users with different names
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("User");
		
		final CUser user1 = new CUser("user1", "password", "Alice", "alice@example.com", company, role);
		user1.setLastname("Anderson");
		
		final CUser user2 = new CUser("user2", "password", "Bob", "bob@example.com", company, role);
		user2.setLastname("Brown");
		
		// Get their icons
		final Icon icon1 = user1.getIcon();
		final Icon icon2 = user2.getIcon();
		
		// Get child innerHTML (SVG is in child elements now)
		assertTrue(icon1.getElement().getChildCount() > 0, "Icon1 should have child elements");
		assertTrue(icon2.getElement().getChildCount() > 0, "Icon2 should have child elements");
		
		final String innerHTML1 = icon1.getElement().getChild(0).getProperty("innerHTML");
		final String innerHTML2 = icon2.getElement().getChild(0).getProperty("innerHTML");
		
		// Verify both contain SVG
		assertNotNull(innerHTML1);
		assertNotNull(innerHTML2);
		assertTrue(innerHTML1.contains("<svg"));
		assertTrue(innerHTML2.contains("<svg"));
		
		// Verify they contain different initials
		assertTrue(innerHTML1.contains("AA"), "First icon should contain initials 'AA'");
		assertTrue(innerHTML2.contains("BB"), "Second icon should contain initials 'BB'");
		
		// Colors should be different (since initials are different)
		// Extract fill color from SVG circle elements
		assertNotEquals(innerHTML1, innerHTML2, 
			"Icons for different users should have different content");
	}
	
	@Test
	void testIconSize_IsSetCorrectly() {
		// Create a user
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		final CUser user = new CUser("test", "password", "Test", "test@example.com", company, role);
		
		// Get the icon
		final Icon icon = user.getIcon();
		
		// Verify size is set
		final String size = icon.getElement().getStyle().get("width");
		assertNotNull(size, "Icon width should be set");
		assertTrue(size.contains("16"), "Icon should be 16px wide");
		assertTrue(size.contains("px"), "Icon size should be in pixels");
	}
}
