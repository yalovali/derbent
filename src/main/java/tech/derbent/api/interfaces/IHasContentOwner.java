package tech.derbent.api.interfaces;
public interface IHasContentOwner {

	IContentOwner getContentOwner();
	void setContentOwner(IContentOwner parentContent);
}
