package tech.derbent.app.comments.service;

import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.app.comments.domain.CComment;

/** Repository interface for CComment entities. Inherits all standard CRUD operations from parent repository. */
public interface ICommentRepository extends IEntityOfCompanyRepository<CComment> {
	// No additional methods needed - uses inherited operations
}
