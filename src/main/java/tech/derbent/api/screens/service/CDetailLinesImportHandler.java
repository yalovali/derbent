package tech.derbent.api.screens.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CAbstractExcelImportHandler;
import tech.derbent.api.imports.service.CImportProjectResolver;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.screens.domain.CDetailLines;
import tech.derbent.api.screens.domain.CDetailSection;

/**
 * Imports CDetailLines rows from Excel (individual fields within a detail/form view).
 *
 * WHY: CDetailLines is not a named or project-scoped entity directly, but its parent CDetailSection is.
 * We resolve the parent by "detailsection" column (section name) within the current project.
 * Upsert key: (detailSection, entityProperty) — one entry per field per section.
 */
@Service
public class CDetailLinesImportHandler extends CAbstractExcelImportHandler<CDetailLines> {

	private final CDetailLinesService detailLinesService;
	private final CDetailSectionService detailSectionService;
	private final CImportProjectResolver projectResolver;

	public CDetailLinesImportHandler(final CDetailLinesService detailLinesService,
			final CDetailSectionService detailSectionService, final CImportProjectResolver projectResolver) {
		this.detailLinesService = detailLinesService;
		this.detailSectionService = detailSectionService;
		this.projectResolver = projectResolver;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.ofEntries(
				Map.entry("Detail Section", "detailsection"),
				Map.entry("Entity Property", "entityproperty"),
				Map.entry("Field Caption", "fieldcaption"),
				Map.entry("Relation Field Name", "relationfieldname"),
				Map.entry("Item Order", "itemorder"),
				Map.entry("Is Hidden", "ishidden"),
				Map.entry("Is Readonly", "isreadonly"),
				Map.entry("Is Required", "isrequired"),
				Map.entry("Have Next One On Same Line", "havenextoneonsameline"),
				Map.entry("Section As Tab", "sectionastab"),
				Map.entry("Company", "company"),
				Map.entry("Project", "project"));
	}

	@Override
	public Class<CDetailLines> getEntityClass() { return CDetailLines.class; }

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("detailsection", "entityproperty", "fieldcaption", "relationfieldname");
	}

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CDetailLines");
		names.add("DetailLines");
		names.add("Detail Lines");
		names.add("Detail Line");
		return names;
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final var row = row(rowData);
		// Resolve effective project from "project" column if present; otherwise use session project.
		final String projectName = row.string("project");
		final String companyName = row.string("company");
		final CProject<?> effectiveProject;
		if (projectName.isBlank() || (project.getName() != null && projectName.equalsIgnoreCase(project.getName()))) {
			effectiveProject = project;
		} else {
			var company = project.getCompany();
			if (company != null && companyName != null && !companyName.isBlank()) {
				company = projectResolver.findCompanyByName(companyName).orElse(null);
			}
			if (company == null) {
				return CImportRowResult.error(rowNumber, "Project company context is required", rowData);
			}
			final var resolved = projectResolver.findProjectByNameAndCompany(projectName, company);
			if (resolved.isEmpty()) {
				return CImportRowResult.error(rowNumber,
						"Project '" + projectName + "' not found. Create it before importing.", rowData);
			}
			effectiveProject = resolved.get();
		}
		final String sectionName = row.string("detailsection");
		if (sectionName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Detail Section name is required", rowData);
		}
		final String entityProperty = row.string("entityproperty");
		if (entityProperty.isBlank()) {
			return CImportRowResult.error(rowNumber, "Entity Property is required", rowData);
		}
		final String fieldCaption = row.string("fieldcaption");
		if (fieldCaption.isBlank()) {
			return CImportRowResult.error(rowNumber, "Field Caption is required", rowData);
		}
		final String relationFieldName = row.string("relationfieldname");
		if (relationFieldName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Relation Field Name is required", rowData);
		}
		final CDetailSection section = detailSectionService.findByNameAndProject(effectiveProject, sectionName);
		if (section == null) {
			return CImportRowResult.error(rowNumber,
					"Detail Section '" + sectionName + "' not found. Create it (via Detail Section sheet) before Detail Lines.",
					rowData);
		}
		// WHY: upsert by (section, entityProperty) so bootstrap re-runs don't create duplicate lines.
		final CDetailLines entity = detailLinesService.findBySectionAndEntityProperty(section, entityProperty)
				.orElseGet(() -> new CDetailLines(section, relationFieldName, entityProperty));
		entity.setFieldCaption(fieldCaption);
		entity.setRelationFieldName(relationFieldName);
		final Integer importedOrder = row.optionalInt("itemorder").orElse(null);
		if (importedOrder != null && importedOrder >= 1) {
			entity.setItemOrder(importedOrder);
		} else if (entity.getItemOrder() == null || entity.getItemOrder() < 1) {
			entity.setItemOrder(detailLinesService.getNextItemOrder(section));
		}
		row.optionalBoolean("ishidden").ifPresent(entity::setIsHidden);
		row.optionalBoolean("isreadonly").ifPresent(entity::setIsReadonly);
		row.optionalBoolean("isrequired").ifPresent(entity::setIsRequired);
		row.optionalBoolean("havenextoneonsameline").ifPresent(entity::setHaveNextOneOnSameLine);
		row.optionalBoolean("sectionastab").ifPresent(entity::setSectionAsTab);
		row.optionalString("width").ifPresent(entity::setWidth);
		row.optionalString("sectionname").ifPresent(entity::setSectionName);
		row.optionalString("dataproviderbean").ifPresent(entity::setDataProviderBean);
		row.optionalString("defaultvalue").ifPresent(entity::setDefaultValue);
		row.optionalString("fielddescription").ifPresent(entity::setDescription);
		row.optionalInt("maxlength").ifPresent(entity::setMaxLength);
		row.optionalString("relatedentitytype").ifPresent(entity::setRelatedEntityType);
		if (!options.isDryRun()) {
			detailLinesService.save(entity);
		}
		return CImportRowResult.success(rowNumber, sectionName + "." + entityProperty);
	}
}
