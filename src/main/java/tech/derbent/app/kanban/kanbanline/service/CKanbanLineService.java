package tech.derbent.app.kanban.kanbanline.service;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.utils.Check;
import tech.derbent.app.kanban.kanbanline.domain.CKanbanLine;
import tech.derbent.base.session.service.ISessionService;

@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CKanbanLineService extends CEntityOfCompanyService<CKanbanLine> implements IEntityRegistrable {

	private static final Logger LOGGER = LoggerFactory.getLogger(CKanbanLineService.class);

	public CKanbanLineService(final IKanbanLineRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CKanbanLine> getEntityClass() { return CKanbanLine.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CKanbanLineInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceKanbanLine.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final CKanbanLine entity) {
		super.initializeNewEntity(entity);
		Check.notNull(entity, "Entity cannot be null");
		LOGGER.debug("Initializing new Kanban line");
		setNameOfEntity(entity, "Kanban Line");
		if (entity.getColumns() == null) {
			entity.setColumns(new java.util.ArrayList<>());
		}
	}
}
