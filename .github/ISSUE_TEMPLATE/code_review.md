---
name: Code review
about: Track code review tasks for the project
title: '[CODE REVIEW] '
labels: 'code-review, documentation'
assignees: ''

---

**Review Scope**
Describe the scope of the code review (e.g., specific module, feature, or entire codebase section).

**Areas to Review**
List the specific areas, files, or components that need review:
- [ ] Area 1
- [ ] Area 2
- [ ] Area 3

**Review Objectives**
What are the main objectives of this code review?
- [ ] Code quality and maintainability
- [ ] Adherence to coding standards
- [ ] Security vulnerabilities
- [ ] Performance optimization
- [ ] Documentation completeness
- [ ] Test coverage
- [ ] Other: [specify]

**Priority**
- [ ] High - Critical issues affecting production
- [ ] Medium - Important improvements needed
- [ ] Low - Nice-to-have improvements

**Review Checklist**
- [ ] Code follows project coding standards (see `docs/architecture/coding-standards.md`)
- [ ] C-prefix convention applied to all custom classes
- [ ] Proper use of CNotificationService/CNotifications
- [ ] No direct Vaadin notification calls
- [ ] Repository queries include ORDER BY clause
- [ ] Service classes are stateless (no user-specific state)
- [ ] Entity constants defined (ENTITY_TITLE_SINGULAR, ENTITY_TITLE_PLURAL)
- [ ] Proper error handling with Check utility
- [ ] UI components follow typeName naming convention
- [ ] Event handlers follow on_xxx_eventType pattern
- [ ] Factory methods follow create_xxx pattern
- [ ] Import statements used instead of full class names
- [ ] Tests are comprehensive and passing
- [ ] Documentation is up-to-date

**Files/Modules to Review**
List specific files or modules:
```
src/main/java/tech/derbent/...
```

**Related Issues**
Link any related issues or PRs that prompted this review.

**Timeline**
Target completion date: [YYYY-MM-DD]

**Reviewer(s)**
Tag team members who should participate in the review: @username

**Additional Context**
Add any other context, background, or specific concerns about the code that needs review.

**Notes**
Any additional notes or observations during the review process.
