package tech.derbent.api.ui.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.api.ui.component.basic.CH3;
import tech.derbent.api.ui.notifications.CNotificationService;
import tech.derbent.api.utils.CAuxillaries;
import tech.derbent.api.utils.Check;

public abstract class CDialog extends Dialog {

	private static final long serialVersionUID = 1L;
	protected final HorizontalLayout buttonLayout = new HorizontalLayout();
	private CH3 formTitle;
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected VerticalLayout mainLayout;

	/** Constructor for CDialog. Initializes the dialog with a default layout. */
	public CDialog() {
		super();
		initializeDialog();
	}

	/** Child must implement: dialog header title. */
	public abstract String getDialogTitleString();
	/** Child must implement: form title.
	 * @throws Exception */
	protected abstract Icon getFormIcon() throws Exception;

	public H3 getFormTitle() { return formTitle; }

	/** Child must implement: form title. */
	protected abstract String getFormTitleString();

	/** Common initialization for all CDialog instances. */
	protected final void initializeDialog() {
		CAuxillaries.setId(this);
		// LOGGER.debug("CDialog initialized with ID: {}", getId().orElse("none"));
	}

	protected abstract void setupButtons();
	protected abstract void setupContent() throws Exception;

	/* call this class in child constructor after all fields are initialized, use setupContent and setupButtons to customize content */
	protected void setupDialog() throws Exception {
		try {
			LOGGER.debug("Setting up dialog: {}", getDialogTitleString());
			setHeaderTitle(getHeaderTitle());
			setModal(true);
			setCloseOnEsc(true);
			setCloseOnOutsideClick(false);
			setWidth("500px");
			mainLayout = new VerticalLayout();
			mainLayout.setPadding(false);
			mainLayout.setSpacing(true);
			final HorizontalLayout headerLayout = new HorizontalLayout();
			headerLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
			headerLayout.setSpacing(true);
			final Icon icon = getFormIcon();
			Check.notNull(icon, "Form icon cannot be null");
			icon.setSize("24px");
			headerLayout.add(icon);
			formTitle = new CH3(getFormTitleString());
			headerLayout.add(formTitle);
			mainLayout.add(headerLayout);
			add(mainLayout);
			//
			buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
			buttonLayout.getStyle().set("margin-top", "16px");
			getFooter().add(buttonLayout);
			setupContent();
			setupButtons();
			// Add colorful border and background to make dialog more appealing
			getElement().getStyle().set("border", "2px solid #1976D2");
			getElement().getStyle().set("border-radius", "12px");
			getElement().getStyle().set("box-shadow", "0 4px 20px rgba(25, 118, 210, 0.3)");
			// Set a subtle gradient background
			getElement().getStyle().set("background", "linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%)");
		} catch (final Exception e) {
			CNotificationService.showException("Error setting up dialog", e);
		}
	}
}
