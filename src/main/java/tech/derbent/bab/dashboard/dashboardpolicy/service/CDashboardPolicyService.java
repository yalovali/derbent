package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;

/** CDashboardActionsService - Service for BAB Actions Dashboard entities. Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent
 * pattern: Concrete service with @Service annotation. Provides business logic for dashboard actions management: - Dashboard configuration validation
 * - Node statistics calculation and updates - Policy relationship management - Auto-apply functionality - Layout configuration management - Dashboard
 * performance monitoring */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CDashboardPolicyService extends CEntityOfProjectService<CDashboardPolicy> implements IEntityRegistrable, IEntityWithView {

	/** Dashboard statistics data class. */
	public static record DashboardStatistics(long totalDashboards, long autoApplyCount, double averageNodes, double averageRules) {}

	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardPolicyService.class);
	private static final int MAX_NODE_LIST_WIDTH = 90;
	private static final int MAX_REFRESH_INTERVAL = 3600; // 1 hour
	// Dashboard validation constants
	private static final int MIN_NODE_LIST_WIDTH = 10;
	private static final int MIN_REFRESH_INTERVAL = 5;
	// Valid dashboard layouts
	private static final String[] VALID_LAYOUTS = {
			"SPLIT_PANE", "TABBED", "GRID"
	};

	public CDashboardPolicyService(final IDashboardPolicyRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Copy entity fields from source to target. */
	@Override
	public void copyEntityFieldsTo(final CDashboardPolicy source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof CDashboardPolicy)) {
			return;
		}
		final CDashboardPolicy targetDashboard = (CDashboardPolicy) target;
		// STEP 3: Copy dashboard-specific fields using DIRECT setter/getter
		targetDashboard.setDashboardLayout(source.getDashboardLayout());
		targetDashboard.setNodeListWidth(source.getNodeListWidth());
		targetDashboard.setRefreshIntervalSeconds(source.getRefreshIntervalSeconds());
		targetDashboard.setAutoApplyPolicy(source.getAutoApplyPolicy());
		targetDashboard.setShowInactiveNodes(source.getShowInactiveNodes());
		// Reset statistics for copy
		targetDashboard.setTotalNodes(0);
		targetDashboard.setActiveNodes(0);
		targetDashboard.setTotalRules(0);
		targetDashboard.setActiveRules(0);
		// Copy active policy if included in options
		if (options.includesRelations() && source.getActivePolicy() != null) {
			targetDashboard.setActivePolicy(source.getActivePolicy());
		}
		LOGGER.debug("Copied dashboard actions '{}' with options: {}", source.getName(), options);
	}

	/** Find dashboards by layout type. */
	@Transactional (readOnly = true)
	public List<CDashboardPolicy> findByLayout(final String layout, final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notBlank(layout, "Layout cannot be blank");
		Check.notNull(project, "Project cannot be null");
		return ((IDashboardPolicyRepository) repository).findByLayoutAndProject(layout, project);
	}

	/** Find dashboards with high refresh rate. */
	@Transactional (readOnly = true)
	public List<CDashboardPolicy> findHighRefreshRate(final Integer thresholdSeconds, final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(thresholdSeconds, "Threshold cannot be null");
		Check.notNull(project, "Project cannot be null");
		return ((IDashboardPolicyRepository) repository).findHighRefreshRate(thresholdSeconds, project);
	}

	/** Find main dashboard by naming convention or node count. */
	@Transactional (readOnly = true)
	public Optional<CDashboardPolicy> findMainDashboard(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		// First try to find by naming convention
		final List<CDashboardPolicy> mainDashboards = ((IDashboardPolicyRepository) repository).findMainDashboards(project);
		if (!mainDashboards.isEmpty()) {
			return Optional.of(mainDashboards.get(0));
		}
		// Fall back to dashboard with most nodes
		final List<CDashboardPolicy> withMostNodes = ((IDashboardPolicyRepository) repository).findWithMostNodes(project);
		if (!withMostNodes.isEmpty()) {
			return Optional.of(withMostNodes.get(0));
		}
		return Optional.empty();
	}

	/** Find dashboards with active policies. */
	@Transactional (readOnly = true)
	public List<CDashboardPolicy> findWithActivePolicies(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IDashboardPolicyRepository) repository).findWithActivePolicies(project);
	}

	/** Find dashboards with auto-apply enabled. */
	@Transactional (readOnly = true)
	public List<CDashboardPolicy> findWithAutoApply(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		return ((IDashboardPolicyRepository) repository).findWithAutoApply(project);
	}

	/** Get dashboard statistics summary. */
	@Transactional (readOnly = true)
	public DashboardStatistics getDashboardStatistics(final tech.derbent.api.projects.domain.CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<Object> stats = ((IDashboardPolicyRepository) repository).getDashboardStatistics(project);
		if (stats.size() >= 4) {
			return new DashboardStatistics(((Number) stats.get(0)).longValue(), // total dashboards
					((Number) stats.get(1)).longValue(), // auto-apply count
					((Number) stats.get(2)).doubleValue(), // average nodes
					((Number) stats.get(3)).doubleValue() // average rules
			);
		}
		return new DashboardStatistics(0L, 0L, 0.0, 0.0);
	}
	// Dashboard management operations

	@Override
	public Class<CDashboardPolicy> getEntityClass() { return CDashboardPolicy.class; }

	// IEntityRegistrable implementation
	@Override
	public Class<?> getInitializerServiceClass() {
		return Object.class; // Placeholder - will be updated in Phase 8
	}

	@Override
	public Class<?> getPageServiceClass() {
		return Object.class; // Placeholder - will be updated in Phase 8
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (!(entity instanceof CDashboardPolicy)) {
			return;
		}
		final CDashboardPolicy dashboard = (CDashboardPolicy) entity;
		LOGGER.debug("Initializing new dashboard actions entity");
		// Set default dashboard layout if not set
		if (dashboard.getDashboardLayout() == null || dashboard.getDashboardLayout().isEmpty()) {
			dashboard.setDashboardLayout("SPLIT_PANE");
		}
		// Set default node list width for split pane
		if (dashboard.getNodeListWidth() == null) {
			dashboard.setNodeListWidth(30);
		}
		// Set default refresh interval
		if (dashboard.getRefreshIntervalSeconds() == null) {
			dashboard.setRefreshIntervalSeconds(60);
		}
		// Set default auto-apply setting
		if (dashboard.getAutoApplyPolicy() == null) {
			dashboard.setAutoApplyPolicy(false);
		}
		// Set default show inactive nodes setting
		if (dashboard.getShowInactiveNodes() == null) {
			dashboard.setShowInactiveNodes(true);
		}
		// Initialize statistics to zero
		initializeStatistics(dashboard);
		LOGGER.debug("Dashboard actions entity initialization complete for: {}", dashboard.getName());
	}

	/** Initialize dashboard statistics to zero. */
	private void initializeStatistics(final CDashboardPolicy dashboard) {
		if (dashboard.getTotalNodes() == null) {
			dashboard.setTotalNodes(0);
		}
		if (dashboard.getActiveNodes() == null) {
			dashboard.setActiveNodes(0);
		}
		if (dashboard.getTotalRules() == null) {
			dashboard.setTotalRules(0);
		}
		if (dashboard.getActiveRules() == null) {
			dashboard.setActiveRules(0);
		}
	}

	/** Set active policy for dashboard. */
	@Transactional
	public void setActivePolicy(final CDashboardPolicy dashboard, final CDashboardPolicy policy) {
		Check.notNull(dashboard, "Dashboard cannot be null");
		if (policy != null && !policy.getProject().equals(dashboard.getProject())) {
			throw new CValidationException("Policy must belong to the same project as the dashboard");
		}
		if (policy != null && (policy.getIsActive() == null || !policy.getIsActive())) {
			LOGGER.warn("Setting inactive policy '{}' as active policy for dashboard '{}'", policy.getName(), dashboard.getName());
		}
		dashboard.setActivePolicy(policy);
		save(dashboard);
		if (policy != null) {
			LOGGER.info("Set active policy '{}' for dashboard '{}'", policy.getName(), dashboard.getName());
		} else {
			LOGGER.info("Removed active policy from dashboard '{}'", dashboard.getName());
		}
	}

	/** Update dashboard configuration. */
	@Transactional
	public void updateDashboardConfiguration(final CDashboardPolicy dashboard, final String layout, final Integer nodeListWidth,
			final Integer refreshInterval, final Boolean autoApply, final Boolean showInactive) {
		Check.notNull(dashboard, "Dashboard cannot be null");
		// Update configuration
		if (layout != null) {
			dashboard.setDashboardLayout(layout);
		}
		if (nodeListWidth != null) {
			dashboard.setNodeListWidth(nodeListWidth);
		}
		if (refreshInterval != null) {
			dashboard.setRefreshIntervalSeconds(refreshInterval);
		}
		if (autoApply != null) {
			dashboard.setAutoApplyPolicy(autoApply);
		}
		if (showInactive != null) {
			dashboard.setShowInactiveNodes(showInactive);
		}
		save(dashboard);
		LOGGER.info("Updated configuration for dashboard '{}'", dashboard.getName());
	}

	/** Update dashboard statistics. */
	@Transactional
	public void updateStatistics(final CDashboardPolicy dashboard) {
		Check.notNull(dashboard, "Dashboard cannot be null");
		// In a real implementation, these would query the actual node and policy services
		// For now, we'll save the entity to update the last modified time
		save(dashboard);
		LOGGER.debug("Updated statistics for dashboard '{}'", dashboard.getName());
	}

	/** Validate auto-apply policy configuration. */
	private void validateAutoApplyConfiguration(final CDashboardPolicy entity) {
		if (entity.getAutoApplyPolicy() != null && entity.getAutoApplyPolicy()) {
			// Auto-apply requires an active policy
			if (entity.getActivePolicy() == null) {
				LOGGER.warn("Dashboard '{}' has auto-apply enabled but no active policy configured", entity.getName());
			} else if (entity.getActivePolicy().getIsActive() == null || !entity.getActivePolicy().getIsActive()) {
				LOGGER.warn("Dashboard '{}' has auto-apply enabled but the configured policy '{}' is not active", entity.getName(),
						entity.getActivePolicy().getName());
			}
		}
	}

	/** Validate dashboard layout. */
	private void validateDashboardLayout(final String dashboardLayout) {
		final String upperLayout = dashboardLayout.toUpperCase();
		for (final String validLayout : VALID_LAYOUTS) {
			if (validLayout.equals(upperLayout)) {
				return;
			}
		}
		throw new CValidationException(
				String.format("Invalid dashboard layout '%s'. Valid layouts are: %s", dashboardLayout, String.join(", ", VALID_LAYOUTS)));
	}

	@Override
	protected void validateEntity(final CDashboardPolicy entity) {
		super.validateEntity(entity);
		// Required fields - explicit validation for critical dashboard fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		// String length validation - MANDATORY helper usage
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		if (entity.getDescription() != null) {
			validateStringLength(entity.getDescription(), "Description", CEntityConstants.MAX_LENGTH_DESCRIPTION);
		}
		// Dashboard layout validation
		if (entity.getDashboardLayout() == null || entity.getDashboardLayout().trim().isEmpty()) {
			throw new CValidationException("Dashboard layout is required");
		}
		validateStringLength(entity.getDashboardLayout(), "Dashboard Layout", 20);
		validateDashboardLayout(entity.getDashboardLayout());
		// Node list width validation (for SPLIT_PANE layout)
		if (entity.getNodeListWidth() != null) {
			validateNumericField(entity.getNodeListWidth(), "Node List Width", MAX_NODE_LIST_WIDTH);
			if (entity.getNodeListWidth() < MIN_NODE_LIST_WIDTH || entity.getNodeListWidth() > MAX_NODE_LIST_WIDTH) {
				throw new CValidationException(
						String.format("Node list width must be between %d%% and %d%%", MIN_NODE_LIST_WIDTH, MAX_NODE_LIST_WIDTH));
			}
			// Validate width is meaningful for split pane layout
			if ("SPLIT_PANE".equals(entity.getDashboardLayout()) && (entity.getNodeListWidth() < 15 || entity.getNodeListWidth() > 75)) {
				LOGGER.warn("Node list width {}% may not be practical for split pane layout in dashboard '{}'", entity.getNodeListWidth(),
						entity.getName());
			}
		}
		// Refresh interval validation
		if (entity.getRefreshIntervalSeconds() != null) {
			validateNumericField(entity.getRefreshIntervalSeconds(), "Refresh Interval", MAX_REFRESH_INTERVAL);
			if (entity.getRefreshIntervalSeconds() < MIN_REFRESH_INTERVAL) {
				throw new CValidationException(String.format("Refresh interval must be at least %d seconds", MIN_REFRESH_INTERVAL));
			}
			if (entity.getRefreshIntervalSeconds() > MAX_REFRESH_INTERVAL) {
				throw new CValidationException(String.format("Refresh interval cannot exceed %d seconds (1 hour)", MAX_REFRESH_INTERVAL));
			}
			// Warn about performance implications
			if (entity.getRefreshIntervalSeconds() < 30) {
				LOGGER.warn(
						"Very short refresh interval ({} seconds) for dashboard '{}'. "
								+ "Consider using at least 30 seconds to avoid performance issues.",
						entity.getRefreshIntervalSeconds(), entity.getName());
			}
		}
		// Statistics validation (read-only fields should be non-negative)
		validateStatisticsFields(entity);
		// Auto-apply policy validation
		validateAutoApplyConfiguration(entity);
		// Unique name validation - MANDATORY helper usage
		validateUniqueNameInProject((IDashboardPolicyRepository) repository, entity, entity.getName(), entity.getProject());
	}

	/** Validate statistics fields. */
	private void validateStatisticsFields(final CDashboardPolicy entity) {
		if (entity.getTotalNodes() != null && entity.getTotalNodes() < 0) {
			throw new CValidationException("Total nodes cannot be negative");
		}
		if (entity.getActiveNodes() != null && entity.getActiveNodes() < 0) {
			throw new CValidationException("Active nodes cannot be negative");
		}
		if (entity.getTotalRules() != null && entity.getTotalRules() < 0) {
			throw new CValidationException("Total rules cannot be negative");
		}
		if (entity.getActiveRules() != null && entity.getActiveRules() < 0) {
			throw new CValidationException("Active rules cannot be negative");
		}
		// Validate logical relationships
		if (entity.getTotalNodes() != null && entity.getActiveNodes() != null && entity.getActiveNodes() > entity.getTotalNodes()) {
			throw new CValidationException("Active nodes cannot exceed total nodes");
		}
		if (entity.getTotalRules() != null && entity.getActiveRules() != null && entity.getActiveRules() > entity.getTotalRules()) {
			throw new CValidationException("Active rules cannot exceed total rules");
		}
	}
}
