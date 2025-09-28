package tech.derbent.api.interfaces;
/** Interface for content owners (like pages) that can provide context-specific data for content resource resolvers such as comboboxes in forms. This
 * allows FormBuilder to access methods on the current page/context owner instead of just service beans, enabling context-aware data providers.
 * Enhanced to support better access to parent container data and context.
 * @author Derbent Framework
 * @since 1.0 */
public interface IContentOwner {

	Object getCurrentEntity();
	void setCurrentEntity(Object entity);
	void populateForm() throws Exception;
}
