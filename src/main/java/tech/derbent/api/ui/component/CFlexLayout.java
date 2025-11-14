package tech.derbent.api.ui.component;

import com.vaadin.flow.component.orderedlayout.FlexLayout;

public class CFlexLayout extends FlexLayout {

	private static final long serialVersionUID = 1L;

	public static CFlexLayout forEntityPage() {
		final CFlexLayout layout = new CFlexLayout();
		layout.setClassName("base-details-layout");
		layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
		layout.setFlexDirection(FlexLayout.FlexDirection.ROW);
		layout.setAlignItems(FlexLayout.Alignment.STRETCH);
		layout.setJustifyContentMode(FlexLayout.JustifyContentMode.START);
		return layout;
	}

	/** Default constructor. */
	public CFlexLayout() {
		super();
		initializeFlexLayout();
	}

	private void initializeFlexLayout() {
		setSizeFull();
		setWidthFull();
	}
}
