package tech.derbent.bab.dashboard.dashboardpolicy.service;

import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.bab.dashboard.dashboardpolicy.domain.CDashboardPolicy;

/** IDashboardActionsRepository - Repository interface for BAB Actions Dashboard entities. Layer: Service (MVC) Active when: 'bab' profile is active
 * Following Derbent pattern: Concrete repository with HQL queries. Handles dashboard actions persistence with complete eager loading for UI
 * performance. Provides specialized queries for dashboard configuration, policy relationships, and statistics. */
@Profile ("bab")
public interface IDashboardPolicyRepository extends IEntityOfProjectRepository<CDashboardPolicy> {

	/** Count dashboards by layout type. Useful for layout usage analytics. */
	@Query ("SELECT COUNT(e) FROM #{#entityName} e WHERE e.dashboardLayout = :layout AND e.project = :project")
	long countByLayoutAndProject(@Param ("layout") String layout, @Param ("project") CProject<?> project);
	/** Find dashboards by active nodes count range. Useful for operational status monitoring. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.activeNodes BETWEEN :minNodes AND :maxNodes
			AND e.project = :project
			ORDER BY e.activeNodes DESC
			""")
	List<CDashboardPolicy> findByActiveNodesRange(@Param ("minNodes") Integer minNodes, @Param ("maxNodes") Integer maxNodes,
			@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.activePolicy ap
			LEFT JOIN FETCH ap.rules apr
			LEFT JOIN FETCH apr.sourceNode
			LEFT JOIN FETCH apr.destinationNode
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.id = :id
			""")
	Optional<CDashboardPolicy> findById(@Param ("id") Long id);
	/** Find dashboards by layout type. Useful for layout-specific functionality. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.dashboardLayout = :layout AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findByLayoutAndProject(@Param ("layout") String layout, @Param ("project") CProject<?> project);
	/** Find dashboards by node list width range. Useful for UI layout optimization. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.nodeListWidth BETWEEN :minWidth AND :maxWidth
			AND e.project = :project
			ORDER BY e.nodeListWidth ASC
			""")
	List<CDashboardPolicy> findByNodeListWidthRange(@Param ("minWidth") Integer minWidth, @Param ("maxWidth") Integer maxWidth,
			@Param ("project") CProject<?> project);
	/** Find dashboards by refresh interval. Useful for performance optimization grouping. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.refreshIntervalSeconds = :intervalSeconds AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findByRefreshInterval(@Param ("intervalSeconds") Integer intervalSeconds, @Param ("project") CProject<?> project);
	/** Find dashboards by total nodes count range. Useful for complexity-based dashboard grouping. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.totalNodes BETWEEN :minNodes AND :maxNodes
			AND e.project = :project
			ORDER BY e.totalNodes DESC
			""")
	List<CDashboardPolicy> findByTotalNodesRange(@Param ("minNodes") Integer minNodes, @Param ("maxNodes") Integer maxNodes,
			@Param ("project") CProject<?> project);
	/** Get distinct dashboard layouts in project. Useful for layout filter dropdown. */
	@Query (
		"SELECT DISTINCT e.dashboardLayout FROM #{#entityName} e WHERE e.dashboardLayout IS NOT NULL AND e.project = :project ORDER BY e.dashboardLayout"
	)
	List<String> findDistinctLayoutsByProject(@Param ("project") CProject<?> project);
	/** Find dashboards by grid layout configuration. Specific query for GRID layout type. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.dashboardLayout = 'GRID' AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findGridLayoutDashboards(@Param ("project") CProject<?> project);
	/** Find dashboards with high refresh rate. Useful for performance monitoring (refresh interval < threshold). */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.refreshIntervalSeconds < :thresholdSeconds AND e.project = :project
			ORDER BY e.refreshIntervalSeconds ASC
			""")
	List<CDashboardPolicy> findHighRefreshRate(@Param ("thresholdSeconds") Integer thresholdSeconds, @Param ("project") CProject<?> project);
	/** Find main dashboard by naming convention. Looks for dashboards with common main dashboard names. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE LOWER(e.name) IN ('main', 'primary', 'default', 'dashboard')
			AND e.project = :project
			ORDER BY e.totalNodes DESC, e.name ASC
			""")
	List<CDashboardPolicy> findMainDashboards(@Param ("project") CProject<?> project);
	/** Find dashboards needing statistics update. Returns dashboards where statistics might be outdated. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.lastModifiedDate > :sinceDate AND e.project = :project
			ORDER BY e.lastModifiedDate DESC
			""")
	List<CDashboardPolicy> findModifiedSince(@Param ("sinceDate") java.time.LocalDateTime sinceDate, @Param ("project") CProject<?> project);
	/** Find dashboards showing inactive nodes. Useful for node visibility configuration. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.showInactiveNodes = true AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findShowingInactiveNodes(@Param ("project") CProject<?> project);
	/** Find dashboards by split layout configuration. Specific query for SPLIT_PANE layout type. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.dashboardLayout = 'SPLIT_PANE' AND e.project = :project
			ORDER BY e.nodeListWidth ASC
			""")
	List<CDashboardPolicy> findSplitLayoutDashboards(@Param ("project") CProject<?> project);
	/** Find dashboards by tabbed layout configuration. Specific query for TABBED layout type. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.dashboardLayout = 'TABBED' AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findTabbedLayoutDashboards(@Param ("project") CProject<?> project);
	/** Find dashboards with active policies. Critical for policy application monitoring. */
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.activePolicy ap
			LEFT JOIN FETCH ap.rules
			WHERE e.activePolicy IS NOT NULL
			AND e.activePolicy.isActive = true
			AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findWithActivePolicies(@Param ("project") CProject<?> project);
	/** Find dashboards with active rules. Returns dashboards with configured policy rules. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.activeRules > 0 AND e.project = :project
			ORDER BY e.activeRules DESC
			""")
	List<CDashboardPolicy> findWithActiveRules(@Param ("project") CProject<?> project);
	/** Find dashboards with auto-apply enabled. Critical for automated policy deployment. */
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.activePolicy
			WHERE e.autoApplyPolicy = true AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findWithAutoApply(@Param ("project") CProject<?> project);
	/** Find dashboard with most nodes. Useful for identifying the main dashboard in a project. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.totalNodes = (
			    SELECT MAX(d.totalNodes) FROM #{#entityName} d WHERE d.project = :project
			) AND e.project = :project
			ORDER BY e.name ASC
			""")
	List<CDashboardPolicy> findWithMostNodes(@Param ("project") CProject<?> project);
	/** Find dashboards with node statistics. Returns dashboards with actual node counts for validation. */
	@Query ("""
			SELECT e FROM #{#entityName} e
			WHERE e.totalNodes > 0 AND e.project = :project
			ORDER BY e.totalNodes DESC
			""")
	List<CDashboardPolicy> findWithNodes(@Param ("project") CProject<?> project);
	/** Get dashboard configuration statistics. Returns: [totalDashboards, autoApplyCount, averageNodes, averageRules] */
	@Query ("""
			SELECT
			    COUNT(e),
			    SUM(CASE WHEN e.autoApplyPolicy = true THEN 1 ELSE 0 END),
			    COALESCE(AVG(CAST(e.totalNodes AS double)), 0),
			    COALESCE(AVG(CAST(e.activeRules AS double)), 0)
			FROM #{#entityName} e
			WHERE e.project = :project
			""")
	List<Object> getDashboardStatistics(@Param ("project") CProject<?> project);
	@Override
	@Query ("""
			SELECT DISTINCT e FROM #{#entityName} e
			LEFT JOIN FETCH e.project
			LEFT JOIN FETCH e.assignedTo
			LEFT JOIN FETCH e.createdBy
			LEFT JOIN FETCH e.activePolicy ap
			LEFT JOIN FETCH ap.rules
			LEFT JOIN FETCH e.attachments
			LEFT JOIN FETCH e.comments
			LEFT JOIN FETCH e.links
			WHERE e.project = :project
			ORDER BY e.id DESC
			""")
	List<CDashboardPolicy> listByProjectForPageView(@Param ("project") CProject<?> project);
}
