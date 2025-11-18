package tech.derbent.api.interfaces;

public interface IHasContentOwner {

	IContentOwner getContentOwner();
	void populateForm() throws Exception;
	void setContentOwner(IContentOwner parentContent);
}
