package tech.derbent.bab.dashboard.dashboardinterfaces.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.dashboardinterfaces.domain.CDashboardInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentInterfaceSummary;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentCanInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentEthernetInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentSerialInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentRosNodes;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentModbusInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentUsbInterfaces;
import tech.derbent.bab.dashboard.dashboardinterfaces.view.CComponentAudioDevices;
import tech.derbent.base.session.service.ISessionService;

/**
 * CPageServiceDashboardInterfaces - PageService for BAB interface configuration dashboard.
 * <p>
 * Layer: Service (MVC)
 * Profile: bab
 * <p>
 * Following Derbent pattern: Concrete PageService with @Profile annotation for BAB.
 * Provides specialized dashboard functionality for BAB interface configuration management.
 * <p>
 * Features:
 * <ul>
 * <li>Interface summary overview component</li>
 * <li>CAN interface configuration component</li>
 * <li>Ethernet interface settings component</li>
 * <li>Serial interface configuration component</li>
 * <li>ROS node management component</li>
 * <li>Modbus interface configuration component</li>
 * </ul>
 */
@Profile("bab")
public class CPageServiceDashboardInterfaces extends CPageServiceDynamicPage<CDashboardInterfaces> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceDashboardInterfaces.class);
    private final ISessionService sessionService;

    public CPageServiceDashboardInterfaces(final IPageServiceImplementer<CDashboardInterfaces> view) {
        super(view);
        // Initialize session service from Spring context (following CPageServiceActivity pattern)
        try {
            sessionService = tech.derbent.api.config.CSpringContext.getBean(ISessionService.class);
            LOGGER.debug("Initialized {} with session service", getClass().getSimpleName());
        } catch (final Exception e) {
            LOGGER.error("Failed to initialize session service for {}: {}", getClass().getSimpleName(), e.getMessage());
            throw new RuntimeException("Session service initialization failed", e);
        }
    }

    @Override
    public void actionReport() throws Exception {
        LOGGER.debug("Interface dashboard report action triggered");
        try {
            super.actionReport();
        } catch (final Exception e) {
            LOGGER.error("Error generating BAB interface dashboard report: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void bind() {
        try {
            LOGGER.debug("Binding {} to dynamic page for BAB interface dashboard entity {}.",
                    this.getClass().getSimpleName(),
                    CDashboardInterfaces.class.getSimpleName());
            Check.notNull(getView(), "View must not be null to bind BAB interface dashboard page service.");
            super.bind();
            // Initialize BAB-specific interface components
            initializeBabInterfaceComponents();
        } catch (final Exception e) {
            LOGGER.error("Error binding {} to dynamic page for BAB interface dashboard entity {}: {}",
                    this.getClass().getSimpleName(),
                    CDashboardInterfaces.class.getSimpleName(), e.getMessage());
            throw e;
        }
    }

    // Interface Summary Component
    public Component createComponentInterfaceSummary() {
        try {
            LOGGER.debug("Creating BAB interface summary component");
            final CComponentInterfaceSummary component = new CComponentInterfaceSummary(sessionService);
            LOGGER.debug("Created interface summary component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB interface summary component: {}", e.getMessage());
            CNotificationService.showException("Failed to load interface summary component", e);
            return CDiv.errorDiv("Failed to load interface summary component: " + e.getMessage());
        }
    }

    // CAN Interface Component
    public Component createComponentCanInterfaces() {
        try {
            LOGGER.debug("Creating BAB CAN interfaces component");
            final CComponentCanInterfaces component = new CComponentCanInterfaces(sessionService);
            LOGGER.debug("Created CAN interfaces component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB CAN interfaces component: {}", e.getMessage());
            CNotificationService.showException("Failed to load CAN interfaces component", e);
            return CDiv.errorDiv("Failed to load CAN interfaces component: " + e.getMessage());
        }
    }

    // Ethernet Interface Component
    public Component createComponentEthernetInterfaces() {
        try {
            LOGGER.debug("Creating BAB Ethernet interfaces component");
            final CComponentEthernetInterfaces component = new CComponentEthernetInterfaces(sessionService);
            LOGGER.debug("Created Ethernet interfaces component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB Ethernet interfaces component: {}", e.getMessage());
            CNotificationService.showException("Failed to load Ethernet interfaces component", e);
            return CDiv.errorDiv("Failed to load Ethernet interfaces component: " + e.getMessage());
        }
    }

    // Serial Interface Component
    public Component createComponentSerialInterfaces() {
        try {
            LOGGER.debug("Creating BAB serial interfaces component");
            final CComponentSerialInterfaces component = new CComponentSerialInterfaces(sessionService);
            LOGGER.debug("Created serial interfaces component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB serial interfaces component: {}", e.getMessage());
            CNotificationService.showException("Failed to load serial interfaces component", e);
            return CDiv.errorDiv("Failed to load serial interfaces component: " + e.getMessage());
        }
    }

    // ROS Node Component
    public Component createComponentRosNodes() {
        try {
            LOGGER.debug("Creating BAB ROS nodes component");
            final CComponentRosNodes component = new CComponentRosNodes(sessionService);
            LOGGER.debug("Created ROS nodes component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB ROS nodes component: {}", e.getMessage());
            CNotificationService.showException("Failed to load ROS nodes component", e);
            return CDiv.errorDiv("Failed to load ROS nodes component: " + e.getMessage());
        }
    }

    // Modbus Interface Component
    public Component createComponentModbusInterfaces() {
        try {
            LOGGER.debug("Creating BAB Modbus interfaces component");
            final CComponentModbusInterfaces component = new CComponentModbusInterfaces(sessionService);
            LOGGER.debug("Created Modbus interfaces component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB Modbus interfaces component: {}", e.getMessage());
            CNotificationService.showException("Failed to load Modbus interfaces component", e);
            return CDiv.errorDiv("Failed to load Modbus interfaces component: " + e.getMessage());
        }
    }

    // USB Interface Component  
    public Component createComponentUsbInterfaces() {
        try {
            LOGGER.debug("Creating BAB USB interfaces component");
            final CComponentUsbInterfaces component = new CComponentUsbInterfaces(sessionService);
            LOGGER.debug("Created USB interfaces component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB USB interfaces component: {}", e.getMessage());
            CNotificationService.showException("Failed to load USB interfaces component", e);
            return CDiv.errorDiv("Failed to load USB interfaces component: " + e.getMessage());
        }
    }

    // Audio Device Component  
    public Component createComponentAudioDevices() {
        try {
            LOGGER.debug("Creating BAB audio devices component");
            final CComponentAudioDevices component = new CComponentAudioDevices(sessionService);
            LOGGER.debug("Created audio devices component successfully");
            return component;
        } catch (final Exception e) {
            LOGGER.error("Error creating BAB audio devices component: {}", e.getMessage());
            CNotificationService.showException("Failed to load audio devices component", e);
            return CDiv.errorDiv("Failed to load audio devices component: " + e.getMessage());
        }
    }

    /**
     * Initialize BAB-specific interface dashboard components.
     * Called during bind() to set up dashboard-specific functionality.
     */
    private void initializeBabInterfaceComponents() {
        try {
            LOGGER.debug("Initializing BAB interface dashboard components");
            // Add interface-specific initialization logic here if needed
            // For now, components are created on-demand via factory methods
            LOGGER.debug("BAB interface dashboard components initialized successfully");
        } catch (final Exception e) {
            LOGGER.error("Error initializing BAB interface dashboard components: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize BAB interface dashboard components", e);
        }
    }
}