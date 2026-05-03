package tech.derbent.api.imports.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

/**
 * Small utility wrapper around a normalized rowData map.
 *
 * WHY: almost every import handler repeats the same trim/default/parse logic; centralizing it keeps
 * handlers focused on business rules (relations, defaults, validation) instead of string plumbing.
 */
public final class CExcelRow {

    private final Map<String, String> rowData;

    public CExcelRow(final Map<String, String> rowData) {
        this.rowData = rowData == null ? Map.of() : rowData;
    }

    /**
     * Normalizes tokens to match {@link CExcelImportService}'s canonical header mapping.
     *
     * WHY: the engine lower-cases and strips whitespace; handlers should be able to request values
     * with either Java-ish names (e.g. dueDate) or display-ish names (e.g. Due Date) safely.
     */
    public static String normalizeToken(final String token) {
        if (token == null) {
            return "";
        }
        return token.trim().toLowerCase().replaceAll("\\s+", "");
    }

    public String string(final String token) {
        return rowData.getOrDefault(normalizeToken(token), "").trim();
    }

    public boolean isBlank(final String token) {
        return string(token).isBlank();
    }

    public Optional<String> optionalString(final String token) {
        final String v = string(token);
        return v.isBlank() ? Optional.empty() : Optional.of(v);
    }

    public Optional<Integer> optionalInt(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Integer.valueOf(v));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid integer for " + normalizeToken(token) + ": '" + v + "'", e);
        }
    }

    public Optional<Long> optionalLong(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Long.valueOf(v));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid long for " + normalizeToken(token) + ": '" + v + "'", e);
        }
    }

    public Optional<BigDecimal> optionalBigDecimal(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new BigDecimal(v));
        } catch (final Exception e) {
            throw new IllegalArgumentException("Invalid decimal for " + normalizeToken(token) + ": '" + v + "'", e);
        }
    }

    public Optional<LocalDate> optionalLocalDate(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        return CImportParsers.tryParseLocalDate(v);
    }

    public Optional<LocalTime> optionalLocalTime(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        return CImportParsers.tryParseLocalTime(v);
    }

    public Optional<LocalDateTime> optionalLocalDateTime(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        return CImportParsers.tryParseLocalDateTime(v);
    }

    public Optional<Boolean> optionalBoolean(final String token) {
        final String v = string(token);
        if (v.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Boolean.valueOf(CImportParsers.parseBooleanLenient(v)));
    }

    public Map<String, String> raw() {
        return rowData;
    }
}
