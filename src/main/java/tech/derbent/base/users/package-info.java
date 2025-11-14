/**
 * User management module.
 * <p>
 * This module handles user accounts and authentication:
 * <ul>
 *   <li>User registration and profiles</li>
 *   <li>User authentication</li>
 *   <li>Company-based multi-tenancy</li>
 *   <li>User preferences and settings</li>
 *   <li>Profile pictures</li>
 * </ul>
 * <p>
 * Package structure:
 * <ul>
 *   <li>{@code domain/} - {@link tech.derbent.base.users.domain.CUser} entity</li>
 *   <li>{@code service/} - {@link tech.derbent.base.users.service.CUserService} and repositories</li>
 *   <li>{@code view/} - User management UI</li>
 *   <li>{@code config/} - User-related configuration</li>
 * </ul>
 * <p>
 * Users are associated with a {@link tech.derbent.app.companies.domain.CCompany}
 * which provides multi-tenant isolation.
 *
 * @see tech.derbent.base.login Login module
 * @see tech.derbent.app.companies Company module
 * @see tech.derbent.base.session Session management
 */
@NullMarked
package tech.derbent.base.users;

import org.jspecify.annotations.NullMarked;
