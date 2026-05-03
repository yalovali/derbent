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
        applyMetaFieldsDeclaredOn(entity, row, CAgileEntity.class);
    }
}
