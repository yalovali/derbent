package tech.derbent.abstracts.views.dialogs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import tech.derbent.abstracts.utils.CAuxillaries;

public abstract class CDialog extends Dialog {
	private static final long serialVersionUID = 1L;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	protected VerticalLayout mainLayout;
	protected final HorizontalLayout buttonLayout = new HorizontalLayout();
	private boolean setupDone = false;

	/** Constructor for CDialog. Initializes the dialog with a default layout. */
	public CDialog() {
		super();
		initializeDialog();
	}

	/** Child must implement: form title. */
	protected abstract Icon getFormIcon();

	/** Child must implement: form title. */
	protected abstract String getFormTitle();

	/** Child must implement: dialog header title. */
	@Override
	public abstract String getHeaderTitle();

	/** Common initialization for all CDialog instances. */
	protected final void initializeDialog() {
		CAuxillaries.setId(this);
		LOGGER.debug("CDialog initialized with ID: {}", getId().orElse("none"));
	}

	protected abstract void setupButtons();

	protected abstract void setupContent() throws Exception;

	/* call this class in child constructor after all fields are initialized, use setupContent and setupButtons to customize content */
	protected final void setupDialog() {
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
		if (icon != null) {
			icon.setSize("24px");
			headerLayout.add(icon);
		}
		headerLayout.add(new H3(getFormTitle()));
		mainLayout.add(headerLayout);
		add(mainLayout);
		//
		buttonLayout.setJustifyContentMode(HorizontalLayout.JustifyContentMode.CENTER);
		buttonLayout.getStyle().set("margin-top", "16px");
		getFooter().add(buttonLayout);
		try {
			setupContent();
		} catch (final Exception e) {
			LOGGER.error("Error setting up dialog content: {}", e.getMessage(), e);
			Notification.show("Error setting up dialog: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
		}
		setupButtons();
		setupDone = true;
	}
}
