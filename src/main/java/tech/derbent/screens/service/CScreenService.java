package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.session.service.CSessionService;

@Service
@PreAuthorize ("isAuthenticated()")
public class CScreenService extends CEntityOfProjectService<CScreen> {

	public CScreenService(final CScreenRepository repository, final Clock clock, final CSessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Transactional (readOnly = true)
	public List<CScreen> findActiveByProject(final CProject project) {
		return ((CScreenRepository) repository).findActiveByProject(project);
	}

	public CScreen findById(final Long id) {
		Check.notNull(id, "ID must not be null");
		return ((CScreenRepository) repository).findByIdWithEagerLoading(id).orElse(null);
	}

	@Transactional (readOnly = true)
	public CScreen findByIdWithScreenLines(final Long id) {
		Check.notNull(id, "ID must not be null");
		return ((CScreenRepository) repository).findByIdWithScreenLines(id).orElse(null);
	}

	@Transactional (readOnly = true)
	public CScreen findByNameAndProject(final CProject project, final String name) {
		Check.notNull(project, "Project must not be null");
		Check.notBlank(name, "Name must not be blank");
		if ((project == null) || (name == null) || name.isBlank()) {
			return null;
		}
		return ((CScreenRepository) repository).findByNameAndProject(project, name).orElse(null);
	}

	@Override
	protected Class<CScreen> getEntityClass() { return CScreen.class; }

	@Override
	public CScreen newEntity(final String name) {
		// For CScreen, we need a project, so we'll create a temporary one This will be
		// properly set when the screen is actually saved
		final CScreen screen = new CScreen();
		screen.setName(name);
		screen.setIsActive(true);
		return screen;
	}

	@Override
	public CScreen newEntity(final String name, final CProject project) {
		final CScreen screen = new CScreen(name, project);
		screen.setIsActive(true);
		return screen;
	}
}
