package tech.derbent.bab.dashboard.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import tech.derbent.bab.uiobjects.domain.CObject;

/**
 * CDiskInfo - Disk/filesystem information model from Calimero server.
 * <p>
 * This is NOT a JPA entity - it's a simple data object parsed from Calimero HTTP API responses.
 * Represents a single disk or filesystem mount point.
 * <p>
 * JSON structure from Calimero:
 * <pre>
 * {
 *   "mountPoint": "/",
 *   "filesystem": "",
 *   "device": "",
 *   "totalBytes": 500000000000,
 *   "usedBytes": 250000000000,
 *   "availableBytes": 250000000000,
 *   "usagePercent": 50.0
 * }
 * </pre>
 * Note: Calimero sends bytes, converted to GB. filesystem/type fields are often empty.
 */
public class CDiskInfo extends CObject {
	
	private static final long serialVersionUID = 1L;
	private static final Logger LOGGER = LoggerFactory.getLogger(CDiskInfo.class);
	
	public static CDiskInfo createFromJson(final JsonObject json) {
		final CDiskInfo diskInfo = new CDiskInfo();
		diskInfo.fromJson(json);
		return diskInfo;
	}
	
	private String filesystem = "";
	private String type = "";
	private String mountPoint = "";
	private Double totalGB = 0.0;
	private Double usedGB = 0.0;
	private Double availableGB = 0.0;
	private Double usagePercent = 0.0;
	private Long inodes = 0L;
	private Long inodesUsed = 0L;
	private Double inodesPercent = 0.0;
	
	public CDiskInfo() {
		// Default constructor
	}
	
	@Override
	protected void fromJson(final JsonObject json) {
		try {
			// Calimero sends filesystem (empty) - use device instead if available
			if (json.has("device")) {
				filesystem = json.get("device").getAsString();
			} else if (json.has("filesystem")) {
				filesystem = json.get("filesystem").getAsString();
			}
			
			// Calimero sends filesystem type (empty) - keep type field
			if (json.has("filesystem") && !json.get("filesystem").getAsString().isEmpty()) {
				type = json.get("filesystem").getAsString();
			} else if (json.has("type")) {
				type = json.get("type").getAsString();
			}
			
			if (json.has("mountPoint")) {
				mountPoint = json.get("mountPoint").getAsString();
			}
			
			// Calimero sends totalBytes, usedBytes, availableBytes - convert to GB
			if (json.has("totalBytes")) {
				totalGB = json.get("totalBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
			}
			if (json.has("usedBytes")) {
				usedGB = json.get("usedBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
			}
			if (json.has("availableBytes")) {
				availableGB = json.get("availableBytes").getAsDouble() / (1024.0 * 1024.0 * 1024.0);
			}
			
			if (json.has("usagePercent")) {
				usagePercent = json.get("usagePercent").getAsDouble();
			}
			
			// Inode fields not provided by Calimero
			if (json.has("inodes")) {
				inodes = json.get("inodes").getAsLong();
			}
			if (json.has("inodesUsed")) {
				inodesUsed = json.get("inodesUsed").getAsLong();
			}
			if (json.has("inodesPercent")) {
				inodesPercent = json.get("inodesPercent").getAsDouble();
			}
		} catch (final Exception e) {
			LOGGER.error("Error parsing CDiskInfo from JSON: {}", e.getMessage());
		}
	}
	
	@Override
	protected String toJson() {
		// Disk info is read-only from server
		return "{}";
	}
	
	public String getFilesystem() { return filesystem; }
	public String getType() { return type; }
	public String getMountPoint() { return mountPoint; }
	public Double getTotalGB() { return totalGB; }
	public Double getUsedGB() { return usedGB; }
	public Double getAvailableGB() { return availableGB; }
	public Double getUsagePercent() { return usagePercent; }
	public Long getInodes() { return inodes; }
	public Long getInodesUsed() { return inodesUsed; }
	public Double getInodesPercent() { return inodesPercent; }
	
	/**
	 * Get disk usage display string.
	 * @return Formatted usage (e.g., "250.5 GB / 500.0 GB (50.1%)")
	 */
	public String getUsageDisplay() {
		return String.format("%.1f GB / %.1f GB (%.1f%%)", usedGB, totalGB, usagePercent);
	}
	
	/**
	 * Get available space display string.
	 * @return Formatted available (e.g., "249.5 GB free")
	 */
	public String getAvailableDisplay() {
		return String.format("%.1f GB free", availableGB);
	}
	
	/**
	 * Get inode usage display string.
	 * @return Formatted inodes (e.g., "16384000 / 32768000 (50.0%)")
	 */
	public String getInodesDisplay() {
		return String.format("%d / %d (%.1f%%)", inodesUsed, inodes, inodesPercent);
	}
	
	/**
	 * Check if disk usage is critical (> 90%).
	 * @return true if usage exceeds 90%
	 */
	public boolean isCritical() {
		return usagePercent != null && usagePercent > 90.0;
	}
	
	/**
	 * Check if disk usage is warning level (> 75%).
	 * @return true if usage exceeds 75%
	 */
	public boolean isWarning() {
		return usagePercent != null && usagePercent > 75.0 && !isCritical();
	}
}
