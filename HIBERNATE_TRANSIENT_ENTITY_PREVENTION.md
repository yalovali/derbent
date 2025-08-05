# Hibernate Transient Entity Prevention Guidelines

## Overview
This document outlines coding rules and best practices to prevent `org.hibernate.TransientPropertyValueException` errors that occur when trying to save entities that reference unsaved (transient) related entities.

## The Problem
The error occurs when:
1. Creating an entity (Entity A) that has a relationship to another entity (Entity B)
2. Entity B exists in memory but hasn't been saved to the database (transient state)
3. Attempting to save Entity A with a reference to the transient Entity B

**Example Error:**
```
org.hibernate.TransientPropertyValueException: Not-null property references a transient value - transient instance must be saved before current operation: tech.derbent.comments.domain.CComment.activity -> tech.derbent.activities.domain.CActivity
```

## Root Cause Fixed
In `CSampleDataInitializer`, activities were being created and configured but not saved before comments were created that referenced them. This violated Hibernate's requirement that referenced entities must be persistent.

## Coding Rules to Prevent This Issue

### 1. Always Save Referenced Entities First
```java
// ❌ WRONG: Creating comments before saving the activity
CActivity activity = new CActivity("My Activity", project);
activity.setDescription("Some description");
// Missing: activityService.save(activity);
commentService.createComment("My comment", activity, user); // This will fail!

// ✅ CORRECT: Save the activity first
CActivity activity = new CActivity("My Activity", project);
activity.setDescription("Some description");
activityService.save(activity); // Save first!
commentService.createComment("My comment", activity, user); // Now this works
```

### 2. Follow Proper Entity Creation Pattern
For any method that creates entities with relationships:
```java
private void createSampleEntity() {
    // 1. Create and configure the main entity
    CActivity activity = new CActivity("Name", project);
    activity.setProperty1(value1);
    activity.setProperty2(value2);
    
    // 2. Save the main entity BEFORE creating related entities
    activityService.save(activity);
    
    // 3. Now create related entities that reference the saved entity
    commentService.createComment("Comment 1", activity, user);
    commentService.createComment("Comment 2", activity, user);
}
```

### 3. Batch Save Pattern
When creating multiple related entities:
```java
// Create and save all main entities first
CActivity activity1 = new CActivity("Activity 1", project);
CActivity activity2 = new CActivity("Activity 2", project);
activityService.save(activity1);
activityService.save(activity2);

// Then create all related entities
commentService.createComment("Comment for activity 1", activity1, user);
commentService.createComment("Comment for activity 2", activity2, user);
```

### 4. Database Cleanup for Restart Safety
When implementing sample data initialization:
```java
@Override
public void run(ApplicationArguments args) {
    // Check for existing data and handle appropriately
    if (!isDatabaseEmpty() && !args.containsOption("force-init")) {
        return; // Skip if data exists
    }
    
    // Clear existing data if force init requested
    if (args.containsOption("force-init")) {
        clearSampleData(); // Clear in reverse dependency order
    }
    
    loadSampleData();
}
```

## Test Strategy

### 1. Unit Tests
Create tests that validate entity creation patterns:
```java
@Test
void testEntityCreationPattern() {
    // Create main entity
    CActivity activity = new CActivity("Test", project);
    
    // Save it first
    activityService.save(activity);
    
    // Then create dependent entities
    assertDoesNotThrow(() -> {
        commentService.createComment("Test comment", activity, user);
    });
}
```

### 2. Integration Tests
Test the complete sample data initialization process:
```java
@Test
void testSampleDataInitializationDoesNotFailWithTransientEntities() {
    assertDoesNotThrow(() -> {
        sampleDataInitializer.loadSampleData();
    });
}
```

## Detection Strategies

### 1. Code Review Checklist
- [ ] Are all entities saved before creating dependent entities?
- [ ] Is the dependency order correct in initialization methods?
- [ ] Are there any `commentService.createComment()` calls without preceding `activityService.save()`?
- [ ] Does the cleanup logic handle entity dependencies correctly?

### 2. Static Analysis Rules
Consider implementing custom rules that detect:
- Method calls to `createComment()` without preceding `save()` calls
- Entity creation patterns that violate the save-first principle

### 3. Runtime Validation
Add logging or assertions to catch transient entity issues:
```java
public CComment createComment(String text, CActivity activity, CUser author) {
    if (activity.getId() == null) {
        throw new IllegalArgumentException("Activity must be saved before creating comments");
    }
    // ... rest of method
}
```

## Files Modified in This Fix
- `CSampleDataInitializer.java` - Added 9 `activityService.save()` calls
- Added `clearSampleData()` method for restart safety
- Added force initialization flag support
- Created test to validate the fix

## Prevention Summary
1. **Always save entities before creating dependent entities**
2. **Follow proper entity creation patterns**
3. **Implement proper database cleanup for restarts**
4. **Create tests that validate entity relationships**
5. **Use code review checklists to catch these issues**

By following these guidelines, similar Hibernate transient entity issues can be prevented in the future.