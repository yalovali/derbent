package tech.derbent.bab.policybase.trigger.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.trigger.domain.CBabPolicyTrigger;

/**
 * CPageServiceBabPolicyTrigger - Page service for BAB policy trigger management.
 * 
 * Provides UI page services for trigger entities including:
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
public class CPageServiceBabPolicyTrigger extends CPageServiceDynamicPage<CBabPolicyTrigger> {

    public CPageServiceBabPolicyTrigger(final IPageServiceImplementer<CBabPolicyTrigger> view) {
        super(view);
    }
}