package tech.derbent.abstracts.views.grids;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import tech.derbent.abstracts.domains.CEntityDB;
import tech.derbent.abstracts.domains.IIconSet;
import tech.derbent.abstracts.interfaces.CSearchable;
import tech.derbent.abstracts.services.CAbstractService;
import tech.derbent.abstracts.utils.CPageableUtils;
import tech.derbent.abstracts.utils.Check;
import tech.derbent.abstracts.views.CAbstractEntityDBPage;
import tech.derbent.session.service.CSessionService;

public abstract class CGridViewBaseDBEntity<EntityClass extends CEntityDB<EntityClass>> extends CAbstractEntityDBPage<EntityClass>
		implements IIconSet {
	private static final long serialVersionUID = 1L;

	public CGridViewBaseDBEntity(final Class<EntityClass> entityClass, final CAbstractService<EntityClass> entityService,
			final CSessionService sessionService) {
		super(entityClass, entityService, sessionService);
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
			LOGGER.debug("Grid query - offset: {}, limit: {}, sortOrders: {}, searchText: '{}'", query.getOffset(), query.getLimit(),
					query.getSortOrders(), currentSearchText);
			final Pageable originalPageable = VaadinSpringDataHelpers.toSpringPageRequest(query);
			final Pageable safePageable = CPageableUtils.validateAndFix(originalPageable);
			final Page<EntityClass> result;
			if ((currentSearchText != null) && !currentSearchText.trim().isEmpty() && CSearchable.class.isAssignableFrom(entityClass)) {
				result = entityService.list(safePageable, currentSearchText);
			} else {
				result = entityService.list(safePageable);
			}
			Check.notNull(result, "Data provider returned null list");
			LOGGER.debug("Data provider returned {} items", result.getTotalElements());
			return result.stream();
		}, query -> {
			long result = 0;
			if ((currentSearchText != null) && !currentSearchText.trim().isEmpty() && CSearchable.class.isAssignableFrom(entityClass)) {
				// return (int) entityService.count(currentSearchText);
				result = (int) entityService.count();
			} else {
				result = (int) entityService.count();
			}
			LOGGER.debug("Grid query - offset: {}, limit: {}, sortOrders: {}, searchText: '{}' result:{}", query.getOffset(), query.getLimit(),
					query.getSortOrders(), currentSearchText, result);
			return (int) result;
		});
	}
}
