# Derbent Project Management Application
Derbent is a Java Spring Boot + Vaadin collaborative project management application inspired by Jira and ProjeQtOr, targeting small to medium-sized offices. Built with Java 21, Spring Boot 3.5, Vaadin 24.8, and Playwright-based UI testing infrastructure.

**ALWAYS reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.**

---

## 🤖 AI Assistant Workflow Preferences

### Autonomous Operation Mode
**The AI assistant should work autonomously without asking for permission for standard operations:**

✅ **ALWAYS proceed without confirmation for:**
- File modifications (create, edit, delete)
- Code refactoring and improvements
- Running builds, tests, and validation
- Applying code formatting (mvn spotless:apply)
- Taking screenshots for documentation
- Committing changes to git with descriptive messages
- Installing dependencies or tools when needed
- Running Playwright tests after UI changes

❌ **ONLY ask for confirmation when:**
- Deleting entire directories or multiple files
- Making breaking changes to public APIs
- Modifying production configuration files
- Pushing to remote git repository
- Making database schema changes in production mode

### Standard Workflow After Completing Tasks
When completing a task, **automatically execute this workflow:**

1. **Code Formatting**: Apply Eclipse formatter (via IDE) or configure Spotless Maven plugin
2. **Compilation Check**: Run `mvn clean compile` (if code changes)
3. **Test Validation**: Run appropriate tests (if code changes affect functionality)
4. **Git Commit**: Commit changes with a descriptive message following conventional commits format
5. **Summary**: Provide a brief summary of what was done

### Git Commit Preferences
- **Always commit** completed work without asking
- Use **conventional commit** format: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`
- Include detailed description of changes
- Include usage examples for new features
- Stage only related files (don't commit unrelated changes)

### Testing Preferences
- **Run Playwright tests automatically** after UI changes:
  - Quick validation: `./run-playwright-tests.sh menu`
  - Comprehensive: `./run-playwright-tests.sh comprehensive`
- **Capture screenshots** for documentation when making UI changes
- **Use H2 profile** for development testing: `-Dspring.profiles.active=h2`
- **Run with visible browser** for initial test runs to verify behavior

### Communication Style
- **Be concise** - provide essential information without over-explaining
- **Use action-oriented language** - "Running tests...", "Applying formatting...", "Committing changes..."
- **Report progress** for long-running operations
- **Highlight critical issues** that need attention
- **Summarize results** at the end of operations

---

## Working Effectively

### Environment Setup (CRITICAL)
```bash
# ALWAYS source the Java environment setup first
source ./bin/setup-java-env.sh

# This ensures Java 21 is used (REQUIRED by pom.xml)
# The script automatically configures JAVA_HOME and PATH

# Quick verification of environment (recommended on first use)
./bin/verify-environment.sh
# This checks Java 21, Maven, SO libraries, and compilation
```

### Bootstrap, Build, and Test the Repository
```bash
# Prerequisites: Java 21 and Maven 3.9+ are required
source ./bin/setup-java-env.sh  # Sets up Java 21
java -version    # Should show Java 21
mvn -version     # Should show Maven 3.9+

# Clean and compile (NEVER CANCEL: takes 15+ seconds after first build)
mvn clean compile
# TIMEOUT: Set 10+ minutes. Expected time: 12-15 seconds for incremental builds

# Test compilation (NEVER CANCEL: takes 10+ seconds)  
mvn test-compile
# TIMEOUT: Set 5+ minutes. Expected time: 10-15 seconds

# Apply code formatting (if Spotless is configured)
# Note: Spotless plugin needs to be added to pom.xml first
# mvn spotless:apply

# Check code formatting (if Spotless is configured)
# mvn spotless:check
```

### Run the Application
```bash
# ALWAYS source Java environment first
source ./bin/setup-java-env.sh

# Note: Code formatting can be done via IDE using eclipse-formatter.xml
# Or by configuring Spotless Maven plugin in pom.xml

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
./bin/install-so-libraries.sh

