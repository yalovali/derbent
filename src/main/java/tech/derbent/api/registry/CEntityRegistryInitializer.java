package tech.derbent.api.registry;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import tech.derbent.api.config.CSpringContext;

/** Initializes the entity registry at application startup. This class registers all entities, services, and their metadata. Order is set to run early
 * in the startup process. */
@Component
@Order (1)
public class CEntityRegistryInitializer implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(CEntityRegistryInitializer.class);

	public static void registerAll() {
		// Clear previous state (useful for tests / re-deploys)
		CEntityRegistry.clear();
		final Map<String, IEntityRegistrable> beans = CSpringContext.getBeansOfType(IEntityRegistrable.class);
		LOGGER.info("Found {} IEntityRegistrable beans to register", beans.size());
		beans.values().forEach(registrable -> {
			try {
				// LOGGER.debug("Registering IEntityRegistrable bean: {}", registrable.getClass().getName());
				CEntityRegistry.register(registrable);
			} catch (final Exception e) {
				LOGGER.error("Failed to register entity from bean {}: {}", registrable.getClass().getName(), e.getMessage(), e);
			}
		});
	}

	@Override
	public void run(final String... args) throws Exception {
		LOGGER.info("Initializing entity registry...");
		try {
			registerAll();
			// print all registered entities for debug
			// CEntityRegistry.print();
			CEntityRegistry.markInitialized();
			// LOGGER.info("Entity registry initialized successfully with {} entities", CEntityRegistry.getRegisteredCount());
		} catch (final Exception e) {
			LOGGER.error("Failed to initialize entity registry", e);
			throw e;
		}
	}
}
