# Sample Initialization Pattern Enforcement - AGENTS.md Update

## Add to Section 4: Entity Management Patterns

### 4.9 Sample Initialization Pattern (MANDATORY)

**RULE**: All entity initializer services MUST implement `initializeSample()` following standardized patterns.

#### Entity Types & Patterns

##### 1. Business Entities (Activity, Meeting, Decision, etc.)

**Pattern**: Seed data with loop + optional relationships

```java
public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
    // Seed data structure
    record EntitySeed(String name, String description, String field1, int field2) {}
    
    final List<EntitySeed> seeds = List.of(
        new EntitySeed("Name 1", "Description 1", "value1", 10),
        new EntitySeed("Name 2", "Description 2", "value2", 20)
    );
    
    try {
        final EntityService service = CSpringContext.getBean(EntityService.class);
        final List<Entity> created = new ArrayList<>();
        
        for (final EntitySeed seed : seeds) {
            Entity entity = new Entity(seed.name(), project);
            entity.setDescription(seed.description());
            // ... set fields from seed
            entity = service.save(entity);
            created.add(entity);
            
            if (minimal) break;
        }
        
        // Add relationships (only if not minimal)
        if (!minimal && !created.isEmpty()) {
            addRelationshipsToEntities(created, project);
        }
        
        LOGGER.debug("Created {} sample entities", created.size());
    } catch (final Exception e) {
        LOGGER.error("Error initializing samples: {}", project.getName(), e);
        throw new RuntimeException("Failed to initialize samples: " + project.getName(), e);
    }
}
```

##### 2. Type Entities (ActivityType, MeetingType, etc.)

**Pattern**: Name/Description array + `initializeCompanyEntity()` helper

```java
public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
    final String[][] nameAndDescriptions = {
        { "Type Name 1", "Type description 1" },
        { "Type Name 2", "Type description 2" },
        { "Type Name 3", "Type description 3" }
    };
    
    final CCompany company = project.getCompany();
    initializeCompanyEntity(
        nameAndDescriptions,
        (CEntityOfCompanyService<?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)), 
        company, 
        minimal, 
        null
    );
}
```

##### 3. Hierarchical Entities (Epic → Feature → UserStory → Activity)

**Pattern**: Parent parameters + return array

```java
public static EntityType[] initializeSample(final CProject<?> project, final boolean minimal,
        final ParentType parent1, final ParentType parent2) throws Exception {
    
    record EntitySeed(String name, String description, int parentIndex) {}
    
    final List<EntitySeed> seeds = List.of(
        new EntitySeed("Name 1", "Description 1", 0),  // Links to parent1
        new EntitySeed("Name 2", "Description 2", 1)   // Links to parent2
    );
    
    final ParentType[] parents = { parent1, parent2 };
    final EntityType[] created = new EntityType[2];
    int index = 0;
    
    for (final EntitySeed seed : seeds) {
        EntityType entity = new EntityType(seed.name(), project);
        entity.setDescription(seed.description());
        
        // Link to parent using seed index
        final ParentType parent = parents[seed.parentIndex()];
        if (parent != null) {
            entity.setParent(parent);
        } else if (parent1 != null) {
            entity.setParent(parent1);  // Fallback
        }
        
        entity = service.save(entity);
        created[index++] = entity;
        
        if (minimal) break;
    }
    
    return created;
}
```

#### Sample Relationship Utilities (MANDATORY)

**RULE**: Use existing initializer service methods for relationships - NO separate helper classes.

##### Comments

```java
// In entity initializer after creating entities:
final List<CComment> comments = CCommentInitializerService.createSampleComments(
    new String[] { "Comment text 1", "Comment text 2" },
    new boolean[] { false, true }  // importance flags
);
entity.getComments().addAll(comments);
service.save(entity);
```

##### Attachments

