package tech.derbent.risks.service;

import java.time.Clock;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.vaadin.flow.router.Menu;

import jakarta.annotation.security.PermitAll;
import jakarta.transaction.Transactional;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.risks.domain.CRisk;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(order = 0, icon = "vaadin:clipboard-check", title = "Settings.Risks")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskService extends CAbstractNamedEntityService<CRisk> {

    CRiskService(final CRiskRepository repository, final Clock clock) {
        super(repository, clock);
    }

    // Now using the inherited createEntity(String name) method from CAbstractNamedEntityService
    // The original createEntity method is replaced by the parent class implementation
    
    @Override
    protected CRisk createNewEntityInstance() {
        return new CRisk();
    }

    /**
     * Finds risks by project.
     */
    public List<CRisk> findByProject(final CProject project) {
        return ((CRiskRepository) repository).findByProject(project);
    }

    /**
     * Gets paginated risks by project.
     */
    public Page<CRisk> listByProject(final CProject project, final Pageable pageable) {
        return ((CRiskRepository) repository).findByProject(project, pageable);
    }
}
