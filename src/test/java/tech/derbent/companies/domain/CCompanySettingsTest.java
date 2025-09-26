package tech.derbent.companies.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Test class for CCompany configuration settings functionality. Validates that the new company settings follow the CSystemSettings pattern. */
class CCompanySettingsTest {

	@Test
	void testCompanyDefaultSettings() {
		// Create a new company
		CCompany company = new CCompany("Test Company");
		// Verify default values are set correctly
		assertEquals("lumo-light", company.getCompanyTheme());
		assertEquals("#1976d2", company.getPrimaryColor());
		assertEquals("09:00", company.getWorkingHoursStart());
		assertEquals("17:00", company.getWorkingHoursEnd());
		assertEquals("Europe/Istanbul", company.getCompanyTimezone());
		assertEquals("tr", company.getDefaultLanguage());
		assertTrue(company.getEnableNotifications());
	}

	@Test
	void testCompanySettingsConfiguration() {
		// Create a new company
		CCompany company = new CCompany("Configuration Test Company");
		// Set custom configuration values
		company.setCompanyTheme("lumo-dark");
		company.setCompanyLogoUrl("/assets/custom-logo.svg");
		company.setPrimaryColor("#4caf50");
		company.setWorkingHoursStart("08:00");
		company.setWorkingHoursEnd("18:00");
		company.setCompanyTimezone("America/New_York");
		company.setDefaultLanguage("en");
		company.setEnableNotifications(false);
		company.setNotificationEmail("test@example.com");
		// Verify settings are applied correctly
		assertEquals("lumo-dark", company.getCompanyTheme());
		assertEquals("/assets/custom-logo.svg", company.getCompanyLogoUrl());
		assertEquals("#4caf50", company.getPrimaryColor());
		assertEquals("08:00", company.getWorkingHoursStart());
		assertEquals("18:00", company.getWorkingHoursEnd());
		assertEquals("America/New_York", company.getCompanyTimezone());
		assertEquals("en", company.getDefaultLanguage());
		assertFalse(company.getEnableNotifications());
		assertEquals("test@example.com", company.getNotificationEmail());
	}

	@Test
	void testCompanyToString() {
		// Create a company with settings
		CCompany company = new CCompany("Test Company");
		company.setDescription("Test Description");
		company.setAddress("Test Address");
		company.setPhone("+90-123-456-7890");
		company.setEmail("test@example.com");
		company.setWebsite("https://test.com");
		company.setCompanyTheme("lumo-dark");
		company.setPrimaryColor("#ff5722");
		// Verify toString includes new settings
		String result = company.toString();
		assertTrue(result.contains("companyTheme='lumo-dark'"));
		assertTrue(result.contains("primaryColor='#ff5722'"));
		assertTrue(result.contains("Test Company"));
	}
}
