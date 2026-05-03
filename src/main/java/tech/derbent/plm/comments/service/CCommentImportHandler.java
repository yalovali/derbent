package tech.derbent.plm.comments.service;

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
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/**
 * Imports comment rows and attaches them to an owner entity.
 *
 * WHY: Comments are child entities (unidirectional @OneToMany) so the workbook must declare
 * the owner reference; we persist by saving the owner.
 */
@Service
@Profile({"derbent", "default"})
public class CCommentImportHandler implements IEntityImportHandler<CComment> {

	private final CProjectItemReferenceResolver itemResolver;
	private final IUserRepository userRepository;

	public CCommentImportHandler(final CProjectItemReferenceResolver itemResolver,
			final IUserRepository userRepository) {
		this.itemResolver = itemResolver;
		this.userRepository = userRepository;
	}

	@Override
	public Class<CComment> getEntityClass() { return CComment.class; }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CComment");
		names.add("Comment");
		names.add("Comments");
		return names;
	}

	@Override
	public Map<String, String> getColumnAliases() {
		return Map.ofEntries(
				Map.entry("Owner Type", "ownertype"),
				Map.entry("Owner Name", "ownername"),
				Map.entry("Comment Text", "commenttext"),
				Map.entry("Text", "commenttext"),
				Map.entry("Author", "author"),
				Map.entry("Important", "important")
		);
	}

	@Override
	public Set<String> getRequiredColumns() {
		return Set.of("ownertype", "ownername", "commenttext");
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final String ownerType = rowData.getOrDefault("ownertype", "").trim();
		final String ownerName = rowData.getOrDefault("ownername", "").trim();
		final String commentText = rowData.getOrDefault("commenttext", "").trim();
		if (ownerType.isBlank() || ownerName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Owner Type and Owner Name are required", rowData);
		}
		if (commentText.isBlank()) {
			return CImportRowResult.error(rowNumber, "Comment Text is required", rowData);
		}
		final var ownerOpt = itemResolver.findByTypeAndName(ownerType, ownerName, project);
		if (ownerOpt.isEmpty()) {
			return CImportRowResult.error(rowNumber,
					"Owner '" + ownerType + ":" + ownerName + "' not found in project", rowData);
		}
		if (!(ownerOpt.get() instanceof final IHasComments owner)) {
			return CImportRowResult.error(rowNumber,
					"Owner entity does not support comments: " + ownerOpt.get().getClass().getSimpleName(), rowData);
		}
		final String authorLogin = rowData.getOrDefault("author", "").trim();
		final CUser author;
		if (!authorLogin.isBlank()) {
			// WHY: login/username is the only stable identifier for users across environments.
			author = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), authorLogin).orElse(null);
			if (author == null) {
				return CImportRowResult.error(rowNumber, "Author user '" + authorLogin + "' not found in company", rowData);
			}
		} else {
			author = null;
		}
		final CComment comment = new CComment(commentText, author);
		comment.setCompany(project.getCompany());
		final String importantStr = rowData.getOrDefault("important", "").trim();
		if (!importantStr.isBlank()) {
			comment.setImportant(Set.of("true", "yes", "1").contains(importantStr.toLowerCase()));
		}
		owner.getComments().add(comment);
		if (!options.isDryRun()) {
			itemResolver.save((tech.derbent.api.entityOfProject.domain.CProjectItem<?, ?>) ownerOpt.get());
		}
		return CImportRowResult.success(rowNumber, ownerName);
	}
}
