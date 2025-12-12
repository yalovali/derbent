package tech.derbent.api.interfaces;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.shared.Registration;

/** Test class for verifying toString helper methods in IHas interfaces. Validates that each interface provides meaningful string representation
 * methods that implementing classes can use to build their toString() output. */
class IHasInterfacesToStringTest {

	/** Test implementation of IHasColor for testing purposes. */
	private static class TestHasColor implements IHasColor {

		private String color;

		public TestHasColor(final String color) {
			this.color = color;
		}

		@Override
		public String getColor() { return color; }

		@Override
		public void setColor(final String color) { this.color = color; }

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toColorString());
		}
	}

	/** Test implementation of IHasContentOwner for testing purposes. */
	private static class TestHasContentOwner implements IHasContentOwner {

		private IContentOwner contentOwner;

		public TestHasContentOwner(final IContentOwner contentOwner) {
			this.contentOwner = contentOwner;
		}

		@Override
		public IContentOwner getContentOwner() { return contentOwner; }

		@Override
		public void populateForm() throws Exception {}

		@Override
		public void setContentOwner(final IContentOwner parentContent) { this.contentOwner = parentContent; }

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toContentOwnerString());
		}
	}

	/** Test implementation of IHasDragControl for testing purposes. */
	private static class TestHasDragControl implements IHasDragControl {

		private boolean dragEnabled;
		private boolean dropEnabled;

		public TestHasDragControl(final boolean dragEnabled, final boolean dropEnabled) {
			this.dragEnabled = dragEnabled;
			this.dropEnabled = dropEnabled;
		}

		@Override
		public boolean isDragEnabled() { return dragEnabled; }

		@Override
		public boolean isDropEnabled() { return dropEnabled; }

		@Override
		public void setDragEnabled(final boolean enabled) { this.dragEnabled = enabled; }

		@Override
		public void setDropEnabled(final boolean enabled) { this.dropEnabled = enabled; }

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toDragControlString());
		}
	}

	/** Test implementation of IHasDragEnd for testing purposes. */
	private static class TestHasDragEnd implements IHasDragEnd<String> {

		@Override
		public Registration addDragEndListener(final ComponentEventListener<GridDragEndEvent<String>> listener) {
			return () -> {};
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toDragEndString());
		}
	}

	/** Test implementation of IHasDragStart for testing purposes. */
	private static class TestHasDragStart implements IHasDragStart<String> {

		@Override
		public Registration addDragStartListener(final ComponentEventListener<GridDragStartEvent<String>> listener) {
			return () -> {};
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toDragStartString());
		}
	}

	/** Test implementation of IHasDrop for testing purposes. */
	private static class TestHasDrop implements IHasDrop<String> {

		@Override
		public Registration addDropListener(final ComponentEventListener<GridDropEvent<?>> listener) {
			return () -> {};
		}

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toDropString());
		}
	}

	/** Test implementation of IHasIcon for testing purposes. */
	private static class TestHasIcon implements IHasIcon {

		private String color;
		private final String iconString;

		public TestHasIcon(final String iconString, final String color) {
			this.iconString = iconString;
			this.color = color;
		}

		@Override
		public String getColor() { return color; }

		@Override
		public String getIconString() { return iconString; }

		@Override
		public void setColor(final String color) { this.color = color; }

		@Override
		public String toString() {
			return String.format("%s[%s]", getClass().getSimpleName(), toColorString());
		}
	}

	/** Tests that IHasColor.toColorString() returns correct format. */
	@Test
	void testIHasColor_toColorString() {
		// Given: An object implementing IHasColor
		final TestHasColor testObj = new TestHasColor("#FF0000");
		// When: Calling toColorString()
		final String result = testObj.toColorString();
		// Then: Should return color in expected format
		assertNotNull(result);
		assertEquals("color=#FF0000", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("color=#FF0000"));
	}

	/** Tests that IHasContentOwner.toContentOwnerString() returns correct format. */
	@Test
	void testIHasContentOwner_toContentOwnerString_WithNullOwner() {
		// Given: An object implementing IHasContentOwner with null owner
		final TestHasContentOwner testObj = new TestHasContentOwner(null);
		// When: Calling toContentOwnerString()
		final String result = testObj.toContentOwnerString();
		// Then: Should return null indicator
		assertNotNull(result);
		assertEquals("contentOwner=null", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("contentOwner=null"));
	}

	/** Tests that IHasDragControl.toDragControlString() returns correct format. */
	@Test
	void testIHasDragControl_toDragControlString() {
		// Given: An object implementing IHasDragControl with both enabled
		final TestHasDragControl testObj = new TestHasDragControl(true, false);
		// When: Calling toDragControlString()
		final String result = testObj.toDragControlString();
		// Then: Should return drag/drop state in expected format
		assertNotNull(result);
		assertEquals("dragEnabled=true, dropEnabled=false", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("dragEnabled=true"));
		assertTrue(toStringResult.contains("dropEnabled=false"));
	}

	/** Tests that IHasDragEnd.toDragEndString() returns correct format. */
	@Test
	void testIHasDragEnd_toDragEndString() {
		// Given: An object implementing IHasDragEnd
		final TestHasDragEnd testObj = new TestHasDragEnd();
		// When: Calling toDragEndString()
		final String result = testObj.toDragEndString();
		// Then: Should return drag end support indicator
		assertNotNull(result);
		assertEquals("dragEndSupported=true", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("dragEndSupported=true"));
	}

	/** Tests that IHasDragStart.toDragStartString() returns correct format. */
	@Test
	void testIHasDragStart_toDragStartString() {
		// Given: An object implementing IHasDragStart
		final TestHasDragStart testObj = new TestHasDragStart();
		// When: Calling toDragStartString()
		final String result = testObj.toDragStartString();
		// Then: Should return drag start support indicator
		assertNotNull(result);
		assertEquals("dragStartSupported=true", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("dragStartSupported=true"));
	}

	/** Tests that IHasDrop.toDropString() returns correct format. */
	@Test
	void testIHasDrop_toDropString() {
		// Given: An object implementing IHasDrop
		final TestHasDrop testObj = new TestHasDrop();
		// When: Calling toDropString()
		final String result = testObj.toDropString();
		// Then: Should return drop support indicator
		assertNotNull(result);
		assertEquals("dropSupported=true", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("dropSupported=true"));
	}

	/** Tests that IHasIcon.toColorString() returns correct format with icon and color. */
	@Test
	void testIHasIcon_toColorString() {
		// Given: An object implementing IHasIcon
		final TestHasIcon testObj = new TestHasIcon("vaadin:user", "#00FF00");
		// When: Calling toColorString()
		final String result = testObj.toColorString();
		// Then: Should return icon and color in expected format
		assertNotNull(result);
		assertEquals("icon=vaadin:user, color=#00FF00", result);
		// And: Should be usable in toString()
		final String toStringResult = testObj.toString();
		assertTrue(toStringResult.contains("icon=vaadin:user"));
		assertTrue(toStringResult.contains("color=#00FF00"));
	}
}
