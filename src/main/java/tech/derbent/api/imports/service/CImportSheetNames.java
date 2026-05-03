package tech.derbent.api.imports.service;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

        // WHY: import handlers are registered early; if the registry is not ready we still want to
        // accept the canonical ENTITY_TITLE_* constants used across the domain model.
        readStaticStringField(entityClass, "ENTITY_TITLE").ifPresent(names::add);
        readStaticStringField(entityClass, "ENTITY_TITLE_SINGULAR").ifPresent(names::add);
        readStaticStringField(entityClass, "ENTITY_TITLE_PLURAL").ifPresent(names::add);

        return names;
    }

    private static java.util.Optional<String> readStaticStringField(final Class<?> clazz, final String fieldName) {
        try {
            final Field field = clazz.getDeclaredField(fieldName);
            if (!Modifier.isStatic(field.getModifiers()) || field.getType() != String.class) {
                return java.util.Optional.empty();
            }
            field.setAccessible(true);
            final Object value = field.get(null);
            if (value instanceof final String str && !str.isBlank()) {
                return java.util.Optional.of(str);
            }
            return java.util.Optional.empty();
        } catch (final Exception ignored) {
            return java.util.Optional.empty();
        }
    }
}
