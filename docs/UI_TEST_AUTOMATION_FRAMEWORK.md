# UI Test Automation Framework Documentation

## Overview

This document describes the comprehensive UI test automation framework implemented for the Derbent application. The framework provides dual automation support using both **Playwright** and **Selenium WebDriver**, enabling robust cross-browser testing and comprehensive validation of the application's user interface.

## Framework Architecture

### Dual Testing Framework Approach

The implementation provides two complementary automation frameworks:

1. **Playwright Framework** - Modern, fast, and reliable
2. **Selenium WebDriver Framework** - Mature, widely adopted, industry standard

Both frameworks test the same functionality but offer different advantages:

| Feature | Playwright | Selenium |
|---------|------------|----------|
| Speed | ⚡ Very Fast | 🐢 Moderate |
| Browser Support | Modern browsers | All browsers including legacy |
| API Design | Modern async/await | Traditional synchronous |
| Setup Complexity | Simple | Moderate |
| Industry Adoption | Growing rapidly | Mature ecosystem |
| Mobile Testing | Built-in | Via Appium |
| Debugging | Excellent trace viewer | Various tools |

## Test Categories

### 1. Authentication Tests
- **Login Functionality**: Valid credentials, session management
- **Logout Functionality**: Proper session termination
- **Invalid Login Handling**: Error messages, security validation
- **Session Timeout**: Automatic logout after inactivity

### 2. Navigation Tests
- **View Navigation**: Between Projects, Meetings, Activities, Decisions
- **Menu Interactions**: Side navigation, breadcrumbs
- **URL Routing**: Direct navigation, browser back/forward
- **Responsive Menu**: Mobile navigation behavior

### 3. Grid Interaction Tests
- **Data Display**: Grid loading, data visualization
- **Sorting**: Column sorting functionality
- **Filtering**: Search and filter capabilities
- **Cell Interactions**: Row selection, cell clicking
- **Pagination**: Large dataset navigation

### 4. CRUD Operations Tests
- **Create**: New entity creation forms
- **Read**: Data display and detail views
- **Update**: Edit functionality and data modification
- **Delete**: Deletion with confirmation dialogs

### 5. Form Validation Tests
- **Required Fields**: Validation messages
- **Data Types**: Email, date, number validation
- **Business Rules**: Custom validation logic
- **Error Handling**: User-friendly error messages

### 6. Responsive Design Tests
- **Desktop**: 1920x1080 full desktop view
- **Tablet Landscape**: 1024x768 tablet view
- **Tablet Portrait**: 768x1024 portrait mode
- **Mobile**: 375x667 mobile phone view

### 7. Accessibility Tests
- **Semantic HTML**: Proper heading structure
- **ARIA Labels**: Screen reader compatibility
- **Keyboard Navigation**: Tab order and focus management
- **Color Contrast**: Visual accessibility standards

## Implementation Details

### Project Structure

```
src/test/java/tech/derbent/ui/automation/
├── PlaywrightUIAutomationTest.java    # Playwright test suite
├── SeleniumUIAutomationTest.java      # Selenium test suite
└── SimpleUIDemo.java                  # Basic demo tests

scripts/
├── run-ui-tests.sh                    # Master test runner (both frameworks)
├── run-playwright-tests.sh            # Playwright-specific runner
└── run-selenium-tests.sh              # Selenium-specific runner

target/screenshots/                     # Test execution screenshots
├── playwright-*.png                   # Playwright screenshots
└── selenium-*.png                     # Selenium screenshots
```

### Test Configuration

Both frameworks use consistent configuration:

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "logging.level.tech.derbent=DEBUG"
})
```

### Credentials and Test Data

- **Test Username**: `admin`
- **Test Password**: `test123` (aligned with project standards)
- **Test Database**: H2 in-memory database for isolation
- **Dynamic Test Data**: Timestamp-based unique identifiers

## Running Tests

### Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Internet connection** for downloading browser binaries
4. **Application running** on localhost (or configured port)

### Quick Start

```bash
# Run all tests with both frameworks
./run-ui-tests.sh all

# Run only Playwright tests
./run-playwright-tests.sh all

