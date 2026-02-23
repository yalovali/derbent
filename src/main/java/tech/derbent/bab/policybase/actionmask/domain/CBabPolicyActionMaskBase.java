package tech.derbent.bab.policybase.actionmask.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.action.domain.CBabPolicyAction;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

/** Abstract base for destination-node action mask definitions. */
@Entity
@Table (name = "cbab_policy_action_mask", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"policy_action_id", "name"
		})
})
@AttributeOverride (name = "id", column = @Column (name = "bab_policy_action_mask_id"))
@Inheritance (strategy = InheritanceType.JOINED)
@DiscriminatorColumn (name = "mask_kind", discriminatorType = DiscriminatorType.STRING)
@Profile ("bab")
@JsonFilter ("babScenarioFilter")
public abstract class CBabPolicyActionMaskBase<EntityClass extends CBabPolicyActionMaskBase<EntityClass>> extends CEntityNamed<EntityClass>
		implements IEntityRegistrable {

	private static final Map<String, Set<String>> EXCLUDED_FIELDS_BAB_POLICY = createExcludedFieldMap_BabPolicy();

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		return Map.of("CBabPolicyActionMaskBase", Set.of("policyAction", "active", "id"));
	}

	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false, description = "Order priority among masks of same destination node",
			hidden = false
	)
	private Integer executionOrder = 0;
	@Column (name = "output_method", length = 60)
	@AMetaData (
			displayName = "Output Method", required = false, readOnly = false,
			description = "Output method applied by this action mask", hidden = false, maxLength = 60, dataProviderBean = "pageservice",
			dataProviderMethod = "getComboValuesOfOutputMethod"
	)
	private String outputMethod = "";
	@Column (name = "output_action_mappings", length = 16000)
	@AMetaData (
			displayName = "Output Action Mappings", required = false, readOnly = false,
			description = "Mappings from source outputs to destination protocol variables", hidden = false, dataProviderBean = "pageservice",
			createComponentMethod = "createComponentOutputActionMappings", captionVisible = false
	)
	private List<ROutputActionMapping> outputActionMappings = new ArrayList<>();

	@Column (name = "policy_action_id", nullable = false)
	private Long policyActionId;

	@Transient
	@AMetaData (
			displayName = "Policy Action", required = true, readOnly = true, description = "Policy action that owns this action mask",
			hidden = true, dataProviderBean = "none", hideNavigateToButton = true, hideEditButton = true
	)
	@JsonIgnore
	private transient CBabPolicyAction policyAction;

	/** Default constructor for JPA. */
	protected CBabPolicyActionMaskBase() {
		// JPA constructor
	}

	protected CBabPolicyActionMaskBase(final Class<EntityClass> clazz, final String name, final CBabPolicyAction policyAction) {
		super(clazz, name);
		setPolicyAction(policyAction);
	}

	public abstract Class<? extends CBabNodeEntity<?>> getAllowedNodeType();

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	public Integer getExecutionOrder() { return executionOrder; }

	public abstract String getMaskKind();

	public List<ROutputActionMapping> getOutputActionMappings() { return outputActionMappings; }

	public String getOutputMethod() { return outputMethod; }

	public CBabPolicyAction getPolicyAction() { return policyAction; }

	public Long getPolicyActionId() { return policyActionId; }

	public CBabNodeEntity<?> getDestinationNode() {
		return policyAction != null ? policyAction.getDestinationNode() : null;
	}

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setOutputActionMappings(final List<ROutputActionMapping> outputActionMappings) {
		if (outputActionMappings == null || outputActionMappings.isEmpty()) {
			this.outputActionMappings = new ArrayList<>();
			updateLastModified();
			return;
		}
		final LinkedHashMap<String, ROutputActionMapping> uniqueMappings = new LinkedHashMap<>();
		outputActionMappings.stream().filter(mapping -> mapping != null && !mapping.outputName().isBlank())
				.forEach(mapping -> uniqueMappings.putIfAbsent(mapping.outputName().toLowerCase(Locale.ROOT), mapping));
		this.outputActionMappings = uniqueMappings.values().stream().collect(Collectors.toCollection(ArrayList::new));
		updateLastModified();
	}

	public void setOutputMethod(final String outputMethod) {
		this.outputMethod = outputMethod == null ? "" : outputMethod.trim();
		updateLastModified();
	}

	public void setPolicyAction(final CBabPolicyAction policyAction) {
		final Long nextPolicyActionId = policyAction != null ? policyAction.getId() : null;
		final boolean policyActionIdChanged = !Objects.equals(policyActionId, nextPolicyActionId);
		this.policyAction = policyAction;
		policyActionId = nextPolicyActionId;
		if (policyActionIdChanged) {
			updateLastModified();
		}
	}
}
