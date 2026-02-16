package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterROS;

/** Repository for ROS policy filters. */
@Profile ("bab")
public interface IBabPolicyFilterROSRepository extends IPolicyFilterEntityRepository<CBabPolicyFilterROS> {
}
