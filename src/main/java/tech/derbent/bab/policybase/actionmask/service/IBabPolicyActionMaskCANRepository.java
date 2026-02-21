package tech.derbent.bab.policybase.actionmask.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskCAN;

@Profile ("bab")
public interface IBabPolicyActionMaskCANRepository extends IPolicyActionMaskEntityRepository<CBabPolicyActionMaskCAN> {}
