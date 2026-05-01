package tech.derbent.api.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.dashboard.dashboardprojecttype.domain.CDashboardProjectType;
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
public abstract class CDashboardProject<EntityClass> extends CProjectItem<EntityClass, CDashboardProjectType> {

	@ManyToOne (fetch = FetchType.EAGER)
	@JoinColumn (name = "entitytype_id", nullable = true)
	@AMetaData (
			displayName = "Dashboard Project Type", required = false, readOnly = false,
			description = "Type category of this dashboard project", hidden = false, dataProviderBean = "service",
			setBackgroundFromColor = true, useIcon = true
	)
	private CDashboardProjectType entityType;

	// Dashboard widget configuration field
    @Column(name = "dashboard_widget", length = 100)
    @AMetaData(
        displayName = "Dashboard Widget",
        required = false,
        readOnly = false,
        description = "Dashboard widget configuration for this dashboard project",
        hidden = false,
        maxLength = 100
        // Removed createComponentMethod since no service provides the component
    )
    private String dashboardWidget;
    
    /** Default constructor for JPA. */
    protected CDashboardProject() {
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
	@Override
	public CDashboardProjectType getEntityType() { return entityType; }

    public String getDashboardWidget() { return dashboardWidget; }

	@Override
	public void setEntityType(final CDashboardProjectType entityType) {
		this.entityType = entityType;
		updateLastModified();
	}

    public void setDashboardWidget(final String dashboardWidget) {
        this.dashboardWidget = dashboardWidget;
        updateLastModified();
    }
}