package tech.derbent.plm.invoices.invoicetype.service;

import java.time.Clock;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.derbent.api.entity.domain.CPageServiceInvoiceType;
import tech.derbent.api.entityOfProject.domain.CTypeEntityService;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.api.registry.IEntityWithView;
import tech.derbent.api.session.service.ISessionService;
import tech.derbent.plm.invoices.invoicetype.domain.CInvoiceType;

@Profile ({"derbent", "default"})
@Service
@PreAuthorize ("isAuthenticated()")
@Transactional (readOnly = true)
public class CInvoiceTypeService extends CTypeEntityService<CInvoiceType> implements IEntityRegistrable, IEntityWithView {

	CInvoiceTypeService(final IInvoiceTypeRepository repository, final Clock clock, final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	@Override
	public Class<CInvoiceType> getEntityClass() { return CInvoiceType.class; }

	@Override
	public Class<?> getInitializerServiceClass() { return CInvoiceTypeInitializerService.class; }

	@Override
	public Class<?> getPageServiceClass() { return CPageServiceInvoiceType.class; }

	@Override
	public Class<?> getServiceClass() { return this.getClass(); }

	@Override
	protected void validateEntity(final CInvoiceType entity) {
		super.validateEntity(entity);
		validateUniqueNameInCompany((IInvoiceTypeRepository) repository, entity, entity.getName(), entity.getCompany());
	}
}
