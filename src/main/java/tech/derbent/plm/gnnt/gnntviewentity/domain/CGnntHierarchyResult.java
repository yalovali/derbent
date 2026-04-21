package tech.derbent.plm.gnnt.gnntviewentity.domain;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;

public class CGnntHierarchyResult {

	private final Map<String, List<CGnntItem>> childrenByParentKey;
	private final List<CGnntItem> flatItems;
	private final List<CGnntItem> rootItems;

	public CGnntHierarchyResult(final List<CGnntItem> rootItems, final Map<String, List<CGnntItem>> childrenByParentKey,
			final List<CGnntItem> flatItems) {
		this.rootItems = rootItems != null ? List.copyOf(rootItems) : List.of();
		this.childrenByParentKey = childrenByParentKey != null ? Map.copyOf(childrenByParentKey) : Map.of();
		this.flatItems = flatItems != null ? List.copyOf(flatItems) : List.of();
	}

	public List<CGnntItem> getChildren(final CGnntItem parent) {
		if (parent == null || parent.getEntityKey() == null) {
			return List.of();
		}
		return childrenByParentKey.getOrDefault(parent.getEntityKey(), List.of());
	}

	public boolean hasChildren(final CGnntItem item) {
		return item != null && !getChildren(item).isEmpty();
	}

	public Map<String, List<CGnntItem>> getChildrenByParentKey() {
		return Collections.unmodifiableMap(childrenByParentKey);
	}

	public List<CGnntItem> getFlatItems() {
		return flatItems;
	}

	public List<CGnntItem> getRootItems() {
		return rootItems;
	}

	public boolean isEmpty() {
		return flatItems.isEmpty();
	}
}
