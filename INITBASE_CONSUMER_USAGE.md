# initBase Consumer Parameter Usage Guide

## Overview

The `initBase` method in `CInitializerServiceBase` now includes a `Consumer<CPageEntity>` parameter that allows you to customize the `CPageEntity` before it is saved.

## Quick Start - Most Common Use Case

**Hide the CRUD toolbar** (most common use case):

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    // Hide CRUD toolbar - common for read-only or specialized pages
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, page -> {
            page.setAttributeHideTopCrudtoolbar(true);
        });
}
```

## Method Signature

```java
public static void initBase(
    final Class<?> clazz, 
    final CProject<?> project, 
    final CGridEntityService gridEntityService,
    final CDetailSectionService detailSectionService, 
    final CPageEntityService pageEntityService, 
    final CDetailSection detailSection,
    final CGridEntity grid, 
    final String menuTitle, 
    final String pageTitle, 
    final String pageDescription, 
    final boolean showInQuickToolbar,
    final String order, 
    final Consumer<CPageEntity> pageCustomizer) // <-- NEW PARAMETER
```

## Usage Examples

### Example 1: No Customization (Pass null)

Most initializer services don't need customization, so pass `null`:

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    // No customization needed - pass null
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, null);
}
```

### Example 2: Hide CRUD Toolbar

A common use case is to hide the top CRUD toolbar for read-only or specialized pages:

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    // Hide CRUD toolbar for this page
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, page -> {
            page.setAttributeHideTopCrudtoolbar(true);
        });
}
```

### Example 3: Multiple Page Customizations

You can combine multiple customizations in the consumer:

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    // Multiple customizations
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, page -> {
            page.setAttributeHideTopCrudtoolbar(true);
            page.setAttributeReadonly(true);
            page.setIcon("vaadin:cog");
            page.setColor("#FF5722");
        });
}
```

### Example 4: Conditional Customization

You can also use conditional logic in the consumer:

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, page -> {
            // Conditional customization based on project type
            if (project instanceof CProject_Bab) {
                page.setAttributeHideTopCrudtoolbar(true);
                page.setDescription("BAB Gateway - " + pageDescription);
            } else {
                page.setAttributeShowInQuickToolbar(true);
            }
        });
}
```

### Example 5: Using Helper Methods

For complex customizations, you can extract to helper methods:

```java
public static void initialize(final CProject<?> project, 
        final CGridEntityService gridEntityService,
        final CDetailSectionService detailSectionService, 
        final CPageEntityService pageEntityService) throws Exception {
    final CDetailSection detailSection = createBasicView(project);
    final CGridEntity grid = createGridEntity(project);
    
    // Use method reference to helper
    initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, 
        detailSection, grid, menuTitle, pageTitle, pageDescription, 
        showInQuickToolbar, menuOrder, CMyInitializerService::customizePageSettings);
}

private static void customizePageSettings(final CPageEntity page) {
    page.setAttributeHideTopCrudtoolbar(true);
    page.setAttributeReadonly(true);
    page.setIcon("vaadin:cog");
    // More complex customization logic...
}
```

## Common Use Cases

### 1. Hide CRUD Toolbar
```java
page -> page.setAttributeHideTopCrudtoolbar(true)
```

### 2. Make Page Read-Only
```java
page -> page.setAttributeReadonly(true)
```

### 3. Set Custom Icon
```java
page -> page.setIcon("vaadin:custom-icon")
```

### 4. Add Custom CSS Class
```java
page -> page.setCssClass("custom-page-style")
```

### 5. Multiple Customizations
```java
page -> {
    page.setAttributeHideTopCrudtoolbar(true);
    page.setAttributeShowInQuickToolbar(false);
    page.setIcon("vaadin:wrench");
    page.setColor("#FF5722");
}
```

## Benefits

1. **Flexibility**: Each initializer can customize its page without modifying the base method
2. **Optional**: Pass `null` when no customization is needed (most cases)
3. **Clean Code**: Keeps customization logic in the initializer service where it belongs
4. **Type-Safe**: Consumer provides compile-time safety

## Migration

All existing calls to `initBase` have been updated to pass `null` as the last parameter, maintaining backward compatibility.
