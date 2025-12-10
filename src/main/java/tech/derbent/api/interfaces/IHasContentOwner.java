package tech.derbent.api.interfaces;

public interface IHasContentOwner {

	IContentOwner getContentOwner();
	void populateForm() throws Exception;
	void setContentOwner(IContentOwner parentContent);
	
	/**
	 * Returns a string representation of this object including content owner information.
	 * This method can be used by implementing classes to build their toString() output.
	 * 
	 * @return a string representation including content owner information
	 */
	default String toContentOwnerString() {
		final IContentOwner owner = getContentOwner();
		return String.format("contentOwner=%s", 
			owner != null ? owner.getClass().getSimpleName() : "null");
	}
}
