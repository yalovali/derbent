# Login JSON Long Conversion Exception Fix

## Issue Description
Users encountered a JSON long conversion exception during login after sample data initialization. The error occurred when the login form submitted the company ID to the Spring Security authentication handler.

## Root Cause
The issue was in `CCustomLoginView.handleLogin()` method:

```java
// BEFORE (problematic code):
Long companyId = selectedCompany.getId();
getElement().executeJs("...", username, password, companyId, redirectView);
```

When a Java `Long` object is passed to JavaScript via Vaadin's `executeJs()`, it can be serialized as a JSON object rather than a primitive number, depending on the serialization context. This caused the authentication filter to receive a JSON string like `{"id":1}` instead of just `"1"`.

## Solution

### 1. Primary Fix: Convert to String in CCustomLoginView
Changed the company ID handling to explicitly convert to String before passing to JavaScript:

```java
// AFTER (fixed code):
String companyIdStr = String.valueOf(selectedCompany.getId());
getElement().executeJs("...", username, password, companyIdStr, redirectView);
```

**Benefits:**
- Ensures clean string representation
- Prevents JSON serialization issues
- Makes the data type contract explicit

### 2. Secondary Fix: Defensive Parsing in CAuthenticationSuccessHandler
Added defensive logic to handle JSON-like strings if they still occur:

```java
String cleanedParam = companyIdParam.trim();
if (cleanedParam.startsWith("{") || cleanedParam.startsWith("[")) {
    LOGGER.warn("Company ID parameter appears to be JSON '{}', attempting to extract numeric value", companyIdParam);
    // Extract numeric value from JSON-like string
    cleanedParam = cleanedParam.replaceAll("[^0-9]", "");
    if (cleanedParam.isEmpty()) {
        LOGGER.warn("Unable to extract numeric value from company ID parameter '{}'", companyIdParam);
        return null;
    }
}
```

**Benefits:**
- Provides fallback handling for edge cases
- Logs warnings for debugging
- Improves robustness

## Testing

### Manual Verification
All verification checks passed:
- ✅ Company ID is converted to String in handleLogin()
- ✅ Company ID String is passed to JavaScript correctly
- ✅ JSON detection logic added to authentication handler
- ✅ JSON extraction logic implemented
- ✅ Code compiles successfully

### Expected Behavior
1. User navigates to login page
2. Selects company from dropdown
3. Enters username and password
4. Clicks login button
5. Company ID is submitted as plain string "1" instead of JSON
6. Authentication succeeds without conversion errors
7. User is redirected to the home page

## Files Modified

### 1. `src/main/java/tech/derbent/login/view/CCustomLoginView.java`
- Line 102: Changed from `Long companyId = selectedCompany.getId();` to `String companyIdStr = String.valueOf(selectedCompany.getId());`
- Line 112: Changed parameter from `companyId` to `companyIdStr`

### 2. `src/main/java/tech/derbent/login/service/CAuthenticationSuccessHandler.java`
- Lines 182-195: Added JSON detection and extraction logic in `extractCompanyId()` method

## Impact
- **Risk Level:** Low - Changes are minimal and defensive
- **Backward Compatibility:** Fully maintained - existing login flows continue to work
- **Performance:** No performance impact - simple string conversion
- **Testing:** Verified by compilation and code inspection

## Related Documentation
- See `AUTHENTICATION_CALL_HIERARCHY.md` for detailed authentication flow
- See `AUTHENTICATION_IMPLEMENTATION_SUMMARY.md` for authentication architecture

## Future Improvements
Consider:
1. Adding unit tests specifically for the `extractCompanyId()` method
2. Creating integration tests that simulate different JSON scenarios
3. Reviewing other Vaadin-to-Spring parameter passing for similar issues
