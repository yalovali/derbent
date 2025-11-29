package tech.derbent.api.ui.component;

import com.vaadin.flow.component.tabs.TabSheet;
import tech.derbent.api.utils.CAuxillaries;

public class CTabSheet extends TabSheet {
	private static final long serialVersionUID = 1L;

	public CTabSheet() {
		super();
		initializeComponent();
	}

	/** Common initialization for all CButton instances. This method can be overridden by subclasses to provide additional initialization. */
	protected void initializeComponent() {
		setSizeFull();
		CAuxillaries.setId(this);
	}
}
