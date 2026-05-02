package tech.derbent.api.imports.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Top-level result of an Excel import operation. */
public class CImportResult {

    private final boolean dryRun;
    private boolean rolledBack = false;
    private String globalErrorMessage;
    private final List<CImportSheetResult> sheetResults = new ArrayList<>();

    public CImportResult(final boolean dryRun) {
        this.dryRun = dryRun;
    }

    public void addSheetResult(final CImportSheetResult result) {
        sheetResults.add(result);
    }

    public String getGlobalErrorMessage() { return globalErrorMessage; }

    public List<CImportSheetResult> getSheetResults() {
        return Collections.unmodifiableList(sheetResults);
    }

    public long getTotalErrors() {
        return sheetResults.stream().mapToLong(CImportSheetResult::getErrorCount).sum();
    }

    public long getTotalSkipped() {
        return sheetResults.stream().mapToLong(CImportSheetResult::getSkippedCount).sum();
    }

    public long getTotalSuccess() {
        return sheetResults.stream().mapToLong(CImportSheetResult::getSuccessCount).sum();
    }

    public boolean hasGlobalError() { return globalErrorMessage != null; }
    public boolean isDryRun() { return dryRun; }
    public boolean isFullSuccess() { return getTotalErrors() == 0 && !hasGlobalError(); }
    public boolean isRolledBack() { return rolledBack; }
    public void setGlobalErrorMessage(final String msg) { this.globalErrorMessage = msg; }
    public void setRolledBack(final boolean rolledBack) { this.rolledBack = rolledBack; }
}
