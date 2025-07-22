package tech.derbent.abstracts.views;

import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * CExceptionDialog - Dialog for displaying error messages and exceptions to users. Layer: View (MVC) Used when an error
 * occurs that the user should be informed about.
 */
public final class CExceptionDialog extends CBaseDialog {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     *            The error message to display
     */
    public CExceptionDialog(final String message) {
        super("Error", message);
        LOGGER.debug("CExceptionDialog created with message: {}", message);
    }

    /**
     * @param title
     *            Custom error title
     * @param message
     *            The error message to display
     */
    public CExceptionDialog(final String title, final String message) {
        super(title, message);
        LOGGER.debug("CExceptionDialog created with title: {} and message: {}", title, message);
    }

    /**
     * Convenience constructor for displaying exception information.
     * 
     * @param exception
     *            The exception to display
     */
    public CExceptionDialog(final Exception exception) {
        super("Error", exception.getMessage() != null ? exception.getMessage() : "An unexpected error occurred");
        LOGGER.debug("CExceptionDialog created for exception: {}", exception.getClass().getSimpleName());
    }

    @Override
    protected Icon getDialogIcon() {
        final Icon icon = VaadinIcon.EXCLAMATION_CIRCLE.create();
        icon.setColor("var(--lumo-error-color)");
        return icon;
    }

    @Override
    protected ButtonVariant getOkButtonVariant() {
        return ButtonVariant.LUMO_ERROR;
    }
}