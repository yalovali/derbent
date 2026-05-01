# InitializerService Architecture

**Version**: 1.0
**Date**: 2026-04-29
**Status**: MANDATORY - All entity initializer services MUST follow this architecture

---

## Overview

Initializer services bootstrap the UI metadata (grids, detail sections, pages, sample data) for every entity in the system. They run at application startup via `CDataInitializer` and are **all-static utility classes** — no Spring `@Autowired` fields, no instance state.

---

## Class Hierarchy

```
CInitializerServiceBase                          (api/screens/service/)
  ├── CEntityNamedInitializerService             (abstract — named entities)
  │     └── CEntityOfProjectInitializerService   (abstract — project-scoped entities)
  │           └── CProjectItemInitializerService  (abstract — project items)
  │                 └── CActivityInitializerService  (concrete leaf)
  │                     CUserStoryInitializerService (concrete leaf)
  │                     ... (all PLM project-item initializers)
  │
  │           CGridEntityInitializerService       (concrete — extends CEntityOfProject)
  │           CMasterInitializerService           (concrete — extends CEntityOfProject)
  │           ... (other project-scoped entity initializers)
  │
  ├── CEntityInitializerService                  (abstract — api/entity/service/)
  │
  └── (BAB + other domain initializers that extend CInitializerServiceBase directly)
        CDashboardProject_BabInitializerService
        CBabPolicyActionMask*InitializerService
        ...
```

---

## Layer Responsibilities

### `CInitializerServiceBase`
Root of all initializers. Provides:
- **Menu constants** — `Menu_Order_*` and `MenuTitle_*` static fields used by all leaf services.
- **`createBaseGridEntity(project, clazz)`** — creates a `CGridEntity` wired to the entity's service bean.
- **`createBaseScreenEntity(project, clazz)`** — creates a `CDetailSection` shell.
- **`createPageEntity(...)`** — creates a `CPageEntity` wiring grid + detail section + menu position.
- **`initBase(...)`** — saves grid, detail section, page in one call (used by all `initialize()` implementations).
- **`initializeCompanyEntity()`** — loop helper for `CEntityOfCompany` seed data.
- **`initializeProjectEntity()`** — loop helper for `CEntityOfProject` seed data.

### `CEntityNamedInitializerService`
Adds **id / name / description** standard block. Use for any named entity.
- `createBasicView(scr, clazz, project, newSection)` — appends id/name/description lines to an existing `CDetailSection`.
- `createTypeEntityView(project, clazz, ...)` — full view factory for company-scoped **CXxxType** entities (adds company, workflow, color, sortOrder, active, optional audit).

### `CEntityOfProjectInitializerService`
Overrides `createBasicView` to add **active** after description (project-scoped pattern):
- `createBasicView(scr, clazz, project, newSection)` — id / name / description / active.
- `createBasicView(project, clazz)` / `createBasicView(project, clazz, newSection)` — factory overloads that allocate the `CDetailSection` and call the mutating variant.

### `CProjectItemInitializerService`
Empty abstract marker for **project-item** entities (activities, user stories, epics, …). Reserved for future project-item-specific shared helpers.

---

## Mandatory Method Contracts

Every concrete initializer MUST implement three static methods:

```java
// 1. Detail view — field layout for the entity's form
public static CDetailSection createBasicView(final CProject<?> project) throws Exception

// 2. Grid view — column list for the entity's list/grid
public static CGridEntity createGridEntity(final CProject<?> project)

// 3. System registration — saves grid + detail + page
public static void initialize(
    final CProject<?> project,
    final CGridEntityService gridEntityService,
    final CDetailSectionService detailSectionService,
    final CPageEntityService pageEntityService) throws Exception
```

Most concrete initializers also implement:

```java
// 4. Sample data — seeds demo entities
public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception
// (or the hierarchical variant with parent parameters)
```

---

## Canonical Implementation Template

