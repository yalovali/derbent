package tech.derbent.plm.risklevel.risklevel.service;

import java.time.Clock;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.risklevel.risklevel.domain.CRiskLevel;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:level-up", title = "Settings.Risk Levels")
@PermitAll // When security is enabled, allow all authenticated users
public class CRiskLevelService extends CProjectItemService<CRiskLevel> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRiskLevelService.class);

	CRiskLevelService(final IRiskLevelRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
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
	public void initializeNewEntity(final CRiskLevel entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new risk level entity");
		entity.setRiskLevel(1); // Default: level 1
		LOGGER.debug("Risk level initialization complete with default level: 1");
	}

	@Override
	protected void validateEntity(final CRiskLevel entity) throws CValidationException {
		super.validateEntity(entity);
		if (entity.getRiskLevel() != null && (entity.getRiskLevel() < 1 || entity.getRiskLevel() > 10)) {
			throw new CValidationException("Risk level must be between 1 and 10.");
		}
		final Optional<CRiskLevel> existing = ((IRiskLevelRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existing.isPresent() && !existing.get().getId().equals(entity.getId())) {
			throw new CValidationException(String.format(ValidationMessages.DUPLICATE_NAME, entity.getName()));
		}
	}
}
