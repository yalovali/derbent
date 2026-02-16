package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;

/**
 * @deprecated Replaced by typed repositories:
 *             {@link IBabPolicyFilterCSVRepository}, {@link IBabPolicyFilterCANRepository},
 *             {@link IBabPolicyFilterROSRepository}.
 */
@Deprecated
@Profile ("bab")
public interface IBabPolicyFilterRepository {
}
