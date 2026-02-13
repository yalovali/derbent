package tech.derbent.bab.policybase.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.action.service.CBabPolicyActionService;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;
import tech.derbent.bab.policybase.filter.service.CBabPolicyFilterService;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;
import tech.derbent.bab.policybase.trigger.service.CBabPolicyTriggerService;

/** CBabPolicybaseInitializerService - Sample data initializer for BAB policybase entities. Creates sample triggers, actions, and filters for BAB
 * policy rules system. Provides complete examples of each entity type with realistic configurations. Layer: Service (MVC) Active when: 'bab' profile
 * is active Following Derbent pattern: Initializer service for sample data */
@Service
@Profile ("bab")
public class CBabPolicybaseInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabPolicybaseInitializerService.class);
	private final CBabPolicyActionService actionService;
	private final CBabPolicyFilterService filterService;
	private final CBabPolicyTriggerService triggerService;

	public CBabPolicybaseInitializerService(final CBabPolicyTriggerService triggerService, final CBabPolicyActionService actionService,
			final CBabPolicyFilterService filterService) {
		this.triggerService = triggerService;
		this.actionService = actionService;
		this.filterService = filterService;
	}

	/** Create sample action entities. */
	private void initializeSampleActions(final CProject<?> project) {
		LOGGER.debug("Creating sample actions for project: {}", project.getName());
		// 1. Forward Action - Data Routing
		final CBabPolicyAction forwardAction = new CBabPolicyAction("Forward to Database", project);
		forwardAction.setActionType(CBabPolicyAction.ACTION_TYPE_FORWARD);
		forwardAction.setDescription("Forward sensor data to central database");
		forwardAction.setExecutionPriority(70);
		forwardAction.setAsyncExecution(false);
		forwardAction.setLogInput(false);
		forwardAction.setLogOutput(true);
		actionService.save(forwardAction);
		// 2. Transform Action - Data Processing
		final CBabPolicyAction transformAction = new CBabPolicyAction("Transform JSON", project);
		transformAction.setActionType(CBabPolicyAction.ACTION_TYPE_TRANSFORM);
		transformAction.setDescription("Transform data format from CSV to JSON");
		transformAction.setExecutionPriority(60);
		transformAction.setAsyncExecution(true);
		transformAction.setTimeoutSeconds(30);
		transformAction.setFileNodeEnabled(true);
		transformAction.setHttpNodeEnabled(true);
		actionService.save(transformAction);
		// 3. Store Action - Data Persistence
		final CBabPolicyAction storeAction = new CBabPolicyAction("Store to File", project);
		storeAction.setActionType(CBabPolicyAction.ACTION_TYPE_STORE);
		storeAction.setDescription("Store processed data to file system");
		storeAction.setExecutionPriority(50);
		storeAction.setAsyncExecution(true);
		storeAction.setRetryCount(5);
		storeAction.setRetryDelaySeconds(10);
		actionService.save(storeAction);
		// 4. Notify Action - Alert System
		final CBabPolicyAction notifyAction = new CBabPolicyAction("Email Alert", project);
		notifyAction.setActionType(CBabPolicyAction.ACTION_TYPE_NOTIFY);
		notifyAction.setDescription("Send email notification for critical events");
		notifyAction.setExecutionPriority(90);
		notifyAction.setAsyncExecution(true);
		notifyAction.setTimeoutSeconds(15);
		notifyAction.setLogExecution(true);
		actionService.save(notifyAction);
		// 5. Execute Action - External Command
		final CBabPolicyAction executeAction = new CBabPolicyAction("Restart Service", project);
		executeAction.setActionType(CBabPolicyAction.ACTION_TYPE_EXECUTE);
		executeAction.setDescription("Restart system service on failure");
		executeAction.setExecutionPriority(100);
		executeAction.setAsyncExecution(false);
		executeAction.setTimeoutSeconds(120);
		executeAction.setRetryCount(3);
		actionService.save(executeAction);
		// 6. Filter Action - Data Filtering
		final CBabPolicyAction filterAction = new CBabPolicyAction("Filter Invalid Data", project);
		filterAction.setActionType(CBabPolicyAction.ACTION_TYPE_FILTER);
		filterAction.setDescription("Filter out invalid or corrupted data");
		filterAction.setExecutionPriority(80);
		filterAction.setExecutionOrder(1); // Execute early in pipeline
		filterAction.setCanNodeEnabled(true);
		filterAction.setModbusNodeEnabled(true);
		filterAction.setFileNodeEnabled(true);
		actionService.save(filterAction);
		// 7. Validate Action - Data Validation
		final CBabPolicyAction validateAction = new CBabPolicyAction("Validate Schema", project);
		validateAction.setActionType(CBabPolicyAction.ACTION_TYPE_VALIDATE);
		validateAction.setDescription("Validate data against predefined schema");
		validateAction.setExecutionPriority(85);
		validateAction.setExecutionOrder(0); // Execute first
		validateAction.setLogInput(true);
		validateAction.setLogOutput(true);
		actionService.save(validateAction);
		// 8. Log Action - System Logging
		final CBabPolicyAction logAction = new CBabPolicyAction("System Logger", project);
		logAction.setActionType(CBabPolicyAction.ACTION_TYPE_LOG);
		logAction.setDescription("Log system events and data processing");
		logAction.setExecutionPriority(30);
		logAction.setAsyncExecution(true);
		logAction.setTimeoutSeconds(5);
		logAction.setSyslogNodeEnabled(true);
		actionService.save(logAction);
		LOGGER.debug("Created {} sample actions", 8);
	}

	/** Create sample filter entities. */
	private void initializeSampleFilters(final CProject<?> project) {
		LOGGER.debug("Creating sample filters for project: {}", project.getName());
		// 1. CSV Filter - File Processing
		final CBabPolicyFilter csvFilter = new CBabPolicyFilter("CSV Data Filter", project);
		csvFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_CSV);
		csvFilter.setDescription("Filter CSV data files for sensor readings");
		csvFilter.setExecutionOrder(1);
		csvFilter.setCaseSensitive(false);
		csvFilter.setLogRejections(true);
		csvFilter.setFileNodeEnabled(true);
		csvFilter.setCacheEnabled(true);
		csvFilter.setCacheSizeLimit(500);
		filterService.save(csvFilter);
		// 2. JSON Filter - API Data
		final CBabPolicyFilter jsonFilter = new CBabPolicyFilter("JSON API Filter", project);
		jsonFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_JSON);
		jsonFilter.setDescription("Filter JSON data from API endpoints");
		jsonFilter.setExecutionOrder(2);
		jsonFilter.setLogicOperator("AND");
		jsonFilter.setNullHandling("reject");
		jsonFilter.setHttpNodeEnabled(true);
		jsonFilter.setRosNodeEnabled(true);
		filterService.save(jsonFilter);
		// 3. XML Filter - Configuration Data
		final CBabPolicyFilter xmlFilter = new CBabPolicyFilter("XML Config Filter", project);
		xmlFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_XML);
		xmlFilter.setDescription("Filter XML configuration files");
		xmlFilter.setExecutionOrder(3);
		xmlFilter.setCaseSensitive(true);
		xmlFilter.setMaxProcessingTimeMs(10000);
		xmlFilter.setFileNodeEnabled(true);
		filterService.save(xmlFilter);
		// 4. Regex Filter - Text Processing
		final CBabPolicyFilter regexFilter = new CBabPolicyFilter("Text Pattern Filter", project);
		regexFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_REGEX);
		regexFilter.setDescription("Filter text data using regex patterns");
		regexFilter.setExecutionOrder(4);
		regexFilter.setCaseSensitive(false);
		regexFilter.setLogMatches(true);
		regexFilter.setSyslogNodeEnabled(true);
		regexFilter.setFileNodeEnabled(true);
		filterService.save(regexFilter);
		// 5. Range Filter - Numeric Data
		final CBabPolicyFilter rangeFilter = new CBabPolicyFilter("Numeric Range Filter", project);
		rangeFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_RANGE);
		rangeFilter.setDescription("Filter numeric data within valid ranges");
		rangeFilter.setExecutionOrder(5);
		rangeFilter.setLogicOperator("AND");
		rangeFilter.setCanNodeEnabled(true);
		rangeFilter.setModbusNodeEnabled(true);
		rangeFilter.setCacheEnabled(true);
		filterService.save(rangeFilter);
		// 6. Condition Filter - Business Logic
		final CBabPolicyFilter conditionFilter = new CBabPolicyFilter("Business Rule Filter", project);
		conditionFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_CONDITION);
		conditionFilter.setDescription("Apply business logic conditions");
		conditionFilter.setExecutionOrder(6);
		conditionFilter.setLogicOperator("OR");
		conditionFilter.setNullHandling("default");
		conditionFilter.setMaxProcessingTimeMs(5000);
		filterService.save(conditionFilter);
		// 7. Transform Filter - Data Transformation
		final CBabPolicyFilter transformFilter = new CBabPolicyFilter("Data Transform Filter", project);
		transformFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_TRANSFORM);
		transformFilter.setDescription("Transform data structure and format");
		transformFilter.setExecutionOrder(7);
		transformFilter.setLogMatches(true);
		transformFilter.setLogRejections(false);
		transformFilter.setHttpNodeEnabled(true);
		transformFilter.setFileNodeEnabled(true);
		filterService.save(transformFilter);
		// 8. Validate Filter - Data Validation
		final CBabPolicyFilter validateFilter = new CBabPolicyFilter("Schema Validation Filter", project);
		validateFilter.setFilterType(CBabPolicyFilter.FILTER_TYPE_VALIDATE);
		validateFilter.setDescription("Validate data against JSON schema");
		validateFilter.setExecutionOrder(0); // Execute first
		validateFilter.setLogRejections(true);
		validateFilter.setNullHandling("reject");
		validateFilter.setCacheEnabled(true);
		filterService.save(validateFilter);
		LOGGER.debug("Created {} sample filters", 8);
	}

	/** Initialize sample policybase entities for a BAB project. */
	@Transactional
	public void initializeSamplePolicybaseEntities(final CProject<?> project, @SuppressWarnings ("unused") final CCompany company) {
		LOGGER.info("Initializing BAB policybase sample entities for project: {}", project.getName());
		try {
			// Initialize sample triggers
			initializeSampleTriggers(project);
			// Initialize sample actions
			initializeSampleActions(project);
			// Initialize sample filters
			initializeSampleFilters(project);
			LOGGER.info("Successfully initialized {} policybase entities for project '{}'", "sample", project.getName());
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize policybase sample entities for project '{}': {}", project.getName(), e.getMessage(), e);
			throw new RuntimeException("Failed to initialize policybase sample entities", e);
		}
	}

	/** Create sample trigger entities. */
	private void initializeSampleTriggers(final CProject<?> project) {
		LOGGER.debug("Creating sample triggers for project: {}", project.getName());
		// 1. Periodic Trigger - Data Collection
		final CBabPolicyTrigger periodicTrigger = new CBabPolicyTrigger("Data Collection Periodic", project);
		periodicTrigger.setTriggerType(CBabPolicyTrigger.TRIGGER_TYPE_PERIODIC);
		periodicTrigger.setCronExpression("0 */5 * * * *"); // Every 5 minutes
		periodicTrigger.setDescription("Periodic data collection from sensors");
		periodicTrigger.setExecutionPriority(80);
		periodicTrigger.setExecutionOrder(1);
		periodicTrigger.setCanNodeEnabled(true);
		periodicTrigger.setModbusNodeEnabled(true);
		periodicTrigger.setRosNodeEnabled(true);
		triggerService.save(periodicTrigger);
		// 2. Startup Trigger - System Initialization
		final CBabPolicyTrigger startupTrigger = new CBabPolicyTrigger("System Startup", project);
		startupTrigger.setTriggerType(CBabPolicyTrigger.TRIGGER_TYPE_AT_START);
		startupTrigger.setDescription("Initialize system on startup");
		startupTrigger.setExecutionPriority(100);
		startupTrigger.setExecutionOrder(0);
		startupTrigger.setTimeoutSeconds(60);
		triggerService.save(startupTrigger);
		// 3. Manual Trigger - Emergency Stop
		final CBabPolicyTrigger manualTrigger = new CBabPolicyTrigger("Emergency Stop", project);
		manualTrigger.setTriggerType(CBabPolicyTrigger.TRIGGER_TYPE_MANUAL);
		manualTrigger.setDescription("Manual emergency stop trigger");
		manualTrigger.setExecutionPriority(100);
		manualTrigger.setExecutionOrder(0);
		manualTrigger.setLogExecution(true);
		triggerService.save(manualTrigger);
		// 4. Always Trigger - Continuous Monitoring
		final CBabPolicyTrigger alwaysTrigger = new CBabPolicyTrigger("Continuous Monitor", project);
		alwaysTrigger.setTriggerType(CBabPolicyTrigger.TRIGGER_TYPE_ALWAYS);
		alwaysTrigger.setDescription("Continuous monitoring of critical systems");
		alwaysTrigger.setExecutionPriority(60);
		alwaysTrigger.setTimeoutSeconds(10);
		alwaysTrigger.setCanNodeEnabled(true);
		alwaysTrigger.setHttpNodeEnabled(true);
		alwaysTrigger.setSyslogNodeEnabled(true);
		triggerService.save(alwaysTrigger);
		// 5. Once Trigger - Initial Configuration
		final CBabPolicyTrigger onceTrigger = new CBabPolicyTrigger("Initial Configuration", project);
		onceTrigger.setTriggerType(CBabPolicyTrigger.TRIGGER_TYPE_ONCE);
		onceTrigger.setDescription("One-time initial system configuration");
		onceTrigger.setExecutionPriority(90);
		onceTrigger.setExecutionOrder(0);
		onceTrigger.setRetryCount(0); // No retries for one-time triggers
		triggerService.save(onceTrigger);
		LOGGER.debug("Created {} sample triggers", 5);
	}
}
