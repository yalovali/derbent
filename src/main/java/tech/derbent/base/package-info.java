/**
 * Base infrastructure package containing core system functionality.
 * <p>
 * This package provides essential infrastructure services:
 * <ul>
 *   <li>{@link tech.derbent.base.login} - Authentication and login</li>
 *   <li>{@link tech.derbent.base.session} - Session management</li>
 *   <li>{@link tech.derbent.base.users} - User management</li>
 *   <li>{@link tech.derbent.base.setup} - System setup and configuration</li>
 * </ul>
 * <p>
 * These modules are required for the application to function and are
 * loaded before business modules in {@link tech.derbent.app}.
 *
 * @see tech.derbent.api Core framework
 * @see tech.derbent.app Business modules
 */
@NullMarked
package tech.derbent.base;

import org.jspecify.annotations.NullMarked;
