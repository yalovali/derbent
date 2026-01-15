package tech.derbent.bab.device.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.page.service.CPageEntityService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.domain.CDetailSection;
import tech.derbent.api.screens.domain.CGridEntity;
import tech.derbent.api.screens.service.CDetailLinesService;
import tech.derbent.api.screens.service.CDetailSectionService;
import tech.derbent.api.screens.service.CGridEntityService;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.api.screens.service.CInitializerServiceNamedEntity;
import tech.derbent.api.utils.Check;
import tech.derbent.bab.device.domain.CBabDevice;
import tech.derbent.bab.node.domain.CBabNodeCAN;
import tech.derbent.bab.node.domain.CBabNodeEthernet;
import tech.derbent.bab.node.domain.CBabNodeModbus;
import tech.derbent.bab.node.domain.CBabNodeROS;
import tech.derbent.bab.node.service.CBabNodeService;

public class CBabDeviceInitializerService extends CInitializerServiceBase {

static final Class<?> clazz = CBabDevice.class;
private static final Logger LOGGER = LoggerFactory.getLogger(CBabDeviceInitializerService.class);
private static final String menuOrder = Menu_Order_SYSTEM + ".1";
private static final String menuTitle = MenuTitle_SYSTEM + ".Devices";
private static final String pageDescription = "IoT gateway device management and configuration";
private static final String pageTitle = "Device Management";
private static final boolean showInQuickToolbar = true;

public static CDetailSection createBasicView(final CProject project) throws Exception {
Check.notNull(project, "project cannot be null");
try {
final CDetailSection detailSection = createBaseScreenEntity(project, clazz);
CInitializerServiceNamedEntity.createBasicView(detailSection, clazz, project, true);

detailSection.addScreenLine(CDetailLinesService.createSection("Device Information"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "serialNumber"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "firmwareVersion"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "hardwareRevision"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "deviceStatus"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastSeen"));

detailSection.addScreenLine(CDetailLinesService.createSection("Network Configuration"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "ipAddress"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "macAddress"));

detailSection.addScreenLine(CDetailLinesService.createSection("System"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "company"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdBy"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "active"));

detailSection.addScreenLine(CDetailLinesService.createSection("Audit"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "createdDate"));
detailSection.addScreenLine(CDetailLinesService.createLineFromDefaults(clazz, "lastModifiedDate"));

detailSection.debug_printScreenInformation();
return detailSection;
} catch (final Exception e) {
LOGGER.error("Error creating device view.", e);
throw e;
}
}

public static CGridEntity createGridEntity(final CProject project) {
final CGridEntity grid = createBaseGridEntity(project, clazz);
grid.setColumnFields(List.of("id", "name", "description", "serialNumber", "firmwareVersion", 
"hardwareRevision", "deviceStatus", "lastSeen", "ipAddress", "macAddress", 
"company", "createdBy", "active", "createdDate", "lastModifiedDate"));
return grid;
}

public static void initialize(final CProject project, final CGridEntityService gridEntityService,
final CDetailSectionService detailSectionService, final CPageEntityService pageEntityService) throws Exception {
final CDetailSection detailSection = createBasicView(project);
final CGridEntity grid = createGridEntity(project);
initBase(clazz, project, gridEntityService, detailSectionService, pageEntityService, detailSection, grid, 
menuTitle, pageTitle, pageDescription, showInQuickToolbar, menuOrder);
}

public static void initializeSample(final CProject project, final boolean minimal) throws Exception {
final CCompany company = project.getCompany();
LOGGER.info("Initializing BAB sample data for company: {}", company.getName());

final CBabDeviceService deviceService = (CBabDeviceService) CSpringContext.getBean(
CEntityRegistry.getServiceClassForEntity(clazz));
final CBabNodeService nodeService = (CBabNodeService) CSpringContext.getBean(CBabNodeService.class);

// Use overloaded method that accepts company parameter (no session context during initialization)
CBabDevice device = deviceService.getUniqueDevice(company).orElse(null);

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

if (nodeService.countByDevice(device) == 0) {
createSampleNodes(device, nodeService, minimal);
}
}

private static void createSampleNodes(final CBabDevice device, final CBabNodeService nodeService, final boolean minimal) {
final CBabNodeCAN canNode = new CBabNodeCAN("CAN Bus Interface", device);
canNode.setDescription("Primary CAN bus interface for vehicle communication");
canNode.setBitrate(500000);
canNode.setSamplePoint(0.875);
canNode.setInterfaceName("can0");
canNode.setEnabled(true);
canNode.setNodeStatus("Active");
nodeService.save(canNode);
LOGGER.info("Created CAN node: {}", canNode.getName());

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
