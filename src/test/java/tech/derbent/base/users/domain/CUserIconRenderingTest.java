package tech.derbent.base.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.roles.domain.CUserCompanyRole;
import tech.derbent.api.utils.CImageUtils;

/** Test class to verify that user icons render correctly with the new innerHTML-based implementation. This test validates that icons are properly
 * generated and contain SVG content in their innerHTML. */

class CUserIconRenderingTest {

	@Test
	void testGetIcon_WithoutProfilePicture_ContainsSvgInInnerHTML() {
		// Create a user without a profile picture
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole("Admin", company);
		final CUser user = new CUser("jdoe", "password", "John", "john@example.com", company, role);
		user.setLastname("Doe");
		// Get the icon
		final Icon icon = user.getIcon();
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		// With element replacement, SVG is directly in the icon element's innerHTML
		final String innerHTML = icon.getElement().getProperty("innerHTML");
		assertNotNull(innerHTML, "Icon element should contain SVG innerHTML");
		assertTrue(innerHTML.startsWith("<svg"), "Icon innerHTML should start with <svg tag");
		assertTrue(innerHTML.contains("</svg>"), "Icon innerHTML should end with </svg> tag");
		assertTrue(innerHTML.contains("circle"), "Icon innerHTML should contain a circle element for avatar background");
		assertTrue(innerHTML.contains("text"), "Icon innerHTML should contain a text element for initials");
		assertTrue(innerHTML.contains("JD"), "Icon innerHTML should contain the user's initials 'JD'");
		// Verify the element is a span, not vaadin-icon
		assertEquals("span", icon.getElement().getTag(), "Icon element should be a span to support custom SVG");
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
		final byte[] avatarData = CImageUtils.generateAvatarWithInitials("JS", 16);
		user.setProfilePictureData(avatarData);
		// Get the icon
		final Icon icon = user.getIcon();
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		// With element replacement, SVG is directly in the icon element's innerHTML
		final String innerHTML = icon.getElement().getProperty("innerHTML");
		assertNotNull(innerHTML, "Icon element innerHTML should contain SVG");
		assertTrue(innerHTML.startsWith("<svg"), "Icon innerHTML should start with <svg tag");
		assertTrue(innerHTML.contains("</svg>"), "Icon innerHTML should end with </svg> tag");
		assertTrue(innerHTML.contains("image"), "Icon innerHTML should contain an image element for profile picture");
		assertTrue(innerHTML.contains("href=\"data:"), "Icon innerHTML should contain a data URL for the embedded image");
		// Verify the element is a span
		assertEquals("span", icon.getElement().getTag(), "Icon element should be a span to support custom SVG");
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
		// Get innerHTML directly from the elements (element replacement approach)
		final String innerHTML1 = icon1.getElement().getProperty("innerHTML");
		final String innerHTML2 = icon2.getElement().getProperty("innerHTML");
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
		assertNotEquals(innerHTML1, innerHTML2, "Icons for different users should have different content");
	}
}
