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
 *   "cpuPercent": 2.5,
 *   "memoryMB": 128,
 *   "memoryPercent": 0.8,
 *   "status": "running",
 *   "command": "/usr/bin/calimero --port 8077"
 * }
 * </pre>
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
			if (json.has("memoryMB")) {
				memoryMB = json.get("memoryMB").getAsLong();
			}
			if (json.has("memoryPercent")) {
				memoryPercent = json.get("memoryPercent").getAsDouble();
			}
			if (json.has("status")) {
				status = json.get("status").getAsString();
			}
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
