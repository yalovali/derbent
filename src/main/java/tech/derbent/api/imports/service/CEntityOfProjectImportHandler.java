package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.exceptions.CValidationException;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;

/** Base importer for project-scoped entities.
 * <p>
 * RULE: This handler level owns importing fields declared on {@link CEntityOfProject} (e.g. {@code assignedTo}). Child handlers must not duplicate
 * this mapping.
 * </p>
 * <p>
 * PARENT COLUMN: Excel rows may include a "project" column. When present, the row is resolved to that specific project; when absent, the
 * session/context project is used. This makes each row self-describing and allows a single workbook to target multiple projects.
 * </p>
 */
public abstract class CEntityOfProjectImportHandler<T extends CEntityOfProject<T>>
		extends CEntityNamedImportHandler<T> {

	private final CImportProjectResolver projectResolver;
	protected final IUserRepository userRepository;

	protected CEntityOfProjectImportHandler(final IUserRepository userRepository,
			final CImportProjectResolver projectResolver) {
		this.userRepository = userRepository;
		this.projectResolver = projectResolver;
	}

	protected final Optional<CImportRowResult> applyEntityOfProjectFields(final T entity, final CExcelRow row,
			final CProject<?> project, final int rowNumber, final Map<String, String> rowData) {
		applyEntityNamedFields(entity, row);
		final String assignedToLogin = row.string("assignedto");
		if (!assignedToLogin.isBlank()) {
			final CCompany company = project.getCompany();
			final CUser user = userRepository.findByUsernameIgnoreCase(company.getId(), assignedToLogin).orElse(null);
			if (user == null) {
				return Optional.of(CImportRowResult.error(rowNumber,
						"Assigned user '" + assignedToLogin + "' not found in company. Create it before importing.",
						rowData));
			}
			entity.setAssignedTo(user);
		}
		return Optional.empty();
	}

	protected final CProject<?> resolveProjectFromRow(final CExcelRow row, final CProject<?> sessionProject) {
		final String projectName = row.string("project");
		if (projectName.isBlank()) {
			return sessionProject;
		}
		if (sessionProject.getName() != null && projectName.equalsIgnoreCase(sessionProject.getName())) {
			return sessionProject;
		}
		CCompany company = sessionProject.getCompany();
		final String companyName = row.string("company");
		if (!companyName.isBlank() && !isWildcard(companyName)) {
			company = projectResolver.findCompanyByName(companyName).orElse(null);
		}
		if (company == null) {
			throw new CValidationException("Company '" + companyName + "' not found. Create it before importing.");
		}
		return projectResolver.findProjectByNameAndCompany(projectName, company)
				.orElseThrow(() -> new CValidationException("Project '" + projectName + "' not found in company '"
						+ companyName + "'. Create it before importing."));
	}
}
