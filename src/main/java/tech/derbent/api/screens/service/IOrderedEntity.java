package tech.derbent.api.screens.service;
public interface IOrderedEntity {

	Integer getItemOrder();
	void setItemOrder(Integer previousOrder);
}
