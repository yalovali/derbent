package tech.derbent.orders.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.orders.domain.CApprovalStatus;

/**
 * CApprovalStatusService - Service layer for CApprovalStatus entity. Layer: Service (MVC) Handles business logic for
 * approval status operations including creation, validation, and management of approval status entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class CApprovalStatusService extends CEntityOfProjectService<CApprovalStatus> {

    CApprovalStatusService(final CApprovalStatusRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<CApprovalStatus> getEntityClass() {
        return CApprovalStatus.class;
    }
}