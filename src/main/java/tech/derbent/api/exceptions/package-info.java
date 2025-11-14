/**
 * Custom exception classes.
 * <p>
 * This package provides application-specific exceptions:
 * <ul>
 *   <li>{@code CException} - Base application exception</li>
 *   <li>{@code CExceptionNotify} - Exception with user notification</li>
 *   <li>{@code CInitializationException} - Initialization failures</li>
 *   <li>{@code CReflectionException} - Reflection operation failures</li>
 *   <li>{@code CImageProcessingException} - Image processing errors</li>
 * </ul>
 * <p>
 * All custom exceptions follow the C-prefix naming convention and extend
 * appropriate Java exception base classes.
 *
 * @see tech.derbent.api.ui.notifications Notification system
 */
@NullMarked
package tech.derbent.api.exceptions;

import org.jspecify.annotations.NullMarked;
