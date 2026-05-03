package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.workflow.service.CWorkflowEntityService;

/**
 * Base importer for {@link CTypeEntity} implementations that can optionally reference a workflow.
 *
 * WHY: ActivityType/IssueType/MeetingType/DecisionType (and more) had near-identical import code;
 * pushing the shared logic here reduces duplication and makes it easier to expand system_init.xlsx.
 */
public abstract class CAbstractWorkflowTypeImportHandler<T extends CTypeEntity<T>>
        extends CAbstractExcelImportHandler<T> {

    protected final CWorkflowEntityService workflowEntityService;

    protected CAbstractWorkflowTypeImportHandler(final CWorkflowEntityService workflowEntityService) {
        this.workflowEntityService = workflowEntityService;
    }

    protected abstract Optional<T> findByNameAndCompany(String name, CCompany company);

    protected abstract T createNew(String name, CCompany company);

    protected abstract void save(T entity);

    /** Hook for entity-specific fields (e.g. requiresApproval on DecisionType). */
    protected void applyExtraFields(final T entity, final CExcelRow row, final CProject<?> project, final int rowNumber) {
        // default: no extra fields
    }

    @Override
    public Set<String> getRequiredColumns() {
        return Set.of("name");
    }

    @Override
    public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
            final CImportOptions options) {
        final CExcelRow row = row(rowData);
        final String name = row.string("name");
        if (name.isBlank()) {
            return CImportRowResult.error(rowNumber, "Name is required", rowData);
        }
        if (project.getCompany() == null) {
            return CImportRowResult.error(rowNumber, "Project company is required to create types", rowData);
        }
        final CCompany company = project.getCompany();

        // WHY: system init Excel is intended to be re-runnable (and also safe on top of code-initialized reference data).
        // Upsert-by-name avoids unique constraint violations.
        final T type = findByNameAndCompany(name, company).orElseGet(() -> createNew(name, company));

        row.optionalString("color").ifPresent(type::setColor);
        row.optionalInt("sortorder").ifPresent(type::setSortOrder);
        row.optionalInt("level").ifPresent(type::setLevel);
        row.optionalBoolean("canhavechildren").ifPresent(type::setCanHaveChildren);
        row.optionalBoolean("attributenondeletable").ifPresent(type::setAttributeNonDeletable);

        final String workflowName = row.string("workflow");
        if (!workflowName.isBlank()) {
            final var wf = workflowEntityService.findByNameAndCompany(workflowName, company).orElse(null);
            if (wf == null) {
                return CImportRowResult.error(rowNumber, "Workflow '" + workflowName + "' not found", rowData);
            }
            type.setWorkflow(wf);
        }

        applyExtraFields(type, row, project, rowNumber);

        if (!options.isDryRun()) {
            save(type);
        }
        return CImportRowResult.success(rowNumber, name);
    }
}
