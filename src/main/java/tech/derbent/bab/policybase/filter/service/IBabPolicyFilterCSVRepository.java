package tech.derbent.bab.policybase.filter.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.filter.domain.CBabPolicyFilterCSV;

/** Repository for CSV policy filters. */
@Profile ("bab")
public interface IBabPolicyFilterCSVRepository extends IPolicyFilterEntityRepository<CBabPolicyFilterCSV> {
}
