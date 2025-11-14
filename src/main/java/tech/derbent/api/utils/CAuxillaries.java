package tech.derbent.api.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.app.gannt.view.CProjectGanntView;

public class CAuxillaries {

	public static final Logger LOGGER = LoggerFactory.getLogger(CAuxillaries.class);

	public static String formatWidthPx(int i) {
		if (i <= 0) {
			return null;
		}
		return i + "px";
	}

	public static String generateId(final Component component) {
		final String prefix = component.getClass().getSimpleName().toLowerCase();
		String suffix;
		final String text = getComponentText(component);
		if ((text != null) && !text.trim().isEmpty()) {
			suffix = text.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
		} else {
			final String tag = component.getElement().getTag();
			if ((tag != null) && !tag.trim().isEmpty()) {
				suffix = tag.toLowerCase() + "-" + System.currentTimeMillis();
			} else {
				suffix = String.valueOf(System.currentTimeMillis());
			}
		}
		return prefix + "-" + suffix;
	}

	/** Get available entity types for screen configuration.
	 * @return list of entity types */
	public static List<String> getAvailableEntityTypes() {
		return List.of("CActivity", "CMeeting", "CRisk", "CProject", "CUser");
	}

	private static Method getClazzMethod(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = clazz.getMethod(methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			Check.isTrue(method.getParameterCount() == 0, "Method " + methodName + " in class " + clazz.getName() + " has parameters");
			return method;
		} catch (final Exception e) {
			LOGGER.error("Error getting method " + methodName + " from class " + clazz.getName(), e);
			throw e;
		}
	}

