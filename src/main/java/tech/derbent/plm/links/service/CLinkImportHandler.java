package tech.derbent.plm.links.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CProjectItemReferenceResolver;
import tech.derbent.api.imports.service.IEntityImportHandler;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * Imports link rows and attaches them to owner entities.
 *
 * WHY: Links are child entities (unidirectional @OneToMany) and must be persisted by saving the owner.
 */
@Service
@Profile({"derbent", "default"})
public class CLinkImportHandler implements IEntityImportHandler<CLink> {

	private final CProjectItemReferenceResolver itemResolver;

	public CLinkImportHandler(final CProjectItemReferenceResolver itemResolver) {
		this.itemResolver = itemResolver;
	}

	@Override
	public Class<CLink> getEntityClass() { return CLink.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CLink");
		names.add("Link");
		names.add("Links");
		return names;
	}

	@Override
	public Map<String, String> getColumnAliases() {
		return Map.ofEntries(
				Map.entry("Source Type", "sourcetype"),
				Map.entry("Source Name", "sourcename"),
				Map.entry("Target Type", "targettype"),
				Map.entry("Target Name", "targetname"),
				Map.entry("Link Type", "linktype"),
				Map.entry("Description", "description"),
				Map.entry("Bidirectional", "bidirectional")
		);
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("sourcetype", "sourcename", "targettype", "targetname");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String sourceType = rowData.getOrDefault("sourcetype", "").trim();
		final String sourceName = rowData.getOrDefault("sourcename", "").trim();
		final String targetType = rowData.getOrDefault("targettype", "").trim();
		final String targetName = rowData.getOrDefault("targetname", "").trim();
		if (sourceType.isBlank() || sourceName.isBlank() || targetType.isBlank() || targetName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Source/Target Type and Name are required", rowData);
		}
		final var sourceOpt = itemResolver.findByTypeAndName(sourceType, sourceName, project);
		if (sourceOpt.isEmpty()) {
			return CImportRowResult.error(rowNumber,
					"Source '" + sourceType + ":" + sourceName + "' not found in project", rowData);
		}
		final var targetOpt = itemResolver.findByTypeAndName(targetType, targetName, project);
		if (targetOpt.isEmpty()) {
			return CImportRowResult.error(rowNumber,
					"Target '" + targetType + ":" + targetName + "' not found in project", rowData);
		}
		if (!(sourceOpt.get() instanceof final IHasLinks sourceOwner)) {
			return CImportRowResult.error(rowNumber,
					"Source entity does not support links: " + sourceOpt.get().getClass().getSimpleName(), rowData);
		}
		final String linkTypeValue = rowData.getOrDefault("linktype", "").trim();
		final String description = rowData.getOrDefault("description", "").trim();

		final CLink link = new CLink(sourceOpt.get().getClass().getSimpleName(), sourceOpt.get().getId(),
				targetOpt.get().getClass().getSimpleName(), targetOpt.get().getId(),
				linkTypeValue.isBlank() ? "Related" : linkTypeValue);
		link.setCompany(project.getCompany());
		if (!description.isBlank()) {
			link.setDescription(description);
		}
		sourceOwner.getLinks().add(link);

		final boolean bidirectional = Set.of("true", "yes", "1").contains(rowData.getOrDefault("bidirectional", "").trim().toLowerCase());
		if (bidirectional) {
			if (!(targetOpt.get() instanceof final IHasLinks targetOwner)) {
				return CImportRowResult.error(rowNumber,
						"Bidirectional requested but target does not support links: " + targetOpt.get().getClass().getSimpleName(), rowData);
			}
			final CLink reverse = new CLink(targetOpt.get().getClass().getSimpleName(), targetOpt.get().getId(),
					sourceOpt.get().getClass().getSimpleName(), sourceOpt.get().getId(),
					linkTypeValue.isBlank() ? "Related" : linkTypeValue);
			reverse.setCompany(project.getCompany());
			if (!description.isBlank()) {
				reverse.setDescription(description);
			}
			targetOwner.getLinks().add(reverse);
			if (!options.isDryRun()) {
				itemResolver.save((tech.derbent.api.entityOfProject.domain.CProjectItem<?, ?>) targetOpt.get());
			}
		}

		if (!options.isDryRun()) {
			itemResolver.save((tech.derbent.api.entityOfProject.domain.CProjectItem<?, ?>) sourceOpt.get());
		}
		return CImportRowResult.success(rowNumber, sourceName);
	}
}
