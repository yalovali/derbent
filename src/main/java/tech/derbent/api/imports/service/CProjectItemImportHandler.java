package tech.derbent.api.imports.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.domains.CTypeEntity;
import tech.derbent.api.entityOfCompany.domain.CProjectItemStatus;
import tech.derbent.api.entityOfCompany.service.CProjectItemStatusService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.service.IUserRepository;

/** Base importer for project-scoped items with a {@code CTypeEntity} type + status + assigned user.
 * <p>
 * WHY: many project management entities (Epic/Feature/UserStory/Requirement/Milestone/Deliverable/...) share the same import mechanics:
 * <ul>
 * <li>upsert-by-name within the active project</li>
 * <li>resolve Status by name within the active company</li>
 * <li>resolve EntityType by name within the active company</li>
 * <li>resolve Assigned To by company-scoped username/login</li>
 * </ul>
 * Implementations provide the persistence + lookup hooks; this class keeps the import format consistent.
 * </p>
 */
public abstract class CProjectItemImportHandler<T extends CProjectItem<T, TType>, TType extends CTypeEntity<TType>>
		extends CEntityOfProjectImportHandler<T> {

	protected final CProjectItemStatusService statusService;

	protected CProjectItemImportHandler(final CProjectItemStatusService statusService,
			final IUserRepository userRepository) {
		super(userRepository);
		this.statusService = statusService;
	}

	/** Hook for entity-specific fields (dates, points, acceptance criteria, etc.). */
	protected void applyExtraFields(@SuppressWarnings ("unused") final T entity,
			@SuppressWarnings ("unused") final CExcelRow row, @SuppressWarnings ("unused") final CProject<?> project,
			@SuppressWarnings ("unused") final int rowNumber,
			@SuppressWarnings ("unused") final Map<String, String> rowData) {
		// default: no extra fields
	}

	protected abstract T createNew(String name, CProject<?> project);
	protected abstract Optional<T> findByNameAndProject(String name, CProject<?> project);
	protected abstract Optional<TType> findTypeByNameAndCompany(String name, CCompany company);

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		// Common human header: "Type". Concrete entities typically expose "Epic Type", "Requirement Type", etc via @AMetaData.
		return Map.of("Type", "entitytype");
	}

	protected abstract Class<TType> getTypeClass();

	@Override
	@Transactional
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final CExcelRow row = row(rowData);
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final var companyError = validateProjectHasCompany(project, rowNumber, rowData);
		if (companyError.isPresent()) {
			return companyError.get();
		}
		final String name = row.string("name");
		final CCompany company = project.getCompany();
		// WHY: system_init.xlsx and sample workbooks are intended to be re-runnable.
		final T entity = findByNameAndProject(name, project).orElseGet(() -> createNew(name, project));
		final var projectFieldsError = applyEntityOfProjectFields(entity, row, project, rowNumber, rowData);
		if (projectFieldsError.isPresent()) {
			return projectFieldsError.get();
		}
		final String statusName = row.string("status");
		if (!statusName.isBlank()) {
			final CProjectItemStatus status = statusService.findByNameAndCompany(statusName, company).orElse(null);
			if (status == null) {
				return CImportRowResult.error(rowNumber,
						"Status '" + statusName + "' not found. Create it before importing.", rowData);
			}
			entity.setStatus(status);
		}
		final String typeName = row.string("entitytype");
		if (!typeName.isBlank()) {
			final TType type = findTypeByNameAndCompany(typeName, company).orElse(null);
			if (type == null) {
				return CImportRowResult.error(rowNumber,
						getTypeClass().getSimpleName().replaceFirst("^C", "").replace("Type", " Type") + " '" + typeName
								+ "' not found. Create it before importing.",
						rowData);
			}
			entity.setEntityType(type);
		} else if (isTypeRequired()) {
			return CImportRowResult.error(rowNumber,
					getTypeClass().getSimpleName().replaceFirst("^C", "").replace("Type", " Type") + " is required",
					rowData);
		}
		try {
			applyExtraFields(entity, row, project, rowNumber, rowData);
		} catch (final RuntimeException e) {
			return CImportRowResult.error(rowNumber, e.getClass().getSimpleName() + ": " + e.getMessage(), rowData);
		}
		if (!options.isDryRun()) {
			save(entity);
		}
		return CImportRowResult.success(rowNumber, name);
	}

	protected boolean isTypeRequired() { return false; }

	protected abstract void save(T entity);
}
