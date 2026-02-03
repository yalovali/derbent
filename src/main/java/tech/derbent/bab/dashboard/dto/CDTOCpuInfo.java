package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDTOCpuInfo - CPU information model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents detailed CPU information and usage statistics.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "model": "Intel(R) Core(TM) i7-9700K CPU @ 3.60GHz",
 *   "cores": 8,
 *   "threads": 16,
 *   "architecture": "x86_64",
 *   "usagePercent": 25.5,
 *   "userPercent": 15.0,
 *   "systemPercent": 10.5,
 *   "idlePercent": 74.5,
 *   "iowaitPercent": 0.0,
 *   "frequency": 3600,
 *   "maxFrequency": 4900,
 *   "temperature": 45.0
 * }
 * </pre>
 */
public class CDTOCpuInfo extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDTOCpuInfo.class);
	
	public static CDTOCpuInfo createFromJson(final JsonObject json) {
		final CDTOCpuInfo cpuInfo = new CDTOCpuInfo();
		cpuInfo.fromJson(json);
		return cpuInfo;
	}
	
	private String model = "";
	private Integer cores = 0;
	private Integer threads = 0;
	private String architecture = "";
	private Double usagePercent = 0.0;
	private Double userPercent = 0.0;
	private Double systemPercent = 0.0;
	private Double idlePercent = 100.0;
	private Double iowaitPercent = 0.0;
	private Integer frequency = 0;
	private Integer maxFrequency = 0;
	private Double temperature = 0.0;
	
	public CDTOCpuInfo() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			if (json.has("model")) {
				model = json.get("model").getAsString();
			}
			if (json.has("cores")) {
				cores = json.get("cores").getAsInt();
			}
			if (json.has("threads")) {
				threads = json.get("threads").getAsInt();
			}
			if (json.has("architecture")) {
				architecture = json.get("architecture").getAsString();
			}
			if (json.has("usagePercent")) {
				usagePercent = json.get("usagePercent").getAsDouble();
			}
			if (json.has("userPercent")) {
				userPercent = json.get("userPercent").getAsDouble();
			}
			if (json.has("systemPercent")) {
				systemPercent = json.get("systemPercent").getAsDouble();
			}
			if (json.has("idlePercent")) {
				idlePercent = json.get("idlePercent").getAsDouble();
			}
			if (json.has("iowaitPercent")) {
				iowaitPercent = json.get("iowaitPercent").getAsDouble();
			}
			if (json.has("frequency")) {
				frequency = json.get("frequency").getAsInt();
			}
			if (json.has("maxFrequency")) {
				maxFrequency = json.get("maxFrequency").getAsInt();
			}
			if (json.has("temperature")) {
				temperature = json.get("temperature").getAsDouble();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CDTOCpuInfo from JSON: {}", e.getMessage());
		}
	}
	
	@Override
	protected String toJson() {
		// CPU info is read-only from server
		return "{}";
	}
	
	public String getModel() { return model; }
	public Integer getCores() { return cores; }
	public Integer getThreads() { return threads; }
	public String getArchitecture() { return architecture; }
	public Double getUsagePercent() { return usagePercent; }
	public Double getUserPercent() { return userPercent; }
	public Double getSystemPercent() { return systemPercent; }
	public Double getIdlePercent() { return idlePercent; }
	public Double getIowaitPercent() { return iowaitPercent; }
	public Integer getFrequency() { return frequency; }
	public Integer getMaxFrequency() { return maxFrequency; }
	public Double getTemperature() { return temperature; }
	
	/**
	 * Get core count display string.
	 * @return Formatted cores (e.g., "8 cores / 16 threads")
	 */
	public String getCoreDisplay() {
		return String.format("%d cores / %d threads", cores, threads);
	}
	
	/**
	 * Get frequency display string.
	 * @return Formatted frequency (e.g., "3.6 GHz / 4.9 GHz max")
	 */
	public String getFrequencyDisplay() {
		return String.format("%.1f GHz / %.1f GHz max", 
				frequency / 1000.0, maxFrequency / 1000.0);
	}
	
	/**
	 * Get temperature display string.
	 * @return Formatted temperature (e.g., "45.0°C")
	 */
	public String getTemperatureDisplay() {
		return String.format("%.1f°C", temperature);
	}
	
	/**
	 * Check if CPU usage is high (> 80%).
	 * @return true if usage exceeds 80%
	 */
	public boolean isHighUsage() {
		return usagePercent != null && usagePercent > 80.0;
	}
	
	/**
	 * Check if CPU temperature is high (> 75°C).
	 * @return true if temperature exceeds 75°C
	 */
	public boolean isHighTemperature() {
		return temperature != null && temperature > 75.0;
	}
}
