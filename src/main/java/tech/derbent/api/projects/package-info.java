/**
 * Project management module.
 * <p>
 * This module provides core project management functionality:
 * <ul>
 *   <li>Project creation and configuration</li>
 *   <li>Project lifecycle management</li>
 *   <li>Multi-tenant project isolation</li>
 *   <li>Project hierarchies and relationships</li>
 *   <li>Project-level permissions</li>
 * </ul>
 * <p>
 * Package structure:
 * <ul>
 *   <li>{@code domain/} - {@link tech.derbent.api.projects.domain.CProject} and related entities</li>
 *   <li>{@code service/} - {@link tech.derbent.api.projects.service.CProjectService},
 *   {@link tech.derbent.api.projects.service.CProject_DerbentService},
 *   {@link tech.derbent.api.projects.service.CProject_BabService} and repositories</li>
 *   <li>{@code view/} - Project UI components</li>
 *   <li>{@code events/} - Project-related events</li>
 * </ul>
 * <p>
 * Projects serve as the primary organizational unit and provide context for
 * activities, meetings, risks, and other project-scoped entities.
 *
 * @see tech.derbent.plm.activities Activity module
 * @see tech.derbent.api.entityOfProject.domain.CEntityOfProject Base class
 */
@NullMarked
package tech.derbent.api.projects;

import org.jspecify.annotations.NullMarked;
