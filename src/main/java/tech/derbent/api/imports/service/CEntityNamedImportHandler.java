package tech.derbent.api.imports.service;

import tech.derbent.api.entity.domain.CEntityNamed;

/**
 * Base importer for {@link CEntityNamed} entities.
 *
 * WHY: Mirrors the domain inheritance chain and provides an explicit extension point.
 */
public abstract class CEntityNamedImportHandler<T extends CEntityNamed<T>> extends CEntityImportHandler<T> {
    // Intentionally empty.
}
