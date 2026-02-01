package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.html.H6;

public class CH6 extends H6 {

	private static final long serialVersionUID = 1L;

	public CH6(String text) {
		super(text);
		initializeComponent();
	}

	private void initializeComponent() {
		// getStyle().set("text-align", "center");
		getStyle().set("display", "flex").set("justify-content", "space-evenly");
		setWidthFull();
		// Height set to null for proper sizing (no setHeightFull - makes headers look ugly)
		setHeight(null);
		getStyle().set("margin", "0");
		getStyle().set("padding", "0");
	}
}
