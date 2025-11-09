# Selenium Testing Integration for Derbent

## Quick Start

This project now supports **Selenium WebDriver** UI testing alongside Playwright tests.

### Run Selenium Tests

```bash
# Run all Selenium tests (headless mode)
./run-selenium-tests.sh

# Run with visible browser (watch tests execute)
./run-selenium-tests.sh --visible

# Run specific test
./run-selenium-tests.sh -t CSeleniumProjectCrudDemoTest

# Run specific test with visible browser
./run-selenium-tests.sh --visible -t CSeleniumProjectCrudDemoTest
```

### Available Selenium Tests

1. **CSeleniumProjectCrudDemoTest** - Complete CRUD workflow for Projects
2. **CSeleniumActivityCrudTest** - CRUD operations for Activities
3. **CSeleniumUserCrudTest** - User management CRUD operations

### Why Selenium?

✅ **Better Copilot Integration** - Selenium is widely recognized by AI coding assistants
✅ **Industry Standard** - Most popular browser automation framework
✅ **Flexible Browser Support** - Works with Chrome, Firefox, Edge, Safari
✅ **Dual Mode Operation** - Run headless (CI/CD) or visible (debugging)
✅ **Rich Ecosystem** - Extensive community and documentation

### Test Output

- **Screenshots**: `target/screenshots/selenium-*.png`
- **Test Reports**: `target/surefire-reports/`
- **Console Output**: Detailed logging with emoji indicators

### Documentation

See [SELENIUM_TESTING_GUIDE.md](SELENIUM_TESTING_GUIDE.md) for comprehensive documentation including:
- Configuration options
- Writing new tests
- Helper method reference
- CI/CD integration
- Troubleshooting
- Best practices

### Example Test

```java
@Test
@DisplayName("✅ Complete CRUD workflow for Projects using Selenium")
void testProjectCrudOperations() {
    // Login
    loginToApplication();
    
    // Navigate to view
    navigateTo("/project-overview");
    
    // Create new project
    clickNew();
    fillFirstTextField("My Project");
    fillFirstTextArea("Project description");
    selectFirstComboBoxOption();
    clickSave();
    
    // Verify
    boolean hasData = verifyGridHasData();
    
    // Take screenshot
    takeScreenshot("project-created");
}
```

### Copilot Integration

Selenium tests are designed to work seamlessly with GitHub Copilot and other AI assistants:

1. **Standard API**: Uses widely-recognized Selenium methods
2. **Clear Naming**: Descriptive method names that Copilot understands
3. **Comprehensive Base Class**: All common operations available
4. **Good Documentation**: Inline comments and Javadoc
5. **Example Tests**: Multiple working examples to learn from

### Running Tests in CI/CD

**GitHub Actions:**
```yaml
- name: Run Selenium Tests
  run: ./run-selenium-tests.sh --headless
  
- name: Upload Screenshots
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: selenium-screenshots
    path: target/screenshots/selenium-*.png
```

**Jenkins:**
```groovy
stage('Selenium Tests') {
    steps {
        sh './run-selenium-tests.sh --headless'
    }
    post {
        always {
            archiveArtifacts artifacts: 'target/screenshots/selenium-*.png'
            junit 'target/surefire-reports/*.xml'
        }
    }
}
```

---

For the complete guide, see [SELENIUM_TESTING_GUIDE.md](SELENIUM_TESTING_GUIDE.md)
