package tech.derbent.activities.tests;

import tech.derbent.abstracts.tests.CGenericViewTest;
import tech.derbent.activities.domain.CActivity;
import tech.derbent.activities.view.CActivitiesView;

/**
 * CActivitiesViewGenericTest - Activities view test using generic superclass
 * Inherits all common test patterns: navigation, view loading, new item creation,
 * grid interactions, accessibility, and ComboBox testing.
 * 
 * Uses class annotations and metadata instead of magic strings.
 * Only takes screenshots on test failures to reduce overhead.
 */
public class CActivitiesViewGenericTest extends CGenericViewTest<CActivity> {

	@Override
	protected Class<?> getViewClass() {
		return CActivitiesView.class;
	}

	@Override
	protected Class<CActivity> getEntityClass() {
		return CActivity.class;
	}
}