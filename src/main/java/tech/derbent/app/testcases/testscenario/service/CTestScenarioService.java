package tech.derbent.app.testcases.testscenario.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.testcases.testscenario.domain.CTestScenario;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@PermitAll
public class CTestScenarioService extends CEntityOfProjectService<CTestScenario> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestScenarioService.class);

	CTestScenarioService(final ITestScenarioRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTestScenario testScenario) {
		return super.checkDeleteAllowed(testScenario);
	}

	@Override
	public Class<CTestScenario> getEntityClass() {
		return CTestScenario.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return null;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return null;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CTestScenario entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new test scenario entity");
	}
}
