# Getting Started with Derbent Development

## Prerequisites

Before you begin, ensure you have the following installed:

- ☕ **Java 21+** (OpenJDK or Oracle JDK)
- 📦 **Maven 3.9+**
- 🐘 **PostgreSQL 12+** (optional, H2 available for development)
- 🔧 **IDE**: IntelliJ IDEA, Eclipse, or VS Code with Java extensions
- 🤖 **GitHub Copilot** (recommended for AI-assisted development)

## Quick Setup

### 1. Clone the Repository

```bash
git clone https://github.com/yalovali/derbent.git
cd derbent
```

### 2. Verify Prerequisites

```bash
# Check Java version (should be 21+)
java -version

# Check Maven version (should be 3.9+)
mvn -version
```

### 3. Build the Project

```bash
# Clean and compile (takes ~12-15 seconds)
# NEVER CANCEL: This is expected to take time
mvn clean compile

# Apply code formatting
mvn spotless:apply
```

### 4. Run with H2 Database (Development Mode)

```bash
# Start application with in-memory H2 database
# NEVER CANCEL: Takes 12-15 seconds to start
mvn spring-boot:run -Dspring.profiles.active=h2

# Alternative: Use Maven profile
mvn spring-boot:run -Ph2-local-development
```

### 5. Access the Application

Open your browser to: **http://localhost:8080**

The application will:
- Create sample data automatically on first run
- Provide default login credentials
- Show the login page with company selection

## Development Workflow

### Daily Development Cycle

```bash
# 1. Pull latest changes
git pull origin main

# 2. Apply formatting before coding
mvn spotless:apply

# 3. Make your changes
# ... edit code ...

# 4. Check formatting
mvn spotless:check

# 5. Compile and test
mvn clean compile
./run-playwright-tests.sh mock  # Optional: Run UI tests

# 6. Commit your changes
git add .
git commit -m "Your descriptive message"
git push
```

### Running Tests

```bash
# Run all Playwright UI tests (takes ~37-40 seconds)
# NEVER CANCEL: This is expected time
./run-playwright-tests.sh mock

# Run comprehensive tests (takes 2-5 minutes)
./run-playwright-tests.sh comprehensive

# Run specific test categories
./run-playwright-tests.sh status-types
./run-playwright-tests.sh main-views
./run-playwright-tests.sh admin-views
./run-playwright-tests.sh kanban-views

# Clean test artifacts
./run-playwright-tests.sh clean
```

## IDE Setup

### IntelliJ IDEA (Recommended)

1. **Import Project**
   - File → Open → Select `pom.xml`
   - Import as Maven project

2. **Configure Java SDK**
   - File → Project Structure → Project
   - Set SDK to Java 21
   - Set language level to 21

3. **Install Plugins**
   - Vaadin (for UI development)
   - GitHub Copilot (for AI assistance)
   - Lombok (if not already installed)

4. **Configure Code Style**
   - File → Settings → Editor → Code Style → Java
   - Import: `eclipse-formatter.xml` from project root

5. **Enable Annotation Processing**
   - File → Settings → Build, Execution, Deployment → Compiler → Annotation Processors
   - Check "Enable annotation processing"

### VS Code

1. **Install Extensions**
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Vaadin
   - GitHub Copilot

2. **Configure Java Home**
   - Open Settings (Ctrl+,)
   - Search for "java.home"
   - Set to Java 21 installation path

3. **Import Project**
   - File → Open Folder → Select project root
   - VS Code will auto-detect Maven project

### Eclipse

1. **Import Maven Project**
   - File → Import → Maven → Existing Maven Projects
   - Select project root directory

2. **Configure Java Version**
   - Right-click project → Properties → Java Build Path
   - Set JRE to Java 21

3. **Apply Code Formatter**
   - Window → Preferences → Java → Code Style → Formatter
   - Import: `eclipse-formatter.xml`

## Project Structure Overview

```
derbent/
├── src/main/java/tech/derbent/
│   ├── api/                    # Core framework
│   │   ├── domains/           # Base entity classes
│   │   ├── services/          # Base service classes
│   │   ├── views/             # Base view classes
│   │   └── utils/             # Utility classes
│   ├── activities/            # Activity management
│   │   ├── domain/           # CActivity entity
│   │   ├── service/          # CActivityService
│   │   └── view/             # Activity UI components
│   ├── users/                 # User management
│   ├── projects/              # Project management
│   ├── meetings/              # Meeting management
│   └── risks/                 # Risk management
├── src/main/resources/
│   ├── application.properties # Main configuration
│   └── application-*.properties # Profile-specific configs
├── src/test/java/             # Test files
├── docs/                      # Documentation
│   ├── architecture/          # Design patterns
│   └── development/           # Developer guides
├── pom.xml                    # Maven configuration
└── run-playwright-tests.sh    # Test runner script
```

## First Steps After Setup

### 1. Explore Sample Data

When you first run the application:

```bash
mvn spring-boot:run -Dspring.profiles.active=h2
```

The application creates:
- Multiple companies
- Sample users for each company
- Projects with activities
- Statuses, types, and priorities

### 2. Login with Default Credentials

1. Open http://localhost:8080
2. Select a company from dropdown
3. Login with username: `admin`, password: `test123`

### 3. Explore Main Features

Navigate through:
- **Dashboard**: Overview of projects and activities
- **Activities**: Task management and tracking
- **Projects**: Project organization
- **Users**: User management
- **Kanban Board**: Visual task management
- **Administration**: System settings

