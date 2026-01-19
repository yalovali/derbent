/**
 * Business modules package containing domain-specific functionality.
 * <p>
 * Each subpackage represents a business module following the standard structure:
 * <ul>
 *   <li>{@code domain/} - Domain entities and value objects</li>
 *   <li>{@code service/} - Business logic and repositories</li>
 *   <li>{@code view/} - UI components and pages (when applicable)</li>
 * </ul>
 * <p>
 * Available modules:
 * <ul>
 *   <li>{@link tech.derbent.plm.activities} - Activity and task management</li>
 *   <li>{@link tech.derbent.api.projects} - Project management</li>
 *   <li>{@link tech.derbent.plm.meetings} - Meeting scheduling and tracking</li>
 *   <li>{@link tech.derbent.api.companies} - Company/organization management</li>
 *   <li>{@link tech.derbent.plm.comments} - Comment system</li>
 *   <li>{@link tech.derbent.plm.decisions} - Decision tracking</li>
 *   <li>{@link tech.derbent.plm.risks} - Risk management</li>
 *   <li>{@link tech.derbent.plm.orders} - Order management</li>
 *   <li>{@link tech.derbent.api.roles} - Role-based access control</li>
 *   <li>{@link tech.derbent.api.workflow} - Workflow management</li>
 *   <li>{@link tech.derbent.plm.gannt} - Gantt chart visualization</li>
 *   <li>{@link tech.derbent.api.page} - Dynamic page system</li>
 * </ul>
 * <p>
 * All modules follow the C-prefix naming convention and extend base classes
 * from {@link tech.derbent.api}.
 *
 * @see tech.derbent.api Core framework
 * @see tech.derbent.base Infrastructure modules
 */
@NullMarked
package tech.derbent.plm;

import org.jspecify.annotations.NullMarked;
