package tech.derbent.app.attachments.view;

import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.app.attachments.service.CAttachmentService;
import tech.derbent.base.session.service.ISessionService;

/**
 * Factory for creating attachment components for any entity type.
 * 
 * This factory allows entities to have attachment support without duplicating
 * createAttachmentsComponent() methods in each page service.
 * 
 * Usage in entity @AMetaData:
 * <pre>
 * {@literal @}AMetaData(
 *     displayName = "Attachments",
 *     required = false,
 *     description = "File attachments",
 *     createComponentMethodBean = "CAttachmentComponentFactory",
 *     createComponentMethod = "createComponent"
 * )
 * private List<CAttachment> attachments = new ArrayList<>();
 * </pre>
 * 
 * The form builder will invoke:
 * CAttachmentComponentFactory.createComponent(entity, "attachments")
 * 
 * Layer: View (MVC)
 */
@Component("CAttachmentComponentFactory")
public class CAttachmentComponentFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CAttachmentComponentFactory.class);
	
	@Autowired
	private CAttachmentService attachmentService;
	
	@Autowired
	private ISessionService sessionService;
	
	/**
	 * Create an attachment list component for any entity.
	 * 
	 * This method is called by the form builder via reflection when rendering
	 * entity detail forms that have the attachments field with proper @AMetaData.
	 * 
	 * @param <T> the entity type
	 * @param masterEntity the parent entity (Activity, Risk, Meeting, Sprint, Project, User, etc.)
	 * @param fieldName the field name (typically "attachments")
	 * @return configured attachment list component
	 */
	public <T extends CEntityDB<T>> CComponentListAttachments<T> createComponent(
			final T masterEntity, 
			final String fieldName) {
		
		Objects.requireNonNull(masterEntity, "Master entity cannot be null");
		Objects.requireNonNull(fieldName, "Field name cannot be null");
		
		LOGGER.debug("Creating attachment component for {} field '{}'", 
				masterEntity.getClass().getSimpleName(), fieldName);
		
		// Create component with entity type
		@SuppressWarnings("unchecked")
		final Class<T> entityClass = (Class<T>) masterEntity.getClass();
		
		final CComponentListAttachments<T> component = new CComponentListAttachments<>(
				entityClass,
				attachmentService,
				sessionService
		);
		
		// Set master entity
		component.setMasterEntity(masterEntity);
		
		LOGGER.debug("Created attachment component for {}", entityClass.getSimpleName());
		
		return component;
	}
	
	/**
	 * Alternative method signature for compatibility.
	 * Some form builders may call with just the entity.
	 */
	public <T extends CEntityDB<T>> CComponentListAttachments<T> createComponent(
			final T masterEntity) {
		return createComponent(masterEntity, "attachments");
	}
}
