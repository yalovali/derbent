package tech.derbent.orders.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.orders.domain.COrderApproval;

/**
 * COrderApprovalService - Service layer for COrderApproval entity. Layer: Service (MVC) Handles business logic for
 * order approval operations including creation, validation, and management of order approval entities.
 */
@Service
@PreAuthorize("isAuthenticated()")
@Transactional(readOnly = true)
public class COrderApprovalService extends CAbstractNamedEntityService<COrderApproval> {

    COrderApprovalService(final COrderApprovalRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<COrderApproval> getEntityClass() {
        return COrderApproval.class;
    }
}