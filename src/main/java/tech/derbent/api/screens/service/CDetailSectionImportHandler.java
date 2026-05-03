package tech.derbent.api.screens.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CEntityOfProjectImportHandler;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.users.service.IUserRepository;

/** Imports CDetailSection rows from Excel (project-scoped form/detail view configuration). */
@Service
public class CDetailSectionImportHandler extends CEntityOfProjectImportHandler<CDetailSection> {

	private final CDetailSectionService detailSectionService;
	private final CImportProjectResolver projectResolver;

	public CDetailSectionImportHandler(final CDetailSectionService detailSectionService,
			final IUserRepository userRepository, final CImportProjectResolver projectResolver) {
		super(userRepository);
		this.detailSectionService = detailSectionService;
		this.projectResolver = projectResolver;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of(
				"Name", "name",
				"Entity Type", "entitytype",
				"Screen Title", "screentitle",
				"Header Text", "headertext",
				"Default Section", "defaultsection",
				"Non Deletable", "attributenondeletable",
				"Company", "company",
				"Project", "project");
	}

	@Override
	public Class<CDetailSection> getEntityClass() { return CDetailSection.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("entitytype"); }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CDetailSection");
		names.add("DetailSection");
		names.add("Detail Section");
		names.add("Detail Sections");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CDetailSection.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CDetailSection.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	private static String resolveViewNameFromEntityType(final String entityType) {
		if (entityType == null || entityType.isBlank()) {
			return "";
		}
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entityType);
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
		final String entityType = row.string("entitytype");
		if (entityType.isBlank()) {
			return CImportRowResult.error(rowNumber, "Entity Type is required", rowData);
		}
		if (row.string("name").isBlank()) {
			rowData.put("name", resolveViewNameFromEntityType(entityType));
		}
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final String name = row.string("name");
		// WHY: screen configuration must be re-runnable; upsert by name avoids duplicate bootstrap runs failing.
		CDetailSection entity = detailSectionService.findByNameAndProject(effectiveProject, name);
		if (entity == null) {
			entity = new CDetailSection(name, effectiveProject);
		}
		final var projectFieldsError = applyEntityOfProjectFields(entity, row, effectiveProject, rowNumber, rowData);
		if (projectFieldsError.isPresent()) {
			return projectFieldsError.get();
		}
		entity.setEntityType(entityType);
		row.optionalString("screentitle").ifPresent(entity::setScreenTitle);
		row.optionalString("headertext").ifPresent(entity::setHeaderText);
		row.optionalBoolean("defaultsection").ifPresent(entity::setDefaultSection);
		row.optionalBoolean("attributenondeletable").ifPresent(entity::setAttributeNonDeletable);
		if (!options.isDryRun()) {
			detailSectionService.save(entity);
		}
		return CImportRowResult.success(rowNumber, name);
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
}
