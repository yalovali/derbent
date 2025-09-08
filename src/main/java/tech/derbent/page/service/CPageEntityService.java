package tech.derbent.page.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.domains.CProjectItemService;
import tech.derbent.page.domain.CPageEntity;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CPageEntityService extends CProjectItemService<CPageEntity> {

	public CPageEntityService(final CPageEntityRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CPageEntity> getEntityClass() { return CPageEntity.class; }
}
