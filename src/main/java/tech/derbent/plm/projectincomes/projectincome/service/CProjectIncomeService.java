package tech.derbent.plm.projectincomes.projectincome.service;

import java.math.BigDecimal;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.api.workflow.service.IHasStatusAndWorkflow;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.projectincomes.projectincome.domain.CProjectIncome;
import tech.derbent.plm.projectincomes.projectincometype.service.CProjectIncomeTypeService;

@Profile("derbent")
@Service
@PreAuthorize ("isAuthenticated()")
@PermitAll
public class CProjectIncomeService extends CProjectItemService<CProjectIncome> implements IEntityRegistrable, IEntityWithView {

	@SuppressWarnings ("unused")
	private static final Logger LOGGER = LoggerFactory.getLogger(CProjectIncomeService.class);
	private final CProjectIncomeTypeService typeService;

	CProjectIncomeService(final IProjectIncomeRepository repository, final Clock clock, final ISessionService sessionService,
			final CProjectIncomeTypeService projectincomeTypeService, final CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
		typeService = projectincomeTypeService;
	}

	@Override
	public String checkDeleteAllowed(final CProjectIncome entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<CProjectIncome> getEntityClass() { return CProjectIncome.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CProjectIncomeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceProjectIncome.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final Object entity) {
		super.initializeNewEntity(entity);
		initializeNewEntity_IHasStatusAndWorkflow((IHasStatusAndWorkflow<?>) entity, sessionService.getActiveCompany().orElseThrow(), typeService,
				statusService);
	}

	/**
	 * Copy CProjectIncome-specific fields from source to target entity.
	 * Uses direct setter/getter calls for clarity.
	 * 
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy
	 */
	@Override
	public void copyEntityFieldsTo(final CProjectIncome source, final CEntityDB<?> target,
			final CCloneOptions options) {
		super.copyEntityFieldsTo(source, target, options);
		
		if (!(target instanceof CProjectIncome)) {
			return;
		}
		final CProjectIncome targetIncome = (CProjectIncome) target;
		
		// Copy basic fields
		targetIncome.setAmount(source.getAmount());
		
		// Copy dates conditionally
		if (!options.isResetDates()) {
			targetIncome.setIncomeDate(source.getIncomeDate());
		}
		
		// Copy relations conditionally
		if (options.includesRelations()) {
			targetIncome.setCurrency(source.getCurrency());
		}
		
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	@Override
	protected void validateEntity(final CProjectIncome entity) {
		super.validateEntity(entity);
		
		// 1. Required Fields
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		Check.notNull(entity.getProject(), ValidationMessages.PROJECT_REQUIRED);
		Check.notNull(entity.getEntityType(), "Income type is required");
		
		// 2. Unique Name Check - USE STATIC HELPER
		validateUniqueNameInProject((IProjectIncomeRepository) repository, entity, entity.getName().trim(), entity.getProject());
		
		// 3. Numeric Check - USE STATIC HELPER
		if (entity.getAmount() != null) {
			validateNumericField(entity.getAmount(), "Amount", new BigDecimal("9999999999.99"));
		}
	}
}
