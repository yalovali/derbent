package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.html.H3;

public class CH3 extends H3 {

	private static final long serialVersionUID = 1L;

	public CH3(String text) {
		super(text);
		initilizeComponent();
	}

	private void initilizeComponent() {
		// getStyle().set("text-align", "center");
		getStyle().set("display", "flex").set("justify-content", "space-evenly");
		setWidthFull();
		setHeightFull();
	}
}