```java
public class C{Entity}InitializerService extends CProjectItemInitializerService {
    // (or CEntityOfProjectInitializerService for non-project-item project entities)

    static final Class<?> clazz = C{Entity}.class;
    private static final Logger LOGGER = LoggerFactory.getLogger(C{Entity}InitializerService.class);
    private static final String menuOrder = Menu_Order_PROJECT + ".XX";
    private static final String menuTitle = MenuTitle_PROJECT + ".{Entities}";
    private static final String pageDescription = "{Entity} management for projects";
    private static final String pageTitle = "{Entity} Management";
    private static final boolean showInQuickToolbar = false;

    public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
        try {
            final CDetailSection scr = createBaseScreenEntity(project, clazz);
            CEntityNamedInitializerService.createBasicView(scr, clazz, project, true);
            // --- entity-specific sections ---
            scr.addScreenLine(CDetailLinesService.createSection("Details"));
            scr.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "entityType"));
            
            // --- standard composition (if applicable) ---
            CAttachmentInitializerService.addDefaultSection(scr, clazz);
            CLinkInitializerService.addDefaultSection(scr, clazz);
            CCommentInitializerService.addDefaultSection(scr, clazz);
            CParentRelationInitializerService.addDefaultSection(scr, clazz, project);
            scr.debug_printScreenInformation();
            return scr;
        } catch (final Exception e) {
            LOGGER.error("Error creating {entity} view.");
            throw e;
        }
    }

    public static CGridEntity createGridEntity(final CProject<?> project) {
        final CGridEntity grid = createBaseGridEntity(project, clazz);
        grid.setColumnFields(List.of("id", "name", "description", "entityType", "status",
                "assignedTo", "project", "createdDate"));
        return grid;
    }

    public static void initialize(final CProject<?> project,
            final CGridEntityService gridEntityService,
            final CDetailSectionService detailSectionService,
            final CPageEntityService pageEntityService) throws Exception {
        final CDetailSection detailSection = createBasicView(project);
        final CGridEntity grid = createGridEntity(project);
        initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService,
                detailSection, grid, menuTitle, pageTitle, pageDescription,
                showInQuickToolbar, menuOrder, null);
    }

    public static void initializeSample(final CProject<?> project, final boolean minimal)
            throws Exception {
        record EntitySeed(String name, String description) {}
        final List<EntitySeed> seeds = List.of(
                new EntitySeed("Sample One", "First sample {entity}"),
                new EntitySeed("Sample Two", "Second sample {entity}"));
        try {
            final C{Entity}Service service = CSpringContext.getBean(C{Entity}Service.class);
            for (final EntitySeed seed : seeds) {
                final C{Entity} entity = new C{Entity}(seed.name(), project);
                entity.setDescription(seed.description());
                service.save(entity);
                if (minimal) break;
            }
        } catch (final Exception e) {
            LOGGER.error("Error initializing sample {entities} for project: {} reason={}",
                    project.getName(), e.getMessage());
            throw new RuntimeException("Failed to initialize sample {entities}", e);
        }
    }
}
```

---

## Type Entity Initializer (Special Case)

Company-scoped type entities (`CXxxType`) use `CEntityNamedInitializerService.createTypeEntityView()` directly — they do **not** have `createdBy`, `assignedTo`, or `project` fields:

```java
public static CDetailSection createBasicView(final CProject<?> project) throws Exception {
    return CEntityNamedInitializerService.createTypeEntityView(
            project, clazz, "Display Configuration", true /* includeAudit */);
}
```

---

## Wiring into CDataInitializer (MANDATORY)

Every new initializer must be called from `CDataInitializer`:

```java
// In the project-loop (initialize pages):
CActivityInitializerService.initialize(project, gridService, detailService, pageService);

// In the sample-data section:
CActivityInitializerService.initializeSample(project, minimal, userStory1, userStory2);
```

Parent entities must be created before children in the hierarchy:
`Epic → Feature → UserStory → Activity`

---

## Standard Composition Sections

These helpers add consistent sections to any entity's detail view:

| Initializer helper | Section added | When to use |
|--------------------|--------------|-------------|
| `CAttachmentInitializerService.addDefaultSection(scr, clazz)` | Attachments | Any entity implementing `IHasAttachments` |
| `CLinkInitializerService.addDefaultSection(scr, clazz)` | Links | Any entity that can be linked |
| `CCommentInitializerService.addDefaultSection(scr, clazz)` | Comments | Any entity implementing `IHasComments` |
| `CParentRelationInitializerService.addDefaultSection(scr, clazz, project)` | Parent relation | Hierarchical project items |

---

## Rules

1. **All methods are `public static`** — initializers are never instantiated.
2. **Extend the correct level**: project items → `CProjectItemInitializerService`; other project entities → `CEntityOfProjectInitializerService`; type entities → `CEntityNamedInitializerService`; company-level → `CInitializerServiceBase`.
3. **Never skip `initBase()`** — always saves grid, detail section, and page together.
4. **Use `Menu_Order_*` and `MenuTitle_*` constants** — never raw strings.
5. **Call `scr.debug_printScreenInformation()`** at the end of `createBasicView()`.
6. **Do NOT create initializers for relation classes** (e.g., `CWorkflowStatusRelation`).
7. **BAB profile**: initializers should be annotated `@Service @Profile("bab")` and extend `CInitializerServiceBase` directly (no PLM abstracts).

---

## Related Documentation

- [Sample Initialization Pattern](../patterns/SAMPLE_INITIALIZATION_PATTERN.md) — `initializeSample()` seed-data pattern
- [New Entity Complete Checklist](NEW_ENTITY_COMPLETE_CHECKLIST.md) — full checklist for new entities
- [Service Layer Patterns](service-layer-patterns.md) — service class patterns
- [Entity Inheritance Patterns](entity-inheritance-patterns.md) — entity class hierarchy

---

**Last Updated**: 2026-04-29
