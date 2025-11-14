package tech.derbent.api.ui;

import java.util.List;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.shared.Registration;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.ui.component.CVerticalLayout;
import tech.derbent.api.views.CAbstractEntityDBPage;

public abstract class CMasterViewSectionBase<EntityClass extends CEntityDB<EntityClass>> extends CVerticalLayout {

	// --- Custom Event Definition ---
	public static class SelectionChangeEvent<T extends CEntityDB<T>> extends ComponentEvent<CMasterViewSectionBase<T>> {

		private static final long serialVersionUID = 1L;
		private final T selectedItem;

		public SelectionChangeEvent(final CMasterViewSectionBase<T> source, final T selectedItem) {
			super(source, false);
			this.selectedItem = selectedItem;
		}

		public T getSelectedItem() { return selectedItem; }
	}

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

	public abstract void createMasterView() throws Exception;
	// Additional methods and properties can be added here
	public abstract EntityClass getSelectedItem();
	public abstract void refreshMasterView() throws Exception;
	public abstract void select(EntityClass object);
	public abstract void selectLastOrFirst(EntityClass orElse);
	public abstract void setDataProvider(CallbackDataProvider<EntityClass, Void> masterQuery) throws Exception;
	public abstract void setItems(List<EntityClass> filteredMeetings) throws Exception;
}
