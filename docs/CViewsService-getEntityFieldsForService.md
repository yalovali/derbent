# CViewsService - getEntityFieldsForService Method

## Overview

The `getEntityFieldsForService` method has been added to `CViewsService` to provide dynamic field extraction from entity classes based on service bean names.

## Usage

```java
@Autowired
private CViewsService viewsService;

// Get field names for CActivity entity through CActivityService
List<String> activityFields = viewsService.getEntityFieldsForService("CActivityService");

// Example output: [activityType, estimatedHours, actualHours, remainingHours, status, priority, progressPercentage, startDate, dueDate, completionDate, estimatedCost, actualCost, hourlyRate, acceptanceCriteria, notes, results]
```

## Method Signature

```java
public List<String> getEntityFieldsForService(String serviceBeanName)
```

### Parameters
- `serviceBeanName`: The name of the service bean (e.g., "CActivityService", "CProjectService", "CUserService")

### Returns
- `List<String>`: List of field names from the corresponding entity class, or empty list if service not found or not valid

## How It Works

1. **Service Discovery**: Uses Spring's `ApplicationContext` to get the service bean by name
2. **Entity Class Extraction**: Calls the service's `getEntityClass()` method to get the corresponding entity class
3. **Field Reflection**: Uses Java reflection to extract declared fields from the entity class
4. **Filtering**: Excludes static fields, LOGGER fields, and internal fields (starting with "$")

## Example Service-to-Entity Mappings

| Service Bean Name | Entity Class | Example Fields |
|-------------------|--------------|----------------|
| CActivityService | CActivity | activityType, status, estimatedHours, startDate, dueDate |
| CProjectService | CProject | name, description, startDate, endDate |
| CUserService | CUser | username, email, firstName, lastName |
| CMeetingService | CMeeting | meetingType, status, startTime, endTime |

## Error Handling

The method gracefully handles various error conditions:

- **Null/Empty Input**: Returns empty list for null or empty service name
- **Missing ApplicationContext**: Returns empty list if Spring context is not available (e.g., in unit tests)
- **Non-existent Service**: Returns empty list if the service bean is not found
- **Invalid Service Type**: Returns empty list if the bean doesn't extend `CAbstractService`
- **Reflection Errors**: Logs errors and returns empty list if field extraction fails

## Integration with Existing Methods

This method complements the existing `getAvailableBeans()` method:

```java
// Get all available service beans
List<String> availableServices = viewsService.getAvailableBeans();

// For each service, get its entity fields
for (String serviceName : availableServices) {
    List<String> fields = viewsService.getEntityFieldsForService(serviceName);
    System.out.println(serviceName + " -> " + fields);
}
```

## Requirements

- Service classes must extend `CAbstractService<EntityClass>`
- Service classes must implement the `getEntityClass()` method
- Entity classes should follow the standard Derbent domain class pattern
- Spring ApplicationContext must be available for the method to work

## Testing

The functionality is tested with:
- Unit tests for edge cases and error handling
- Integration tests for real Spring context scenarios
- Manual tests demonstrating field extraction from actual entity classes

See `CViewsServiceTest` and `ManualFieldExtractionTest` for examples.