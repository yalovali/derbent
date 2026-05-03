package tech.derbent.api.imports.service;

import java.util.LinkedHashSet;
import java.util.Set;
import tech.derbent.api.registry.CEntityRegistry;

/**
 * Canonical sheet name generator.
 *
 * WHY: we want a strict, predictable naming scheme (singular/plural + class name variants) while
 * still being flexible enough for human-authored Excel files.
 */
public final class CImportSheetNames {

    private CImportSheetNames() { /* utility class */ }

    public static Set<String> forEntity(final Class<?> entityClass) {
        final Set<String> names = new LinkedHashSet<>();
        if (entityClass == null) {
            return names;
        }
        final String simple = entityClass.getSimpleName();
        names.add(simple);
        if (simple.startsWith("C") && simple.length() > 1) {
            final String noPrefix = simple.substring(1);
            names.add(noPrefix);
            // WHY: humans name Excel sheets with spaces ("Ticket Priority") while code uses camel-case (TicketPriority).
            final String spaced = noPrefix.replaceAll("(?<=[a-z])(?=[A-Z])", " ");
            if (!spaced.equals(noPrefix)) {
                names.add(spaced);
            }
        }
        // Include registry titles (usually the UI menu labels).
        try {
            final String singular = CEntityRegistry.getEntityTitleSingular(entityClass);
            final String plural = CEntityRegistry.getEntityTitlePlural(entityClass);
            if (singular != null && !singular.isBlank()) {
                names.add(singular);
            }
            if (plural != null && !plural.isBlank()) {
                names.add(plural);
            }
        } catch (final Exception ignored) {
            // WHY: some registries may not be fully ready during early bean initialization.
        }
        return names;
    }
}
