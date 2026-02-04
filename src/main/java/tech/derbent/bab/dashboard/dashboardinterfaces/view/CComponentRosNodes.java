package tech.derbent.bab.dashboard.dashboardinterfaces.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.ui.component.basic.CH4;
import tech.derbent.api.ui.component.basic.CSpan;
import tech.derbent.bab.dashboard.dashboardproject_bab.service.CAbstractCalimeroClient;
import tech.derbent.bab.http.clientproject.domain.CClientProject;
import tech.derbent.bab.uiobjects.view.CComponentBabBase;
import tech.derbent.base.session.service.ISessionService;

public class CComponentRosNodes extends CComponentBabBase {
    public static final String ID_ROOT = "custom-ros-nodes-component";
    private static final Logger LOGGER = LoggerFactory.getLogger(CComponentRosNodes.class);
    private static final long serialVersionUID = 1L;

    public CComponentRosNodes(final ISessionService sessionService) {
        super(sessionService);
        initializeComponents();
    }

    @Override
    protected void initializeComponents() {
        setId(ID_ROOT);
        final CH4 title = new CH4("ROS Nodes");
        final CSpan placeholder = new CSpan("ROS node configuration - implementation in progress");
        placeholder.getStyle().set("color", "var(--lumo-secondary-text-color)");
        add(title, placeholder);
        LOGGER.debug("Initialized ROS nodes component (placeholder)");
    }

    @Override
    protected void refreshComponent() {
        LOGGER.debug("Refreshing ROS nodes component");
    }

    @Override
    protected CAbstractCalimeroClient createCalimeroClient(CClientProject clientProject) {
        return null;
    }

    @Override
    protected String getHeaderText() {
        return "ROS Nodes";
    }

    @Override
    public ISessionService getSessionService() {
        return sessionService;
    }
}