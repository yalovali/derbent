package tech.derbent.api.registry;

import tech.derbent.api.utils.CColorUtils;

public interface IEntityWithView {

	default String getDefaultColor() throws Exception { return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_COLOR"); }

	default String getDefaultIconName() throws Exception { return CColorUtils.getStaticStringValue(getEntityClass(), "DEFAULT_ICON"); }

	Class<?> getEntityClass();

	default String getEntityTitlePlural() throws Exception { return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_PLURAL"); }

	/** Gets the singular title for this entity (e.g., "Activity", "User", "Project").
	 * @return the singular entity title
	 * @throws Exception */
	default String getEntityTitleSingular() throws Exception {
		return CColorUtils.getStaticStringValue(getEntityClass(), "ENTITY_TITLE_SINGULAR");
	}

	Class<?> getInitializerServiceClass();
}