# Run Playwright UI automation tests with screenshots (NEVER CANCEL: takes 37+ seconds)
./run-playwright-tests.sh menu
# TIMEOUT: Set 5+ minutes. Expected time: 37-40 seconds
# Generates screenshots in target/screenshots/
# NOTE: Script automatically sets up Java 21 environment
# NOTE: Tests automatically initialize sample data if company combobox is empty

# Run comprehensive Playwright tests (NEVER CANCEL: takes 2+ minutes)
./run-playwright-tests.sh comprehensive
# TIMEOUT: Set 10+ minutes. Expected time: 2-5 minutes
# Tests all views, grids, and CRUD operations

# Run specific Playwright test scenarios
./run-playwright-tests.sh menu           # Fast menu navigation
./run-playwright-tests.sh comprehensive  # Complete view & CRUD testing
./run-playwright-tests.sh all-views      # Navigate all application views
./run-playwright-tests.sh crud           # CRUD operations testing

# Interactive mode - configure before running
INTERACTIVE_MODE=true ./run-playwright-tests.sh menu

# Fast headless execution without screenshots
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true ./run-playwright-tests.sh menu

# Slow motion debugging (visible browser with delays)
PLAYWRIGHT_SLOWMO=500 ./run-playwright-tests.sh menu

# Mobile viewport testing (iPhone 12)
PLAYWRIGHT_VIEWPORT_WIDTH=390 PLAYWRIGHT_VIEWPORT_HEIGHT=844 ./run-playwright-tests.sh menu

# Clean test artifacts
./run-playwright-tests.sh clean

# Show all available options
./run-playwright-tests.sh help

# For detailed testing guidelines and patterns, see:
# docs/development/copilot-guidelines.md
```

## Validation

### ALWAYS run through complete validation scenarios after making changes:

#### 1. Build and Format Validation
```bash
# CRITICAL: Always run these in sequence before committing
source ./bin/setup-java-env.sh             # Setup Java 21 (REQUIRED)
# Note: Apply code formatting via IDE (Eclipse formatter: eclipse-formatter.xml)
mvn clean compile                      # Full build (NEVER CANCEL: 12-15 seconds)
```

#### 2. Application Startup Validation  
```bash
# Start application and verify it loads (use H2 profile for development)
source ./bin/setup-java-env.sh
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
./bin/install-so-libraries.sh  # Run once after cloning

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

#### C-Prefix Convention (MANDATORY)
**ALL custom classes MUST be prefixed with "C"** to distinguish from framework classes:

```java
// ✅ CORRECT
public class CActivity extends CProjectItem<CActivity> { }
public class CActivityService extends CEntityOfProjectService<CActivity> { }
public class CActivityView extends CAbstractPage { }
public class CButton extends Button { }

// ❌ INCORRECT
public class Activity { }  // Missing C-prefix
public class ActivityService { }  // Missing C-prefix
```

**Benefits**: Instant recognition of custom vs. framework classes, enhanced IDE navigation, AI-assisted development optimization

**Exceptions**: 
- Interface names start with `I`: `IActivityRepository`, `ISessionService`
- Test classes: `CActivityTest`, `CActivityServiceTest`
- Package names: lowercase without prefix

#### Core Development Patterns
- **Follow MVC pattern**: Model (domain), View (UI), Controller (service)
- **Always use CAbstractService** as base for service classes
- **Entity classes extend CEntityDB<T>** for database entities
- **Views extend appropriate CAbstract*Page** base classes
- **Use @AMetaData** for metadata-driven development (see Entity Constants section below)

### UI Component Naming Standards (MANDATORY - No Exceptions)

#### 1. Field Naming Convention: typeName Format
ALL UI component fields MUST follow the `{type}{Name}` convention:

