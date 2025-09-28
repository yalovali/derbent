package tech.derbent.orders.service;

import tech.derbent.api.services.IEntityOfProjectRepository;
import tech.derbent.orders.domain.CCurrency;

public interface ICurrencyRepository extends IEntityOfProjectRepository<CCurrency> {
	// Inherits standard operations from CAbstractNamedRepository
	// Additional custom query methods can be added here if needed
}
