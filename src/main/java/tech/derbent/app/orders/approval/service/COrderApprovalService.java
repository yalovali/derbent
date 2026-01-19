package tech.derbent.app.orders.approval.service;

import java.time.Clock;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import tech.derbent.api.entity.service.CEntityNamedService;
import tech.derbent.api.exceptions.CInitializationException;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.app.orders.approval.domain.CApprovalStatus;
import tech.derbent.app.orders.approval.domain.COrderApproval;
import tech.derbent.app.orders.order.domain.COrder;
import tech.derbent.app.orders.order.service.COrderService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.base.session.service.ISessionService;
import tech.derbent.base.users.domain.CUser;

/** COrderApprovalService - Service layer for COrderApproval entity. Layer: Service (MVC) Handles business logic for order approval operations
 * including creation, validation, and management of order approval entities. */
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class COrderApprovalService extends CEntityNamedService<COrderApproval> implements IEntityRegistrable, IEntityWithView {

	COrderApprovalService(final IOrderApprovalRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public String checkDeleteAllowed(final COrderApproval entity) {
		return super.checkDeleteAllowed(entity);
	}

	@Override
	public Class<COrderApproval> getEntityClass() { return COrderApproval.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return COrderApprovalInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceOrderApproval.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	public void initializeNewEntity(final COrderApproval entity) {
		super.initializeNewEntity(entity);
		// Get current user from session
		final CUser currentUser = sessionService.getActiveUser()
				.orElseThrow(() -> new CInitializationException("No active user in session - cannot initialize order approval"));
		entity.setApprover(currentUser);
		if (entity.getApprovalLevel() == null) {
			entity.setApprovalLevel(1);
		}
		if (entity.getApprovalStatus() == null) {
			final CCompany company = sessionService.getActiveCompany()
					.orElseThrow(() -> new CInitializationException("No active company in session - cannot initialize approval status"));
			final CApprovalStatusService approvalStatusService = CSpringContext.getBean(CApprovalStatusService.class);
			final List<CApprovalStatus> statuses =
					approvalStatusService.listByCompanyForPageView(company, PageRequest.of(0, 1), "").getContent();
			if (statuses.isEmpty()) {
				throw new CInitializationException("No approval statuses available for company - cannot initialize order approval");
			}
			entity.setApprovalStatus(statuses.get(0));
		}
		if (entity.getOrder() == null) {
			final CProject<?> project = sessionService.getActiveProject()
					.orElseThrow(() -> new CInitializationException("No active project in session - cannot initialize order approval"));
			final COrderService orderService = CSpringContext.getBean(COrderService.class);
			final List<COrder> orders = orderService.listByProject(project);
			if (orders.isEmpty()) {
				throw new CInitializationException("No orders available for project - cannot initialize order approval");
			}
			entity.setOrder(orders.get(0));
		}
	}

	@Override
	@Transactional
	public COrderApproval save(final COrderApproval entity) {
		final COrderApproval saved = super.save(entity);
		if (saved.getId() == null) {
			return saved;
		}
		return ((IOrderApprovalRepository) repository).findById(saved.getId()).orElse(saved);
	}
}
