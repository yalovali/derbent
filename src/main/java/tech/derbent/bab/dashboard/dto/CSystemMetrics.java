package tech.derbent.bab.dashboard.dto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CSystemMetrics - System resource metrics model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents system CPU, memory, and disk metrics.
 * <p>
 * JSON structure from Calimero API (operation="metrics"):
 * <pre>
 * {
 *   "cpu": {
 *     "usagePercent": 15.5,
 *     "loadAvg1Min": 1.5,
 *     "loadAvg5Min": 1.2,
 *     "loadAvg15Min": 1.0,
 *     "coreCount": 8
 *   },
 *   "memory": {
 *     "totalBytes": 17179869184,
 *     "usedBytes": 2147483648,
 *     "availableBytes": 15032385536,
 *     "freeBytes": 14979909632,
 *     "usagePercent": 12.5
 *   },
 *   "swap": {
 *     "totalBytes": 8589934592,
 *     "usedBytes": 0,
 *     "freeBytes": 8589934592,
 *     "usagePercent": 0.0
 *   },
 *   "system": {
 *     "uptimeSeconds": 86400,
 *     "processCount": 342,
 *     "hostname": "localhost"
 *   }
 * }
 * </pre>
 * 
 * Note: Disk metrics are retrieved separately via operation="diskUsage" and are NOT
 * included in the metrics response. The diskUsedGB/diskTotalGB/diskUsagePercent fields
 * will remain at their default (zero) values unless populated from a separate API call.
 */
public class CSystemMetrics extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CSystemMetrics.class);
	
	public static CSystemMetrics createFromJson(final JsonObject json) {
		final CSystemMetrics metrics = new CSystemMetrics();
		metrics.fromJson(json);
		return metrics;
	}
	
	private BigDecimal cpuUsagePercent = BigDecimal.ZERO;
	private Long memoryUsedMB = 0L;
	private Long memoryTotalMB = 0L;
	private BigDecimal memoryUsagePercent = BigDecimal.ZERO;
	private BigDecimal diskUsedGB = BigDecimal.ZERO;
	private BigDecimal diskTotalGB = BigDecimal.ZERO;
	private BigDecimal diskUsagePercent = BigDecimal.ZERO;
	private Long uptimeSeconds = 0L;
	private BigDecimal loadAverage1 = BigDecimal.ZERO;
	private BigDecimal loadAverage5 = BigDecimal.ZERO;
	private BigDecimal loadAverage15 = BigDecimal.ZERO;
	
	public CSystemMetrics() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			// Parse nested CPU metrics
			if (json.has("cpu") && json.get("cpu").isJsonObject()) {
				final JsonObject cpu = json.getAsJsonObject("cpu");
				// Calimero returns: usagePercent, usage (both same value)
				if (cpu.has("usagePercent")) {
					cpuUsagePercent = BigDecimal.valueOf(cpu.get("usagePercent").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
				} else if (cpu.has("usage")) {
					cpuUsagePercent = BigDecimal.valueOf(cpu.get("usage").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
				}
			}
			
			// Parse load averages (separate from CPU in Calimero response)
			if (json.has("loadAverage") && json.get("loadAverage").isJsonObject()) {
				final JsonObject loadAvg = json.getAsJsonObject("loadAverage");
				if (loadAvg.has("1min")) {
					loadAverage1 = BigDecimal.valueOf(loadAvg.get("1min").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
				}
				if (loadAvg.has("5min")) {
					loadAverage5 = BigDecimal.valueOf(loadAvg.get("5min").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
				}
				if (loadAvg.has("15min")) {
					loadAverage15 = BigDecimal.valueOf(loadAvg.get("15min").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
				}
			}
			
			// Parse nested memory metrics
			if (json.has("memory") && json.get("memory").isJsonObject()) {
				final JsonObject memory = json.getAsJsonObject("memory");
				if (memory.has("usedBytes")) {
					memoryUsedMB = memory.get("usedBytes").getAsLong() / (1024 * 1024);
				}
				if (memory.has("totalBytes")) {
					memoryTotalMB = memory.get("totalBytes").getAsLong() / (1024 * 1024);
				}
				if (memory.has("usagePercent")) {
					memoryUsagePercent = BigDecimal.valueOf(memory.get("usagePercent").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
				}
			}
			
			// Parse uptime (direct field in Calimero response, not nested in "system")
			if (json.has("uptime")) {
				// Calimero returns uptime as double (seconds with decimals)
				uptimeSeconds = json.get("uptime").getAsLong();
			}
			
			// Note: Disk metrics are NOT included in the "metrics" operation response
			// They must be retrieved separately via "diskUsage" operation
			// For now, disk fields remain at their default (zero) values
			
		} catch (final Exception e) {
			LOGGER.error("Error parsing CSystemMetrics from JSON: {}", e.getMessage(), e);
		}
	}
	
	public BigDecimal getCpuUsagePercent() { return cpuUsagePercent; }
	public Long getMemoryUsedMB() { return memoryUsedMB; }
	public Long getMemoryTotalMB() { return memoryTotalMB; }
	public BigDecimal getMemoryUsagePercent() { return memoryUsagePercent; }
	public BigDecimal getDiskUsedGB() { return diskUsedGB; }
	public BigDecimal getDiskTotalGB() { return diskTotalGB; }
	public BigDecimal getDiskUsagePercent() { return diskUsagePercent; }
	public Long getUptimeSeconds() { return uptimeSeconds; }
	public BigDecimal getLoadAverage1() { return loadAverage1; }
	public BigDecimal getLoadAverage5() { return loadAverage5; }
	public BigDecimal getLoadAverage15() { return loadAverage15; }
	
	/**
	 * Get human-readable uptime string.
	 * @return Formatted uptime (e.g., "1d 2h 30m")
	 */
	public String getUptimeDisplay() {
		if (uptimeSeconds == null || uptimeSeconds == 0) {
			return "-";
		}
		
		final long days = uptimeSeconds / 86400;
		final long hours = (uptimeSeconds % 86400) / 3600;
		final long minutes = (uptimeSeconds % 3600) / 60;
		
		if (days > 0) {
			return String.format("%dd %dh %dm", days, hours, minutes);
		} else if (hours > 0) {
			return String.format("%dh %dm", hours, minutes);
		} else {
			return String.format("%dm", minutes);
		}
	}
	
	/**
	 * Get memory usage display string.
	 * @return Formatted memory (e.g., "2048 MB / 16384 MB (12.5%)")
	 */
	public String getMemoryDisplay() {
		if (memoryTotalMB == null || memoryTotalMB == 0) {
			return "-";
		}
		return String.format("%d MB / %d MB (%.1f%%)", memoryUsedMB, memoryTotalMB, memoryUsagePercent);
	}
	
	/**
	 * Get disk usage display string.
	 * @return Formatted disk (e.g., "50.5 GB / 500.0 GB (10.1%)")
	 */
	public String getDiskDisplay() {
		if (diskTotalGB == null || diskTotalGB.compareTo(BigDecimal.ZERO) == 0) {
			return "-";
		}
		return String.format("%.2f GB / %.2f GB (%.1f%%)", diskUsedGB, diskTotalGB, diskUsagePercent);
	}
	
	/**
	 * Get load average display string.
	 * @return Formatted load (e.g., "1.50 / 1.20 / 1.00")
	 */
	public String getLoadAverageDisplay() {
		return String.format("%.2f / %.2f / %.2f", loadAverage1, loadAverage5, loadAverage15);
	}
	
	@Override
	protected String toJson() {
		// Not used for outbound requests - metrics are read-only from server
		return "{}";
	}
}
