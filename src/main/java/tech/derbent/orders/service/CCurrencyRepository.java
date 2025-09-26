package tech.derbent.orders.service;

import tech.derbent.api.services.CEntityOfProjectRepository;
import tech.derbent.orders.domain.CCurrency;

public interface CCurrencyRepository extends CEntityOfProjectRepository<CCurrency> {
	// Inherits standard operations from CAbstractNamedRepository
	// Additional custom query methods can be added here if needed
}
