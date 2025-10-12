package tech.derbent.api.views.dialogs;

import java.util.List;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.vaadin.flow.component.icon.Icon;
import tech.derbent.api.annotations.CFormBuilder;
import tech.derbent.api.components.CEnhancedBinder;
import tech.derbent.api.domains.CEntityDB;
import tech.derbent.api.interfaces.IContentOwner;
import tech.derbent.api.services.CAbstractEntityRelationService;
import tech.derbent.api.services.CAbstractService;
import tech.derbent.api.ui.notifications.CNotifications;
import tech.derbent.api.utils.CColorUtils;
import tech.derbent.api.utils.Check;

/** Abstract base class for relationship dialogs. This class provides common functionality for dialogs that manage relationships between two entity
 * types. It handles the common patterns of: - Entity selection via ComboBox - Role and permission management - Form validation with proper error
 * messages - Bidirectional relationship management Child classes must implement the abstract methods to provide specific behavior for their entity
 * types.
 * @param <RelationshipClass>  The relationship entity type (e.g., CUserProjectSettings)
 * @param <MainEntityClass>    The main entity type that owns the relationship
 * @param <RelatedEntityClass> The related entity type being selected */
public abstract class CDBRelationDialog<RelationshipClass extends CEntityDB<RelationshipClass>, MainEntityClass extends CEntityDB<MainEntityClass>,
		RelatedEntityClass extends CEntityDB<RelatedEntityClass>> extends CDBEditDialog<RelationshipClass> {

	protected static final Logger LOGGER = LoggerFactory.getLogger(CDBRelationDialog.class);
	private static final long serialVersionUID = 1L;
	protected CEnhancedBinder<RelationshipClass> binder;
	protected CAbstractService<RelatedEntityClass> detailService;
	protected CFormBuilder<RelationshipClass> formBuilder;
	protected final MainEntityClass mainEntity;
	protected CAbstractService<MainEntityClass> masterService;
	private final IContentOwner parentContent;
	private final CAbstractEntityRelationService<RelationshipClass> relationService;

	/** Constructor for relationship dialogs.
	 * @param parentContent   The parent content owner for context access
	 * @param relationship    The relationship entity to edit, or null for new
	 * @param mainEntity      The main entity that owns the relationship
	 * @param masterService   Service for the main entity
	 * @param detailService   Service for the related entity
	 * @param relationService
	 * @param onSave          Callback executed when relationship is saved
	 * @param isNew           True for new relationships, false for editing existing ones */
	public CDBRelationDialog(final IContentOwner parentContent, final RelationshipClass relationship, final MainEntityClass mainEntity,
			final CAbstractService<MainEntityClass> masterService, final CAbstractService<RelatedEntityClass> detailService,
			final CAbstractEntityRelationService<RelationshipClass> relationService, final Consumer<RelationshipClass> onSave, final boolean isNew) {
		super(relationship, onSave, isNew);
		this.parentContent = parentContent;
		this.mainEntity = mainEntity;
		this.detailService = detailService;
		this.masterService = masterService;
		this.relationService = relationService;
		// Create a binder specific to the relationship entity - child classes can override
		initializeBinder();
	}

	/** Returns the dialog title based on whether this is a new or edit operation. */
	@Override
	public String getDialogTitleString() { return isNew ? getNewDialogTitle() : getEditDialogTitle(); }

	protected abstract String getEditDialogTitle();
	protected abstract String getEditFormTitle();
	protected abstract List<String> getFormFields();

	@Override
	protected Icon getFormIcon() throws Exception { return CColorUtils.getIconForEntity(mainEntity); }

	/** Returns the form title based on whether this is a new or edit operation. */
	@Override
	protected String getFormTitleString() { return isNew ? getNewFormTitle() : getEditFormTitle(); }

	protected abstract String getNewDialogTitle();
	protected abstract String getNewFormTitle();

	/** Gets the parent content owner for accessing context data. This allows dialogs to access data from the parent page/container.
	 * @return the parent content owner */
	protected IContentOwner getParentContent() { return parentContent; }

	protected void initializeBinder() {
		@SuppressWarnings ("unchecked")
		final Class<RelationshipClass> entityClass = (Class<RelationshipClass>) getEntity().getClass();
		binder = new CEnhancedBinder<>(entityClass);
	}

	protected void performSave() {
		try {
			// If onSave callback is provided, use it (callback pattern)
			Check.notNull(getEntity(), "Entity must not be null when performing save");
			Check.notNull(relationService, "Relation service must not be null when performing save without callback");
			relationService.save(getEntity());
			if (onSave != null) {
				onSave.accept(getEntity());
			}
			LOGGER.info("Entity saved successfully using service: {}", getEntity().getId());
		} catch (final Exception e) {
			LOGGER.error("Error during save operation.");
			throw e;
		}
	}

	/** Default implementation of populateForm using the binder. Child classes can override. */
	@Override
	protected void populateForm() {
		Check.notNull(binder, "Binder must be initialized before populating the form");
		binder.readBean(getEntity());
	}

	/** Override the save method to use unified relationship save functionality with binder support.
	 * @throws Exception */
	@Override
	protected void save() throws Exception {
		try {
			LOGGER.debug("Saving relationship data: {}", getEntity());
			validateForm();
			// Write form data to entity using the binder
			if (binder != null) {
				binder.writeBean(getEntity());
			}
			// Delegate to service-specific or callback-based saving
			performSave();
			close();
			CNotifications.showSuccess(isNew ? getSuccessCreateMessage() : getSuccessUpdateMessage());
		} catch (final Exception e) {
			LOGGER.error("Error saving relationship", e);
			CNotifications.showError("Error: " + e.getMessage());
			throw e;
		}
	}

	/** Default implementation of setupDialog that creates the form using CFormBuilder. Child classes can override. */
	@Override
	public void setupDialog() throws Exception {
		super.setupDialog();
		@SuppressWarnings ("unchecked")
		final Class<RelationshipClass> entityClass = (Class<RelationshipClass>) getEntity().getClass();
		formBuilder = new CFormBuilder<>(parentContent, entityClass, binder, getFormFields());
		getDialogLayout().add(formBuilder.getFormLayout());
	}

	@Override
	protected void validateForm() {}
}
