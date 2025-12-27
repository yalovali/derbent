package tech.derbent.base.users.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.utils.CImageUtils;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.roles.domain.CUserCompanyRole;

/** Test class for user SVG icon functionality. Verifies that icons are properly generated using SVG data URLs with Vaadin's Icon component. */
class CUserSvgIconTest {

	
	@Test
	void testGenerateAvatarSvg_ConsistentColors() {
		// Generate avatars with the same initials multiple times
		final String svg1 = CImageUtils.generateAvatarSvg("AB", 16);
		final String svg2 = CImageUtils.generateAvatarSvg("AB", 16);
		// Colors should be consistent for the same initials
		assertEquals(svg1, svg2, "SVG should be identical for same initials");
	}

	
	@Test
	void testGenerateAvatarSvg_CreatesValidSvg() {
		// Test the SVG generation utility
		final String svgContent = CImageUtils.generateAvatarSvg("JD", 16);
		// Verify SVG structure
		assertNotNull(svgContent, "SVG content should not be null");
		assertTrue(svgContent.startsWith("<svg"), "Should start with SVG tag");
		assertTrue(svgContent.contains("circle"), "Should contain a circle element");
		assertTrue(svgContent.contains("text"), "Should contain a text element");
		assertTrue(svgContent.contains("JD"), "Should contain the initials");
		assertTrue(svgContent.endsWith("</svg>"), "Should end with closing SVG tag");
	}

	
	@Test
	void testGenerateAvatarSvg_DifferentColors() {
		// Generate avatars with different initials
		final String svg1 = CImageUtils.generateAvatarSvg("AA", 16);
		final String svg2 = CImageUtils.generateAvatarSvg("BB", 16);
		// Colors should be different for different initials
		assertNotEquals(svg1, svg2, "SVG should be different for different initials");
	}

	
	@Test
	void testGetIcon_WithoutProfilePicture_GeneratesSvgAvatar() {
		// Create a user without a profile picture
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		final CUser user = new CUser("testuser", "password", "John Doe", "john@example.com", company, role);
		// Get the icon
		final Icon icon = user.getIcon();
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		// With the new element replacement approach, SVG is directly in the element's innerHTML
		final String innerHTML = icon.getElement().getProperty("innerHTML");
		assertNotNull(innerHTML, "Element innerHTML should contain SVG");
		assertTrue(innerHTML.startsWith("<svg"), "Element innerHTML should be SVG content");
		assertTrue(innerHTML.contains("svg"), "Element innerHTML should contain SVG tags");
	}

	
	@Test
	void testGetIcon_WithProfilePicture_UsesImageData() throws Exception {
		// Create a user with a profile picture (generate a simple PNG)
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		final CUser user = new CUser("testuser", "password", "Jane Smith", "jane@example.com", company, role);
		// Generate a test avatar image
		final byte[] avatarData = CImageUtils.generateAvatarWithInitials("JS", 16);
		user.setProfilePictureData(avatarData);
		// Get the icon
		final Icon icon = user.getIcon();
		// Verify icon is not null
		assertNotNull(icon, "Icon should not be null");
		// With the new element replacement approach, SVG is directly in the element's innerHTML
		final String innerHTML = icon.getElement().getProperty("innerHTML");
		assertNotNull(innerHTML, "Element innerHTML should contain SVG");
		assertTrue(innerHTML.startsWith("<svg"), "Element innerHTML should be SVG content");
		assertTrue(innerHTML.contains("image"), "Element innerHTML should contain an image element");
	}

	
	@Test
	void testGetInitials_ExtractsCorrectly() {
		final CCompany company = new CCompany("Test Company");
		final CUserCompanyRole role = new CUserCompanyRole();
		role.setName("Admin");
		// Test with first and last name
		final CUser user1 = new CUser("jdoe", "password", "John", "john@example.com", company, role);
		user1.setLastname("Doe");
		assertEquals("JD", user1.getInitials(), "Should extract first and last name initials");
		// Test with only first name (single word gives single initial, needs last name for second)
		final CUser user2 = new CUser("jane", "password", "Jane", "jane@example.com", company, role);
		assertEquals("J", user2.getInitials(), "Should extract initial from first name only");
		// Test with multi-word first name
		final CUser user3 = new CUser("jsmith", "password", "John Smith", "jsmith@example.com", company, role);
		assertEquals("JS", user3.getInitials(), "Should extract initials from multi-word first name");
	}
}
