/**
 * Company management module.
 * <p>
 * This module provides multi-tenant company/organization management:
 * <ul>
 *   <li>Company registration and configuration</li>
 *   <li>Multi-tenant data isolation</li>
 *   <li>Company-wide settings</li>
 *   <li>User-company associations</li>
 * </ul>
 * <p>
 * Package structure:
 * <ul>
 *   <li>{@code domain/} - {@link tech.derbent.api.companies.domain.CCompany} entity</li>
 *   <li>{@code service/} - {@link tech.derbent.api.companies.service.CCompanyService} and repositories</li>
 * </ul>
 * <p>
 * Companies provide the top-level tenant isolation. All users, projects, and
 * project-scoped entities are associated with a specific company.
 *
 * @see tech.derbent.api.users User module
 * @see tech.derbent.api.session Session management
 */
@NullMarked
package tech.derbent.api.companies;

import org.jspecify.annotations.NullMarked;
