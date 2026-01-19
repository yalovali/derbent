package tech.derbent.plm.links.service;

import tech.derbent.api.entityOfCompany.service.IEntityOfCompanyRepository;
import tech.derbent.plm.links.domain.CLink;

/** Repository interface for CLink entities. Inherits all standard CRUD operations from parent repository. */
public interface ILinkRepository extends IEntityOfCompanyRepository<CLink> {
	// No additional methods needed - uses inherited operations
}
