package tech.derbent.abstracts.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * StatusEntity annotation to mark entity classes as status entities that have color properties. This annotation is used
 * to identify entities that represent statuses and should be rendered with color-aware components.
 * 
 * <p>
 * This annotation enables automatic detection of status entities by the form builder and grid components, allowing them
 * to apply appropriate color rendering without manual configuration.
 * </p>
 * 
 * <p>
 * <strong>Usage Example:</strong>
 * </p>
 * 
 * <pre>
 * {
 *     &#64;code
 *     &#64;StatusEntity(colorField = "color", nameField = "name")
 *     @Entity
 *     public class CDecisionStatus extends CStatus {
 *         // Entity implementation
 *     }
 * }
 * </pre>
 * 
 * <p>
 * <strong>Requirements:</strong>
 * </p>
 * <ul>
 * <li>The entity must extend {@link tech.derbent.base.domain.CStatus} or
 * {@link tech.derbent.abstracts.domains.CTypeEntity}</li>
 * <li>The entity must have a getColor() method that returns a color string</li>
 * <li>Color strings should be in hex format (e.g., "#FF0000")</li>
 * </ul>
 * 
 * @author Derbent Framework
 * @since 1.0
 * @see tech.derbent.base.domain.CStatus
 * @see tech.derbent.abstracts.domains.CTypeEntity
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface StatusEntity {

    /**
     * The name of the field that contains the color value.
     * 
     * @return the color field name, "color" by default
     */
    String colorField() default "color";

    /**
     * The name of the field that contains the display name.
     * 
     * @return the name field name, "name" by default
     */
    String nameField() default "name";

    /**
     * The category or type of status this entity represents. This can be used for grouping or filtering status
     * entities.
     * 
     * @return the status category, empty string by default
     */
    String category() default "";

    /**
     * Whether this status entity represents final states. Final states typically represent completed or terminated
     * statuses.
     * 
     * @return true if this represents final states, false by default
     */
    boolean finalStatus() default false;

    /**
     * The default color to use if no color is specified.
     * 
     * @return the default color in hex format, "#95a5a6" (gray) by default
     */
    String defaultColor() default "#95a5a6";
}