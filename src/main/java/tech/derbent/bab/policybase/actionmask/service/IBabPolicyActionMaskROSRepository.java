package tech.derbent.bab.policybase.actionmask.service;

import org.springframework.context.annotation.Profile;
import tech.derbent.bab.policybase.actionmask.domain.CBabPolicyActionMaskROS;

@Profile ("bab")
public interface IBabPolicyActionMaskROSRepository extends IPolicyActionMaskEntityRepository<CBabPolicyActionMaskROS> {}
