package tech.derbent.api.imports.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Per-row result from an Excel import operation. */
public class CImportRowResult {

    /** 1-based row number in the sheet (includes header row). */
    private final int rowNumber;
    private final CImportRowStatus status;
    /** Name of the entity that was created (null on error/skip). */
    private final String entityName;
    /** Human-readable error message (null on success/skip). */
    private final String errorMessage;
    /** Raw cell values read from the row. */
    private final Map<String, String> rawData;

    public static CImportRowResult error(final int rowNumber, final String errorMessage, final Map<String, String> rawData) {
        return new CImportRowResult(rowNumber, CImportRowStatus.ERROR, null, errorMessage, rawData);
    }

    public static CImportRowResult skipped(final int rowNumber) {
        return new CImportRowResult(rowNumber, CImportRowStatus.SKIPPED, null, null, Collections.emptyMap());
    }

    public static CImportRowResult success(final int rowNumber, final String entityName) {
        return new CImportRowResult(rowNumber, CImportRowStatus.SUCCESS, entityName, null, Collections.emptyMap());
    }

    private CImportRowResult(final int rowNumber, final CImportRowStatus status, final String entityName,
            final String errorMessage, final Map<String, String> rawData) {
        this.rowNumber = rowNumber;
        this.status = status;
        this.entityName = entityName;
        this.errorMessage = errorMessage;
        this.rawData = Collections.unmodifiableMap(new LinkedHashMap<>(rawData));
    }

    public String getEntityName() { return entityName; }
    public String getErrorMessage() { return errorMessage; }
    public Map<String, String> getRawData() { return rawData; }
    public int getRowNumber() { return rowNumber; }
    public CImportRowStatus getStatus() { return status; }
    public boolean isError() { return status == CImportRowStatus.ERROR; }
    public boolean isSkipped() { return status == CImportRowStatus.SKIPPED; }
    public boolean isSuccess() { return status == CImportRowStatus.SUCCESS; }
}
