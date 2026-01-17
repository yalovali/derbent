package automated_tests.tech.derbent.ui.automation.signatures;

import com.microsoft.playwright.Page;
import automated_tests.tech.derbent.ui.automation.components.IComponentTester;

/** Defines a UI control signature that can be detected on a page and mapped to a component tester. */
public interface IControlSignature {

	/** @return human-readable signature name for logging */
	String getSignatureName();

	/** @return true if the signature is detected on the page */
	boolean isDetected(Page page);

	/** @return component tester to execute when signature is detected */
	IComponentTester getTester();
}
