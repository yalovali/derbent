package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import tech.derbent.api.services.pageservice.CPageServiceDynamicPage;
import tech.derbent.api.services.pageservice.IPageServiceImplementer;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilter;

/**
 * CPageServiceBabPolicyFilter - Page service for BAB policy filter management.
 * 
 * Provides UI page services for filter entities including:
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
public class CPageServiceBabPolicyFilter extends CPageServiceDynamicPage<CBabPolicyFilter> {

    public CPageServiceBabPolicyFilter(final IPageServiceImplementer<CBabPolicyFilter> view) {
        super(view);
    }
}