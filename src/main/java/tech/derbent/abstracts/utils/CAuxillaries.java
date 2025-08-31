package tech.derbent.abstracts.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.Component;

public class CAuxillaries {
	public static final Logger LOGGER = LoggerFactory.getLogger(CAuxillaries.class);

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

	private static Method getClazzMethodStatic(final Class<?> clazz, final String methodName) throws ClassNotFoundException, NoSuchMethodException {
		try {
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

	private static Method getClazzMethodStatic(final String className, final String methodName) throws ClassNotFoundException, NoSuchMethodException {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class<?> clazz = Class.forName(className, true, cl);
		return getClazzMethodStatic(clazz, methodName);
	}

	private static String getComponentText(final Component component) {
		if (component instanceof com.vaadin.flow.component.HasText) {
			return ((com.vaadin.flow.component.HasText) component).getText();
		}
		return null;
	}

	public static List<String> invokeStaticMethodOfList(final String className, final String methodName) throws Exception {
		final Method method = getClazzMethodStatic(className, methodName);
		if (!List.class.isAssignableFrom(method.getReturnType())) {
			throw new IllegalArgumentException("Method " + methodName + " in class " + className + " is not static or does not return List<String>");
		}
		@SuppressWarnings ("unchecked")
		final List<String> result = (List<String>) method.invoke(null);
		return result;
	}

	public static String invokeStaticMethodOfStr(final Class<?> clazz, final String methodName) throws Exception {
		final Method method = getClazzMethodStatic(clazz, methodName);
		if (method.getReturnType() != String.class) {
			throw new RuntimeException("Method " + methodName + " in class " + clazz.getName() + " does not return String");
		}
		return (String) method.invoke(null);
	}

	public static String invokeStaticMethodOfStr(final String className, final String methodName) throws Exception {
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final Class<?> clazz = Class.forName(className, true, cl);
		return invokeStaticMethodOfStr(clazz, methodName);
	}

	public static void setId(final Component component) {
		final String id = generateId(component);
		component.setId(id);
	}
}
