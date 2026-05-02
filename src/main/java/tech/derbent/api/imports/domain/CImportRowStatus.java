package tech.derbent.api.imports.domain;

/** Import row processing outcome. */
public enum CImportRowStatus {
    /** Row imported successfully. */
    SUCCESS,
    /** Row had a validation or relation error and was not imported. */
    ERROR,
    /** Row was skipped (comment row or blank row). */
    SKIPPED
}
