package tech.derbent.api.imports.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Collects all IEntityImportHandler beans and maps sheet names to handlers.
 * Spring automatically injects every bean implementing the interface.
 */
@Service
public class CImportHandlerRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(CImportHandlerRegistry.class);

    private final List<IEntityImportHandler<?>> handlers;

    public CImportHandlerRegistry(final List<IEntityImportHandler<?>> handlers) {
        this.handlers = handlers;
        LOGGER.info("Import handler registry initialized with {} handlers", handlers.size());
        handlers.forEach(h -> LOGGER.debug("  handler: {} → sheets: {}", h.getEntityClass().getSimpleName(), h.getSupportedSheetNames()));
    }

    /**
     * Finds the handler whose supported sheet names contain sheetName (case-insensitive).
     * Returns empty if no handler matches.
     */
    public Optional<IEntityImportHandler<?>> findHandler(final String sheetName) {
        if (sheetName == null || sheetName.isBlank()) {
            return Optional.empty();
        }
        final String normalized = sheetName.trim().toLowerCase();
        for (final IEntityImportHandler<?> handler : handlers) {
            for (final String name : handler.getSupportedSheetNames()) {
                if (name.toLowerCase().equals(normalized)) {
                    return Optional.of(handler);
                }
            }
        }
        return Optional.empty();
    }

    public List<IEntityImportHandler<?>> getAllHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
