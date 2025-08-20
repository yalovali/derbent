package tech.derbent.abstracts.annotations;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;

import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.abstracts.views.CHorizontalLayout;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/**
 * Enhanced CEntityFormBuilder that uses CEntityFieldService and EntityFieldInfo as an alternative to metadata
 * annotations. This class extends the capabilities of the original CEntityFormBuilder to work with dynamic field
 * definitions.
 * 
 * Layer: Component (MVC) Follows coding standards with "C" prefix.
 */
public class CEnhancedEntityFormBuilder<EntityClass> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CEnhancedEntityFormBuilder.class);

    private final Map<String, Component> componentMap;
    private final Map<String, CHorizontalLayout> horizontalLayoutMap;
    private final CVerticalLayout formLayout;

    public CEnhancedEntityFormBuilder(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
            final List<EntityFieldInfo> fieldInfos)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {
        this.componentMap = new java.util.HashMap<>();
        this.horizontalLayoutMap = new java.util.HashMap<>();
        this.formLayout = new CVerticalLayout(false, false, false);

        buildFormFromFieldInfosInternal(entityClass, binder, fieldInfos);
    }

    /**
     * Builds a form layout using EntityFieldInfo list instead of metadata annotations. This method provides an
     * alternative to the traditional metadata-based approach.
     */
    private void buildFormFromFieldInfosInternal(final Class<?> entityClass, final CEnhancedBinder<EntityClass> binder,
            final List<EntityFieldInfo> fieldInfos)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        LOGGER.info("Building form from {} field infos for entity class: {}", fieldInfos.size(),
                entityClass.getSimpleName());

        final FormLayout formLayoutComponent = new FormLayout();

        for (final EntityFieldInfo fieldInfo : fieldInfos) {
            if (fieldInfo.isHidden()) {
                LOGGER.debug("Skipping hidden field: {}", fieldInfo.getFieldName());
                continue;
            }

            try {
                final Component component = createComponentFromFieldInfo(entityClass, binder, fieldInfo);
                if (component != null) {
                    formLayoutComponent.add(component);
                    componentMap.put(fieldInfo.getFieldName(), component);
                    LOGGER.debug("Added component for field: {}", fieldInfo.getFieldName());
                }
            } catch (final Exception e) {
                LOGGER.error("Error creating component for field: {}", fieldInfo.getFieldName(), e);
            }
        }

        formLayout.add(formLayoutComponent);
    }

    /**
     * Creates a Vaadin component based on EntityFieldInfo. This method replaces the metadata annotation processing with
     * dynamic field info processing.
     */
    private Component createComponentFromFieldInfo(final Class<?> entityClass,
            final CEnhancedBinder<EntityClass> binder, final EntityFieldInfo fieldInfo)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        // For now, use the original CEntityFormBuilder to create components
        // This provides fallback compatibility while we implement the enhanced version
        final List<String> singleFieldList = List.of(fieldInfo.getFieldName());

        try {
            final CVerticalLayout componentLayout = CEntityFormBuilder.buildForm(entityClass, binder, singleFieldList);

            if (componentLayout.getComponentCount() > 0) {
                return componentLayout.getComponentAt(0);
            }
        } catch (final Exception e) {
            LOGGER.warn("Failed to create component for field {} using original builder, error: {}",
                    fieldInfo.getFieldName(), e.getMessage());
        }

        return null;
    }

    /**
     * Static factory method to create enhanced form using EntityFieldInfo list.
     */
    public static <EntityClass> CVerticalLayout buildFormFromFieldInfos(final Class<?> entityClass,
            final CEnhancedBinder<EntityClass> binder, final List<EntityFieldInfo> fieldInfos)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        final CEnhancedEntityFormBuilder<EntityClass> builder = new CEnhancedEntityFormBuilder<>(entityClass, binder,
                fieldInfos);
        return builder.getFormLayout();
    }

    public Map<String, Component> getComponentMap() {
        return componentMap;
    }

    public CVerticalLayout getFormLayout() {
        return formLayout;
    }

    public Map<String, CHorizontalLayout> getHorizontalLayoutMap() {
        return horizontalLayoutMap;
    }
}