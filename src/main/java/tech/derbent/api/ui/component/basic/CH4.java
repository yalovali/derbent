package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.html.H4;

public class CH4 extends H4 {

	private static final long serialVersionUID = 1L;

	public CH4(String text) {
		super(text);
		initializeComponent();
	}

	private void initializeComponent() {
		// getStyle().set("text-align", "center");
		getStyle().set("display", "flex").set("justify-content", "space-evenly");
		setWidthFull();
		setHeightFull();
	}
}
