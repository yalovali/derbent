package tech.derbent.api.registry;

import tech.derbent.api.utils.CColorUtils;

public interface IEntityWithView {

	default String getDefaultColor() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_COLOR");
		} catch (final Exception e) {
			e.printStackTrace();
			return "0x123456";
		}
	}

	default String getDefaultIconName() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_ICON");
		} catch (final Exception e) {
			e.printStackTrace();
			return "vaadin:tasks";
		}
	}

	Class<?> getEntityClass();

	default String getEntityTitlePlural() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_PLURAL");
		} catch (@SuppressWarnings ("unused") final Exception e) {
			// Fallback: derive from singular + "s"
			return getEntityTitleSingular() + "s";
		}
	}

	/** Gets the singular title for this entity (e.g., "Activity", "User", "Project").
	 * @return the singular entity title */
	default String getEntityTitleSingular() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_SINGULAR");
		} catch (@SuppressWarnings ("unused") final Exception e) {
			// Fallback: derive from class name by removing C prefix
			String simpleName = getEntityClass().getSimpleName();
			if (simpleName.startsWith("C")) {
				simpleName = simpleName.substring(1);
			}
			return simpleName;
		}
	}

	Class<?> getInitializerServiceClass();
}
