package tech.derbent.plm.storage.storageitem.service;

import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.page.service.CPageService;

@Service
@Menu(icon = "vaadin:package", title = "Types.StorageItemTypes")
@PermitAll
public class CPageServiceStorageItemType extends CPageService<CStorageItemType> {

    public CPageServiceStorageItemType(final CStorageItemTypeService service) {
        super(service);
    }
}
