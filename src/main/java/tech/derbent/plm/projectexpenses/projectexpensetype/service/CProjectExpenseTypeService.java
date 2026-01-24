package tech.derbent.plm.projectexpenses.projectexpensetype.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.projectexpenses.projectexpense.service.IProjectExpenseRepository;
import tech.derbent.plm.projectexpenses.projectexpensetype.domain.CProjectExpenseType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectExpenseTypeService extends CTypeEntityService<CProjectExpenseType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectExpenseTypeService.class);
	@Autowired
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
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for projectexpense type: {}", entity.getName(), e);
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
		final CCompany activeCompany = sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
		final long typeCount = ((IProjectExpenseTypeRepository) repository).countByCompany(activeCompany);
		final String autoName = String.format("ProjectExpenseType %02d", typeCount + 1);
		((CEntityNamed<?>) entity).setName(autoName);
	}

	@Override
	protected void validateEntity(final CProjectExpenseType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CProjectExpenseType> existing =
				((IProjectExpenseTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
