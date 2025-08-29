package tech.derbent.abstracts.views.grids;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import tech.derbent.abstracts.domains.CEntityNamed;
import tech.derbent.abstracts.services.CAbstractNamedEntityService;
import tech.derbent.abstracts.utils.CPageableUtils;
import tech.derbent.abstracts.views.CAbstractNamedEntityPage;
import tech.derbent.screens.service.CScreenService;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseNamed<EntityClass extends CEntityNamed<EntityClass>> extends CAbstractNamedEntityPage<EntityClass> {
	private static final long serialVersionUID = 1L;

	public CGridViewBaseNamed(final Class<EntityClass> entityClass, final CAbstractNamedEntityService<EntityClass> entityService,
			final CSessionService sessionService, final CScreenService screenService) {
		super(entityClass, entityService, sessionService, screenService);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createDetailsComponent() throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void createMasterComponent() {
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
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			// *** TEK KAYNAK: her zaman search'lü metodu kullan ***
			return entityService.list(pageable, term).stream();
		}, query -> {
			final String term = (currentSearchText == null) ? "" : currentSearchText.trim();
			// *** COUNT DA AYNI METOTTAN ***
			final long total = entityService.list(PageRequest.of(0, 1), term).getTotalElements();
			// getTotalElements();
			return (int) Math.min(total, Integer.MAX_VALUE);
		});
	}
}
