package tech.derbent.api.registry;

import tech.derbent.api.utils.CColorUtils;

/** Interface for entities and services that can register themselves with the entity registry. Implementing classes should provide metadata about
 * themselves for fast lookup. */
public interface IEntityRegistrable {

	default String getDefaultColor() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_COLOR");
		} catch (Exception e) {
			e.printStackTrace();
			return "0x123456";
		}
	}

	default String getDefaultIconName() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_ICON");
		} catch (Exception e) {
			e.printStackTrace();
			return "vaadin:tasks";
		}
	}

	Class<?> getEntityClass();
	Class<?> getInitializerServiceClass();
	Class<?> getPageServiceClass();
	Class<?> getServiceClass();

	default String getSimpleName() { return getEntityClass().getSimpleName(); }

	/** Gets the singular title for this entity (e.g., "Activity", "User", "Project").
	 * @return the singular entity title */
	default String getEntityTitleSingular() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_SINGULAR");
		} catch (Exception e) {
			// Fallback: derive from class name by removing C prefix
			String simpleName = getEntityClass().getSimpleName();
			if (simpleName.startsWith("C")) {
				simpleName = simpleName.substring(1);
			}
			return simpleName;
		}
	}

	/** Gets the plural title for this entity (e.g., "Activities", "Users", "Projects").
	 * @return the plural entity title */
	default String getEntityTitlePlural() {
		try {
			return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_PLURAL");
		} catch (Exception e) {
			// Fallback: derive from singular + "s"
			return getEntityTitleSingular() + "s";
		}
	}
}
