package tech.derbent.bab.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.node.domain.CBabNode;

/**
 * Page service for CBabNode hierarchy.
 * Following Derbent pattern: Page service extends CPageServiceDynamicPage.
 * Abstract base for concrete node type page services (CAN, Ethernet, Modbus, ROS).
 */
public abstract class CPageServiceBabNode<NodeType extends CBabNode<NodeType>> extends CPageServiceDynamicPage<NodeType> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNode.class);
	private static final long serialVersionUID = 1L;

	protected CPageServiceBabNode(final IPageServiceImplementer<NodeType> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNode initialized for view: {}", view.getClass().getSimpleName());
	}
}
