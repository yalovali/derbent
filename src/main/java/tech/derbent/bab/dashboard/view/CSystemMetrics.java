package tech.derbent.bab.dashboard.view;

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
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "cpuUsagePercent": 15.5,
 *   "memoryUsedMB": 2048,
 *   "memoryTotalMB": 16384,
 *   "memoryUsagePercent": 12.5,
 *   "diskUsedGB": 50.5,
 *   "diskTotalGB": 500.0,
 *   "diskUsagePercent": 10.1,
 *   "uptime": 86400,
 *   "loadAverage1": 1.5,
 *   "loadAverage5": 1.2,
 *   "loadAverage15": 1.0
 * }
 * </pre>
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
			if (json.has("cpuUsagePercent")) {
				cpuUsagePercent = BigDecimal.valueOf(json.get("cpuUsagePercent").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
			}
			if (json.has("memoryUsedMB")) {
				memoryUsedMB = json.get("memoryUsedMB").getAsLong();
			}
			if (json.has("memoryTotalMB")) {
				memoryTotalMB = json.get("memoryTotalMB").getAsLong();
			}
			if (json.has("memoryUsagePercent")) {
				memoryUsagePercent = BigDecimal.valueOf(json.get("memoryUsagePercent").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
			}
			if (json.has("diskUsedGB")) {
				diskUsedGB = BigDecimal.valueOf(json.get("diskUsedGB").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
			}
			if (json.has("diskTotalGB")) {
				diskTotalGB = BigDecimal.valueOf(json.get("diskTotalGB").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
			}
			if (json.has("diskUsagePercent")) {
				diskUsagePercent = BigDecimal.valueOf(json.get("diskUsagePercent").getAsDouble()).setScale(1, RoundingMode.HALF_UP);
			}
			if (json.has("uptime")) {
				uptimeSeconds = json.get("uptime").getAsLong();
			}
			if (json.has("loadAverage1")) {
				loadAverage1 = BigDecimal.valueOf(json.get("loadAverage1").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
			}
			if (json.has("loadAverage5")) {
				loadAverage5 = BigDecimal.valueOf(json.get("loadAverage5").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
			}
			if (json.has("loadAverage15")) {
				loadAverage15 = BigDecimal.valueOf(json.get("loadAverage15").getAsDouble()).setScale(2, RoundingMode.HALF_UP);
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CSystemMetrics from JSON: {}", e.getMessage());
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
