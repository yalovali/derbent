package tech.derbent.api.ui.component.basic;

import java.util.Collection;
import com.vaadin.flow.component.Component;

public class CFormHorizontalLayout extends CHorizontalLayout {

	private static final long serialVersionUID = 1L;
	private boolean haveNextOneOnSameLine = false;

	public CFormHorizontalLayout() {
		super(false, true, false);
		setClassName("form-field-layout");
		setAlignItems(Alignment.BASELINE);
	}

	@Override
	public void add(Collection<Component> components) {
		super.add(components);
	}

	public boolean isHaveNextOneOnSameLine() { return haveNextOneOnSameLine; }

	public void setHaveNextOneOnSameLine(boolean haveNextOneOnSameLine) { this.haveNextOneOnSameLine = haveNextOneOnSameLine; }
}
