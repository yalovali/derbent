# Derbent Project Management Application
Derbent is a Java Spring Boot + Vaadin collaborative project management application inspired by Jira and ProjeQtOr, targeting small to medium-sized offices. Built with Java 21, Spring Boot 3.5, Vaadin 24.8, and Playwright-based UI testing infrastructure.

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

## Working Effectively

### Environment Setup (CRITICAL)
```bash
# ALWAYS source the Java environment setup first
source ./setup-java-env.sh

# This ensures Java 21 is used (REQUIRED by pom.xml)
# The script automatically configures JAVA_HOME and PATH

# Quick verification of environment (recommended on first use)
./verify-environment.sh
# This checks Java 21, Maven, SO libraries, and compilation
```

### Bootstrap, Build, and Test the Repository
```bash
# Prerequisites: Java 21 and Maven 3.9+ are required
source ./setup-java-env.sh  # Sets up Java 21
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.9+

# Clean and compile (NEVER CANCEL: takes 15+ seconds after first build)
mvn clean compile
# TIMEOUT: Set 10+ minutes. Expected time: 12-15 seconds for incremental builds

# Test compilation (NEVER CANCEL: takes 10+ seconds)  
mvn test-compile
# TIMEOUT: Set 5+ minutes. Expected time: 10-15 seconds

# Apply code formatting (required before commits)
mvn spotless:apply
# TIMEOUT: Set 2+ minutes. Expected time: 5 seconds

# Check code formatting
mvn spotless:check
# TIMEOUT: Set 2+ minutes. Expected time: 2-3 seconds
```

### Run the Application
```bash
# ALWAYS source Java environment and apply formatting first
source ./setup-java-env.sh
mvn spotless:apply

# Start the application (NEVER CANCEL: takes 15+ seconds)
# NOTE: The default configuration uses PostgreSQL. For development without PostgreSQL:
mvn spring-boot:run -Dspring.profiles.active=h2
# TIMEOUT: Set 5+ minutes. Expected time: 12-15 seconds to start
# Application will be available at http://localhost:8080

# Alternative: Use Maven profile for H2 database
mvn spring-boot:run -Ph2-local-development

# Stop the application
# Use Ctrl+C or: pkill -f "spring-boot:run"
```

### Testing Infrastructure  
```bash
# IMPORTANT: Before running any tests, ensure SO libraries are installed
# Run once after cloning the repository:
./install-so-libraries.sh

# Run Playwright UI automation tests with screenshots (NEVER CANCEL: takes 37+ seconds)
./run-playwright-tests.sh menu
# TIMEOUT: Set 5+ minutes. Expected time: 37-40 seconds
# Generates screenshots in target/screenshots/
# NOTE: Script automatically sets up Java 21 environment
# NOTE: Tests automatically initialize sample data if company combobox is empty

# Run comprehensive Playwright tests (NEVER CANCEL: takes 2+ minutes)
./run-playwright-tests.sh comprehensive
# TIMEOUT: Set 10+ minutes. Expected time: 2-5 minutes

# Run specific Playwright test categories
./run-playwright-tests.sh login         # Company login test
./run-playwright-tests.sh status-types  # Status and type views
./run-playwright-tests.sh buttons       # Button functionality test
./run-playwright-tests.sh all           # Run all tests

# Clean test artifacts
./run-playwright-tests.sh clean

# For detailed testing guidelines and patterns, see:
# docs/development/copilot-guidelines.md
```

## Validation

### ALWAYS run through complete validation scenarios after making changes:

#### 1. Build and Format Validation
```bash
# CRITICAL: Always run these in sequence before committing
source ./setup-java-env.sh             # Setup Java 21 (REQUIRED)
mvn spotless:apply                     # Fix formatting issues
mvn spotless:check                     # Verify formatting is correct
mvn clean compile                      # Full build (NEVER CANCEL: 12-15 seconds)
```

#### 2. Application Startup Validation  
```bash
# Start application and verify it loads (use H2 profile for development)
source ./setup-java-env.sh
mvn spring-boot:run -Dspring.profiles.active=h2 &
APP_PID=$!

# Wait for startup (12-15 seconds)
sleep 20

# Test application is responding  
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/
# Expected: 302 (redirect to login page)

# Stop application
kill $APP_PID
```