```java
// ✅ CORRECT - typeName format
private CButton buttonAdd;              // button{Name}
private CButton buttonDelete;           // button{Name}
private CButton buttonSave;             // button{Name}
private CButton buttonMoveUp;           // button{Name}
private CButton buttonMoveDown;         // button{Name}
private CDialog dialogConfirmation;     // dialog{Name}
private CDialog dialogEntitySelection;  // dialog{Name}
private CVerticalLayout layoutMain;     // layout{Name}
private CHorizontalLayout layoutToolbar; // layout{Name}
private CGrid<CEntity> gridItems;       // grid{Name}
private ComboBox<String> comboBoxStatus; // comboBox{Name}
private TextField textFieldName;        // textField{Name}

// ❌ WRONG - Do NOT use these patterns
private CButton addBtn;                 // Wrong: use buttonAdd
private CButton btn_delete;             // Wrong: use buttonDelete  
private Button addButton;               // Wrong: use CButton buttonAdd
private CDialog selectionDlg;           // Wrong: use dialogSelection
private HorizontalLayout toolbar;       // Wrong: use CHorizontalLayout layoutToolbar
private Grid<CEntity> grid;             // Wrong: use CGrid gridItems
```

#### 2. Event Handler Naming Convention: on_xxx_eventType
ALL event handlers MUST follow the `on_{componentName}_{eventType}` pattern:

```java
// ✅ CORRECT - on_componentName_eventType format
protected void on_buttonAdd_clicked() {
    // Handle add button click
}

protected void on_buttonDelete_clicked() {
    // Handle delete button click
}

protected void on_buttonMoveUp_clicked() {
    // Handle move up button click
}

protected void on_gridItems_selected(CEntity item) {
    // Handle grid selection
}

protected void on_gridItems_doubleClicked(CEntity item) {
    // Handle grid double-click
}

protected void on_comboBoxStatus_changed(String status) {
    // Handle status change
}

protected void on_dialogConfirmation_closed() {
    // Handle dialog close
}

// ❌ WRONG - Do NOT use these patterns
private void handleAdd() { }            // Wrong: use on_buttonAdd_clicked
private void onDeleteClick() { }        // Wrong: use on_buttonDelete_clicked
private void processSelection() { }     // Wrong: use on_xxx_selected
private void handleDoubleClick() { }    // Wrong: use on_gridItems_doubleClicked
```

#### 3. Factory Method Naming Convention: create_xxx
ALL component factory methods MUST follow the `create_{componentName}` pattern:

```java
// ✅ CORRECT - create_componentName format
protected CButton create_buttonAdd() {
    final CButton button = new CButton(VaadinIcon.PLUS.create());
    button.addClickListener(e -> on_buttonAdd_clicked());
    return button;
}

protected CButton create_buttonDelete() {
    final CButton button = new CButton(VaadinIcon.TRASH.create());
    button.addClickListener(e -> on_buttonDelete_clicked());
    return button;
}

protected CDialog create_dialogConfirmation() {
    // Create and return dialog
}

// ❌ WRONG - Do NOT use these patterns
protected CButton createAddButton() { }     // Wrong: use create_buttonAdd
protected CButton getDeleteButton() { }     // Wrong: use create_buttonDelete
protected void setupButtons() { }           // Wrong: use create_xxx for each button
```

#### 4. Component Inheritance: Always Use C-Prefixed Components
NEVER use raw Vaadin components. ALWAYS use C-prefixed wrappers:

| ❌ Vaadin Component | ✅ Use Instead |
|---------------------|----------------|
| `Button` | `CButton` |
| `Dialog` | `CDialog` |
| `VerticalLayout` | `CVerticalLayout` |
| `HorizontalLayout` | `CHorizontalLayout` |
| `Grid<T>` | `CGrid<T>` |
| `TextField` | `CTextField` |
| `FormLayout` | `CFormLayout` |
| `FlexLayout` | `CFlexLayout` |
| `TabSheet` | `CTabSheet` |
| `Tab` | `CTab` |
| `Div` | `CDiv` |
| `Span` | `CSpan` |
| `H3` | `CH3` |
| `Scroller` | `CScroller` |

#### 5. Avoid Lambda Consumers - Use Named Methods
NEVER put complex logic in lambda expressions. ALWAYS delegate to named event handlers:

