# Derbent - Collaborative Project Management Application 

**ALWAYS follow these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the information here.**

## Working Effectively

### Prerequisites and Setup
- Java 17 (confirmed working: OpenJDK 17.0.16)
- Maven 3.9+ (confirmed working: Apache Maven 3.9.11)
- **Database Options:**
  - **Production:** PostgreSQL on localhost:5432 (default in application.properties)
  - **Development/Testing:** H2 in-memory database (use profile or override)

### Essential Build Commands
**NEVER CANCEL builds or long-running commands. Set explicit timeouts of 60+ minutes for builds and 30+ minutes for tests.**

```bash
# Clean build directory - takes ~21 seconds
mvn clean

# Compile application - takes ~4 minutes. NEVER CANCEL: Set timeout to 10+ minutes
mvn compile

# Full build with tests - takes ~51 seconds. NEVER CANCEL: Set timeout to 10+ minutes  
mvn clean install

# Code formatting check - takes ~28 seconds
mvn spotless:check

# Apply code formatting - takes ~6 seconds
mvn spotless:apply
```

### Running the Application

**For development with H2 database (no PostgreSQL setup required):**
```bash
# REQUIRED: Temporarily edit src/main/resources/application.properties
# Comment out PostgreSQL lines (lines 8-11) and uncomment H2 lines (lines 13-16)
# Then run:
mvn spring-boot:run

# Alternative: Use test profile which has H2 configured
mvn spring-boot:run -Dspring.profiles.active=test
```

**For production with PostgreSQL:**
```bash
# Requires PostgreSQL running on localhost:5432 with database 'derbent'
mvn spring-boot:run
```

**Application startup timing:**
- Application starts in ~13-15 seconds with H2
- Accessible at http://localhost:8080
- Sample data is automatically loaded on first startup
- **Default test users:** admin/test123, various project managers and team members

### Testing Commands

**NEVER CANCEL test runs. Tests may take 15+ minutes. Use explicit timeouts of 30+ minutes.**

```bash
# Run all tests - takes ~3 minutes 20 seconds. NEVER CANCEL: Set timeout to 30+ minutes
mvn test -Dtest="*Test" --batch-mode

# Run Playwright UI automation tests (mock) - takes ~35 seconds
./run-playwright-tests.sh mock

# Run comprehensive Playwright tests for all views
./run-playwright-tests.sh comprehensive

# Run specific test categories
./run-playwright-tests.sh login        # Login/logout tests
./run-playwright-tests.sh crud         # CRUD operation tests  
./run-playwright-tests.sh navigation   # Navigation tests
./run-playwright-tests.sh accessibility # Accessibility tests
```

**Test organization structure:**
- `src/test/java/unit_tests/` - Unit and integration tests
- `src/test/java/ui_tests/` - Vaadin UI component tests
- `src/test/java/automated_tests/` - Playwright browser automation

### Validation Requirements

**ALWAYS run these validation steps before completing any changes:**

1. **Code formatting:** `mvn spotless:apply`
2. **Build validation:** `mvn clean install` (takes ~51 seconds, set timeout to 10+ minutes)
3. **Application startup test:** Start app with H2 and verify http://localhost:8080 responds
4. **UI testing:** Run `./run-playwright-tests.sh mock` for screenshot validation

## Manual Testing Scenarios

**ALWAYS test these user scenarios after making changes:**

1. **Login Flow:** Use admin/test123 to login and access dashboard
2. **Navigation:** Test main menu items (Projects, Activities, Meetings, Users)
3. **CRUD Operations:** Create, edit, and delete a test project or activity
4. **Form Validation:** Test required field validation on forms
5. **Grid Interactions:** Test sorting, filtering, and pagination in data grids

## Common Development Tasks

### Database Configuration
- **H2 console:** Available at http://localhost:8080/h2-console when using H2 profile
- **H2 connection:** `jdbc:h2:mem:testdb`, username: `sa`, password: (empty)
- **PostgreSQL:** Default connection assumes `postgres:derbent@localhost:5432/derbent`

### Technology Stack Details
- **Java 17** with Spring Boot 3.5
- **Vaadin Flow 24.8** for UI framework
- **Hibernate/JPA** for data persistence
- **H2/PostgreSQL** database support
- **Playwright 1.40.0** for UI test automation
- **Maven** build tool with Spotless for code formatting

### Key Project Structure
```
src/main/java/tech/derbent/
├── abstracts/          # Base classes and annotations
├── activities/         # Activity management
├── administration/     # Company settings
├── companies/          # Company management  
├── meetings/           # Meeting management
├── projects/           # Project management
├── setup/             # System settings
├── users/             # User management
└── Application.java   # Main Spring Boot application

src/test/
├── unit_tests/        # Unit and integration tests
├── ui_tests/          # Vaadin UI component tests
└── automated_tests/   # Playwright browser automation
```

### Code Quality Standards
- Follow coding standards in `docs/architecture/coding-standards.md`
- Use `mvn spotless:apply` for consistent formatting
- All new code must include appropriate tests
- Follow MVC pattern separation (Model, View, Controller)
- Use annotation-based configuration where possible

### Troubleshooting Common Issues

**Build Issues:**
- "Connection refused" database errors: Use H2 database override for development (see command above)
- "No tests executed": Ensure test classes follow naming convention (*Test.java)
- Spotless formatting failures: Run `mvn spotless:apply` to fix

**Database Issues:**
- PostgreSQL connection errors: Application.properties defaults to PostgreSQL. Use H2 override for development
- "Unable to determine Dialect": Database connection issue. Use H2 override or ensure PostgreSQL is running
- Sample data not loading: Normal on subsequent starts when data exists

**Application Issues:**
- Port 8080 already in use: Stop other instances or change port
- Sample data not loading: Check logs for initialization errors
- UI components not rendering: Clear browser cache and restart application

### Performance Notes
- **Build times:** Full build ~51 seconds, compile ~4 minutes
- **Test execution:** All tests ~3:20 minutes, Playwright tests ~35 seconds
- **Application startup:** ~13-15 seconds with sample data loading
- **Code formatting:** Check ~28 seconds, apply ~6 seconds

**Memory recommendations:**
- Minimum 2GB RAM for development
- 4GB+ recommended for running full test suite
- Playwright tests generate 150+ screenshots (~50MB)

## CI/CD Integration
- GitHub Actions workflow available at `.github/workflows/ai-code-review.yml`
- Maven Surefire configured for test execution in different categories
- Spotless integrated for automatic code formatting verification
- Use `mvn clean install` for full CI validation pipeline

---

> **Remember:** NEVER CANCEL long-running builds or tests. Always use appropriate timeouts (60+ minutes for builds, 30+ minutes for tests) and wait for completion.
