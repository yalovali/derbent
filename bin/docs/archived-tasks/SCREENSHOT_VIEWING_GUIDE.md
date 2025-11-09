# Screenshot Viewing Guide

This guide helps you view and understand the 23 screenshots generated from Playwright test execution.

## Quick Start

All screenshots are located in the `target/screenshots/` directory in PNG format (1280x720 resolution).

### View All Screenshots
```bash
# List all screenshots
ls -lh target/screenshots/

# View with an image viewer (Linux)
eog target/screenshots/*.png

# View with Preview (macOS)
open target/screenshots/*.png

# View in browser
file:///path/to/derbent/target/screenshots/
```

---

## Screenshots by Category

### 🔐 Login & Authentication (7 screenshots)

#### Initial Login Flow
1. **01-login-page.png** (4.2 KB)
   - Clean login page, minimal size
   - Shows login form before sample data

2. **01-login-page-loaded.png** (799 KB)
   - Login page with sample data initialized
   - Includes "DB Minimal" button and sample data UI

3. **post-login.png** (807 KB)
   - Successful login state from simple login test
   - Shows main application interface after authentication

#### Sample Data Journey
4. **02-sample-data-initialized.png** (799 KB)
   - State after sample data has been loaded
   - Database populated with test data

5. **03-post-login-page.png** (806 KB)
   - Post-login application state from screenshot test
   - Shows application layout and navigation

6. **sample-journey-post-login.png** (807 KB)
   - Sample data menu navigation test post-login
   - Menu items visible

7. **04-final-state.png** (806 KB)
   - Final state of login screenshot test
   - Complete application view

#### Error States
8. **sample-journey-db-verification-failed.png** (807 KB)
   - Database verification error during sample data test
   - Shows error notification or state

---

### 📝 CRUD Operations (12 screenshots)

All CRUD operation screenshots show initial state and error conditions for different entity types.

#### Activity Type
9. **activity-type-initial.png** (807 KB)
   - Activity Type management page initial view
   - Shows grid with existing activity types

10. **activity-type-crud-error.png** (807 KB)
    - Error state during Activity Type CRUD operation
    - Error notification visible

#### Activity Status
11. **activity-status-initial.png** (807 KB)
    - Activity Status management page initial view
    - Shows grid with existing statuses

12. **activity-status-crud-error.png** (807 KB)
    - Error during Activity Status CRUD operation
    - Error notification or dialog

#### Meeting Type
13. **meeting-type-initial.png** (807 KB)
    - Meeting Type management page
    - Grid showing meeting types

14. **meeting-type-crud-error.png** (807 KB)
    - Meeting Type CRUD operation error
    - Error state captured

#### Decision Type
15. **decision-type-initial.png** (807 KB)
    - Decision Type management view
    - Initial grid state

16. **decision-type-crud-error.png** (807 KB)
    - Decision Type CRUD error state
    - Shows error condition

#### Order Type
17. **order-type-initial.png** (807 KB)
    - Order Type management interface
    - Grid with order types

18. **order-type-crud-error.png** (807 KB)
    - Order Type CRUD operation error
    - Error notification displayed

#### Approval Status
19. **approval-status-initial.png** (807 KB)
    - Approval Status management page
    - Shows status grid

20. **approval-status-crud-error.png** (807 KB)
    - Approval Status CRUD error
    - Error state visible

---

### 🔄 Workflow & Special Tests (3 screenshots)

#### Workflow Tests
21. **phase1-logged-in.png** (807 KB)
    - Workflow status CRUD test initial state
    - Shows logged-in application state

22. **workflow-navigation-failed.png** (799 KB)
    - Workflow navigation error state
    - Captures navigation failure point

#### Button Functionality
23. **button-test-logged-in.png** (807 KB)
    - Button functionality test state
    - Application view during button testing
    - Shows toolbar buttons (New, Save, Delete)

---

## Screenshot Analysis

### File Size Patterns
- **Small (4-5 KB):** Simple, early-stage screenshots (e.g., initial login page)
- **Medium (799 KB):** Screenshots with moderate UI complexity
- **Large (806-807 KB):** Full application screenshots with complete UI

### Resolution
All screenshots: **1280 x 720 pixels**
- Standard HD resolution
- Good balance between detail and file size
- Suitable for documentation and debugging

### Format
- **Type:** PNG (Portable Network Graphics)
- **Color:** 8-bit/color RGB
- **Compression:** Non-interlaced

