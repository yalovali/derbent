package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.dashboardproject_bab.domain.CDashboardProject_Bab;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentDiskUsage;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentDnsConfiguration;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentInterfaceList;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentRoutingTable;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentSystemMetrics;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentSystemProcessList;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentSystemServices;
import tech.derbent.bab.dashboard.dashboardproject_bab.view.CComponentWebServiceDiscovery;
import tech.derbent.api.session.service.ISessionService;

/** CPageServiceDashboardProject_Bab - PageService for BAB dashboard projects. Layer: Service (MVC) Following Derbent pattern: Concrete PageService
 * with @Profile annotation for BAB. Provides specialized dashboard functionality for BAB project management. Features: - Project dashboard overview
 * with widgets - BAB-specific dashboard components - Dashboard statistics and metrics - Custom report actions for dashboard data */
@Profile ("bab")
public class CPageServiceDashboardProject_Bab extends CPageServiceDynamicPage<CDashboardProject_Bab> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceDashboardProject_Bab.class);
	private final ISessionService sessionService;

	public CPageServiceDashboardProject_Bab(final IPageServiceImplementer<CDashboardProject_Bab> view) {
		super(view);
		// Initialize session service from Spring context (following CPageServiceActivity pattern)
		try {
			sessionService = tech.derbent.api.config.CSpringContext.getBean(ISessionService.class);
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize ISessionService - interface list component will fail", e);
			throw new IllegalStateException("Cannot initialize page service without session service", e);
		}
	}

	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Dashboard report action triggered for BAB dashboard projects");
		try {
			super.actionReport();
		} catch (final Exception e) {
			LOGGER.error("Error generating BAB dashboard report: {}", e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for BAB dashboard entity {}.", this.getClass().getSimpleName(),
					CDashboardProject_Bab.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind BAB dashboard page service.");
			super.bind();
			// Initialize BAB-specific dashboard components
			initializeBabDashboardComponents();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for BAB dashboard entity {}: {}", this.getClass().getSimpleName(),
					CDashboardProject_Bab.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	public Component createComponentDiskUsage() {
		try {
			LOGGER.debug("Creating BAB dashboard disk usage component");
			final CComponentDiskUsage component = new CComponentDiskUsage(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard disk usage: {}", e.getMessage());
			CNotificationService.showException("Failed to load disk usage component", e);
			return CDiv.errorDiv("Failed to load disk usage component: " + e.getMessage());
		}
	}

	public Component createComponentDnsConfiguration() {
		try {
			LOGGER.debug("Creating BAB dashboard DNS configuration component");
			final CComponentDnsConfiguration component = new CComponentDnsConfiguration(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard DNS configuration: {}", e.getMessage());
			CNotificationService.showException("Failed to load DNS configuration component", e);
			return CDiv.errorDiv("Failed to load DNS configuration component: " + e.getMessage());
		}
	}

	public Component createComponentInterfaceList() {
		try {
			LOGGER.debug("Creating BAB dashboard interface list component");
			final CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard interface list: {}", e.getMessage());
			CNotificationService.showException("Failed to load interface list component", e);
			return CDiv.errorDiv("Failed to load interface list component: " + e.getMessage());
		}
	}

	public Component createComponentRoutingTable() {
		try {
			LOGGER.debug("Creating BAB dashboard routing table component");
			final CComponentRoutingTable component = new CComponentRoutingTable(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard routing table: {}", e.getMessage());
			CNotificationService.showException("Failed to load routing table component", e);
			return CDiv.errorDiv("Failed to load routing table component: " + e.getMessage());
		}
	}

	public Component createComponentSystemMetrics() {
		try {
			LOGGER.debug("Creating BAB dashboard system metrics component");
			final CComponentSystemMetrics component = new CComponentSystemMetrics(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system metrics: {}", e.getMessage());
			CNotificationService.showException("Failed to load system metrics component", e);
			return CDiv.errorDiv("Failed to load system metrics component: " + e.getMessage());
		}
	}

	public Component createComponentSystemProcessList() {
		try {
			LOGGER.debug("Creating BAB dashboard system process list component");
			final CComponentSystemProcessList component = new CComponentSystemProcessList(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system process list: {}", e.getMessage());
			CNotificationService.showException("Failed to load system process list component", e);
			return CDiv.errorDiv("Failed to load system process list component: " + e.getMessage());
		}
	}

	public Component createComponentSystemServices() {
		try {
			LOGGER.debug("Creating BAB dashboard system services component");
			final CComponentSystemServices component = new CComponentSystemServices(sessionService);
			registerComponent(component.getComponentName(), component);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system services: {}", e.getMessage());
			CNotificationService.showException("Failed to load system services component", e);
			return CDiv.errorDiv("Failed to load system services component: " + e.getMessage());
		}
	}

	/** Creates webservice discovery component for BAB dashboard. Called by CFormBuilder when building form from @AMetaData.
	 * @return CComponentWebServiceDiscovery for API endpoint discovery */
	public Component createComponentWebServiceDiscovery() {
		try {
			LOGGER.debug("Creating BAB dashboard webservice discovery component");
			final CComponentWebServiceDiscovery component = new CComponentWebServiceDiscovery(sessionService);
			registerComponent(component.getComponentName(), component);
			LOGGER.debug("Created webservice discovery component successfully");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard webservice discovery component: {}", e.getMessage());
			CNotificationService.showException("Failed to load webservice discovery component", e);
			return CDiv.errorDiv("Failed to load webservice discovery component: " + e.getMessage());
		}
	}

	/** Initialize BAB-specific dashboard components. Adds dashboard widgets, statistics cards, and BAB monitoring components. */
	private void initializeBabDashboardComponents() {
		try {
			LOGGER.debug("Initializing BAB dashboard components");
			// This method can be extended to add BAB-specific dashboard initialization
			// For example: device status widgets, communication metrics, etc.
		} catch (final Exception e) {
			LOGGER.error("Error initializing BAB dashboard components: {}", e.getMessage(), e);
		}
	}
}
