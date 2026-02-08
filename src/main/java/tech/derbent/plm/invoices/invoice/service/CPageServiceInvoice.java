package tech.derbent.plm.invoices.invoice.service;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.theme.lumo.LumoUtility;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.grid.view.CGridViewBaseDBEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceHasStatusAndWorkflow;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.invoices.invoice.domain.CInvoice;
import tech.derbent.plm.invoices.payment.domain.CPaymentStatus;

public class CPageServiceInvoice extends CPageServiceDynamicPage<CInvoice> implements IPageServiceHasStatusAndWorkflow<CInvoice> {

	/** Helper method to create a metric row with icon.
	 * @param label    The label for the metric
	 * @param value    The value to display
	 * @param iconName The Vaadin icon to use
	 * @return Component showing the metric row */
	private static Component createMetricRow(final String label, final String value, final VaadinIcon iconName) {
		return createMetricRow(label, value, iconName, false);
	}

	/** Helper method to create a metric row with icon and optional highlight.
	 * @param label     The label for the metric
	 * @param value     The value to display
	 * @param iconName  The Vaadin icon to use
	 * @param highlight Whether to highlight this row
	 * @return Component showing the metric row */
	private static Component createMetricRow(final String label, final String value, final VaadinIcon iconName, final boolean highlight) {
		final HorizontalLayout row = new HorizontalLayout();
		row.addClassNames(LumoUtility.Gap.SMALL, LumoUtility.AlignItems.CENTER);
		row.setWidthFull();
		row.setJustifyContentMode(HorizontalLayout.JustifyContentMode.BETWEEN);
		final HorizontalLayout labelSection = new HorizontalLayout();
		labelSection.addClassNames(LumoUtility.Gap.XSMALL, LumoUtility.AlignItems.CENTER);
		final Icon icon = iconName.create();
		icon.setSize("16px");
		icon.addClassNames(LumoUtility.TextColor.SECONDARY);
		final Span labelSpan = new Span(label);
		if (highlight) {
			labelSpan.addClassNames(LumoUtility.FontWeight.BOLD);
		}
		labelSection.add(icon, labelSpan);
		final Span valueSpan = new Span(value);
		if (highlight) {
			valueSpan.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.FontSize.LARGE);
		}
		row.add(labelSection, valueSpan);
		return row;
	}

	/** Creates a milestone badge component if invoice is linked to a milestone.
	 * @param invoice The invoice to check for milestone linkage
	 * @return Component showing milestone badge or empty div */
	public static Component createMilestoneBadge(final CInvoice invoice) {
		if (invoice == null || !Boolean.TRUE.equals(invoice.getIsMilestonePayment()) || invoice.getRelatedMilestone() == null) {
			return new Div();
		}
		final HorizontalLayout badge = new HorizontalLayout();
		badge.addClassNames(LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Padding.Vertical.XSMALL, LumoUtility.Gap.XSMALL,
				LumoUtility.AlignItems.CENTER);
		badge.getStyle().set("background-color", "var(--lumo-primary-color-10pct)").set("color", "var(--lumo-primary-text-color)")
				.set("border-radius", "var(--lumo-border-radius-m)").set("display", "inline-flex");
		final Icon icon = VaadinIcon.FLAG.create();
		icon.setSize("16px");
		final Span text = new Span("Milestone: " + invoice.getRelatedMilestone().getName());
		text.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);
		badge.add(icon, text);
		return badge;
	}

	/** Creates a payment plan info component if invoice is part of a payment plan.
	 * @param invoice The invoice to check for payment plan
	 * @return Component showing payment plan info or empty div */
	public static Component createPaymentPlanInfo(final CInvoice invoice) {
		if (invoice == null || invoice.getPaymentPlanInstallments() == null || invoice.getInstallmentNumber() == null) {
			return new Div();
		}
		final HorizontalLayout info = new HorizontalLayout();
		info.addClassNames(LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Padding.Vertical.XSMALL, LumoUtility.Gap.XSMALL,
				LumoUtility.AlignItems.CENTER);
		info.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "var(--lumo-border-radius-m)").set("display",
				"inline-flex");
		final Icon icon = VaadinIcon.CALENDAR_CLOCK.create();
		icon.setSize("16px");
		final Span text = new Span(String.format("Installment %d of %d", invoice.getInstallmentNumber(), invoice.getPaymentPlanInstallments()));
		text.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.FontWeight.MEDIUM);
		info.add(icon, text);
		return info;
	}

	/** Creates a payment status badge component with color coding. This is a reusable component for displaying payment status.
	 * @param status The payment status to display
	 * @return Component showing colored payment status badge */
	public static Component createPaymentStatusBadge(final CPaymentStatus status) {
		if (status == null) {
			return new Span("Unknown");
		}
		final Span badge = new Span(status.name());
		badge.addClassNames(LumoUtility.Padding.Horizontal.SMALL, LumoUtility.Padding.Vertical.XSMALL, LumoUtility.FontSize.SMALL,
				LumoUtility.FontWeight.MEDIUM);
		badge.getStyle().set("border-radius", "var(--lumo-border-radius-m)").set("display", "inline-block");
		// Color code by status
		switch (status) {
		case PAID:
			badge.getStyle().set("background-color", "var(--lumo-success-color-10pct)").set("color", "var(--lumo-success-text-color)");
			break;
		case PARTIAL:
			badge.getStyle().set("background-color", "var(--lumo-primary-color-10pct)").set("color", "var(--lumo-primary-text-color)");
			break;
		case LATE:
			badge.getStyle().set("background-color", "var(--lumo-error-color-10pct)").set("color", "var(--lumo-error-text-color)");
			break;
		case PENDING:
		case DUE:
			badge.getStyle().set("background-color", "var(--lumo-warning-color-10pct)").set("color", "var(--lumo-warning-text-color)");
			break;
		default:
			badge.getStyle().set("background-color", "var(--lumo-contrast-10pct)").set("color", "var(--lumo-secondary-text-color)");
		}
		return badge;
	}

	/** Helper method to format currency values.
	 * @param amount The amount to format
	 * @return Formatted currency string */
	private static String formatCurrency(final BigDecimal amount) {
		if (amount == null) {
			return "$0.00";
		}
		return String.format("$%,.2f", amount);
	}

	Logger LOGGER = LoggerFactory.getLogger(CPageServiceInvoice.class);
	private CProjectItemStatusService statusService;
	Long serialVersionUID = 1L;

	public CPageServiceInvoice(IPageServiceImplementer<CInvoice> view) {
		super(view);
		try {
			statusService = CSpringContext.getBean(CProjectItemStatusService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize CProjectItemStatusService - status changes will not be validated", e);
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for entity {}.", this.getClass().getSimpleName(), CInvoice.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind page service.");
			super.bind();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for entity {}: {}", this.getClass().getSimpleName(), CInvoice.class.getSimpleName(),
					e.getMessage());
			throw e;
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Report action triggered for CInvoice");
		if (getView() instanceof CGridViewBaseDBEntity) {
			final CGridViewBaseDBEntity<CInvoice> gridView = (CGridViewBaseDBEntity<CInvoice>) getView();
			gridView.generateGridReport();
		} else {
			super.actionReport();
		}
	}


	/** Creates a financial summary panel component showing key invoice metrics. This is a reusable component that can be added to invoice detail
	 * views. Note: This is a UI helper method, not a CFormBuilder component factory. It does NOT require registerComponent() call.
	 * @param invoice The invoice to display summary for
	 * @return Component showing financial summary */
	public Component createFinancialSummaryPanel(final CInvoice invoice) {
		if (invoice == null) {
			return new Div();
		}
		final VerticalLayout panel = new VerticalLayout();
		panel.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.Gap.SMALL);
		panel.getStyle().set("background-color", "var(--lumo-contrast-5pct)").set("border-radius", "var(--lumo-border-radius-m)");
		// Title
		final Span title = new Span("Financial Summary");
		title.addClassNames(LumoUtility.FontSize.LARGE, LumoUtility.FontWeight.BOLD);
		// Metrics
		final Component subtotalRow = createMetricRow("Subtotal", formatCurrency(invoice.getSubtotal()), VaadinIcon.MONEY);
		final Component taxRow = createMetricRow("Tax (" + invoice.getTaxRate() + "%)", formatCurrency(invoice.getTaxAmount()), VaadinIcon.CALC);
		final Component discountRow =
				createMetricRow("Discount (" + invoice.getDiscountRate() + "%)", formatCurrency(invoice.getDiscountAmount()), VaadinIcon.TAG);
		final Component totalRow = createMetricRow("Total Amount", formatCurrency(invoice.getTotalAmount()), VaadinIcon.INVOICE, true);
		final Component paidRow = createMetricRow("Paid Amount", formatCurrency(invoice.getPaidAmount()), VaadinIcon.CHECK_CIRCLE);
		final Component balanceRow = createMetricRow("Balance Due", formatCurrency(invoice.getRemainingBalance()), VaadinIcon.CLOCK, true);
		panel.add(title, subtotalRow, taxRow, discountRow, totalRow, paidRow, balanceRow);
		return panel;
	}

	@Override
	public CProjectItemStatusService getProjectItemStatusService() { return statusService; }
}
