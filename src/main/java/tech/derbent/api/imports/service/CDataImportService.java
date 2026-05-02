package tech.derbent.api.imports.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.imports.domain.CDataImport;
import tech.derbent.api.imports.domain.CImportResult;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;

@Profile ({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CDataImportService extends CEntityOfCompanyService<CDataImport>
        implements IEntityRegistrable, IEntityWithView {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDataImportService.class);

    public CDataImportService(final IDataImportRepository repository, final Clock clock,
            final ISessionService sessionService) {
        super(repository, clock, sessionService);
    }

    /** Returns the most recent import jobs for a company (up to 50). */
    @Transactional (readOnly = true)
    public List<CDataImport> listRecentByCompany(final CCompany company) {
        Check.notNull(company, "Company cannot be null when listing import jobs");
        final List<CDataImport> all = ((IDataImportRepository) repository).findByCompanyOrderByImportedAtDesc(company);
        return all.size() > 50 ? all.subList(0, 50) : all;
    }

    /** Persists an import result as a CDataImport history record.
     * @param result      the completed import result
     * @param company     company context
     * @param fileName    original uploaded file name
     * @param username    name of the user who ran the import
     * @return the saved CDataImport entity */
    @Transactional
    public CDataImport saveImportResult(final CImportResult result, final CCompany company,
            final String fileName, final String username) {
        Check.notNull(result, "Import result cannot be null");
        Check.notNull(company, "Company cannot be null when saving import result");
        final String jobName = buildJobName(fileName);
        final CDataImport job = new CDataImport(jobName, company);
        job.setFileName(fileName != null ? fileName : "unknown");
        job.setImportedAt(LocalDateTime.now());
        job.setImportedBy(username != null ? username : "unknown");
        job.setDryRun(result.isDryRun());
        job.setRolledBack(result.isRolledBack());
        job.setTotalSuccess((int) result.getTotalSuccess());
        job.setTotalErrors((int) result.getTotalErrors());
        job.setTotalSkipped((int) result.getTotalSkipped());
        try {
            return save(job);
        } catch (final Exception e) {
            LOGGER.error("Failed to save import job record for file '{}' reason={}", fileName, e.getMessage());
            throw e;
        }
    }

    private String buildJobName(final String fileName) {
        final String base = fileName != null ? fileName.replaceAll("[^a-zA-Z0-9._-]", "_") : "import";
        final String timestamp = LocalDateTime.now().toString().substring(0, 16).replace("T", " ");
        return base + " (" + timestamp + ")";
    }

    @Override
    public Class<CDataImport> getEntityClass() { return CDataImport.class; }

    @Override
    public Class<?> getInitializerServiceClass() { return CDataImportInitializerService.class; }

    @Override
    public Class<?> getPageServiceClass() { return CPageServiceDataImport.class; }

    @Override
    public Class<?> getServiceClass() { return this.getClass(); }

    @Override
    public void initializeNewEntity(final Object entity) {
        super.initializeNewEntity(entity);
    }

    @Override
    protected void validateEntity(final CDataImport entity) {
        super.validateEntity(entity);
        Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
        Check.notNull(entity.getCompany(), ValidationMessages.COMPANY_REQUIRED);
        validateUniqueNameInCompany((IDataImportRepository) repository, entity, entity.getName(), entity.getCompany());
    }
}
