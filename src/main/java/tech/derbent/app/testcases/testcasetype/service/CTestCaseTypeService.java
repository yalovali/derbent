package tech.derbent.app.testcases.testcasetype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.app.testcases.testcasetype.domain.CTestCaseType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize("isAuthenticated()")
@Menu(icon = "vaadin:tag", title = "Administration.Test Case Types")
@PermitAll
public class CTestCaseTypeService extends CTypeEntityService<CTestCaseType> implements IEntityRegistrable, IEntityWithView {

	private static final Logger LOGGER = LoggerFactory.getLogger(CTestCaseTypeService.class);

	CTestCaseTypeService(final ITestCaseTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final CTestCaseType testCaseType) {
		return super.checkDeleteAllowed(testCaseType);
	}

	@Override
	public Class<CTestCaseType> getEntityClass() {
		return CTestCaseType.class;
	}

	@Override
	public Class<?> getInitializerServiceClass() {
		return CTestCaseTypeInitializerService.class;
	}

	@Override
	public Class<?> getPageServiceClass() {
		return CPageServiceTestCaseType.class;
	}

	@Override
	public Class<?> getServiceClass() {
		return this.getClass();
	}

	@Override
	public void initializeNewEntity(final CTestCaseType entity) {
		super.initializeNewEntity(entity);
		LOGGER.debug("Initializing new test case type entity");
	}
}