### 4. Review Documentation

Read key documentation:
- [Entity Inheritance Patterns](../architecture/entity-inheritance-patterns.md)
- [Service Layer Patterns](../architecture/service-layer-patterns.md)
- [View Layer Patterns](../architecture/view-layer-patterns.md)
- [Coding Standards](../architecture/coding-standards.md)
- [GitHub Copilot Guidelines](copilot-guidelines.md)

## Common Development Tasks

### Adding a New Entity

1. **Create Domain Class** (e.g., `CTask.java`)
```java
@Entity
@Table(name = "ctask")
public class CTask extends CProjectItem<CTask> {
    // Fields with @AMetaData annotations
}
```

2. **Create Repository Interface**
```java
public interface ITaskRepository extends IEntityOfProjectRepository<CTask> {
    // Custom query methods
}
```

3. **Create Service Class**
```java
@Service
@PreAuthorize("isAuthenticated()")
public class CTaskService extends CEntityOfProjectService<CTask> {
    // Business logic methods
}
```

4. **Create View/Page**
```java
@Route("tasks")
@RolesAllowed("USER")
public class CTaskView extends CAbstractPage {
    // UI implementation
}
```

### Adding a New Feature

1. **Plan the feature**
   - Identify entities needed
   - Define relationships
   - Plan UI workflow

2. **Create entities** (domain layer)

3. **Implement services** (business layer)

4. **Build UI** (view layer)

5. **Add tests**
   - Unit tests for services
   - Playwright tests for UI

6. **Update documentation**

### Debugging Tips

#### Enable SQL Logging

Add to `application-h2.properties`:
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

#### Use Database Query Debugging

The project includes SQL debugging tools:
```bash
# See DATABASE_QUERY_DEBUGGING.md for details
```

#### View Hibernate Statistics

```properties
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=DEBUG
```

## Troubleshooting

### Build Failures

**Problem**: Maven compile fails

**Solutions**:
```bash
# Clean build
mvn clean

# Update dependencies
mvn clean install -U

# Skip tests
mvn clean compile -DskipTests
```

### Java Version Issues

**Problem**: "invalid target release: 21"

**Solution**: Ensure Java 21 is active
```bash
# Check current Java version
java -version

# Set JAVA_HOME (Linux/Mac)
export JAVA_HOME=/path/to/java21

# Set JAVA_HOME (Windows)
set JAVA_HOME=C:\path\to\java21
```

### Application Won't Start

**Problem**: Application fails to start

**Check**:
1. Port 8080 is not in use
2. Database connection (if using PostgreSQL)
3. Check logs in console output

**Solutions**:
```bash
# Use different port
mvn spring-boot:run -Dserver.port=8081

# Use H2 instead of PostgreSQL
mvn spring-boot:run -Dspring.profiles.active=h2
```

### Playwright Tests Fail

**Problem**: UI tests fail

**Solutions**:
```bash
# Ensure application is not running
# Tests start their own instance

# Clean test artifacts
./run-playwright-tests.sh clean

# Re-run tests
./run-playwright-tests.sh mock
```

## Best Practices

### 1. Follow Naming Conventions

All custom classes use C-prefix:
- ✅ `CActivity`, `CUser`, `CProject`
- ❌ `Activity`, `User`, `Project`

### 2. Use Code Formatting

Always format before committing:
```bash
mvn spotless:apply
```

### 3. Write Tests

Add tests for new features:
- Unit tests for services
- Integration tests for workflows
- Playwright tests for UI

### 4. Update Documentation

Document significant changes:
- Update README.md if adding major features
- Add to relevant architecture docs
- Update this guide if changing setup process

### 5. Use Git Properly

```bash
# Create feature branch
git checkout -b feature/my-feature

# Commit with descriptive messages
git commit -m "Add task management feature"

# Push for review
git push origin feature/my-feature
```

## Next Steps

Now that you're set up:

1. **Explore the Codebase**
   - Browse `src/main/java/tech/derbent/activities/` as an example module
   - Study entity, service, and view patterns

2. **Try Making Changes**
   - Add a field to an existing entity
   - Create a new service method
   - Modify a view layout

3. **Run Tests**
   - Verify your changes with tests
   - Add new tests for your features

4. **Learn Advanced Topics**
   - [Entity Inheritance Patterns](../architecture/entity-inheritance-patterns.md)
   - [Service Layer Patterns](../architecture/service-layer-patterns.md)
   - [Copilot Guidelines](copilot-guidelines.md)

5. **Join the Community**
   - Report issues on GitHub
   - Submit pull requests
   - Share feedback

## Resources

### Documentation
- [Architecture Documentation](../architecture/)
- [Testing Guide](../testing/PLAYWRIGHT_TEST_SUMMARY.md)
- [Database Debugging](../DATABASE_QUERY_DEBUGGING.md)

### External Resources
- [Vaadin Documentation](https://vaadin.com/docs)
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/)
- [JPA/Hibernate Guide](https://hibernate.org/orm/documentation/)

### Getting Help
- GitHub Issues: Report bugs or request features
- Code Examples: Study existing modules like `activities`, `users`, `projects`
- Stack Overflow: Tag with `vaadin`, `spring-boot`, or `jpa`

## Summary

You now have:
- ✅ Development environment set up
- ✅ Application running locally
- ✅ Understanding of project structure
- ✅ Knowledge of development workflow
- ✅ Resources for learning more

Start exploring and building! The codebase is designed to be intuitive and Copilot-friendly.

**Happy coding! 🚀**
