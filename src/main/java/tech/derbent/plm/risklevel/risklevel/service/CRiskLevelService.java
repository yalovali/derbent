package tech.derbent.plm.risklevel.risklevel.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.risklevel.risklevel.domain.CRiskLevel;

@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskLevelService extends CProjectItemService<CRiskLevel> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskLevelService.class);

	CRiskLevelService(final IRiskLevelRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
	}

	@Override
	public String checkDeleteAllowed(final CRiskLevel riskLevel) {
		return super.checkDeleteAllowed(riskLevel);
	}

	@Override
	public Class<CRiskLevel> getEntityClass() { return CRiskLevel.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CRiskLevelInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceRiskLevel.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
	}

	@Override
	protected void validateEntity(final CRiskLevel entity) throws CValidationException {
		super.validateEntity(entity);
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		// 3. Risk Level Range Check
		if (entity.getRiskLevel() != null && (entity.getRiskLevel() < 1 || entity.getRiskLevel() > 10)) {
			throw new CValidationException("Risk level must be between 1 and 10.");
		}
		// 4. Unique Name Check
		final Optional<CRiskLevel> existing = ((IRiskLevelRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(ValidationMessages.DUPLICATE_NAME.formatted(entity.getName()));
		}
	}
}
