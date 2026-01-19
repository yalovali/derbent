package tech.derbent.plm.storage.storageitem.service;

import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.page.service.CPageService;
import tech.derbent.plm.storage.storageitem.domain.CStorageItem;

@Service
@Menu(icon = "vaadin:package", title = "PLM.StorageItems")
@PermitAll
public class CPageServiceStorageItem extends CPageService<CStorageItem> {

    public CPageServiceStorageItem(final CStorageItemService service) {
        super(service);
    }
}
