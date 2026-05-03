package tech.derbent.plm.comments.service;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.imports.domain.CImportOptions;
import tech.derbent.api.imports.domain.CImportRowResult;
import tech.derbent.api.imports.service.CEntityOfCompanyImportHandler;
import tech.derbent.api.imports.service.CProjectItemReferenceResolver;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.api.users.service.IUserRepository;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;

/** Imports comment rows and attaches them to an owner entity. WHY: Comments are child entities (unidirectional @OneToMany) so the workbook must
 * declare the owner reference; we persist by saving the owner. */
@Service
@Profile ({
		"derbent", "default"
})
public class CCommentImportHandler extends CEntityOfCompanyImportHandler<CComment> {

	private final CProjectItemReferenceResolver itemResolver;
	private final IUserRepository userRepository;

	public CCommentImportHandler(final CProjectItemReferenceResolver itemResolver,
			final IUserRepository userRepository) {
		this.itemResolver = itemResolver;
		this.userRepository = userRepository;
	}

	@Override
	protected Map<String, String> getAdditionalColumnAliases() {
		return Map.ofEntries(Map.entry("Owner Type", "ownertype"), Map.entry("Owner Name", "ownername"),
				Map.entry("Comment Text", "commenttext"), Map.entry("Text", "commenttext"),
				Map.entry("Author", "author"), Map.entry("Important", "important"));
	}

	@Override
	public Class<CComment> getEntityClass() { return CComment.class; }

	@Override
	public Set<String> getRequiredColumns() { return Set.of("ownertype", "ownername", "commenttext"); }

	@Override
	public Set<String> getSupportedSheetNames() {
		final Set<String> names = new LinkedHashSet<>();
		names.add("CComment");
		names.add("Comment");
		names.add("Comments");
		return names;
	}

	@Override
	public CImportRowResult importRow(final Map<String, String> rowData, final CProject<?> project, final int rowNumber,
			final CImportOptions options) {
		final var row = row(rowData);
		final String ownerType = row.string("ownertype");
		final String ownerName = row.string("ownername");
		final String commentText = row.string("commenttext");
		if (ownerType.isBlank() || ownerName.isBlank()) {
			return CImportRowResult.error(rowNumber, "Owner Type and Owner Name are required", rowData);
		}
		if (commentText.isBlank()) {
			return CImportRowResult.error(rowNumber, "Comment Text is required", rowData);
		}
		final var ownerOpt = itemResolver.findByTypeAndName(ownerType, ownerName, project);
		if (ownerOpt.isEmpty()) {
			return CImportRowResult.error(rowNumber, "Owner '" + ownerType + ":" + ownerName + "' not found in project",
					rowData);
		}
		if (!(ownerOpt.get() instanceof final IHasComments owner)) {
			return CImportRowResult.error(rowNumber,
					"Owner entity does not support comments: " + ownerOpt.get().getClass().getSimpleName(), rowData);
		}
		final String authorLogin = row.string("author");
		final CUser author;
		if (!authorLogin.isBlank()) {
			// WHY: login/username is the only stable identifier for users across environments.
			author = userRepository.findByUsernameIgnoreCase(project.getCompany().getId(), authorLogin).orElse(null);
			if (author == null) {
				return CImportRowResult.error(rowNumber, "Author user '" + authorLogin + "' not found in company",
						rowData);
			}
		} else {
			author = null;
		}
		final CComment comment = new CComment(commentText, author);
		applyEntityOfCompanyFields(comment, project.getCompany());
		row.optionalBoolean("important").ifPresent(comment::setImportant);
		owner.getComments().add(comment);
		if (!options.isDryRun()) {
			itemResolver.save(ownerOpt.get());
		}
		return CImportRowResult.success(rowNumber, ownerName);
	}
}
