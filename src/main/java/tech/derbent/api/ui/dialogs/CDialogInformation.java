package tech.derbent.api.ui.dialogs;

import java.lang.reflect.InvocationTargetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.VaadinIcon;

/** CInformationDialog - Dialog for displaying informational messages to users. Layer: View (MVC) Used to provide helpful information or confirmation
 * of successful actions. */
public final class CDialogInformation extends CDialogInfoBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDialogInformation.class);
	private static final long serialVersionUID = 1L;

	/** @param message The information message to display
	 * @throws Exception
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchMethodException */
	public CDialogInformation(final String message) {
		this("Information", message);
	}

	/** @param title   The dialog title
	 * @param message The information message to display */
	public CDialogInformation(final String title, final String message) {
		super(title, message, VaadinIcon.INFO_CIRCLE.create());
		LOGGER.debug("CInformationDialog created with message: {}", message);
	}
}
