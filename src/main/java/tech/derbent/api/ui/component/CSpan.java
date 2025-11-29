package tech.derbent.api.ui.component;

import com.vaadin.flow.component.html.Span;
import tech.derbent.api.utils.CAuxillaries;

public class CSpan extends Span {

	private static final long serialVersionUID = 1L;

	private void initializeComponent() {
		getStyle().set("margin", "0px");
		getStyle().set("padding", "0px");
		getStyle().set("text-align", "center");
	}

	public CSpan() {
		this("");
		initializeComponent();
	}

	public CSpan(String text) {
		super(text);
		initializeComponent();
	}

	public CSpan(String text, int i) {
		super(text);
		initializeComponent();
		setWidth(CAuxillaries.formatWidthPx(i));
	}
}
