package tech.derbent.plm.gnnt.gnntviewentity.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import tech.derbent.api.agileparentrelation.service.CAgileParentRelationService;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.entityOfProject.service.CEntityOfProjectService;
import tech.derbent.api.interfaces.IHasAgileParentRelation;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.utils.Check;
import tech.derbent.plm.gnnt.gnntviewentity.view.components.CGnntTimelineHeader.CGanttTimelineRange;
import tech.derbent.plm.gnnt.gnntitem.domain.CGnntItem;
import tech.derbent.plm.gnnt.gnntviewentity.domain.CGnntViewEntity;

@Service
@Profile({"derbent", "default"})
@PreAuthorize("isAuthenticated()")
public class CGnntTimelineService {

	private static final Logger LOGGER = LoggerFactory.getLogger(CGnntTimelineService.class);

	public List<CGnntItem> listTimelineItems(final CGnntViewEntity gnntViewEntity) {
		Check.notNull(gnntViewEntity, "Gnnt view entity cannot be null");
		Check.notNull(gnntViewEntity.getProject(), "Gnnt view entity must belong to a project");
		final List<CGnntItem> items = new ArrayList<>();
		final AtomicLong uniqueIdSequence = new AtomicLong(1);
		final CProject<?> project = gnntViewEntity.getProject();
		for (final String entityKey : CEntityRegistry.getAllRegisteredEntityKeys()) {
			final Class<?> entityClass = CEntityRegistry.getEntityClass(entityKey);
			if (!CProjectItem.class.isAssignableFrom(entityClass) || !IHasAgileParentRelation.class.isAssignableFrom(entityClass)) {
				continue;
			}
			final Class<?> serviceClass = CEntityRegistry.getServiceClassForEntity(entityClass);
			if (serviceClass == null) {
				continue;
			}
			try {
				final Object serviceBean = CSpringContext.getBean(serviceClass);
				if (!(serviceBean instanceof CEntityOfProjectService<?>)) {
					continue;
				}
				final CEntityOfProjectService<?> projectService = (CEntityOfProjectService<?>) serviceBean;
				for (final Object rawEntity : projectService.listByProject(project)) {
					if (!(rawEntity instanceof CProjectItem<?>)) {
						continue;
					}
					final CProjectItem<?> projectItem = (CProjectItem<?>) rawEntity;
					items.add(new CGnntItem(projectItem, uniqueIdSequence.getAndIncrement(), CAgileParentRelationService.getDepth(projectItem)));
				}
			} catch (final Exception e) {
				LOGGER.debug("Skipping Gnnt timeline source {} because it could not be queried: {}", entityClass.getSimpleName(), e.getMessage());
			}
		}
		items.sort(Comparator.comparingInt(CGnntItem::getHierarchyLevel).thenComparing(CGnntItem::getStartDate,
				Comparator.nullsLast(LocalDate::compareTo)).thenComparing(CGnntItem::getName, Comparator.nullsLast(String::compareToIgnoreCase)));
		return items;
	}

	public CGanttTimelineRange resolveRange(final List<CGnntItem> items) {
		if (items == null || items.isEmpty()) {
			final LocalDate today = LocalDate.now();
			return new CGanttTimelineRange(today.minusDays(7), today.plusDays(21));
		}
		LocalDate start = null;
		LocalDate end = null;
		for (final CGnntItem item : items) {
			if (!item.hasDates()) {
				continue;
			}
			if (start == null || item.getStartDate().isBefore(start)) {
				start = item.getStartDate();
			}
			if (end == null || item.getEndDate().isAfter(end)) {
				end = item.getEndDate();
			}
		}
		if (start == null || end == null) {
			final LocalDate today = LocalDate.now();
			return new CGanttTimelineRange(today.minusDays(7), today.plusDays(21));
		}
		return new CGanttTimelineRange(start.minusDays(3), end.plusDays(3));
	}
}
