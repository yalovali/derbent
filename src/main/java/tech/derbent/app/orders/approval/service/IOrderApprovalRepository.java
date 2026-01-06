package tech.derbent.app.orders.approval.service;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import tech.derbent.api.entity.service.IAbstractNamedRepository;
import tech.derbent.app.orders.approval.domain.COrderApproval;

/** COrderApprovalRepository - Repository interface for COrderApproval entities. Layer: Service (MVC) Provides data access operations for order
 * approvals, extending the standard CAbstractNamedRepository to inherit common CRUD and query operations. */
public interface IOrderApprovalRepository extends IAbstractNamedRepository<COrderApproval> {

	@Override
	@EntityGraph (attributePaths = {
			"order", "approvalStatus", "approver"
	})
	@Query ("SELECT oa FROM COrderApproval oa")
	List<COrderApproval> findAllForPageView(Sort sort);
	@Override
	@EntityGraph (attributePaths = {
			"order", "approvalStatus", "approver"
	})
	Optional<COrderApproval> findById(Long id);
}
