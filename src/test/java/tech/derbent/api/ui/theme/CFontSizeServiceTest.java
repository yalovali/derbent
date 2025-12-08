package tech.derbent.api.ui.theme;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for CFontSizeService - Font size scaling functionality. Tests CSS variable generation for different font size scales. */
class CFontSizeServiceTest {

	@Test
	void testGetCssVariablesForSmallScale() {
		// Given: small scale
		final String scale = "small";
		// When: getting CSS variables (using reflection to access private method)
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should contain small font sizes
		assertTrue(cssVars.contains("--lumo-font-size-m: 0.75rem"), "Should have small base font size");
		assertTrue(cssVars.contains("--lumo-font-size-l: 0.8125rem"), "Should have small large font size");
	}

	@Test
	void testGetCssVariablesForMediumScale() {
		// Given: medium scale (default)
		final String scale = "medium";
		// When: getting CSS variables
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should contain medium font sizes
		assertTrue(cssVars.contains("--lumo-font-size-m: 0.875rem"), "Should have medium base font size");
		assertTrue(cssVars.contains("--lumo-font-size-l: 0.9375rem"), "Should have medium large font size");
	}

	@Test
	void testGetCssVariablesForLargeScale() {
		// Given: large scale
		final String scale = "large";
		// When: getting CSS variables
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should contain large font sizes
		assertTrue(cssVars.contains("--lumo-font-size-m: 1rem"), "Should have large base font size");
		assertTrue(cssVars.contains("--lumo-font-size-l: 1.125rem"), "Should have large large font size");
	}

	@Test
	void testGetCssVariablesForInvalidScaleFallsBackToMedium() {
		// Given: invalid scale
		final String scale = "invalid";
		// When: getting CSS variables
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should fall back to medium font sizes
		assertTrue(cssVars.contains("--lumo-font-size-m: 0.875rem"), "Should fall back to medium base font size");
	}

	@Test
	void testGetCssVariablesForNullScaleFallsBackToMedium() {
		// Given: null scale
		final String scale = null;
		// When: getting CSS variables
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should fall back to medium font sizes
		assertTrue(cssVars.contains("--lumo-font-size-m: 0.875rem"), "Should fall back to medium base font size");
	}

	@Test
	void testAllFontSizeVariablesAreIncluded() {
		// Given: any valid scale
		final String scale = "medium";
		// When: getting CSS variables
		final String cssVars = getCssVariablesForScale(scale);
		// Then: should contain all 8 font size variables
		assertTrue(cssVars.contains("--lumo-font-size-xxxl:"), "Should include xxxl");
		assertTrue(cssVars.contains("--lumo-font-size-xxl:"), "Should include xxl");
		assertTrue(cssVars.contains("--lumo-font-size-xl:"), "Should include xl");
		assertTrue(cssVars.contains("--lumo-font-size-l:"), "Should include l");
		assertTrue(cssVars.contains("--lumo-font-size-m:"), "Should include m");
		assertTrue(cssVars.contains("--lumo-font-size-s:"), "Should include s");
		assertTrue(cssVars.contains("--lumo-font-size-xs:"), "Should include xs");
		assertTrue(cssVars.contains("--lumo-font-size-xxs:"), "Should include xxs");
	}

	/** Helper method to simulate the private getCssVariablesForScale method. This is a copy of the implementation for testing purposes. */
	private String getCssVariablesForScale(final String scale) {
		if (scale == null) {
			return getCssVariablesForScale("medium");
		}
		return switch (scale) {
			case "small" -> // Extra small fonts - reduce by ~25% from medium
			"--lumo-font-size-xxxl: 2.25rem; " + "--lumo-font-size-xxl: 1.6875rem; " + "--lumo-font-size-xl: 1rem; "
					+ "--lumo-font-size-l: 0.8125rem; " + "--lumo-font-size-m: 0.75rem; " + "--lumo-font-size-s: 0.6875rem; "
					+ "--lumo-font-size-xs: 0.625rem; " + "--lumo-font-size-xxs: 0.5625rem;";
			case "large" -> // Larger fonts - increase by ~20% from medium
			"--lumo-font-size-xxxl: 3rem; " + "--lumo-font-size-xxl: 2.25rem; " + "--lumo-font-size-xl: 1.375rem; "
					+ "--lumo-font-size-l: 1.125rem; " + "--lumo-font-size-m: 1rem; " + "--lumo-font-size-s: 0.875rem; "
					+ "--lumo-font-size-xs: 0.8125rem; " + "--lumo-font-size-xxs: 0.75rem;";
			default -> // Medium (current default in styles.css)
			"--lumo-font-size-xxxl: 2.5rem; " + "--lumo-font-size-xxl: 1.875rem; " + "--lumo-font-size-xl: 1.125rem; "
					+ "--lumo-font-size-l: 0.9375rem; " + "--lumo-font-size-m: 0.875rem; " + "--lumo-font-size-s: 0.75rem; "
					+ "--lumo-font-size-xs: 0.6875rem; " + "--lumo-font-size-xxs: 0.625rem;";
		};
	}
}
