package tech.derbent.plm.storage.transaction.domain;

public enum CTransactionType {
    STOCK_IN,
    STOCK_OUT,
    ADJUSTMENT,
    TRANSFER,
    EXPIRED,
    DAMAGED,
    LOST
}
