package tech.derbent.plm.storage.transaction.domain;

/**
 * Transaction types for storage inventory movements.
 */
public enum CTransactionType {
    STOCK_IN("Stock In", "Items received into storage"),
    STOCK_OUT("Stock Out", "Items issued from storage"),
    ADJUSTMENT("Adjustment", "Inventory correction or count adjustment"),
    TRANSFER("Transfer", "Stock moved between locations"),
    EXPIRED("Expired", "Items removed due to expiration"),
    DAMAGED("Damaged", "Items removed due to damage"),
    LOST("Lost", "Items removed due to loss/theft");

    private final String displayName;
    private final String description;

    CTransactionType(final String displayName, final String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