```java
// ❌ WRONG - Complex lambda (hard to read, hard to override)
buttonAdd.addClickListener(e -> {
    try {
        Check.notNull(entity, "Entity cannot be null");
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error adding entity", ex);
        CNotificationService.showException("Error adding entity", ex);
    }
});

// ✅ CORRECT - Delegate to named method
buttonAdd.addClickListener(e -> on_buttonAdd_clicked());

protected void on_buttonAdd_clicked() {
    try {
        Check.notNull(entity, "Entity cannot be null");
        final CEntity newEntity = createNewEntity();
        service.save(newEntity);
        refreshGrid();
        CNotificationService.showSaveSuccess();
    } catch (final Exception ex) {
        LOGGER.error("Error adding entity", ex);
        CNotificationService.showException("Error adding entity", ex);
    }
}
```

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

### Entity Constants (MANDATORY)
Every entity class MUST define these constants for proper system integration:

```java
public class CActivity extends CProjectItem<CActivity> {
    public static final String DEFAULT_COLOR = "#DC143C";
    public static final String DEFAULT_ICON = "vaadin:tasks";
    public static final String ENTITY_TITLE_PLURAL = "Activities";      // MANDATORY
    public static final String ENTITY_TITLE_SINGULAR = "Activity";       // MANDATORY
    private static final Logger LOGGER = LoggerFactory.getLogger(CActivity.class);
    public static final String VIEW_NAME = "Activities View";
}
```

**Using Entity Titles via CEntityRegistry**:
```java
// Get entity class from title
Class<?> entityClass = CEntityRegistry.getEntityClassByTitle("Activity");
Class<?> entityClass = CEntityRegistry.getEntityClassByTitle("Activities");

// Get title from entity class
String singular = CEntityRegistry.getEntityTitleSingular(CActivity.class);  // "Activity"
String plural = CEntityRegistry.getEntityTitlePlural(CActivity.class);      // "Activities"
```

### Metadata-Driven Development
Use `@AMetaData` annotations for automatic UI generation:

```java
@Column(nullable = false, length = 255)
@Size(max = 255)
@NotBlank(message = "Name is required")
@AMetaData(
    displayName = "Activity Name",    // UI label
    required = true,                   // Required field indicator
    readOnly = false,                  // Editable in forms
    description = "Activity name",     // Tooltip/help text
    hidden = false,                    // Visible in UI
    maxLength = 255,                   // Max input length
    dataProviderBean = "CUserService"  // For ComboBox data source
)
private String name;
```

### Validation and Error Handling (CRITICAL)

#### Use Check Utility for Fail-Fast Validation
```java
// ✅ CORRECT - Use Check utility
Check.notNull(entity, "Entity cannot be null");
Check.notBlank(name, "Name cannot be blank");
Check.notEmpty(list, "List cannot be empty");
Check.isTrue(value > 0, "Value must be positive");
Check.instanceOf(obj, CEntityNamed.class, "Object must be an instance of CEntityNamed");

// ❌ INCORRECT - Manual checks
if (entity == null) {
    throw new IllegalArgumentException("Entity cannot be null");
}
```

#### Developer Errors vs Runtime Errors
**Developer Errors (MUST throw exceptions)**:
- Calling `getName()` on object that doesn't have name property
- Calling `getStatus()` on object that doesn't implement status interface
- Wrong type being passed to method expecting specific type
- **Action**: Use `Check.instanceOf()` to fail fast

**Runtime Errors (CAN handle gracefully)**:
- User input validation
- Network failures
- File not found
- Database connection issues

```java
// ✅ CORRECT - Exposing developer error immediately
private String getEntityName(EntityClass item) {
    Check.notNull(item, "Item cannot be null");
    Check.instanceOf(item, CEntityNamed.class, "Item must be of type CEntityNamed");
    return ((CEntityNamed<?>) item).getName();
}

// ❌ WRONG - Hiding developer error
private String getEntityName(EntityClass item) {
    if (item instanceof CEntityNamed) {
        return ((CEntityNamed<?>) item).getName();
    }
    return "";  // WRONG: Developer should never call this with wrong type
}
```

### Form Binding
- **Use CEnhancedBinder** for form binding instead of vanilla Vaadin Binder

### Database Configuration
- **Development**: Use H2 profile: `mvn spring-boot:run -Dspring.profiles.active=h2`
- **Production**: PostgreSQL (requires manual setup and database server)
- **Schema**: Hibernate auto-creates tables with sample data
- **Sample Data**: Automatically loaded on startup via CSampleDataInitializer