	private static Method getClazzMethodStatic(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = clazz.getMethod(methodName);
			if (Modifier.isStatic(method.getModifiers())) {
				return method;
			} else {
				throw new IllegalArgumentException("Method " + methodName + " in class " + clazz.getName() + " is not statric");
			}
		} catch (final Exception e) {
			LOGGER.error("Error getting method " + methodName + " from class " + clazz.getName(), e);
			throw e;
		}
	}

	private static Method getClazzMethodStatic(final String className, final String methodName) throws Exception {
		Check.notBlank(className, "className is blank");
		Check.notBlank(methodName, "methodName is blank");
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Check.notNull(cl, "ClassLoader is null");
		final Class<?> clazz = Class.forName(className, true, cl);
		Check.notNull(clazz, "Class " + className + " not found");
		return getClazzMethodStatic(clazz, methodName);
	}

	private static String getComponentText(final Component component) {
		Check.notNull(component, "component is null");
		if (component instanceof com.vaadin.flow.component.HasText) {
			return ((com.vaadin.flow.component.HasText) component).getText();
		}
		return null;
	}

	// convert all cases to use this method instead of reflection
	public static Class<?> getEntityClass(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		try {
			return CEntityRegistry.getEntityClass(simpleName);
		} catch (final IllegalArgumentException e) {
			LOGGER.error("Unknown entity type: {} - Entity not registered in registry", simpleName);
			throw e;
		}
	}

	public static Class<?> getEntityServiceClasses(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		try {
			return CEntityRegistry.getEntityServiceClass(simpleName);
		} catch (final IllegalArgumentException e) {
			LOGGER.error("Unknown entity type: {} - Service not registered in registry", simpleName);
			throw e;
		}
	}

	public static Class<?> getInitializerService(final Class<?> entityClass) {
		try {
			return CEntityRegistry.getInitializerService(entityClass);
		} catch (final IllegalArgumentException e) {
			LOGGER.error("Unknown entity type: {} - Initializer not registered in registry", entityClass.getSimpleName());
			throw e;
		}
	}

	public static Class<?> getInitializerService(final String entityType) {
		Check.notBlank(entityType, "Entity type must not be empty");
		Class<?> clazz = getEntityClass(entityType);
		return getInitializerService(clazz);
	}

	/** Get a method from a class without caching.
	 * @param clazz          the class containing the method
	 * @param methodName     the method name
	 * @param parameterTypes the parameter types (if any)
	 * @return the Method object or null if not found */
	public static Method getMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
		try {
			Check.notNull(clazz, "clazz is null");
			Check.notBlank(methodName, "methodName is blank");
			final Method method = clazz.getMethod(methodName, parameterTypes);
			method.setAccessible(true);
			return method;
		} catch (final NoSuchMethodException e) {
			LOGGER.warn("Method not found: {}.{}", clazz.getSimpleName(), methodName);
			return null;
		}
	}

	public static Class<?> getServiceClassForEntity(final Class<?> entityClass) {
		try {
			return CEntityRegistry.getServiceClassForEntity(entityClass);
		} catch (final IllegalArgumentException e) {
			LOGGER.error("Unknown entity type: {} - Service not registered in registry", entityClass.getSimpleName());
			throw e;
		}
	}

	public static Class<?> getServiceClassFromName(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		try {
			return CEntityRegistry.getServiceClassByName(simpleName);
		} catch (final IllegalArgumentException e) {
			LOGGER.error("Unknown service type: {} - Service not registered in registry", simpleName);
			throw e;
		}
	}

	public static Class<?> getViewClassForEntity(final String simpleName) {
		Check.notBlank(simpleName, "Entity type must not be empty");
		switch (simpleName) {
		case "CProjectGanntView":
			return CProjectGanntView.class;
		default:
			LOGGER.error("Unknown entity type: " + simpleName + " dont forget to update CAuxillaries");
			throw new IllegalArgumentException("Unknown entity type: " + simpleName);
		}
	}

	/** Safely invokes a method on an object.
	 * @param target     the target object
	 * @param methodName the method name
	 * @param args       the method arguments
	 * @return the method result or null if invocation failed
	 * @throws Exception */
	public static Object invokeMethod(final Object target, final String methodName, final Object... args) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(target, "target is null");
			Class<?>[] paramTypes = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
			}
			Method method = getMethod(target.getClass(), methodName, paramTypes);
			if (method != null) {
				return method.invoke(target, args);
			}
			method = getMethod(target.getClass(), methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + target.getClass().getName());
			return method.invoke(target);
		} catch (final Exception e) {
			LOGGER.error("Failed to invoke method {}.{}: {}", target.getClass().getSimpleName(), methodName, e.getMessage());
			throw e;
		}
	}

	public static String invokeMethodOfString(final Object entity, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(entity, "clazz is null");
			final Method method = getClazzMethod(entity.getClass(), methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + entity.getClass().getName());
			// check the method returns String
			Check.isTrue(method.getReturnType() == String.class,
					"Method " + methodName + " in class " + entity.getClass().getName() + " does not return String");
			// invoke the method and get the result
			final String result = (String) method.invoke(entity);
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error invoking method " + methodName + " of class " + entity.getClass().getName(), e);
			throw e;
		}
	}

	public static void invokeMethodOfVoid(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = getClazzMethod(clazz, methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			if (method.getReturnType() != void.class) {
				throw new RuntimeException("Method " + methodName + " in class " + clazz.getName() + " does not return void");
			}
			method.invoke(null);
		} catch (final Exception e) {
			LOGGER.error("Error invoking method " + methodName + " of class " + clazz.getName(), e);
			throw e;
		}
	}

	public static List<String> invokeStaticMethodOfList(final String className, final String methodName) throws Exception {
		try {
			Check.notBlank(className, "className is blank");
			Check.notBlank(methodName, "methodName is blank");
			final Method method = getClazzMethodStatic(className, methodName);
			if (!List.class.isAssignableFrom(method.getReturnType())) {
				throw new IllegalArgumentException(
						"Method " + methodName + " in class " + className + " is not static or does not return List<String>");
			}
			@SuppressWarnings ("unchecked")
			final List<String> result = (List<String>) method.invoke(null);
			return result;
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + className, e);
			throw e;
		}
	}

	public static String invokeStaticMethodOfStr(final Class<?> clazz, final String methodName) throws Exception {
		try {
			Check.notBlank(methodName, "methodName is blank");
			Check.notNull(clazz, "clazz is null");
			final Method method = getClazzMethodStatic(clazz, methodName);
			Check.notNull(method, "Method " + methodName + " not found in class " + clazz.getName());
			if (method.getReturnType() != String.class) {
				throw new RuntimeException("Method " + methodName + " in class " + clazz.getName() + " does not return String");
			}
			return (String) method.invoke(null);
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + clazz.getName(), e);
			throw e;
		}
	}

	public static String invokeStaticMethodOfStr(final String className, final String methodName) throws Exception {
		try {
			Check.notBlank(className, "className is blank");
			Check.notBlank(methodName, "methodName is blank");
			final ClassLoader cl = Thread.currentThread().getContextClassLoader();
			Check.notNull(cl, "ClassLoader is null");
			final Class<?> clazz = Class.forName(className, true, cl);
			Check.notNull(clazz, "Class " + className + " not found");
			return invokeStaticMethodOfStr(clazz, methodName);
		} catch (final Exception e) {
			LOGGER.error("Error invoking static method " + methodName + " of class " + className, e);
			throw e;
		}
	}

	public static void setId(final Component component) {
		Check.notNull(component, "component is null");
		final String id = generateId(component);
		component.setId(id);
	}
}
