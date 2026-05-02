package tech.derbent.api.imports.domain;

/** Configuration options for an Excel import session. */
public class CImportOptions {

    /** If true, process all rows and report results but do not commit any changes. */
    private boolean dryRun = false;
    /** If true, roll back the entire import when any row fails. */
    private boolean rollbackOnError = false;
    /** If true, sheets whose names do not match any registered handler are silently ignored. */
    private boolean skipUnknownSheets = true;

    /**
     * If true, handlers may auto-create missing lookup entities (e.g. Status/Type) instead of failing the row.
     * WHY: large bootstrapping workbooks are easier to maintain when they can "declare what they need" and let the
     * importer fill in prerequisite reference data in a controlled, opt-in way.
     */
    private boolean autoCreateLookups = false;

    public static CImportOptions defaults() {
        return new CImportOptions();
    }

    public boolean isAutoCreateLookups() { return autoCreateLookups; }
    public boolean isDryRun() { return dryRun; }
    public boolean isRollbackOnError() { return rollbackOnError; }
    public boolean isSkipUnknownSheets() { return skipUnknownSheets; }
    public void setAutoCreateLookups(final boolean autoCreateLookups) { this.autoCreateLookups = autoCreateLookups; }
    public void setDryRun(final boolean dryRun) { this.dryRun = dryRun; }
    public void setRollbackOnError(final boolean rollbackOnError) { this.rollbackOnError = rollbackOnError; }
    public void setSkipUnknownSheets(final boolean skipUnknownSheets) { this.skipUnknownSheets = skipUnknownSheets; }
}
