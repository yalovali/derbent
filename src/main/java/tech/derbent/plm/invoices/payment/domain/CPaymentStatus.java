package tech.derbent.plm.invoices.payment.domain;

/** Payment status enumeration for tracking payment states. */
public enum CPaymentStatus {
	/** Payment is pending and awaiting processing. */
	PENDING,
	
	/** Payment is due and should be processed. */
	DUE,
	
	/** Payment is overdue and late. */
	LATE,
	
	/** Payment has been partially paid. */
	PARTIAL,
	
	/** Payment has been fully paid. */
	PAID,
	
	/** Payment has been cancelled. */
	CANCELLED,
	
	/** Payment has been refunded. */
	REFUNDED,
	
	/** Payment is in dispute. */
	DISPUTED
}
