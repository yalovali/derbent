package tech.derbent.api.imports.service;

import java.util.Map;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CAgileEntity;

/**
 * Base importer for {@link CAgileEntity} implementations.
 *
 * <p>WHY: Epic/Feature/User Story share the same agile payload (schedule + estimation + AC/notes/results + sprint fields)
 * via {@link CAgileEntity}. Centralising the mapping avoids drift between handlers and ensures imports stay consistent
 * as the agile model evolves.</p>
 */
public abstract class CAgileEntityImportHandler<T extends CAgileEntity<T, TType>, TType extends CTypeEntity<TType>>
        extends CProjectItemImportHandler<T, TType> {

    protected CAgileEntityImportHandler(final CProjectItemStatusService statusService,
            final IUserRepository userRepository) {
        super(statusService, userRepository);
    }

    @Override
    protected void applyExtraFields(final T entity, final CExcelRow row, final CProject<?> project, final int rowNumber,
            final Map<String, String> rowData) {
        row.optionalString("acceptancecriteria").ifPresent(entity::setAcceptanceCriteria);
        row.optionalString("notes").ifPresent(entity::setNotes);
        row.optionalString("results").ifPresent(entity::setResults);

        row.optionalLong("storypoint").ifPresent(entity::setStoryPoint);
        row.optionalLocalDate("startdate").ifPresent(entity::setStartDate);
        row.optionalLocalDate("duedate").ifPresent(entity::setDueDate);
        row.optionalLocalDate("completiondate").ifPresent(entity::setCompletionDate);
        row.optionalInt("progresspercentage").ifPresent(entity::setProgressPercentage);

        row.optionalBigDecimal("estimatedhours").ifPresent(entity::setEstimatedHours);
        row.optionalBigDecimal("estimatedcost").ifPresent(entity::setEstimatedCost);
        row.optionalBigDecimal("actualhours").ifPresent(entity::setActualHours);
        row.optionalBigDecimal("actualcost").ifPresent(entity::setActualCost);
    }
}