### Multi-User & Concurrency Patterns (CRITICAL for Web Applications)

#### Stateless Service Pattern (MANDATORY)
All services MUST be stateless to support multiple concurrent users. Services are Spring singletons shared across ALL users.

```java
// ✅ CORRECT: Stateless Service
@Service
public class CActivityService extends CEntityOfProjectService<CActivity> {
    // ✅ GOOD: Only dependencies, no user-specific state
    private final IActivityRepository repository;
    private final Clock clock;
    private final ISessionService sessionService;
    
    public CActivityService(IActivityRepository repository, Clock clock, ISessionService sessionService) {
        super(repository, clock, sessionService);
        this.repository = repository;
        this.sessionService = sessionService;
    }
    
    @Transactional(readOnly = true)
    public List<CActivity> getUserActivities() {
        // ✅ GOOD: Get user from session each time
        CUser currentUser = sessionService.getActiveUser()
            .orElseThrow(() -> new IllegalStateException("No active user"));
        return repository.findByUserId(currentUser.getId());
    }
}

// ❌ WRONG: Service with User-Specific State
@Service
public class CBadActivityService {
    // ❌ WRONG: User state stored in service (shared across ALL users!)
    private CUser currentUser;
    private List<CActivity> cachedActivities;
    
    public void setCurrentUser(CUser user) {
        // ❌ WRONG: This will be overwritten by other users' requests
        this.currentUser = user;
    }
    
    public List<CActivity> getActivities() {
        // ❌ WRONG: Returns wrong data when multiple users access simultaneously
        return cachedActivities;
    }
}
```

**Service Field Rules**:
| Field Type | Allowed? | Example | Notes |
|------------|----------|---------|-------|
| Repository dependency | ✅ Yes | `private final IUserRepository repository` | Injected via constructor |
| Clock dependency | ✅ Yes | `private final Clock clock` | Injected via constructor |
| Session service | ✅ Yes | `private final ISessionService sessionService` | Injected via constructor |
| Logger | ✅ Yes | `private static final Logger LOGGER` | Thread-safe, immutable |
| Constants | ✅ Yes | `private static final String MENU_TITLE` | Immutable |
| User context | ❌ No | `private CUser currentUser` | **WRONG! Shared across users** |
| User data cache | ❌ No | `private List<CEntity> userCache` | **WRONG! Shared across users** |
| Mutable static collections | ❌ No | `private static Map<Long, CUser> cache` | **WRONG! Not thread-safe** |

#### Session State Management
```java
// ✅ DO: Use VaadinSession for User-Specific State
@Service
public class CPreferenceService {
    private static final String PREFERENCE_KEY = "userPreference";
    
    public void savePreference(String preference) {
        // ✅ GOOD: Each user has their own VaadinSession
        VaadinSession.getCurrent().setAttribute(PREFERENCE_KEY, preference);
    }
    
    public String getPreference() {
        // ✅ GOOD: Retrieved from user's own session
        return (String) VaadinSession.getCurrent().getAttribute(PREFERENCE_KEY);
    }
}

// ✅ DO: Retrieve Context from Session Per-Request
@Service
public class CActivityService extends CAbstractService<CActivity> {
    
    @Transactional(readOnly = true)
    public List<CActivity> findAll() {
        // ✅ GOOD: Get company from session each time method is called
        CCompany currentCompany = getCurrentCompany();
        return repository.findByCompany_Id(currentCompany.getId());
    }
    
    private CCompany getCurrentCompany() {
        Check.notNull(sessionService, "Session service required");
        CCompany company = sessionService.getCurrentCompany();
        Check.notNull(company, "No active company");
        return company;
    }
}
```

**Multi-User Safety Checklist**:
1. **No instance fields storing user data** ✓
2. **No instance fields storing collections of user data** ✓
3. **No static mutable collections** ✓
4. **All user context retrieved from sessionService** ✓
5. **No caching of user-specific data in service** ✓

### Repository Query Patterns (MANDATORY)

#### Repository Query Ordering - All Queries MUST Include ORDER BY
All repository query methods returning List or Page MUST include explicit ORDER BY clause:

