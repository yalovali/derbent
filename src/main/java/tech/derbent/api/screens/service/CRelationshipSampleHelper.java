package tech.derbent.api.screens.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entity.domain.CEntityDB;
import tech.derbent.api.entityOfCompany.domain.CEntityOfCompany;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.service.CAbstractService;
import tech.derbent.base.users.domain.CUser;
import tech.derbent.base.users.service.CUserService;
import tech.derbent.plm.attachments.domain.IHasAttachments;
import tech.derbent.plm.comments.domain.CComment;
import tech.derbent.plm.comments.domain.IHasComments;
import tech.derbent.plm.comments.service.CCommentInitializerService;
import tech.derbent.plm.links.domain.CLink;
import tech.derbent.plm.links.domain.IHasLinks;

/**
 * Utility service for adding sample relationships to entities during initialization.
 * Provides reusable methods for adding comments, attachments, and links.
 */
public final class CRelationshipSampleHelper {

	private static final Logger LOGGER = LoggerFactory.getLogger(CRelationshipSampleHelper.class);

	/**
	 * Add sample comments to an entity.
	 * 
	 * @param entity the entity to add comments to (must implement IHasComments)
	 * @param commentTexts array of comment texts
	 * @param importantFlags array indicating which comments are important (optional)
	 */
	public static void addSampleComments(final CEntityDB<?> entity, final String[] commentTexts, 
			final boolean[] importantFlags) {
		if (!(entity instanceof IHasComments)) {
			LOGGER.warn("Entity {} does not implement IHasComments", entity.getClass().getSimpleName());
			return;
		}

		try {
			final IHasComments hasComments = (IHasComments) entity;
			final List<CComment> comments = CCommentInitializerService.createSampleComments(commentTexts, importantFlags);
			hasComments.getComments().addAll(comments);
			LOGGER.debug("Added {} comments to {}", comments.size(), entity.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.warn("Error adding comments to {}: {}", entity.getClass().getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Add sample comments to an entity (all non-important).
	 * 
	 * @param entity the entity to add comments to (must implement IHasComments)
	 * @param commentTexts array of comment texts
	 */
	public static void addSampleComments(final CEntityDB<?> entity, final String... commentTexts) {
		addSampleComments(entity, commentTexts, null);
	}

	/**
	 * Add sample attachments to an entity.
	 * 
	 * @param entity the entity to add attachments to (must implement IHasAttachments)
	 * @param project the project (for getting company context)
	 * @param attachmentInfos array of attachment info: [filename, description, size, mimeType]
	 */
	public static void addSampleAttachments(final CEntityDB<?> entity, final CProject<?> project, 
			final String[][] attachmentInfos) {
		if (!(entity instanceof IHasAttachments)) {
			LOGGER.warn("Entity {} does not implement IHasAttachments", entity.getClass().getSimpleName());
			return;
		}
		
		if (!(entity instanceof CEntityOfCompany)) {
			LOGGER.warn("Entity {} does not extend CEntityOfCompany", entity.getClass().getSimpleName());
			return;
		}

		try {
			final IHasAttachments hasAttachments = (IHasAttachments) entity;
			final CUserService userService = CSpringContext.getBean(CUserService.class);
			final CUser user = userService.getRandom(project.getCompany());
			
			if (user == null) {
				LOGGER.warn("No users available for creating attachments");
				return;
			}

			for (final String[] info : attachmentInfos) {
				final String filename = info[0];
				final String description = info.length > 1 ? info[1] : "";
				final long fileSize = info.length > 2 ? Long.parseLong(info[2]) : 10240L;
				final String contentPath = "samples/" + filename;

				final tech.derbent.plm.attachments.domain.CAttachment attachment = 
					new tech.derbent.plm.attachments.domain.CAttachment(filename, fileSize, contentPath, user);
				attachment.setDescription(description);
				attachment.setCompany(project.getCompany());
				hasAttachments.getAttachments().add(attachment);
			}
			
			LOGGER.debug("Added {} attachments to {}", attachmentInfos.length, entity.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.warn("Error adding attachments to {}: {}", entity.getClass().getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Add a link from source entity to a random target entity.
	 * 
	 * @param sourceEntity the source entity (must implement IHasLinks)
	 * @param project the project (for finding random entities)
	 * @param targetEntityClass the target entity class to link to
	 * @param targetServiceClass the service class for the target entity
	 * @param linkType the type of link (e.g., "Related", "Depends On")
	 * @param linkDescription description of the link
	 */
	public static void addRandomLink(final CEntityDB<?> sourceEntity, final CProject<?> project,
			final Class<?> targetEntityClass, final Class<?> targetServiceClass,
			final String linkType, final String linkDescription) {
		if (!(sourceEntity instanceof IHasLinks)) {
			LOGGER.warn("Entity {} does not implement IHasLinks", sourceEntity.getClass().getSimpleName());
			return;
		}

		try {
			final IHasLinks hasLinks = (IHasLinks) sourceEntity;
			final CAbstractService<?> targetService = (CAbstractService<?>) CSpringContext.getBean(targetServiceClass);
			
			// Get random target entity from the same project
			final Object randomTarget = targetService.getClass()
				.getMethod("getRandom", CProject.class)
				.invoke(targetService, project);
			
			if (randomTarget != null && randomTarget instanceof CEntityDB) {
				final CEntityDB<?> targetEntity = (CEntityDB<?>) randomTarget;
				
				final CLink link = new CLink(
					sourceEntity.getClass().getSimpleName(),
					sourceEntity.getId(),
					targetEntityClass.getSimpleName(),
					targetEntity.getId(),
					linkType
				);
				link.setDescription(linkDescription);
				link.setCompany(project.getCompany());
				hasLinks.getLinks().add(link);
				
				LOGGER.debug("Linked {} '{}' to {} '{}' (type: {})", 
					sourceEntity.getClass().getSimpleName(), sourceEntity.getId(),
					targetEntityClass.getSimpleName(), targetEntity.getId(), linkType);
			}
		} catch (final Exception e) {
			LOGGER.debug("Could not add link from {} to {}: {}", 
				sourceEntity.getClass().getSimpleName(), targetEntityClass.getSimpleName(), e.getMessage());
		}
	}

	/**
	 * Add sample relationships (comments, attachments, links) to an entity in one call.
	 * 
	 * @param entity the entity
	 * @param service the service for saving the entity
	 * @param project the project
	 * @param commentTexts optional comment texts (can be null)
	 * @param importantFlags optional important flags for comments (can be null)
	 * @param attachmentInfos optional attachment info arrays (can be null)
	 */
	@SuppressWarnings("unchecked")
	public static <T extends CEntityDB<?>> void addAllRelationships(final T entity, 
			final CAbstractService<T> service, final CProject<?> project,
			final String[] commentTexts, final boolean[] importantFlags,
			final String[][] attachmentInfos) {
		
		if (commentTexts != null && commentTexts.length > 0) {
			addSampleComments(entity, commentTexts, importantFlags);
		}
		
		if (attachmentInfos != null && attachmentInfos.length > 0) {
			addSampleAttachments(entity, project, attachmentInfos);
		}
		
		// Save entity with relationships
		try {
			service.save((T) entity);
			LOGGER.debug("Saved {} with relationships", entity.getClass().getSimpleName());
		} catch (final Exception e) {
			LOGGER.warn("Error saving {} with relationships: {}", 
				entity.getClass().getSimpleName(), e.getMessage());
		}
	}

	private CRelationshipSampleHelper() {
		// Utility class - no instantiation
	}
}
