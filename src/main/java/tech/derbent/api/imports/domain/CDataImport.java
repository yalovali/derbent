package tech.derbent.api.imports.domain;

import java.time.LocalDateTime;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;

@Entity
@Table (name = "cdataimport")
@AttributeOverride (name = "id", column = @Column (name = "dataimport_id"))
public class CDataImport extends CEntityOfCompany<CDataImport> {

    public static final String DEFAULT_COLOR = "#1565C0";
    public static final String DEFAULT_ICON = "vaadin:upload-alt";
    public static final String ENTITY_TITLE_PLURAL = "Import Jobs";
    public static final String ENTITY_TITLE_SINGULAR = "Import Job";
    public static final String VIEW_NAME = "Import Job Management";

    @Column (name = "file_name", length = 512)
    @AMetaData (
            displayName = "File Name", required = false, readOnly = true,
            description = "Name of the imported file", hidden = false, maxLength = 512
    )
    private String fileName;

    @Column (name = "imported_at")
    @AMetaData (
            displayName = "Import Date", required = false, readOnly = true,
            description = "When the import was performed", hidden = false
    )
    private LocalDateTime importedAt;

    @Column (name = "imported_by", length = 255)
    @AMetaData (
            displayName = "Imported By", required = false, readOnly = true,
            description = "Username who performed the import", hidden = false, maxLength = 255
    )
    private String importedBy;

    @Column (name = "dry_run")
    @AMetaData (
            displayName = "Dry Run", required = false, readOnly = true,
            description = "Whether this was a dry run (no data saved)", hidden = false
    )
    private boolean dryRun;

    @Column (name = "rolled_back")
    @AMetaData (
            displayName = "Rolled Back", required = false, readOnly = true,
            description = "Whether the import was rolled back", hidden = false
    )
    private boolean rolledBack;

    @Column (name = "total_success")
    @AMetaData (
            displayName = "Imported Rows", required = false, readOnly = true,
            description = "Number of rows successfully imported", hidden = false
    )
    private int totalSuccess;

    @Column (name = "total_errors")
    @AMetaData (
            displayName = "Error Rows", required = false, readOnly = true,
            description = "Number of rows that failed to import", hidden = false
    )
    private int totalErrors;

    @Column (name = "total_skipped")
    @AMetaData (
            displayName = "Skipped Rows", required = false, readOnly = true,
            description = "Number of rows that were skipped", hidden = false
    )
    private int totalSkipped;

    /** Default constructor for JPA. */
    protected CDataImport() {}

    public CDataImport(final String name, final CCompany company) {
        super(CDataImport.class, name, company);
        initializeDefaults();
    }

    private void initializeDefaults() {
        this.importedAt = LocalDateTime.now();
        CSpringContext.getServiceClassForEntity(this).initializeNewEntity(this);
    }

    public String getFileName() { return fileName; }
    public LocalDateTime getImportedAt() { return importedAt; }
    public String getImportedBy() { return importedBy; }
    public boolean isDryRun() { return dryRun; }
    public boolean isRolledBack() { return rolledBack; }
    public int getTotalSuccess() { return totalSuccess; }
    public int getTotalErrors() { return totalErrors; }
    public int getTotalSkipped() { return totalSkipped; }

    public void setFileName(final String fileName) { this.fileName = fileName; }
    public void setImportedAt(final LocalDateTime importedAt) { this.importedAt = importedAt; }
    public void setImportedBy(final String importedBy) { this.importedBy = importedBy; }
    public void setDryRun(final boolean dryRun) { this.dryRun = dryRun; }
    public void setRolledBack(final boolean rolledBack) { this.rolledBack = rolledBack; }
    public void setTotalSuccess(final int totalSuccess) { this.totalSuccess = totalSuccess; }
    public void setTotalErrors(final int totalErrors) { this.totalErrors = totalErrors; }
    public void setTotalSkipped(final int totalSkipped) { this.totalSkipped = totalSkipped; }
}
