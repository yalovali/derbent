package tech.derbent.api.ui.component;

import com.vaadin.flow.component.orderedlayout.Scroller;
import tech.derbent.api.utils.CAuxillaries;

public class CScroller extends Scroller {
	private static final long serialVersionUID = 1L;

	public CScroller() {
		super();
		initializeComponent();
	}

	/** Common initialization for all CButton instances. This method can be overridden by subclasses to provide additional initialization. */
	protected void initializeComponent() {
		CAuxillaries.setId(this);
		setScrollDirection(Scroller.ScrollDirection.VERTICAL);
		setSizeFull();
	}
}
