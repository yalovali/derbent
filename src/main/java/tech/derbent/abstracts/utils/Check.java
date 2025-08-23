package tech.derbent.abstracts.utils;

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

	/** Checks that a number is positive (> 0). Uses default error message. */
	public static void checkPositive(final Number number) {
		checkPositive(number, "Number must be positive");
	}

	/** Checks that a number is positive (> 0). */
	public static void checkPositive(final Number number, final String message) {
		notNull(number, message);
		if (number.doubleValue() <= 0) {
			final String m = msg(message, "Number must be positive");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that a number is within a specified range (inclusive). Uses default error message. */
	public static void checkRange(final Number number, final Number min, final Number max) {
		checkRange(number, min, max, null);
	}

	/** Checks that a number is within a specified range (inclusive). */
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

	/** Checks a boolean condition and throws IllegalArgumentException if false. Uses default error message. */
	public static void condition(final boolean condition) {
		isTrue(condition, DEFAULT_CONDITION_MESSAGE);
	}

	/** Checks a boolean condition and throws IllegalArgumentException if false. */
	public static void isTrue(final boolean condition, final String message) {
		if (!condition) {
			final String m = msg(message, DEFAULT_CONDITION_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that a collection contains a specific element. Uses default error message. */
	public static void contains(final Collection<?> collection, final Object element) {
		contains(collection, element, null);
	}

	/** Checks that a collection contains a specific element. */
	public static void contains(final Collection<?> collection, final Object element, final String message) {
		notNull(collection, message);
		if (!collection.contains(element)) {
			final String def = String.format("Collection does not contain element: %s", element);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that a string contains a specific substring. Uses default error message. */
	public static void contains(final String string, final String substring) {
		contains(string, substring, null);
	}

	/** Checks that a string contains a specific substring. */
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

	/** Checks that two objects are equal using equals() method. Uses default error message. */
	public static void equals(final Object obj1, final Object obj2) {
		equals(obj1, obj2, null);
	}

	/** Checks that two objects are equal using equals() method. */
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

	/** Checks that an index is valid for a collection. Uses default error message. */
	public static void index(final int index, final int size) {
		index(index, size, null);
	}

	/** Checks that an index is valid for a collection. */
	public static void index(final int index, final int size, final String message) {
		if ((index < 0) || (index >= size)) {
			final String def = String.format("Index %d is out of bounds for size %d", index, size);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that an object is an instance of a specific class. Uses default error message. */
	public static void instanceOf(final Object object, final Class<?> expectedClass) {
		instanceOf(object, expectedClass, null);
	}

	/** Checks that an object is an instance of a specific class. */
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

	/** Checks that a string length is within a specified range. Uses default error message. */
	public static void lengthRange(final String string, final int minLength, final int maxLength) {
		lengthRange(string, minLength, maxLength, null);
	}

	/** Checks that a string length is within a specified range. */
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

	/** Checks that a string matches a regular expression pattern. Uses default error message. */
	public static void matches(final String string, final String regex) {
		matches(string, regex, null);
	}

	/** Checks that a string matches a regular expression pattern. */
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

	/** Checks that a string has a maximum length. Uses default error message. */
	public static void maxLength(final String string, final int maxLength) {
		maxLength(string, maxLength, null);
	}

	/** Checks that a string has a maximum length. */
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

	/** Checks that a collection has a maximum size. Uses default error message. */
	public static void maxSize(final Collection<?> collection, final int maxSize) {
		maxSize(collection, maxSize, null);
	}

	/** Checks that a collection has a maximum size. */
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

	/** Checks that a string has a minimum length. Uses default error message. */
	public static void minLength(final String string, final int minLength) {
		minLength(string, minLength, null);
	}

	/** Checks that a string has a minimum length. */
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

	/** Checks that a collection has a minimum size. Uses default error message. */
	public static void minSize(final Collection<?> collection, final int minSize) {
		minSize(collection, minSize, null);
	}

	/** Checks that a collection has a minimum size. */
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

	/** Checks that a number is non-negative (>= 0). Uses default error message. */
	public static void nonNegative(final Number number) {
		nonNegative(number, "Number must be non-negative");
	}

	/** Checks that a number is non-negative (>= 0). */
	public static void nonNegative(final Number number, final String message) {
		notNull(number, message);
		if (number.doubleValue() < 0) {
			final String m = msg(message, "Number must be non-negative");
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that a string is not null and not blank (empty or whitespace only). Uses default error message. */
	public static void notBlank(final String string) {
		notBlank(string, DEFAULT_BLANK_MESSAGE);
	}

	/** Checks that a string is not null and not blank (empty or whitespace only). */
	public static void notBlank(final String string, final String message) {
		notNull(string, message);
		if (string.trim().isEmpty()) {
			final String m = msg(message, DEFAULT_BLANK_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that a collection is not null and not empty. Uses default error message. */
	public static void notEmpty(final Collection<?> object) {
		notEmpty(object, DEFAULT_EMPTY_MESSAGE);
	}

	/** Checks that a collection is not null and not empty. */
	public static void notEmpty(final Collection<?> object, final String message) {
		notNull(object, message);
		if (object.isEmpty()) {
			final String m = msg(message, DEFAULT_EMPTY_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that an array is not null and not empty. Uses default error message. */
	public static void notEmpty(final Object[] array) {
		notEmpty(array, DEFAULT_EMPTY_MESSAGE);
	}

	/** Checks that an array is not null and not empty. */
	public static void notEmpty(final Object[] array, final String message) {
		notNull(array, message);
		if (array.length == 0) {
			final String m = msg(message, DEFAULT_EMPTY_MESSAGE);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that two objects are not equal using equals() method. Uses default error message. */
	public static void notEquals(final Object obj1, final Object obj2) {
		notEquals(obj1, obj2, null);
	}

	/** Checks that two objects are not equal using equals() method. */
	public static void notEquals(final Object obj1, final Object obj2, final String message) {
		if (((obj1 == null) && (obj2 == null)) || ((obj1 != null) && obj1.equals(obj2))) {
			final String def = String.format("Objects must not be equal: %s == %s", obj1, obj2);
			final String m = msg(message, def);
			logFail(m);
			throw new IllegalArgumentException(m);
		}
	}

	/** Checks that an object is not null. Uses default error message. */
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

	/** Checks that a collection size is within a specified range. Uses default error message. */
	public static void sizeRange(final Collection<?> collection, final int minSize, final int maxSize) {
		sizeRange(collection, minSize, maxSize, null);
	}

	/** Checks that a collection size is within a specified range. */
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
