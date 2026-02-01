package tech.derbent.bab.dashboard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.dashboard.domain.CDashboardProject_Bab;
import tech.derbent.bab.dashboard.view.CComponentInterfaceList;
import tech.derbent.base.session.service.ISessionService;

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
			this.sessionService = tech.derbent.api.config.CSpringContext.getBean(ISessionService.class);
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

	public Component createComponentInterfaceList() {
		try {
			LOGGER.debug("Creating BAB dashboard interface list component");
			final CComponentInterfaceList component = new CComponentInterfaceList(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard interface list: {}", e.getMessage());
			CNotificationService.showException("Failed to load interface list component", e);
			return CDiv.errorDiv("Failed to load interface list component: " + e.getMessage());
		}
	}
	
	public Component createComponentSystemMetrics() {
		try {
			LOGGER.debug("Creating BAB dashboard system metrics component");
			final tech.derbent.bab.dashboard.view.CComponentSystemMetrics component = 
				new tech.derbent.bab.dashboard.view.CComponentSystemMetrics(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system metrics: {}", e.getMessage());
			CNotificationService.showException("Failed to load system metrics component", e);
			return CDiv.errorDiv("Failed to load system metrics component: " + e.getMessage());
		}
	}
	
	public Component createComponentDiskUsage() {
		try {
			LOGGER.debug("Creating BAB dashboard disk usage component");
			final tech.derbent.bab.dashboard.view.CComponentDiskUsage component = 
				new tech.derbent.bab.dashboard.view.CComponentDiskUsage(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard disk usage: {}", e.getMessage());
			CNotificationService.showException("Failed to load disk usage component", e);
			return CDiv.errorDiv("Failed to load disk usage component: " + e.getMessage());
		}
	}
	
	public Component createComponentSystemServices() {
		try {
			LOGGER.debug("Creating BAB dashboard system services component");
			final tech.derbent.bab.dashboard.view.CComponentSystemServices component = 
				new tech.derbent.bab.dashboard.view.CComponentSystemServices(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system services: {}", e.getMessage());
			CNotificationService.showException("Failed to load system services component", e);
			return CDiv.errorDiv("Failed to load system services component: " + e.getMessage());
		}
	}
	
	public Component createComponentSystemProcessList() {
		try {
			LOGGER.debug("Creating BAB dashboard system process list component");
			final tech.derbent.bab.dashboard.view.CComponentSystemProcessList component = 
				new tech.derbent.bab.dashboard.view.CComponentSystemProcessList(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard system process list: {}", e.getMessage());
			CNotificationService.showException("Failed to load system process list component", e);
			return CDiv.errorDiv("Failed to load system process list component: " + e.getMessage());
		}
	}
	
	public Component createComponentDnsConfiguration() {
		try {
			LOGGER.debug("Creating BAB dashboard DNS configuration component");
			final tech.derbent.bab.dashboard.view.CComponentDnsConfiguration component = 
				new tech.derbent.bab.dashboard.view.CComponentDnsConfiguration(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard DNS configuration: {}", e.getMessage());
			CNotificationService.showException("Failed to load DNS configuration component", e);
			return CDiv.errorDiv("Failed to load DNS configuration component: " + e.getMessage());
		}
	}
	
	public Component createComponentRoutingTable() {
		try {
			LOGGER.debug("Creating BAB dashboard routing table component");
			final tech.derbent.bab.dashboard.view.CComponentRoutingTable component = 
				new tech.derbent.bab.dashboard.view.CComponentRoutingTable(sessionService);
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB dashboard routing table: {}", e.getMessage());
			CNotificationService.showException("Failed to load routing table component", e);
			return CDiv.errorDiv("Failed to load routing table component: " + e.getMessage());
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
