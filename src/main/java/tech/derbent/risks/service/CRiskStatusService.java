package tech.derbent.risks.service;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.risks.domain.CRiskStatus;

/**
 * CRiskStatusService - Service class for managing CRiskStatus entities. Layer: Service (MVC) Provides business logic
 * for risk status management including CRUD operations, validation, and workflow management.
 */
@Service
@Transactional
public class CRiskStatusService extends CEntityOfProjectService<CRiskStatus> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CRiskStatusService.class);

    @Autowired
    public CRiskStatusService(final CRiskStatusRepository riskStatusRepository, final Clock clock) {
        super(riskStatusRepository, clock);

        if (riskStatusRepository == null) {
            LOGGER.error("CRiskStatusService constructor - Repository parameter is null");
            throw new IllegalArgumentException("Risk status repository cannot be null");
        }
    }

    /**
     * Create default risk statuses if they don't exist. This method should be called during application startup.
     */
    public void createDefaultStatusesIfNotExist() {
        LOGGER.debug("createDefaultStatusesIfNotExist() - Creating default risk statuses");
        // TODO implement default statuses creation logic
    }

    @Override
    protected Class<CRiskStatus> getEntityClass() {
        return CRiskStatus.class;
    }
}