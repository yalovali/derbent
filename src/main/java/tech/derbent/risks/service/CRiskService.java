package tech.derbent.risks.service;

import java.time.Clock;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vaadin.flow.router.Menu;

import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.risks.domain.CRisk;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CAbstractService<CRisk> {

    CRiskService(final CRiskRepository repository, final Clock clock) {
        super(repository, clock);
    }

    // Additional methods specific to CRisk can be added here
    @Transactional
    public void createEntity(final String name) {
        if ("fail".equals(name)) {
            throw new RuntimeException("This is for testing the error handler");
        }
        final var entity = new CRisk();
        entity.setName(name);
        repository.saveAndFlush(entity);
    }
}
