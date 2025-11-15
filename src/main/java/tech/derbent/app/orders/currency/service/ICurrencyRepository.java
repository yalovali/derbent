package tech.derbent.app.orders.currency.service;

import tech.derbent.api.entityOfProject.service.IEntityOfProjectRepository;
import tech.derbent.app.orders.currency.domain.CCurrency;

public interface ICurrencyRepository extends IEntityOfProjectRepository<CCurrency> {
	// Inherits standard operations from CAbstractNamedRepository
	// Additional custom query methods can be added here if needed
}
