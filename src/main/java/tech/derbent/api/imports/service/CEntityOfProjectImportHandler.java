package tech.derbent.api.imports.service;

import tech.derbent.api.entityOfProject.domain.CEntityOfProject;

/**
 * Base importer for project-scoped entities.
 */
public abstract class CEntityOfProjectImportHandler<T extends CEntityOfProject<T>> extends CEntityNamedImportHandler<T> {
    // Intentionally empty.
}
