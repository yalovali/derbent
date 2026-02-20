package tech.derbent.bab.policybase.filter.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterBase;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;

/** Coordinator service exposing polymorphic access to all BAB policy filter entities. */
@Service
@Profile ("bab")
@PreAuthorize ("isAuthenticated()")
public class CBabPolicyFilterService {

	private final CBabPolicyFilterCANService canService;
	private final CBabPolicyFilterCSVService csvService;
	private final CBabPolicyFilterROSService rosService;

	public CBabPolicyFilterService(final CBabPolicyFilterCSVService csvService, final CBabPolicyFilterCANService canService,
			final CBabPolicyFilterROSService rosService) {
		this.csvService = csvService;
		this.canService = canService;
		this.rosService = rosService;
	}

	public List<CBabPolicyFilterBase<?>> findCachedFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.findCachedFilters(project));
		allFilters.addAll(canService.findCachedFilters(project));
		allFilters.addAll(rosService.findCachedFilters(project));
		return sortFilters(allFilters);
	}

	public List<CBabPolicyFilterBase<?>> findEnabledFilters(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.findEnabledFilters(project));
		allFilters.addAll(canService.findEnabledFilters(project));
		allFilters.addAll(rosService.findEnabledFilters(project));
		return sortFilters(allFilters);
	}

	public List<CBabPolicyFilterBase<?>> findFiltersForNodeType(final CProject<?> project, final String nodeType) {
		Check.notNull(project, "Project cannot be null");
		Check.notBlank(nodeType, "Node type cannot be blank");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.findFiltersForNodeType(project, nodeType));
		allFilters.addAll(canService.findFiltersForNodeType(project, nodeType));
		allFilters.addAll(rosService.findFiltersForNodeType(project, nodeType));
		return sortFilters(allFilters);
	}

	public List<CBabPolicyFilterBase<?>> findEnabledFilters(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.findEnabledFilters(parentNode));
		allFilters.addAll(canService.findEnabledFilters(parentNode));
		allFilters.addAll(rosService.findEnabledFilters(parentNode));
		return sortFilters(allFilters);
	}

	public List<CBabPolicyFilterBase<?>> listByParentNode(final CBabNodeEntity<?> parentNode) {
		Check.notNull(parentNode, "Parent node cannot be null");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.listByParentNode(parentNode));
		allFilters.addAll(canService.listByParentNode(parentNode));
		allFilters.addAll(rosService.listByParentNode(parentNode));
		return sortFilters(allFilters);
	}

	public List<CBabPolicyFilterBase<?>> listByProject(final CProject<?> project) {
		Check.notNull(project, "Project cannot be null");
		final List<CBabPolicyFilterBase<?>> allFilters = new ArrayList<>();
		allFilters.addAll(csvService.listByProject(project));
		allFilters.addAll(canService.listByProject(project));
		allFilters.addAll(rosService.listByProject(project));
		return sortFilters(allFilters);
	}

	private List<CBabPolicyFilterBase<?>> sortFilters(final List<CBabPolicyFilterBase<?>> filters) {
		filters.sort(Comparator.comparing((CBabPolicyFilterBase<?> filter) -> filter.getExecutionOrder(), Comparator.nullsLast(Integer::compareTo))
				.thenComparing(filter -> filter.getName(), Comparator.nullsLast(String::compareToIgnoreCase)));
		return filters;
	}
}
