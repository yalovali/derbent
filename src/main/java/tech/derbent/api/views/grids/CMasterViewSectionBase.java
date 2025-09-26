package tech.derbent.api.views.grids;

import java.util.List;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.views.CAbstractEntityDBPage;
import tech.derbent.api.views.components.CDiv;
import tech.derbent.api.views.grids.CMasterViewSectionGrid.SelectionChangeEvent;

public abstract class CMasterViewSectionBase<EntityClass extends CEntityDB<EntityClass>> extends CDiv {

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
		return addListener(SelectionChangeEvent.class, (ComponentEventListener) listener);
	}

	public abstract void createMasterView();
	// Additional methods and properties can be added here
	public abstract EntityClass getSelectedItem();
	public abstract void refreshMasterView();
	public abstract void select(EntityClass object);
	public abstract void selectLastOrFirst(EntityClass orElse);
	public abstract void setDataProvider(CallbackDataProvider<EntityClass, Void> masterQuery);
	public abstract void setItems(List<EntityClass> filteredMeetings);
}
