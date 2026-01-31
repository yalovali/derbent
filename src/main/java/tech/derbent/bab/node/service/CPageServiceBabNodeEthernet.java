package tech.derbent.bab.node.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.node.domain.CBabNodeEthernet;

/**
 * Page service for CBabNodeEthernet entity.
 * Following Derbent pattern: Concrete page service for Ethernet node views.
 * Provides UI action handling and component lifecycle for Ethernet node views.
 */
public class CPageServiceBabNodeEthernet extends CPageServiceBabNode<CBabNodeEthernet> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CPageServiceBabNodeEthernet.class);
	private static final long serialVersionUID = 1L;

	public CPageServiceBabNodeEthernet(final IPageServiceImplementer<CBabNodeEthernet> view) {
		super(view);
		LOGGER.debug("CPageServiceBabNodeEthernet initialized for view: {}", view.getClass().getSimpleName());
	}
}
