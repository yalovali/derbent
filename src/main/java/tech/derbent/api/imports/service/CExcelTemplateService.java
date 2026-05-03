package tech.derbent.api.imports.service;

import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import tech.derbent.api.utils.Check;

/**
 * Provides access to committed Excel templates shipped with the application.
 * <p>
 * WHY: templates must be stable (and editable by developers) so "Excel init" can be a real alternative
 * to code-based initializers and DB reset flows.
 * </p>
 * <p>
 * STRUCTURE: Import workbooks are split into:
 * <ul>
 * <li>system_init_types.xlsx - static reference data (Activity Type, Status, Workflow, etc.)</li>
 * <li>system_init_data.xlsx - project data items (Activity, Requirement, Decision, etc.)</li>
 * </ul>
 * WHY: Type definitions are shared across projects and should be imported once per company,
 * while data items belong to specific projects and should be imported per project.
 * </p>
 */
@Service
public class CExcelTemplateService {

    public static final String SYSTEM_INIT_TEMPLATE_RESOURCE_PATH = "excel/system_init.xlsx";
    public static final String SYSTEM_INIT_TYPES_TEMPLATE_RESOURCE_PATH = "excel/system_init_types.xlsx";
    public static final String SYSTEM_INIT_DATA_TEMPLATE_RESOURCE_PATH = "excel/system_init_data.xlsx";
    public static final String SCREENS_INIT_TEMPLATE_RESOURCE_PATH = "excel/screens_init.xlsx";

    /**
     * Opens the static types workbook (Activity Type, Status, Workflow, etc.).
     * WHY: Types are company-scoped and should be imported before data items.
     */
    public InputStream openSystemInitTypesTemplate() {
        final ClassPathResource resource = new ClassPathResource(SYSTEM_INIT_TYPES_TEMPLATE_RESOURCE_PATH);
        Check.isTrue(resource.exists(), "Missing classpath resource: " + SYSTEM_INIT_TYPES_TEMPLATE_RESOURCE_PATH);
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + SYSTEM_INIT_TYPES_TEMPLATE_RESOURCE_PATH, e);
        }
    }

    /**
     * Opens the project data items workbook (Activity, Requirement, Decision, etc.).
     * WHY: Data items are project-scoped and should be imported after types.
     */
    public InputStream openSystemInitDataTemplate() {
        final ClassPathResource resource = new ClassPathResource(SYSTEM_INIT_DATA_TEMPLATE_RESOURCE_PATH);
        Check.isTrue(resource.exists(), "Missing classpath resource: " + SYSTEM_INIT_DATA_TEMPLATE_RESOURCE_PATH);
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + SYSTEM_INIT_DATA_TEMPLATE_RESOURCE_PATH, e);
        }
    }

    /**
     * Opens the unified system init template (deprecated - use split types/data templates instead).
     * @deprecated Use {@link #openSystemInitTypesTemplate()} and {@link #openSystemInitDataTemplate()} instead
     */
    @Deprecated
    public InputStream openSystemInitTemplate(final boolean minimal) {
        final ClassPathResource resource = new ClassPathResource(SYSTEM_INIT_TEMPLATE_RESOURCE_PATH);
        Check.isTrue(resource.exists(), "Missing classpath resource: " + SYSTEM_INIT_TEMPLATE_RESOURCE_PATH);
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + SYSTEM_INIT_TEMPLATE_RESOURCE_PATH, e);
        }
    }

    /**
     * @deprecated Use {@link #openSystemInitTypesTemplate()} and {@link #openSystemInitDataTemplate()} instead
     */
    @Deprecated
    public InputStream openSystemInitTemplate() {
        return openSystemInitTemplate(false);
    }

    /** Returns the screens layout init template, or null if the file is not present (screens init is optional). */
    public InputStream openScreensInitTemplate() {
        final ClassPathResource resource = new ClassPathResource(SCREENS_INIT_TEMPLATE_RESOURCE_PATH);
        if (!resource.exists()) {
            return null;
        }
        try {
            return resource.getInputStream();
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to open " + SCREENS_INIT_TEMPLATE_RESOURCE_PATH, e);
        }
    }
}
