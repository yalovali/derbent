package tech.derbent.api.registry;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.utils.Check;

/** Central registry for entity metadata using map-based lookups. This replaces the switch-case pattern in CAuxillaries with O(1) lookup performance.
 * Services can register themselves at application startup for fast access later. */
public class CEntityRegistry {

	private static final Map<Class<?>, String> defaultColors = new ConcurrentHashMap<>();
	private static final Map<String, String> defaultColorsByName = new ConcurrentHashMap<>();
	private static final Map<Class<?>, String> defaultIcons = new ConcurrentHashMap<>();
	private static final Map<String, String> defaultIconsByName = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> entityClasses = new ConcurrentHashMap<>();
	private static volatile boolean initialized = false;
	private static final Map<Class<?>, Class<?>> initializerServices = new ConcurrentHashMap<>();
	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityRegistry.class);
	private static final Map<Class<?>, Class<?>> pageServiceClasses = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> pageServiceClassesByName = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> serviceClasses = new ConcurrentHashMap<>();
	private static final Map<Class<?>, Class<?>> serviceClassesByEntity = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> serviceClassesByName = new ConcurrentHashMap<>();

	/** Clears all registrations (primarily for testing). */
	public static void clear() {
		entityClasses.clear();
		serviceClasses.clear();
		serviceClassesByEntity.clear();
		initializerServices.clear();
		serviceClassesByName.clear();
		pageServiceClasses.clear();
		pageServiceClassesByName.clear();
		defaultIcons.clear();
		defaultColors.clear();
		defaultIconsByName.clear();
		defaultColorsByName.clear();
		initialized = false;
		LOGGER.info("Entity registry cleared");
	}

	/** Gets the default color for an entity class.
	 * @param entityClass the entity class
	 * @return the color code, or null if not registered */
	public static String getDefaultColor(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return defaultColors.get(entityClass);
	}

	/** Gets the default color for an entity by class name.
	 * @param className the fully qualified class name
	 * @return the color code, or null if not registered */
	public static String getDefaultColorByName(final String className) {
		Check.notBlank(className, "Class name cannot be blank");
		return defaultColorsByName.get(className);
	}

	/** Gets the default icon for an entity class.
	 * @param entityClass the entity class
	 * @return the icon string, or null if not registered */
	public static String getDefaultIcon(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return defaultIcons.get(entityClass);
	}

	/** Gets the default icon for an entity by class name.
	 * @param className the fully qualified class name
	 * @return the icon string, or null if not registered */
	public static String getDefaultIconByName(final String className) {
		Check.notBlank(className, "Class name cannot be blank");
		return defaultIconsByName.get(className);
	}

	/** Gets the entity class for a given simple name.
	 * @param simpleName the simple name (e.g., "CActivity")
	 * @return the entity class
	 * @throws IllegalArgumentException if entity not found */
	public static Class<?> getEntityClass(final String simpleName) {
		Check.notBlank(simpleName, "Simple name cannot be blank");
		final Class<?> clazz = entityClasses.get(simpleName);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
		return clazz;
	}

	/** Gets the service class for a given entity simple name.
	 * @param simpleName the simple name (e.g., "CActivity")
	 * @return the service class
	 * @throws IllegalArgumentException if service not found */
	public static Class<?> getEntityServiceClass(final String simpleName) {
		Check.notBlank(simpleName, "Simple name cannot be blank");
		final Class<?> clazz = serviceClasses.get(simpleName);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
		return clazz;
	}

	/** Gets the initializer service class for an entity class.
	 * @param entityClass the entity class
	 * @return the initializer service class
	 * @throws IllegalArgumentException if initializer not found */
	public static Class<?> getInitializerService(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		final Class<?> clazz = initializerServices.get(entityClass);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown entity type: " + entityClass.getSimpleName());
		}
		return clazz;
	}

	/** Gets the page service class for an entity class.
	 * @param entityClass the entity class
	 * @return the page service class, or null if not registered */
	public static Class<?> getPageServiceClass(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return pageServiceClasses.get(entityClass);
	}

	/** Gets the page service class by service name.
	 * @param pageServiceName the page service name (e.g., "CPageServiceActivity")
	 * @return the page service class, or null if not registered */
	public static Class<?> getPageServiceClassByName(final String pageServiceName) {
		Check.notBlank(pageServiceName, "Page service name cannot be blank");
		return pageServiceClassesByName.get(pageServiceName);
	}

	/** Gets the count of registered entities.
	 * @return the count */
	public static int getRegisteredCount() { return entityClasses.size(); }

	/** Gets the service class by service name.
	 * @param serviceName the service name (e.g., "CActivityService")
	 * @return the service class
	 * @throws IllegalArgumentException if service not found */
	public static Class<?> getServiceClassByName(final String serviceName) {
		Check.notBlank(serviceName, "Service name cannot be blank");
		final Class<?> clazz = serviceClassesByName.get(serviceName);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown service type: " + serviceName);
		}
		return clazz;
	}

	/** Gets the service class for a given entity class.
	 * @param entityClass the entity class
	 * @return the service class
	 * @throws IllegalArgumentException if service not found */
	public static Class<?> getServiceClassForEntity(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		final Class<?> clazz = serviceClassesByEntity.get(entityClass);
		if (clazz == null) {
			throw new IllegalArgumentException("Unknown entity type: " + entityClass.getSimpleName());
		}
		return clazz;
	}

	/** Checks if the registry has been initialized.
	 * @return true if initialized, false otherwise */
	public static boolean isInitialized() { return initialized; }

	/** Checks if an entity is registered.
	 * @param simpleName the simple name
	 * @return true if registered, false otherwise */
	public static boolean isRegistered(final String simpleName) {
		return entityClasses.containsKey(simpleName);
	}

	/** Marks the registry as initialized. This should be called after all entities have been registered. */
	public static void markInitialized() {
		initialized = true;
		LOGGER.info("Entity registry initialized with {} entities", entityClasses.size());
	}

	/** Registers an entity and its associated metadata.
	 * @param registrable the entity metadata to register */
	public static void register(final IEntityRegistrable registrable) {
		try {
			Check.notNull(registrable, "Registrable cannot be null");
			Check.notNull(registrable.getEntityClass(), "Entity class cannot be null");
			Check.notNull(registrable.getServiceClass(), "Service class cannot be null");
			Check.notBlank(registrable.getSimpleName(), "Simple name cannot be blank");
			Check.notNull(registrable.getInitializerServiceClass(), "Initializer service class cannot be null");
			Check.notNull(registrable.getPageServiceClass(), "Page service class cannot be null" + " for entity: " + registrable.getSimpleName());
			Check.notBlank(registrable.getDefaultIconName(), "Default icon name cannot be blank");
			Check.notBlank(registrable.getDefaultColor(), "Default color cannot be blank");
			final Class<?> entityClass = registrable.getEntityClass();
			final String simpleName = registrable.getSimpleName();
			final Class<?> serviceClass = registrable.getServiceClass();
			LOGGER.debug("Registered entity class: {} -> {}", simpleName, entityClass.getName());
			// Register entity class
			entityClasses.put(simpleName, entityClass);
			serviceClasses.put(simpleName, serviceClass);
			serviceClassesByEntity.put(entityClass, serviceClass);
			serviceClassesByName.put(serviceClass.getSimpleName(), serviceClass);
			initializerServices.put(entityClass, registrable.getInitializerServiceClass());
			pageServiceClasses.put(entityClass, registrable.getPageServiceClass());
			pageServiceClassesByName.put(registrable.getPageServiceClass().getSimpleName(), registrable.getPageServiceClass());
			defaultIcons.put(entityClass, registrable.getDefaultIconName());
			defaultIconsByName.put(entityClass.getName(), registrable.getDefaultIconName());
			defaultColors.put(entityClass, registrable.getDefaultColor());
			defaultColorsByName.put(entityClass.getName(), registrable.getDefaultColor());
		} catch (final Exception e) {
			LOGGER.error("Error registering entity: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to register entity", e);
		}
	}

	// Private constructor to prevent instantiation
	private CEntityRegistry() {
		// Utility class
	}
}