```java
// In entity initializer after creating entities:
final List<CAttachment> attachments = CAttachmentInitializerService.createSampleAttachments(
    new String[][] {
        { "filename.pdf", "Description", "12345" },  // [name, description, size]
        { "diagram.png", "Architecture diagram", "67890" }
    },
    project.getCompany()
);
entity.getAttachments().addAll(attachments);
service.save(entity);
```

##### Links

```java
// In entity initializer after creating entities:
final CLink link = CLinkInitializerService.createRandomLink(
    sourceEntity, project,
    TargetEntityClass.class,
    TargetEntityService.class,
    "Link Type",  // e.g., "Related", "Depends On", "Implements"
    "Link description",
    project.getCompany()
);
if (link != null) {
    entity.getLinks().add(link);
}
service.save(entity);
```

#### Checklist for Sample Initialization

Before committing any initializer with `initializeSample()`:

- [ ] **Method signature** correct (static, public, appropriate parameters)
- [ ] **Seed data** defined as Java record or String[][] array
- [ ] **Loop** used for entity creation (no duplicate code blocks)
- [ ] **Minimal mode** supported (breaks after first item when `minimal=true`)
- [ ] **Try-catch** wrapper with proper error logging
- [ ] **Services** obtained via `CSpringContext.getBean()`
- [ ] **Relationships** added AFTER entity creation (if not minimal)
- [ ] **Relationship utilities** from respective initializer services
  - [ ] Comments: `CCommentInitializerService.createSampleComments()`
  - [ ] Attachments: `CAttachmentInitializerService.createSampleAttachments()`
  - [ ] Links: `CLinkInitializerService.createRandomLink()`
- [ ] **Logging** includes info for each created entity
- [ ] **JavaDoc** comment describes parameters and return value
- [ ] **Return type** appropriate (array for hierarchies, void otherwise)
- [ ] **NO separate helper classes** - use existing initializer utilities

#### Anti-Patterns

❌ **DON'T create separate helper classes**:
```java
// WRONG - Don't create CRelationshipSampleHelper or similar
public class CRelationshipSampleHelper {
    public static void addComments(...) { }
}
```

❌ **DON'T duplicate relationship code**:
```java
// WRONG - Manual comment creation
final CUser user = userService.getRandom(company);
final CComment comment = new CComment("text", user);
entity.getComments().add(comment);
```

✅ **DO use initializer service utilities**:
```java
// CORRECT - Use existing initializer utilities
final List<CComment> comments = CCommentInitializerService.createSampleComments("text");
entity.getComments().addAll(comments);
```

#### Integration with CDataInitializer

```java
// In CDataInitializer.loadSampleData():

// Independent entities
CDecisionInitializerService.initializeSample(project, minimal);
CMeetingInitializerService.initializeSample(project, minimal);

// Hierarchical entities - maintain order
final CEpic[] epics = CEpicInitializerService.initializeSample(project, minimal);
final CFeature[] features = CFeatureInitializerService.initializeSample(
    project, minimal, epics[0], epics[1]);
final CUserStory[] stories = CUserStoryInitializerService.initializeSample(
    project, minimal, features[0], features[1]);
CActivityInitializerService.initializeSample(project, minimal, stories[0], stories[1]);
```

#### Documentation Reference

- Pattern guide: `/docs/patterns/SAMPLE_INITIALIZATION_PATTERN.md`
- Summary: `/docs/patterns/SAMPLE_INITIALIZATION_SUMMARY.md`
- Examples: Check existing initializers (CActivityInitializerService, CEpicInitializerService, CActivityTypeInitializerService)

---

## Summary of Changes

This update adds mandatory sample initialization pattern enforcement to AGENTS.md:

1. **Entity-specific patterns** for business entities, type entities, and hierarchical entities
2. **Relationship utilities** in respective initializer services (NO separate helper classes)
3. **Comprehensive checklist** for verification
4. **Anti-patterns** to avoid
5. **Integration guidance** for CDataInitializer

All AI agents MUST follow these patterns when creating or modifying entity initializers.
