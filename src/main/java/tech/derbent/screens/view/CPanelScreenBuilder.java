package tech.derbent.screens.view;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H3;

import tech.derbent.abstracts.annotations.CEnhancedEntityFormBuilder;
import tech.derbent.abstracts.components.CEnhancedBinder;
import tech.derbent.abstracts.views.CVerticalLayout;
import tech.derbent.screens.domain.CScreenLines;
import tech.derbent.screens.service.CEntityFieldService;
import tech.derbent.screens.service.CEntityFieldService.EntityFieldInfo;

/**
 * CPanelScreenBuilder - Panel builder for handling CScreen sections. Layer: View (MVC)
 * 
 * This class creates panels for screen sections and processes CScreenLines within those sections using
 * CEntityFormBuilder for field binding.
 * 
 * Follows coding standards with "C" prefix.
 */
public class CPanelScreenBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CPanelScreenBuilder.class);

    private final String sectionName;
    private final List<CScreenLines> sectionLines;
    private final CEntityFieldService entityFieldService;

    public CPanelScreenBuilder(final String sectionName, final List<CScreenLines> sectionLines,
            final CEntityFieldService entityFieldService) {
        this.sectionName = sectionName;
        this.sectionLines = sectionLines;
        this.entityFieldService = entityFieldService;
    }

    /**
     * Builds a section panel with form fields based on screen lines. This panel is aware of populate requests and
     * handles data binding.
     * 
     * @param binder
     *            The enhanced binder for data binding
     * @param entityType
     *            The entity type for the fields
     * @return CVerticalLayout containing the section panel
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public <T> CVerticalLayout buildSectionPanel(final CEnhancedBinder<T> binder, final String entityType)
            throws NoSuchMethodException, SecurityException, IllegalAccessException, InvocationTargetException {

        LOGGER.info("Building section panel: {} with {} lines", sectionName, sectionLines.size());

        final CVerticalLayout panelLayout = new CVerticalLayout(false, false, false);

        // Create section header if section name exists
        if (sectionName != null && !sectionName.trim().isEmpty()) {
            final H3 sectionHeader = new H3(sectionName);
            sectionHeader.addClassName("section-header");
            panelLayout.add(sectionHeader);
        }

        // Process screen lines and create field components
        final List<EntityFieldInfo> fieldInfos = convertScreenLinesToFieldInfos(entityType);

        if (!fieldInfos.isEmpty()) {
            try {
                final Class<?> entityClass = getEntityClass(entityType);
                final CVerticalLayout fieldsLayout = CEnhancedEntityFormBuilder.buildFormFromFieldInfos(entityClass,
                        binder, fieldInfos);

                // Wrap in Details component for collapsible section
                if (sectionName != null && !sectionName.trim().isEmpty()) {
                    final Details sectionDetails = new Details(sectionName, fieldsLayout);
                    sectionDetails.setOpened(true); // Open by default
                    sectionDetails.addClassName("screen-section");
                    panelLayout.add(sectionDetails);
                } else {
                    panelLayout.add(fieldsLayout);
                }

            } catch (final Exception e) {
                LOGGER.error("Error building section panel for: {}", sectionName, e);
            }
        }

        return panelLayout;
    }

    /**
     * Handles populate requests for the panel. This method ensures the panel can respond to data population events.
     */
    public <T> void populatePanel(final T entity) {
        LOGGER.debug("Populating panel: {} with entity data", sectionName);

        // Panel populate logic will be handled by the binder automatically
        // Additional custom populate logic can be added here if needed
    }

    /**
     * Converts screen lines to EntityFieldInfo list for form building.
     */
    private List<EntityFieldInfo> convertScreenLinesToFieldInfos(final String entityType) {
        final List<EntityFieldInfo> fieldInfos = new java.util.ArrayList<>();

        for (final CScreenLines line : sectionLines) {
            if (line.getIsActive() == null || line.getIsActive()) {
                final EntityFieldInfo fieldInfo = createFieldInfoFromScreenLine(line, entityType);
                if (fieldInfo != null) {
                    fieldInfos.add(fieldInfo);
                }
            }
        }

        // Sort by order
        fieldInfos.sort((a, b) -> Integer.compare(a.getOrder(), b.getOrder()));

        return fieldInfos;
    }

    /**
     * Creates EntityFieldInfo from CScreenLines with all field properties.
     */
    private EntityFieldInfo createFieldInfoFromScreenLine(final CScreenLines line, final String entityType) {
        try {
            // Try to get field info from service first (for metadata-based info)
            EntityFieldInfo fieldInfo = entityFieldService.getEntityFieldInfo(entityType, line.getEntityProperty());

            if (fieldInfo == null) {
                // Create new field info if not found
                fieldInfo = new EntityFieldInfo();
                fieldInfo.setFieldName(line.getEntityProperty());
                fieldInfo.setDisplayName(line.getEntityProperty());
                fieldInfo.setFieldType("String"); // Default type
            }

            // Override with screen line specific settings
            if (line.getFieldCaption() != null && !line.getFieldCaption().trim().isEmpty()) {
                fieldInfo.setDisplayName(line.getFieldCaption());
            }

            if (line.getFieldDescription() != null && !line.getFieldDescription().trim().isEmpty()) {
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

            if (line.getDefaultValue() != null && !line.getDefaultValue().trim().isEmpty()) {
                fieldInfo.setDefaultValue(line.getDefaultValue());
            }

            if (line.getDataProviderBean() != null && !line.getDataProviderBean().trim().isEmpty()) {
                fieldInfo.setDataProviderBean(line.getDataProviderBean());
            }

            LOGGER.debug("Created field info for: {} with display name: {}", fieldInfo.getFieldName(),
                    fieldInfo.getDisplayName());

            return fieldInfo;

        } catch (final Exception e) {
            LOGGER.error("Error creating field info for screen line: {}", line.getEntityProperty(), e);
            return null;
        }
    }

    /**
     * Gets the entity class for the given entity type.
     */
    private Class<?> getEntityClass(final String entityType) {
        try {
            // Try different package patterns for entity classes
            final String[] packagePatterns = { "tech.derbent." + entityType.toLowerCase() + ".domain." + entityType,
                    "tech.derbent.users.domain." + entityType, // Special case for CUser
                    "tech.derbent.companies.domain." + entityType, "tech.derbent.projects.domain." + entityType,
                    "tech.derbent.activities.domain." + entityType };

            for (final String className : packagePatterns) {
                try {
                    return Class.forName(className);
                } catch (final ClassNotFoundException ignored) {
                    // Try next pattern
                }
            }

            LOGGER.warn("Could not find entity class for type: {}", entityType);
            return Object.class;

        } catch (final Exception e) {
            LOGGER.error("Error getting entity class for type: {}", entityType, e);
            return Object.class;
        }
    }

    public String getSectionName() {
        return sectionName;
    }

    public List<CScreenLines> getSectionLines() {
        return sectionLines;
    }
}