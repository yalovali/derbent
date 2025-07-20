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

## 8. Coding styles

- Use as much as final etc keywords
- If you thing a similar features can use a base class create an abstract base class with abstract fields and methods
- always start a class with capital C. Like CUser CSettings etc. 
- Ensure always PostgreSQL-only configuration. Always update data.sql to have proper sample database initial values
- always check lazy loading issues using best practices, give comments.
- always check nullpointers and null values, if necassary check empty strings.
- always try to use base classes.
- every important function should have a logger at the start of function, no need to log at end of functions.
- try to catch exceptions and handle them in vaadin ui always nicely
- use vaadin ui components to display exception if possible.
- try to use MetaData.java and CEntityFormBuilder.java to bind and generate forms for entities if possible.
- extend these base classes or add new ones or add more features, if necessary to increase modularity of the code.
- Always use apropriate icons for views
- for delete operations always use user approval
- selective combobox's are selection only. user should not be able to input text


---

**How to Use:**  
- Reference this guideline for every Copilot-driven code change.
- Adapt and expand with project-specific rules as your Vaadin application grows.
- Link to this file from your main `README.md` for visibility.