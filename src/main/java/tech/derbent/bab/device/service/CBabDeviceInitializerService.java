package tech.derbent.bab.device.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNodeCAN;
import tech.derbent.bab.node.domain.CBabNodeEthernet;
import tech.derbent.bab.node.domain.CBabNodeModbus;
import tech.derbent.bab.node.domain.CBabNodeROS;
import tech.derbent.bab.node.service.CBabNodeService;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.registry.CEntityRegistry;

/**
 * Initializer service for BAB device sample data.
 * Following Derbent pattern: Initializer service with static initializeSample method.
 */
@Component
@Profile("bab")
public class CBabDeviceInitializerService extends CInitializerServiceBase {

	private static final Logger LOGGER = LoggerFactory.getLogger(CBabDeviceInitializerService.class);
	private static final Class<?> clazz = CBabDevice.class;

	/**
	 * Initialize sample BAB device with nodes.
	 * 
	 * @param company the company to create device for
	 * @param minimal if true, create minimal sample data
	 */
	public static void initializeSample(final CCompany company, final boolean minimal) throws Exception {
		LOGGER.info("Initializing BAB sample data for company: {}", company.getName());

		final CBabDeviceService deviceService = (CBabDeviceService) CSpringContext.getBean(
				CEntityRegistry.getServiceClassForEntity(clazz));
		final CBabNodeService nodeService = (CBabNodeService) CSpringContext.getBean(CBabNodeService.class);

		// Create or get unique device for company
		CBabDevice device = deviceService.getUniqueDevice().orElse(null);

		if (device == null) {
			device = new CBabDevice("IoT Gateway Device", company);
			device.setSerialNumber("BAB-GW-" + System.currentTimeMillis());
			device.setFirmwareVersion("1.0.0");
			device.setHardwareRevision("Rev A");
			device.setDeviceStatus("Online");
			device.setIpAddress("192.168.1.100");
			device.setMacAddress("00:1A:2B:3C:4D:5E");
			device.setDescription("Primary IoT gateway device for CAN bus to Ethernet/ROS protocol conversion");
			device = deviceService.save(device);
			LOGGER.info("Created sample device: {}", device.getName());
		} else {
			LOGGER.info("Device already exists: {}", device.getName());
		}

		// Create sample nodes if device has no nodes
		if (nodeService.countByDevice(device) == 0) {
			createSampleNodes(device, nodeService, minimal);
		}
	}

	private static void createSampleNodes(final CBabDevice device, final CBabNodeService nodeService, final boolean minimal) {
		// Create CAN node
		final CBabNodeCAN canNode = new CBabNodeCAN("CAN Bus Interface", device);
		canNode.setDescription("Primary CAN bus interface for vehicle communication");
		canNode.setBitrate(500000);
		canNode.setSamplePoint(0.875);
		canNode.setInterfaceName("can0");
		canNode.setEnabled(true);
		canNode.setNodeStatus("Active");
		nodeService.save(canNode);
		LOGGER.info("Created CAN node: {}", canNode.getName());

		// Create Ethernet node
		final CBabNodeEthernet ethNode = new CBabNodeEthernet("Ethernet Interface", device);
		ethNode.setDescription("Primary Ethernet interface for network communication");
		ethNode.setInterfaceName("eth0");
		ethNode.setIpAddress("192.168.1.100");
		ethNode.setSubnetMask("255.255.255.0");
		ethNode.setGateway("192.168.1.1");
		ethNode.setDhcpEnabled(false);
		ethNode.setEnabled(true);
		ethNode.setNodeStatus("Active");
		nodeService.save(ethNode);
		LOGGER.info("Created Ethernet node: {}", ethNode.getName());

		if (!minimal) {
			// Create Modbus node
			final CBabNodeModbus modbusNode = new CBabNodeModbus("Modbus RTU Interface", device);
			modbusNode.setDescription("Modbus RTU interface for industrial sensors");
			modbusNode.setProtocolType("RTU");
			modbusNode.setSlaveId(1);
			modbusNode.setBaudRate(9600);
			modbusNode.setParity("None");
			modbusNode.setEnabled(false);
			modbusNode.setNodeStatus("Inactive");
			nodeService.save(modbusNode);
			LOGGER.info("Created Modbus node: {}", modbusNode.getName());

			// Create ROS node
			final CBabNodeROS rosNode = new CBabNodeROS("ROS Bridge", device);
			rosNode.setDescription("ROS bridge for robot communication");
			rosNode.setRosMasterUri("http://192.168.1.101:11311");
			rosNode.setNodeName("bab_gateway");
			rosNode.setNamespace("/gateway");
			rosNode.setRosVersion("ROS1");
			rosNode.setEnabled(false);
			rosNode.setNodeStatus("Inactive");
			nodeService.save(rosNode);
			LOGGER.info("Created ROS node: {}", rosNode.getName());
		}
	}
}
