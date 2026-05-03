package tech.derbent.plm.issues.issue.service;

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
import tech.derbent.plm.activities.service.CActivityService;
import tech.derbent.plm.issues.issue.domain.CIssue;
import tech.derbent.plm.issues.issuetype.domain.CIssueType;
import tech.derbent.plm.issues.issuetype.service.CIssueTypeService;

/** Handles import of CIssue rows from Excel. Supported columns: name (required), description, status, type (Issue Type), dueDate Relations resolved
 * by name. */
@Service
@Profile ({
		"derbent", "default"
})
public class CIssueImportHandler extends CProjectItemImportHandler<CIssue, CIssueType> {

	private final CActivityService activityService;
	private final CIssueService issueService;
	private final CIssueTypeService issueTypeService;

	public CIssueImportHandler(final CIssueService issueService, final CIssueTypeService issueTypeService,
			final CProjectItemStatusService statusService, final CActivityService activityService,
			final IUserRepository userRepository) {
		super(statusService, userRepository);
		this.issueService = issueService;
		this.issueTypeService = issueTypeService;
		this.activityService = activityService;
	}

	@Override
	protected void applyExtraFields(final CIssue entity, final CExcelRow row, final CProject<?> project,
			final int rowNumber, final Map<String, String> rowData) {
		applyMetaFieldsDeclaredOn(entity, row, CIssue.class);
		final String linkedActivityName = row.string("linkedactivity");
		if (!linkedActivityName.isBlank()) {
			final var act = activityService.findByNameAndProject(linkedActivityName, project).orElse(null);
			if (act == null) {
				throw new IllegalArgumentException("Linked Activity '" + linkedActivityName + "' not found");
			}
			entity.setLinkedActivity(act);
		}
	}

	@Override
	protected CIssue createNew(final String name, final CProject<?> project) {
		return new CIssue(name, project);
	}

	@Override
	protected Optional<CIssue> findByNameAndProject(final String name, final CProject<?> project) {
		return issueService.findByNameAndProject(name, project);
	}

	@Override
	protected Optional<CIssueType> findTypeByNameAndCompany(final String name, final CCompany company) {
		return issueTypeService.findByNameAndCompany(name, company);
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() { return Map.of("Type", "entitytype"); }

	@Override
	public Class<CIssue> getEntityClass() { return CIssue.class; }

	@Override
	protected Class<CIssueType> getTypeClass() { return CIssueType.class; }

	@Override
	protected void save(final CIssue entity) {
		issueService.save(entity);
	}
}
