package tech.derbent.api.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import tech.derbent.api.domains.CEntityConstants;

/** Utility class for safely creating and validating Pageable objects. Prevents the "max-results cannot be negative" Spring Data exception.
 * @author Derbent Framework
 * @since 1.0 */
public final class CPageableUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageableUtils.class);

	/** Creates a default Pageable object for data provider usage.
	 * @return a safe Pageable object with default values */
	public static Pageable createDefault() {
		return createSafe(0, CEntityConstants.MAX_PAGE_SIZE);
	}

	/** Creates a safe Pageable object with validation to prevent negative values.
	 * @param page the page number (0-based)
	 * @param size the page size
	 * @return a valid Pageable object
	 * @throws IllegalArgumentException if page or size is negative */
	public static Pageable createSafe(final int page, final int size) {
		return createSafe(page, size, Sort.unsorted());
	}

	/** Creates a safe Pageable object with validation and sorting.
	 * @param page the page number (0-based)
	 * @param size the page size
	 * @param sort the sort specification
	 * @return a valid Pageable object
	 * @throws IllegalArgumentException if page or size is negative */
	public static Pageable createSafe(final int page, final int size, final Sort sort) {
		if (page < 0) {
			LOGGER.error("Negative page number detected: {}", page);
			throw new IllegalArgumentException("Page number cannot be negative: " + page);
		}
		int validatedSize = size;
		if (validatedSize < 0) {
			LOGGER.error("Negative page size detected: {}", validatedSize);
			throw new IllegalArgumentException("Page size cannot be negative: " + validatedSize);
		}
		if (validatedSize == 0) {
			LOGGER.warn("Page size is 0, using default size: {}", CEntityConstants.DEFAULT_PAGE_SIZE);
			validatedSize = CEntityConstants.DEFAULT_PAGE_SIZE;
		}
		if (validatedSize > CEntityConstants.MAX_PAGE_SIZE) {
			LOGGER.warn("Page size {} exceeds maximum {}, using maximum", validatedSize, CEntityConstants.MAX_PAGE_SIZE);
			validatedSize = CEntityConstants.MAX_PAGE_SIZE;
		}
		return PageRequest.of(page, validatedSize, sort);
	}

	/** Validates an existing Pageable object to ensure it doesn't have negative values.
	 * @param pageable the Pageable to validate
	 * @return the validated Pageable or a safe default if invalid */
	public static Pageable validateAndFix(final Pageable pageable) {
		if (pageable == null) {
			LOGGER.warn("Pageable is null, returning unpaged");
			return Pageable.unpaged();
		}
		if (!pageable.isPaged()) {
			return pageable; // Unpaged is always safe
		}
		int page = pageable.getPageNumber();
		int size = pageable.getPageSize();
		final Sort sort = pageable.getSort();
		boolean needsFix = false;
		if (page < 0) {
			LOGGER.warn("Fixing negative page number {} to 0", page);
			page = 0;
			needsFix = true;
		}
		if (size < 0) {
			LOGGER.warn("Fixing negative page size {} to default {}", size, CEntityConstants.DEFAULT_PAGE_SIZE);
			size = CEntityConstants.DEFAULT_PAGE_SIZE;
			needsFix = true;
		}
		if (size == 0) {
			LOGGER.warn("Fixing zero page size to default {}", CEntityConstants.DEFAULT_PAGE_SIZE);
			size = CEntityConstants.DEFAULT_PAGE_SIZE;
			needsFix = true;
		}
		return needsFix ? PageRequest.of(page, size, sort) : pageable;
	}

	private CPageableUtils() {
		// Utility class - prevent instantiation
	}
}
