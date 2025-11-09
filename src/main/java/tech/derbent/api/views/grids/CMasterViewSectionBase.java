package tech.derbent.api.views.grids;

import java.util.List;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.api.views.components.CVerticalLayout;
import tech.derbent.api.views.grids.CMasterViewSectionGrid.SelectionChangeEvent;

public abstract class CMasterViewSectionBase<EntityClass extends CEntityDB<EntityClass>> extends CVerticalLayout {

	private static final long serialVersionUID = 1L;
	protected final Class<EntityClass> entityClass;
	protected final CAbstractEntityDBPage<EntityClass> page;

	public CMasterViewSectionBase(final Class<EntityClass> entityClass, final CAbstractEntityDBPage<EntityClass> page) {
		super();
		this.entityClass = entityClass;
		this.page = page;
		setSizeFull();
		// Initialization code here
	}

	// --- Listener Registration ---
	@SuppressWarnings ({
			"unchecked", "rawtypes"
	})
	public Registration addSelectionChangeListener(final ComponentEventListener<SelectionChangeEvent<EntityClass>> listener) {
		System.out.println("=== BASE: addSelectionChangeListener() called on " + this.getClass().getSimpleName() + " ===");
		System.out.println("=== BASE: Registering listener for event type: " + SelectionChangeEvent.class.getName() + " ===");
		Registration reg = addListener(SelectionChangeEvent.class, (ComponentEventListener) listener);
		System.out.println("=== BASE: Listener registered successfully ===");
		return reg;
	}

	public abstract void createMasterView() throws Exception;
	// Additional methods and properties can be added here
	public abstract EntityClass getSelectedItem();
	public abstract void refreshMasterView() throws Exception;
	public abstract void select(EntityClass object);
	public abstract void selectLastOrFirst(EntityClass orElse);
	public abstract void setDataProvider(CallbackDataProvider<EntityClass, Void> masterQuery) throws Exception;
	public abstract void setItems(List<EntityClass> filteredMeetings) throws Exception;
}
