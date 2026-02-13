package tech.derbent.bab.policybase.action.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;

/**
 * CPageServiceBabPolicyAction - Page service for BAB policy action management.
 * 
 * Provides UI page services for action entities including:
 * - Dynamic page routing
 * - Grid and detail view management
 * - Component factory methods
 * - Page navigation support
 * 
 * Layer: Service (MVC)
 * Active when: 'bab' profile is active
 * Following Derbent pattern: Page service extending CPageServiceDynamicPage
 */
@Service
@Profile("bab")
public class CPageServiceBabPolicyAction extends CPageServiceDynamicPage<CBabPolicyAction> {

    public CPageServiceBabPolicyAction(final IPageServiceImplementer<CBabPolicyAction> view) {
        super(view);
    }
}