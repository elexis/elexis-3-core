package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IOrderService;

@Component
public class OrderServiceHolder {
	
	private static IOrderService orderService;
	
	@Reference
	public void setOrderService(IOrderService orderService){
		OrderServiceHolder.orderService = orderService;
	}
	
	public static IOrderService get(){
		if (orderService == null) {
			throw new IllegalStateException("No IOrderService available");
		}
		return orderService;
	}
}
