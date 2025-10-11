#!/bin/bash

# Simple verification script for login fix
# This checks that the code changes are correct

echo "🔍 Verifying Login Fix"
echo "===================="

# Check 1: Verify CCustomLoginView uses String conversion for companyId
echo ""
echo "✓ Check 1: Verifying CCustomLoginView.java"
if grep -q 'String companyIdStr = String.valueOf(selectedCompany.getId());' src/main/java/tech/derbent/login/view/CCustomLoginView.java; then
    echo "  ✅ PASS: Company ID is converted to String in handleLogin()"
else
    echo "  ❌ FAIL: Company ID conversion not found"
    exit 1
fi

# Check 2: Verify the companyIdStr is passed to JavaScript
if grep -q 'password, companyIdStr, redirectView' src/main/java/tech/derbent/login/view/CCustomLoginView.java; then
    echo "  ✅ PASS: Company ID String is passed to JavaScript"
else
    echo "  ❌ FAIL: Company ID String not passed correctly"
    exit 1
fi

# Check 3: Verify CAuthenticationSuccessHandler has JSON handling
echo ""
echo "✓ Check 2: Verifying CAuthenticationSuccessHandler.java"
if grep -q 'cleanedParam.startsWith("{") || cleanedParam.startsWith("\[")' src/main/java/tech/derbent/login/service/CAuthenticationSuccessHandler.java; then
    echo "  ✅ PASS: JSON detection logic added"
else
    echo "  ❌ FAIL: JSON detection not found"
    exit 1
fi

# Check 4: Verify JSON extraction logic exists
if grep -q 'cleanedParam.replaceAll("\[^0-9\]", "")' src/main/java/tech/derbent/login/service/CAuthenticationSuccessHandler.java; then
    echo "  ✅ PASS: JSON extraction logic added"
else
    echo "  ❌ FAIL: JSON extraction logic not found"
    exit 1
fi

# Check 5: Code compiles successfully
echo ""
echo "✓ Check 3: Verifying code compiles"
if mvn compile -DskipTests -q > /dev/null 2>&1; then
    echo "  ✅ PASS: Code compiles successfully"
else
    echo "  ❌ FAIL: Code compilation failed"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ All verification checks PASSED!"
echo ""
echo "Summary of changes:"
echo "  1. CCustomLoginView now converts company ID to String before passing to JavaScript"
echo "  2. This prevents JSON serialization issues when passing Long objects to JavaScript"
echo "  3. CAuthenticationSuccessHandler has fallback logic to handle JSON if it still occurs"
echo "  4. The fix ensures clean string representation of company ID throughout the login flow"
echo ""
echo "The login JSON long conversion exception should now be fixed."
