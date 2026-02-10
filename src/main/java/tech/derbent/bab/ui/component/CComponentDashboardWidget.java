package tech.derbent.bab.ui.component;

import tech.derbent.api.ui.component.enhanced.CComponentBase;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.entity.service.CAbstractService;

/**
 * CComponentDashboardWidget - Abstract base class for dashboard widgets.
 * Layer: UI (MVC)
 * 
 * Following Derbent pattern: Abstract component extending CComponentBase.
 * Provides common dashboard widget functionality for project-level dashboards.
 */
public abstract class CComponentDashboardWidget extends CComponentBase<String> {
    
    private static final long serialVersionUID = 1L;
    
    // Services (injected)
    protected final CAbstractService<?> service;
    protected final ISessionService sessionService;
    
    /** Abstract constructor - initialized by concrete subclasses. */
    protected CComponentDashboardWidget(final CAbstractService<?> service, final ISessionService sessionService) {
        super();
        this.service = service;
        this.sessionService = sessionService;
        
        // Abstract components do NOT call initializeComponents()
        // Concrete subclasses will call initializeComponents()
    }
    
    // Abstract methods implemented by subclasses
    protected abstract void initializeComponents();
    protected abstract void loadData();
    protected abstract String getWidgetTitle();
    
    @Override
    protected void onValueChanged(final String oldValue, final String newValue, final boolean fromClient) {
        // Handle value changes if needed
        if (!fromClient) {
            loadData();
        }
    }
    
    // Helper methods for subclasses
    protected final void refreshData() {
        loadData();
    }
}