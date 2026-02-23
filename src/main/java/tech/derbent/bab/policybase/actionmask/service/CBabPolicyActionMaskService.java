package tech.derbent.bab.policybase.actionmask.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskBase;

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

	public List<CBabPolicyActionMaskBase<?>> findEnabledByPolicyAction(final CBabPolicyAction policyAction) {
		Check.notNull(policyAction, "Policy action cannot be null");
		final List<CBabPolicyActionMaskBase<?>> allMasks = new ArrayList<>();
		allMasks.addAll(canService.findEnabledByPolicyAction(policyAction));
		allMasks.addAll(fileService.findEnabledByPolicyAction(policyAction));
		allMasks.addAll(rosService.findEnabledByPolicyAction(policyAction));
		return sortMasks(allMasks);
	}

	public List<CBabPolicyActionMaskBase<?>> listByPolicyAction(final CBabPolicyAction policyAction) {
		Check.notNull(policyAction, "Policy action cannot be null");
		final List<CBabPolicyActionMaskBase<?>> allMasks = new ArrayList<>();
		allMasks.addAll(canService.listByPolicyAction(policyAction));
		allMasks.addAll(fileService.listByPolicyAction(policyAction));
		allMasks.addAll(rosService.listByPolicyAction(policyAction));
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
