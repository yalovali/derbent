package tech.derbent.plm.projectexpenses.projectexpensetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.projectexpenses.projectexpense.service.IProjectExpenseRepository;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

@Profile({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectExpenseTypeService extends CTypeEntityService<CProjectExpenseType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseTypeService.class);
	private final IProjectExpenseRepository projectexpenseRepository;

	public CProjectExpenseTypeService(final IProjectExpenseTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectExpenseRepository projectexpenseRepository) {
		super(repository, clock, sessionService);
		this.projectexpenseRepository = projectexpenseRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectExpenseType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = projectexpenseRepository.countByType(entity);
			if (usageCount > 0) {
				String string = "Cannot delete. It is being used by %d item%s.";
				return string.formatted(usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for projectexpense type: {} reason={}", entity.getName(), e.getMessage());
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectExpenseType> getEntityClass() { return CProjectExpenseType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectExpenseTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectExpenseType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (!(entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null)) {
			return;
		}
		final CCompany activeCompany =
				sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IProjectExpenseTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = "ProjectExpenseType %02d".formatted(typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CProjectExpenseType entity) {
		super.validateEntity(entity);
		
		// Unique Name Check - USE STATIC HELPER
		validateUniqueNameInCompany((IProjectExpenseTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
