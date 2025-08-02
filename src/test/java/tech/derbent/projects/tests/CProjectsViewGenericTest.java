package tech.derbent.projects.tests;

import tech.derbent.abstracts.tests.CGenericViewTest;
import tech.derbent.projects.domain.CProject;
import tech.derbent.projects.view.CProjectsView;

/**
 * CProjectsViewGenericTest - Projects view test using generic superclass
 * Inherits all common test patterns: navigation, view loading, new item creation,
 * grid interactions, accessibility, and ComboBox testing.
 * 
 * Uses class annotations and metadata instead of magic strings.
 * Only takes screenshots on test failures to reduce overhead.
 */
public class CProjectsViewGenericTest extends CGenericViewTest<CProject> {

	@Override
	protected Class<?> getViewClass() {
		return CProjectsView.class;
	}

	@Override
	protected Class<CProject> getEntityClass() {
		return CProject.class;
	}
}