# Sample Initialization Pattern for Entity Initializers

**Version**: 1.0  
**Date**: 2026-01-26  
**Status**: MANDATORY - All entity initializer services MUST follow this pattern

---

## Overview

All entity initializer services that create sample data MUST implement a static `initializeSample()` method following the standardized **seed data pattern with loops**. This ensures:

- ✅ **Consistency**: All sample data follows the same structure
- ✅ **Maintainability**: Easy to update sample data without code changes  
- ✅ **DRY Principle**: No code duplication
- ✅ **Testability**: Clear separation between data and logic
- ✅ **Scalability**: Easy to add more samples or reduce for minimal mode

---

## Mandatory Pattern Components

### 1. Method Signature

#### For Project-Scoped Entities
```java
/**
 * Initialize sample {entities} for a project.
 *
 * @param project the project to create {entities} for
 * @param minimal if true, creates only 1 {entity}; if false, creates 2+ {entities}
 * @return (optional) array of created entities if needed for hierarchy linking
 */
public static {EntityType}[] initializeSample(final CProject<?> project, final boolean minimal) throws Exception
```

#### For Entities with Parent Dependencies
```java
/**
 * Initialize sample {entities} for a project.
 *
 * @param project       the project to create {entities} for
 * @param minimal       if true, creates only 1 {entity}; if false, creates 2+ {entities}
 * @param parent1       the first parent entity to link to (can be null)
 * @param parent2       the second parent entity for second sample (can be null)
 * @return array of created entities for further hierarchy linking
 */
public static {EntityType}[] initializeSample(final CProject<?> project, final boolean minimal, 
        final {ParentType} parent1, final {ParentType} parent2) throws Exception
```

### 2. Seed Data Structure (MANDATORY)

**Use Java records for seed data**:

```java
// Simple entities
record {Entity}Seed(String name, String description) {}

// With additional fields
record {Entity}Seed(String name, String description, String field1, int field2) {}

// With parent index for hierarchies
record {Entity}Seed(String name, String description, int parentIndex) {}
```

**Define seed data as immutable List**:

```java
final List<{Entity}Seed> seeds = List.of(
    new {Entity}Seed("Sample Name 1", "Sample description 1"),
    new {Entity}Seed("Sample Name 2", "Sample description 2")
);
```

### 3. Loop-Based Creation (MANDATORY)

```java
try {
    // Get required services via CSpringContext
    final {Entity}Service entityService = CSpringContext.getBean({Entity}Service.class);
    final {Type}Service typeService = CSpringContext.getBean({Type}Service.class);
    final CUserService userService = CSpringContext.getBean(CUserService.class);
    
    // Optional: array to store created entities for return
    final {Entity}[] created = new {Entity}[seeds.size()];
    int index = 0;
    
    // Loop through seeds
    for (final {Entity}Seed seed : seeds) {
        // Get random/required dependencies
        final {Type} type = typeService.getRandom(project.getCompany());
        final CUser user = userService.getRandom(project.getCompany());
        
        // Create entity from seed data
        {Entity} entity = new {Entity}(seed.name(), project);
        entity.setDescription(seed.description());
        entity.setEntityType(type);
        entity.setAssignedTo(user);
        
        // Set additional fields from seed (entity-specific)
        // ...
        
        // Save entity
        entity = entityService.save(entity);
        created[index++] = entity;
        
        // Log creation
        LOGGER.info("Created {Entity} '{}' (ID: {})", entity.getName(), entity.getId());
        
        // Support minimal mode
        if (minimal) {
            break;
        }
    }
    
    LOGGER.debug("Created {} sample {entity}(s) for project: {}", index, project.getName());
    return created; // Optional: return if needed for hierarchy
    
} catch (final Exception e) {
    LOGGER.error("Error initializing sample {entities} for project: {}", project.getName(), e);
    throw new RuntimeException("Failed to initialize sample {entities}: " + project.getName(), e);
}
```

---

## Complete Example: Decision Entity

```java
public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
    // Seed data for sample decisions
    record DecisionSeed(String name, String description, String estimatedCost, int implementationDays, int reviewDays) {}
    
    final List<DecisionSeed> seeds = List.of(
        new DecisionSeed("Adopt Cloud-Native Architecture",
            "Strategic decision to migrate to cloud-native architecture", "50000.00", 30, 90),
        new DecisionSeed("Implement Agile Methodology",
            "Transition from waterfall to agile development", "25000.00", 15, 60)
    );
    
    try {
        final CDecisionService decisionService = CSpringContext.getBean(CDecisionService.class);
        final CDecisionTypeService decisionTypeService = CSpringContext.getBean(CDecisionTypeService.class);
        final CProjectItemStatusService statusService = CSpringContext.getBean(CProjectItemStatusService.class);
        final CUserService userService = CSpringContext.getBean(CUserService.class);
        
        int index = 0;
        for (final DecisionSeed seed : seeds) {
            final CDecisionType type = decisionTypeService.getRandom(project.getCompany());
            final CProjectItemStatus status = statusService.getRandom(project.getCompany());
            final CUser user = userService.getRandom(project.getCompany());
            
            final CDecision decision = new CDecision(seed.name(), project);
            decision.setDescription(seed.description());
            decision.setEntityType(type);
            decision.setStatus(status);
            decision.setAssignedTo(user);
            decision.setEstimatedCost(new BigDecimal(seed.estimatedCost()));
            decision.setImplementationDate(LocalDateTime.now().plusDays(seed.implementationDays()));
            decision.setReviewDate(LocalDateTime.now().plusDays(seed.reviewDays()));
            decisionService.save(decision);
            
            index++;
            if (minimal) {
                break;
            }
        }
        
        LOGGER.debug("Created {} sample decision(s) for project: {}", index, project.getName());
    } catch (final Exception e) {
        LOGGER.error("Error initializing sample decisions for project: {}", project.getName(), e);
        throw new RuntimeException("Failed to initialize sample decisions: " + project.getName(), e);
    }
}
```