#### 3. UI Automation Validation
```bash
# IMPORTANT: Ensure SO libraries are installed first
./install-so-libraries.sh  # Run once after cloning

# ALWAYS test UI changes with Playwright screenshots
./run-playwright-tests.sh menu
# Expected: Screenshots generated successfully in 37-40 seconds
# Check target/screenshots/ for generated images
# Tests automatically initialize DB if company combobox is empty

# For specific UI changes, run targeted tests
./run-playwright-tests.sh [category]  # See categories in Testing Infrastructure section

# For complete testing guidelines, workflows, and patterns, refer to:
# docs/development/copilot-guidelines.md
```

#### 4. Manual Validation Scenarios
When making UI or business logic changes, ALWAYS manually test these workflows:

**Login Flow:**
1. Start application: `mvn spring-boot:run -Dspring.profiles.active=h2`
2. Navigate to http://localhost:8080
3. Verify login page displays
4. Test login with sample data (users created automatically)

**Core Navigation:**
1. Test main menu navigation between views
2. Verify project selection works
3. Test creating/editing activities, meetings, users
4. Verify kanban board functionality

**Data Operations:**
1. Create new project
2. Add activities to project  
3. Assign users to activities
4. Test time tracking functionality
5. Verify all changes persist

#### 5. Screenshot Documentation for UI Changes
**CRITICAL: For ALL UI-related changes, ALWAYS provide screenshot evidence in PRs:**

**Using Playwright Browser Automation:**
```bash
# The application provides Playwright browser integration for testing
# This approach is PREFERRED for consistent, reproducible screenshots

# 1. Start the application with H2 database (async mode recommended)
mvn spring-boot:run -Dspring.profiles.active=h2 &
APP_PID=$!

# 2. Wait for application startup (12-15 seconds)
sleep 20

# 3. Use playwright-browser tools to capture screenshots
# - Navigate to the page: playwright-browser_navigate(url)
# - Take snapshots: playwright-browser_snapshot()
# - Interact with elements: playwright-browser_click(), playwright-browser_fill_form()
# - Capture screenshots: playwright-browser_take_screenshot(filename)

# 4. Example workflow for testing delete prevention:
#    a. Navigate to login page
#    b. Click "DB Minimal" button to load sample data
#    c. Login with admin/test123
#    d. Navigate to the entity management page
#    e. Try to delete an entity that should be protected
#    f. Capture screenshot showing the error message
#    g. Save screenshot with descriptive name: "delete-status-error.png"

# 5. Stop the application when done
kill $APP_PID
```

**Screenshot Best Practices:**
- **Capture at key moments**: Before action, during action, after action (showing result)
- **Use descriptive filenames**: `feature-state-description.png` (e.g., `delete-status-error.png`)
- **Show error messages**: Always capture screenshots of validation/error messages
- **Document workflows**: Capture each step of multi-step operations
- **Include in PR**: Embed screenshots in PR description using markdown image syntax
- **Verify screenshots**: Review captured images to ensure they show relevant content

**When to Capture Screenshots:**
1. **Error Messages**: Always capture validation errors, delete restrictions, save failures
2. **New Features**: Show the feature in action with sample data
3. **UI Changes**: Before/after screenshots showing the visual changes
4. **Workflow Changes**: Step-by-step screenshots of the entire workflow
5. **Bug Fixes**: Screenshot showing the bug is fixed

**MANDATORY: Test All Major Entity Screens After Each Update**
After making changes that affect entities, workflows, or UI components, **ALWAYS** test and provide screenshots for ALL major entity screens:

Required Entity Screens to Test:
1. **Activities Management** (`/cdynamicpagerouter/page:3`)
   - Test status transitions
   - Verify initial status is set for new items
   - Show status combobox populated with workflow transitions
   
2. **Meetings Management** (`/cdynamicpagerouter/page:4`)
   - Test meeting status workflow
   - Verify meeting type workflows
   
3. **Projects Management** (`/cdynamicpagerouter/page:1`)
   - Test project-level operations
   
4. **Users Management** (`/cdynamicpagerouter/page:12`)
   - Test user operations

**Workflow Testing Requirements:**
- **ALWAYS** demonstrate status combobox is populated with valid workflow transitions
- **ALWAYS** show initial status is set when creating new items
- **ALWAYS** test different entity types (Activities, Meetings, etc.)
- **ALWAYS** include screenshots in PR description showing:
  - Initial status being set for new items
  - Status combobox showing available transitions
  - Each major entity screen working correctly

## Build Configuration and Timing

### Maven Build Phases and Expected Times:
- **clean**: 2-5 seconds
- **compile**: 12-15 seconds (incremental builds after first compile)
- **test-compile**: 10-15 seconds
- **spotless:apply**: ~5 seconds
- **spotless:check**: ~2-3 seconds
- **spring-boot:run**: 12-15 seconds to start

