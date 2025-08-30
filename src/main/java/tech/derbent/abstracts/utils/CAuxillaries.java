package tech.derbent.abstracts.utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import com.vaadin.flow.component.Component;

public class CAuxillaries {
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

	private static String getComponentText(final Component component) {
		if (component instanceof com.vaadin.flow.component.HasText) {
			return ((com.vaadin.flow.component.HasText) component).getText();
		}
		return null;
	}

	public static List<String> invokeStaticMethod(final String className, final String methodName) throws Exception {
		final Class<?> clazz = Class.forName(className);
		final Method method = clazz.getMethod(methodName);
		Check.notNull(method, "Method " + methodName + " not found in class " + className);
		if (Modifier.isStatic(method.getModifiers()) && List.class.isAssignableFrom(method.getReturnType())) {
			@SuppressWarnings ("unchecked")
			final List<String> result = (List<String>) method.invoke(null);
			// final List<?> rawList = (List<?>) result;
			// Convert to List<String> if possible
			// final List<String> stringList = rawList.stream().filter(item -> item instanceof String).map(item -> (String) item).toList();
			return result;
		} else {
			throw new IllegalArgumentException("Method " + methodName + " in class " + className + " is not static or does not return List<String>");
		}
	}

	public static void setId(final Component component) {
		final String id = generateId(component);
		component.setId(id);
	}
}
