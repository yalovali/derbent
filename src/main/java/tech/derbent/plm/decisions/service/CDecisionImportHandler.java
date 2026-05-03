package tech.derbent.plm.decisions.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.decisions.domain.CDecision;
import tech.derbent.plm.decisions.domain.CDecisionType;

/**
 * Imports CDecision rows from Excel (project items).
 *
 * Extends CAbstractExcelImportHandler so @AMetaData displayNames are automatically registered as
 * column aliases. LocalDateTime fields (implementationdate, reviewdate) accept both "yyyy-MM-dd"
 * and ISO datetime — date-only input is treated as midnight.
 */
@Service
@Profile({"derbent", "default"})
public class CDecisionImportHandler extends CProjectItemImportHandler<CDecision, CDecisionType> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CDecisionImportHandler.class);

    private final CDecisionService decisionService;
    private final CDecisionTypeService decisionTypeService;

    public CDecisionImportHandler(final CDecisionService decisionService, final CDecisionTypeService decisionTypeService,
            final CProjectItemStatusService statusService, final IUserRepository userRepository) {
        super(statusService, userRepository);
        this.decisionService = decisionService;
        this.decisionTypeService = decisionTypeService;
    }

    @Override
    public Class<CDecision> getEntityClass() { return CDecision.class; }

    @Override
    protected Map<String, String> getAdditionalColumnAliases() {
        // WHY: "Decision Type" and "Type" are common header synonyms for entitytype token.
        return Map.of(
                "Decision Type", "entitytype",
                "Type", "entitytype");
    }

    @Override
    protected Class<CDecisionType> getTypeClass() { return CDecisionType.class; }

    @Override
    protected Optional<CDecision> findByNameAndProject(final String name, final CProject<?> project) {
        return decisionService.findByNameAndProject(name, project);
    }

    @Override
    protected CDecision createNew(final String name, final CProject<?> project) {
        return new CDecision(name, project);
    }

    @Override
    protected void save(final CDecision entity) {
        decisionService.save(entity);
    }

    @Override
    protected Optional<CDecisionType> findTypeByNameAndCompany(final String name, final CCompany company) {
        return decisionTypeService.findByNameAndCompany(name, company);
    }

    @Override
    protected void applyExtraFields(final CDecision entity, final CExcelRow row, final CProject<?> project, final int rowNumber,
            final Map<String, String> rowData) {
        final String costRaw = row.string("estimatedcost");
        if (!costRaw.isBlank()) {
            final BigDecimal cost = row.optionalBigDecimal("estimatedcost").orElse(null);
            if (cost == null) {
                throw new IllegalArgumentException("Invalid Estimated Cost: " + costRaw);
            }
            entity.setEstimatedCost(cost);
        }

        applyDateTime(entity, row, "implementationdate", true);
        applyDateTime(entity, row, "reviewdate", false);
    }

    private static void applyDateTime(final CDecision decision, final CExcelRow row, final String token, final boolean impl) {
        final String raw = row.string(token);
        if (raw.isBlank()) {
            return;
        }
        LocalDateTime dt = row.optionalLocalDateTime(token).orElse(null);
        if (dt == null) {
            dt = row.optionalLocalDate(token).map(d -> d.atStartOfDay()).orElse(null);
        }
        if (dt == null) {
            throw new IllegalArgumentException("Cannot parse " + token + " '" + raw + "' (use yyyy-MM-dd or yyyy-MM-ddTHH:mm)");
        }
        if (impl) {
            decision.setImplementationDate(dt);
        } else {
            decision.setReviewDate(dt);
        }
    }
}