```java
// ✅ CORRECT - Explicit ORDER BY
@Query("SELECT e FROM #{#entityName} e WHERE e.project = :project ORDER BY e.name ASC")
List<CActivity> findByProject(@Param("project") CProject project);

@Query("SELECT e FROM #{#entityName} e WHERE e.company = :company ORDER BY e.id DESC")
List<CEntity> findByCompany(@Param("company") CCompany company);

// ❌ INCORRECT - Missing ORDER BY
@Query("SELECT e FROM #{#entityName} e WHERE e.project = :project")
List<CActivity> findByProject(@Param("project") CProject project);
```

**Ordering Rules**:
- **Named entities** (extending CEntityNamed): Use `ORDER BY e.name ASC`
- **Regular entities** (extending CEntityDB): Use `ORDER BY e.id DESC`
- **Sprintable items**: Use `ORDER BY e.sprintOrder ASC NULLS LAST, e.id DESC`

#### Child Entity Repository Standards (Master-Detail Pattern)

Use consistent method naming for child entity repositories:

```java
public interface ISprintItemRepository extends IAbstractRepository<CSprintItem> {

    /** Find all children by master entity. */
    @Query("SELECT e FROM #{#entityName} e WHERE e.sprint = :master ORDER BY e.itemOrder ASC")
    List<CSprintItem> findByMaster(@Param("master") CSprint master);

    /** Find all children by master ID. */
    @Query("SELECT e FROM #{#entityName} e WHERE e.sprint.id = :masterId ORDER BY e.itemOrder ASC")
    List<CSprintItem> findByMasterId(@Param("masterId") Long masterId);

    /** Count children by master. */
    @Query("SELECT COUNT(e) FROM #{#entityName} e WHERE e.sprint = :master")
    Long countByMaster(@Param("master") CSprint master);

    /** Get next item order for new items. */
    @Query("SELECT COALESCE(MAX(e.itemOrder), 0) + 1 FROM #{#entityName} e WHERE e.sprint = :master")
    Integer getNextItemOrder(@Param("master") CSprint master);
}
```

**Standard Method Names**:
| Method Pattern | Purpose | Example |
|----------------|---------|---------|
| `findByMaster(M master)` | Find all children by master entity | `findByMaster(CDetailSection master)` |
| `findByMasterId(Long id)` | Find all children by master ID | `findByMasterId(Long sprintId)` |
| `countByMaster(M master)` | Count children by master entity | `countByMaster(CDetailSection master)` |
| `getNextItemOrder(M master)` | Get next order number | `getNextItemOrder(CDetailSection master)` |

**JPQL Standards**:
- Always use `e` as entity alias: `SELECT e FROM #{#entityName} e`
- Use generic parameter names like `master` instead of entity-specific names
- Always include explicit `e.` prefix for JOINed fields

### Grid and Component Patterns

#### Grid Refresh Pattern
Use `IGridRefreshListener<T>` for Update-Then-Notify pattern:

```java
// Pattern: Action → Update self → refreshGrid() → notifyRefreshListeners() → Listeners refresh
public class CComponentListEntityBase<T extends CEntityDB<T>> 
        implements IGridRefreshListener<T> {
    
    private final List<IGridRefreshListener<T>> refreshListeners = new ArrayList<>();
    
    public void addRefreshListener(IGridRefreshListener<T> listener) {
        refreshListeners.add(listener);
    }
    
    protected void notifyRefreshListeners(T entity) {
        for (IGridRefreshListener<T> listener : refreshListeners) {
            listener.onGridRefresh(entity);
        }
    }
    
    @Override
    public void onGridRefresh(T entity) {
        refreshGrid();  // Refresh this component's grid
    }
}
```

#### Grid Reordering
Always use service `moveItemUp()`/`moveItemDown()` methods for reordering:

```java
// ✅ CORRECT - Use service methods
protected void on_buttonMoveUp_clicked() {
    try {
        T selectedItem = grid.asSingleSelect().getValue();
        Check.notNull(selectedItem, "No item selected");
        service.moveItemUp(selectedItem);
        refreshGrid();
    } catch (Exception ex) {
        LOGGER.error("Error moving item up", ex);
        CNotificationService.showException("Error moving item up", ex);
    }
}

// ❌ WRONG - Manual order field updates
protected void on_buttonMoveUp_clicked() {
    T item = grid.asSingleSelect().getValue();
    item.setOrder(item.getOrder() - 1);  // WRONG: Don't manually update order
    service.save(item);
}
```

