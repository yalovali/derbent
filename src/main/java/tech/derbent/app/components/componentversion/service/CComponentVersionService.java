package tech.derbent.app.components.componentversion.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.entityOfProject.service.CProjectItemService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.app.activities.service.CProjectItemStatusService;
import tech.derbent.app.projects.domain.CProject;
import tech.derbent.app.components.componentversion.domain.CComponentVersion;
import tech.derbent.app.components.componentversiontype.service.CComponentVersionTypeService;
import tech.derbent.app.components.component.service.CComponentService;
import tech.derbent.app.workflow.service.IHasStatusAndWorkflowService;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Menu (order = 0, icon = "vaadin:file-o", title = "Settings.ComponentVersions")
@PermitAll
public class CComponentVersionService extends CProjectItemService<CComponentVersion> implements IEntityRegistrable {

private static final Logger LOGGER = LoggerFactory.getLogger(CComponentVersionService.class);
private final CComponentVersionTypeService componentversionTypeService;
private final CComponentService cComponentService;

CComponentVersionService(final IComponentVersionRepository repository, final Clock clock, 
final ISessionService sessionService,
final CComponentVersionTypeService componentversionTypeService,
final CComponentService cComponentService,
final CProjectItemStatusService projectItemStatusService) {
super(repository, clock, sessionService, projectItemStatusService);
this.componentversionTypeService = componentversionTypeService;
this.cComponentService = cComponentService;
}

@Override
public String checkDeleteAllowed(final CComponentVersion entity) {
return super.checkDeleteAllowed(entity);
}

@Override
public Class<CComponentVersion> getEntityClass() { return CComponentVersion.class; }

@Override
public Class<?> getInitializerServiceClass() { return CComponentVersionInitializerService.class; }

@Override
public Class<?> getPageServiceClass() { return CPageServiceComponentVersion.class; }

@Override
public Class<?> getServiceClass() { return this.getClass(); }

@Override
public void initializeNewEntity(final CComponentVersion entity) {
super.initializeNewEntity(entity);
LOGGER.debug("Initializing new componentversion entity");
final CProject currentProject = sessionService.getActiveProject()
.orElseThrow(() -> new CInitializationException("No active project"));
IHasStatusAndWorkflowService.initializeNewEntity(entity, currentProject, 
componentversionTypeService, projectItemStatusService);
LOGGER.debug("ComponentVersion initialization complete");
}
}
