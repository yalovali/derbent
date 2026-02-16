package tech.derbent.bab.dashboard.dashboardproject_bab.service;

import java.time.Clock;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import tech.derbent.api.domains.CEntityConstants;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.interfaces.CCloneOptions;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.api.utils.Check;
import tech.derbent.api.validation.ValidationMessages;
import tech.derbent.bab.dashboard.dashboardproject_bab.domain.CDashboardProject_Bab;
import tech.derbent.bab.ui.component.CComponentDashboardWidget_Bab;

/** CDashboardProject_BabService - Service for BAB dashboard projects. Layer: Service (MVC) Following Derbent pattern: Concrete service with @Service
 * annotation. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CDashboardProject_BabService extends CProjectItemService<CDashboardProject_Bab> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CDashboardProject_BabService.class);

	public CDashboardProject_BabService(final IDashboardProject_BabRepository repository, final Clock clock, final ISessionService sessionService,
			final tech.derbent.api.entityOfCompany.service.CProjectItemStatusService statusService) {
		super(repository, clock, sessionService, statusService);
	}

	/** Copies entity-specific fields from source to target. MANDATORY: All entity services must implement this method.
	 * @param source  the source entity to copy from
	 * @param target  the target entity to copy to
	 * @param options clone options controlling what fields to copy */
	@Override
	public void copyEntityFieldsTo(final CDashboardProject_Bab source, final CEntityDB<?> target, final CCloneOptions options) {
		// STEP 1: ALWAYS call parent first
		super.copyEntityFieldsTo(source, target, options);
		// STEP 2: Type-check target
		if (!(target instanceof final CDashboardProject_Bab targetEntity)) {
			return;
		}
		// STEP 3: Copy fields using DIRECT setter/getter
		targetEntity.setActive(source.getActive());
		targetEntity.setDashboardWidget(source.getDashboardWidget());
		targetEntity.setDashboardType(source.getDashboardType());
		// STEP 4: Log completion
		LOGGER.debug("Copied {} '{}' with options: {}", getClass().getSimpleName(), source.getName(), options);
	}

	/** Create dashboard widget component. Called by component factory via @AMetaData createComponentMethod. */
	public Component createDashboardWidget() {
		try {
			final CComponentDashboardWidget_Bab component = new CComponentDashboardWidget_Bab(this, sessionService);
			LOGGER.debug("Created dashboard widget component");
			return component;
		} catch (final Exception e) {
			LOGGER.error("Failed to create dashboard widget component.", e);
			final Div errorDiv = new Div();
			errorDiv.setText("Error loading dashboard widget: " + e.getMessage());
			errorDiv.addClassName("error-message");
			return errorDiv;
		}
	}

	// Query methods
	@Transactional (readOnly = true)
	public List<CDashboardProject_Bab> findActiveProjects() {
		return ((IDashboardProject_BabRepository) repository).findByActiveTrue();
	}

	@Override
	public Class<CDashboardProject_Bab> getEntityClass() { return CDashboardProject_Bab.class; }

	// Interface implementations
	@Override
	public Class<?> getInitializerServiceClass() { return CDashboardProject_BabInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceDashboardProject_Bab.class; // BAB dashboard PageService
	}

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CDashboardProject_Bab entity) {
		super.validateEntity(entity);
		// Name validation - MANDATORY for business entities
		Check.notBlank(entity.getName(), ValidationMessages.NAME_REQUIRED);
		// Length checks - Use validateStringLength helper
		validateStringLength(entity.getName(), "Name", CEntityConstants.MAX_LENGTH_NAME);
		// Unique constraint check - Use helper
		validateUniqueNameInProject((IDashboardProject_BabRepository) repository, entity, entity.getName(), entity.getProject());
	}
}
