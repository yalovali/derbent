package tech.derbent.abstracts.views.grids;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import tech.derbent.abstracts.domains.CEntityOfProject;
import tech.derbent.abstracts.services.CEntityOfProjectService;
import tech.derbent.abstracts.utils.CPageableUtils;
import tech.derbent.abstracts.views.CProjectAwareMDPage;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseProject<EntityClass extends CEntityOfProject<EntityClass>> extends CProjectAwareMDPage<EntityClass> {
	private static final long serialVersionUID = 1L;

	protected CGridViewBaseProject(final Class<EntityClass> entityClass, final CEntityOfProjectService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService, screenService);
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	protected void createMasterComponent() {
		masterViewSection = new CMasterViewSectionGrid<EntityClass>(entityClass, this);
	}

	@Override
	protected CallbackDataProvider<EntityClass, Void> getMasterQuery() {
		return new CallbackDataProvider<>(query -> {
			// --- sort (manuel çeviri)
			final List<QuerySortOrder> sortOrders = Optional.ofNullable(query.getSortOrders()).orElse(java.util.Collections.emptyList());
			final Sort springSort = sortOrders.isEmpty() ? Sort.unsorted()
					: Sort.by(sortOrders.stream().map(so -> new Sort.Order(
							so.getDirection() == com.vaadin.flow.data.provider.SortDirection.DESCENDING ? Sort.Direction.DESC : Sort.Direction.ASC,
							so.getSorted())).toList());
			// --- paging
			final int limit = query.getLimit();
			final int offset = query.getOffset();
			final int page = (limit > 0) ? (offset / limit) : 0;
			final Pageable pageable = CPageableUtils.validateAndFix(PageRequest.of(page, Math.max(limit, 1), springSort));
			final var project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project selected"));
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			// *** TEK KAYNAK: her zaman search'lü metodu kullan ***
			return ((CEntityOfProjectService<EntityClass>) entityService).listByProject(project, pageable, term).stream();
		}, query -> {
			final var project = sessionService.getActiveProject().orElseThrow(() -> new IllegalStateException("No active project selected"));
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			// *** COUNT DA AYNI METOTTAN ***
			final long total =
					((CEntityOfProjectService<EntityClass>) entityService).listByProject(project, PageRequest.of(0, 1), term).getTotalElements();
			return (int) Math.min(total, Integer.MAX_VALUE);
		});
	}
}
