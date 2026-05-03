package tech.derbent.api.imports.domain;

/** Configuration options for an Excel import session. */
public class CImportOptions {

    /** If true, process all rows and report results but do not commit any changes. */
    private boolean dryRun = false;
    /** If true, roll back the entire import when any row fails. */
    private boolean rollbackOnError = false;
    /** If true, sheets whose names do not match any registered handler are silently ignored. */
    private boolean skipUnknownSheets = true;
    /** If true, rows with a non-matching "project" token are skipped (useful when importing a multi-project workbook per-project). */
    private boolean skipMismatchedProjectTokens = true;

    public static CImportOptions defaults() {
        return new CImportOptions();
    }

    public boolean isDryRun() { return dryRun; }
    public boolean isRollbackOnError() { return rollbackOnError; }
    public boolean isSkipUnknownSheets() { return skipUnknownSheets; }
    public boolean isSkipMismatchedProjectTokens() { return skipMismatchedProjectTokens; }
    public void setDryRun(final boolean dryRun) { this.dryRun = dryRun; }
    public void setRollbackOnError(final boolean rollbackOnError) { this.rollbackOnError = rollbackOnError; }
    public void setSkipUnknownSheets(final boolean skipUnknownSheets) { this.skipUnknownSheets = skipUnknownSheets; }
    public void setSkipMismatchedProjectTokens(final boolean skipMismatchedProjectTokens) {
        this.skipMismatchedProjectTokens = skipMismatchedProjectTokens;
    }
}
