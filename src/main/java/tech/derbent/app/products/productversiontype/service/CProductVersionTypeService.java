package tech.derbent.app.products.productversiontype.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.products.productversion.service.IProductVersionRepository;
import tech.derbent.app.products.productversiontype.domain.CProductVersionType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CProductVersionTypeService extends CTypeEntityService<CProductVersionType> implements IEntityRegistrable {

private static final Logger LOGGER = LoggerFactory.getLogger(CProductVersionTypeService.class);
@Autowired
private IProductVersionRepository productversionRepository;

public CProductVersionTypeService(final IProductVersionTypeRepository repository, final Clock clock, 
final ISessionService sessionService, final IProductVersionRepository productversionRepository) {
super(repository, clock, sessionService);
this.productversionRepository = productversionRepository;
}

@Override
public String checkDeleteAllowed(final CProductVersionType entity) {
final String superCheck = super.checkDeleteAllowed(entity);
if (superCheck != null) {
return superCheck;
}
try {
final long usageCount = productversionRepository.countByType(entity);
if (usageCount > 0) {
return String.format("Cannot delete. It is being used by %d item%s.", 
usageCount, usageCount == 1 ? "" : "s");
}
return null;
} catch (final Exception e) {
LOGGER.error("Error checking dependencies: {}", entity.getName(), e);
return "Error checking dependencies: " + e.getMessage();
}
}

@Override
public Class<CProductVersionType> getEntityClass() { return CProductVersionType.class; }

@Override
public Class<?> getInitializerServiceClass() { return CProductVersionTypeInitializerService.class; }

@Override
public Class<?> getPageServiceClass() { return CPageServiceProductVersionType.class; }

@Override
public Class<?> getServiceClass() { return this.getClass(); }

@Override
public void initializeNewEntity(final CProductVersionType entity) {
super.initializeNewEntity(entity);
CProject activeProject = sessionService.getActiveProject()
.orElseThrow(() -> new IllegalStateException("No active project in session"));
long typeCount = ((IProductVersionTypeRepository) repository).countByProject(activeProject);
String autoName = String.format("ProductVersionType %02d", typeCount + 1);
entity.setName(autoName);
}
}
