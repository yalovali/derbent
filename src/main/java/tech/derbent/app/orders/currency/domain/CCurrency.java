package tech.derbent.app.orders.currency.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.app.projects.domain.CProject;

/** CCurrency - Domain entity representing currencies used in orders. Layer: Domain (MVC) Defines the various currencies that can be used in order
 * transactions, including currency code, symbol, and exchange rate information. This entity extends CEntityNamed and adds currency-specific fields
 * such as currency code (USD, EUR, etc.) and symbol ($, €, etc.). */
@Entity
@Table (name = "ccurrency")
@AttributeOverride (name = "id", column = @Column (name = "currency_id"))
public class CCurrency extends CEntityOfProject<CCurrency> {

	public static final String DEFAULT_COLOR = "#ffc107";
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String VIEW_NAME = "Currency View";
	@Column (name = "currency_code", nullable = false, length = 3, unique = false)
	@Size (max = 3, min = 3, message = "Currency code must be exactly 3 characters")
	@AMetaData (
			displayName = "Currency Code", required = true, readOnly = false, description = "ISO 4217 currency code (e.g., USD, EUR, GBP)",
			hidden = false, order = 2, maxLength = 3
	)
	private String currencyCode;
	@Column (name = "currency_symbol", nullable = true, length = 5)
	@Size (max = 5)
	@AMetaData (
			displayName = "Symbol", required = false, readOnly = false, description = "Currency symbol (e.g., $, €, £)", hidden = false, order = 3,
			maxLength = 5
	)
	private String currencySymbol;

	public CCurrency() {
		super(CCurrency.class, "New Currency", null);
	}

	public CCurrency(final String name, final CProject project) {
		super(CCurrency.class, name, project);
	}

	public String getCurrencyCode() { return currencyCode; }

	public String getCurrencySymbol() { return currencySymbol; }

	@Override
	public void initializeAllFields() {
		// Initialize lazy-loaded entity relationships from parent class (CEntityOfProject)
		if (getProject() != null) {
			getProject().getName(); // Trigger project loading
		}
		if (getAssignedTo() != null) {
			getAssignedTo().getLogin(); // Trigger assigned user loading
		}
		if (getCreatedBy() != null) {
			getCreatedBy().getLogin(); // Trigger creator loading
		}
	}

	public void setCurrencyCode(final String currencyCode) {
		this.currencyCode = currencyCode;
		updateLastModified();
	}

	public void setCurrencySymbol(final String currencySymbol) {
		this.currencySymbol = currencySymbol;
		updateLastModified();
	}

	@Override
	public String toString() {
		return currencyCode != null ? currencyCode + " (" + getName() + ")" : super.toString();
	}
}
