package tech.derbent.plm.storage.storage.service;

import org.springframework.stereotype.Service;
import com.vaadin.flow.router.Menu;
import jakarta.annotation.security.PermitAll;
import tech.derbent.api.page.service.CPageService;
import tech.derbent.plm.storage.storage.domain.CStorage;

@Service
@Menu(icon = "vaadin:storage", title = "PLM.Storage")
@PermitAll
public class CPageServiceStorage extends CPageService<CStorage> {

    public CPageServiceStorage(final CStorageService service) {
        super(service);
    }
}
