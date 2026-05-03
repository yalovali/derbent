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
import tech.derbent.api.screens.domain.CMasterSection;
import tech.derbent.api.users.service.IUserRepository;

/** Imports CMasterSection rows from Excel (project-scoped master view template configuration). */
@Service
public class CMasterSectionImportHandler extends CEntityOfProjectImportHandler<CMasterSection> {

	private final CMasterSectionService masterSectionService;

	public CMasterSectionImportHandler(final CMasterSectionService masterSectionService,
			final IUserRepository userRepository, final CImportProjectResolver projectResolver) {
		super(userRepository, projectResolver);
		this.masterSectionService = masterSectionService;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Name", "name", "Section Type", "sectiontype", "Section DB Name", "sectiondbname");
	}

	@Override
	public Class<CMasterSection> getEntityClass() { return CMasterSection.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("name", "sectiontype"); }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CMasterSection");
		names.add("MasterSection");
		names.add("Master Section");
		names.add("Master Sections");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CMasterSection.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CMasterSection.class);
			if (singular != null && !singular.isBlank()) {
				names.add(singular);
			}
			if (plural != null && !plural.isBlank()) {
				names.add(plural);
			}
		} catch (final Exception ignored) { /* registry may not be ready */ }
		return names;
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final var row = row(rowData);
		final var nameError = validateEntityNamed(row, rowNumber, rowData);
		if (nameError.isPresent()) {
			return nameError.get();
		}
		final CProject<?> effectiveProject = resolveProjectFromRow(row, project);
		final String name = row.string("name");
		final String sectionType = row.string("sectiontype");
		if (sectionType.isBlank()) {
			return CImportRowResult.error(rowNumber, "Section Type is required", rowData);
		}
		// WHY: screen configuration must be re-runnable; upsert by name avoids duplicate bootstrap runs failing.
		final CMasterSection entity = masterSectionService.findByNameAndProject(name, effectiveProject)
				.orElseGet(() -> new CMasterSection(name, effectiveProject));
		final var projectFieldsError = applyEntityOfProjectFields(entity, row, effectiveProject, rowNumber, rowData);
		if (projectFieldsError.isPresent()) {
			return projectFieldsError.get();
		}
		entity.setSectionType(sectionType);
		row.optionalString("sectiondbname").ifPresent(entity::setSectionDBName);
		if (!options.isDryRun()) {
			masterSectionService.save(entity);
		}
		return CImportRowResult.success(rowNumber, name);
	}
}
