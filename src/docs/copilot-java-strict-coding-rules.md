# Copilot Agent Guideline for Vaadin Java Projects (MVC Design)

this is a project for maintaining work flows, tasks, managerial targets, procedures, methodolgies of a product or project within a company.


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

## 2. Using Copilot for Code Suggestions

- Always specify the layer (Model, View, Controller) when requesting code from Copilot.
- Request Copilot comments for key methods: explain responsibility, interactions, and MVC boundaries.
- Ask Copilot for test stubs and edge-case handling.

**Example Copilot prompt:**
```java
// Copilot: Generate a Vaadin view class for displaying User entities, using UserService as the model.
```

---

## 3. Commenting Standards

- **Every class:** Document its role in MVC.
- **Every method:** Describe parameters, return value, and side effects.
- **Complex logic:** Inline comments explaining tricky parts.
- **Copilot suggestions:** Review and expand comments for clarity.

---

## 4. Java Checks Before Submission

- Compile using `mvn clean install` or your build tool.
- Run static analysis: CheckStyle, PMD, or SonarLint.
- Run unit and integration tests. Ensure coverage for all controller logic.
- Confirm no business logic is present in View classes.
- Check for unused imports and variables.
- Validate all forms of input in controllers.

---

## 5. Pull Request Checklist

- Code follows MVC separation.
- Methods and classes are properly commented.
- All Copilot-generated code reviewed and tested.
- All Java checks passed: compilation, static analysis, testing.
- No hardcoded values in controllers or views.
- UI logic (Vaadin) does not leak into model/service layers.

---

## 6. Example: Annotated MVC Class (Controller)

```java
package controller;

import model.User;
import service.UserService;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

/**
 * UserViewController - Handles user interactions and updates the view.
 * Layer: Controller (MVC)
 */
@Route("users")
public class UserViewController extends VerticalLayout {
    private final UserService userService;

    public UserViewController(UserService userService) {
        this.userService = userService;
        // Initialize view components
        // Copilot: Display users in a Vaadin Grid
    }
}
```

---

## 7. Copilot Review Protocol

After accepting Copilot suggestions, manually review for:
- Correct MVC placement.
- Security (input validation, access control).
- Performance and scalability.
- Proper exception handling.
# GitHub Copilot Strict Coding Rules for Java Project

## 8. Coding Styles & Best Practices

**General Principles**
- Use `final` keyword wherever possible (variables, parameters, methods, classes).
- Favor abstraction: if two or more features are similar, create an abstract base class with abstract fields and methods.
- Always start class names with a capital "C" (e.g., `CUser`, `CSettings`). Do not use standard Java class naming for domain classes.
- Always ensure **PostgreSQL-only** configuration. Update `data.sql` with correct sample and initial database values after any database change.
- Check for lazy loading issues using best practices (e.g., `@Transactional`, `fetch = FetchType.LAZY`). Add comments explaining lazy loading risks or solutions.
- Always check for `null` values and possible `NullPointerException` in every function. If necessary, also check for empty strings.
- Always prefer using base classes to avoid code duplication.
- Every important function must start with a logger statement detailing function name and parameter values (do not log at function end).
- Always catch exceptions in Vaadin UI and handle them gracefully.
- Use Vaadin UI components to display exceptions to users where possible.
- Always use `MetaData.java` and `CEntityFormBuilder.java` to bind and generate forms for entities when possible.
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

**Documentation & Modularity**
- Update the `docs` folder for every significant change:
  - Add new documentation for each project design concept (one file per concept).
  - For complex or Spring-based solutions, create a step-by-step solution file.
- All new UI components, dialogs, and base classes must be documented.

## 9. Strict Prohibitions

- **Never use standard Vaadin `Button` directly!**  
  Immediately refactor to use `CButton` (or its improved/extended version).  
  If `CButton` can be improved, extend it so all buttons share consistent behavior and appearance.
- Never bypass base classes for UI components. Always use or extend base classes from `abstracts/view`.
- Never silently fail on user actions. Always notify the user with an appropriate dialog.
- Never allow editable text in selection-only ComboBoxes.

## 10. Additional Best Practices

- Use dependency injection wherever possible.
- All domain classes should be immutable where practical.
- Always write unit tests for new features or bug fixes.
- Always use proper JavaDoc for public classes and methods.
- Ensure all database changes are backward compatible and thoroughly tested.
- Use enums for all constant lists and types.
- Ensure all UI components are accessible (a11y) and responsive.

---

> **How to use with GitHub Copilot and PRs:**  
> - Add these as a `copilot-java-strict-coding-rules.md` in your project root and in your `docs` folder.  
> - Link to this file in your README and in your PR/issue templates.
> - Remind contributors (and Copilot) to always follow these rules in every code review and commit.
