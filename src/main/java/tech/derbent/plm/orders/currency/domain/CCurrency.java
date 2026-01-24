package tech.derbent.plm.orders.currency.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.projects.domain.CProject;

/** CCurrency - Domain entity representing currencies used in orders. Layer: Domain (MVC) Defines the various currencies that can be used in order
 * transactions, including currency code, symbol, and exchange rate information. This entity extends CEntityNamed and adds currency-specific fields
 * such as currency code (USD, EUR, etc.) and symbol ($, €, etc.). */
@Entity
@Table (name = "ccurrency")
@AttributeOverride (name = "id", column = @Column (name = "currency_id"))
public final class CCurrency extends CEntityOfProject<CCurrency> {

	public static final String DEFAULT_COLOR = "#CD853F"; // X11 Peru - monetary units (darker)
	public static final String DEFAULT_ICON = "vaadin:dollar";
	public static final String ENTITY_TITLE_PLURAL = "Currencies";
	public static final String ENTITY_TITLE_SINGULAR = "Currency";
	public static final String VIEW_NAME = "Currency View";
	@Column (name = "currency_code", nullable = false, length = 3, unique = false)
	@Size (max = 3, min = 3, message = "Currency code must be exactly 3 characters")
	@AMetaData (
			displayName = "Currency Code", required = true, readOnly = false, description = "ISO 4217 currency code (e.g., USD, EUR, GBP)",
			hidden = false, maxLength = 3
	)
	private String currencyCode;
	@Column (name = "currency_symbol", nullable = true, length = 5)
	@Size (max = 5)
	@AMetaData (
			displayName = "Symbol", required = false, readOnly = false, description = "Currency symbol (e.g., $, €, £)", hidden = false, maxLength = 5
	)
	private String currencySymbol;

	public CCurrency() {
		super(CCurrency.class, "New Currency", null);
		initializeDefaults();
	}

	public CCurrency(final String name, final CProject<?> project) {
		super(CCurrency.class, name, project);
		initializeDefaults();
	}

	public String getCurrencyCode() { return currencyCode; }

	public String getCurrencySymbol() { return currencySymbol; }

	private final void initializeDefaults() {
		currencyCode = "USD";
		currencySymbol = "$";
		CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
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
