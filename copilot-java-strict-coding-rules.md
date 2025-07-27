# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

This is a project for maintaining work flows, tasks, managerial targets, procedures, methodologies of a product or project within a company.

For solutions, usage coverage and UI design, inspire from Atlassian Jira product or Projeqtor from page https://projeqtor.org/en

Check [project_design_description.md](./project_design_description.md) file for project scope, update this document if necessary or given task enlarges this scope.

## Table of Contents
- [1. MVC Architecture Principles](#1-mvc-architecture-principles)
- [2. Commenting Standards](#2-commenting-standards)
- [3. Java Checks Before Submission](#3-java-checks-before-submission)
- [4. Pull Request Checklist](#4-pull-request-checklist)
- [5. Copilot Review Protocol](#5-copilot-review-protocol)
- [6. Coding Styles & Best Practices](#6-coding-styles--best-practices)
- [7. CSS Guidelines](#7-css-guidelines)
- [8. Database Rules](#8-database-rules)
- [9. Documentation & Modularity](#9-documentation--modularity)
- [10. Strict Prohibitions](#10-strict-prohibitions)
- [11. Additional Best Practices](#11-additional-best-practices)
- [Related Documentation](#related-documentation)

---

## 1. MVC Architecture Principles

- **Domain:**  
  Represents business logic and data. Use POJOs, Entities, and Service classes.
- **View:**  
  UI components using Vaadin's framework. Avoid embedding business logic in views.
- **Service:**  
  Handles user input, updates models, and refreshes views. Typically, Vaadin views act as controllers.

**Example directory structure:**
```
src/main/java/cuser
├── domain/
├── view/
├── controller/
```

---

## 2. Commenting Standards

- **Every class:** Document its role in MVC.
- **Every method:** Describe parameters, return value, and side effects.
- **Complex logic:** Inline comments explaining tricky parts.
- **Copilot suggestions:** Review and expand comments for clarity.

---

## 3. Java Checks Before Submission

- Compile using `mvn clean install` or your build tool.
- Run static analysis: CheckStyle, PMD, or SonarLint.
- Run unit and integration tests. Ensure coverage for all controller logic.
- Confirm no business logic is present in View classes.
- Check for unused imports and variables.
- Validate all forms of input in controllers.

---

## 4. Pull Request Checklist

- Code follows MVC separation.
- Methods and classes are properly commented.
- All Copilot-generated code reviewed and tested.
- All Java checks passed: compilation, static analysis, testing.
- No hardcoded values in controllers or views.
- UI logic (Vaadin) does not leak into model/service layers.

---

## 5. Copilot Review Protocol

After accepting Copilot suggestions, manually review for:
- Correct MVC placement.
- Security (input validation, access control).
- Performance and scalability.
- Proper exception handling.

---

## 6. Coding Styles & Best Practices

**General Principles**
- Use `final` keyword wherever possible (variables, parameters, methods, classes).
- Favor abstraction: if two or more features are similar, create an abstract base class with abstract fields and methods.
- Always start class names with a capital "C" (e.g., `CUser`, `CSettings`). Do not use standard Java class naming for domain classes.
- Check for lazy loading issues using best practices (e.g., `@Transactional`, `fetch = FetchType.LAZY`). Add comments explaining lazy loading risks or solutions.
- Always check for `null` values and possible `NullPointerException` in every function. If necessary, also check for empty strings.
- Always prefer using base classes to avoid code duplication.
- Every important function must start with a logger statement detailing function name and parameter values (do not log at function end).
- Always catch exceptions in Vaadin UI and handle them gracefully.
- Use Vaadin UI components to display exceptions to users where possible.
- Always use `MetaData.java` and `CEntityFormBuilder.java` to bind and generate forms for entities when possible. (See [annotation-based form building documentation](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md))
- Extend base classes or add new ones to increase modularity and maintainability.
- Always use appropriate icons for views and UI components.
- For delete operations, always require explicit user approval (e.g., confirmation dialog).
- All selective ComboBoxes must be **selection only** (user must not be able to type arbitrary text).
- Keep project documentation updated in the `docs` folder; create separate documents for each design concept. If implementing complex solutions (e.g., Spring technologies), create a step-by-step solution document.
- Use the `abstracts/view` package for project-wide UI components (e.g., `CButton`, `CGrid`). Always use these superclasses and extend them as needed; add new UI classes here.
- Whenever a button action is not applicable (e.g., user not selected but project add requested), do not silently return. Show a user warning explaining the situation. Use new dialog classes:
  - `CWarningDialog`
  - `CInformationDialog`
  - `CExceptionDialog`
  - These dialogs must be simple, visually appealing, and inherit from a common superclass (e.g., `CBaseDialog`).
- Never add loggers at the end of functions. Always log at the start with full detail about the function and parameters.
- All code must follow Java naming conventions for variables and methods, except for class names which must start with "C".

## 7. CSS Guidelines

**FOR CSS:**
- Update CSS names from class name, also update CSS file accordingly
- Always use very simple CSS. Don't use JavaScript in Java or CSS.
- You can use CSS built-in simple functions
- To style a layout use syntax like:
  ```css
  #vaadinLoginOverlayWrapper::part(overlay) {
      background-image: url('./images/background1.png');
  }
  ```
- Check the pattern of CPanelActivityDescription and its super classes (see [CPanelActivityDescription implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md)). Use this pattern to group each entity's fields according to related business topic. Don't leave any field out. Create necessary classes, base classes with same naming convention for every entity and group fields in the view. Only open the first one.
- Always check reference projects and do this task for a better structure, view and requirements of successful resource management, task tracking, project management, budget planning and better UI experience.
- Always if necessary create new classes to support the new requirements with always less code and nice super classes for reusability
- Prepare a detailed requirements document that includes this task description, main features, user roles, and any specific technologies or integrations that should be considered.

For detailed CSS guidelines, see [CSS_GUIDELINES.md](./CSS_GUIDELINES.md).

## 8. Database Rules
- password is always test123 for all users with has code '$2a$10$eBLr1ru7O8ZYEaAnRaNIMeQQf.eb7O/h3wW43bC7Z9ZxVusUdCVXu'
- every entity should have an example in data.sql for per project, per company per user per activity etc...
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change.
- keep spring.jpa.defer-datasource-initialization=true
- dont use memory database H2
- always delete tables at top of data.sql before you insert values into it. check them before they exists
- delete constraints etc. if there is change in the db structure
- always reset sequences in the format and escape characters below with a conditonal to check if they exists first
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
		

## 9. Documentation & Modularity

- Update the `docs` folder for every significant change:
  - Add new documentation for each project design concept (one file per concept).
  - For complex or Spring-based solutions, create a step-by-step solution file.
- All new UI components, dialogs, and base classes must be documented.

### Available Documentation:
- [Project Design Description](./project_design_description.md) - Overall project scope and requirements
- [Enhanced Activity Management](./enhanced-activity-management-implementation.md) - Activity system implementation
- [CSS Guidelines](./CSS_GUIDELINES.md) - Styling standards and best practices
- [CPanelActivityDescription Implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md) - Entity panel patterns
- [User Profile Dialog Implementation](./user-profile-dialog-implementation.md) - Dialog implementation patterns
- [Annotation-Based ComboBox Data Providers](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md) - Form building patterns
- [Hierarchical Side Menu Implementation](./HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md) - Navigation patterns

## 10. Strict Prohibitions

- **Never use standard Vaadin `Button` directly!**  
  Immediately refactor to use `CButton` (or its improved/extended version).  
  If `CButton` can be improved, extend it so all buttons share consistent behavior and appearance.
- Never bypass base classes for UI components. Always use or extend base classes from `abstracts/view`.
- Never silently fail on user actions. Always notify the user with an appropriate dialog.
- Never allow editable text in selection-only ComboBoxes.

## 11. Additional Best Practices

- Use dependency injection wherever possible.
- All domain classes should be immutable where practical.
- Always write unit tests for new features or bug fixes.
- Always use proper JavaDoc for public classes and methods.
- Ensure all database changes are backward compatible and thoroughly tested.
- Use enums for all constant lists and types.
- Ensure all UI components are accessible (a11y) and responsive.

---

## Related Documentation

For comprehensive development guidance, refer to these additional documents:

### Core Architecture & Design
- [Project Design Description](./project_design_description.md) - Complete project requirements and architecture
- [Enhanced Activity Management Requirements](./enhanced-activity-management-requirements.md) - Activity system specifications

### UI/UX Implementation Guides  
- [CSS Guidelines](./CSS_GUIDELINES.md) - Complete styling standards and Vaadin theming
- [CPanelActivityDescription Implementation](./CPANEL_ACTIVITY_DESCRIPTION_IMPLEMENTATION.md) - Entity panel patterns
- [User Profile Dialog Implementation](./user-profile-dialog-implementation.md) - Dialog patterns
- [Hierarchical Side Menu Implementation](./HIERARCHICAL_SIDE_MENU_IMPLEMENTATION.md) - Navigation implementation

### Technical Implementation Patterns
- [Annotation-Based ComboBox Data Providers](./ANNOTATION_BASED_COMBOBOX_DATA_PROVIDERS.md) - Form building with annotations
- [Comprehensive Lazy Loading Fix](./COMPREHENSIVE_LAZY_LOADING_FIX.md) - JPA performance optimization
- [User Company Association Requirements](./user-company-association-requirements.md) - Multi-tenant patterns

### Development Best Practices
- [CActivity Enhancement Summary](./cactivity-enhancement-summary.md) - Activity domain implementation patterns

---

> **How to use with GitHub Copilot and PRs:**  
> - This file exists in your `docs` folder and should also be copied to project root.  
> - Link to this file in your README and in your PR/issue templates.
> - Remind contributors (and Copilot) to always follow these rules in every code review and commit.
> - Reference the related documentation above for specific implementation patterns.
