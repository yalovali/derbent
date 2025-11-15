package tech.derbent.app.components.componenttype.service;

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
import tech.derbent.app.components.component.service.IComponentRepository;
import tech.derbent.app.components.componenttype.domain.CComponentType;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CComponentTypeService extends CTypeEntityService<CComponentType> implements IEntityRegistrable {

private static final Logger LOGGER = LoggerFactory.getLogger(CComponentTypeService.class);
@Autowired
private IComponentRepository componentRepository;

public CComponentTypeService(final IComponentTypeRepository repository, final Clock clock, 
final ISessionService sessionService, final IComponentRepository componentRepository) {
super(repository, clock, sessionService);
this.componentRepository = componentRepository;
}

@Override
public String checkDeleteAllowed(final CComponentType entity) {
final String superCheck = super.checkDeleteAllowed(entity);
if (superCheck != null) {
return superCheck;
}
try {
final long usageCount = componentRepository.countByType(entity);
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
public Class<CComponentType> getEntityClass() { return CComponentType.class; }

@Override
public Class<?> getInitializerServiceClass() { return CComponentTypeInitializerService.class; }

@Override
public Class<?> getPageServiceClass() { return CPageServiceComponentType.class; }

@Override
public Class<?> getServiceClass() { return this.getClass(); }

@Override
public void initializeNewEntity(final CComponentType entity) {
super.initializeNewEntity(entity);
CProject activeProject = sessionService.getActiveProject()
.orElseThrow(() -> new IllegalStateException("No active project in session"));
long typeCount = ((IComponentTypeRepository) repository).countByProject(activeProject);
String autoName = String.format("ComponentType %02d", typeCount + 1);
entity.setName(autoName);
}
}
