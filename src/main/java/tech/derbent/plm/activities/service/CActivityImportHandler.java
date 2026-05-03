package tech.derbent.plm.activities.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.imports.service.CExcelRow;
import tech.derbent.api.imports.service.CProjectItemImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.activities.domain.CActivity;
import tech.derbent.plm.activities.domain.CActivityPriority;
import tech.derbent.plm.activities.domain.CActivityType;

/** Handles import of CActivity rows from Excel. Supported columns (case-insensitive, aliases mapped): name (required), description, status, type
 * (Activity Type), dueDate, estimatedHours Relations resolved by name: status → CProjectItemStatusService, type → CActivityTypeService */
@Service
@Profile ({
		"derbent", "default"
})
public class CActivityImportHandler extends CProjectItemImportHandler<CActivity, CActivityType> {

	private final CActivityService activityService;
	private final CActivityTypeService activityTypeService;
	private final CActivityPriorityService priorityService;

	public CActivityImportHandler(final CActivityService activityService,
			final CActivityTypeService activityTypeService, final CActivityPriorityService priorityService,
			final CProjectItemStatusService statusService, final IUserRepository userRepository) {
		super(statusService, userRepository);
		this.activityService = activityService;
		this.activityTypeService = activityTypeService;
		this.priorityService = priorityService;
	}

	@Override
	protected void applyExtraFields(final CActivity entity, final CExcelRow row, final CProject<?> project,
			final int rowNumber, final Map<String, String> rowData) {
		final String priorityName = row.string("priority");
		if (!priorityName.isBlank()) {
			final CActivityPriority priority =
					priorityService.findByNameAndCompany(priorityName, project.getCompany()).orElse(null);
			if (priority == null) {
				throw new IllegalArgumentException("Priority '" + priorityName + "' not found");
			}
			entity.setPriority(priority);
		}
		applyMetaFieldsDeclaredOn(entity, row, CActivity.class);
	}

	@Override
	protected CActivity createNew(final String name, final CProject<?> project) {
		return new CActivity(name, project);
	}

	@Override
	protected Optional<CActivity> findByNameAndProject(final String name, final CProject<?> project) {
		return activityService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CActivityType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return activityTypeService.findByNameAndCompany(name, company);
	}

	@Override
	public Class<CActivity> getEntityClass() { return CActivity.class; }

	@Override
	protected Class<CActivityType> getTypeClass() { return CActivityType.class; }

	@Override
	protected void save(final CActivity entity) {
		activityService.save(entity);
	}
}
