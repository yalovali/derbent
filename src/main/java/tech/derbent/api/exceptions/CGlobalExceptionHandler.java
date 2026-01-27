package tech.derbent.api.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.vaadin.flow.server.ErrorEvent;
import com.vaadin.flow.server.ErrorHandler;
import com.vaadin.flow.server.VaadinSession;

/** Global exception handler for Vaadin applications. SET BREAKPOINT HERE on error() method to catch any exception. This handler is registered with
 * Vaadin and catches UI-related exceptions. */
@Component
public class CGlobalExceptionHandler implements ErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGlobalExceptionHandler.class);
	private static final long serialVersionUID = 1L;

	/** Register this handler with Vaadin when the session is created */
	public static void register() {
		final VaadinSession session = VaadinSession.getCurrent();
		if (session != null) {
			session.setErrorHandler(new CGlobalExceptionHandler());
			LOGGER.info("🔥 Global exception handler registered with Vaadin session");
		}
	}

	/** 🔴 SET BREAKPOINT HERE 🔴 - This catches ALL Vaadin UI exceptions This method is called by Vaadin when any exception occurs in the UI */
	@Override
	public void error(ErrorEvent event) {
		// 🔴 SET BREAKPOINT ON THIS LINE 🔴
		LOGGER.error("🔥 VAADIN EXCEPTION CAUGHT: {} - {}", event.getThrowable().getClass().getSimpleName(), event.getThrowable().getMessage(),
				event.getThrowable());
		// Also print to console for easier debugging
		System.err.println("🔥🔥🔥 EXCEPTION BREAKPOINT HIT 🔥🔥🔥");
		System.err.println("Exception: " + event.getThrowable().getClass().getSimpleName());
		System.err.println("Message: " + event.getThrowable().getMessage());
		event.getThrowable().printStackTrace();
		System.err.println("🔥🔥🔥 END EXCEPTION DETAILS 🔥🔥🔥");
	}
}
