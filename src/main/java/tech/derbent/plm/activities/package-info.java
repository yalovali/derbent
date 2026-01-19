/**
 * Activity management module.
 * <p>
 * This module provides comprehensive activity/task management functionality including:
 * <ul>
 *   <li>Activity creation and tracking</li>
 *   <li>Status workflow management</li>
 *   <li>Activity types and priorities</li>
 *   <li>Project hierarchy support</li>
 *   <li>Time tracking</li>
 * </ul>
 * <p>
 * Package structure:
 * <ul>
 *   <li>{@code domain/} - {@link tech.derbent.plm.activities.domain.CActivity} and related entities</li>
 *   <li>{@code service/} - {@link tech.derbent.plm.activities.service.CActivityService} and repositories</li>
 * </ul>
 * <p>
 * Activities extend {@link tech.derbent.api.entityOfProject.domain.CProjectItem} and support
 * hierarchical project structures with parent-child relationships.
 *
 * @see tech.derbent.api.projects Project module
 * @see tech.derbent.api.entityOfProject.domain.CProjectItem Base class
 */
@NullMarked
package tech.derbent.plm.activities;

import org.jspecify.annotations.NullMarked;
