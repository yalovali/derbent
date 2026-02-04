package tech.derbent.api.companies.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;

public class CCompanyInitializerService extends CInitializerServiceBase {

	private record CompanySeed(String name, String description, String address, String phone, String email, String website, String taxNumber,
			String theme, String logoUrl, String primaryColor, String workingHoursStart, String workingHoursEnd, String timezone, String language,
			boolean notificationsEnabled, String notificationEmail) {}

	private static final String BAB_COMPANY_DESCRIPTION = "BAB Gateway IOT Management";
	private static final String BAB_COMPANY_EMAIL = "contact@babgateway.local";
	private static final String BAB_COMPANY_NAME = "BAB Gateway";
	private static final String BAB_COMPANY_PRIMARY_COLOR = "#0f4c81";
	static final Class<?> clazz = CCompany.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCompanyInitializerService.class);
	private static final String menuOrder = Menu_Order_SYSTEM + ".1";
	private static final String menuTitle = "Companies";
	private static final String pageDescription = "Company management with contact details";
	private static final String pageTitle = "Company Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			// Basic Company Information
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "address"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "phone"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "email"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "website"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));
			// Company Branding & UI Settings
			detailSection.addScreenLine(CDetailLinesService.createSection("Company Branding & UI"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyTheme"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyLogoUrl"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "primaryColor"));
			// Business Operations
			if (!CSpringContext.isBabProfile()) {
				detailSection.addScreenLine(CDetailLinesService.createSection("Business Operations"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "taxNumber"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workingHoursStart"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "workingHoursEnd"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyTimezone"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "defaultLanguage"));
				// Notification Settings
				detailSection.addScreenLine(CDetailLinesService.createSection("Notification Settings"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "enableNotifications"));
				detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notificationEmail"));
			}
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating company view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "description", "address", "phone", "email", "website", "companyTheme", "primaryColor",
				"enableNotifications", "active"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		CDetailSection detailSection = createBasicView(project);
		CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, MenuTitle_DEVELOPMENT + menuTitle,
				pageTitle, pageDescription, showInQuickToolbar, menuOrder);
		// create a single company page
		grid = createGridEntity(project);
		detailSection = createBasicView(project);
		detailSection.setName("Current Company Detail Section");
		grid.setName("Current Company Grid");
		grid.setAttributeNone(true);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, "System.Current Company", pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final boolean minimal) throws Exception {
		final CCompanyService companyService = CSpringContext.getBean(CCompanyService.class);
		final List<CompanySeed> seeds = List.of(
				new CompanySeed("Of Teknoloji Çözümleri", "Dijital dönüşüm için yenilikçi teknoloji çözümleri",
						"Cumhuriyet Mahallesi, Atatürk Caddesi No:15, Of/Trabzon", "+90-462-751-0101", "iletisim@ofteknoloji.com.tr",
						"https://www.ofteknoloji.com.tr", "TR-123456789", "lumo-dark", "/assets/logos/tech-logo.svg", "#1976d2", "09:00", "18:00",
						"Europe/Istanbul", "tr", true, "bildirim@ofteknoloji.com.tr"),
				new CompanySeed("Of Stratejik Danışmanlık", "Yönetim danışmanlığı ve stratejik planlama hizmetleri",
						"Merkez Mahallesi, Gülbahar Sokağı No:7, Of/Trabzon", "+90-462-751-0303", "merhaba@ofdanismanlik.com.tr",
						"https://www.ofdanismanlik.com.tr", "TR-456789123", "lumo-light", "/assets/logos/consulting-logo.svg", "#4caf50", "08:30",
						"17:30", "Europe/Istanbul", "tr", true, "bildirim@ofdanismanlik.com.tr"));
		final boolean skipSecond = true;
		for (final CompanySeed seed : seeds) {
			final CCompany company = new CCompany(seed.name());
			company.setDescription(seed.description());
			company.setAddress(seed.address());
			company.setPhone(seed.phone());
			company.setEmail(seed.email());
			company.setWebsite(seed.website());
			company.setTaxNumber(seed.taxNumber());
			company.setCompanyTheme(seed.theme());
			company.setCompanyLogoUrl(seed.logoUrl());
			company.setPrimaryColor(seed.primaryColor());
			company.setWorkingHoursStart(seed.workingHoursStart());
			company.setWorkingHoursEnd(seed.workingHoursEnd());
			company.setCompanyTimezone(seed.timezone());
			company.setDefaultLanguage(seed.language());
			company.setEnableNotifications(seed.notificationsEnabled());
			company.setNotificationEmail(seed.notificationEmail());
			company.setActive(true);
			companyService.save(company);
			if (skipSecond) {
				break;
			}
			if (minimal) {
				break;
			}
		}
	}

	/** @param minimal */
	public static CCompany initializeSampleBab(final boolean minimal) throws Exception {
		final CCompanyService companyService = CSpringContext.getBean(CCompanyService.class);
		final CCompany company = companyService.newEntity(BAB_COMPANY_NAME);
		company.setDescription(BAB_COMPANY_DESCRIPTION);
		company.setPrimaryColor(BAB_COMPANY_PRIMARY_COLOR);
		company.setEmail(BAB_COMPANY_EMAIL);
		company.setEnableNotifications(Boolean.TRUE);
		company.setActive(true);
		companyService.save(company);
		return company;
	}
}
