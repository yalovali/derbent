# New Entities to Test (Added This Week)

## Financial Entities (Priority 1)
1. **Budgets** (`/cdynamicpagerouter/budgets`)
2. **Budget Types** (`/cdynamicpagerouter/budget-types`)
3. **Invoices** (`/cdynamicpagerouter/invoices`)
4. **Invoice Items** (`/cdynamicpagerouter/invoice-items`)
5. **Payments** (`/cdynamicpagerouter/payments`)
6. **Orders** (`/cdynamicpagerouter/orders`)
7. **Currencies** (`/cdynamicpagerouter/currencies`)

## Test Management Entities (Priority 2)
8. **Test Cases** (`/cdynamicpagerouter/test-cases`)
9. **Test Scenarios** (`/cdynamicpagerouter/test-scenarios`)
10. **Test Runs** (`/cdynamicpagerouter/test-runs`)
11. **Test Steps** (`/cdynamicpagerouter/test-steps`)
12. **Test Case Results** (`/cdynamicpagerouter/test-case-results`)

## Team/Issue Entities (Priority 3)
13. **Issues** (`/cdynamicpagerouter/issues`)
14. **Issue Types** (`/cdynamicpagerouter/issue-types`)
15. **Teams** (`/cdynamicpagerouter/teams`)

## Test Execution Strategy

### Per Entity:
1. Navigate to entity page
2. Verify grid loads
3. Click "New" button
4. Fill all required fields
5. Click "Save" and verify success
6. Verify entity appears in grid
7. Select entity and click "Edit"
8. Modify fields
9. Click "Save" and verify update
10. **Test Attachments section** (if present)
11. **Test Comments section** (if present)
12. Test any custom actions/buttons
13. Test delete (if not protected)

### Special Focus:
- **Lazy loading issues** - Should show error dialog now
- **Attachment sections** - Must test file upload
- **Comment sections** - Must test add/edit/delete
- **Status workflows** - Test status transitions
- **Validation** - Test required fields
