package tech.derbent.abstracts.utils;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Check {

	/**
	 * Default error message for condition checks
	 */
	private static final String DEFAULT_CONDITION_MESSAGE = "Condition check failed";

	/**
	 * Default error message for null checks
	 */
	private static final String DEFAULT_NULL_MESSAGE = "Object cannot be null";

	/**
	 * Default error message for empty checks
	 */
	private static final String DEFAULT_EMPTY_MESSAGE = "Collection cannot be empty";

	/**
	 * Default error message for blank string checks
	 */
	private static final String DEFAULT_BLANK_MESSAGE = "String cannot be null or blank";

	private static final Logger LOGGER = LoggerFactory.getLogger(Check.class);

	/**
	 * Checks that a number is positive (> 0). Uses default error message.
	 * @param number the number to check
	 * @throws IllegalArgumentException if number is not positive
	 */
	public static void checkPositive(final Number number) {
		checkPositive(number, "Number must be positive");
	}

	/**
	 * Checks that a number is positive (> 0).
	 * @param number  the number to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if number is not positive
	 */
	public static void checkPositive(final Number number, final String message) {
		notNull(number, message);

		if (number.doubleValue() <= 0) {
			throw new IllegalArgumentException(
				message != null ? message : "Number must be positive");
		}
	}

	/**
	 * Checks that a number is within a specified range (inclusive). Uses default error
	 * message.
	 * @param number the number to check
	 * @param min    minimum value (inclusive)
	 * @param max    maximum value (inclusive)
	 * @throws IllegalArgumentException if number is outside the range
	 */
	public static void checkRange(final Number number, final Number min,
		final Number max) {
		checkRange(number, min, max, null);
	}

	/**
	 * Checks that a number is within a specified range (inclusive).
	 * @param number  the number to check
	 * @param min     minimum value (inclusive)
	 * @param max     maximum value (inclusive)
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if number is outside the range
	 */
	public static void checkRange(final Number number, final Number min, final Number max,
		final String message) {
		notNull(number, message);
		notNull(min, message);
		notNull(max, message);
		final double value = number.doubleValue();
		final double minValue = min.doubleValue();
		final double maxValue = max.doubleValue();

		if ((value < minValue) || (value > maxValue)) {
			final String defaultMessage =
				String.format("Number must be between %s and %s (inclusive)", min, max);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks a boolean condition and throws IllegalArgumentException if false. Uses
	 * default error message.
	 * @param condition the condition to check
	 * @throws IllegalArgumentException if condition is false
	 */
	public static void condition(final boolean condition) {
		condition(condition, DEFAULT_CONDITION_MESSAGE);
	}

	/**
	 * Checks a boolean condition and throws IllegalArgumentException if false.
	 * @param condition the condition to check
	 * @param message   custom error message (optional)
	 * @throws IllegalArgumentException if condition is false
	 */
	public static void condition(final boolean condition, final String message) {

		if (!condition) {
			throw new IllegalArgumentException(
				message != null ? message : DEFAULT_CONDITION_MESSAGE);
		}
	}

	/**
	 * Checks that a collection contains a specific element. Uses default error message.
	 * @param collection the collection to check
	 * @param element    the element to look for
	 * @throws IllegalArgumentException if collection doesn't contain the element
	 */
	public static void contains(final Collection<?> collection, final Object element) {
		contains(collection, element, null);
	}

	/**
	 * Checks that a collection contains a specific element.
	 * @param collection the collection to check
	 * @param element    the element to look for
	 * @param message    custom error message (optional)
	 * @throws IllegalArgumentException if collection doesn't contain the element
	 */
	public static void contains(final Collection<?> collection, final Object element,
		final String message) {
		notNull(collection, message);

		if (!collection.contains(element)) {
			final String defaultMessage =
				String.format("Collection does not contain element: %s", element);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a string contains a specific substring. Uses default error message.
	 * @param string    the string to check
	 * @param substring the substring to look for
	 * @throws IllegalArgumentException if string doesn't contain the substring
	 */
	public static void contains(final String string, final String substring) {
		contains(string, substring, null);
	}

	/**
	 * Checks that a string contains a specific substring.
	 * @param string    the string to check
	 * @param substring the substring to look for
	 * @param message   custom error message (optional)
	 * @throws IllegalArgumentException if string doesn't contain the substring
	 */
	public static void contains(final String string, final String substring,
		final String message) {
		notNull(string, message);
		notNull(substring, message);

		if (!string.contains(substring)) {
			final String defaultMessage =
				String.format("String '%s' does not contain '%s'", string, substring);
			LOGGER.error("String contains check failed: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that two objects are equal using equals() method. Uses default error
	 * message.
	 * @param obj1 first object to compare
	 * @param obj2 second object to compare
	 * @throws IllegalArgumentException if objects are not equal
	 */
	public static void equals(final Object obj1, final Object obj2) {
		equals(obj1, obj2, null);
	}

	/**
	 * Checks that two objects are equal using equals() method.
	 * @param obj1    first object to compare
	 * @param obj2    second object to compare
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if objects are not equal
	 */
	public static void equals(final Object obj1, final Object obj2,
		final String message) {

		if ((obj1 == null) && (obj2 == null)) {
			return; // Both null is considered equal
		}

		if ((obj1 == null) || (obj2 == null) || !obj1.equals(obj2)) {
			final String defaultMessage =
				String.format("Objects are not equal: %s != %s", obj1, obj2);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that an index is valid for a collection. Uses default error message.
	 * @param index the index to check
	 * @param size  the size of the collection
	 * @throws IllegalArgumentException if index is invalid
	 */
	public static void index(final int index, final int size) {
		index(index, size, null);
	}

	/**
	 * Checks that an index is valid for a collection.
	 * @param index   the index to check
	 * @param size    the size of the collection
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if index is invalid
	 */
	public static void index(final int index, final int size, final String message) {

		if ((index < 0) || (index >= size)) {
			final String defaultMessage =
				String.format("Index %d is out of bounds for size %d", index, size);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that an object is an instance of a specific class. Uses default error
	 * message.
	 * @param object        the object to check
	 * @param expectedClass the expected class type
	 * @throws IllegalArgumentException if object is not an instance of expectedClass
	 */
	public static void instanceOf(final Object object, final Class<?> expectedClass) {
		instanceOf(object, expectedClass, null);
	}

	/**
	 * Checks that an object is an instance of a specific class.
	 * @param object        the object to check
	 * @param expectedClass the expected class type
	 * @param message       custom error message (optional)
	 * @throws IllegalArgumentException if object is not an instance of expectedClass
	 */
	public static void instanceOf(final Object object, final Class<?> expectedClass,
		final String message) {
		notNull(object, message);
		notNull(expectedClass, message);

		if (!expectedClass.isInstance(object)) {
			final String defaultMessage =
				String.format("Object of type %s is not an instance of %s",
					object.getClass().getSimpleName(), expectedClass.getSimpleName());
			LOGGER.error("Instance check failed: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a string length is within a specified range. Uses default error
	 * message.
	 * @param string    the string to check
	 * @param minLength minimum required length (inclusive)
	 * @param maxLength maximum allowed length (inclusive)
	 * @throws IllegalArgumentException if string length is outside the range
	 */
	public static void lengthRange(final String string, final int minLength,
		final int maxLength) {
		lengthRange(string, minLength, maxLength, null);
	}

	/**
	 * Checks that a string length is within a specified range.
	 * @param string    the string to check
	 * @param minLength minimum required length (inclusive)
	 * @param maxLength maximum allowed length (inclusive)
	 * @param message   custom error message (optional)
	 * @throws IllegalArgumentException if string length is outside the range
	 */
	public static void lengthRange(final String string, final int minLength,
		final int maxLength, final String message) {
		notNull(string, message);
		nonNegative(minLength, message);
		nonNegative(maxLength, message);
		condition(minLength <= maxLength,
			"Minimum length cannot be greater than maximum length");
		final int length = string.length();

		if ((length < minLength) || (length > maxLength)) {
			final String defaultMessage =
				String.format("String length %d is not between %d and %d (inclusive)",
					length, minLength, maxLength);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a string matches a regular expression pattern. Uses default error
	 * message.
	 * @param string the string to check
	 * @param regex  the regular expression pattern
	 * @throws IllegalArgumentException if string doesn't match the pattern
	 */
	public static void matches(final String string, final String regex) {
		matches(string, regex, null);
	}

	/**
	 * Checks that a string matches a regular expression pattern.
	 * @param string  the string to check
	 * @param regex   the regular expression pattern
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if string doesn't match the pattern
	 */
	public static void matches(final String string, final String regex,
		final String message) {
		notNull(string, message);
		notBlank(regex, message);

		if (!string.matches(regex)) {
			final String defaultMessage =
				String.format("String '%s' does not match pattern '%s'", string, regex);
			LOGGER.error("String match check failed: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a string has a maximum length. Uses default error message.
	 * @param string    the string to check
	 * @param maxLength maximum allowed length
	 * @throws IllegalArgumentException if string is longer than maxLength
	 */
	public static void maxLength(final String string, final int maxLength) {
		maxLength(string, maxLength, null);
	}

	/**
	 * Checks that a string has a maximum length.
	 * @param string    the string to check
	 * @param maxLength maximum allowed length
	 * @param message   custom error message (optional)
	 * @throws IllegalArgumentException if string is longer than maxLength
	 */
	public static void maxLength(final String string, final int maxLength,
		final String message) {
		notNull(string, message);
		nonNegative(maxLength, message);

		if (string.length() > maxLength) {
			final String defaultMessage =
				String.format("String length %d exceeds maximum allowed %d",
					string.length(), maxLength);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a collection has a maximum size. Uses default error message.
	 * @param collection the collection to check
	 * @param maxSize    maximum allowed size
	 * @throws IllegalArgumentException if collection size exceeds maxSize
	 */
	public static void maxSize(final Collection<?> collection, final int maxSize) {
		maxSize(collection, maxSize, null);
	}

	/**
	 * Checks that a collection has a maximum size.
	 * @param collection the collection to check
	 * @param maxSize    maximum allowed size
	 * @param message    custom error message (optional)
	 * @throws IllegalArgumentException if collection size exceeds maxSize
	 */
	public static void maxSize(final Collection<?> collection, final int maxSize,
		final String message) {
		notNull(collection, message);
		nonNegative(maxSize, message);

		if (collection.size() > maxSize) {
			final String defaultMessage =
				String.format("Collection size %d exceeds maximum allowed %d",
					collection.size(), maxSize);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a string has a minimum length. Uses default error message.
	 * @param string    the string to check
	 * @param minLength minimum required length
	 * @throws IllegalArgumentException if string is shorter than minLength
	 */
	public static void minLength(final String string, final int minLength) {
		minLength(string, minLength, null);
	}

	/**
	 * Checks that a string has a minimum length.
	 * @param string    the string to check
	 * @param minLength minimum required length
	 * @param message   custom error message (optional)
	 * @throws IllegalArgumentException if string is shorter than minLength
	 */
	public static void minLength(final String string, final int minLength,
		final String message) {
		notNull(string, message);
		nonNegative(minLength, message);

		if (string.length() < minLength) {
			final String defaultMessage =
				String.format("String length %d is less than minimum required %d",
					string.length(), minLength);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a collection has a minimum size. Uses default error message.
	 * @param collection the collection to check
	 * @param minSize    minimum required size
	 * @throws IllegalArgumentException if collection size is less than minSize
	 */
	public static void minSize(final Collection<?> collection, final int minSize) {
		minSize(collection, minSize, null);
	}

	/**
	 * Checks that a collection has a minimum size.
	 * @param collection the collection to check
	 * @param minSize    minimum required size
	 * @param message    custom error message (optional)
	 * @throws IllegalArgumentException if collection size is less than minSize
	 */
	public static void minSize(final Collection<?> collection, final int minSize,
		final String message) {
		notNull(collection, message);
		nonNegative(minSize, message);

		if (collection.size() < minSize) {
			final String defaultMessage =
				String.format("Collection size %d is less than minimum required %d",
					collection.size(), minSize);
			LOGGER.error("Collection size check failed: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that a number is non-negative (>= 0). Uses default error message.
	 * @param number the number to check
	 * @throws IllegalArgumentException if number is negative
	 */
	public static void nonNegative(final Number number) {
		nonNegative(number, "Number must be non-negative");
	}

	/**
	 * Checks that a number is non-negative (>= 0).
	 * @param number  the number to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if number is negative
	 */
	public static void nonNegative(final Number number, final String message) {
		notNull(number, message);

		if (number.doubleValue() < 0) {
			LOGGER.error("Number must be non-negative: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : "Number must be non-negative");
		}
	}

	/**
	 * Checks that a string is not null and not blank (empty or whitespace only). Uses
	 * default error message.
	 * @param string the string to check
	 * @throws IllegalArgumentException if string is null or blank
	 */
	public static void notBlank(final String string) {
		notBlank(string, DEFAULT_BLANK_MESSAGE);
	}

	/**
	 * Checks that a string is not null and not blank (empty or whitespace only).
	 * @param string  the string to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if string is null or blank
	 */
	public static void notBlank(final String string, final String message) {
		notNull(string, message);

		if (string.trim().isEmpty()) {
			LOGGER.error("String must not be blank: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : DEFAULT_BLANK_MESSAGE);
		}
	}

	/**
	 * Checks that a collection is not null and not empty. Uses default error message.
	 * @param object the collection to check
	 * @throws IllegalArgumentException if collection is null or empty
	 */
	public static void notEmpty(final Collection<?> object) {
		notEmpty(object, DEFAULT_EMPTY_MESSAGE);
	}

	/**
	 * Checks that a collection is not null and not empty.
	 * @param object  the collection to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if collection is null or empty
	 */
	public static void notEmpty(final Collection<?> object, final String message) {
		notNull(object, message);

		if (object.isEmpty()) {
			LOGGER.error("Collection must not be empty: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : DEFAULT_EMPTY_MESSAGE);
		}
	}

	/**
	 * Checks that an array is not null and not empty. Uses default error message.
	 * @param array the array to check
	 * @throws IllegalArgumentException if array is null or empty
	 */
	public static void notEmpty(final Object[] array) {
		notEmpty(array, DEFAULT_EMPTY_MESSAGE);
	}

	/**
	 * Checks that an array is not null and not empty.
	 * @param array   the array to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if array is null or empty
	 */
	public static void notEmpty(final Object[] array, final String message) {
		notNull(array, message);

		if (array.length == 0) {
			LOGGER.error("Array must not be empty: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : DEFAULT_EMPTY_MESSAGE);
		}
	}

	/**
	 * Checks that two objects are not equal using equals() method. Uses default error
	 * message.
	 * @param obj1 first object to compare
	 * @param obj2 second object to compare
	 * @throws IllegalArgumentException if objects are equal
	 */
	public static void notEquals(final Object obj1, final Object obj2) {
		notEquals(obj1, obj2, null);
	}

	/**
	 * Checks that two objects are not equal using equals() method.
	 * @param obj1    first object to compare
	 * @param obj2    second object to compare
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if objects are equal
	 */
	public static void notEquals(final Object obj1, final Object obj2,
		final String message) {

		if (((obj1 == null) && (obj2 == null)) || ((obj1 != null) && obj1.equals(obj2))) {
			final String defaultMessage =
				String.format("Objects must not be equal: %s == %s", obj1, obj2);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}

	/**
	 * Checks that an object is not null. Uses default error message.
	 * @param object the object to check
	 * @throws IllegalArgumentException if object is null
	 */
	public static void notNull(final Object object) {
		notNull(object, DEFAULT_NULL_MESSAGE);
	}

	/**
	 * Checks that an object is not null.
	 * @param object  the object to check
	 * @param message custom error message (optional)
	 * @throws IllegalArgumentException if object is null
	 */
	public static void notNull(final Object object, final String message) {

		if (object == null) {
			LOGGER.error("Object must not be null: {}", message);
			throw new IllegalArgumentException(
				message != null ? message : DEFAULT_NULL_MESSAGE);
		}
	}

	/**
	 * Checks that a collection size is within a specified range. Uses default error
	 * message.
	 * @param collection the collection to check
	 * @param minSize    minimum required size (inclusive)
	 * @param maxSize    maximum allowed size (inclusive)
	 * @throws IllegalArgumentException if collection size is outside the range
	 */
	public static void sizeRange(final Collection<?> collection, final int minSize,
		final int maxSize) {
		sizeRange(collection, minSize, maxSize, null);
	}

	/**
	 * Checks that a collection size is within a specified range.
	 * @param collection the collection to check
	 * @param minSize    minimum required size (inclusive)
	 * @param maxSize    maximum allowed size (inclusive)
	 * @param message    custom error message (optional)
	 * @throws IllegalArgumentException if collection size is outside the range
	 */
	public static void sizeRange(final Collection<?> collection, final int minSize,
		final int maxSize, final String message) {
		notNull(collection, message);
		nonNegative(minSize, message);
		nonNegative(maxSize, message);
		condition(minSize <= maxSize, "Minimum size cannot be greater than maximum size");
		final int size = collection.size();

		if ((size < minSize) || (size > maxSize)) {
			final String defaultMessage =
				String.format("Collection size %d is not between %d and %d (inclusive)",
					size, minSize, maxSize);
			throw new IllegalArgumentException(
				message != null ? message : defaultMessage);
		}
	}
}
