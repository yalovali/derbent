package tech.derbent.api.ui.component;

import com.vaadin.flow.component.html.Span;
import tech.derbent.api.utils.CAuxillaries;

public class CSpan extends Span {

	private static final long serialVersionUID = 1L;

	private void initComponent() {
		getStyle().set("margin", "0px");
		getStyle().set("padding", "0px");
		getStyle().set("text-align", "center");
	}

	public CSpan() {
		this("");
		initComponent();
	}

	public CSpan(String text) {
		super(text);
		initComponent();
	}

	public CSpan(String text, int i) {
		super(text);
		initComponent();
		setWidth(CAuxillaries.formatWidthPx(i));
	}
}
