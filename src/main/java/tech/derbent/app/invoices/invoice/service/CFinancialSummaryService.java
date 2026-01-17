package tech.derbent.app.invoices.invoice.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.app.invoices.invoice.domain.CInvoice;
import tech.derbent.app.invoices.payment.domain.CPaymentStatus;
import tech.derbent.app.projectexpenses.projectexpense.domain.CProjectExpense;
import tech.derbent.app.projectexpenses.projectexpense.service.CProjectExpenseService;
import tech.derbent.app.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.app.projectincomes.projectincome.service.CProjectIncomeService;

/** CFinancialSummaryService - Service for generating financial reports and summaries. Provides period-based financial reporting including income,
 * expenses, and profit calculations. */
@Service
@Transactional (readOnly = true)
@PreAuthorize ("hasAnyRole('ADMIN', 'USER')")
public class CFinancialSummaryService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CFinancialSummaryService.class);
	private final CInvoiceService invoiceService;
	private final CProjectExpenseService expenseService;
	private final CProjectIncomeService incomeService;

	public CFinancialSummaryService(final CInvoiceService invoiceService, final CProjectExpenseService expenseService,
			final CProjectIncomeService incomeService) {
		this.invoiceService = invoiceService;
		this.expenseService = expenseService;
		this.incomeService = incomeService;
	}

	/** Calculate net profit (income - expenses) for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Net profit */
	public BigDecimal calculateNetProfit(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final BigDecimal totalIncome = calculateTotalIncome(project, startDate, endDate);
		final BigDecimal totalExpenses = calculateTotalExpenses(project, startDate, endDate);
		return totalIncome.subtract(totalExpenses);
	}

	/** Calculate profit margin percentage for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Profit margin as percentage */
	public BigDecimal calculateProfitMargin(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final BigDecimal totalIncome = calculateTotalIncome(project, startDate, endDate);
		if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
			return BigDecimal.ZERO;
		}
		final BigDecimal netProfit = calculateNetProfit(project, startDate, endDate);
		return netProfit.divide(totalIncome, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
	}

	/** Calculate total project expenses for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Total expenses */
	public BigDecimal calculateTotalExpenses(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final List<CProjectExpense> expenses = expenseService.findAll();
		return expenses.stream().filter(exp -> exp.getProject().getId().equals(project.getId()))
				.filter(exp -> isDateInRange(exp.getExpenseDate(), startDate, endDate)).map(CProjectExpense::getAmount)
				.filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Calculate total project income for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Total income */
	public BigDecimal calculateTotalIncome(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final List<CProjectIncome> incomes = incomeService.findAll();
		return incomes.stream().filter(inc -> inc.getProject().getId().equals(project.getId()))
				.filter(inc -> isDateInRange(inc.getIncomeDate(), startDate, endDate)).map(CProjectIncome::getAmount)
				.filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Calculate total invoiced amount for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Total invoiced amount */
	public BigDecimal calculateTotalInvoiced(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId()))
				.filter(inv -> isDateInRange(inv.getInvoiceDate(), startDate, endDate)).map(CInvoice::getTotalAmount)
				.filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Calculate total outstanding (unpaid) amount for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Total outstanding amount */
	public BigDecimal calculateTotalOutstanding(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final BigDecimal totalInvoiced = calculateTotalInvoiced(project, startDate, endDate);
		final BigDecimal totalPaid = calculateTotalPaid(project, startDate, endDate);
		return totalInvoiced.subtract(totalPaid);
	}

	/** Calculate total overdue amount for a project.
	 * @param project Project to check
	 * @return Total overdue amount */
	public BigDecimal calculateTotalOverdue(final CProject project) {
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId())).filter(CInvoice::isOverdue)
				.map(CInvoice::getRemainingBalance).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Calculate total paid amount for a period.
	 * @param project   Project to calculate for
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Total paid amount */
	public BigDecimal calculateTotalPaid(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId()))
				.filter(inv -> isDateInRange(inv.getInvoiceDate(), startDate, endDate)).map(CInvoice::getPaidAmount)
				.filter(java.util.Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	/** Count invoices by payment status for a project.
	 * @param project Project to check
	 * @param status  Payment status to count
	 * @return Number of invoices with given status */
	public long countInvoicesByStatus(final CProject project, final CPaymentStatus status) {
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId())).filter(inv -> inv.getPaymentStatus() == status)
				.count();
	}

	/** Count overdue invoices for a project.
	 * @param project Project to check
	 * @return Number of overdue invoices */
	public long countOverdueInvoices(final CProject project) {
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId())).filter(CInvoice::isOverdue).count();
	}

	/** Generate a financial summary report for a period.
	 * @param project   Project to report on
	 * @param startDate Start of period
	 * @param endDate   End of period
	 * @return Financial summary report as formatted string */
	public String generateFinancialSummaryReport(final CProject project, final LocalDate startDate, final LocalDate endDate) {
		final StringBuilder report = new StringBuilder();
		report.append("═══════════════════════════════════════════════════════════════\n");
		report.append("                   FINANCIAL SUMMARY REPORT                    \n");
		report.append("═══════════════════════════════════════════════════════════════\n");
		report.append(String.format("Project: %s\n", project.getName()));
		report.append(String.format("Period: %s to %s\n", startDate, endDate));
		report.append("───────────────────────────────────────────────────────────────\n\n");
		report.append("INVOICING:\n");
		report.append(String.format("  Total Invoiced:    %12.2f\n", calculateTotalInvoiced(project, startDate, endDate)));
		report.append(String.format("  Total Paid:        %12.2f\n", calculateTotalPaid(project, startDate, endDate)));
		report.append(String.format("  Total Outstanding: %12.2f\n", calculateTotalOutstanding(project, startDate, endDate)));
		report.append("\n");
		report.append("INCOME & EXPENSES:\n");
		report.append(String.format("  Total Income:      %12.2f\n", calculateTotalIncome(project, startDate, endDate)));
		report.append(String.format("  Total Expenses:    %12.2f\n", calculateTotalExpenses(project, startDate, endDate)));
		report.append(String.format("  Net Profit:        %12.2f\n", calculateNetProfit(project, startDate, endDate)));
		report.append(String.format("  Profit Margin:     %12.2f%%\n", calculateProfitMargin(project, startDate, endDate)));
		report.append("\n");
		report.append("PAYMENT STATUS SUMMARY:\n");
		report.append(String.format("  Paid Invoices:     %12d\n", countInvoicesByStatus(project, CPaymentStatus.PAID)));
		report.append(String.format("  Pending Invoices:  %12d\n", countInvoicesByStatus(project, CPaymentStatus.PENDING)));
		report.append(String.format("  Partial Payments:  %12d\n", countInvoicesByStatus(project, CPaymentStatus.PARTIAL)));
		report.append(String.format("  Overdue Invoices:  %12d\n", countOverdueInvoices(project)));
		report.append(String.format("  Total Overdue Amt: %12.2f\n", calculateTotalOverdue(project)));
		report.append("\n");
		final List<CInvoice> dueSoon = getInvoicesDueSoon(project, 30);
		if (!dueSoon.isEmpty()) {
			report.append("INVOICES DUE WITHIN 30 DAYS:\n");
			for (final CInvoice invoice : dueSoon) {
				report.append(String.format("  %s: %s (%.2f) - Due: %s\n", invoice.getInvoiceNumber(), invoice.getName(),
						invoice.getRemainingBalance(), invoice.getDueDate()));
			}
		}
		report.append("═══════════════════════════════════════════════════════════════\n");
		return report.toString();
	}

	/** Get invoices due within specified days.
	 * @param project Project to check
	 * @param days    Number of days ahead to check
	 * @return List of invoices due within period */
	public List<CInvoice> getInvoicesDueSoon(final CProject project, final int days) {
		final LocalDate futureDate = LocalDate.now().plusDays(days);
		final List<CInvoice> invoices = invoiceService.findAll();
		return invoices.stream().filter(inv -> inv.getProject().getId().equals(project.getId()))
				.filter(inv -> inv.getPaymentStatus() != CPaymentStatus.PAID).filter(inv -> inv.getPaymentStatus() != CPaymentStatus.CANCELLED)
				.filter(inv -> inv.getDueDate() != null).filter(inv -> !inv.getDueDate().isBefore(LocalDate.now()))
				.filter(inv -> !inv.getDueDate().isAfter(futureDate)).sorted((a, b) -> a.getDueDate().compareTo(b.getDueDate())).toList();
	}

	/** Helper method to check if a date falls within a range.
	 * @param date      Date to check
	 * @param startDate Start of range (inclusive)
	 * @param endDate   End of range (inclusive)
	 * @return true if date is within range */
	private boolean isDateInRange(final LocalDate date, final LocalDate startDate, final LocalDate endDate) {
		if (date == null) {
			return false;
		}
		return !date.isBefore(startDate) && !date.isAfter(endDate);
	}
}
