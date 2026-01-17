package automated_tests.tech.derbent.ui.automation.signatures;

import java.util.List;
import java.util.function.Predicate;
import com.microsoft.playwright.Page;
import automated_tests.tech.derbent.ui.automation.components.IComponentTester;

/** Default control signature implementation that uses a predicate for detection. */
public final class CControlSignature implements IControlSignature {

	private final Predicate<Page> detector;
	private final String signatureName;
	private final IComponentTester tester;

	public CControlSignature(final String signatureName, final Predicate<Page> detector, final IComponentTester tester) {
		this.signatureName = signatureName;
		this.detector = detector;
		this.tester = tester;
	}

	public static CControlSignature forSelector(final String signatureName, final String selector, final IComponentTester tester) {
		return new CControlSignature(signatureName, page -> page.locator(selector).count() > 0, tester);
	}

	public static CControlSignature forSelectorsMinMatch(final String signatureName, final List<String> selectors, final int minMatches,
			final IComponentTester tester) {
		return new CControlSignature(signatureName, page -> {
			int matched = 0;
			for (final String selector : selectors) {
				if (page.locator(selector).count() > 0) {
					matched++;
				}
			}
			return matched >= minMatches;
		}, tester);
	}

	@Override
	public String getSignatureName() {
		return signatureName;
	}

	@Override
	public boolean isDetected(final Page page) {
		return detector.test(page);
	}

	@Override
	public IComponentTester getTester() {
		return tester;
	}
}