# Run only Selenium tests
./run-selenium-tests.sh all

# Install browser dependencies
./run-ui-tests.sh install
```

### Detailed Commands

#### Master Test Runner (run-ui-tests.sh)

```bash
# Framework selection
./run-ui-tests.sh all          # Both frameworks
./run-ui-tests.sh playwright   # Playwright only
./run-ui-tests.sh selenium     # Selenium only

# Test categories (both frameworks)
./run-ui-tests.sh login        # Login/logout tests
./run-ui-tests.sh crud         # CRUD operations
./run-ui-tests.sh grid         # Grid interactions
./run-ui-tests.sh navigation   # Navigation tests
./run-ui-tests.sh responsive   # Responsive design
./run-ui-tests.sh validation   # Form validation

# Utilities
./run-ui-tests.sh compare      # Comparative testing
./run-ui-tests.sh install      # Install dependencies
./run-ui-tests.sh clean        # Clean artifacts
```

#### Playwright-Specific Runner

```bash
./run-playwright-tests.sh all           # Complete suite
./run-playwright-tests.sh login         # Login tests
./run-playwright-tests.sh crud          # CRUD tests
./run-playwright-tests.sh accessibility # Accessibility tests
./run-playwright-tests.sh workflow      # Complete workflow
```

#### Selenium-Specific Runner

```bash
./run-selenium-tests.sh all         # Complete suite
./run-selenium-tests.sh login       # Login tests
./run-selenium-tests.sh crud        # CRUD tests
./run-selenium-tests.sh responsive  # Responsive tests
./run-selenium-tests.sh validation  # Form validation
```

### Maven Integration

```bash
# Run specific test class
mvn test -Dtest=PlaywrightUIAutomationTest
mvn test -Dtest=SeleniumUIAutomationTest

# Run specific test method
mvn test -Dtest=PlaywrightUIAutomationTest#testLoginFunctionality
mvn test -Dtest=SeleniumUIAutomationTest#testCRUDOperations

# Run with profiles
mvn test -Dtest=PlaywrightUIAutomationTest -Dspring.profiles.active=test
```

## Test Results and Debugging

### Screenshot Capture

Both frameworks automatically capture screenshots at key moments:
- **Before/After Actions**: Login, form submission, navigation
- **Error Conditions**: Test failures, exceptions
- **Verification Points**: Successful operations, data validation

Screenshots are saved to: `target/screenshots/`
- Playwright: `playwright-{action}-{timestamp}.png`
- Selenium: `selenium-{action}-{timestamp}.png`

### Log Analysis

Comprehensive logging provides detailed information:

```bash
# View test logs
tail -f target/surefire-reports/TEST-*.xml

# Application logs during testing
tail -f application.log
```

### Common Issues and Solutions

#### Browser Installation Issues

**Playwright:**
```bash
# Manual browser installation
./run-playwright-tests.sh install
mvn exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"
```

**Selenium:**
```bash
# WebDriverManager handles automatically
# Check Chrome installation
google-chrome --version
```

#### Port Conflicts

```bash
# Check if application is running
curl -I http://localhost:8080

# Kill existing processes
pkill -f spring-boot:run
```

#### Memory Issues

```bash
# Increase Maven memory
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"
```

## Integration with CI/CD

### GitHub Actions Example

```yaml
name: UI Test Automation

on: [push, pull_request]

jobs:
  ui-tests:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Cache Maven dependencies
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
    
    - name: Install UI test dependencies
      run: ./run-ui-tests.sh install
    
    - name: Run Playwright tests
      run: ./run-playwright-tests.sh all
    
    - name: Run Selenium tests
      run: ./run-selenium-tests.sh all
    
    - name: Upload screenshots
      uses: actions/upload-artifact@v3
      if: always()
      with:
        name: test-screenshots
        path: target/screenshots/
