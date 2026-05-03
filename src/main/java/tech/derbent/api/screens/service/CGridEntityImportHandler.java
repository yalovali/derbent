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

	private static String resolveViewNameFromServiceBean(final String beanName) {
		if (beanName == null || beanName.isBlank()) {
			return "";
		}
		final String entitySimpleName =
				beanName.endsWith("Service") ? beanName.substring(0, beanName.length() - "Service".length()) : beanName;
		try {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entitySimpleName);
			return (String) entityClass.getField("VIEW_NAME").get(null);
		} catch (final Exception ignored) {
			return "";
		}
	}

	private static List<String> splitCsv(final String csv) {
		return Arrays.stream(csv.split(",")).map(String::trim).filter(s -> !s.isBlank()).distinct().toList();
	}

	private final CGridEntityService gridEntityService;

	public CGridEntityImportHandler(final CGridEntityService gridEntityService, final IUserRepository userRepository,
			final CImportProjectResolver projectResolver) {
		super(userRepository, projectResolver);
		this.gridEntityService = gridEntityService;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.of("Name", "name", "Data Service Bean", "dataservicebeanname", "Column Fields", "columnfields",
				"Editable Column Fields", "editablecolumnfields", "None Grid", "attributenone", "Company", "company",
				"Project", "project");
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

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final var row = row(rowData);
		// Resolve effective project from "project" column if present; otherwise use session project.
		final CProject<?> effectiveProject = resolveProjectFromRow(row, project);
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
