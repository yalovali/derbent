package tech.derbent.api.interfaces;
/** Interface for content owners (like pages) that can provide context-specific data for content resource resolvers such as comboboxes in forms. This
 * allows FormBuilder to access methods on the current page/context owner instead of just service beans, enabling context-aware data providers.
 * Enhanced to support better access to parent container data and context.
 * @author Derbent Framework
 * @since 1.0 */
public interface IContentOwner {

	/** Gets the current active entity being displayed/edited by this content owner. This can be used by data providers to create context-specific
	 * content.
	 * @return the current entity, or null if no entity is active */
	Object getCurrentEntity();

	/** Gets a named context value from this content owner. This allows data providers to access specific context information. Common context names
	 * include: - "currentEntity" - returns the same as getCurrentEntity() - "currentUser" - returns the current user if available - "currentProject"
	 * - returns the current project if available - "parentEntity" - returns the parent entity if this is a nested view
	 * @param contextName the name of the context value to retrieve
	 * @return the context value, or null if not found */
	default Object getContextValue(String contextName) {
		if ("currentEntity".equals(contextName)) {
			return getCurrentEntity();
		}
		return null;
	}

	/** Gets the parent content owner if this is a nested content owner. This allows accessing data from parent containers in hierarchical views.
	 * @return the parent content owner, or null if this is the root content owner */
	default IContentOwner getParentContentOwner() { return null; }

	/** Resolves a context value by first checking this content owner, then recursively checking parent content owners up the hierarchy.
	 * @param contextName the name of the context value to retrieve
	 * @return the context value from this or any parent content owner, or null if not found */
	default Object resolveContextValue(String contextName) {
		Object value = getContextValue(contextName);
		if (value != null) {
			return value;
		}
		IContentOwner parent = getParentContentOwner();
		if (parent != null) {
			return parent.resolveContextValue(contextName);
		}
		return null;
	}
}
