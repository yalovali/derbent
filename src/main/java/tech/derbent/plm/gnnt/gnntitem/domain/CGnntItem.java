package tech.derbent.plm.gnnt.gnntitem.domain;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.lang.reflect.Method;
import org.springframework.data.util.ProxyUtils;
import tech.derbent.api.agileparentrelation.domain.CAgileParentRelation;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.users.domain.CUser;
import tech.derbent.plm.gnnt.gnntitem.service.IGnntEntityItem;

/**
 * Gnnt item wrapper (DTO) around project items for timeline display.
 *
 * <p>This is intentionally a lightweight, non-persisted object used by the Gnnt board grid.</p>
 */
public class CGnntItem {

	public static final String DEFAULT_COLOR = "#3B5FA7";
	public static final String DEFAULT_ICON = "vaadin:bar-chart";
	public static final String ENTITY_TITLE_PLURAL = "Gnnt Items";
	public static final String ENTITY_TITLE_SINGULAR = "Gnnt Item";
	private static final String GENERIC_PROJECT_ITEM_ICON = "vaadin:file";
	public static final String VIEW_NAME = "Gnnt Items View";

	private final LocalDate endDate;
	private final CProjectItem<?> entity;
	private final String entityKey;
	private final String entityType;
	private boolean hasChildren;
	private final int hierarchyLevel;
	private final Long parentId;
	private final String parentType;
	private final LocalDate startDate;

	public CGnntItem(final CProjectItem<?> entity, final long uniqueId, final int hierarchyLevel) {
		this.entity = entity;
		id = uniqueId;
		entityType = ProxyUtils.getUserClass(entity.getClass()).getSimpleName();
		entityKey = entityType + ":" + entity.getId();
		startDate = entity.getStartDate();
		endDate = entity.getEndDate();
		if (entity instanceof IHasAgileParentRelation) {
			final CAgileParentRelation agileParentRelation = ((IHasAgileParentRelation) entity).getAgileParentRelation();
			parentId = agileParentRelation != null ? agileParentRelation.getParentItemId() : null;
			parentType = agileParentRelation != null ? agileParentRelation.getParentItemType() : null;
		} else {
			parentId = entity.getParentId();
			parentType = entity.getParentType();
		}
		this.hierarchyLevel = hierarchyLevel;
	}

	private final long id;

	public CUser getAssignedTo() {
		return entity.getAssignedTo();
	}

	public String getColorCode() {
		try {
			final String runtimeColor = CColorUtils.getColorFromEntity(entity);
			if (runtimeColor != null && !runtimeColor.isBlank()) {
				return runtimeColor;
			}
		} catch (final Exception e) {
			// ignore and fall back to static contract
		}
		try {
			final String colorCode = CColorUtils.getStaticIconColorCode(getEntityClass());
			if (colorCode != null && !colorCode.isBlank()) {
				return colorCode;
			}
		} catch (final Exception e) {
			// ignore
		}
		return DEFAULT_COLOR;
	}

	public String getDescription() {
		return entity.getDescription() != null ? entity.getDescription() : "";
	}

	public long getDurationDays() {
		if (startDate != null && endDate != null) {
			return ChronoUnit.DAYS.between(startDate, endDate) + 1;
		}
		return 1;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public CProjectItem<?> getEntity() {
		return entity;
	}

	public Long getEntityId() {
		return entity.getId();
	}

	public String getEntityKey() {
		return entityKey;
	}

	public String getEntityType() {
		return entityType;
	}

	public String getEntityTypeTitle() {
		final String title = CEntityRegistry.getEntityTitleSingular(ProxyUtils.getUserClass(entity.getClass()));
		return title != null ? title : entityType;
	}

	public int getHierarchyLevel() {
		return hierarchyLevel;
	}

	public String getIconString() {
		final String runtimeIcon = resolveEntityIconString();
		if (runtimeIcon != null) {
			return runtimeIcon;
		}
		try {
			final String iconString = CColorUtils.getStaticIconFilename(getEntityClass());
			if (iconString != null && !iconString.isBlank()) {
				return iconString;
			}
		} catch (final Exception e) {
			// ignore
		}
		return DEFAULT_ICON;
	}

	public long getId() {
		return id;
	}

	public String getIndentedName() {
		return entity.getName();
	}

	public String getName() {
		return entity.getName();
	}

	public Long getParentId() {
		return parentId;
	}

	public String getParentType() {
		return parentType;
	}

	public int getProgressPercentage() {
		if (entity instanceof IGnntEntityItem) {
			final Integer progress = ((IGnntEntityItem) entity).getProgressPercentage();
			return progress != null ? progress : 0;
		}
		if (hasDates()) {
			final LocalDate now = LocalDate.now();
			if (now.isAfter(endDate)) {
				return 100;
			}
			if (now.isBefore(startDate)) {
				return 0;
			}
			final long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
			final long elapsedDays = ChronoUnit.DAYS.between(startDate, now);
			if (totalDays > 0) {
				return (int) (elapsedDays * 100 / totalDays);
			}
		}
		return 0;
	}

	public String getResponsibleName() {
		final CUser responsible = getAssignedTo();
		return responsible != null ? responsible.getName() : "Unassigned";
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public boolean hasDates() {
		return startDate != null && endDate != null;
	}

	public boolean isParentItem() {
		return hasChildren;
	}

	public boolean hasParent() {
		return parentId != null && parentType != null;
	}

	public void setHasChildren(final boolean hasChildren) {
		this.hasChildren = hasChildren;
	}

	private Class<?> getEntityClass() {
		return ProxyUtils.getUserClass(entity.getClass());
	}

	private String resolveEntityIconString() {
		try {
			final Method getIconStringMethod = getEntityClass().getMethod("getIconString");
			if (CProjectItem.class.equals(getIconStringMethod.getDeclaringClass())) {
				return null;
			}
			final String iconString = entity.getIconString();
			if (iconString != null && !iconString.isBlank() && !GENERIC_PROJECT_ITEM_ICON.equals(iconString)) {
				return iconString;
			}
		} catch (final Exception e) {
			// ignore and fall back to static icon contract
		}
		return null;
	}
}
