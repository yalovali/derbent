# Validation Refactoring - Remaining Work

## Quick Fix Guide

These 7 services need their private `validateNumericField()` method removed. The static helper from `CAbstractService` will automatically be used instead.

### Files to Fix

1. `src/main/java/tech/derbent/plm/activities/service/CActivityService.java` - Line 135
2. `src/main/java/tech/derbent/plm/agile/service/CAgileEntityService.java` - Line 97
3. `src/main/java/tech/derbent/plm/budgets/budget/service/CBudgetService.java` - Line 85
4. `src/main/java/tech/derbent/plm/customers/customer/service/CCustomerService.java` - Line 109
5. `src/main/java/tech/derbent/plm/invoices/invoiceitem/service/CInvoiceItemService.java` - Line 58
6. `src/main/java/tech/derbent/plm/orders/order/service/COrderService.java` - Line 143
7. `src/main/java/tech/derbent/plm/tickets/ticket/service/CTicketService.java` - Line 101

### What to Delete

In each file, delete this method (signature may vary slightly):

```java
private void validateNumericField(BigDecimal value, String fieldName, BigDecimal max) {
    if (value != null) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        if (value.compareTo(max) > 0) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + max);
        }
    }
}
```

### Why This Works

- The private method has identical signature to the static helper in `CAbstractService`
- When the private method is removed, Java will automatically use the inherited static method
- No other changes needed!

### Verification

```bash
# After deleting the 7 private methods:
mvn compile -Pagents -DskipTests

# Should compile successfully
# Then format:
mvn spotless:apply

# Then commit:
git add -A
git commit -m "Complete validation refactoring: remove duplicate validateNumericField methods"
```

### Optional: Additional Refactoring

While fixing these files, you can also replace manual string length checks with `validateStringLength()`:

**Before:**
```java
if (entity.getSomeField() != null && entity.getSomeField().length() > 100) {
    throw new IllegalArgumentException("Some Field cannot exceed 100 characters");
}
```

**After:**
```java
validateStringLength(entity.getSomeField(), "Some Field", 100);
```

This further reduces code duplication and improves consistency!
