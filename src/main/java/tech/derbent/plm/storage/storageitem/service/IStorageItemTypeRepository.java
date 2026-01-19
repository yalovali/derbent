package tech.derbent.plm.storage.storageitem.service;

import org.springframework.stereotype.Repository;
import tech.derbent.api.entityOfCompany.service.ITypeEntityRepository;

@Repository
public interface IStorageItemTypeRepository extends ITypeEntityRepository<CStorageItemType> {
}
