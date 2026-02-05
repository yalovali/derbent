package tech.derbent.bab.policybase.policy.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.api.ui.component.basic.CDiv;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.policy.domain.CBabPolicy;
import tech.derbent.bab.policybase.policy.view.CComponentPolicyBab;
import tech.derbent.base.session.service.ISessionService;

/** CPageServiceBabPolicy - PageService for BAB policy entities. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern:
 * Concrete PageService with @Profile annotation for BAB. Provides specialized policy management functionality for BAB Actions Dashboard. Features: -
 * Policy rule management dashboard component - Virtual network node policy configuration - Calimero JSON export and application - Custom report
 * actions for policy data Component Factory Methods: - createComponentPolicyBab() - Main policy dashboard widget with: * Virtual node interface list
 * (left panel) * Policy rules grid (right panel - tab 1) * Policy configuration (right panel - tab 2) * Action toolbar (apply, export, refresh) */
@Profile ("bab")
public class CPageServiceBabPolicy extends CPageServiceDynamicPage<CBabPolicy> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabPolicy.class);
	private final ISessionService sessionService;

	/** Constructor for BAB policy page service.
	 * @param view the page view implementer */
	public CPageServiceBabPolicy(final IPageServiceImplementer<CBabPolicy> view) {
		super(view);
		// Initialize session service from Spring context (following BAB pattern)
		try {
			sessionService = CSpringContext.getBean(ISessionService.class);
			LOGGER.debug("CPageServiceBabPolicy initialized for view: {}", view.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize ISessionService - policy dashboard component will fail", e);
			throw new IllegalStateException("Cannot initialize BAB policy page service without session service", e);
		}
	}

	/** Custom report action for BAB policies. Generates policy reports including: - Active policies and their rule counts - Policy priority
	 * distribution - Policy application status - Rule completion statistics
	 * @throws Exception if report generation fails */
	@Override
	public void actionReport() throws Exception {
		LOGGER.debug("Policy report action triggered for BAB policies");
		try {
			super.actionReport();
		} catch (final Exception e) {
			LOGGER.error("Error generating BAB policy report: {}", e.getMessage(), e);
			throw e;
		}
	}

	/** Binds the page service to the dynamic page view. Initializes: - Standard entity form bindings - BAB-specific policy components - Policy
	 * dashboard widgets
	 * @throws IllegalStateException if view is null
	 * @throws RuntimeException      if binding fails */
	@Override
	public void bind() {
		try {
			LOGGER.debug("Binding {} to dynamic page for BAB policy entity {}.", this.getClass().getSimpleName(), CBabPolicy.class.getSimpleName());
			Check.notNull(getView(), "View must not be null to bind BAB policy page service.");
			super.bind();
			// Initialize BAB-specific policy components
			initializeBabPolicyComponents();
		} catch (final Exception e) {
			LOGGER.error("Error binding {} to dynamic page for BAB policy entity {}: {}", this.getClass().getSimpleName(),
					CBabPolicy.class.getSimpleName(), e.getMessage());
			throw e;
		}
	}

	/** Creates the main BAB policy dashboard component. Called by CFormBuilder when building form from @AMetaData. Component factory method pattern
	 * for BAB dashboard widgets. Component Features: - Split layout: 30% node list (left) + 70% work area (right) - Virtual network node interface
	 * list with drag-and-drop support - Policy rules grid with source/destination node columns - Tabbed work area: Rules + Configuration - Action
	 * toolbar: Apply Policy, Export JSON, Refresh Error Handling: - Returns error div on failure (graceful degradation) - Shows notification to user
	 * - Logs exception details
	 * @return CComponentPolicyBab dashboard widget or error div */
	public Component createComponentPolicyBab() {
		try {
			LOGGER.debug("Creating BAB policy dashboard component");
			// Create component with session service for context access
			final CComponentPolicyBab component = new CComponentPolicyBab(sessionService);
			LOGGER.debug("Created BAB policy dashboard component successfully");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Error creating BAB policy dashboard component: {}", e.getMessage(), e);
			CNotificationService.showException("Failed to load policy dashboard component", e);
			return CDiv.errorDiv("Failed to load policy dashboard component: " + e.getMessage());
		}
	}

	/** Initialize BAB-specific policy components. This method can be extended to add BAB-specific policy initialization: - Policy rule validation -
	 * Virtual node availability checks - Calimero gateway connection status - Default policy templates Called during bind() lifecycle. */
	private void initializeBabPolicyComponents() {
		try {
			LOGGER.debug("Initializing BAB policy components");
			// This method can be extended for BAB-specific policy initialization
			// For example:
			// - Validate policy rules for completeness
			// - Check virtual node references
			// - Initialize policy templates
			// - Connect to Calimero gateway for status
		} catch (final Exception e) {
			LOGGER.error("Error initializing BAB policy components: {}", e.getMessage(), e);
			// Don't throw - allow page to load with partial functionality
		}
	}
}
