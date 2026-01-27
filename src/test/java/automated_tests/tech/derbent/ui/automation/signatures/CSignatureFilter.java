package automated_tests.tech.derbent.ui.automation.signatures;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Signature filtering based on include/exclude keyword lists provided via system properties. */
public final class CSignatureFilter {

	private static final String INCLUDE_PROPERTY = "test.signatureInclude";
	private static final String EXCLUDE_PROPERTY = "test.signatureExclude";
	private final List<String> includeKeywords;
	private final List<String> excludeKeywords;

	public CSignatureFilter() {
		includeKeywords = parseKeywords(System.getProperty(INCLUDE_PROPERTY));
		excludeKeywords = parseKeywords(System.getProperty(EXCLUDE_PROPERTY));
	}

	public List<IControlSignature> filter(final List<IControlSignature> signatures) {
		if ((includeKeywords.isEmpty() && excludeKeywords.isEmpty()) || signatures.isEmpty()) {
			return signatures;
		}
		final List<IControlSignature> filtered = new ArrayList<>();
		for (final IControlSignature signature : signatures) {
			if (!includeKeywords.isEmpty() && !matches(signature.getSignatureName(), includeKeywords)) {
				continue;
			}
			if (!excludeKeywords.isEmpty() && matches(signature.getSignatureName(), excludeKeywords)) {
				continue;
			}
			filtered.add(signature);
		}
		return filtered;
	}

	public List<String> getIncludeKeywords() {
		return includeKeywords;
	}

	public List<String> getExcludeKeywords() {
		return excludeKeywords;
	}

	private static List<String> parseKeywords(final String rawValue) {
		if (rawValue == null || rawValue.isBlank()) {
			return List.of();
		}
		final String[] tokens = rawValue.split(",");
		final List<String> keywords = new ArrayList<>();
		for (final String token : tokens) {
			final String normalized = token.trim().toLowerCase(Locale.ROOT);
			if (!normalized.isEmpty()) {
				keywords.add(normalized);
			}
		}
		return keywords;
	}

	private static boolean matches(final String signatureName, final List<String> keywords) {
		final String normalized = signatureName.toLowerCase(Locale.ROOT);
		return keywords.stream().filter(keyword -> normalized.contains(keyword)).findFirst().map(keyword -> true).orElse(false);
	}
}
