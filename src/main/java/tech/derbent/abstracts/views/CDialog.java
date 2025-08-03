package tech.derbent.abstracts.views;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import tech.derbent.abstracts.utils.CAuxillaries;

public abstract class CDialog extends Dialog {

    private static final long serialVersionUID = 1L;
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    protected VerticalLayout mainLayout;
    protected final HorizontalLayout buttonLayout = new HorizontalLayout();

    /**
     * Constructor for CDialog. Initializes the dialog with a default layout.
     */
    public CDialog() {
        super();
        initializeDialog();
    }

    /**
     * Common initialization for all CDialog instances.
     */
    protected void initializeDialog() {
        CAuxillaries.setId(this);
        LOGGER.debug("CDialog initialized with ID: {}", getId().orElse("none"));
    }

    /** Child must implement: form title. */
    protected abstract Icon getFormIcon();

    /** Child must implement: form title. */
    protected abstract String getFormTitle();

    /** Child must implement: dialog header title. */
    @Override
    public abstract String getHeaderTitle();

    protected abstract void setupButtons();

    protected abstract void setupContent();

    /** Sets up dialog properties (title, modal, size, etc.) */
    protected void setupDialog() {
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
        setupContent();
        setupButtons();
    }
}
