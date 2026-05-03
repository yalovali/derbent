package tech.derbent.api.imports.service;

/**
 * Root base class for all Excel entity import handlers.
 *
 * WHY: Import handler inheritance should mirror the entity/service/initializer inheritance chain
 * (CEntityImportHandler -> ... -> concrete handler) to keep the codebase predictable.
 */
public abstract class CEntityImportHandler<T> extends CAbstractExcelImportHandler<T> {
    // Intentionally empty (template root).
}
