package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.utils.CAuxillaries;

public class CVerticalLayoutTop extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	/** Default constructor with no padding, no spacing, and full size. */
	public CVerticalLayoutTop() {
		super();
		initializeComponent();
	}

	/** Constructor with configurable padding, spacing, and margin. */
	public CVerticalLayoutTop(final boolean padding, final boolean spacing, final boolean margin) {
		this();
		setPadding(padding);
		setSpacing(spacing);
		setMargin(margin);
	}

	private void initializeComponent() {
		CAuxillaries.setId(this);
		// 🔥 Ensures layout stretches vertically, leaving space at bottom
		setSizeFull();
		// 🔥 Prevent default spacing/margins unless explicitly set
		setPadding(false);
		setSpacing(true);
		setMargin(false);
		// 🔥 Aligns all children to top-left (vertical: top, horizontal: left)
		setJustifyContentMode(JustifyContentMode.START);
		setDefaultHorizontalComponentAlignment(Alignment.START);
	}
}
