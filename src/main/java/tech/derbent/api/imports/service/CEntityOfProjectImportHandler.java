package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
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

	protected final IUserRepository userRepository;

	protected CEntityOfProjectImportHandler(final IUserRepository userRepository) {
		this.userRepository = userRepository;
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

	/** Resolves the effective project for a row. If the "project" column is blank, returns the session project unchanged. If specified, looks it up by
	 * name within the session project's company. Returns empty if the named project is not found. */
	protected final Optional<CProject<?>> resolveProjectFromRow(final CExcelRow row, final CProject<?> sessionProject,
			final CImportProjectResolver resolver) {
		final String projectName = row.string("project");
		if (projectName.isBlank()) {
			return Optional.of(sessionProject);
		}
		return resolver.findProjectByNameAndCompany(projectName, sessionProject.getCompany());
	}
}
