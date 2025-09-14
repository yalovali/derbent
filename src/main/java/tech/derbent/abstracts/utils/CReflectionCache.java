package tech.derbent.abstracts.utils;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** CReflectionCache - Utility class for caching reflection operations to improve performance. Reduces overhead of repeated reflection calls by
 * caching Method objects. Layer: Utility (MVC) */
public final class CReflectionCache {

	private static final Logger LOGGER = LoggerFactory.getLogger(CReflectionCache.class);
	private static final ConcurrentMap<String, Method> METHOD_CACHE = new ConcurrentHashMap<>();

	/** Gets a method from cache or caches it if not present.
	 * @param clazz          the class containing the method
	 * @param methodName     the method name
	 * @param parameterTypes the parameter types (if any)
	 * @return the Method object or null if not found */
	public static Method getCachedMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		String key = createCacheKey(clazz, methodName, parameterTypes);
		return METHOD_CACHE.computeIfAbsent(key, k -> {
			try {
				Method method = clazz.getMethod(methodName, parameterTypes);
				method.setAccessible(true);
				return method;
			} catch (NoSuchMethodException e) {
				LOGGER.debug("Method not found: {}.{}", clazz.getSimpleName(), methodName);
				return null;
			}
		});
	}

	/** Safely invokes a cached method on an object.
	 * @param target     the target object
	 * @param methodName the method name
	 * @param args       the method arguments
	 * @return the method result or null if invocation failed */
	public static Object safeInvoke(Object target, String methodName, Object... args) {
		if (target == null) {
			return null;
		}
		Class<?>[] paramTypes = new Class<?>[args.length];
		for (int i = 0; i < args.length; i++) {
			paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
		}
		Method method = getCachedMethod(target.getClass(), methodName, paramTypes);
		if (method == null) {
			// Try with no parameters if parameter matching failed
			method = getCachedMethod(target.getClass(), methodName);
		}
		if (method != null) {
			try {
				return method.invoke(target, args);
			} catch (Exception e) {
				LOGGER.debug("Failed to invoke method {}.{}: {}", target.getClass().getSimpleName(), methodName, e.getMessage());
			}
		}
		return null;
	}

	/** Clears the method cache. Useful for testing or memory management. */
	public static void clearCache() {
		METHOD_CACHE.clear();
		LOGGER.debug("Reflection cache cleared");
	}

	/** Gets the current cache size.
	 * @return the number of cached methods */
	public static int getCacheSize() { return METHOD_CACHE.size(); }

	private static String createCacheKey(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
		StringBuilder key = new StringBuilder(clazz.getName()).append("#").append(methodName);
		if (parameterTypes.length > 0) {
			key.append("(");
			for (int i = 0; i < parameterTypes.length; i++) {
				if (i > 0)
					key.append(",");
				key.append(parameterTypes[i].getName());
			}
			key.append(")");
		}
		return key.toString();
	}

	private CReflectionCache() {
		// Utility class - prevent instantiation
	}
}
