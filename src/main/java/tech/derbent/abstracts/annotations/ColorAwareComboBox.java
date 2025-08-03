package tech.derbent.abstracts.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ColorAwareComboBox annotation for ComboBox fields that should display items with colored backgrounds.
 * This annotation is used to mark entity fields that represent status entities and should be rendered
 * with color-aware ComboBox components.
 * 
 * <p>
 * This annotation works in conjunction with the {@link MetaData} annotation to provide additional
 * color rendering capabilities for ComboBox components.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * @MetaData(displayName = "Status", required = true, order = 1)
 * @ColorAwareComboBox
 * private CDecisionStatus status;
 * }</pre>
 * 
 * <p>
 * <strong>Requirements:</strong>
 * </p>
 * <ul>
 * <li>The field type must extend {@link tech.derbent.base.domain.CStatus} or {@link tech.derbent.abstracts.domains.CTypeEntity}</li>
 * <li>The entity must have a getColor() method that returns a color string</li>
 * <li>Color strings should be in hex format (e.g., "#FF0000")</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.annotations.MetaData
 * @see tech.derbent.abstracts.components.CColorAwareComboBox
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColorAwareComboBox {
    
    /**
     * Whether to apply rounded corners to the colored items.
     * 
     * @return true to apply rounded corners, true by default
     */
    boolean roundedCorners() default true;
    
    /**
     * The padding to apply to colored items.
     * 
     * @return the padding specification, "4px 8px" by default
     */
    String padding() default "4px 8px";
    
    /**
     * Whether to automatically calculate contrast text color.
     * When true, text color will be white for dark backgrounds and black for light backgrounds.
     * 
     * @return true to auto-calculate contrast, true by default
     */
    boolean autoContrast() default true;
    
    /**
     * The minimum width for colored items.
     * 
     * @return the minimum width specification, "100%" by default
     */
    String minWidth() default "100%";
}