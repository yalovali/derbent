package tech.derbent.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/** Vaadin configuration to prevent Atmosphere JSR356AsyncSupport initialization issues. This configuration sets system properties that prevent
 * Atmosphere from attempting to initialize problematic WebSocket support and eliminates "Websocket protocol not supported" messages. */
@Configuration
public class VaadinConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(VaadinConfig.class);

	@PostConstruct
	public void configureAtmosphere() {
		LOGGER.info("Configuring Atmosphere system properties to prevent WebSocket initialization issues...");
		// Disable Atmosphere's JSR356 WebSocket support to prevent initialization errors
		System.setProperty("org.atmosphere.container.JSR356AsyncSupport.force", "false");
		System.setProperty("org.atmosphere.cpr.broadcaster.maxProcessingThreads", "1");
		System.setProperty("org.atmosphere.cpr.broadcaster.maxAsyncWriteThreads", "1");
		// Force Atmosphere to use the blocking I/O implementation instead of JSR356
		System.setProperty("org.atmosphere.useBlocking", "true");
		System.setProperty("org.atmosphere.cpr.AtmosphereFramework.autoDetectHandlers", "false");
		// Additional properties to prevent WebSocket protocol detection and usage
		System.setProperty("org.atmosphere.disableAtmosphere", "false"); // Keep atmosphere but disable websocket
		System.setProperty("org.atmosphere.cpr.AtmosphereFramework.autoDetectAsync", "false");
		System.setProperty("org.atmosphere.container.autoDetectHandler", "false");
		System.setProperty("org.atmosphere.websocket.enableProtocol", "false");
		// Explicitly disable WebSocket transport to prevent AsynchronousProcessor messages
		System.setProperty("org.atmosphere.transport.websocket.support", "false");
		System.setProperty("org.atmosphere.runtime.webSocketEngine", "false");
		// Force the use of blocking servlet container support only
		System.setProperty("org.atmosphere.container.servlet", "org.atmosphere.container.BlockingIOCometSupport");
		LOGGER.info("✅ Atmosphere configured to use blocking I/O transport only - WebSocket protocol disabled");
		LOGGER.debug("Atmosphere system properties: JSR356={}, blocking={}, autoDetect={}",
				System.getProperty("org.atmosphere.container.JSR356AsyncSupport.force"), System.getProperty("org.atmosphere.useBlocking"),
				System.getProperty("org.atmosphere.cpr.AtmosphereFramework.autoDetectHandlers"));
	}
}