---

## Anti-Patterns to Avoid

### ❌ WRONG: Duplicate Code Blocks

```java
// DON'T DO THIS
final CEntity entity1 = new CEntity("Name 1", project);
entity1.setDescription("Description 1");
service.save(entity1);

if (!minimal) {
    final CEntity entity2 = new CEntity("Name 2", project);  // ❌ Duplicate code
    entity2.setDescription("Description 2");
    service.save(entity2);
}
```

### ❌ WRONG: Logic Mixed with Data

```java
// DON'T DO THIS
for (int i = 0; i < 2; i++) {
    String name = (i == 0) ? "First" : "Second";  // ❌ Data in logic
    // ...
}
```

### ✅ CORRECT: Seed Data with Loop

```java
record EntitySeed(String name, String description) {}
final List<EntitySeed> seeds = List.of(
    new EntitySeed("First", "First description"),
    new EntitySeed("Second", "Second description")
);

for (final EntitySeed seed : seeds) {
    // Use seed.name() and seed.description()
    if (minimal) break;
}
```

---

## Hierarchical Entity Pattern

For entities with parent dependencies (Epic → Feature → UserStory → Activity):

```java
public static CFeature[] initializeSample(final CProject<?> project, final boolean minimal, 
        final CEpic parentEpic1, final CEpic parentEpic2) throws Exception {
    
    // Seed with parent index
    record FeatureSeed(String name, String description, int parentEpicIndex) {}
    
    final List<FeatureSeed> seeds = List.of(
        new FeatureSeed("Notifications", "Real-time notification system", 0),
        new FeatureSeed("Search", "Advanced search capabilities", 1)
    );
    
    try {
        // Services...
        final CEpic[] parentEpics = { parentEpic1, parentEpic2 };
        final CFeature[] createdFeatures = new CFeature[2];
        int index = 0;
        
        for (final FeatureSeed seed : seeds) {
            // Create entity...
            
            // Link to parent using index
            final CEpic parentEpic = parentEpics[seed.parentEpicIndex()];
            if (parentEpic != null) {
                feature.setParentEpic(parentEpic);
            } else if (parentEpic1 != null) {
                feature.setParentEpic(parentEpic1);  // Fallback
            }
            
            feature = service.save(feature);
            createdFeatures[index++] = feature;
            
            if (minimal) break;
        }
        
        return createdFeatures;
    } catch (final Exception e) {
        // Error handling...
    }
}
```

---

## Checklist

Before committing, verify:

- [ ] ✅ Method is `public static`
- [ ] ✅ Seed data defined as Java `record`
- [ ] ✅ Seed list defined with `List.of()`
- [ ] ✅ Loop used (not duplicate code blocks)
- [ ] ✅ Minimal mode supported (`if (minimal) break;`)
- [ ] ✅ Try-catch wrapper with logging
- [ ] ✅ Services via `CSpringContext.getBean()`
- [ ] ✅ LOGGER.info for each created entity
- [ ] ✅ LOGGER.debug for summary
- [ ] ✅ JavaDoc comment
- [ ] ✅ Return array if needed for hierarchy

---

## Integration Pattern

In `CDataInitializer.loadSampleData()`:

```java
// Independent entities
CDecisionInitializerService.initializeSample(project, minimal);
CMeetingInitializerService.initializeSample(project, minimal);

// Hierarchical entities - maintain order, pass parent arrays
final CEpic[] epics = CEpicInitializerService.initializeSample(project, minimal);
final CFeature[] features = CFeatureInitializerService.initializeSample(
    project, minimal, epics[0], epics[1]);
final CUserStory[] userStories = CUserStoryInitializerService.initializeSample(
    project, minimal, features[0], features[1]);
CActivityInitializerService.initializeSample(
    project, minimal, userStories[0], userStories[1]);
```

**CRITICAL**: Parent entities MUST be created before children.

---

## Benefits

1. ✅ **Single Source of Truth**: All data in seed list
2. ✅ **DRY**: No code duplication  
3. ✅ **Easy Updates**: Change data without touching logic
4. ✅ **Consistent**: Same structure across all entities
5. ✅ **Testable**: Clear separation of concerns
6. ✅ **Maintainable**: Data changes don't affect logic
7. ✅ **Scalable**: Easy to add samples or fields

---

## References

- `CCompanyInitializerService.initializeSample()` - Reference implementation
- `CEpicInitializerService.initializeSample()` - Hierarchical pattern
- `CDecisionInitializerService.initializeSample()` - Simple pattern
- `docs/AGENTS.md` - Overall coding standards
