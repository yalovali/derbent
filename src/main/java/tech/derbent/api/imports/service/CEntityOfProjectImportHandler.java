package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.entityOfProject.domain.CEntityOfProject;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;

/**
 * Base importer for project-scoped entities.
 *
 * <p>RULE: This handler level owns importing fields declared on {@link CEntityOfProject}
 * (e.g. {@code assignedTo}). Child handlers must not duplicate this mapping.</p>
 */
public abstract class CEntityOfProjectImportHandler<T extends CEntityOfProject<T>> extends CEntityNamedImportHandler<T> {

	protected final IUserRepository userRepository;

	protected CEntityOfProjectImportHandler(final IUserRepository userRepository) {
		this.userRepository = userRepository;
	}

	protected final Optional<CImportRowResult> validateProjectHasCompany(final CProject<?> project, final int rowNumber,
			final Map<String, String> rowData) {
		if (project.getCompany() == null) {
			return Optional.of(CImportRowResult.error(rowNumber, "Project company is required", rowData));
		}
		return Optional.empty();
	}

	protected final Optional<CImportRowResult> applyEntityOfProjectFields(final T entity, final CExcelRow row,
			final CProject<?> project, final int rowNumber, final Map<String, String> rowData) {
		applyEntityNamedFields(entity, row);

		final CCompany company = project.getCompany();
		final String assignedToLogin = row.string("assignedto");
		if (!assignedToLogin.isBlank()) {
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
}
