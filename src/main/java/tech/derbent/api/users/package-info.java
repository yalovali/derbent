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
 *   <li>{@code domain/} - {@link tech.derbent.api.users.domain.CUser} entity</li>
 *   <li>{@code service/} - {@link tech.derbent.api.users.service.CUserService} and repositories</li>
 *   <li>{@code view/} - User management UI</li>
 *   <li>{@code config/} - User-related configuration</li>
 * </ul>
 * <p>
 * Users are associated with a {@link tech.derbent.api.companies.domain.CCompany}
 * which provides multi-tenant isolation.
 *
 * @see tech.derbent.api.authentication Login module
 * @see tech.derbent.api.companies Company module
 * @see tech.derbent.api.session Session management
 */
@NullMarked
package tech.derbent.api.users;

import org.jspecify.annotations.NullMarked;
