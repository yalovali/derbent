# CDynamicSingleEntityPageView Usage Guide

## Overview

The `CDynamicSingleEntityPageView` class is designed for displaying single entity pages without grids. This is ideal for:
- System settings pages
- User profile pages  
- Company information pages
- Any scenario where there's only one item per user/project/application

## Key Features

1. **Grid Validation**: Only works with `pageEntity.getGridEntity().getAttributeNone() == true`
2. **Single Entity Display**: Shows only the details section (no grid)
3. **Configurable CRUD Toolbar**: Parametric button visibility control
4. **Data Source Validation**: Shows warning if multiple items exist, displays first item

## Usage Example

### 1. Create Page Entity with AttributeNone Grid

```java
// In an initializer service (like CSystemSettingsInitializerService)
public static void initialize(CProject project, CGridEntityService gridEntityService,
        CDetailSectionService detailSectionService, CPageEntityService pageEntityService) throws Exception {
    
    CDetailSection detailSection = createBasicView(project);
    detailSectionService.save(detailSection);
    
    // Create single entity grid (attributeNone = true)
    CGridEntity singleGrid = createGridEntity(project, true);
    singleGrid.setName("System Settings Single View");
    gridEntityService.save(singleGrid);
    
    // Create page entity
    CPageEntity singlePage = createPageEntity(clazz, project, singleGrid, detailSection, 
        "System.Current Settings", "System Settings", "System-wide configuration settings");
    pageEntityService.save(singlePage);
}

public static CGridEntity createGridEntity(final CProject project, boolean attributeNone) {
    final CGridEntity grid = createBaseGridEntity(project, clazz);
    grid.setAttributeNone(attributeNone); // This is required for single entity view
    grid.setSelectedFields("field1,field2,field3");
    return grid;
}
```

### 2. Use CDynamicSingleEntityPageView

```java
// In a router or view factory
@Route("system-settings")
public class SystemSettingsPage extends Div implements BeforeEnterObserver {
    
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Load the page entity
        CPageEntity pageEntity = pageEntityService.findByRoute("system-settings")
            .orElseThrow(() -> new IllegalStateException("System settings page not found"));
        
        // Create single entity view
        CDynamicSingleEntityPageView singleView = new CDynamicSingleEntityPageView(
            pageEntity, sessionService, detailSectionService, gridEntityService, applicationContext);
        
        // Configure CRUD toolbar (disable delete and new buttons)
        singleView.configureCrudToolbar(false, false, true, true);
        
        removeAll();
        add(singleView);
    }
}
```

### 3. Configuration Options

```java
// Configure CRUD toolbar buttons
view.configureCrudToolbar(
    false,  // enableDelete - hide delete button
    false,  // enableNew - hide new button  
    true,   // enableSave - show save button
    true    // enableReload - show reload/cancel button
);
```

## Validation

The `CDynamicSingleEntityPageView` performs strict validation:

- Throws `IllegalArgumentException` if `pageEntity.getGridEntity().getAttributeNone() != true`
- Logs warning if data source returns more than 1 item
- Always displays the first item from the data source

## Implementation Notes

1. **Inheritance**: Extends `CDynamicPageViewWithSections` 
2. **No Grid**: Creates only details section, skips grid creation
3. **Full Layout**: Details section takes full page space
4. **Button Configuration**: Uses reflection to control button visibility

## Example Initializer Services

See:
- `CCompanyInitializerService.java` - Creates both grid and single entity views
- `CSystemSettingsInitializerService.java` - Updated to support single entity pages

Both follow the pattern of creating two page entities:
1. Standard grid view (`attributeNone = false`)
2. Single entity view (`attributeNone = true`)