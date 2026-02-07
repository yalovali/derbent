package tech.derbent.api.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/** Vaadin configuration to prevent Atmosphere JSR356AsyncSupport initialization issues and handle frontend resource extraction. This configuration
 * sets system properties that prevent Atmosphere from attempting to initialize problematic WebSocket support and eliminates "Websocket protocol not
 * supported" messages. It also ensures proper frontend directory structure for JAR resource extraction. */
@Configuration
public class VaadinConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(VaadinConfig.class);

	@PostConstruct
	public static void configureAtmosphere() {
		LOGGER.info("Configuring Atmosphere to use long-polling only (no WebSocket)...");
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
		// CRITICAL: Disable WebSocket protocol detection in AsynchronousProcessor
		System.setProperty("org.atmosphere.cpr.AsynchronousProcessor.websocket", "false");
		System.setProperty("org.atmosphere.websocket.messageContentType", "application/json");
		// Force long-polling as the only transport
		System.setProperty("org.atmosphere.cpr.AtmosphereFramework.transport", "long-polling");
		LOGGER.info("✅ Atmosphere configured: transport=long-polling, WebSocket=disabled");
		// Ensure frontend directories exist to prevent JAR resource extraction errors
		ensureFrontendDirectories();
	}

	/** Ensures that the frontend directory structure exists to prevent JAR resource extraction errors on Windows. This is particularly important for
	 * StoredObject components which may have issues with path handling during resource extraction. Note: This method is designed for development
	 * environments where the source directory exists. In production (JAR deployments), Vaadin handles resources differently and this method will
	 * silently skip directory creation. */
	private static void ensureFrontendDirectories() {
		try {
			// Get the project base directory - only works in development when running from source
			final String userDir = System.getProperty("user.dir");
			final Path frontendPath = Paths.get(userDir, "src", "main", "frontend");
			// Only proceed if we're running from source (development mode)
			final Path srcPath = Paths.get(userDir, "src");
			if (!Files.exists(srcPath)) {
				LOGGER.debug("Not running from source directory, skipping frontend directory creation");
				return;
			}
			final Path generatedPath = frontendPath.resolve("generated");
			final Path jarResourcesPath = generatedPath.resolve("jar-resources");
			// Create directories if they don't exist
			if (!Files.exists(frontendPath)) {
				Files.createDirectories(frontendPath);
				LOGGER.info("Created frontend directory: {}", frontendPath);
			}
			if (!Files.exists(generatedPath)) {
				Files.createDirectories(generatedPath);
				LOGGER.info("Created generated directory: {}", generatedPath);
			}
			if (!Files.exists(jarResourcesPath)) {
				Files.createDirectories(jarResourcesPath);
				LOGGER.info("Created jar-resources directory: {}", jarResourcesPath);
			}
			// LOGGER.debug("Frontend directory structure verified at: {}", frontendPath);
		} catch (final Exception e) {
			LOGGER.warn("Could not create frontend directories (this is not critical): {}", e.getMessage());
		}
	}
}
