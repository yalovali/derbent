package tech.derbent.api.services.pageservice;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;

@Service
public class CPageServiceUtility {

	private static final List<String> availablePageServices = List.of("CPageServiceActivity", "CPageServiceComment", "CPageServiceCompany",
			"CPageServiceDecision", "CPageServiceMeeting", "CPageServiceOrder", "CPageServiceProject", "CPageServiceProject_Bab",
			"CPageServiceProject_Derbent", "CPageServiceRisk", "CPageServiceUser",
			"CPageServiceSystemSettings", "CPageServiceActivityPriority", "CPageServiceProjectItemStatus", "CPageServiceActivityType",
			"CPageServiceRiskType", "CPageServiceCommentPriority", "CPageServiceDecisionStatus", "CPageServiceDecisionType",
			"CPageServiceMeetingStatus", "CPageServiceMeetingType", "CPageServiceOrderStatus", "CPageServiceOrderType", "CPageServiceOrderApproval",
			"CPageServiceApprovalStatus", "CPageServiceCurrency", "CPageServiceRiskStatus", "CPageServiceUserCompanyRole",
			"CPageServiceUserProjectRole", "CPageServiceUserProjectSettings", "CPageServicePageEntity",
			"CPageServiceGridEntity", "CPageServiceKanbanLine");
	private static Logger LOGGER = LoggerFactory.getLogger(CPageServiceUtility.class);

	/** Gets the PageService class by its simple name. Now uses the entity registry for O(1) lookup performance.
	 * @param serviceName the PageService name (e.g., "CPageServiceActivity")
	 * @return the PageService class
	 * @throws IllegalArgumentException if PageService not found */
	public static Class<?> getPageServiceClassByName(String serviceName) {
		try {
			// Try registry first for fast O(1) lookup
			final Class<?> clazz = CEntityRegistry.getPageServiceClassByName(serviceName);
			if (clazz != null) {
				return clazz;
			}
			// If not found in registry, throw exception
			Check.fail("Page service '{}' not registered in entity registry:" + serviceName);
			return null;
		} catch (final Exception e) {
			LOGGER.error("Page service '{}' not implemented", serviceName);
			throw e;
		}
	}

	public static List<String> getPageServiceList() { return CPageServiceUtility.availablePageServices; }

	/** Maps entity class to corresponding PageService class name. Now uses the entity registry for O(1) lookup performance.
	 * @param entityClass The entity class
	 * @return The PageService class name, or null if not mapped */
	public static String getPageServiceNameForEntityClass(Class<?> entityClass) {
		try {
			// Try registry first for fast O(1) lookup
			final Class<?> pageServiceClass = CEntityRegistry.getPageServiceClass(entityClass);
			if (pageServiceClass != null) {
				return pageServiceClass.getSimpleName();
			}
			// Return null for entities that don't have a PageService yet
			return null;
		} catch (final Exception e) {
			LOGGER.debug("No PageService registered for entity class: {} {}", entityClass.getSimpleName(), e.getMessage());
			return null;
		}
	}

	public CPageServiceUtility() {
		super();
	}
}
