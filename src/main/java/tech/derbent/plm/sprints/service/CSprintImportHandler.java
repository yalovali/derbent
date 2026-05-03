package tech.derbent.plm.sprints.service;

import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.sprints.domain.CSprint;
import tech.derbent.plm.sprints.domain.CSprintType;

/** Imports {@link CSprint} rows from Excel into the active project. */
@Service
@Profile({"derbent", "default"})
public class CSprintImportHandler extends CProjectItemImportHandler<CSprint, CSprintType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CSprintImportHandler.class);

	private final CSprintService sprintService;
	private final CSprintTypeService typeService;

	public CSprintImportHandler(final CSprintService sprintService, final CSprintTypeService typeService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository) {
		super(statusService, userRepository);
		this.sprintService = sprintService;
		this.typeService = typeService;
	}

	@Override
	public Class<CSprint> getEntityClass() { return CSprint.class; }

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Type", "entitytype");
	}

	@Override
	protected Class<CSprintType> getTypeClass() { return CSprintType.class; }

	@Override
	protected Optional<CSprint> findByNameAndProject(final String name, final CProject<?> project) {
		return sprintService.findByNameAndProject(name, project);
	}

	@Override
	protected CSprint createNew(final String name, final CProject<?> project) {
		return new CSprint(name, project);
	}

	@Override
	protected void save(final CSprint entity) {
		sprintService.save(entity);
	}

	@Override
	protected Optional<CSprintType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return typeService.findByNameAndCompany(name, company);
	}

	@Override
	protected void applyExtraFields(final CSprint entity, final CExcelRow row, final CProject<?> project, final int rowNumber,
			final Map<String, String> rowData) {
		applyMetaFieldsDeclaredOn(entity, row, CSprint.class);
	}
}