```

### Jenkins Pipeline Example

```groovy
pipeline {
    agent any
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Install Dependencies') {
            steps {
                sh './run-ui-tests.sh install'
            }
        }
        
        stage('Run UI Tests') {
            parallel {
                stage('Playwright Tests') {
                    steps {
                        sh './run-playwright-tests.sh all'
                    }
                }
                stage('Selenium Tests') {
                    steps {
                        sh './run-selenium-tests.sh all'
                    }
                }
            }
        }
    }
    
    post {
        always {
            archiveArtifacts artifacts: 'target/screenshots/*.png', fingerprint: true
            publishTestResults testResultsPattern: 'target/surefire-reports/*.xml'
        }
    }
}
```

## Best Practices

### Test Development Guidelines

1. **Page Object Model**: Implement for better maintainability
2. **Data Independence**: Use unique test data with timestamps
3. **Screenshot Strategy**: Capture before/after critical actions
4. **Error Handling**: Graceful failure with informative messages
5. **Wait Strategies**: Proper explicit waits for dynamic content

### Code Quality Standards

1. **Commenting**: Comprehensive JavaDoc for all test methods
2. **Logging**: Detailed info logging for test execution flow
3. **Assertions**: Clear, specific assertions with meaningful messages
4. **Cleanup**: Proper test cleanup and resource management
5. **Isolation**: Independent tests that don't rely on execution order

### Performance Considerations

1. **Parallel Execution**: Framework supports parallel test execution
2. **Browser Reuse**: Context reuse where appropriate
3. **Test Data**: Minimize database interactions
4. **Screenshots**: Selective capture to reduce overhead
5. **Cleanup**: Regular cleanup of test artifacts

## Troubleshooting Guide

### Common Test Failures

#### Login Issues
- **Symptom**: Login tests fail consistently
- **Solutions**: 
  - Verify test credentials match application
  - Check database state and user existence
  - Validate login form selectors

#### Navigation Failures
- **Symptom**: Cannot navigate between views
- **Solutions**:
  - Verify navigation menu structure
  - Check for loading indicators
  - Validate route configurations

#### Grid Interaction Issues
- **Symptom**: Grid tests fail to find elements
- **Solutions**:
  - Check grid component rendering
  - Verify data loading completion
  - Update element selectors

#### Form Validation Problems
- **Symptom**: Validation tests don't trigger errors
- **Solutions**:
  - Verify form field requirements
  - Check validation message selectors
  - Validate business rule implementation

### Debug Mode Execution

#### Playwright Debug Mode
```bash
# Run with headed browser
PLAYWRIGHT_HEADLESS=false ./run-playwright-tests.sh all

# Enable debug logging
DEBUG=pw:api ./run-playwright-tests.sh all
```

#### Selenium Debug Mode
```bash
# Run with visible browser
SELENIUM_HEADLESS=false ./run-selenium-tests.sh all

# Enable detailed logging
mvn test -Dtest=SeleniumUIAutomationTest -Dselenium.debug=true
```

## Framework Comparison Results

### Performance Metrics

| Metric | Playwright | Selenium |
|--------|------------|----------|
| Test Execution Speed | ~2x faster | Baseline |
| Browser Launch Time | ~1s | ~3s |
| Element Finding | Faster | Moderate |
| Screenshot Capture | Very fast | Moderate |
| Memory Usage | Lower | Higher |

### Reliability Metrics

| Metric | Playwright | Selenium |
|--------|------------|----------|
| Flaky Test Rate | Lower | Moderate |
| Wait Strategy | Auto-wait | Manual waits |
| Error Recovery | Better | Standard |
| Cross-browser | Excellent | Excellent |

### Maintenance Metrics

| Metric | Playwright | Selenium |
|--------|------------|----------|
| API Stability | High | Very High |
| Documentation | Excellent | Extensive |
| Community | Growing | Mature |
| Tool Ecosystem | Modern | Comprehensive |

## Conclusion

The dual UI testing framework provides comprehensive coverage of the Derbent application's user interface functionality. By leveraging both Playwright and Selenium, the framework ensures:

- **Reliability**: Cross-validation through dual implementation
- **Performance**: Playwright for fast feedback
- **Compatibility**: Selenium for broad browser support
- **Maintainability**: Well-structured, documented code
- **Scalability**: Easy addition of new test scenarios

This implementation follows the project's coding standards and provides a solid foundation for continuous quality assurance of the application's user interface.