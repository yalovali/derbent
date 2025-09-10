package tech.derbent.screens.service;

import java.time.Clock;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.screens.domain.CMasterSection;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CMasterSectionService extends CEntityOfProjectService<CMasterSection> {

	public CMasterSectionService(final CMasterSectionRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	protected Class<CMasterSection> getEntityClass() { return CMasterSection.class; }
}
