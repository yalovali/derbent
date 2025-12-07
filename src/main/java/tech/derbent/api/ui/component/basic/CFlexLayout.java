package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.orderedlayout.FlexLayout;

/** CFlexLayout - Enhanced base class for flex layouts in the application.
 * <p>
 * Note: This class already implements ClickNotifier through Vaadin's FlexLayout base class. */
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
		initializeComponent();
	}

	private void initializeComponent() {
		setSizeFull();
		setWidthFull();
	}
}
