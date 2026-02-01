package tech.derbent.bab.dashboard.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemProcess - System process model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a running system process.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "pid": 1234,
 *   "name": "calimero",
 *   "user": "root",
 *   "cpuPercent": 0.0,
 *   "memPercent": 2.5,
 *   "memRssBytes": 134217728,
 *   "memVirtBytes": 268435456,
 *   "state": "S"
 * }
 * </pre>
 * Note: Calimero sends "state" (R/S/D/Z/T), "memPercent", "memRssBytes", "memVirtBytes".
 * For compatibility, these are mapped to status, memoryPercent, memoryMB fields.
 */
public class CSystemProcess extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemProcess.class);
	
	public static CSystemProcess createFromJson(final JsonObject json) {
		final CSystemProcess process = new CSystemProcess();
		process.fromJson(json);
		return process;
	}
	
	private Long pid = 0L;
	private String name = "";
	private String user = "";
	private Double cpuPercent = 0.0;
	private Long memoryMB = 0L;
	private Double memoryPercent = 0.0;
	private String status = "";
	private String command = "";
	private Long memRssBytes = 0L;
	private Long memVirtBytes = 0L;
	
	public CSystemProcess() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("pid")) {
				pid = json.get("pid").getAsLong();
			}
			if (json.has("name")) {
				name = json.get("name").getAsString();
			}
			if (json.has("user")) {
				user = json.get("user").getAsString();
			}
			if (json.has("cpuPercent")) {
				cpuPercent = json.get("cpuPercent").getAsDouble();
			}
			
			// Calimero sends memRssBytes and memVirtBytes, convert to MB
			if (json.has("memRssBytes")) {
				memRssBytes = json.get("memRssBytes").getAsLong();
				memoryMB = memRssBytes / (1024 * 1024);  // Convert bytes to MB
			}
			if (json.has("memVirtBytes")) {
				memVirtBytes = json.get("memVirtBytes").getAsLong();
			}
			
			// Calimero sends memPercent (not memoryPercent)
			if (json.has("memPercent")) {
				memoryPercent = json.get("memPercent").getAsDouble();
			}
			
			// Calimero sends state (not status) - R/S/D/Z/T
			if (json.has("state")) {
				status = json.get("state").getAsString();
			}
			
			// command field is not provided by Calimero (would need /proc/[pid]/cmdline)
			if (json.has("command")) {
				command = json.get("command").getAsString();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CSystemProcess from JSON: {}", e.getMessage());
		}
	}
	
	public Long getPid() { return pid; }
	public String getName() { return name; }
	public String getUser() { return user; }
	public Double getCpuPercent() { return cpuPercent; }
	public Long getMemoryMB() { return memoryMB; }
	public Double getMemoryPercent() { return memoryPercent; }
	public String getStatus() { return status; }
	public String getCommand() { return command; }
	public Long getMemRssBytes() { return memRssBytes; }
	public Long getMemVirtBytes() { return memVirtBytes; }
	
	/**
	 * Check if process status is running.
	 * @return true if status is "running"
	 */
	public boolean isRunning() {
		return "running".equalsIgnoreCase(status);
	}
	
	/**
	 * Get CPU usage display string.
	 * @return Formatted CPU (e.g., "2.5%")
	 */
	public String getCpuDisplay() {
		return String.format("%.1f%%", cpuPercent);
	}
	
	/**
	 * Get memory usage display string.
	 * @return Formatted memory (e.g., "128 MB (0.8%)")
	 */
	public String getMemoryDisplay() {
		return String.format("%d MB (%.1f%%)", memoryMB, memoryPercent);
	}
	
	@Override
	protected String toJson() {
		// Not used for outbound requests - processes are read-only from server
		return "{}";
	}
}
