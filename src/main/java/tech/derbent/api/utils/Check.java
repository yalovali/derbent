package tech.derbent.api.utils;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Check {

	/** Default error message for condition checks */
	private static final String DEFAULT_CONDITION_MESSAGE = "Condition check failed";
	/** Default error message for null checks */
	private static final String DEFAULT_NULL_MESSAGE = "Object cannot be null";
	/** Default error message for empty checks */
	private static final String DEFAULT_EMPTY_MESSAGE = "Collection cannot be empty";
	/** Default error message for blank string checks */
	private static final String DEFAULT_BLANK_MESSAGE = "String cannot be null or blank";
	private static final Logger LOGGER = LoggerFactory.getLogger(Check.class);
	/* ============================== */
	/* Common caller/location helpers */
	/* ============================== */

	/** Returns first stack frame outside this utility, formatted as ShortClass.method(File.java:line). */
	private static String callerLocation() {
		final StackTraceElement caller = findExternalCaller();
		final String className = shortClassName(caller.getClassName());
		return String.format("%s.%s(%s:%d)", className, caller.getMethodName(), caller.getFileName(), caller.getLineNumber());
	}

	public static void checkPositive(final Number number) {
		checkPositive(number, "Number must be positive");
	}

	public static void checkPositive(final Number number, final String message) {
		notNull(number, message);
		if (number.doubleValue() <= 0) {
			final String m = msg(message, "Number must be positive");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void checkRange(final Number number, final Number min, final Number max) {
		checkRange(number, min, max, null);
	}

	/** Validates that a number falls within specified inclusive bounds. Performs null validation on all parameters and range validation on the
	 * numeric value.
	 * @param number  the number to validate
	 * @param min     the minimum allowed value (inclusive)
	 * @param max     the maximum allowed value (inclusive)
	 * @param message custom error message, or null for default
	 * @throws IllegalArgumentException if any parameter is null or number is outside range */
	public static void checkRange(final Number number, final Number min, final Number max, final String message) {
		notNull(number, message);
		notNull(min, message);
		notNull(max, message);
		final double value = number.doubleValue();
		final double minValue = min.doubleValue();
		final double maxValue = max.doubleValue();
		if ((value < minValue) || (value > maxValue)) {
			final String def = String.format("Number must be between %s and %s (inclusive)", min, max);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}
	/* ============================== */
	/* Checks */
	/* ============================== */

	public static void condition(final boolean condition) {
		isTrue(condition, DEFAULT_CONDITION_MESSAGE);
	}

	public static void contains(final Collection<?> collection, final Object element) {
		contains(collection, element, null);
	}

	public static void contains(final Collection<?> collection, final Object element, final String message) {
		notNull(collection, message);
		if (!collection.contains(element)) {
			final String def = String.format("Collection does not contain element: %s", element);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void contains(final String string, final String substring) {
		contains(string, substring, null);
	}

	/** Validates that a string contains a specified substring. Performs null validation on both parameters before checking containment.
	 * @param string    the string to search within
	 * @param substring the substring to search for
	 * @param message   custom error message, or null for default
	 * @throws IllegalArgumentException if either parameter is null or substring not found */
	public static void contains(final String string, final String substring, final String message) {
		notNull(string, message);
		notNull(substring, message);
		if (!string.contains(substring)) {
			final String def = String.format("String '%s' does not contain '%s'", string, substring);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void equals(final Object obj1, final Object obj2) {
		equals(obj1, obj2, null);
	}

	/** Validates that two objects are equal using the equals() method. Handles null cases properly - two null objects are considered equal.
	 * @param obj1    the first object to compare
	 * @param obj2    the second object to compare
	 * @param message custom error message, or null for default
	 * @throws IllegalArgumentException if objects are not equal */
	public static void equals(final Object obj1, final Object obj2, final String message) {
		if ((obj1 == null) && (obj2 == null)) {
			return; // Both null is considered equal
		}
		if ((obj1 == null) || (obj2 == null) || !obj1.equals(obj2)) {
			final String def = String.format("Objects are not equal: %s != %s", obj1, obj2);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void fail(final String message) {
		logFail(message);
		throw new IllegalArgumentException(message);
	}

	/** Finds the first stack trace element that is not from Check or java.lang.Thread. */
	private static StackTraceElement findExternalCaller() {
		final String self = Check.class.getName();
		final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		for (final StackTraceElement ste : stack) {
			final String cn = ste.getClassName();
			if (!cn.equals(self) && !cn.equals(Thread.class.getName())) {
				return ste;
			}
		}
		// Fallback to a reasonable default if not found
		return stack.length > 2 ? stack[2] : stack[stack.length - 1];
	}

	public static void index(final int index, final int size) {
		index(index, size, null);
	}

	public static void index(final int index, final int size, final String message) {
		if ((index < 0) || (index >= size)) {
			final String def = String.format("Index %d is out of bounds for size %d", index, size);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void instanceOf(final Object object, final Class<?> expectedClass) {
		instanceOf(object, expectedClass, null);
	}

	/** Validates that an object is an instance of the specified class. Performs null validation on both object and expected class before type
	 * checking.
	 * @param object        the object to validate
	 * @param expectedClass the expected class type
	 * @param message       custom error message, or null for default
	 * @throws IllegalArgumentException if object is null, expectedClass is null, or object is not an instance */
	public static void instanceOf(final Object object, final Class<?> expectedClass, final String message) {
		notNull(object, message);
		notNull(expectedClass, message);
		if (!expectedClass.isInstance(object)) {
			final String def =
					String.format("Object of type %s is not an instance of %s", object.getClass().getSimpleName(), expectedClass.getSimpleName());
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void isNull(final String string) {
		isNull(string, "Object must be null");
	}

	public static void isNull(final Object object, final String message) {
		if (object != null) {
			final String m = msg(message, "Object must be null");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void isBlank(final String string) {
		isBlank(string, "String must be null or blank");
	}

	public static void isBlank(final String string, final String message) {
		if ((string != null) && !string.trim().isEmpty()) {
			final String m = msg(message, "String must be null or blank");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void isTrue(final boolean condition, final String message) {
		if (!condition) {
			final String m = msg(message, DEFAULT_CONDITION_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void lengthRange(final String string, final int minLength, final int maxLength) {
		lengthRange(string, minLength, maxLength, null);
	}

	/** Validates that a string length falls within specified bounds. Performs validation on string, length parameters, and logical consistency before
	 * checking length.
	 * @param string    the string to validate
	 * @param minLength minimum allowed length (inclusive)
	 * @param maxLength maximum allowed length (inclusive)
	 * @param message   custom error message, or null for default
	 * @throws IllegalArgumentException if string is null, lengths are negative, min > max, or length out of range */
	public static void lengthRange(final String string, final int minLength, final int maxLength, final String message) {
		notNull(string, message);
		nonNegative(minLength, message);
		nonNegative(maxLength, message);
		isTrue(minLength <= maxLength, "Minimum length cannot be greater than maximum length");
		final int length = string.length();
		if ((length < minLength) || (length > maxLength)) {
			final String def = String.format("String length %d is not between %d and %d (inclusive)", length, minLength, maxLength);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Log a failure at caller location with the final message. */
	private static void logFail(final String finalMessage) {
		LOGGER.error("{} @ {}", finalMessage, callerLocation());
	}

	public static void matches(final String string, final String regex) {
		matches(string, regex, null);
	}

	/** Validates that a string matches a regular expression pattern. Performs null/blank validation on both parameters before pattern matching.
	 * @param string  the string to validate against the pattern
	 * @param regex   the regular expression pattern to match
	 * @param message custom error message, or null for default
	 * @throws IllegalArgumentException if string is null, regex is null/blank, or pattern doesn't match */
	public static void matches(final String string, final String regex, final String message) {
		notNull(string, message);
		notBlank(regex, message);
		if (!string.matches(regex)) {
			final String def = String.format("String '%s' does not match pattern '%s'", string, regex);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void maxLength(final String string, final int maxLength) {
		maxLength(string, maxLength, null);
	}

	/** Validates that a string does not exceed a maximum length. Performs null validation on string and non-negative validation on maxLength.
	 * @param string    the string to validate
	 * @param maxLength maximum allowed length (must be non-negative)
	 * @param message   custom error message, or null for default
	 * @throws IllegalArgumentException if string is null, maxLength is negative, or string exceeds length */
	public static void maxLength(final String string, final int maxLength, final String message) {
		notNull(string, message);
		nonNegative(maxLength, message);
		if (string.length() > maxLength) {
			final String def = String.format("String length %d exceeds maximum allowed %d", string.length(), maxLength);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void maxSize(final Collection<?> collection, final int maxSize) {
		maxSize(collection, maxSize, null);
	}

	/** Validates that a collection does not exceed a maximum size. Performs null validation on collection and non-negative validation on maxSize.
	 * @param collection the collection to validate
	 * @param maxSize    maximum allowed size (must be non-negative)
	 * @param message    custom error message, or null for default
	 * @throws IllegalArgumentException if collection is null, maxSize is negative, or collection exceeds size */
	public static void maxSize(final Collection<?> collection, final int maxSize, final String message) {
		notNull(collection, message);
		nonNegative(maxSize, message);
		if (collection.size() > maxSize) {
			final String def = String.format("Collection size %d exceeds maximum allowed %d", collection.size(), maxSize);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void minLength(final String string, final int minLength) {
		minLength(string, minLength, null);
	}

	/** Validates that a string meets a minimum length requirement. Performs null validation on string and non-negative validation on minLength.
	 * @param string    the string to validate
	 * @param minLength minimum required length (must be non-negative)
	 * @param message   custom error message, or null for default
	 * @throws IllegalArgumentException if string is null, minLength is negative, or string is too short */
	public static void minLength(final String string, final int minLength, final String message) {
		notNull(string, message);
		nonNegative(minLength, message);
		if (string.length() < minLength) {
			final String def = String.format("String length %d is less than minimum required %d", string.length(), minLength);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void minSize(final Collection<?> collection, final int minSize) {
		minSize(collection, minSize, null);
	}

	/** Validates that a collection meets a minimum size requirement. Performs null validation on collection and non-negative validation on minSize.
	 * @param collection the collection to validate
	 * @param minSize    minimum required size (must be non-negative)
	 * @param message    custom error message, or null for default
	 * @throws IllegalArgumentException if collection is null, minSize is negative, or collection is too small */
	public static void minSize(final Collection<?> collection, final int minSize, final String message) {
		notNull(collection, message);
		nonNegative(minSize, message);
		if (collection.size() < minSize) {
			final String def = String.format("Collection size %d is less than minimum required %d", collection.size(), minSize);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Resolve message (custom or default). */
	private static String msg(final String custom, final String deflt) {
		return (custom != null) ? custom : deflt;
	}

	public static void nonNegative(final Number number) {
		nonNegative(number, "Number must be non-negative");
	}

	public static void nonNegative(final Number number, final String message) {
		notNull(number, message);
		if (number.doubleValue() < 0) {
			final String m = msg(message, "Number must be non-negative");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notBlank(final String string) {
		notBlank(string, DEFAULT_BLANK_MESSAGE);
	}

	public static void notBlank(final String string, final String message) {
		notNull(string, message);
		if (string.trim().isEmpty()) {
			final String m = msg(message, DEFAULT_BLANK_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notEmpty(final Collection<?> object) {
		notEmpty(object, DEFAULT_EMPTY_MESSAGE);
	}

	public static void notEmpty(final Collection<?> object, final String message) {
		notNull(object, message);
		if (object.isEmpty()) {
			final String m = msg(message, DEFAULT_EMPTY_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notEmpty(final Object[] array) {
		notEmpty(array, DEFAULT_EMPTY_MESSAGE);
	}

	public static void notEmpty(final Object[] array, final String message) {
		notNull(array, message);
		if (array.length == 0) {
			final String m = msg(message, DEFAULT_EMPTY_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notEmpty(final byte[] array) {
		notEmpty(array, DEFAULT_EMPTY_MESSAGE);
	}

	public static void notEmpty(final byte[] array, final String message) {
		notNull(array, message);
		if (array.length == 0) {
			final String m = msg(message, DEFAULT_EMPTY_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notEquals(final Object obj1, final Object obj2) {
		notEquals(obj1, obj2, null);
	}

	public static void notEquals(final Object obj1, final Object obj2, final String message) {
		if (((obj1 == null) && (obj2 == null)) || ((obj1 != null) && obj1.equals(obj2))) {
			final String def = String.format("Objects must not be equal: %s == %s", obj1, obj2);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	public static void notNull(final Object object) {
		notNull(object, DEFAULT_NULL_MESSAGE);
	}

	public static void notNull(final Object object, final String message) {
		if (object == null) {
			final String m = msg(message, DEFAULT_NULL_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Converts FQCN to simple class name safely. */
	private static String shortClassName(final String fqcn) {
		final int idx = fqcn.lastIndexOf('.');
		return ((idx >= 0) && (idx < (fqcn.length() - 1))) ? fqcn.substring(idx + 1) : fqcn;
	}

	public static void sizeRange(final Collection<?> collection, final int minSize, final int maxSize) {
		sizeRange(collection, minSize, maxSize, null);
	}

	/** Validates that a collection size falls within specified bounds. Performs validation on collection, size parameters, and logical consistency
	 * before checking size.
	 * @param collection the collection to validate
	 * @param minSize    minimum allowed size (inclusive, must be non-negative)
	 * @param maxSize    maximum allowed size (inclusive, must be non-negative)
	 * @param message    custom error message, or null for default
	 * @throws IllegalArgumentException if collection is null, sizes are negative, min > max, or size out of range */
	public static void sizeRange(final Collection<?> collection, final int minSize, final int maxSize, final String message) {
		notNull(collection, message);
		nonNegative(minSize, message);
		nonNegative(maxSize, message);
		isTrue(minSize <= maxSize, "Minimum size cannot be greater than maximum size");
		final int size = collection.size();
		if ((size < minSize) || (size > maxSize)) {
			final String def = String.format("Collection size %d is not between %d and %d (inclusive)", size, minSize, maxSize);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}
}
