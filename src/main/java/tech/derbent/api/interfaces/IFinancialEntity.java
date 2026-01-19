package tech.derbent.api.interfaces;

import java.math.BigDecimal;
import tech.derbent.plm.orders.currency.domain.CCurrency;

/** Interface for entities with financial amounts.
 * Provides standard methods for handling monetary values with currency. */
public interface IFinancialEntity {
    
    /** Get the monetary amount.
     * @return the amount */
    BigDecimal getAmount();
    
    /** Set the monetary amount.
     * @param amount the amount to set */
    void setAmount(BigDecimal amount);
    
    /** Get the currency.
     * @return the currency */
    CCurrency getCurrency();
    
    /** Set the currency.
     * @param currency the currency to set */
    void setCurrency(CCurrency currency);
    
    /** Check if this entity has a positive amount.
     * @return true if amount is greater than zero */
    default boolean hasAmount() {
        return getAmount() != null && getAmount().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /** Get the amount or zero if null.
     * @return the amount or BigDecimal.ZERO */
    default BigDecimal getAmountOrZero() {
        return getAmount() != null ? getAmount() : BigDecimal.ZERO;
    }
}
