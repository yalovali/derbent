/**
 * UI framework components and utilities.
 * <p>
 * This package contains the Vaadin-based UI framework:
 * <ul>
 *   <li>{@link tech.derbent.api.ui.component} - Reusable UI components</li>
 *   <li>{@link tech.derbent.api.ui.view} - Base view/page classes</li>
 *   <li>{@link tech.derbent.api.ui.dialogs} - Dialog components</li>
 *   <li>{@link tech.derbent.api.ui.notifications} - Notification system</li>
 *   <li>{@link tech.derbent.api.ui.config} - UI configuration</li>
 * </ul>
 * <p>
 * Key features:
 * <ul>
 *   <li>Enhanced data binding with {@code CEnhancedBinder}</li>
 *   <li>Unified notification system via {@code CNotificationService}</li>
 *   <li>Reusable dialog components</li>
 *   <li>Base page classes for consistent UI patterns</li>
 * </ul>
 * <p>
 * <strong>IMPORTANT:</strong> Always use {@code CNotificationService} for
 * user notifications, never direct {@code Notification.show()} calls.
 *
 * @see tech.derbent.api.ui.notifications.CNotificationService
 * @see tech.derbent.api.ui.component
 * @see tech.derbent.api.ui.view
 */
@NullMarked
package tech.derbent.api.ui;

import org.jspecify.annotations.NullMarked;
