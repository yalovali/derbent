package tech.derbent.bab.node.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.bab.device.domain.CBabDevice;

/** CBabNodeCAN - CAN Bus communication node. Following Derbent pattern: Concrete entity with specific fields. */
@Entity
@Table (name = "cbab_node_can")
public class CBabNodeCAN extends CBabNode {

	public static final String DEFAULT_COLOR = "#FF5722";
	public static final String DEFAULT_ICON = "vaadin:car";
	public static final String ENTITY_TITLE_PLURAL = "CAN Nodes";
	public static final String ENTITY_TITLE_SINGULAR = "CAN Node";
	private static final Logger LOGGER = LoggerFactory.getLogger(CBabNodeCAN.class);
	private static final long serialVersionUID = 1L;
	public static final String VIEW_NAME = "CAN Node Configuration";
	@Column (name = "bitrate", nullable = true)
	@AMetaData (displayName = "Bitrate", required = false, readOnly = false, description = "CAN bus bitrate (e.g., 500000)", hidden = false)
	private Integer bitrate;
	@Column (name = "sample_point", nullable = true)
	@AMetaData (displayName = "Sample Point", required = false, readOnly = false, description = "CAN bus sample point (0.0-1.0)", hidden = false)
	private Double samplePoint;
	@Column (name = "interface_name", nullable = true, length = 50)
	@Size (max = 50)
	@AMetaData (
			displayName = "Interface", required = false, readOnly = false, description = "Linux CAN interface name (e.g., can0)", hidden = false,
			maxLength = 50
	)
	private String interfaceName;

	/** Default constructor for JPA. */
	public CBabNodeCAN() {
		super();
	}

	public CBabNodeCAN(final String name, final CBabDevice device) {
		super(CBabNodeCAN.class, name, device, "CAN");
	}

	// Getters and Setters
	public Integer getBitrate() { return bitrate; }

	public String getInterfaceName() { return interfaceName; }

	public Double getSamplePoint() { return samplePoint; }

	@Override
	protected void initializeDefaults() {
		super.initializeDefaults();
		if (bitrate == null) {
			bitrate = 500000;
		}
		if (samplePoint == null) {
			samplePoint = 0.875;
		}
		if (interfaceName == null) {
			interfaceName = "can0";
		}
	}

	public void setBitrate(final Integer bitrate) {
		this.bitrate = bitrate;
		updateLastModified();
	}

	public void setInterfaceName(final String interfaceName) {
		this.interfaceName = interfaceName;
		updateLastModified();
	}

	public void setSamplePoint(final Double samplePoint) {
		this.samplePoint = samplePoint;
		updateLastModified();
	}
}
