package tech.derbent.api.interfaces;

/** Interface for components that have a content owner reference.
 * <p>
 * This interface provides content owner management functionality and a helper
 * method for including content owner information in toString() implementations.
 * </p> */
public interface IHasContentOwner {

	IContentOwner getContentOwner();
	void populateForm() throws Exception;
	void setContentOwner(IContentOwner parentContent);
	
	/** Returns a string representation of the content owner for debugging.
	 * <p>
	 * This default method provides a helper for implementing classes to include
	 * content owner information in their toString() methods.
	 * </p>
	 * @return a string representation of the content owner, e.g., "contentOwner=null" or "contentOwner=ClassName@hashcode" */
	default String toContentOwnerString() {
		final IContentOwner owner = getContentOwner();
		return "contentOwner=" + (owner == null ? "null" : owner.toString());
	}
}
