package tech.derbent.plm.storage.storagetype.service;

import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.page.service.CPageService;
import tech.derbent.plm.storage.storagetype.domain.CStorageType;

@Service
@Menu(icon = "vaadin:storage", title = "Types.StorageTypes")
@PermitAll
public class CPageServiceStorageType extends CPageService<CStorageType> {

    public CPageServiceStorageType(final CStorageTypeService service) {
        super(service);
    }
}
