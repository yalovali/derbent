package tech.derbent.plm.deliverables.deliverable.service;

import java.util.Optional;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.deliverables.deliverable.domain.CDeliverable;
import tech.derbent.plm.deliverables.deliverabletype.domain.CDeliverableType;
import tech.derbent.plm.deliverables.deliverabletype.service.CDeliverableTypeService;

/** Imports {@link CDeliverable} rows from Excel into the active project. */
@Service
@Profile ({
		"derbent", "bab", "default"
})
public class CDeliverableImportHandler extends CProjectItemImportHandler<CDeliverable, CDeliverableType> {

	private final CDeliverableService deliverableService;
	private final CDeliverableTypeService typeService;

	public CDeliverableImportHandler(final CDeliverableService deliverableService,
			final CDeliverableTypeService typeService, final CProjectItemStatusService statusService,
			final IUserRepository userRepository, final CImportProjectResolver projectResolver) {
		super(statusService, userRepository, projectResolver);
		this.deliverableService = deliverableService;
		this.typeService = typeService;
	}

	@Override
	protected CDeliverable createNew(final String name, final CProject<?> project) {
		return new CDeliverable(name, project);
	}

	@Override
	protected Optional<CDeliverable> findByNameAndProject(final String name, final CProject<?> project) {
		return deliverableService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CDeliverableType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return typeService.findByNameAndCompany(name, company);
	}

	@Override
	public Class<CDeliverable> getEntityClass() { return CDeliverable.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("name", "entitytype"); }

	@Override
	protected Class<CDeliverableType> getTypeClass() { return CDeliverableType.class; }

	@Override
	protected boolean isTypeRequired() { return true; }

	@Override
	protected void save(final CDeliverable entity) {
		deliverableService.save(entity);
	}
}
