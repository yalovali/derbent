package tech.derbent.plm.documenttypes.service;
import org.springframework.stereotype.Service;
import tech.derbent.api.companies.domain.CCompany;
import tech.derbent.api.config.CSpringContext;
import tech.derbent.api.entityOfCompany.service.CEntityOfCompanyService;
import tech.derbent.api.projects.domain.CProject;
import tech.derbent.api.registry.CEntityRegistry;
import tech.derbent.api.screens.service.CInitializerServiceBase;
import tech.derbent.plm.documenttypes.domain.CDocumentType;

/** Initializer service for CDocumentType entities. Provides sample data initialization for document types. */
@Service
public class CDocumentTypeInitializerService extends CInitializerServiceBase {

	private static final Class<?> clazz = CDocumentType.class;

	/** Initialize sample document types for a project.
	 * @param project the project to initialize document types for
	 * @param minimal if true, create minimal dataset (3 types), otherwise full dataset (10 types)
	 * @throws Exception if initialization fails */
	public static void initializeSample(final CProject<?> project, final boolean minimal) throws Exception {
		final CCompany company = project.getCompany();
		final String[][] nameAndDescriptions = minimal ? new String[][] {
				{
						"Specification", "Requirements and specifications documents"
				}, {
						"Design Document", "Technical design and architecture documents"
				}, {
						"Meeting Minutes", "Meeting notes and minutes"
				}
		} : new String[][] {
				{
						"Specification", "Requirements and specifications documents"
				}, {
						"Design Document", "Technical design and architecture documents"
				}, {
						"Meeting Minutes", "Meeting notes and minutes"
				}, {
						"Test Report", "Test plans, cases, and results"
				}, {
						"User Manual", "End user documentation and guides"
				}, {
						"API Documentation", "API specifications and reference docs"
				}, {
						"Contract", "Legal and contractual documents"
				}, {
						"Presentation", "Slide decks and presentations"
				}, {
						"Diagram", "Architecture diagrams, flowcharts, wireframes"
				}, {
						"Other", "Miscellaneous documents"
				}
		};
		initializeCompanyEntity(nameAndDescriptions, (CEntityOfCompanyService<
				?>) CSpringContext.getBean(CEntityRegistry.getServiceClassForEntity(clazz)),
				company, minimal, null);
	}
}
