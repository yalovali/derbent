# CDataProviderResolver Parameter Pattern Usage Example

This document demonstrates how the enhanced CDataProviderResolver works with parameter patterns.

## Example: CPageEntity gridEntity Field

The `CPageEntity.gridEntity` field demonstrates the parameter pattern:

```java
@ManyToOne
@JoinColumn (name = "grid_entity_id")
@AMetaData (
    displayName = "Grid Entity", 
    required = false, 
    readOnly = false, 
    description = "Grid entity configuration for this page", 
    hidden = false,
    dataProviderMethod = "listForComboboxSelector", 
    dataProviderBean = "CGridEntityService", 
    dataProviderParamMethod = "getProject",
    order = 98
)
private CGridEntity gridEntity;
```

## How It Works

1. **Parameter Resolution**: When resolving data for the gridEntity field, CDataProviderResolver:
   - Calls `getProject()` method on the service bean to get the parameter value
   - Passes this parameter to the data provider method

2. **Method Call Hierarchy**: The resolver tries these method signatures in order:
   ```java
   // 1. With parameter and Pageable
   listForComboboxSelector(Object param, Pageable pageable)
   
   // 2. With parameter only  
   listForComboboxSelector(Object param)
   
   // 3. With Pageable only (original behavior)
   listForComboboxSelector(Pageable pageable)
   
   // 4. Without parameters (fallback)
   listForComboboxSelector()
   ```

3. **Service Implementation Example**:
   ```java
   @Service
   public class CGridEntityService {
       
       public String getProject() {
           // Returns current project context
           return sessionService.getCurrentProject().getId();
       }
       
       public List<CGridEntity> listForComboboxSelector(Object projectId, Pageable pageable) {
           // Filter grid entities by project
           return gridEntityRepository.findByProjectId((String) projectId, pageable);
       }
       
       public List<CGridEntity> listForComboboxSelector(Object projectId) {
           // Filter grid entities by project (no pagination)
           return gridEntityRepository.findByProjectId((String) projectId);
       }
       
       public List<CGridEntity> listForComboboxSelector(Pageable pageable) {
           // Return all grid entities (fallback)
           return gridEntityRepository.findAll(pageable);
       }
   }
   ```

## Benefits

- **Context-Aware Filtering**: Data can be filtered based on current context (e.g., current project)
- **Backward Compatibility**: Existing services without parameter support continue to work
- **Flexible Implementation**: Services can implement any combination of the method signatures
- **Performance**: Built-in caching for method resolution reduces reflection overhead

## Usage in Other Entities

You can use this pattern in any entity field:

```java
@ManyToOne
@AMetaData (
    displayName = "Activity", 
    dataProviderMethod = "listActiveForProject", 
    dataProviderBean = "CActivityService", 
    dataProviderParamMethod = "getCurrentProjectId"
)
private CActivity activity;
```

This enhancement enables more sophisticated data provider scenarios while maintaining simplicity for basic use cases.