### CRITICAL Timeout Settings:
- **Build commands**: MINIMUM 5 minutes timeout
- **Playwright tests**: MINIMUM 5 minutes timeout
- **Application startup**: MINIMUM 2 minutes timeout

### NEVER CANCEL warnings:
- **mvn clean compile** - Takes 12-15 seconds for incremental builds, DO NOT CANCEL
- **./run-playwright-tests.sh** - Takes 37+ seconds minimum, DO NOT CANCEL
- **mvn spring-boot:run** - Takes 12-15 seconds to start, DO NOT CANCEL

## Key Development Patterns

### Project Structure
```
src/main/java/tech/derbent/
├── abstracts/           # Base classes, annotations, utilities
├── activities/          # Activity management (CRITICAL feature)
├── companies/           # Company management  
├── meetings/            # Meeting management
├── projects/            # Project management (CRITICAL feature)
├── users/              # User management (CRITICAL feature)
├── setup/              # System settings
└── administration/     # Company settings

docs/                   # Essential documentation
├── architecture/       # Design patterns and coding standards
├── implementation/     # Key feature implementation patterns
└── testing/           # Playwright testing strategies
```

### Coding Standards (CRITICAL - Follow Strictly)
- **ALL domain classes MUST be prefixed with "C"** (e.g., CActivity, CUser, CProject)
- **Follow MVC pattern**: Model (domain), View (UI), Controller (service)
- **Always use CAbstractService** as base for service classes
- **Entity classes extend CEntityDB<T>** for database entities
- **Views extend appropriate CAbstract*Page** base classes

### Notification Standards (CRITICAL - ALWAYS Follow)
**NEVER use direct Vaadin Notification.show() calls or manual dialog instantiation. ALWAYS use the unified notification system:**

#### For Components with Dependency Injection:
```java
@Autowired
private CNotificationService notificationService;

// Toast notifications - use appropriate method for context
notificationService.showSuccess("Data saved successfully");    // Green, bottom-start, 2s
notificationService.showError("Save failed");                  // Red, middle, 8s  
notificationService.showWarning("Check your input");           // Orange, top-center, 5s
notificationService.showInfo("Process completed");             // Blue, bottom-start, 5s

// Modal dialogs for important messages
notificationService.showErrorDialog(exception);
notificationService.showWarningDialog("Important warning message");
notificationService.showConfirmationDialog("Delete item?", () -> deleteItem());

// Convenience methods for common operations
notificationService.showSaveSuccess();
notificationService.showDeleteSuccess();
notificationService.showCreateSuccess();
notificationService.showSaveError();
notificationService.showDeleteError();
notificationService.showOptimisticLockingError();
```

#### For Utility Classes or Static Contexts:
```java
import tech.derbent.api.ui.notifications.CNotifications;

// Use static methods when dependency injection not available
CNotifications.showSuccess("Operation completed");
CNotifications.showError("Something went wrong");
CNotifications.showSaveSuccess();
CNotifications.showDeleteError();
CNotifications.showWarningDialog("Important warning");
```

#### FORBIDDEN Patterns (Do NOT Use):
```java
// ❌ NEVER use direct Vaadin calls
Notification.show("message");
Notification.show("message", 3000, Notification.Position.TOP_CENTER);

// ❌ NEVER instantiate dialogs directly  
new CWarningDialog("message").open();
new CInformationDialog("message").open();
new CExceptionDialog(exception).open();

// ❌ NEVER use inconsistent positioning/styling
notification.setPosition(Position.MIDDLE);
notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
```

#### Integration Patterns:
- **CAbstractEntityDBPage descendants**: Service auto-injected, use `notificationService` field
- **Components**: Add `@Autowired CNotificationService notificationService` field and setter
- **Utility classes**: Use `CNotifications` static methods
- **Dialogs extending CDBEditDialog**: Service available if injected in parent

**Rule: Every notification/message to users MUST go through CNotificationService or CNotifications**
- **Use CEnhancedBinder** for form binding instead of vanilla Vaadin Binder

### Database Configuration
- **Development**: Use H2 profile: `mvn spring-boot:run -Dspring.profiles.active=h2`
- **Production**: PostgreSQL (requires manual setup and database server)
- **Schema**: Hibernate auto-creates tables with sample data
- **Sample Data**: Automatically loaded on startup via CSampleDataInitializer

