package tech.derbent.app.decisions.service;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.companies.domain.CCompany;
import tech.derbent.app.decisions.domain.CDecisionType;
import tech.derbent.base.session.service.ISessionService;

/** CDecisionTypeService - Service class for CDecisionType entities. Layer: Service (MVC) Provides business logic operations for company-aware
 * decision type management including validation, creation, and status management. */
@Service
@PreAuthorize ("isAuthenticated()")
public class CDecisionTypeService extends CTypeEntityService<CDecisionType> implements IEntityRegistrable, IEntityWithView {

	public CDecisionTypeService(final IDecisionTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Checks dependencies before allowing decision type deletion. Always calls super.checkDeleteAllowed() first to ensure all parent-level checks
	 * (null validation, non-deletable flag) are performed.
	 * @param entity the decision type entity to check
	 * @return null if type can be deleted, error message otherwise */
	@Override
	public String checkDeleteAllowed(final CDecisionType entity) {
		// Call super class first to check common dependencies
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		// No specific dependencies to check yet - stub for future implementation
		return null;
	}

	/** Finds all active decision types for a company.
	 * @param company the company
	 * @return list of active decision types for the company */
	@Transactional (readOnly = true)
	public List<CDecisionType> findAllActiveByCompany(final CCompany company) {
		Optional.ofNullable(company).orElse(null);
		return ((IDecisionTypeRepository) repository).findByCompanyAndActiveTrue(company);
	}

	@Override
	public Class<CDecisionType> getEntityClass() { return CDecisionType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CDecisionTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceDecisionType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	/** Initializes a new decision type with default values based on current session and available data.
	 * @param entity the newly created decision type to initialize
	 * @throws IllegalStateException if required fields cannot be initialized */
	@Override
	public void initializeNewEntity(final CDecisionType entity) {
		super.initializeNewEntity(entity);
		setNameOfEntity(entity, "Decision Type");
	}
}
