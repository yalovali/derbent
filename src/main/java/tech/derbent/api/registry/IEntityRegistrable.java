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

	default String getDefaultIconName() { // TODO Auto-generated method stub
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
}
