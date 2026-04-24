package tech.derbent.api.parentrelation.service;

import com.vaadin.flow.component.Component;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.agile.view.CComponentAgileChildren;
import tech.derbent.plm.agile.view.CComponentAgileParentSelector;

/**
 * Shared page-service factories for hierarchy-aware placeholder components.
 *
 * <p>Keeping the wiring here removes repeated Spring lookups from every page service that exposes
 * parent/children placeholders.</p>
 */
public final class CHierarchyPageSupport {

	private CHierarchyPageSupport() {
		// Utility class.
	}

	public static Component createChildrenComponent() {
		return new CComponentAgileChildren(CSpringContext.getBean(CParentRelationService.class),
				CSpringContext.getBean(CPageEntityService.class), CSpringContext.getBean(ISessionService.class));
	}

	public static Component createParentComponent() {
		return new CComponentAgileParentSelector(CSpringContext.getBean(CParentRelationService.class));
	}
}
