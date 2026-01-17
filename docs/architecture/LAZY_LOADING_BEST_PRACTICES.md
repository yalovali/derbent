# Lazy Loading Best Practices - Preventing LazyInitializationException

## Problem

When entities with `@OneToMany(fetch = FetchType.LAZY)` collections are loaded and then accessed outside the Hibernate session context, a `LazyInitializationException` occurs.

**Common scenario:**
```
1. Entity loaded in repository/service (@Transactional)
2. Session closes after method returns
3. UI component tries to access lazy collection
4. Exception: "could not initialize proxy - no Session"
```

## Solution

**ALWAYS eagerly fetch lazy collections in repository queries** when the entity will be used in UI forms.

### Mandatory Pattern for All Repository Queries

For entities implementing `IHasAttachments` and/or `IHasComments`, **ALWAYS include:**

```java
LEFT JOIN FETCH r.attachments
LEFT JOIN FETCH r.comments
```

### Example: Complete Repository Pattern

```java
public interface IEntityRepository extends IEntityOfProjectRepository<CEntity> {

    @Override
    @Query (
        "SELECT r FROM CEntity r " +
        "LEFT JOIN FETCH r.project " +
        "LEFT JOIN FETCH r.assignedTo " +
        "LEFT JOIN FETCH r.createdBy " +
        "LEFT JOIN FETCH r.status " +
        "LEFT JOIN FETCH r.entityType et " +
        "LEFT JOIN FETCH et.workflow " +
        "LEFT JOIN FETCH r.attachments " +  // ✅ MANDATORY if implements IHasAttachments
        "LEFT JOIN FETCH r.comments " +     // ✅ MANDATORY if implements IHasComments
        "WHERE r.id = :id"
    )
    Optional<CEntity> findById(@Param ("id") Long id);

    @Override
    @Query ("""
            SELECT r FROM CEntity r
            LEFT JOIN FETCH r.project
            LEFT JOIN FETCH r.assignedTo
            LEFT JOIN FETCH r.createdBy
            LEFT JOIN FETCH r.status
            LEFT JOIN FETCH r.entityType et
            LEFT JOIN FETCH et.workflow
            LEFT JOIN FETCH r.attachments
            LEFT JOIN FETCH r.comments
            WHERE r.project = :project
            ORDER BY r.name ASC
            """)
    List<CEntity> listByProjectForPageView(@Param ("project") CProject project);
}
```

## Checklist for New Entities

When creating a new entity that implements `IHasAttachments` or `IHasComments`:

- [ ] ✅ Domain class has `@OneToMany(fetch = FetchType.LAZY)` for attachments/comments
- [ ] ✅ Repository `findById()` includes `LEFT JOIN FETCH r.attachments`
- [ ] ✅ Repository `findById()` includes `LEFT JOIN FETCH r.comments`
- [ ] ✅ Repository `listByProjectForPageView()` includes `LEFT JOIN FETCH r.attachments`
- [ ] ✅ Repository `listByProjectForPageView()` includes `LEFT JOIN FETCH r.comments`
- [ ] ✅ Any custom query methods also include eager fetching
- [ ] ✅ Test the entity page in comprehensive test suite

## Common Entities Fixed

The following entities have been updated to follow this pattern:

| Entity | Repository | Status |
|--------|-----------|--------|
| CActivity | IActivityRepository | ✅ Already correct |
| CMeeting | IMeetingRepository | ✅ Already correct |
| CRisk | IRiskRepository | ✅ Already correct |
| CDecision | IDecisionRepository | ✅ Already correct |
| CIssue | IIssueRepository | ✅ Already correct |
| COrder | IOrderRepository | ✅ Already correct |
| CAsset | IAssetRepository | ✅ **FIXED** |
| CTicket | ITicketRepository | ✅ **FIXED** |
| CDeliverable | IDeliverableRepository | ✅ **FIXED** |
| CProject | IProjectRepository | ✅ Already correct |
| CUser | IUserRepository | ✅ **FIXED** (2026-01-17) - Added comments to findById |

## Testing

The comprehensive Playwright test (`CPageTestAuxillaryComprehensiveTest`) will catch these issues:

```bash
# Run comprehensive test to verify all pages
./run-playwright-tests.sh comprehensive
```

**The test uses fail-fast:** It stops on the first LazyInitializationException and reports:
- Entity name
- Field causing the issue
- Full stack trace

## Performance Considerations

**Q:** Doesn't eager fetching hurt performance?

**A:** In our use case, **NO**:
- Forms always need attachments/comments to display the UI components
- Lazy loading + N+1 queries is actually slower
- Single JOIN FETCH query is more efficient
- Collections are typically small (< 100 items)

**Q:** What if collections are huge?

**A:** Consider these alternatives:
1. Paginate the attachments/comments grid
2. Load on-demand when tab/accordion is opened
3. Use DTO projections for list views, full entity for detail views

## Code Review Checklist

When reviewing PRs that add new entities:

- [ ] Does entity implement `IHasAttachments` or `IHasComments`?
- [ ] Does repository query include `LEFT JOIN FETCH` for these collections?
- [ ] Has the developer tested the page manually?
- [ ] Has the comprehensive test passed for this entity?

## References

- Existing correct implementations: `IActivityRepository`, `IMeetingRepository`, `IUserRepository`
- Hibernate documentation: https://docs.jboss.org/hibernate/orm/6.0/userguide/html_single/Hibernate_User_Guide.html#fetching
- Spring Data JPA: https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query

## Update History

- 2026-01-17: Fixed CUser.findById() - added missing LEFT JOIN FETCH u.comments
- 2026-01-16: Initial document created after fixing Asset, Ticket, and Deliverable entities
- Pattern applies to ALL entities with `IHasAttachments` or `IHasComments`
