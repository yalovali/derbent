# Manual Testing Guide for SVG Image Support

## Overview
This guide provides instructions for manually testing SVG image support in the Derbent application after the fix to CImageUtils.

## Issue Fixed
Previously, when users uploaded SVG images as profile pictures, the application would fail with:
- `ImageIO.read()` returning `null`
- Error: "Unable to read image data - unsupported format"

The fix now properly detects SVG images and returns them as-is (since SVG is vector-based and doesn't need resizing).

## Test Cases

### Test Case 1: Upload SVG Profile Picture

1. **Start the application:**
   ```bash
   cd /home/runner/work/derbent/derbent
   export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
   export PATH=$JAVA_HOME/bin:$PATH
   mvn spring-boot:run -Dspring.profiles.active=h2
   ```

2. **Access the application:**
   - Open browser to http://localhost:8080
   - Login with admin credentials (from sample data)

3. **Navigate to User Profile:**
   - Go to User Management
   - Edit a user or your own profile
   - Click on the profile picture upload area

4. **Upload SVG File:**
   - Select an SVG file (e.g., icon.svg)
   - Expected Result: ✅ File uploads successfully
   - Expected Result: ✅ SVG displays correctly in the UI
   - Expected Result: ✅ No errors in console logs

5. **Verify Data:**
   - Save the user profile
   - Reload the page
   - Expected Result: ✅ SVG profile picture persists and displays correctly

### Test Case 2: Validate SVG Format Detection

**Using Automated Tests:**
```bash
cd /home/runner/work/derbent/derbent
mvn test -Dtest=CImageUtilsSvgTest
```

Expected Results:
- ✅ All 9 tests pass
- ✅ SVG detection works correctly
- ✅ SVG validation works correctly
- ✅ SVG resize returns original data

### Test Case 3: Mixed Format Upload

1. **Test with different image formats:**
   - Upload PNG → Expected: ✅ Resized to 100x100
   - Upload JPG → Expected: ✅ Resized to 100x100
   - Upload GIF → Expected: ✅ Resized to 100x100
   - Upload SVG → Expected: ✅ Returned as-is (no resizing)

2. **Verify existing functionality:**
   ```bash
   mvn test -Dtest=CUserIconTest
   ```
   Expected: ✅ All 13 tests pass (no regressions)

## Sample SVG File for Testing

Create a file named `test-profile.svg`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
  <circle cx="50" cy="50" r="50" fill="#f5f5f5"/>
  <circle cx="50" cy="35" r="15" fill="#999999"/>
  <path d="m25 75c0-14 11-25 25-25s25 11 25 25" fill="#999999"/>
</svg>
```

This creates a simple user avatar in SVG format.

## Verification Checklist

- [x] CImageUtils compiles successfully
- [x] All new SVG tests pass (9/9)
- [x] All existing tests pass (13/13 CUserIconTest)
- [x] SVG added to SUPPORTED_FORMATS array
- [x] isSvgImage() helper method implemented
- [x] resizeImage() returns SVG data unchanged
- [x] validateImageData() validates SVG correctly
- [ ] Manual browser test with SVG upload
- [ ] Manual verification of SVG display in UI
- [ ] Check console logs for proper logging messages

## Expected Log Output

When uploading an SVG file, you should see:
```
INFO tech.derbent.api.utils.CImageUtils -- CImageUtils.resizeImage called with targetWidth=100, targetHeight=100
INFO tech.derbent.api.utils.CImageUtils -- Image is SVG format - returning original data (vector format scales without resizing)
```

## Regression Testing

Ensure no regressions by running:
```bash
# Run all tests
mvn test

# Or run specific image-related tests
mvn test -Dtest=CUserIconTest,CImageUtilsSvgTest
```

All tests should pass without errors.

## Notes

- SVG files are vector-based and scale perfectly at any size
- No quality loss occurs with SVG at different resolutions
- The application now supports: JPG, JPEG, PNG, GIF, and SVG formats
- SVG files are returned as-is from resizeImage() method
- The fix is minimal and non-invasive to existing code
