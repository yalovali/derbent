package tech.derbent.api.domains;

import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.derbent.api.entity.service.CAbstractService;
import tech.derbent.api.entityOfProject.domain.CProjectItem;
import tech.derbent.api.utils.Check;
import tech.derbent.base.session.service.ISessionService;

/** COneToOneRelationServiceBase - Generic base service for all one-to-one composition pattern entities.
 * <p>
 * This abstract service provides common operations for managing one-to-one owned entities. Subclasses should extend this and add domain-specific
 * operations.
 * </p>
 * <p>
 * <strong>GENERIC OPERATIONS PROVIDED:</strong>
 * </p>
 * <ul>
 * <li>Validation helpers for null checks and instance checks</li>
 * <li>Common logging patterns</li>
 * <li>Owner item access and manipulation</li>
 * </ul>
 * <p>
 * <strong>IMPLEMENTATION PATTERN:</strong>
 * </p>
 *
 * <pre>
 * &#64;Service
 * public class CMyRelationService extends COneToOneRelationServiceBase&lt;CMyRelation&gt; {
 * 
 * 	private final IMyRelationRepository repository;
 * 
 * 	public CMyRelationService(final IMyRelationRepository repository, final Clock clock, final ISessionService sessionService) {
 * 		super(repository, clock, sessionService);
 * 		this.repository = repository;
 * 	}
 * 
 * 	&#64;Override
 * 	protected Class&lt;CMyRelation&gt; getEntityClass() { return CMyRelation.class; }
 * 
 * 	// Add domain-specific methods here
 * 	public void setMyProperty(CProjectItem&lt;?&gt; entity, String value) {
 * 		validateOwnership(entity, IHasMyRelation.class);
 * 		IHasMyRelation hasRelation = (IHasMyRelation) entity;
 * 		hasRelation.getMyRelation().setMyProperty(value);
 * 	}
 * }
 * </pre>
 *
 * @param <T> The concrete type of the one-to-one relation entity
 * @author Derbent Framework */
public abstract class COneToOneRelationServiceBase<T extends COneToOneRelationBase<T>> extends CAbstractService<T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(COneToOneRelationServiceBase.class);

	/** Constructor for subclasses.
	 * @param repository     The repository for the relation entity
	 * @param clock          The clock for timestamps
	 * @param sessionService The session service */
	protected COneToOneRelationServiceBase(final tech.derbent.api.entity.service.IAbstractRepository<T> repository, final Clock clock,
			final ISessionService sessionService) {
		super(repository, clock, sessionService);
	}

	/** Get the logger for subclasses.
	 * @return the logger */
	protected Logger getLogger() { return LOGGER; }

	/** Log an error.
	 * @param message   The error message
	 * @param throwable The exception */
	protected void logError(final String message, final Throwable throwable) {
		LOGGER.error(message, throwable);
	}

	/** Log a successful operation.
	 * @param operation  The operation name
	 * @param entityName The entity name
	 * @param entityId   The entity ID */
	protected void logOperation(final String operation, final String entityName, final Long entityId) {
		LOGGER.info("{} for entity '{}' (ID: {})", operation, entityName, entityId);
	}

	/** Log a warning.
	 * @param message The warning message
	 * @param args    Format arguments */
	protected void logWarning(final String message, final Object... args) {
		LOGGER.warn(message, args);
	}

	/** Validate that entity doesn't reference itself in hierarchy.
	 * @param entityId     The entity ID
	 * @param referenceId  The reference ID
	 * @param errorMessage The error message if validation fails
	 * @throws IllegalArgumentException if entity references itself */
	protected void validateNotSelfReference(final Long entityId, final Long referenceId, final String errorMessage) {
		if (entityId != null && entityId.equals(referenceId)) {
			throw new IllegalArgumentException(errorMessage);
		}
	}

	/** Validate that an entity has the required interface for this relation type.
	 * @param entity         The entity to validate
	 * @param interfaceClass The required interface class
	 * @throws IllegalArgumentException if entity doesn't implement the interface */
	protected void validateOwnership(final CProjectItem<?> entity, final Class<?> interfaceClass) {
		Check.notNull(entity, "Entity cannot be null");
		Check.notNull(entity.getId(), "Entity must be persisted");
		if (!interfaceClass.isInstance(entity)) {
			throw new IllegalArgumentException(
					String.format("Entity %s must implement %s", entity.getClass().getSimpleName(), interfaceClass.getSimpleName()));
		}
	}

	/** Validate that two entities belong to the same project.
	 * @param entity1 First entity
	 * @param entity2 Second entity
	 * @throws IllegalArgumentException if entities are in different projects */
	protected void validateSameProject(final CProjectItem<?> entity1, final CProjectItem<?> entity2) {
		Check.notNull(entity1, "First entity cannot be null");
		Check.notNull(entity2, "Second entity cannot be null");
		Check.notNull(entity1.getProject(), "First entity must have a project");
		Check.notNull(entity2.getProject(), "Second entity must have a project");
		if (!entity1.getProject().getId().equals(entity2.getProject().getId())) {
			throw new IllegalArgumentException(String.format("Entities must belong to the same project. %s is in project %d, %s is in project %d",
					entity1.getName(), entity1.getProject().getId(), entity2.getName(), entity2.getProject().getId()));
		}
	}
}
