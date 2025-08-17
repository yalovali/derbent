package tech.derbent.screens.service;

import java.time.Clock;
import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.projects.domain.CProject;
import tech.derbent.screens.domain.CScreen;

@Service
@PreAuthorize("isAuthenticated()")
public class CScreenService extends CEntityOfProjectService<CScreen> {

    public CScreenService(final CScreenRepository repository, final Clock clock) {
        super(repository, clock);
    }

    @Override
    protected Class<CScreen> getEntityClass() {
        return CScreen.class;
    }

    /**
     * Find screen by ID with optimized eager loading. Uses repository method with JOIN FETCH to prevent N+1 queries.
     * 
     * @param id
     *            the screen ID
     * @return the screen with eagerly loaded associations, or null if not found
     */
    public CScreen findById(final Long id) {
        if (id == null) {
            return null;
        }
        return ((CScreenRepository) repository).findByIdWithEagerLoading(id).orElse(null);
    }

    /**
     * Find screens by project and entity type.
     * 
     * @param project
     *            the project
     * @param entityType
     *            the entity type
     * @return list of screens for the given project and entity type
     */
    @Transactional(readOnly = true)
    public List<CScreen> findByProjectAndEntityType(final CProject project, final String entityType) {
        return ((CScreenRepository) repository).findByProjectAndEntityType(project, entityType);
    }

    /**
     * Find active screens by project.
     * 
     * @param project
     *            the project
     * @return list of active screens for the given project
     */
    @Transactional(readOnly = true)
    public List<CScreen> findActiveByProject(final CProject project) {
        return ((CScreenRepository) repository).findActiveByProject(project);
    }

    /**
     * Create a new screen with default values.
     * 
     * @param name
     *            the screen name
     * @param project
     *            the project
     * @return the new screen
     */
    public CScreen newEntity(final String name, final CProject project) {
        final CScreen screen = new CScreen(name, project);
        screen.setIsActive(true);
        return screen;
    }

    /**
     * Create a new screen with default values using a dummy project. This method is called by the framework when no
     * specific project is available.
     * 
     * @param name
     *            the screen name
     * @return the new screen
     */
    @Override
    public CScreen newEntity(final String name) {
        // For CScreen, we need a project, so we'll create a temporary one
        // This will be properly set when the screen is actually saved
        final CScreen screen = new CScreen();
        screen.setName(name);
        screen.setIsActive(true);
        return screen;
    }
}