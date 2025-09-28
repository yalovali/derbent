package tech.derbent.api.interfaces;
public interface IHasContentOwner {

	IContentOwner getContentOwner();
	void setContentOwner(IContentOwner parentContent);
	void populateForm(Object entity);
	void populateForm();
}
