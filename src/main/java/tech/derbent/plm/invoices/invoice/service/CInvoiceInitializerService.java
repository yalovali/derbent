package tech.derbent.plm.invoices.invoice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
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
import tech.derbent.plm.attachments.service.CAttachmentInitializerService;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.invoiceitem.domain.CInvoiceItem;
import tech.derbent.plm.invoices.invoiceitem.service.CInvoiceItemService;
import tech.derbent.plm.invoices.payment.domain.CPayment;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;
import tech.derbent.plm.invoices.payment.service.CPaymentService;
import tech.derbent.plm.milestones.milestone.domain.CMilestone;
import tech.derbent.plm.milestones.milestone.service.CMilestoneService;
import tech.derbent.plm.orders.currency.domain.CCurrency;
import tech.derbent.plm.orders.currency.service.CCurrencyService;
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

	public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
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
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "relatedMilestone"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "isMilestonePayment"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Payment Plan"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "paymentPlanInstallments"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "installmentNumber"));
			detailSection.addScreenLine(CDetailLinesService.createSection("Additional Information"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "paymentTerms"));
			detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "notes"));
			// Attachments section
			CAttachmentInitializerService.addDefaultSection(detailSection, clazz);
			// Comments section
			CCommentInitializerService.addDefaultSection(detailSection, clazz);
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

	public static CGridEntity createGridEntity(final CProject<?> project) {
		final CGridEntity grid = createBaseGridEntity(project, clazz);
		grid.setColumnFields(List.of("id", "name", "invoiceNumber", "invoiceDate", "dueDate", "customerName", "totalAmount", "paidAmount",
				"paymentStatus", "isMilestonePayment", "relatedMilestone", "installmentNumber", "paymentPlanInstallments", "status", "project",
				"issuedBy", "createdDate"));
		return grid;
	}

	public static void initialize(final CProject<?> project, final CGridEntityService gridEntityService,
			final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
		final CDetailSection detailSection = createBasicView(project);
		final CGridEntity grid = createGridEntity(project);
		initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, menuTitle, pageTitle,
				pageDescription, showInQuickToolbar, menuOrder);
	}

	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CInvoiceService invoiceService = (CInvoiceService) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz));
		final CInvoiceItemService invoiceItemService = CSpringContext.getBean(CInvoiceItemService.class);
		final CPaymentService paymentService = CSpringContext.getBean(CPaymentService.class);
		final CMilestoneService milestoneService = CSpringContext.getBean(CMilestoneService.class);
		final CCurrencyService currencyService = CSpringContext.getBean(CCurrencyService.class);
		final CUserService userService = CSpringContext.getBean(CUserService.class);
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
		final CCurrency currency = currencyService.getRandom(project);
		final CUser issuer = userService.getRandom(project.getCompany());
		final List<CMilestone> milestones = milestoneService.findAll();
		LOGGER.info("Creating comprehensive financial sample data with real-world scenarios...");
		// Scenario 1: Milestone-based payment invoice (Paid in full upon milestone acceptance)
		LOGGER.info("Creating Scenario 1: Milestone Acceptance Payment Invoice");
		CInvoice invoice1 = new CInvoice("Milestone Alpha Release Payment", project);
		invoice1.setInvoiceNumber(String.format("INV-%s-1001", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice1.setInvoiceDate(LocalDate.now().minusDays(45));
		invoice1.setDueDate(invoice1.getInvoiceDate().plusDays(30));
		invoice1.setCustomerName("Tech Solutions Inc.");
		invoice1.setCustomerEmail("billing@techsolutions.com");
		invoice1.setCustomerAddress("123 Innovation Drive, San Francisco, CA 94105, USA");
		invoice1.setCustomerTaxId("TAX-12345678");
		invoice1.setCurrency(currency);
		invoice1.setIssuedBy(issuer);
		invoice1.setIsMilestonePayment(true);
		if (!milestones.isEmpty()) {
			invoice1.setRelatedMilestone(milestones.get(0));
		}
		invoice1.setPaymentTerms("Net 30 - Milestone acceptance payment due upon completion verification");
		invoice1.setTaxRate(new BigDecimal("20.00"));
		invoice1.setDiscountRate(BigDecimal.ZERO);
		invoice1 = invoiceService.save(invoice1);
		// Add line items for milestone invoice
		final CInvoiceItem item1_1 = new CInvoiceItem(invoice1, 1);
		item1_1.setDescription("Alpha Release Development");
		item1_1.setQuantity(new BigDecimal("1"));
		item1_1.setUnitPrice(new BigDecimal("15000.00"));
		item1_1.calculateLineTotal();
		invoiceItemService.save(item1_1);
		final CInvoiceItem item1_2 = new CInvoiceItem(invoice1, 2);
		item1_2.setDescription("Testing & QA Services");
		item1_2.setQuantity(new BigDecimal("1"));
		item1_2.setUnitPrice(new BigDecimal("3000.00"));
		item1_2.calculateLineTotal();
		invoiceItemService.save(item1_2);
		invoice1.recalculateAmounts();
		invoice1.setPaidAmount(invoice1.getTotalAmount());
		invoice1.updatePaymentStatus();
		invoiceService.save(invoice1);
		// Create payment record for milestone invoice
		final CPayment payment1 = new CPayment(invoice1, invoice1.getTotalAmount());
		payment1.setPaymentDate(invoice1.getDueDate().minusDays(5));
		payment1.setPaymentMethod("Bank Transfer");
		payment1.setReferenceNumber("REF-ML-ALPHA-001");
		payment1.setReceivedBy(issuer);
		payment1.setStatus(CPaymentStatus.PAID);
		payment1.setNotes("Payment received upon Alpha milestone acceptance");
		paymentService.save(payment1);
		if (minimal) {
			return;
		}
		// Scenario 2: Payment Plan Invoice with 4 installments (Currently on installment 2)
		LOGGER.info("Creating Scenario 2: Payment Plan Invoice - Installment 2 of 4");
		CInvoice invoice2 = new CInvoice("Annual License Payment - Installment 2", project);
		invoice2.setInvoiceNumber(String.format("INV-%s-1002", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice2.setInvoiceDate(LocalDate.now().minusDays(60));
		invoice2.setDueDate(LocalDate.now().minusDays(30));
		invoice2.setCustomerName("Global Enterprises Ltd.");
		invoice2.setCustomerEmail("accounts@globalent.com");
		invoice2.setCustomerAddress("456 Business Park, London EC1A 1BB, UK");
		invoice2.setCustomerTaxId("GB123456789");
		invoice2.setCurrency(currency);
		invoice2.setIssuedBy(issuer);
		invoice2.setPaymentPlanInstallments(4);
		invoice2.setInstallmentNumber(2);
		invoice2.setPaymentTerms("Quarterly payment plan - 4 equal installments");
		invoice2.setTaxRate(new BigDecimal("20.00"));
		invoice2 = invoiceService.save(invoice2);
		final CInvoiceItem item2_1 = new CInvoiceItem(invoice2, 1);
		item2_1.setDescription("Enterprise License Q2 Installment");
		item2_1.setQuantity(new BigDecimal("1"));
		item2_1.setUnitPrice(new BigDecimal("12500.00"));
		item2_1.calculateLineTotal();
		invoiceItemService.save(item2_1);
		invoice2.recalculateAmounts();
		invoice2.setPaidAmount(invoice2.getTotalAmount());
		invoice2.updatePaymentStatus();
		invoiceService.save(invoice2);
		// Payment for installment 2
		final CPayment payment2 = new CPayment(invoice2, invoice2.getTotalAmount());
		payment2.setPaymentDate(invoice2.getDueDate().minusDays(2));
		payment2.setPaymentMethod("Credit Card");
		payment2.setReferenceNumber("CC-Q2-2026-789");
		payment2.setReceivedBy(issuer);
		payment2.setStatus(CPaymentStatus.PAID);
		payment2.setNotes("Second quarterly installment - on time payment");
		paymentService.save(payment2);
		// Scenario 3: Partial Payment Invoice (50% paid, balance due)
		LOGGER.info("Creating Scenario 3: Partial Payment Invoice");
		CInvoice invoice3 = new CInvoice("Custom Development Project", project);
		invoice3.setInvoiceNumber(String.format("INV-%s-1003", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice3.setInvoiceDate(LocalDate.now().minusDays(20));
		invoice3.setDueDate(LocalDate.now().plusDays(10));
		invoice3.setCustomerName("StartupCo Technologies");
		invoice3.setCustomerEmail("finance@startupco.io");
		invoice3.setCustomerAddress("789 Startup Lane, Austin, TX 78701, USA");
		invoice3.setCustomerTaxId("EIN-98-7654321");
		invoice3.setCurrency(currency);
		invoice3.setIssuedBy(issuer);
		invoice3.setPaymentTerms("50% upfront, 50% upon delivery");
		invoice3.setTaxRate(new BigDecimal("18.00"));
		invoice3.setDiscountRate(new BigDecimal("10.00"));
		invoice3 = invoiceService.save(invoice3);
		final CInvoiceItem item3_1 = new CInvoiceItem(invoice3, 1);
		item3_1.setDescription("Backend API Development");
		item3_1.setQuantity(new BigDecimal("160"));
		item3_1.setUnitPrice(new BigDecimal("50.00"));
		item3_1.calculateLineTotal();
		invoiceItemService.save(item3_1);
		final CInvoiceItem item3_2 = new CInvoiceItem(invoice3, 2);
		item3_2.setDescription("Frontend UI/UX Design");
		item3_2.setQuantity(new BigDecimal("90"));
		item3_2.setUnitPrice(new BigDecimal("50.00"));
		item3_2.calculateLineTotal();
		invoiceItemService.save(item3_2);
		invoice3.recalculateAmounts();
		invoice3.setPaidAmount(invoice3.getTotalAmount().multiply(new BigDecimal("0.5")));
		invoice3.updatePaymentStatus();
		invoiceService.save(invoice3);
		// Partial payment record
		final CPayment payment3 = new CPayment(invoice3, invoice3.getTotalAmount().multiply(new BigDecimal("0.5")));
		payment3.setPaymentDate(invoice3.getInvoiceDate().plusDays(2));
		payment3.setPaymentMethod("Wire Transfer");
		payment3.setReferenceNumber("WIRE-DEV-2026-456");
		payment3.setReceivedBy(issuer);
		payment3.setStatus(CPaymentStatus.PAID);
		payment3.setNotes("50% upfront payment received - balance due upon delivery");
		paymentService.save(payment3);
		// Scenario 4: Overdue Invoice (Late payment)
		LOGGER.info("Creating Scenario 4: Overdue Invoice");
		CInvoice invoice4 = new CInvoice("Consulting Services - January 2026", project);
		invoice4.setInvoiceNumber(String.format("INV-%s-1004", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice4.setInvoiceDate(LocalDate.now().minusDays(50));
		invoice4.setDueDate(LocalDate.now().minusDays(20));
		invoice4.setCustomerName("Legacy Systems Corp");
		invoice4.setCustomerEmail("payables@legacysystems.com");
		invoice4.setCustomerAddress("321 Old Town Road, Boston, MA 02108, USA");
		invoice4.setCurrency(currency);
		invoice4.setIssuedBy(issuer);
		invoice4.setPaymentTerms("Net 30 - Late payment subject to 1.5% monthly interest");
		invoice4.setTaxRate(new BigDecimal("20.00"));
		invoice4 = invoiceService.save(invoice4);
		final CInvoiceItem item4_1 = new CInvoiceItem(invoice4, 1);
		item4_1.setDescription("Technical Consulting Hours");
		item4_1.setQuantity(new BigDecimal("40"));
		item4_1.setUnitPrice(new BigDecimal("150.00"));
		item4_1.calculateLineTotal();
		invoiceItemService.save(item4_1);
		invoice4.recalculateAmounts();
		invoice4.setPaidAmount(BigDecimal.ZERO);
		invoice4.updatePaymentStatus();
		invoiceService.save(invoice4);
		// Scenario 5: Beta Release Milestone Payment (Pending payment)
		LOGGER.info("Creating Scenario 5: Beta Release Milestone Payment (Pending)");
		CInvoice invoice5 = new CInvoice("Milestone Beta Release Payment", project);
		invoice5.setInvoiceNumber(String.format("INV-%s-1005", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice5.setInvoiceDate(LocalDate.now().minusDays(10));
		invoice5.setDueDate(LocalDate.now().plusDays(20));
		invoice5.setCustomerName("Tech Solutions Inc.");
		invoice5.setCustomerEmail("billing@techsolutions.com");
		invoice5.setCustomerAddress("123 Innovation Drive, San Francisco, CA 94105, USA");
		invoice5.setCustomerTaxId("TAX-12345678");
		invoice5.setCurrency(currency);
		invoice5.setIssuedBy(issuer);
		invoice5.setIsMilestonePayment(true);
		if (milestones.size() > 1) {
			invoice5.setRelatedMilestone(milestones.get(1));
		}
		invoice5.setPaymentTerms("Net 30 - Beta milestone acceptance payment");
		invoice5.setTaxRate(new BigDecimal("20.00"));
		invoice5 = invoiceService.save(invoice5);
		final CInvoiceItem item5_1 = new CInvoiceItem(invoice5, 1);
		item5_1.setDescription("Beta Release Development");
		item5_1.setQuantity(new BigDecimal("1"));
		item5_1.setUnitPrice(new BigDecimal("18000.00"));
		item5_1.calculateLineTotal();
		invoiceItemService.save(item5_1);
		final CInvoiceItem item5_2 = new CInvoiceItem(invoice5, 2);
		item5_2.setDescription("User Acceptance Testing");
		item5_2.setQuantity(new BigDecimal("1"));
		item5_2.setUnitPrice(new BigDecimal("4000.00"));
		item5_2.calculateLineTotal();
		invoiceItemService.save(item5_2);
		invoice5.recalculateAmounts();
		invoice5.setPaidAmount(BigDecimal.ZERO);
		invoice5.updatePaymentStatus();
		invoiceService.save(invoice5);
		// Scenario 6: Payment Plan - Installment 3 of 4 (Due soon)
		LOGGER.info("Creating Scenario 6: Payment Plan - Installment 3 of 4 (Due Soon)");
		CInvoice invoice6 = new CInvoice("Annual License Payment - Installment 3", project);
		invoice6.setInvoiceNumber(String.format("INV-%s-1006", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice6.setInvoiceDate(LocalDate.now().minusDays(5));
		invoice6.setDueDate(LocalDate.now().plusDays(25));
		invoice6.setCustomerName("Global Enterprises Ltd.");
		invoice6.setCustomerEmail("accounts@globalent.com");
		invoice6.setCustomerAddress("456 Business Park, London EC1A 1BB, UK");
		invoice6.setCustomerTaxId("GB123456789");
		invoice6.setCurrency(currency);
		invoice6.setIssuedBy(issuer);
		invoice6.setPaymentPlanInstallments(4);
		invoice6.setInstallmentNumber(3);
		invoice6.setPaymentTerms("Quarterly payment plan - 4 equal installments");
		invoice6.setTaxRate(new BigDecimal("20.00"));
		invoice6 = invoiceService.save(invoice6);
		final CInvoiceItem item6_1 = new CInvoiceItem(invoice6, 1);
		item6_1.setDescription("Enterprise License Q3 Installment");
		item6_1.setQuantity(new BigDecimal("1"));
		item6_1.setUnitPrice(new BigDecimal("12500.00"));
		item6_1.calculateLineTotal();
		invoiceItemService.save(item6_1);
		invoice6.recalculateAmounts();
		invoice6.setPaidAmount(BigDecimal.ZERO);
		invoice6.updatePaymentStatus();
		invoiceService.save(invoice6);
		// Scenario 7: Maintenance & Support Subscription (Monthly recurring)
		LOGGER.info("Creating Scenario 7: Monthly Maintenance Subscription");
		CInvoice invoice7 = new CInvoice("Monthly Maintenance & Support - February 2026", project);
		invoice7.setInvoiceNumber(String.format("INV-%s-1007", project.getCompany().getName().substring(0, 3).toUpperCase()));
		invoice7.setInvoiceDate(LocalDate.now().minusDays(15));
		invoice7.setDueDate(LocalDate.now().plusDays(15));
		invoice7.setCustomerName("RetailChain Solutions");
		invoice7.setCustomerEmail("it-billing@retailchain.com");
		invoice7.setCustomerAddress("999 Commerce Blvd, Chicago, IL 60601, USA");
		invoice7.setCurrency(currency);
		invoice7.setIssuedBy(issuer);
		invoice7.setPaymentTerms("Net 30 - Recurring monthly subscription");
		invoice7.setTaxRate(new BigDecimal("20.00"));
		invoice7 = invoiceService.save(invoice7);
		final CInvoiceItem item7_1 = new CInvoiceItem(invoice7, 1);
		item7_1.setDescription("Premium Support Package");
		item7_1.setQuantity(new BigDecimal("1"));
		item7_1.setUnitPrice(new BigDecimal("2500.00"));
		item7_1.calculateLineTotal();
		invoiceItemService.save(item7_1);
		final CInvoiceItem item7_2 = new CInvoiceItem(invoice7, 2);
		item7_2.setDescription("System Monitoring");
		item7_2.setQuantity(new BigDecimal("1"));
		item7_2.setUnitPrice(new BigDecimal("500.00"));
		item7_2.calculateLineTotal();
		invoiceItemService.save(item7_2);
		invoice7.recalculateAmounts();
		invoice7.setPaidAmount(BigDecimal.ZERO);
		invoice7.updatePaymentStatus();
		invoiceService.save(invoice7);
		LOGGER.info("Successfully created 7 comprehensive financial scenario invoices with realistic data");
	}
}
