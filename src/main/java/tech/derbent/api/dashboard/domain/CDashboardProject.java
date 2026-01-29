package tech.derbent.api.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.projects.domain.CProject;

/**
 * CDashboardProject - Abstract base class for dashboard project entities.
 * Layer: Domain (MVC)
 * 
 * Following Derbent pattern: @MappedSuperclass for inheritance.
 * This is a PROJECT ENTITY (extends CProjectItem), NOT a project type!
 * 
 * Used for visual dashboard displays similar to Kanban boards.
 * Provides dashboard widget management for project-scoped dashboard entities.
 */
@MappedSuperclass  // Abstract entities are @MappedSuperclass
public abstract class CDashboardProject<EntityClass> extends CProjectItem<EntityClass> {
    
    // Dashboard widget configuration field
    @Column(name = "dashboard_widget", length = 100)
    @AMetaData(
        displayName = "Dashboard Widget",
        required = false,
        readOnly = false,
        description = "Dashboard widget configuration for this dashboard project",
        hidden = false,
        maxLength = 100,
        createComponentMethod = "createDashboardWidget"
    )
    private String dashboardWidget;
    
    /** Default constructor for JPA. */
    protected CDashboardProject() {
        super();
        // Abstract JPA constructors do NOT call initializeDefaults() (RULE 1)
    }
    
    protected CDashboardProject(Class<EntityClass> clazz, String name, CProject<?> project) {
        super(clazz, name, project);
        // Abstract constructors do NOT call initializeDefaults()
        // Concrete subclasses will call initializeDefaults() which will chain to abstract implementation
    }
    
    // Abstract initializeDefaults - implemented by subclasses
    // No implementation here - each concrete class implements
    
    // Common getters/setters
    public String getDashboardWidget() { return dashboardWidget; }
    public void setDashboardWidget(String dashboardWidget) { 
        this.dashboardWidget = dashboardWidget;
        updateLastModified();
    }
}