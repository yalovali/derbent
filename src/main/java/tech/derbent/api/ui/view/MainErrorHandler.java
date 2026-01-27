package tech.derbent.api.ui.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.vaadin.flow.server.VaadinServiceInitListener;
import tech.derbent.api.exceptions.CGlobalExceptionHandler;

@Configuration
class MainErrorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(MainErrorHandler.class);

	@Bean
	VaadinServiceInitListener errorHandlerInitializer() {
		return (event) -> event.getSource().addSessionInitListener(sessionInitEvent -> {
			// Use our enhanced global exception handler
			final CGlobalExceptionHandler globalHandler = new CGlobalExceptionHandler();
			sessionInitEvent.getSession().setErrorHandler(globalHandler);
			LOGGER.info("🔥 Enhanced Global Exception Handler registered - breakpoints are active!");
		});
	}
}
