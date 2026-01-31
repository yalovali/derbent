package tech.derbent.bab.calimero.service;

/** Simple DTO describing the current Calimero service status for UI and automated tests. */
public final class CCalimeroServiceStatus {

	private final boolean enabled;
	private final boolean running;
	private final String message;

	private CCalimeroServiceStatus(final boolean enabled, final boolean running, final String message) {
		this.enabled = enabled;
		this.running = running;
		this.message = message == null ? "" : message;
	}

	public static CCalimeroServiceStatus of(final boolean enabled, final boolean running, final String message) {
		return new CCalimeroServiceStatus(enabled, running, message);
	}

	public boolean isEnabled() { return enabled; }

	public boolean isRunning() { return running; }

	public String getMessage() { return message; }
}
