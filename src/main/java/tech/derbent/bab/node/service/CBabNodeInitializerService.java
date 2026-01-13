package tech.derbent.bab.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.node.domain.CBabNode;
import tech.derbent.api.companies.domain.CCompany;

/**
 * Initializer service for BAB node sample data.
 * Following Derbent pattern: Initializer service with static initializeSample method.
 */
@Component
@Profile("bab")
public class CBabNodeInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeInitializerService.class);
	private static final Class<?> clazz = CBabNode.class;

	/**
	 * Initialize sample BAB nodes.
	 * Note: Nodes are created via CBabDeviceInitializerService, not independently.
	 * 
	 * @param company the company
	 * @param minimal if true, create minimal sample data
	 */
	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		LOGGER.debug("CBabNode sample data created via CBabDeviceInitializerService");
		// Nodes are created as part of device initialization
		// This method is here for consistency with Derbent pattern
	}
}
