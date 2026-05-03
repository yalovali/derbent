package tech.derbent.plm.activities.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.plm.activities.domain.CActivityPriority;

/** Imports CActivityPriority rows from Excel (company-scoped reference data). */
@Service
@Profile({"derbent", "default"})
public class CActivityPriorityImportHandler implements IEntityImportHandler<CActivityPriority> {

	private final CActivityPriorityService priorityService;

	public CActivityPriorityImportHandler(final CActivityPriorityService priorityService) {
		this.priorityService = priorityService;
	}

	@Override
	public Class<CActivityPriority> getEntityClass() {
		return CActivityPriority.class;
	}

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CActivityPriority");
		names.add("ActivityPriority");
		names.add("Activity Priority");
		names.add("Activity Priorities");
		try {
			final String singular = CEntityRegistry.getEntityTitleSingular(CActivityPriority.class);
			final String plural = CEntityRegistry.getEntityTitlePlural(CActivityPriority.class);
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
	public Map<String, String> getColumnAliases() {
		return Map.of(
				"Name", "name",
				"Color", "color",
				"Sort Order", "sortorder",
				"Priority Level", "prioritylevel",
				"Is Default", "isdefault");
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("name");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String name = rowData.getOrDefault("name", "").trim();
		if (name.isBlank()) {
			return CImportRowResult.error(rowNumber, "Name is required", rowData);
		}
		if (project.getCompany() == null) {
			return CImportRowResult.error(rowNumber, "Project company is required to create activity priorities", rowData);
		}
		// WHY: system init Excel is intended to be re-runnable; upsert-by-name avoids unique constraint violations.
		final CActivityPriority priority = priorityService.findByNameAndCompany(name, project.getCompany())
				.orElseGet(() -> new CActivityPriority(name, project.getCompany()));

		final String color = rowData.getOrDefault("color", "").trim();
		if (!color.isBlank()) {
			priority.setColor(color);
		}
		final String sortOrderStr = rowData.getOrDefault("sortorder", "").trim();
		if (!sortOrderStr.isBlank()) {
			try {
				priority.setSortOrder(Integer.valueOf(sortOrderStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid sort order: " + sortOrderStr, rowData);
			}
		}
		final String priorityLevelStr = rowData.getOrDefault("prioritylevel", "").trim();
		if (!priorityLevelStr.isBlank()) {
			try {
				priority.setPriorityLevel(Integer.valueOf(priorityLevelStr));
			} catch (final Exception e) {
				return CImportRowResult.error(rowNumber, "Invalid priority level: " + priorityLevelStr, rowData);
			}
		}
		final String isDefaultStr = rowData.getOrDefault("isdefault", "").trim();
		if (!isDefaultStr.isBlank()) {
			priority.setIsDefault(Set.of("true", "yes", "1").contains(isDefaultStr.toLowerCase()));
		}

		if (!options.isDryRun()) {
			priorityService.save(priority);
		}
		return CImportRowResult.success(rowNumber, name);
	}
}
