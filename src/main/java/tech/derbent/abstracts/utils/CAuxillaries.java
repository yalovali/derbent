package tech.derbent.abstracts.utils;

import com.vaadin.flow.component.Component;

public class CAuxillaries {

	public static String generateId(final Component component) {
		final String prefix = component.getClass().getSimpleName().toLowerCase();
		String suffix;
		final String text = getComponentText(component);

		if ((text != null) && !text.trim().isEmpty()) {
			suffix = text.toLowerCase().trim().replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
		}
		else {
			final String tag = component.getElement().getTag();

			if ((tag != null) && !tag.trim().isEmpty()) {
				suffix = tag.toLowerCase() + "-" + System.currentTimeMillis();
			}
			else {
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

	public static void setId(final Component component) {
		final String id = generateId(component);
		component.setId(id);
	}
}
