package tech.derbent.api.interfaces;

import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.services.CAbstractService;

/** Interface for content owners (like pages) that can provide context-specific data for content resource resolvers such as comboboxes in forms. This
 * allows FormBuilder to access methods on the current page/context owner instead of just service beans, enabling context-aware data providers.
 * Enhanced to support better access to parent container data and context.
 * @author Derbent Framework
 * @since 1.0 */
public interface IContentOwner {

	public CEntityDB<?> createNewEntityInstance() throws Exception;
	Object getCurrentEntity();
	String getCurrentEntityIdString();
	public CAbstractService<?> getEntityService();
	void populateForm() throws Exception;
	void setCurrentEntity(Object entity);
}
