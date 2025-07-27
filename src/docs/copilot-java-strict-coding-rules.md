# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

This is a project for maintaining work flows, tasks, managerial targets, procedures, methodologies of a product or project within a company.

For solutions, usage coverage and UI design, inspire from Atlassian Jira product or Projeqtor from page https://projeqtor.org/en

Check src/docs/project_design_description.md file for project scope, update this document if necessary or given task enlarges this scope.

---

## 1. MVC Architecture Principles

- **Domain:**  
  Represents business logic and data. Use POJOs, Entities, and Service classes.
- **View:**  
  UI components using Vaadin's framework. Avoid embedding business logic in views.
- **Controller/Service:**  
  Handles user input, updates models, and refreshes views. Typically, Vaadin views act as controllers.

**Example directory structure:**
```
src/main/java/cuser
├── domain/
├── view/
├── controller/
```

---

## 2. Development Environment Setup

- Use Java 17+ with Spring Boot 3+
- Configure PostgreSQL as the primary database (avoid H2 in production)
- Use Maven with the provided pom.xml configuration
- Follow the eclipse-formatter.xml code formatting rules
- Enable Spring Boot DevTools for hot reloading during development

---

## 3. Commenting Standards

- **Every class:** Document its role in MVC.
- **Every method:** Describe parameters, return value, and side effects.
- **Complex logic:** Inline comments explaining tricky parts.
- **Copilot suggestions:** Review and expand comments for clarity.

---

## 4. Security and Validation Standards

- Always validate all user inputs in controllers and services
- Use Spring Security for authentication and authorization
- Implement proper RBAC (Role-Based Access Control)
- Never store sensitive data in plain text
- Use parameterized queries to prevent SQL injection
- Implement CSRF protection for all forms

---

## 5. Testing Requirements

- Write unit tests for all business logic and service methods
- Use TestContainers for integration testing with PostgreSQL
- Maintain test coverage above 80% for critical business logic
- Test all validation scenarios and edge cases
- Mock external dependencies appropriately

---

## 6. Java Build and Quality Checks

- Compile using `mvn clean install` or your build tool.
- Run static analysis: CheckStyle, PMD, or SonarLint.
- Run unit and integration tests. Ensure coverage for all controller logic.
- Confirm no business logic is present in View classes.
- Check for unused imports and variables.
- Validate all forms of input in controllers.

---

## 7. Pull Request Checklist

- Code follows MVC separation principles
- Methods and classes are properly commented with JavaDoc
- All Copilot-generated code has been reviewed and tested
- All Java quality checks passed: compilation, static analysis, testing
- No hardcoded values in controllers or views
- UI logic (Vaadin) does not leak into model/service layers
- Database changes include proper migrations and sample data

---

## 8. Copilot Review Protocol

After accepting Copilot suggestions, manually review for:
- Correct MVC placement and architecture compliance
- Security considerations (input validation, access control)
- Performance and scalability implications
- Proper exception handling and error messages
- Code quality and maintainability standards

---

## 9. Coding Styles & Best Practices

**General Principles:**
- Use `final` keyword wherever possible (variables, parameters, methods, classes)
- Favor abstraction: if two or more features are similar, create an abstract base class with abstract fields and methods
- Always start class names with a capital "C" (e.g., `CUser`, `CSettings`). Do not use standard Java class naming for domain classes
- Check for lazy loading issues using best practices (e.g., `@Transactional`, `fetch = FetchType.LAZY`). Add comments explaining lazy loading risks or solutions
- Always check for `null` values and possible `NullPointerException` in every function. If necessary, also check for empty strings
- Always prefer using base classes to avoid code duplication
- Every important function must start with a logger statement detailing function name and parameter values (do not log at function end)
- Always catch exceptions in Vaadin UI and handle them gracefully
- Use Vaadin UI components to display exceptions to users where possible
- Always use `MetaData.java` and `CEntityFormBuilder.java` to bind and generate forms for entities when possible
- Extend base classes or add new ones to increase modularity and maintainability
- Always use appropriate icons for views and UI components
- For delete operations, always require explicit user approval (e.g., confirmation dialog)
- All selective ComboBoxes must be **selection only** (user must not be able to type arbitrary text)
- Keep project documentation updated in the `docs` folder; create separate documents for each design concept. If implementing complex solutions (e.g., Spring technologies), create a step-by-step solution document
- Use the `abstracts/view` package for project-wide UI components (e.g., `CButton`, `CGrid`). Always use these superclasses and extend them as needed; add new UI classes here
- Whenever a button action is not applicable (e.g., user not selected but project add requested), do not silently return. Show a user warning explaining the situation. Use new dialog classes:
  - `CWarningDialog`
  - `CInformationDialog`
  - `CExceptionDialog`
  - These dialogs must be simple, visually appealing, and inherit from a common superclass (e.g., `CBaseDialog`)