### Sample Data Initialization Pattern (MANDATORY)

All InitializerService classes MUST implement the `initializeSample()` pattern:

```java
public class CEntityTypeInitializerService extends CInitializerServiceBase {
    
    private static final Class<?> clazz = CEntityType.class;
    
    // Sample data initialization method (REQUIRED)
    public static void initializeSample(final CProject project, 
            final boolean minimal) throws Exception {
        final String[][] nameAndDescriptions = {
            { "Type 1", "Description for type 1" },
            { "Type 2", "Description for type 2" },
            { "Type 3", "Description for type 3" }
        };
        initializeProjectEntity(nameAndDescriptions,
            (CEntityOfProjectService<?>) CSpringContext.getBean(
                CEntityRegistry.getServiceClassForEntity(clazz)), 
            project, minimal, null);
    }
}
```

**Implementation Rules**:
1. Method MUST be named `initializeSample`
2. Parameters: `(final CProject project, final boolean minimal)`
3. Data Format: Use `String[][]` for name and description pairs
4. Base Method: Call `initializeProjectEntity()` from `CInitializerServiceBase`
5. Respect the `minimal` parameter to create reduced datasets

### Database Configuration
- **Development**: Use H2 profile: `mvn spring-boot:run -Dspring.profiles.active=h2`
- **Production**: PostgreSQL (requires manual setup and database server)
- **Schema**: Hibernate auto-creates tables with sample data
- **Sample Data**: Automatically loaded on startup via CSampleDataInitializer

### Testing Strategy
- **Playwright Tests**: Located in `src/test/java/automated_tests/`
- **UI Automation**: Browser-based testing with screenshot capture
- **Manual Tests**: Documented scenarios for critical workflows

### Playwright Test Configuration Options
The test script supports multiple configuration options via environment variables:

**Test Scenarios:**
- `menu` - Fast menu navigation test (~37 seconds)
- `comprehensive` - Complete view & CRUD testing (2-5 minutes)
- `all-views` - Navigate all application views
- `crud` - CRUD operations testing

**Configuration Environment Variables:**
- `PLAYWRIGHT_HEADLESS=true|false` - Browser visibility (default: false)
- `PLAYWRIGHT_SHOW_CONSOLE=true|false` - Console output (default: true)
- `PLAYWRIGHT_SKIP_SCREENSHOTS=true|false` - Screenshot capture (default: false)
- `PLAYWRIGHT_SLOWMO=0-5000` - Action delay in ms for debugging (default: 0)
- `PLAYWRIGHT_VIEWPORT_WIDTH=800-3840` - Viewport width (default: 1920)
- `PLAYWRIGHT_VIEWPORT_HEIGHT=600-2160` - Viewport height (default: 1080)
- `INTERACTIVE_MODE=true|false` - Show configuration menu (default: false)

**Common Use Cases:**
```bash
# Interactive configuration (recommended for first-time)
INTERACTIVE_MODE=true ./run-playwright-tests.sh menu

# Fast CI/CD mode (headless, no screenshots, no console)
PLAYWRIGHT_HEADLESS=true PLAYWRIGHT_SKIP_SCREENSHOTS=true PLAYWRIGHT_SHOW_CONSOLE=false ./run-playwright-tests.sh comprehensive

# Debug mode (visible browser with slow motion)
PLAYWRIGHT_SLOWMO=500 ./run-playwright-tests.sh menu

# Mobile testing (iPhone 12)
PLAYWRIGHT_VIEWPORT_WIDTH=390 PLAYWRIGHT_VIEWPORT_HEIGHT=844 ./run-playwright-tests.sh menu
```

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
**AI Assistant should automatically run these before committing:**
```bash
# 1. Setup Java environment (ALWAYS run first)
source ./bin/setup-java-env.sh

# 2. Apply code formatting via IDE
# Use eclipse-formatter.xml in your IDE (IntelliJ IDEA, Eclipse, VS Code)
# Or configure Spotless Maven plugin for automated formatting

# 3. Full build verification (NEVER CANCEL: 12-15 seconds)
mvn clean compile

# 4. Check for forbidden notification patterns (should return empty):
grep -r "Notification\.show\|new.*Dialog.*\.open()" src/main/java --include="*.java" | grep -v "CNotificationService.java"

# 5. Optional: UI validation after UI changes
./run-playwright-tests.sh menu  # Quick validation (37-40 seconds)

# 6. Commit changes automatically with descriptive message
git add [changed-files]
git commit -m "feat: descriptive message"

# For complete testing guidelines, workflows, and patterns:
# See docs/development/copilot-guidelines.md
```

