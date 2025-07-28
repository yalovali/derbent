# Auxiliary Service Methods Usage Guide

This document explains how to use the auxiliary service methods that have been added to follow the coding guidelines in `copilot-java-strict-coding-rules.md`.

## Overview

Following the coding guidelines, auxiliary functions have been added to service classes to handle common entity setup patterns instead of directly setting fields or loading everything in constructors. These methods provide better separation of concerns and more maintainable code.

## Available Auxiliary Methods

### CActivityService Auxiliary Methods

#### Basic Activity Setup
```java
// Set activity type and description
CActivity activity = activityService.setActivityType(activity, activityType, "Description");

// Set assigned users
CActivity activity = activityService.setAssignedUsers(activity, assignedUser, creatorUser);

// Set time tracking information
CActivity activity = activityService.setTimeTracking(activity, 
    new BigDecimal("40.00"), new BigDecimal("35.50"), new BigDecimal("4.50"));
```

#### Advanced Activity Setup
```java
// Set status and priority
CActivity activity = activityService.setStatusAndPriority(activity, status, priority, 75);

// Set date information
CActivity activity = activityService.setDateInfo(activity, startDate, dueDate, completionDate);

// Set budget information
CActivity activity = activityService.setBudgetInfo(activity, estimatedCost, actualCost, hourlyRate);

// Set additional information
CActivity activity = activityService.setAdditionalInfo(activity, criteria, notes, parentActivity);
```

#### Comprehensive Setup
```java
// Set multiple properties in one call
CActivity activity = activityService.setFullActivityInfo(activity, activityType, 
    description, assignedUser, estimatedHours, startDate, dueDate);
```

### CMeetingService Auxiliary Methods

#### Meeting Participants
```java
// Set participants
Set<CUser> participants = new HashSet<>();
participants.add(user1);
participants.add(user2);
CMeeting meeting = meetingService.setParticipants(meeting, participants);

// Set attendees (who actually attended)
CMeeting meeting = meetingService.setAttendees(meeting, attendeeSet);
```

#### Meeting Details
```java
// Set meeting details (type, dates, location)
CMeeting meeting = meetingService.setMeetingDetails(meeting, meetingType, 
    startDateTime, endDateTime, "Conference Room A");

// Set meeting content (agenda, related activity, responsible person)
CMeeting meeting = meetingService.setMeetingContent(meeting, agenda, relatedActivity, responsible);

// Set meeting status and notes
CMeeting meeting = meetingService.setMeetingStatus(meeting, status, minutes, linkedElement);
```

### CUserService Auxiliary Methods

#### User Profile Setup
```java
// Set user profile information
CUser user = userService.setUserProfile(user, lastname, phone, profilePictureData);

// Set company association
CUser user = userService.setCompanyAssociation(user, company);

// Set user role and permissions
CUser user = userService.setUserRole(user, userRole, securityRoles);
```

## Usage Examples from CSampleDataInitializer

### Activity Creation Example
```java
private void createBackendDevActivity() {
    final CProject project = findProjectByName("Digital Transformation Initiative");
    final CActivity backendDev = new CActivity("Backend API Development", project);
    
    // Use auxiliary methods instead of direct field setting
    activityService.setActivityType(backendDev, null, 
        "Develop REST API endpoints for user management and authentication");
    
    final CUser manager = findUserByLogin("jsmith");
    final CUser admin = findUserByLogin("admin");
    activityService.setAssignedUsers(backendDev, manager, admin);
    
    activityService.setTimeTracking(backendDev, 
        new BigDecimal("40.00"), new BigDecimal("35.50"), new BigDecimal("4.50"));
    
    activityService.setDateInfo(backendDev, 
        LocalDate.now().minusDays(10), LocalDate.now().plusDays(5), null);
}
```

### User Creation Example
```java
private void createAdminUser() {
    final CUser admin = userService.createLoginUser("admin", STANDARD_PASSWORD,
        "Administrator", "admin@system.com", "ADMIN,USER");
    
    // Use auxiliary methods for profile setup
    final byte[] profilePictureBytes = "profile-picture".getBytes();
    userService.setUserProfile(admin, "System", "+1-555-1001", profilePictureBytes);
    
    userService.setUserRole(admin, CUserRole.ADMIN, "ADMIN,USER");
    
    final CCompany company = findCompanyByName("TechNova Solutions");
    userService.setCompanyAssociation(admin, company);
}
```

### Meeting Creation Example
```java
private void createSampleProjectMeeting() {
    final CProject project = findProjectByName("Digital Transformation Initiative");
    final CMeeting meeting = new CMeeting("Weekly Project Status Meeting", project);
    
    // Set meeting details
    meetingService.setMeetingDetails(meeting, null, 
        LocalDateTime.now().plusDays(1).withHour(14).withMinute(0), 
        LocalDateTime.now().plusDays(1).withHour(15).withMinute(0), 
        "Conference Room A");
    
    // Set participants
    final Set<CUser> participants = new HashSet<>();
    participants.add(findUserByLogin("admin"));
    participants.add(findUserByLogin("jsmith"));
    meetingService.setParticipants(meeting, participants);
    
    // Set meeting content
    final CUser responsible = findUserByLogin("jsmith");
    meetingService.setMeetingContent(meeting, 
        "Weekly status update on project progress", null, responsible);
}
```

## Benefits

1. **Better Separation of Concerns**: Business logic is handled in the service layer
2. **Reusable Code**: Auxiliary methods can be used in multiple places
3. **Maintainable**: Centralized entity setup logic
4. **Follows Guidelines**: Avoids loading everything in constructors
5. **Proper Logging**: Each auxiliary method includes logging for debugging
6. **Error Handling**: Null checking and validation in service methods

## Adding New Auxiliary Methods

When adding new auxiliary methods, follow these patterns:

1. **Logging**: Always log method entry with parameters
2. **Null Checking**: Validate input parameters
3. **Transactional**: Use `@Transactional` for database operations
4. **Return Entity**: Return the modified entity for method chaining
5. **Error Handling**: Handle exceptions gracefully

### Template for New Auxiliary Method
```java
@Transactional
public CEntity setEntityProperty(final CEntity entity, final PropertyType property) {
    LOGGER.info("setEntityProperty called for entity: {} with property: {}",
        entity != null ? entity.getName() : "null", property);

    if (entity == null) {
        LOGGER.warn("Entity is null, cannot set property");
        return null;
    }

    if (property != null) {
        entity.setProperty(property);
    }

    return save(entity);
}
```

This approach ensures consistency with the coding guidelines and provides a maintainable foundation for future development.