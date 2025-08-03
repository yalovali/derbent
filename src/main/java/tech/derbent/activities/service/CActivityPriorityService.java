package tech.derbent.activities.service;

import java.time.Clock;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.activities.domain.CActivityPriority;

@Service
@Transactional
public class CActivityPriorityService extends CEntityOfProjectService<CActivityPriority> {

    private final CActivityPriorityRepository activityPriorityRepository;

    public CActivityPriorityService(final CActivityPriorityRepository repository, final Clock clock) {
        super(repository, clock);
        this.activityPriorityRepository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CActivityPriority> findByName(final String name) {

        if ((name == null) || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return activityPriorityRepository.findByNameIgnoreCase(name.trim());
    }

    @Transactional(readOnly = true)
    public Optional<CActivityPriority> findDefaultPriority() {
        return activityPriorityRepository.findByIsDefaultTrue();
    }

    @Override
    protected Class<CActivityPriority> getEntityClass() {
        return CActivityPriority.class;
    }
}