---

## Using Screenshots for Debugging

### Identifying Issues

1. **Compare Initial vs Error States**
   ```bash
   # Example: Activity Type
   diff -u <(file target/screenshots/activity-type-initial.png) \
           <(file target/screenshots/activity-type-crud-error.png)
   ```

2. **Check Error Notifications**
   - Error screenshots typically show Vaadin notification cards
   - Look for red error messages or warning dialogs
   - Check console output in test logs for details

3. **Verify Navigation**
   - Check if expected UI elements are present
   - Verify menu items and buttons are visible
   - Confirm page titles and breadcrumbs

### Common Patterns in Screenshots

#### Successful States
- Clean UI with no error notifications
- Populated grids with data
- Active navigation menus
- Visible toolbar buttons

#### Error States
- Red notification cards (Vaadin error style)
- Empty or partially loaded grids
- Missing UI elements
- JavaScript console errors (check test logs)

---

## Integrating Screenshots into Documentation

### Markdown Integration
```markdown
![Login Page](target/screenshots/01-login-page.png)
*Initial login page view*
```

### HTML Integration
```html
<img src="target/screenshots/01-login-page.png" 
     alt="Login Page" 
     width="640" 
     height="360">
```

### Creating Animated GIFs
```bash
# Combine login flow screenshots into animated GIF
convert -delay 100 \
  target/screenshots/01-login-page.png \
  target/screenshots/02-sample-data-initialized.png \
  target/screenshots/03-post-login-page.png \
  login-flow.gif
```

---

## Screenshot Comparison Tools

### ImageMagick Commands
```bash
# Compare two screenshots
compare activity-type-initial.png \
        activity-type-crud-error.png \
        diff-output.png

# Create side-by-side comparison
convert activity-type-initial.png \
        activity-type-crud-error.png \
        +append comparison.png
```

### GUI Tools
- **Linux:** Eye of GNOME (eog), GIMP
- **macOS:** Preview, Pixelmator
- **Windows:** Paint, IrfanView
- **Cross-platform:** GIMP, Photoshop

---

## Archiving Screenshots

### Create Archive
```bash
# Create timestamped archive
tar -czf playwright-screenshots-$(date +%Y%m%d-%H%M%S).tar.gz \
  target/screenshots/

# Create ZIP archive
zip -r playwright-screenshots-$(date +%Y%m%d-%H%M%S).zip \
  target/screenshots/
```

### Cloud Storage
```bash
# Example: Upload to AWS S3
aws s3 sync target/screenshots/ \
  s3://my-bucket/test-screenshots/$(date +%Y%m%d)/

# Example: Upload to Google Drive (using gdrive CLI)
gdrive upload -r target/screenshots/
```

---

## Best Practices

### When Taking Screenshots
1. ✅ Capture at consistent resolution (1280x720)
2. ✅ Use descriptive filenames
3. ✅ Take screenshots at key test milestones
4. ✅ Capture error states for debugging
5. ✅ Document what each screenshot shows

### When Reviewing Screenshots
1. 🔍 Compare initial vs error states
2. 🔍 Look for missing UI elements
3. 🔍 Check error notification text
4. 🔍 Verify data is loaded in grids
5. 🔍 Confirm navigation elements are present

### When Sharing Screenshots
1. 📤 Include context in filename
2. 📤 Add annotations if needed
3. 📤 Reference screenshot in bug reports
4. 📤 Include timestamp information
5. 📤 Provide test method name for traceability

---

## Troubleshooting

### Screenshots Not Generated
- Check Playwright browser installation
- Verify `target/screenshots/` directory exists
- Check test logs for screenshot errors
- Ensure headless mode is working

### Screenshots Too Large
- Reduce resolution in test configuration
- Use JPEG format instead of PNG
- Apply PNG optimization tools

### Screenshots Missing Content
- Increase wait times before screenshot
- Check if page is fully loaded
- Verify browser window size
- Check for JavaScript errors

---

## Additional Resources

- **Playwright Documentation:** https://playwright.dev/
- **Test Report:** `PLAYWRIGHT_TEST_EXECUTION_REPORT.md`
- **Test Runner Script:** `run-all-playwright-tests.sh`
- **Test Logs:** `target/test-reports/`

---

**Total Screenshots:** 23  
**Total Size:** ~18 MB  
**Resolution:** 1280 x 720  
**Format:** PNG  
**Generated:** 2025-10-27
