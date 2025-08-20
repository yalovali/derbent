package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.vaadin.flow.component.formlayout.FormLayout;

import tech.derbent.abstracts.annotations.CEnhancedEntityFormBuilder;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.screens.domain.CScreen;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/**
 * CScreenBuilder - Builder class for creating dynamic forms based on CScreen definitions. Layer: View (MVC)
 * 
 * This class reads CScreen and CScreenLines to build dynamic forms with proper field binding and validation. Supports
 * sections and nested panels.
 * 
 * Follows coding standards with "C" prefix.
 */
@Component
public class CScreenBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CScreenBuilder.class);

    private final CEntityFieldService entityFieldService;

    public CScreenBuilder(final CEntityFieldService entityFieldService) {
        this.entityFieldService = entityFieldService;
    }

    /**
     * Builds a detailed form layout based on CScreen definition.
     * 
     * @param screen
     *            The CScreen definition to build form from
     * @param binder
     *            The enhanced binder for data binding
     * @return FormLayout containing all components
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public <T> FormLayout detailsFormBuilder(final CScreen screen, final CEnhancedBinder<T> binder)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        LOGGER.info("Building details form for screen: {} with entity type: {}", screen.getScreenTitle(),
                screen.getEntityType());

        final FormLayout mainFormLayout = new FormLayout();

        if (screen.getScreenLines() == null || screen.getScreenLines().isEmpty()) {
            LOGGER.warn("No screen lines found for screen: {}", screen.getScreenTitle());
            return mainFormLayout;
        }

        // Sort screen lines by order
        final List<CScreenLines> sortedLines = new ArrayList<>(screen.getScreenLines());
        sortedLines.sort((a, b) -> Integer.compare(a.getLineOrder() != null ? a.getLineOrder() : 999,
                b.getLineOrder() != null ? b.getLineOrder() : 999));

        processScreenLines(sortedLines, mainFormLayout, binder, screen.getEntityType());

        return mainFormLayout;
    }

    /**
     * Processes screen lines and creates appropriate components or sections.
     */
    private <T> void processScreenLines(final List<CScreenLines> screenLines, final FormLayout parentLayout,
            final CEnhancedBinder<T> binder, final String entityType)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        final List<CScreenLines> currentSectionLines = new ArrayList<>();
        String currentSectionName = null;

        for (final CScreenLines line : screenLines) {
            if (isSection(line)) {
                // Process previous section if exists
                if (!currentSectionLines.isEmpty()) {
                    processSection(currentSectionName, currentSectionLines, parentLayout, binder, entityType);
                    currentSectionLines.clear();
                }

                // Start new section
                currentSectionName = line.getSectionName();
                LOGGER.debug("Starting new section: {}", currentSectionName);

            } else {
                // Add line to current section
                currentSectionLines.add(line);
            }
        }

        // Process the final section
        if (!currentSectionLines.isEmpty()) {
            processSection(currentSectionName, currentSectionLines, parentLayout, binder, entityType);
        }
    }

    /**
     * Processes a section by creating a CPanelScreenBuilder and adding it to the parent layout.
     */
    private <T> void processSection(final String sectionName, final List<CScreenLines> sectionLines,
            final FormLayout parentLayout, final CEnhancedBinder<T> binder, final String entityType)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        LOGGER.debug("Processing section '{}' with {} lines", sectionName, sectionLines.size());

        if (sectionName != null) {
            // Create panel for section
            final CPanelScreenBuilder panelBuilder = new CPanelScreenBuilder(sectionName, sectionLines,
                    entityFieldService);

            final CVerticalLayout sectionLayout = panelBuilder.buildSectionPanel(binder, entityType);
            parentLayout.add(sectionLayout);

        } else {
            // Process lines without section (add directly to main form)
            final List<EntityFieldInfo> fieldInfos = convertScreenLinesToFieldInfos(sectionLines, entityType);

            if (!fieldInfos.isEmpty()) {
                final CVerticalLayout fieldsLayout = CEnhancedEntityFormBuilder
                        .buildFormFromFieldInfos(getEntityClass(entityType), binder, fieldInfos);
                parentLayout.add(fieldsLayout);
            }
        }
    }

    /**
     * Converts CScreenLines to EntityFieldInfo list.
     */
    private List<EntityFieldInfo> convertScreenLinesToFieldInfos(final List<CScreenLines> screenLines,
            final String entityType) {

        final List<EntityFieldInfo> fieldInfos = new ArrayList<>();

        for (final CScreenLines line : screenLines) {
            if (line.getIsActive() == null || line.getIsActive()) {
                final EntityFieldInfo fieldInfo = createFieldInfoFromScreenLine(line, entityType);
                if (fieldInfo != null) {
                    fieldInfos.add(fieldInfo);
                }
            }
        }

        return fieldInfos;
    }

    /**
     * Creates EntityFieldInfo from CScreenLines.
     */
    private EntityFieldInfo createFieldInfoFromScreenLine(final CScreenLines line, final String entityType) {
        try {
            // Try to get field info from service first
            EntityFieldInfo fieldInfo = entityFieldService.getEntityFieldInfo(entityType, line.getEntityProperty());

            if (fieldInfo == null) {
                // Create new field info if not found
                fieldInfo = new EntityFieldInfo();
                fieldInfo.setFieldName(line.getEntityProperty());
                fieldInfo.setDisplayName(
                        line.getFieldCaption() != null ? line.getFieldCaption() : line.getEntityProperty());
            }

            // Override with screen line specific settings
            if (line.getFieldCaption() != null) {
                fieldInfo.setDisplayName(line.getFieldCaption());
            }

            if (line.getFieldDescription() != null) {
                fieldInfo.setDescription(line.getFieldDescription());
            }

            if (line.getIsRequired() != null) {
                fieldInfo.setRequired(line.getIsRequired());
            }

            if (line.getIsReadonly() != null) {
                fieldInfo.setReadOnly(line.getIsReadonly());
            }

            if (line.getIsHidden() != null) {
                fieldInfo.setHidden(line.getIsHidden());
            }

            if (line.getLineOrder() != null) {
                fieldInfo.setOrder(line.getLineOrder());
            }

            if (line.getMaxLength() != null) {
                fieldInfo.setMaxLength(line.getMaxLength());
            }

            if (line.getDefaultValue() != null) {
                fieldInfo.setDefaultValue(line.getDefaultValue());
            }

            if (line.getDataProviderBean() != null) {
                fieldInfo.setDataProviderBean(line.getDataProviderBean());
            }

            return fieldInfo;

        } catch (final Exception e) {
            LOGGER.error("Error creating field info for screen line: {}", line.getEntityProperty(), e);
            return null;
        }
    }

    /**
     * Determines if a screen line represents a section.
     */
    private boolean isSection(final CScreenLines line) {
        return CEntityFieldService.SECTION.equals(line.getFieldClass())
                || (line.getSectionName() != null && !line.getSectionName().trim().isEmpty());
    }

    /**
     * Gets the entity class for the given entity type.
     */
    private Class<?> getEntityClass(final String entityType) {
        try {
            return Class.forName("tech.derbent." + entityType.toLowerCase() + ".domain." + entityType);
        } catch (final ClassNotFoundException e) {
            LOGGER.error("Could not find entity class for type: {}", entityType, e);
            return Object.class;
        }
    }
}