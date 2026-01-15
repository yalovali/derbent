package tech.derbent.app.invoices.invoice.service;

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
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.orders.currency.domain.CCurrency;
import tech.derbent.app.orders.currency.service.CCurrencyService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;

public class CInvoiceInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CInvoice.class;
	private static final Logger LOGGER = LoggerFactory.getLogger(CInvoiceInitializerService.class);
	private static final String menuOrder = Menu_Order_PROJECT + ".20";
	private static final String menuTitle = MenuTitle_PROJECT + ".Invoices";
	private static final String pageDescription = "Invoice and payment management";
	private static final String pageTitle = "Invoice Management";
	private static final boolean showInQuickToolbar = false;

	public static CDetailSection createBasicView(final CProject project) throws Exception {
		try {
			final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
			CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "invoiceNumber"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "invoiceDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "dueDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "status"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "paymentStatus"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Customer Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "customerName"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "customerEmail"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "customerAddress"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "customerTaxId"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Financial Details"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "currency"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "subtotal"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "taxRate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "taxAmount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "discountRate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "discountAmount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "totalAmount"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "paidAmount"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Line Items"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "invoiceItems"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Payments"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "payments"));

			detailSection.addScreenLine(CDetailLinesService.createSection("Context"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "project"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "issuedBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "paymentTerms"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));

			// Attachments section
			tech.derbent.app.attachments.service.CAttachmentInitializerService.addAttachmentsSection(detailSection, clazz);

			// Comments section
			tech.derbent.app.comments.service.CCommentInitializerService.addCommentsSection(detailSection, clazz);

			detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));
			detailSection.debug_printScreenInformation();
			return detailSection;
		} catch (final Exception e) {
			LOGGER.error("Error creating invoice view.");
			throw e;
		}
	}

	public static CGridEntity createGridEntity(final CProject project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "invoiceNumber", "invoiceDate", "dueDate", "customerName",
				"totalAmount", "paidAmount", "paymentStatus", "status", "project", "issuedBy", "createdDate"));
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
		final CInvoiceService invoiceService = (CInvoiceService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final List<CInvoice> existingInvoices = invoiceService.findAll();
		if (!existingInvoices.isEmpty()) {
			LOGGER.info("Clearing {} existing invoices for project: {}", existingInvoices.size(), project.getName());
			for (final CInvoice existingInvoice : existingInvoices) {
				try {
					invoiceService.delete(existingInvoice);
				} catch (final Exception e) {
					LOGGER.warn("Could not delete existing invoice {}: {}", existingInvoice.getId(), e.getMessage());
				}
			}
		}

		final String[][] nameAndDescriptions = {
				{ "Invoice Q1-2026", "Invoice for Q1 project deliverables" },
				{ "Invoice Q2-2026", "Invoice for Q2 project deliverables" },
				{ "Consulting Services Invoice", "Professional consulting services rendered" },
				{ "Development Phase 1", "First phase of development work" },
				{ "Maintenance Services", "Monthly maintenance and support services" }
		};
		initializeProjectEntity(nameAndDescriptions,
				(CEntityOfProjectService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), project, minimal,
				(item, index) -> {
					final CInvoice invoice = (CInvoice) item;
					final CUser user = CSpringContext.getBean(CUserService.class).getRandom(project.getCompany());
					invoice.setIssuedBy(user);

					// Set invoice number
					invoice.setInvoiceNumber(String.format("INV-%s-%04d", project.getCompany().getName().substring(0, 3).toUpperCase(),
							1000 + index));

					// Set dates
					invoice.setInvoiceDate(LocalDate.now().minusDays(30L - index * 5));
					invoice.setDueDate(invoice.getInvoiceDate().plusDays(30));

					// Set customer information
					invoice.setCustomerName("Customer " + (index + 1));
					invoice.setCustomerEmail("customer" + (index + 1) + "@example.com");
					invoice.setCustomerAddress("123 Main St, City, Country");

					// Set currency
					final CCurrency currency = CSpringContext.getBean(CCurrencyService.class).getRandom(project);
					invoice.setCurrency(currency);

					// Set financial amounts
					invoice.setSubtotal(new BigDecimal(5000 + index * 1000));
					invoice.setTaxRate(new BigDecimal("20.00"));
					invoice.setDiscountRate(new BigDecimal("5.00"));

					// Calculate amounts
					invoice.recalculateAmounts();

					// Set partial payment for some invoices
					if (index % 2 == 0) {
						invoice.setPaidAmount(invoice.getTotalAmount().multiply(new BigDecimal("0.5")));
					} else if (index % 3 == 0) {
						invoice.setPaidAmount(invoice.getTotalAmount());
					}

					invoice.updatePaymentStatus();
				});
	}
}
