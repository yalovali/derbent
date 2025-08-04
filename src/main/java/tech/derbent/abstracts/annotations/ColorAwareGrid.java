package tech.derbent.abstracts.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ColorAwareGrid annotation for Grid columns that should display status entities with colored backgrounds. This
 * annotation is used to mark methods in Grid view classes that should create color-aware status columns.
 * 
 * <p>
 * This annotation is typically used on methods that add status columns to grids, enabling automatic color rendering
 * based on the status entity's color property.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>{@code
 * @ColorAwareGrid(columnHeader = "Decision Status", columnKey = "status")
 * public void addStatusColumn() {
 *     grid.addStatusColumn(CDecision::getStatus, "Decision Status", "status");
 * }
 * }</pre>
 * 
 * <p>
 * <strong>Requirements:</strong>
 * </p>
 * <ul>
 * <li>The status entity must extend {@link tech.derbent.base.domain.CStatus} or
 * {@link tech.derbent.abstracts.domains.CTypeEntity}</li>
 * <li>The entity must have a getColor() method that returns a color string</li>
 * <li>Color strings should be in hex format (e.g., "#FF0000")</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.abstracts.views.CEntityGrid
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ColorAwareGrid {

    /**
     * The header text for the color-aware column.
     * 
     * @return the column header text
     */
    String columnHeader() default "Status";

    /**
     * The key identifier for the color-aware column.
     * 
     * @return the column key
     */
    String columnKey() default "status";

    /**
     * Whether to apply rounded corners to the colored cells.
     * 
     * @return true to apply rounded corners, true by default
     */
    boolean roundedCorners() default true;

    /**
     * The padding to apply to colored cells.
     * 
     * @return the padding specification, "4px 8px" by default
     */
    String padding() default "4px 8px";

    /**
     * Whether to automatically calculate contrast text color. When true, text color will be white for dark backgrounds
     * and black for light backgrounds.
     * 
     * @return true to auto-calculate contrast, true by default
     */
    boolean autoContrast() default true;

    /**
     * The minimum width for colored cells.
     * 
     * @return the minimum width specification, "80px" by default
     */
    String minWidth() default "80px";

    /**
     * Whether to center-align the text in colored cells.
     * 
     * @return true to center-align text, true by default
     */
    boolean centerAlign() default true;

    /**
     * The font weight for colored cells.
     * 
     * @return the font weight specification, "500" by default
     */
    String fontWeight() default "500";
}