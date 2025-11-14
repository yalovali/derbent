/**
 * Custom annotations for metadata-driven development.
 * <p>
 * This package provides annotations that drive automatic UI generation
 * and configuration:
 * <ul>
 *   <li>{@code @AMetaData} - UI field metadata (display name, order, validation)</li>
 *   <li>Spring auxiliary annotations for component configuration</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>{@code
 * @Column(nullable = false, length = 255)
 * @NotBlank(message = "Name is required")
 * @AMetaData(
 *     displayName = "Activity Name",
 *     required = true,
 *     order = 10,
 *     maxLength = 255
 * )
 * private String name;
 * }</pre>
 *
 * @see tech.derbent.api.services.pageservice Page service system
 */
@NullMarked
package tech.derbent.api.annotations;

import org.jspecify.annotations.NullMarked;
