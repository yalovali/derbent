package tech.derbent.plm.projectincomes.projectincometype.service;

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
import tech.derbent.plm.projectincomes.projectincome.service.IProjectIncomeRepository;
import tech.derbent.plm.projectincomes.projectincometype.domain.CProjectIncomeType;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProjectIncomeTypeService extends CTypeEntityService<CProjectIncomeType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectIncomeTypeService.class);
	@Autowired
	private final IProjectIncomeRepository projectincomeRepository;

	public CProjectIncomeTypeService(final IProjectIncomeTypeRepository repository, final Clock clock, final ISessionService sessionService,
			final IProjectIncomeRepository projectincomeRepository) {
		super(repository, clock, sessionService);
		this.projectincomeRepository = projectincomeRepository;
	}

	@Override
	public String checkDeleteAllowed(final CProjectIncomeType entity) {
		final String superCheck = super.checkDeleteAllowed(entity);
		if (superCheck != null) {
			return superCheck;
		}
		try {
			final long usageCount = projectincomeRepository.countByType(entity);
			if (usageCount > 0) {
				return String.format("Cannot delete. It is being used by %d item%s.", usageCount, usageCount == 1 ? "" : "s");
			}
			return null;
		} catch (final Exception e) {
			LOGGER.error("Error checking dependencies for projectincome type: {}", entity.getName(), e);
			return "Error checking dependencies: " + e.getMessage();
		}
	}

	@Override
	public Class<CProjectIncomeType> getEntityClass() { return CProjectIncomeType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectIncomeTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectIncomeType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		if (entity instanceof final CEntityNamed entityCasted && entityCasted.getName() == null) {
			final CCompany activeCompany =
					sessionService.getActiveCompany().orElseThrow(() -> new IllegalStateException("No active company in session"));
			final long typeCount = ((IProjectIncomeTypeRepository) repository).countByCompany(activeCompany);
			final String autoName = String.format("ProjectIncomeType %02d", typeCount + 1);
			((CEntityNamed<?>) entity).setName(autoName);
		}
	}

	@Override
	protected void validateEntity(final CProjectIncomeType entity) {
		super.validateEntity(entity);
		// Unique Name Check
		final Optional<CProjectIncomeType> existing =
				((IProjectIncomeTypeRepository) repository).findByNameAndCompany(entity.getName(), entity.getCompany());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_COMPANY);
		}
	}
}