### Testing Strategy
- **Playwright Tests**: Located in `src/test/java/automated_tests/`
- **UI Automation**: Browser-based testing with screenshot capture
- **Manual Tests**: Documented scenarios for critical workflows

## Common Tasks

### Adding New Entities
1. Create domain class extending `CEntityDB<T>` in appropriate package
2. Create repository interface extending `CAbstractRepository<T>`
3. Create service class extending `CAbstractService<T>`
4. Create view class extending appropriate `CAbstract*Page`
5. Add navigation entry in `MainLayout.java`
6. Add Playwright tests for UI validation

### Debugging Issues
```bash
# Check application logs during startup (use H2 profile for development)
mvn spring-boot:run -Dspring.profiles.active=h2 | grep -E "(ERROR|WARN|DEBUG)"

# Validate database connectivity - H2 console available at:
# http://localhost:8080/h2-console (when using H2 profile)
# URL: jdbc:h2:mem:testdb, User: sa, Password: (empty)

# Check test failures
./run-playwright-tests.sh menu 2>&1 | grep -A 5 -B 5 "FAILURE\|ERROR"

# Generate test reports with screenshots
./run-playwright-tests.sh all
# Reports and screenshots in target/screenshots/
```

### Code Quality Checks
```bash
# ALWAYS run before committing:
mvn spotless:apply      # Fix formatting
mvn spotless:check      # Verify formatting
mvn clean compile      # Full build verification (NEVER CANCEL: 12-15 seconds)

# Check for forbidden notification patterns (should return empty):
grep -r "Notification\.show\|new.*Dialog.*\.open()" src/main/java --include="*.java" | grep -v "CNotificationService.java"

# Optional quality checks:
./run-playwright-tests.sh menu  # UI validation (37-40 seconds)

# For complete testing guidelines, workflows, and patterns:
# See docs/development/copilot-guidelines.md
```

## Technology Stack Reference

### Core Technologies
- **Java 21** - Programming language (REQUIRED - configured via setup-java-env.sh)
- **Spring Boot 3.5** - Application framework  
- **Vaadin Flow 24.8** - UI framework
- **Hibernate/JPA** - Data persistence
- **H2 Database** - Development database
- **PostgreSQL** - Production database
- **Maven 3.9+** - Build tool

### Testing Technologies
- **JUnit 5** - Unit testing framework
- **Playwright** - Browser automation testing
- **Spring Boot Test** - Application testing framework

### Development Tools
- **Spotless** - Code formatting (Eclipse formatter)
- **Spring DevTools** - Hot reload in development
- **Vaadin Control Center** - Development tools

## Common Failures and Solutions

### Build Failures
- **"Unable to determine Dialect"**: Database configuration issue, ensure H2 profile is active
- **Spotless violations**: Run `mvn spotless:apply` to fix formatting
- **Compilation errors**: Usually due to missing imports or incorrect generics

### Test Failures  
- **Spring context failures**: Expected in development, focus on new test failures only
- **Playwright browser issues**: Use `./run-playwright-tests.sh mock` for screenshot testing
- **Timeout errors**: Increase timeout values, builds take 12-15 seconds (incremental)

### Application Startup Issues
- **"Unable to determine Dialect"**: Database configuration issue, use H2 profile: `mvn spring-boot:run -Dspring.profiles.active=h2`
- **Port 8080 in use**: Kill existing process with `pkill -f spring-boot:run`
- **Database errors**: For development, use H2 profile to avoid PostgreSQL dependency
- **Vaadin compilation**: Delete `target/` and rebuild with `mvn clean compile`

## File Locations Reference

### Key Configuration Files
- `pom.xml` - Maven configuration and dependencies
- `src/main/resources/application.properties` - Application configuration
- `src/main/resources/application-h2.properties` - H2 database configuration
- `eclipse-formatter.xml` - Code formatting configuration
- `.prettierrc.json` - TypeScript/JavaScript formatting

### Important Documentation  
- `docs/architecture/coding-standards.md` - CRITICAL coding guidelines
- `docs/testing/playwright-*.md` - Playwright testing strategies
- `README.md` - Project overview and quick start

### Build and Test Scripts
- `run-playwright-tests.sh` - Playwright UI automation
- `mvnw` / `mvnw.cmd` - Maven wrapper scripts

---

**Remember**: Always follow the coding standards in `docs/architecture/coding-standards.md` and validate ALL changes with the build and test procedures documented above. When in doubt, refer to existing patterns in the codebase and comprehensive documentation in the `docs/` directory.
