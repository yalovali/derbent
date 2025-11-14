# Module Derbent Project Management System

Derbent is a comprehensive project management application built with Java 21, Spring Boot 3.5, and Vaadin 24.8.

## Package Overview

### tech.derbent.abstracts
Core abstract classes, base entities, and utilities that provide the foundation for the application.
Includes base classes for services, repositories, pages, and database entities.

### tech.derbent.activities
Activity management module - handles project activities, tasks, and their lifecycle.
Core feature for project tracking and management.

### tech.derbent.projects
Project management module - main entity for organizing work.
Provides project creation, editing, and tracking capabilities.

### tech.derbent.users
User management and authentication module.
Handles user accounts, roles, permissions, and profile management.

### tech.derbent.meetings
Meeting management module - scheduling and tracking of project meetings.
Includes meeting minutes, attendees, and follow-up actions.

### tech.derbent.companies
Company/organization management module.
Multi-tenant support for different companies using the system.

### tech.derbent.setup
System configuration and settings module.
Handles application-wide settings and preferences.

### tech.derbent.administration
Administrative functions and company-specific settings.
Configuration and maintenance tools for administrators.

## Architecture

The application follows a layered MVC architecture:

- **Domain Layer**: Entity classes (C-prefixed classes extending CEntityDB)
- **Repository Layer**: Data access interfaces (extending CAbstractRepository)
- **Service Layer**: Business logic (extending CAbstractService)
- **View Layer**: UI components (Vaadin Flow components, C-prefixed pages)

## Naming Conventions

All domain classes are prefixed with "C" (e.g., CActivity, CUser, CProject).
This convention helps distinguish domain entities from framework classes.

## Key Technologies

- Java 21
- Spring Boot 3.5
- Vaadin Flow 24.8
- Hibernate/JPA
- PostgreSQL/H2

## Documentation Generation

This documentation is generated using Dokka with full inheritance and call graph support.
