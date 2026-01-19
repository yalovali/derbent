package tech.derbent.plm.storage.storagetype.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfCompany.service.ITypeEntityRepository;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

@Repository
public interface IStorageTypeRepository extends ITypeEntityRepository<CStorageType> {
}
