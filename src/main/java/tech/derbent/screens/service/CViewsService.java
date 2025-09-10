package tech.derbent.screens.service;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.vaadin.hilla.ApplicationContextProvider;
import tech.derbent.abstracts.services.CAbstractService;

@Service
public class CViewsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CViewsService.class);

	public List<String> getAvailableBaseTypes() {
		LOGGER.debug("Retrieving available base types for views");
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser", "CProjectGannt");
	}

	/** Get available entity line class types for a given base entity type. This includes the entity itself and related entities accessible through
	 * relationships.
	 * @param baseEntityType the base entity type (e.g., "CActivity")
	 * @return list of entity line class types including direct and related entities */
	public List<String> getAvailableEntityLineTypes(final String baseEntityType) {
		LOGGER.debug("Retrieving available entity line types for base type: {}", baseEntityType);
		final List<String> entityLineTypes = new ArrayList<>();
		// Add the base entity itself
		entityLineTypes.add(baseEntityType);
		// Add related entity types based on the base entity
		switch (baseEntityType) {
		case "CActivity":
			entityLineTypes.add("Project of Activity");
			entityLineTypes.add("Assigned User of Activity");
			entityLineTypes.add("Created User of Activity");
			entityLineTypes.add("Activity Type of Activity");
			entityLineTypes.add("Activity Status of Activity");
			entityLineTypes.add("Activity Priority of Activity");
			entityLineTypes.add("Parent Activity of Activity");
			break;
		case "CMeeting":
			entityLineTypes.add("Project of Meeting");
			entityLineTypes.add("Assigned User of Meeting");
			entityLineTypes.add("Created User of Meeting");
			entityLineTypes.add("Meeting Type of Meeting");
			entityLineTypes.add("Meeting Status of Meeting");
			break;
		case "CRisk":
			entityLineTypes.add("Project of Risk");
			entityLineTypes.add("Assigned User of Risk");
			entityLineTypes.add("Created User of Risk");
			entityLineTypes.add("Risk Status of Risk");
			entityLineTypes.add("Risk Severity of Risk");
			break;
		case "CProject":
			entityLineTypes.add("Created User of Project");
			break;
		case "CUser":
			// User doesn't have many relationships in the current model
			break;
		default:
			LOGGER.warn("Unknown base entity type: {}", baseEntityType);
			break;
		}
		return entityLineTypes;
	}

	/** Get the actual entity class name for a given entity line type. Maps descriptive names like "Project of Activity" to actual class names like
	 * "CProject".
	 * @param entityLineType the entity line type (e.g., "Project of Activity")
	 * @return the actual entity class name (e.g., "CProject") */
	public String getEntityClassNameForLineType(final String entityLineType) {
		LOGGER.debug("Getting entity class name for line type: {}", entityLineType);
		// Direct entity types
		if (entityLineType.equals("CActivity") || entityLineType.equals("CMeeting") || entityLineType.equals("CRisk")
				|| entityLineType.equals("CProject") || entityLineType.equals("CUser")) {
			return entityLineType;
		}
		// Related entity types
		if (entityLineType.contains("Project of")) {
			return "CProject";
		} else if (entityLineType.contains("User of") || entityLineType.contains("Assigned User") || entityLineType.contains("Created User")) {
			return "CUser";
		} else if (entityLineType.contains("Activity Type of")) {
			return "CActivityType";
		} else if (entityLineType.contains("Activity Status of")) {
			return "CActivityStatus";
		} else if (entityLineType.contains("Activity Priority of")) {
			return "CActivityPriority";
		} else if (entityLineType.contains("Parent Activity of")) {
			return "CActivity";
		} else if (entityLineType.contains("Meeting Type of")) {
			return "CMeetingType";
		} else if (entityLineType.contains("Meeting Status of")) {
			return "CMeetingStatus";
		} else if (entityLineType.contains("Risk Status of")) {
			return "CRiskStatus";
		} else if (entityLineType.contains("Risk Severity of")) {
			return "CRiskSeverity";
		}
		LOGGER.warn("Unknown entity line type: {}", entityLineType);
		return entityLineType; // fallback to the original type
	}

	public List<String> getAvailableBeans() {
		// These are the service class names corresponding to the entity types
		LOGGER.debug("Retrieving available service beans for views");
		// get beans from application context
		List<String> serviceBeans = new ArrayList<>();
		try {
			if (ApplicationContextProvider.getApplicationContext() == null) {
				LOGGER.warn("ApplicationContext is not available, returning empty list");
				return serviceBeans;
			}
			for (final String beanName : ApplicationContextProvider.getApplicationContext().getBeanDefinitionNames()) {
				LOGGER.debug("Bean found: {}", beanName);
				if (beanName.endsWith("Service")) {
					serviceBeans.add(beanName);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error retrieving service beans: {}", e.getMessage(), e);
		}
		// Return the list of service beans
		return serviceBeans;
		// Note: Ensure these service classes exist in your application
	}

	/** Get the entity field names for a given service bean name. For example, if given "CActivityService", returns the field names of the CActivity
	 * entity class.
	 * @param serviceBeanName the name of the service bean (e.g., "CActivityService")
	 * @return list of field names from the corresponding entity class */
	public List<String> getEntityFieldsForService(final String serviceBeanName) {
		LOGGER.debug("Getting entity fields for service bean: {}", serviceBeanName);
		try {
			// Check for null or empty service name
			if (serviceBeanName == null || serviceBeanName.trim().isEmpty()) {
				LOGGER.debug("Service bean name is null or empty");
				return List.of();
			}
			// Check if ApplicationContext is available
			if (ApplicationContextProvider.getApplicationContext() == null) {
				LOGGER.warn("ApplicationContext is not available");
				return List.of();
			}
			// Get the service bean from Spring application context
			Object serviceBean = ApplicationContextProvider.getApplicationContext().getBean(serviceBeanName);
			// Check if the service extends CAbstractService
			if (!(serviceBean instanceof CAbstractService)) {
				LOGGER.warn("Service bean {} does not extend CAbstractService", serviceBeanName);
				return List.of();
			}
			// Cast to CAbstractService and get the entity class
			CAbstractService<?> abstractService = (CAbstractService<?>) serviceBean;
			Class<?> entityClass = getEntityClassFromService(abstractService);
			if (entityClass == null) {
				LOGGER.warn("Could not determine entity class for service: {}", serviceBeanName);
				return List.of();
			}
			// Get all declared fields from the entity class
			List<String> fieldNames = new ArrayList<>();
			Field[] fields = entityClass.getDeclaredFields();
			for (Field field : fields) {
				// Skip static fields and logger fields
				if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || "LOGGER".equals(field.getName())
						|| field.getName().startsWith("$")) {
					continue;
				}
				fieldNames.add(field.getName());
			}
			LOGGER.debug("Found {} fields for entity class {}", fieldNames.size(), entityClass.getSimpleName());
			return fieldNames;
		} catch (Exception e) {
			LOGGER.error("Error getting entity fields for service {}: {}", serviceBeanName, e.getMessage(), e);
			return List.of();
		}
	}

	/** Helper method to get the entity class from a service using reflection.
	 * @param service the service instance
	 * @return the entity class or null if not found */
	private Class<?> getEntityClassFromService(final CAbstractService<?> service) {
		try {
			// Handle CGLIB proxies by getting the superclass if needed
			Class<?> serviceClass = service.getClass();
			// If this is a CGLIB proxy, get the actual superclass
			if (serviceClass.getName().contains("$$SpringCGLIB$$")) {
				serviceClass = serviceClass.getSuperclass();
			}
			// Try to call the getEntityClass() method using reflection
			Method getEntityClassMethod = serviceClass.getDeclaredMethod("getEntityClass");
			getEntityClassMethod.setAccessible(true);
			return (Class<?>) getEntityClassMethod.invoke(service);
		} catch (Exception e) {
			LOGGER.debug("Could not get entity class from service using getEntityClass() method: {}", e.getMessage());
			return null;
		}
	}
}
