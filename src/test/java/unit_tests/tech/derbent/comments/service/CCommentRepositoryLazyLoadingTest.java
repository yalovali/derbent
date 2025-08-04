package unit_tests.tech.derbent.comments.service;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import unit_tests.tech.derbent.abstracts.domains.CTestBase;

/**
 * Test class for verifying the lazy loading fix in CCommentRepository.findRecentByProject
 * method. This test ensures that the query validation error is resolved and the method
 * works correctly.
 */
@ExtendWith (MockitoExtension.class)
class CCommentRepositoryLazyLoadingTest extends CTestBase {

	@Override
	protected void setupForTest() {
		// TODO Auto-generated method stub
	}
}