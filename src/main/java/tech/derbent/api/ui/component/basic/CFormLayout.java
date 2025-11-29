package tech.derbent.api.ui.component.basic;

import com.vaadin.flow.component.formlayout.FormLayout;
import tech.derbent.api.utils.CAuxillaries;

/** CFormLayout - Enhanced base class for form layouts in the application. Layer: View (MVC) Provides common initialization patterns, utility methods
 * for responsive design, field organization, and styling. Extends Vaadin FormLayout with application-specific enhancements. */
public class CFormLayout extends FormLayout {

	private static final long serialVersionUID = 1L;

	/** Creates a single-column form layout.
	 * @return new CFormLayout with single column */
	public static CFormLayout singleColumn() {
		final CFormLayout form = new CFormLayout();
		form.setResponsiveSteps(new ResponsiveStep("0", 1));
		return form;
	}

	/** Default constructor. */
	public CFormLayout() {
		super();
		initializeComponent();
	}

	/** Common initialization for all CFormLayout instances. */
	protected void initializeComponent() {
		CAuxillaries.setId(this);
		// Set default responsive behavior
		setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("500px", 2));
	}
}
