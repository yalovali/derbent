package tech.derbent.bab.policybase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionInitializerService;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterInitializerService;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerInitializerService;

/** CBabPolicybaseInitializerService - Coordinator service for BAB policybase entity initialization. Delegates sample creation to individual
 * initializer services following the standard Derbent pattern. This service orchestrates initialization of all policybase components: - Policy
 * triggers (event detection and scheduling) - Policy actions (data processing and routing) - Policy filters (data validation and transformation)
 * Layer: Service (MVC) Active when: 'bab' profile is active Following Derbent pattern: Coordinator service for related entity groups */
@Service
@Profile ("bab")
public class CBabPolicybaseInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicybaseInitializerService.class);

	/** Initialize sample policybase entities for a BAB project.
	 * 
	 * Delegates to individual initializer services following the standard Derbent pattern.
	 * 
	 * @param project the project to create entities for
	 * @param minimal if true, creates minimal samples (1 of each); if false, creates comprehensive samples */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		LOGGER.info("Initializing BAB policybase sample entities for project: {}", project.getName());
		
		try {
			// Delegate to individual initializer services (standard Derbent pattern)
			CBabPolicyTriggerInitializerService.initializeSample(project, minimal);
			CBabPolicyActionInitializerService.initializeSample(project, minimal);
			CBabPolicyFilterInitializerService.initializeSample(project, minimal);
			
			LOGGER.info("Successfully initialized policybase sample entities for project: {}", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize policybase sample entities for project '{}': {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to initialize policybase sample entities", e);
		}
	}
}
