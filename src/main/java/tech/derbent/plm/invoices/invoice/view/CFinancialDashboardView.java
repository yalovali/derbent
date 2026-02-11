package tech.derbent.plm.invoices.invoice.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;
import com.vaadin.flow.theme.lumo.LumoUtility.Margin;
import com.vaadin.flow.theme.lumo.LumoUtility.Padding;
import jakarta.annotation.security.RolesAllowed;
import tech.derbent.api.entity.view.CAbstractPage;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.projects.service.CProjectService;
import tech.derbent.api.ui.component.enhanced.CDashboardStatCard;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.invoice.service.CFinancialSummaryService;
import tech.derbent.plm.invoices.invoice.service.CInvoiceService;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;

/** CFinancialDashboardView - Financial summary dashboard for project management. Displays comprehensive financial metrics including invoices,
 * payments, income, expenses, and profitability. Provides period-based filtering and real-time financial reporting. */
@Route (value = "financial-dashboard", registerAtStartup = false)
@PageTitle ("Financial Dashboard")
@RolesAllowed ({
		"ADMIN", "USER"
})
public class CFinancialDashboardView extends CAbstractPage {

	public static final String DEFAULT_COLOR = "#FFD700"; // Gold - financial data
	public static final String DEFAULT_ICON = "vaadin:dollar";
	private static final Logger LOGGER = LoggerFactory.getLogger(CFinancialDashboardView.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "Financial Dashboard View";
	private CProject<?> currentProject;
	private Grid<CInvoice> dueSoonGrid;
	private DatePicker endDatePicker;
	private final CFinancialSummaryService financialSummaryService;
	private CDashboardStatCard netProfitCard;
	private CDashboardStatCard overdueInvoicesCard;
	private CDashboardStatCard profitMarginCard;
	private Button refreshButton;
	private Pre reportTextArea;
	private final ISessionService sessionService;
	private DatePicker startDatePicker;
	private CDashboardStatCard totalExpensesCard;
	private CDashboardStatCard totalIncomeCard;
	// UI Components
	private CDashboardStatCard totalInvoicedCard;
	private CDashboardStatCard totalOutstandingCard;
	private CDashboardStatCard totalPaidCard;

	public CFinancialDashboardView(final CFinancialSummaryService financialSummaryService,
			@SuppressWarnings ("unused") final CInvoiceService invoiceService, @SuppressWarnings ("unused") final CProjectService<?> projectService,
			final ISessionService sessionService) {
		this.financialSummaryService = financialSummaryService;
		this.sessionService = sessionService;
	}

	@Override
	public void beforeEnter(final BeforeEnterEvent event) {
		try {
			final Optional<CProject<?>> projectOpt = sessionService.getActiveProject();
			if (!projectOpt.isPresent()) {
				CNotificationService.showError("No project selected. Please select a project first.");
				event.forwardTo("home");
				return;
			}
			currentProject = projectOpt.get();
			initializeView();
			refreshDashboard();
		} catch (final Exception e) {
			LOGGER.error("Error initializing financial dashboard: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to initialize financial dashboard", e);
		}
	}

	private void createDateRangeFilter() {
		final HorizontalLayout filterLayout = new HorizontalLayout();
		filterLayout.addClassNames(Gap.MEDIUM, Margin.Bottom.LARGE);
		filterLayout.setWidthFull();
		filterLayout.setAlignItems(HorizontalLayout.Alignment.BASELINE);
		startDatePicker = new DatePicker("Start Date");
		startDatePicker.setValue(LocalDate.now().withDayOfMonth(1)); // First day of current month
		startDatePicker.setWidth("200px");
		endDatePicker = new DatePicker("End Date");
		endDatePicker.setValue(LocalDate.now()); // Today
		endDatePicker.setWidth("200px");
		refreshButton = new Button("Refresh Dashboard", VaadinIcon.REFRESH.create());
		refreshButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		refreshButton.addClickListener(event -> refreshDashboard());
		final Button thisMonthButton = new Button("This Month", VaadinIcon.CALENDAR.create());
		thisMonthButton.addClickListener(event -> {
			startDatePicker.setValue(LocalDate.now().withDayOfMonth(1));
			endDatePicker.setValue(LocalDate.now());
			refreshDashboard();
		});
		final Button thisQuarterButton = new Button("This Quarter", VaadinIcon.CALENDAR_CLOCK.create());
		thisQuarterButton.addClickListener(event -> {
			final LocalDate now = LocalDate.now();
			final int currentQuarter = (now.getMonthValue() - 1) / 3;
			startDatePicker.setValue(now.withMonth(currentQuarter * 3 + 1).withDayOfMonth(1));
			endDatePicker.setValue(LocalDate.now());
			refreshDashboard();
		});
		filterLayout.add(startDatePicker, endDatePicker, refreshButton, thisMonthButton, thisQuarterButton);
		add(filterLayout);
	}

	private void createDetailedReportSection() {
		final H2 sectionTitle = new H2("Detailed Financial Report");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.LARGE);
		reportTextArea = new Pre();
		reportTextArea.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("padding", "var(--lumo-space-m)")
				.set("border-radius", "var(--lumo-border-radius-m)").set("overflow-x", "auto").set("font-family", "monospace")
				.set("font-size", "var(--lumo-font-size-s)").set("white-space", "pre");
		reportTextArea.setWidthFull();
		add(sectionTitle, reportTextArea);
	}

