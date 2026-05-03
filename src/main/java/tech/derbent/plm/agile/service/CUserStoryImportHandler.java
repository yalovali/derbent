package tech.derbent.plm.agile.service;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CAgileEntityImportHandler;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.agile.domain.CUserStory;
import tech.derbent.plm.agile.domain.CUserStoryType;

/** Imports {@link CUserStory} rows from Excel into the active project. */
@Service
@Profile ({
		"derbent", "bab", "default"
})
public class CUserStoryImportHandler extends CAgileEntityImportHandler<CUserStory, CUserStoryType> {

	private final CUserStoryTypeService typeService;
	private final CUserStoryService userStoryService;

	public CUserStoryImportHandler(final CUserStoryService userStoryService, final CUserStoryTypeService typeService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository,
			final CImportProjectResolver projectResolver) {
		super(statusService, userRepository, projectResolver);
		this.userStoryService = userStoryService;
		this.typeService = typeService;
	}

	@Override
	protected CUserStory createNew(final String name, final CProject<?> project) {
		return new CUserStory(name, project);
	}

	@Override
	protected Optional<CUserStory> findByNameAndProject(final String name, final CProject<?> project) {
		return userStoryService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CUserStoryType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return typeService.findByNameAndCompany(name, company);
	}

	@Override
	public Class<CUserStory> getEntityClass() { return CUserStory.class; }

	@Override
	protected Class<CUserStoryType> getTypeClass() { return CUserStoryType.class; }

	@Override
	protected void save(final CUserStory entity) {
		userStoryService.save(entity);
	}
}
