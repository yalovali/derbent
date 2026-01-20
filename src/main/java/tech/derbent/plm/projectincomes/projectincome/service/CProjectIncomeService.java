package tech.derbent.plm.projectincomes.projectincome.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.plm.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeService;

import java.math.BigDecimal;
import java.util.Optional;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.validation.ValidationMessages;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (icon = "vaadin:file-o", title = "Settings.ProjectIncomes")
@PermitAll
public class CProjectIncomeService extends CProjectItemService<CProjectIncome> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectIncomeService.class);
	private final CProjectIncomeTypeService projectincomeTypeService;

	CProjectIncomeService(final IProjectIncomeRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectIncomeTypeService projectincomeTypeService, final CProjectItemStatusService projectItemStatusService) {
		super(repository, clock, sessionService, projectItemStatusService);
		this.projectincomeTypeService = projectincomeTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectIncome entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	protected void validateEntity(final CProjectIncome entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Income type is required");
		
		// 2. Length Checks
		if (entity.getName().length() > CEntityConstants.MAX_LENGTH_NAME) {
			throw new IllegalArgumentException(ValidationMessages.formatMaxLength(ValidationMessages.NAME_MAX_LENGTH, CEntityConstants.MAX_LENGTH_NAME));
		}
		
		// 3. Unique Checks
		final Optional<CProjectIncome> existingName = ((IProjectIncomeRepository) repository).findByNameAndProject(entity.getName(), entity.getProject());
		if (existingName.isPresent() && !existingName.get().getId().equals(entity.getId())) {
			throw new IllegalArgumentException(ValidationMessages.DUPLICATE_NAME_IN_PROJECT);
		}
		
		// 4. Numeric Checks
		if (entity.getAmount() != null) {
			if (entity.getAmount().compareTo(BigDecimal.ZERO) < 0) {
				throw new IllegalArgumentException("Amount must be positive");
			}
			if (entity.getAmount().compareTo(new BigDecimal("9999999999.99")) > 0) {
				throw new IllegalArgumentException("Amount cannot exceed 9,999,999,999.99");
			}
		}
	}

	@Override
	public Class<CProjectIncome> getEntityClass() { return CProjectIncome.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectIncomeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectIncome.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@SuppressWarnings ("null")
	@Override
	public void initializeNewEntity(final CProjectIncome entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new projectincome entity");
		final CProject<?> currentProject = sessionService.getActiveProject()
				.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize projectincome"));
		IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, projectincomeTypeService, projectItemStatusService);
		LOGGER.debug("ProjectIncome initialization complete");
	}
}