- Never add loggers at the end of functions. Always log at the start with full detail about the function and parameters
- All code must follow Java naming conventions for variables and methods, except for class names which must start with "C"

**CSS Guidelines:**
- Update CSS names from class name, also update CSS file accordingly
- Always use very simple CSS. Don't use JavaScript in Java or CSS
- You can use CSS built-in simple functions
- To style a layout use syntax like:
  ```css
  #vaadinLoginOverlayWrapper::part(overlay) {
      background-image: url('./images/background1.png');
  }
  ```
- Check the pattern of CPanelActivityDescription and its super classes. Use this pattern to group each entity's fields according to related business topic. Don't leave any field out. Create necessary classes, base classes with same naming convention for every entity and group fields in the view. Only open the first one
- Always check reference projects and do this task for a better structure, view and requirements of successful resource management, task tracking, project management, budget planning and better UI experience
- Always if necessary create new classes to support the new requirements with always less code and nice super classes for reusability
- Prepare a detailed requirements document that includes this task description, main features, user roles, and any specific technologies or integrations that should be considered

**Database Rules:**
- Password is always test123 for all users with hash code '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu'
- Every entity should have an example in data.sql for per project, per company per user per activity etc...
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change
- Keep spring.jpa.defer-datasource-initialization=true
- Don't use memory database H2 in production
- Always delete tables at top of data.sql before you insert values into it. Check them before they exist
- Delete constraints etc. if there is change in the DB structure
- Always reset sequences in the format and escape characters below with a conditional to check if they exist first:
  ```sql
  DO '
  BEGIN
      IF EXISTS (
          SELECT 1
          FROM pg_class c
          JOIN pg_namespace n ON n.oid = c.relnamespace
          WHERE c.relkind = ''S'' AND c.relname = ''ctask_task_id_seq''
      ) THEN
          EXECUTE ''SELECT setval(''''ctask_task_id_seq'''', 1, false)'';
      END IF;
  END;
  ';
  ```

**Documentation & Modularity:**
- Update the `docs` folder for every significant change:
  - Add new documentation for each project design concept (one file per concept)
  - For complex or Spring-based solutions, create a step-by-step solution file
- All new UI components, dialogs, and base classes must be documented
- Maintain consistency with existing documentation patterns

---

## 10. Strict Prohibitions

- **Never use standard Vaadin `Button` directly!**  
  Immediately refactor to use `CButton` (or its improved/extended version).  
  If `CButton` can be improved, extend it so all buttons share consistent behavior and appearance
- Never bypass base classes for UI components. Always use or extend base classes from `abstracts/view`
- Never silently fail on user actions. Always notify the user with an appropriate dialog
- Never allow editable text in selection-only ComboBoxes
- Never use H2 database in production environments
- Never log sensitive information (passwords, tokens, personal data)
- Never commit hardcoded configuration values or secrets

---

## 11. Additional Best Practices

- Use dependency injection wherever possible
- All domain classes should be immutable where practical
- Always write unit tests for new features or bug fixes
- Always use proper JavaDoc for public classes and methods
- Ensure all database changes are backward compatible and thoroughly tested
- Use enums for all constant lists and types
- Ensure all UI components are accessible (a11y) and responsive
- Follow SOLID principles in class design
- Implement proper logging with appropriate log levels
- Use transactions appropriately for data consistency

---

## 12. Integration with GitHub Copilot

> **How to use with GitHub Copilot and PRs:**  
> - Reference this file (`copilot-java-strict-coding-rules.md`) in your project root and in your `docs` folder  
> - Link to this file in your README and in your PR/issue templates  
> - Remind contributors (and Copilot) to always follow these rules in every code review and commit  
> - Use these guidelines as context when requesting Copilot suggestions  
> - Always review and adapt Copilot-generated code to match these standards
