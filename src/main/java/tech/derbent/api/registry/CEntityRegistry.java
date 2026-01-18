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
	// changed key type from Class<?> to String to match usages (simple name -> class)
	private static final Map<String, Class<?>> entityClasses = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> entityClassesByPluralTitle = new ConcurrentHashMap<>();
	private static final Map<String, Class<?>> entityClassesBySingularTitle = new ConcurrentHashMap<>();
	private static final Map<Class<?>, String> entityTitlesPlural = new ConcurrentHashMap<>();
	// Entity title mappings
	private static final Map<Class<?>, String> entityTitlesSingular = new ConcurrentHashMap<>();
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
		entityTitlesSingular.clear();
		entityTitlesPlural.clear();
		entityClassesBySingularTitle.clear();
		entityClassesByPluralTitle.clear();
		initialized = false;
		// LOGGER.info("Entity registry cleared");
	}

	public static String getDefaultColor(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return defaultColors.get(entityClass);
	}

	public static String getDefaultColorByName(final String className) {
		Check.notBlank(className, "Class name cannot be blank");
		return defaultColorsByName.get(className);
	}

	public static String getDefaultIcon(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return defaultIcons.get(entityClass);
	}

	public static String getDefaultIconByName(final String className) {
		Check.notBlank(className, "Class name cannot be blank");
		return defaultIconsByName.get(className);
	}

	public static Class<?> getEntityClass(final String simpleName) {
		Check.notBlank(simpleName, "Simple name cannot be blank");
		final Class<?> clazz = entityClasses.get(simpleName);
		Check.notNull(clazz, "Entity class not found for name: " + simpleName);
		return clazz;
	}

	/** Gets the entity class by its plural title.
	 * @param pluralTitle the plural title (e.g., "Activities")
	 * @return the entity class or null if not found */
	public static Class<?> getEntityClassByPluralTitle(final String pluralTitle) {
		Check.notBlank(pluralTitle, "Plural title cannot be blank");
		return entityClassesByPluralTitle.get(pluralTitle);
	}

	/** Gets the entity class by its singular title.
	 * @param singularTitle the singular title (e.g., "Activity")
	 * @return the entity class or null if not found */
	public static Class<?> getEntityClassBySingularTitle(final String singularTitle) {
		Check.notBlank(singularTitle, "Singular title cannot be blank");
		return entityClassesBySingularTitle.get(singularTitle);
	}

	/** Gets the entity class by either singular or plural title.
	 * @param title the title (singular or plural)
	 * @return the entity class or null if not found */
	public static Class<?> getEntityClassByTitle(final String title) {
		Check.notBlank(title, "Title cannot be blank");
		// Try singular first
		final Class<?> clazz = entityClassesBySingularTitle.get(title);
		if (clazz != null) {
			return clazz;
		}
		// Try plural
		return entityClassesByPluralTitle.get(title);
	}

	public static Class<?> getEntityServiceClass(final String simpleName) {
		Check.notBlank(simpleName, "Simple name cannot be blank");
		final Class<?> clazz = serviceClasses.get(simpleName);
		Check.notNull(clazz, "Service class not found for entity: " + simpleName);
		return clazz;
	}

	/** Returns all registered entity keys (simple class names).
	 * @return sorted list of registered entity keys */
	public static java.util.List<String> getAllRegisteredEntityKeys() {
		return entityClasses.keySet().stream().sorted().toList();
	}

	/** Gets the plural title for an entity class.
	 * @param entityClass the entity class
	 * @return the plural title or null if not registered */
	public static String getEntityTitlePlural(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return entityTitlesPlural.get(entityClass);
	}

	/** Gets the singular title for an entity class.
	 * @param entityClass the entity class
	 * @return the singular title or null if not registered */
	public static String getEntityTitleSingular(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return entityTitlesSingular.get(entityClass);
	}

	public static Class<?> getInitializerService(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		final Class<?> clazz = initializerServices.get(entityClass);
		Check.notNull(clazz, "Initializer service not found for entity: " + entityClass.getSimpleName());
		return clazz;
	}

	public static Class<?> getPageServiceClass(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		return pageServiceClasses.get(entityClass);
	}

	public static Class<?> getPageServiceClassByName(final String pageServiceName) {
		Check.notBlank(pageServiceName, "Page service name cannot be blank");
		return pageServiceClassesByName.get(pageServiceName);
	}

	public static int getRegisteredCount() { return entityClasses.size(); }

	public static Class<?> getServiceClassByName(final String serviceName) {
		Check.notBlank(serviceName, "Service name cannot be blank");
		final Class<?> clazz = serviceClassesByName.get(serviceName);
		Check.notNull(clazz, "Service class not found for name: " + serviceName);
		return clazz;
	}

	public static Class<?> getServiceClassForEntity(final Class<?> entityClass) {
		Check.notNull(entityClass, "Entity class cannot be null");
		final Class<?> clazz = serviceClassesByEntity.get(entityClass);
		Check.notNull(clazz, "Service class not found for entity: " + entityClass.getSimpleName());
		return clazz;
	}

	public static boolean isInitialized() { return initialized; }

	public static boolean isRegistered(final String simpleName) {
		return entityClasses.containsKey(simpleName);
	}

	/** Marks the registry as initialized. This should be called after all entities have been registered. */
	public static void markInitialized() {
		initialized = true;
	}

	/** Registers an entity and its associated metadata.
	 * @param registrable the entity metadata to register */
	public static void register(final IEntityRegistrable registrable) {
		try {
			Check.notNull(registrable, "Registrable cannot be null");
			Check.notNull(registrable.getEntityClass(), "Entity class cannot be null");
			Check.notNull(registrable.getServiceClass(), "Service class cannot be null");
			Check.notBlank(registrable.getSimpleName(), "Simple name cannot be blank");
			final Class<?> entityClass = registrable.getEntityClass();
			final String simpleName = registrable.getSimpleName();
			final Class<?> serviceClass = registrable.getServiceClass();
			// LOGGER.debug("Registered entity class: {} -> {}", simpleName, entityClass.getName());
			// Register entity class
			entityClasses.put(simpleName, entityClass);
			serviceClasses.put(simpleName, serviceClass);
			serviceClassesByEntity.put(entityClass, serviceClass);
			serviceClassesByName.put(serviceClass.getSimpleName(), serviceClass);
			// Optional initializer service
			// Optional page service
			final Class<?> pageService = registrable.getPageServiceClass();
			if (pageService != null) {
				pageServiceClasses.put(entityClass, pageService);
				pageServiceClassesByName.put(pageService.getSimpleName(), pageService);
			}
			// Optional default icon
			if (registrable instanceof IEntityWithView) {
				final IEntityWithView entityWithView = (IEntityWithView) registrable;
				final Class<?> initializer = entityWithView.getInitializerServiceClass();
				if (initializer != null) {
					initializerServices.put(entityClass, initializer);
				}
				final String defaultIconName = entityWithView.getDefaultIconName();
				if (defaultIconName != null && !defaultIconName.isBlank()) {
					defaultIcons.put(entityClass, defaultIconName);
					defaultIconsByName.put(entityClass.getName(), defaultIconName);
				}
				// Optional default color
				final String defaultColor = entityWithView.getDefaultColor();
				if (defaultColor != null && !defaultColor.isBlank()) {
					defaultColors.put(entityClass, defaultColor);
					defaultColorsByName.put(entityClass.getName(), defaultColor);
				}
				// Entity titles (singular and plural)
				final String singularTitle = entityWithView.getEntityTitleSingular();
				if (singularTitle != null && !singularTitle.isBlank()) {
					entityTitlesSingular.put(entityClass, singularTitle);
					entityClassesBySingularTitle.put(singularTitle, entityClass);
				}
				final String pluralTitle = entityWithView.getEntityTitlePlural();
				if (pluralTitle != null && !pluralTitle.isBlank()) {
					entityTitlesPlural.put(entityClass, pluralTitle);
					entityClassesByPluralTitle.put(pluralTitle, entityClass);
				}
			}
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
