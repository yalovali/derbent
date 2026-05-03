package tech.derbent.api.screens.service;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CEntityOfProjectImportHandler;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.users.service.IUserRepository;

/** Imports CGridEntity rows from Excel (project-scoped "view" configuration). */
@Service
public class CGridEntityImportHandler extends CEntityOfProjectImportHandler<CGridEntity> {

	private static List<String> splitCsv(final String csv) {
		return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();
	}

	private final CGridEntityService gridEntityService;
	private final CImportProjectResolver projectResolver;

	public CGridEntityImportHandler(final CGridEntityService gridEntityService, final IUserRepository userRepository,
			final CImportProjectResolver projectResolver) {
		super(userRepository);
		this.gridEntityService = gridEntityService;
		this.projectResolver = projectResolver;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Name", "name", "Data Service Bean", "dataservicebeanname", "Column Fields", "columnfields",
				"Editable Column Fields", "editablecolumnfields", "None Grid", "attributenone", "Company", "company",
				"Project", "project");
	}

	private java.util.Optional<CProject<?>> resolveProjectByNameAndCompany(final CProject<?> sessionProject,
			final String companyName, final String projectName) {
		var company = sessionProject.getCompany();
		if (company != null && companyName != null && !companyName.isBlank()) {
			company = projectResolver.findCompanyByName(companyName).orElse(null);
		}
		if (company == null) {
			return java.util.Optional.empty();
		}
		return projectResolver.findProjectByNameAndCompany(projectName, company);
	}

	@Override
	public Class<CGridEntity> getEntityClass() { return CGridEntity.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("dataservicebeanname"); }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CGridEntity");
		names.add("GridEntity");
		names.add("Grid Entity");
		names.add("Grid Entities");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CGridEntity.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CGridEntity.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	private static String resolveViewNameFromServiceBean(final String beanName) {
		if (beanName == null || beanName.isBlank()) {
			return "";
		}
		final String entitySimpleName = beanName.endsWith("Service") ? beanName.substring(0, beanName.length() - "Service".length()) : beanName;
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entitySimpleName);
			return (String) entityClass.getField("VIEW_NAME").get(null);
		} catch (final Exception ignored) {
			return "";
		}
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final var row = row(rowData);
		// Resolve effective project from "project" column if present; otherwise use session project.
		final String projectName = row.string("project");
		final String companyName = row.string("company");
		final var resolvedProject = projectName.isBlank() || (project.getName() != null && projectName.equalsIgnoreCase(project.getName()))
				? java.util.Optional.of(project)
				: resolveProjectByNameAndCompany(project, companyName, projectName);
		if (resolvedProject.isEmpty()) {
			return CImportRowResult.error(rowNumber,
					"Project '" + projectName + "' not found. Create it before importing.", rowData);
		}
		final CProject<?> effectiveProject = resolvedProject.get();
		final var companyError = validateProjectHasCompany(effectiveProject, rowNumber, rowData);
		if (companyError.isPresent()) {
			return companyError.get();
		}
		final String beanName = row.string("dataservicebeanname");
		if (beanName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Data Service Bean is required", rowData);
		}
		if (row.string("name").isBlank()) {
			rowData.put("name", resolveViewNameFromServiceBean(beanName));
		}
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final String name = row.string("name");
		// WHY: view configuration should be re-runnable; we upsert by name to avoid duplicate bootstrap runs failing.
		final CGridEntity entity = gridEntityService.findByNameAndProject(name, effectiveProject)
				.orElseGet(() -> new CGridEntity(name, effectiveProject));
		final var projectFieldsError = applyEntityOfProjectFields(entity, row, effectiveProject, rowNumber, rowData);
		if (projectFieldsError.isPresent()) {
			return projectFieldsError.get();
		}
		entity.setDataServiceBeanName(beanName);
		row.optionalString("columnfields").ifPresent(v -> entity.setColumnFields(splitCsv(v)));
		row.optionalString("editablecolumnfields").ifPresent(v -> entity.setEditableColumnFields(splitCsv(v)));
		row.optionalBoolean("attributenone").ifPresent(entity::setAttributeNone);
		if (!options.isDryRun()) {
			gridEntityService.save(entity);
		}
		return CImportRowResult.success(rowNumber, name);
	}
}
