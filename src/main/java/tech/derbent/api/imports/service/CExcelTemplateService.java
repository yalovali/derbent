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
    public static final String SYSTEM_INIT_MINIMAL_TEMPLATE_RESOURCE_PATH = "excel/system_init_min.xlsx";

    public InputStream openSystemInitTemplate(final boolean minimal) {
        final String resourcePath = minimal ? SYSTEM_INIT_MINIMAL_TEMPLATE_RESOURCE_PATH : SYSTEM_INIT_TEMPLATE_RESOURCE_PATH;
        final ClassPathResource resource = new ClassPathResource(resourcePath);
        Check.isTrue(resource.exists(), "Missing classpath resource: " + resourcePath);
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + resourcePath, e);
        }
    }

    public InputStream openSystemInitTemplate() {
        return openSystemInitTemplate(false);
    }
}
