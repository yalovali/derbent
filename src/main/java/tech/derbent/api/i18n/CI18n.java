package tech.derbent.api.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Minimal I18n helper used by views to fetch translations from src/main/resources/i18n/messages_*.properties.
 * This helper keeps a safe fallback to English when resources are missing so tests keep working.
 */
public final class CI18n {

    private static final String BUNDLE_BASE = "i18n.messages";

    private CI18n() { /* utility */ }

    public static String get(final String key, final Object... params) {
        try {
            final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE, Locale.getDefault());
            final String pattern = bundle.getString(key);
            return params == null || params.length == 0 ? pattern : MessageFormat.format(pattern, params);
        } catch (final MissingResourceException e) {
            // Fallback to English messages if available
            try {
                final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE, Locale.ENGLISH);
                final String pattern = bundle.getString(key);
                return params == null || params.length == 0 ? pattern : MessageFormat.format(pattern, params);
            } catch (final MissingResourceException ex) {
                // Last resort: return the key itself so UI still shows something meaningful
                return key;
            }
        }
    }
}
