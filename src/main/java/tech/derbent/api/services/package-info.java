/**
 * Base service classes and repository interfaces.
 * <p>
 * This package provides the service layer foundation:
 * <ul>
 *   <li>{@link tech.derbent.api.services.CAbstractService} - Base service implementation</li>
 *   <li>Repository interfaces with standard CRUD operations</li>
 *   <li>Entity-specific service patterns</li>
 *   <li>Page service system for dynamic UI generation</li>
 * </ul>
 * <p>
 * All business module services extend classes from this package and follow
 * consistent patterns for:
 * <ul>
 *   <li>Transaction management</li>
 *   <li>Validation</li>
 *   <li>Dependency checking</li>
 *   <li>Multi-tenant data isolation</li>
 * </ul>
 * <p>
 * The {@link tech.derbent.api.services.pageservice} subpackage provides
 * metadata-driven dynamic page generation.
 *
 * @see tech.derbent.api.domains Base entity classes
 * @see tech.derbent.api.views Base view classes
 */
@NullMarked
package tech.derbent.api.services;

import org.jspecify.annotations.NullMarked;
