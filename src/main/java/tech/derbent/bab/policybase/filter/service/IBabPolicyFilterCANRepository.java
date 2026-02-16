package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCAN;

/** Repository for CAN policy filters. */
@Profile ("bab")
public interface IBabPolicyFilterCANRepository extends IPolicyFilterEntityRepository<CBabPolicyFilterCAN> {
}
