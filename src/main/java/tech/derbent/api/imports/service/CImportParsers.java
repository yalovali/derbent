package tech.derbent.api.imports.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Set;

/**
 * Shared parsing helpers for Excel import.
 *
 * WHY: different handlers used different date/boolean parsing rules which made large multi-sheet
 * system_init workbooks brittle; centralizing the accepted formats makes imports predictable.
 */
public final class CImportParsers {

    private static final DateTimeFormatter[] DATE_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("d.M.yyyy"),
    };

    private static final DateTimeFormatter[] TIME_FORMATS = {
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss"),
    };

    private CImportParsers() { /* utility class */ }

    public static Optional<LocalDate> tryParseLocalDate(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        final String value = raw.trim();
        for (final DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return Optional.of(LocalDate.parse(value, fmt));
            } catch (final DateTimeParseException ignored) {
                // try next
            }
        }
        // As a last resort, try ISO parser (covers yyyy-MM-dd already, but also accepts some variants).
        try {
            return Optional.of(LocalDate.parse(value));
        } catch (final Exception ignored) {
            return Optional.empty();
        }
    }

    public static Optional<LocalTime> tryParseLocalTime(final String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        final String value = raw.trim();
        for (final DateTimeFormatter fmt : TIME_FORMATS) {
            try {
                return Optional.of(LocalTime.parse(value, fmt));
            } catch (final DateTimeParseException ignored) {
                // try next
            }
        }
        try {
            return Optional.of(LocalTime.parse(value));
        } catch (final Exception ignored) {
            return Optional.empty();
        }
    }

    public static boolean parseBooleanLenient(final String raw) {
        if (raw == null) {
            return false;
        }
        final String value = raw.trim().toLowerCase();
        return Set.of("true", "yes", "y", "1", "on").contains(value);
    }
}