	private void createDueSoonSection() {
		final H2 sectionTitle = new H2("Invoices Due Soon (Next 30 Days)");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.LARGE);
		dueSoonGrid = new Grid<>(CInvoice.class, false);
		dueSoonGrid.addColumn(CInvoice::getInvoiceNumber).setHeader("Invoice #").setAutoWidth(true);
		dueSoonGrid.addColumn(CInvoice::getName).setHeader("Description").setAutoWidth(true);
		dueSoonGrid.addColumn(inv -> formatCurrency(inv.getRemainingBalance())).setHeader("Balance Due").setAutoWidth(true);
		dueSoonGrid.addColumn(CInvoice::getDueDate).setHeader("Due Date").setAutoWidth(true);
		dueSoonGrid.addColumn(inv -> {
			final long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), inv.getDueDate());
			return daysUntilDue + " days";
		}).setHeader("Days Until Due").setAutoWidth(true);
		dueSoonGrid.setHeight("300px");
		dueSoonGrid.addClassNames(Margin.Bottom.LARGE);
		add(sectionTitle, dueSoonGrid);
	}

	private void createInvoiceStatusSection() {
		final H2 sectionTitle = new H2("Payment Status Breakdown");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.LARGE);
		final HorizontalLayout statusCardsLayout = new HorizontalLayout();
		statusCardsLayout.addClassNames(Gap.MEDIUM, Margin.Bottom.LARGE);
		statusCardsLayout.setWidthFull();
		final CDashboardStatCard paidCard = new CDashboardStatCard("Paid Invoices", "0", VaadinIcon.CHECK.create());
		final CDashboardStatCard pendingCard = new CDashboardStatCard("Pending", "0", VaadinIcon.HOURGLASS.create());
		final CDashboardStatCard partialCard = new CDashboardStatCard("Partial Payments", "0", VaadinIcon.ADJUST.create());
		final CDashboardStatCard lateCard = new CDashboardStatCard("Late", "0", VaadinIcon.EXCLAMATION_CIRCLE.create());
		statusCardsLayout.add(paidCard, pendingCard, partialCard, lateCard);
		statusCardsLayout.setId("invoice-status-cards");
		add(sectionTitle, statusCardsLayout);
	}

	private void createProfitabilitySection() {
		final H2 sectionTitle = new H2("Profitability Analysis");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.LARGE);
		final HorizontalLayout profitCardsLayout = new HorizontalLayout();
		profitCardsLayout.addClassNames(Gap.MEDIUM, Margin.Bottom.LARGE);
		profitCardsLayout.setWidthFull();
		totalIncomeCard = new CDashboardStatCard("Total Income", "$0.00", VaadinIcon.MONEY_DEPOSIT.create());
		totalExpensesCard = new CDashboardStatCard("Total Expenses", "$0.00", VaadinIcon.MONEY_WITHDRAW.create());
		netProfitCard = new CDashboardStatCard("Net Profit", "$0.00", VaadinIcon.TRENDING_UP.create());
		profitMarginCard = new CDashboardStatCard("Profit Margin", "0%", VaadinIcon.CHART.create());
		profitCardsLayout.add(totalIncomeCard, totalExpensesCard, netProfitCard, profitMarginCard);
		add(sectionTitle, profitCardsLayout);
	}

	private void createSummaryCards() {
		final H2 sectionTitle = new H2("Invoice Summary");
		sectionTitle.addClassNames(Margin.Bottom.MEDIUM, Margin.Top.MEDIUM);
		final HorizontalLayout cardsRow1 = new HorizontalLayout();
		cardsRow1.addClassNames(Gap.MEDIUM, Margin.Bottom.MEDIUM);
		cardsRow1.setWidthFull();
		totalInvoicedCard = new CDashboardStatCard("Total Invoiced", "$0.00", VaadinIcon.INVOICE.create());
		totalPaidCard = new CDashboardStatCard("Total Paid", "$0.00", VaadinIcon.CHECK_CIRCLE.create());
		totalOutstandingCard = new CDashboardStatCard("Outstanding", "$0.00", VaadinIcon.CLOCK.create());
		overdueInvoicesCard = new CDashboardStatCard("Overdue Invoices", "0", VaadinIcon.WARNING.create());
		cardsRow1.add(totalInvoicedCard, totalPaidCard, totalOutstandingCard, overdueInvoicesCard);
		add(sectionTitle, cardsRow1);
	}

	private String formatCurrency(final BigDecimal amount) {
		if (amount == null) {
			return "$0.00";
		}
		return "$%,.2f".formatted(amount);
	}

	@Override
	public String getPageTitle() { return "Financial Dashboard"; }

	private void initializeView() {
		removeAll();
		addClassNames(Padding.MEDIUM, Gap.LARGE);
		setWidthFull();
		// Header
		final H1 title = new H1("Financial Dashboard");
		title.addClassNames(Margin.Bottom.MEDIUM);
		final Span projectLabel = new Span("Project: " + currentProject.getName());
		projectLabel.getStyle().set("font-size", "var(--lumo-font-size-l)");
		projectLabel.addClassNames(Margin.Bottom.LARGE);
		add(title, projectLabel);
		// Date range filter
		createDateRangeFilter();
		// Summary cards
		createSummaryCards();
		// Detailed sections
		createInvoiceStatusSection();
		createProfitabilitySection();
		createDueSoonSection();
		createDetailedReportSection();
	}

	private void refreshDashboard() {
		try {
			final LocalDate startDate = startDatePicker.getValue();
			final LocalDate endDate = endDatePicker.getValue();
			if (startDate == null || endDate == null) {
				CNotificationService.showWarning("Please select both start and end dates");
				return;
			}
			if (startDate.isAfter(endDate)) {
				CNotificationService.showWarning("Start date must be before end date");
				return;
			}
			// Calculate metrics
			final BigDecimal totalInvoiced = financialSummaryService.calculateTotalInvoiced(currentProject, startDate, endDate);
			final BigDecimal totalPaid = financialSummaryService.calculateTotalPaid(currentProject, startDate, endDate);
			final BigDecimal totalOutstanding = financialSummaryService.calculateTotalOutstanding(currentProject, startDate, endDate);
			final BigDecimal totalIncome = financialSummaryService.calculateTotalIncome(currentProject, startDate, endDate);
			final BigDecimal totalExpenses = financialSummaryService.calculateTotalExpenses(currentProject, startDate, endDate);
			final BigDecimal netProfit = financialSummaryService.calculateNetProfit(currentProject, startDate, endDate);
			final BigDecimal profitMargin = financialSummaryService.calculateProfitMargin(currentProject, startDate, endDate);
			final long overdueCount = financialSummaryService.countOverdueInvoices(currentProject);
			final long paidCount = financialSummaryService.countInvoicesByStatus(currentProject, CPaymentStatus.PAID);
			final long pendingCount = financialSummaryService.countInvoicesByStatus(currentProject, CPaymentStatus.PENDING);
			final long partialCount = financialSummaryService.countInvoicesByStatus(currentProject, CPaymentStatus.PARTIAL);
			final long lateCount = overdueCount;
			// Update summary cards
			totalInvoicedCard.updateValue(formatCurrency(totalInvoiced));
			totalPaidCard.updateValue(formatCurrency(totalPaid));
			totalOutstandingCard.updateValue(formatCurrency(totalOutstanding));
			overdueInvoicesCard.updateValue(String.valueOf(overdueCount));
			totalIncomeCard.updateValue(formatCurrency(totalIncome));
			totalExpensesCard.updateValue(formatCurrency(totalExpenses));
			netProfitCard.updateValue(formatCurrency(netProfit));
			profitMarginCard.updateValue("%.2f%%".formatted(profitMargin));
			// Update status cards
			final HorizontalLayout statusCardsLayout = (HorizontalLayout) getChildren()
					.filter(component -> "invoice-status-cards".equals(component.getId().orElse(""))).findFirst().orElse(null);
			if (statusCardsLayout != null) {
				statusCardsLayout.removeAll();
				final CDashboardStatCard paidCard = new CDashboardStatCard("Paid Invoices", paidCount, VaadinIcon.CHECK.create());
				final CDashboardStatCard pendingCard = new CDashboardStatCard("Pending", pendingCount, VaadinIcon.HOURGLASS.create());
				final CDashboardStatCard partialCard = new CDashboardStatCard("Partial Payments", partialCount, VaadinIcon.ADJUST.create());
				final CDashboardStatCard lateCard = new CDashboardStatCard("Late", lateCount, VaadinIcon.EXCLAMATION_CIRCLE.create());
				statusCardsLayout.add(paidCard, pendingCard, partialCard, lateCard);
			}
			// Update due soon grid
			final List<CInvoice> dueSoon = financialSummaryService.getInvoicesDueSoon(currentProject, 30);
			dueSoonGrid.setItems(dueSoon);
			// Generate detailed report
			final String report = financialSummaryService.generateFinancialSummaryReport(currentProject, startDate, endDate);
			reportTextArea.setText(report);
			CNotificationService.showSuccess("Dashboard refreshed successfully");
		} catch (final Exception e) {
			LOGGER.error("Error refreshing financial dashboard: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to refresh dashboard", e);
		}
	}

	@Override
	protected void setupToolbar() {
		// No toolbar needed for this dashboard view
	}
}
