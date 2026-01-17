package tech.derbent.app.customers.customer.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.app.customers.customer.domain.CCustomer;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CCustomerInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CCustomer.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CCustomerInitializerService.class);
	private static final String menuOrder = Menu_Order_CRM + ".10";
	private static final String menuTitle = MenuTitle_CRM + ".Customers";
	private static final String pageDescription = "Customer account management";
	private static final String pageTitle = "Customer Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "assignedTo"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Company Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companyName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "industry"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "companySize"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "website"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "annualRevenue"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Primary Contact"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "primaryContactName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "primaryContactEmail"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "primaryContactPhone"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Address Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "billingAddress"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "shippingAddress"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Relationship Tracking"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "relationshipStartDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastInteractionDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lifetimeValue"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "customerNotes"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

			// Attachments section - standard section for ALL entities
			tech.derbent.app.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);

			// Comments section - standard section for discussion entities
			tech.derbent.app.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);

			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating customer view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "companyName", "entityType", "status", "industry", "annualRevenue",
				"lastInteractionDate", "lifetimeValue", "assignedTo", "createdBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
		final String[][] nameAndDescriptions = {
				{
						"Acme Corporation", "Leading technology solutions provider"
				}, {
						"TechStart Innovations", "Emerging startup in AI/ML space"
				}, {
						"Global Enterprises Ltd", "Fortune 500 multinational corporation"
				}
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CCustomer customer = (CCustomer) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					customer.setAssignedTo(user);

					// Set company-specific details based on index
					if (index == 0) {
						customer.setCompanyName("Acme Corporation");
						customer.setIndustry("Technology");
						customer.setCompanySize("201-500");
						customer.setWebsite("https://www.acmecorp.example.com");
						customer.setAnnualRevenue(new BigDecimal("5000000"));
						customer.setPrimaryContactName("John Smith");
						customer.setPrimaryContactEmail("john.smith@acmecorp.example.com");
						customer.setPrimaryContactPhone("+1-555-0100");
					} else if (index == 1) {
						customer.setCompanyName("TechStart Innovations");
						customer.setIndustry("Artificial Intelligence");
						customer.setCompanySize("11-50");
						customer.setWebsite("https://www.techstart.example.com");
						customer.setAnnualRevenue(new BigDecimal("500000"));
						customer.setPrimaryContactName("Sarah Johnson");
						customer.setPrimaryContactEmail("sarah.j@techstart.example.com");
						customer.setPrimaryContactPhone("+1-555-0200");
					} else if (index == 2) {
						customer.setCompanyName("Global Enterprises Ltd");
						customer.setIndustry("Manufacturing");
						customer.setCompanySize("500+");
						customer.setWebsite("https://www.globalent.example.com");
						customer.setAnnualRevenue(new BigDecimal("50000000"));
						customer.setPrimaryContactName("Michael Chen");
						customer.setPrimaryContactEmail("m.chen@globalent.example.com");
						customer.setPrimaryContactPhone("+1-555-0300");
					}

					customer.setRelationshipStartDate(LocalDate.now().minusMonths(index * 6));
					customer.setLastInteractionDate(LocalDate.now().minusDays(index * 7));
					customer.setLifetimeValue(customer.getAnnualRevenue().multiply(new BigDecimal("2.5")));
					customer.setBillingAddress("123 Main Street, Suite " + (100 + index * 10) + ", City, State, ZIP");
					customer.setShippingAddress("456 Commerce Drive, Building " + (index + 1) + ", City, State, ZIP");
					customer.setCustomerNotes("Sample customer record created during initialization.");
				});
	}
}
