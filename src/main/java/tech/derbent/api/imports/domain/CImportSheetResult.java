package tech.derbent.api.imports.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Aggregated result for a single Excel sheet import. */
public class CImportSheetResult {

    private final String sheetName;
    /** Human-readable entity type name matched for this sheet (null if unrecognized). */
    private final String entityTypeName;
    private final boolean entityTypeRecognized;
    /** Set when the sheet header row could not be parsed. */
    private String headerErrorMessage;
    private final List<CImportRowResult> rowResults = new ArrayList<>();

    public CImportSheetResult(final String sheetName, final String entityTypeName, final boolean entityTypeRecognized) {
        this.sheetName = sheetName;
        this.entityTypeName = entityTypeName;
        this.entityTypeRecognized = entityTypeRecognized;
    }

    public void addRowResult(final CImportRowResult result) {
        rowResults.add(result);
    }

    public long getErrorCount() {
        return rowResults.stream().filter(CImportRowResult::isError).count();
    }

    public String getEntityTypeName() { return entityTypeName; }
    public String getHeaderErrorMessage() { return headerErrorMessage; }
    public List<CImportRowResult> getRowResults() { return Collections.unmodifiableList(rowResults); }
    public String getSheetName() { return sheetName; }

    public long getSkippedCount() {
        return rowResults.stream().filter(CImportRowResult::isSkipped).count();
    }

    public long getSuccessCount() {
        return rowResults.stream().filter(CImportRowResult::isSuccess).count();
    }

    public int getTotalDataRows() { return rowResults.size(); }
    public boolean hasErrors() { return getErrorCount() > 0 || headerErrorMessage != null; }
    public boolean isEntityTypeRecognized() { return entityTypeRecognized; }
    public void setHeaderErrorMessage(final String msg) { this.headerErrorMessage = msg; }
}