**Workflow Summary:**
1. Make code changes
2. Apply code formatting via IDE (eclipse-formatter.xml)
3. Run `mvn clean compile` to verify build
4. Run appropriate tests (Playwright for UI changes)
5. Commit with conventional commit message
6. Provide summary of changes

### Code Formatting Standards

#### Import Organization (MANDATORY)
**ALWAYS use import statements instead of full class names:**

```java
// ✅ CORRECT
import tech.derbent.app.activities.domain.CActivity;
import tech.derbent.app.projects.domain.CProject;

public class CActivityService {
    public CActivity createActivity(String name, CProject project) {
        CActivity activity = new CActivity(name, project);
        return save(activity);
    }
}

// ❌ INCORRECT
public class CActivityService {
    public tech.derbent.app.activities.domain.CActivity createActivity(
            String name, tech.derbent.app.projects.domain.CProject project) {
        tech.derbent.app.activities.domain.CActivity activity = 
            new tech.derbent.app.activities.domain.CActivity(name, project);
        return save(activity);
    }
}
```

**Benefits**: Improved readability, easier maintenance, better IDE support, reduced line length

**Rule**: Full package paths should only appear in import statements at the top of the file.

#### Cast Expressions MUST Use Short Names (CRITICAL)
**NEVER use full package paths in cast expressions. ALWAYS use short class names with proper imports:**

```java
// ✅ CORRECT - Short class name with import
import tech.derbent.app.meetings.domain.CMeeting;

meetingService.save((CMeeting) item);
activityService.save((CActivity) item);

// ❌ INCORRECT - Full package path in cast
meetingService.save((tech.derbent.app.meetings.domain.CMeeting) item);
activityService.save((tech.derbent.app.activities.domain.CActivity) item);
```

**This applies to:**
- Type casts: `(CMeeting) item`
- Variable declarations: `CMeeting meeting = ...`
- Method parameters: `public void save(CMeeting meeting)`
- Return types: `public CMeeting getMeeting()`
- Generic type parameters: `List<CMeeting> meetings`

**Rule**: Add the import statement at the top of the file and use the short class name everywhere in the code.

#### Spotless Configuration Standards (if configured)
The project uses Eclipse formatter configuration in `eclipse-formatter.xml`. 

To enable automated formatting with Spotless Maven plugin, add to pom.xml:
```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <eclipse>
                <file>eclipse-formatter.xml</file>
            </eclipse>
        </java>
    </configuration>
</plugin>
```

Once configured, use:
```bash
mvn spotless:apply  # Apply formatting
mvn spotless:check  # Check formatting
```

Until Spotless is configured, use your IDE's formatter with eclipse-formatter.xml.

**Code Formatting Standards (from eclipse-formatter.xml)**:
- **Indentation**: 4 spaces (no tabs)
- **Line length**: 140 characters (soft limit)
- **Braces**: Always use braces, even for single-line blocks
- **Import organization**: Remove unused imports, organize alphabetically
- **Final keyword**: Use `final` for method parameters and local variables

```java
// ✅ CORRECT
public void processActivity(final CActivity activity) {
    final String name = activity.getName();
    if (name != null) {
        doSomething(name);
    }
}

// ❌ INCORRECT - No final, no braces
public void processActivity(CActivity activity) {
    String name = activity.getName();
    if (name != null)
        doSomething(name);
}
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
