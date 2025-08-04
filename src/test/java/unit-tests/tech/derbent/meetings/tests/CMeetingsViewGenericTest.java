package tech.derbent.meetings.tests;

import tech.derbent.abstracts.tests.CGenericViewTest;
import tech.derbent.meetings.domain.CMeeting;
import tech.derbent.meetings.view.CMeetingsView;

/**
 * CMeetingsViewGenericTest - Meetings view test using generic superclass Inherits all common test patterns: navigation,
 * view loading, new item creation, grid interactions, accessibility, and ComboBox testing.
 * 
 * Uses class annotations and metadata instead of magic strings. Only takes screenshots on test failures to reduce
 * overhead.
 */
public class CMeetingsViewGenericTest extends CGenericViewTest<CMeeting> {

    @Override
    protected Class<?> getViewClass() {
        return CMeetingsView.class;
    }

    @Override
    protected Class<CMeeting> getEntityClass() {
        return CMeeting.class;
    }
}