# Non-Project Entity Initializer Pattern

## Overview

This document describes the new pattern implemented for initializing entities that are not scoped to specific projects in the Derbent system.

## Problem

Previously, all entity initializers extended `CInitializerServiceBase`, which assumed all entities were project-scoped. However, some entities like `CCompanySettings` and `CCompany` are global/system-wide and should not be tied to specific projects.

## Solution

### New Base Class: CNonProjectInitializerServiceBase

A new abstract base class `CNonProjectInitializerServiceBase` has been created that provides similar functionality to `CInitializerServiceBase` but without project dependencies.

**Key differences:**
- Creates grid entities without project association (`null` project)
- Creates detail sections without project association 
- Creates page entities without project association
- No project parameter required in initialization methods

### Implementation Pattern

1. **Domain Entity Requirements:**
   - Must have `public static final String VIEW_NAME` constant
   - Should be a global/system-wide entity (not project-scoped)

2. **Initializer Service Structure:**
   ```java
   public class CEntityInitializerService extends CNonProjectInitializerServiceBase {
       public static final String BASE_PANEL_NAME = "Entity Information";
       static final Class<?> clazz = CEntity.class;
       
       public static CDetailSection createBasicView() {
           // Create detail section without project
       }
       
       public static CGridEntity createGridEntity() {
           // Create grid entity without project
       }
       
       public static void initialize(CGridEntityService gridEntityService, 
           CDetailSectionService detailSectionService,
           CPageEntityService pageEntityService, 
           boolean showInQuickToolbar) throws Exception {
           // Initialize without project parameter
       }
   }
   ```

3. **Integration with CDataInitializer:**
   ```java
   // In loadSampleData() method:
   // ========== NON-PROJECT RELATED INITIALIZATION PHASE ==========
   CEntityInitializerService.initialize(gridEntityService, screenService, pageEntityService, false);
   ```

## Implemented Examples

### 1. CCompanySettingsInitializerService
- Creates dynamic page for company settings management
- Organizes fields into logical sections (Basic Info, Time Settings, Notifications, etc.)
- No project dependency since company settings are global

### 2. CCompanyNonProjectInitializerService
- Alternative to existing project-scoped `CCompanyInitializerService`
- Creates global company management page
- Demonstrates pattern application to existing entities

## Benefits

1. **Proper Separation of Concerns:** Non-project entities are no longer forced into project scope
2. **Inheritance Pattern:** Reusable base class for similar entities
3. **Backward Compatibility:** Existing project-scoped initializers remain unchanged
4. **Dynamic Page Support:** Non-project entities get proper dynamic page configuration
5. **Code Reuse:** Avoids duplicating initialization logic

## Usage Guidelines

- Use `CNonProjectInitializerServiceBase` for entities that are:
  - Company-wide settings or configurations
  - System-wide metadata or reference data
  - Global administrative entities
  - Any entity that exists independently of projects

- Continue using `CInitializerServiceBase` for entities that are:
  - Project-specific (activities, meetings, decisions, etc.)
  - Project-scoped reference data (types, statuses within projects)
  - Any entity that belongs to or is filtered by project

## Future Considerations

This pattern can be applied to other non-project entities such as:
- System configurations
- Global user preferences
- Application-wide metadata
- Integration settings
- Security configurations

## Migration Strategy

When migrating existing project-scoped entities to non-project:
1. Verify the entity is truly global (not project-specific)
2. Create new non-project initializer service alongside existing one
3. Update CDataInitializer to call non-project version
4. Gradually phase out project-scoped version if appropriate
5. Update corresponding views if they need to be non-project aware