package tech.derbent.api.imports.service;

import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tech.derbent.api.utils.Check;

/**
 * Provides access to committed Excel templates shipped with the application.
 *
 * WHY: templates must be stable (and editable by developers) so "Excel init" can be a real alternative
 * to code-based initializers and DB reset flows.
 */
@Service
public class CExcelTemplateService {

    public static final String SYSTEM_INIT_TEMPLATE_RESOURCE_PATH = "excel/system_init.xlsx";

    public InputStream openSystemInitTemplate() {
        final ClassPathResource resource = new ClassPathResource(SYSTEM_INIT_TEMPLATE_RESOURCE_PATH);
        Check.isTrue(resource.exists(), "Missing classpath resource: " + SYSTEM_INIT_TEMPLATE_RESOURCE_PATH);
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + SYSTEM_INIT_TEMPLATE_RESOURCE_PATH, e);
        }
    }
}
