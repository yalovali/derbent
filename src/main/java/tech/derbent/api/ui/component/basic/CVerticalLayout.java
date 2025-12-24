package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.utils.CAuxillaries;

/** CVerticalLayout - Enhanced base class for vertical layouts in the application. Layer: View (MVC) Provides common initialization patterns, utility
 * methods for spacing, padding, and sizing operations. Extends Vaadin VerticalLayout with application-specific enhancements.
 * <p>
 * Note: This class already implements ClickNotifier through Vaadin's VerticalLayout base class. */
public class CVerticalLayout extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/** Default constructor with no padding, no spacing, and full size. */
	public CVerticalLayout() {
		super();
		initializeComponent();
	}

	/** Constructor with explicit padding, spacing, and margin settings. */
	public CVerticalLayout(final boolean padding, final boolean spacing, final boolean margin) {
		this();
		setPadding(padding);
		setSpacing(spacing);
		setMargin(margin);
	}

	protected void initializeComponent() {
		CAuxillaries.setId(this);
		setSizeFull();
		setPadding(false);
		setSpacing(false);
		setMargin(false);
		setDefaultHorizontalComponentAlignment(Alignment.START);
		setJustifyContentMode(JustifyContentMode.START);
	}
}
