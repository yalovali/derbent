package tech.derbent.bab.policybase.actionmask.domain;

import java.util.Map;
import java.util.Set;
import org.springframework.context.annotation.Profile;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.derbent.api.annotations.AMetaData;
import tech.derbent.api.entity.domain.CEntityNamed;
import tech.derbent.api.registry.IEntityRegistrable;
import tech.derbent.bab.policybase.node.domain.CBabNodeEntity;
import tech.derbent.bab.utils.CJsonSerializer.EJsonScenario;

/** Abstract base for destination-node action mask definitions. */
@Entity
@Table (name = "cbab_policy_action_mask", uniqueConstraints = {
		@UniqueConstraint (columnNames = {
				"parent_node_id", "name"
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

	@ManyToOne (fetch = FetchType.EAGER, optional = false)
	@JoinColumn (name = "parent_node_id", nullable = false)
	@OnDelete (action = OnDeleteAction.CASCADE)
	@AMetaData (
			displayName = "Destination Node", required = true, readOnly = true,
			description = "Destination node that owns this action mask", hidden = true, dataProviderBean = "none"
	)
	@JsonIgnore
	private CBabNodeEntity<?> parentNode;

	@Column (name = "execution_order", nullable = false)
	@AMetaData (
			displayName = "Execution Order", required = false, readOnly = false,
			description = "Order priority among masks of same destination node", hidden = false
	)
	private Integer executionOrder = 0;

	@Column (name = "mask_configuration_json", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Mask Configuration JSON", required = false, readOnly = false,
			description = "Large configuration payload for this mask", hidden = false
	)
	private String maskConfigurationJson = "{}";

	@Column (name = "mask_template_json", columnDefinition = "TEXT")
	@AMetaData (
			displayName = "Mask Template JSON", required = false, readOnly = false,
			description = "Large template payload for this mask", hidden = false
	)
	private String maskTemplateJson = "{}";

	/** Default constructor for JPA. */
	protected CBabPolicyActionMaskBase() {
		// JPA constructor
	}

	protected CBabPolicyActionMaskBase(final Class<EntityClass> clazz, final String name, final CBabNodeEntity<?> parentNode) {
		super(clazz, name);
		setParentNode(parentNode);
	}

	private static Map<String, Set<String>> createExcludedFieldMap_BabPolicy() {
		return Map.of("CBabPolicyActionMaskBase", Set.of("parentNode"));
	}

	public Integer getExecutionOrder() { return executionOrder; }

	@Override
	public Map<String, Set<String>> getExcludedFieldMapForScenario(final EJsonScenario scenario) {
		return mergeExcludedFieldMaps(super.getExcludedFieldMapForScenario(scenario),
				getScenarioExcludedFieldMap(scenario, Map.of(), EXCLUDED_FIELDS_BAB_POLICY));
	}

	public abstract String getMaskKind();

	public String getMaskConfigurationJson() { return maskConfigurationJson; }

	public String getMaskTemplateJson() { return maskTemplateJson; }

	public abstract Class<? extends CBabNodeEntity<?>> getAllowedNodeType();

	public CBabNodeEntity<?> getParentNode() { return parentNode; }

	public void setExecutionOrder(final Integer executionOrder) {
		this.executionOrder = executionOrder;
		updateLastModified();
	}

	public void setMaskConfigurationJson(final String maskConfigurationJson) {
		this.maskConfigurationJson = maskConfigurationJson;
		updateLastModified();
	}

	public void setMaskTemplateJson(final String maskTemplateJson) {
		this.maskTemplateJson = maskTemplateJson;
		updateLastModified();
	}

	public void setParentNode(final CBabNodeEntity<?> parentNode) {
		this.parentNode = parentNode;
		updateLastModified();
	}
}
