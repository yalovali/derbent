package tech.derbent.bab.policybase.actionmask.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Coordinator service exposing polymorphic access to all action mask entities. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyActionMaskService {

	private final CBabPolicyActionMaskCANService canService;
	private final CBabPolicyActionMaskFileService fileService;
	private final CBabPolicyActionMaskROSService rosService;

	public CBabPolicyActionMaskService(final CBabPolicyActionMaskCANService canService, final CBabPolicyActionMaskFileService fileService,
			final CBabPolicyActionMaskROSService rosService) {
		this.canService = canService;
		this.fileService = fileService;
		this.rosService = rosService;
	}

	public List<CBabPolicyActionMaskBase<?>> findEnabledByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		final List<CBabPolicyActionMaskBase<?>> allMasks = new ArrayList<>();
		allMasks.addAll(canService.findEnabledByParentNode(parentNode));
		allMasks.addAll(fileService.findEnabledByParentNode(parentNode));
		allMasks.addAll(rosService.findEnabledByParentNode(parentNode));
		return sortMasks(allMasks);
	}

	public List<CBabPolicyActionMaskBase<?>> listByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		final List<CBabPolicyActionMaskBase<?>> allMasks = new ArrayList<>();
		allMasks.addAll(canService.listByParentNode(parentNode));
		allMasks.addAll(fileService.listByParentNode(parentNode));
		allMasks.addAll(rosService.listByParentNode(parentNode));
		return sortMasks(allMasks);
	}

	public List<CBabPolicyActionMaskBase<?>> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<CBabPolicyActionMaskBase<?>> allMasks = new ArrayList<>();
		allMasks.addAll(canService.listByProject(project));
		allMasks.addAll(fileService.listByProject(project));
		allMasks.addAll(rosService.listByProject(project));
		return sortMasks(allMasks);
	}

	private List<CBabPolicyActionMaskBase<?>> sortMasks(final List<CBabPolicyActionMaskBase<?>> masks) {
		masks.sort(Comparator.comparing(CBabPolicyActionMaskBase<?>::getExecutionOrder, Comparator.nullsLast(Integer::compareTo))
				.thenComparing(CBabPolicyActionMaskBase::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
		return masks;
	}
}
